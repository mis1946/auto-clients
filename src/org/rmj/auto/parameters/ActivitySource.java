/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.parameters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;

/**
 *
 * @author User
 */
public class ActivitySource {
    private final String MASTER_TABLE = "event_type";
    private GRider poGRider;
    private String psBranchCd;
    public String psMessage;
    private int pnEditMode;
    private boolean pbWithParent;
    private MasterCallback poCallback;
       
    private boolean pbWithUI;
    
    private CachedRowSet poActSrc;

    public ActivitySource(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
        
        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;                       
    }    
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    public int getEditMode(){
        return pnEditMode;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public int getItemCount() throws SQLException{
        poActSrc.last();
        return poActSrc.getRow();
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poActSrc.first();
        
        switch (fnIndex){            
            case 1://sActTypID
            case 2://sActTypDs
            case 3://sEventTyp
                poActSrc.updateObject(fnIndex, (String) foValue);
                poActSrc.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
                                                            
        }
    }
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poActSrc, fsIndex), foValue);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poActSrc, fsIndex));
    }
    
    public Object getMaster(int fnIndex) throws SQLException{
        poActSrc.first();
        return poActSrc.getObject(fnIndex);
    }
    
    /**
    * Switches the edit mode to update, allowing changes to be made to the activity source record.
    * @return True to indicate that the edit mode has been switched to update.
    */
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        return true;        
    }
    
    /**
    * Creates a new record for an activity source.
    * @return True if a new record is successfully created, otherwise sets an error message and returns false.
    */
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_ActEvent(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poActSrc = factory.createCachedRowSet();
            poActSrc.populate(loRS);
            MiscUtil.close(loRS);
            
            poActSrc.last();
            poActSrc.moveToInsertRow();
            
            MiscUtil.initRowSet(poActSrc);       
            poActSrc.updateString("cRecdStat", RecordStatus.ACTIVE);    
            poActSrc.insertRow();
            poActSrc.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    /**
    * Saves the changes made to an activity source record.
    * @return True if the record is successfully saved, otherwise sets an error message and returns false.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            String lsSQL = "";
            
            if (!pbWithParent) poGRider.beginTrans();
            if (pnEditMode == EditMode.ADDNEW){ //add
                System.out.println(SQLUtil.toSQL(MiscUtil.getNextCode(MASTER_TABLE, "sActTypID", false, poGRider.getConnection(), psBranchCd)));
                poActSrc.updateString("sActTypID",MiscUtil.getNextCode(MASTER_TABLE, "sActTypID", true, poGRider.getConnection(), psBranchCd) );                                                             
                poActSrc.updateString("sEntryByx", poGRider.getUserID());
                poActSrc.updateObject("dEntryDte", (Date) poGRider.getServerDate());                
                poActSrc.updateRow();                
                lsSQL = MiscUtil.rowset2SQL(poActSrc, MASTER_TABLE, "");
            }     
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }
                                    
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                psMessage = poGRider.getErrMsg();
                if (!pbWithParent) poGRider.rollbackTrans();
                return false;
            }
            if (!pbWithParent) poGRider.commitTrans();
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    /**
    * Searches for event types and retrieves them from a database.
    * @return True if event types are found; otherwise, false with a message.
    * @throws SQLException if a database error occurs.
    */
    public boolean searchEventType() throws SQLException {
        
        String lsSQL = MiscUtil.addCondition(getSQ_ActEvent(), " cRecdStat = '1' ");

        ResultSet loRS;       
        loRS = poGRider.executeQuery(lsSQL);
        System.out.println(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider,
                lsSQL,
                "",
                "Event Type»Source",
                "sEventTyp»sActTypDs",
                "sEventTyp»sActTypDs",
                0);
        if (loJSON == null) {
            psMessage = "No record found/selected.";
            return false;
        }         
        return true;
    }
    
    private String getSQ_ActEvent() {
        return " SELECT"
                + " sActTypID "
                + " ,sActTypDs "
                + " ,sEventTyp "
                + " ,cRecdStat "
                + " ,sEntryByx "
                + " ,dEntryDte "
                + " FROM event_type";
    }
    
    /**
    * Validates the entry for the activity type.
    * @return True if the entry is valid, false otherwise.
    */
    private boolean isEntryOK() throws SQLException{
        poActSrc.first();

        if (poActSrc.getString("sActTypDs").isEmpty()){
            psMessage = "Activity type Description is not set.";
            return false;
        }
        
        if (poActSrc.getString("sEventTyp").isEmpty()){
            psMessage = "Activity type is not set.";
            return false;
        }
        
        String lsSQL = getSQ_ActEvent();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL, "sActTypDs = " + SQLUtil.toSQL(poActSrc.getString("sActTypDs")) +
                                                " AND sEventTyp = " + SQLUtil.toSQL(poActSrc.getString("sEventTyp"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Activity Type Description.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        return true;
    }
}
