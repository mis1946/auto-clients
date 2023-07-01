/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.parts.parameters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
 * @author Arsiela
 * Date Created: 06-29-2023
 */
public class PartsMeasure {
    private final String MASTER_TABLE = "measure";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
    
    public PartsMeasure(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;                       
    }  
    
    public int getEditMode(){
        return pnEditMode;
    }
    
    public String getMessage(){
         return psMessage;         
    }
    
    public void setWithUI(boolean fbValue) {
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    public int getItemCount() throws SQLException{
        poMaster.last();
        return poMaster.getRow();
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poMaster.first();
        
        switch (fnIndex){            
            case 2://sMeasurNm      
            case 3://sShortDsc
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 4://cRecdStat
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
                break;
                                                            
        }
    }
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poMaster, fsIndex), foValue);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    
    public Object getMaster(int fnIndex) throws SQLException{
        poMaster.first();
        return poMaster.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poMaster.absolute(fnRow);
        return poMaster.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);       
            poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);    
            poMaster.insertRow();
            poMaster.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    //for autoloading list
//    public boolean LoadList() throws SQLException{
//        if (poGRider == null){
//            psMessage = "Application driver is not set.";
//            return false;
//        }
//        
//        psMessage = "";
//        
//        String lsSQL;
//        ResultSet loRS;
//        RowSetFactory factory = RowSetProvider.newFactory();
//        
//        //open master
//        loRS = poGRider.executeQuery(getSQ_Master());
//        poMaster = factory.createCachedRowSet();
//        poMaster.populate(loRS);
//        MiscUtil.close(loRS);
//        
//        return true;
//    }
    
    public boolean searchRecord() throws SQLException{
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , ""
                                                    , "Measure ID»Measure Name»Measure Abbrev"
                                                    , "sMeasurID»sMeasurNm»sShortDsc"
                                                    , "sMeasurID»sMeasurNm»sShortDsc"
                                                    , 0);
        
       
        if (loJSON == null){
            psMessage = "No record found/selected.";
            return false;
        } else {
            if (OpenRecord((String) loJSON.get("sMeasurID")) ){
            }else {
                psMessage = "No record found/selected.";
                return false;
            }
        }
               
        return true;
    }
    
    public boolean OpenRecord(String fsValue){
        try {
            String lsSQL = "";
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "sMeasurID = " + SQLUtil.toSQL(fsValue));
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }    
    
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        return true;        
    }
    
    /**
    Updates the record status of a Measure.
    @param fsValue The value to identify the Measure record.
    @param fbValue The new status value. True for activated, false for deactivated.
    @return True if the record status was successfully updated, false otherwise.
    */
    public boolean UpdateRecordStatus(String fsValue, boolean fbValue){
        if (pnEditMode != EditMode.READY){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            String lsSQL = "";
            //update  
            if (fbValue){
                poMaster.updateObject("cRecdStat", 1);
            } else {
                poMaster.updateObject("cRecdStat", 0);
            }
            poMaster.updateString("sModified", poGRider.getUserID());
            poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
            poMaster.updateRow();
            
            lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                        MASTER_TABLE, 
                                        "", 
                                        "sMeasurID = " + SQLUtil.toSQL(fsValue));
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            
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
        
        if (fbValue){
            psMessage = "Measure Successfully Activated.";
        } else {
            psMessage = "Measure Successfully Deactivated.";
        }
        pnEditMode = EditMode.UNKNOWN;
        return true;     
    }
    
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            String lsSQL = "";
            if (pnEditMode == EditMode.ADDNEW){ //add
                System.out.println(MiscUtil.getNextCode(MASTER_TABLE, "sMeasurID", false, poGRider.getConnection(), psBranchCd) );
                //return true;
                poMaster.updateString("sMeasurID",MiscUtil.getNextCode(MASTER_TABLE, "sMeasurID", false, poGRider.getConnection(), psBranchCd) );                                                             
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "");
            } else { //update  
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "", 
                                            "sMeasurID = " + SQLUtil.toSQL((String) getMaster("sMeasurID")));
            }
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            
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
    
    private String getSQ_Master(){
        return "SELECT" + 
                    " sMeasurID" + //1
                    ", IFNULL(sMeasurNm,'') sMeasurNm" + //2
                    ", IFNULL(sShortDsc,'') sShortDsc" + //3
                    ", cRecdStat" + //4
                    ", sModified" + //5
                    ", dModified" + //6
                " FROM measure ";
    }
    
    private boolean isEntryOK() throws SQLException{
        poMaster.first();
        
        if (poMaster.getString("sMeasurNm").isEmpty()){
            psMessage = "Measure Name is not set.";
            return false;
        }
       
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL, "sMeasurNm = " + SQLUtil.toSQL(poMaster.getString("sMeasurNm")) +
                                                " AND sMeasurID <> " + SQLUtil.toSQL(poMaster.getString("sMeasurID"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Measure Description.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        /*CHECK WHEN Measure IS ALREADY LINKED TO INVENTORY MASTER/ITEM INFORMATION*/
//        lsSQL = getSQ_VhclDesc();
//        lsSQL = MiscUtil.addCondition(lsSQL, "sMeasurID = " + SQLUtil.toSQL(poMaster.getString("sMeasurID")) ); 
//        loRS = poGRider.executeQuery(lsSQL);
//        if (MiscUtil.RecordCount(loRS) > 0){
//            psMessage = "Measure is already used in Item Information. Please contact system administrator to address this issue.";
//            MiscUtil.close(loRS);        
//            return false;
//        }
                       
        return true;
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poMaster.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poMaster.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poMaster.getMetaData().getColumnType(lnCtr));
            if (poMaster.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poMaster.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poMaster.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }
}
