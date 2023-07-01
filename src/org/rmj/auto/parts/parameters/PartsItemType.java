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
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * Date Created: 06-30-2023
 */
public class PartsItemType {
    private final String MASTER_TABLE = "inv_type";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
    private CachedRowSet poOriginalMaster;
    
    public PartsItemType(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
            case 1://sInvTypCd
            case 2://sDescript
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 3://cRecdStat
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
                                                    , "Type Code»Description"
                                                    , "sInvTypCd»sDescript"
                                                    , "sInvTypCd»sDescript"
                                                    , 0);
        
       
        if (loJSON == null){
            psMessage = "No record found/selected.";
            return false;
        } else {
            if (OpenRecord((String) loJSON.get("sInvTypCd")) ){
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
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "sInvTypCd = " + SQLUtil.toSQL(fsValue));
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
        try {
            if (poMaster != null){
                poOriginalMaster = (CachedRowSet) poMaster.createCopy();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartsItemType.class.getName()).log(Level.SEVERE, null, ex);
        }
        pnEditMode = EditMode.UPDATE;
        return true;        
    }
    
    /**
    Updates the record status of a Item Type.
    @param fsValue The value to identify the Item Type record.
    @param fbValue The new status value. True for activated, false for deactivated.
    @return True if the record status was successfully updated, false otherwise.
    */
    public boolean UpdateRecordStatus(String fsValue, boolean fbValue){
        if (pnEditMode != EditMode.READY){
            psMessage = "Invalid update mode detected.";
            return false;
        }
            
        try {
            if (poMaster != null){
                poOriginalMaster = (CachedRowSet) poMaster.createCopy();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PartsItemType.class.getName()).log(Level.SEVERE, null, ex);
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
                                        "sInvTypCd = " + SQLUtil.toSQL(fsValue));
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
            // Update the original state of the table
            poOriginalMaster = (CachedRowSet) poMaster.createCopy();
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        if (fbValue){
            psMessage = "Item Type Successfully Activated.";
        } else {
            psMessage = "Item Type Successfully Deactivated.";
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
                //poMaster.updateString("sInvTypCd",MiscUtil.getNextCode(MASTER_TABLE, "sInvTypCd", false, poGRider.getConnection(), psBranchCd) );                                                             
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
                                            "sInvTypCd = " + SQLUtil.toSQL(poOriginalMaster.getString("sInvTypCd")));
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
            // Update the original state of the table
            poOriginalMaster = (CachedRowSet) poMaster.createCopy();
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    private String getSQ_Master(){
        return "SELECT" + 
                    " sInvTypCd" + //1
                    ", IFNULL(sDescript,'') sDescript" + //2
                    ", cRecdStat" + //3
                    ", sModified" + //4
                    ", dModified" + //5
                " FROM inv_type ";
    }
    
    private String getSQ_InvCategory(){
        return "SELECT" + 
                    " sCategrCd" + //1
                    ", IFNULL(sDescript,'') sDescript" + //2
                    ", IFNULL(sInvTypCd,'') sInvTypCd" + //3
                " FROM inventory_category " ;
    }
    
    
    private boolean isEntryOK() throws SQLException{
        poMaster.first();
        poOriginalMaster.first();
                
        if (poMaster.getString("sInvTypCd").isEmpty()){
            psMessage = "Item Type Code is not set.";
            return false;
        }
        
        if (poMaster.getString("sDescript").isEmpty()){
            psMessage = "Item Type Description is not set.";
            return false;
        }
        
        String lsSQL = "";
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(getSQ_Master(), "sInvTypCd = " + SQLUtil.toSQL(poMaster.getString("sInvTypCd")) +
                                                        " AND sInvTypCd <> " + SQLUtil.toSQL(poOriginalMaster.getString("sInvTypCd")));
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Item Type Code.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        lsSQL = MiscUtil.addCondition(getSQ_Master(), "sDescript = " + SQLUtil.toSQL(poMaster.getString("sDescript")) +
                                                        " AND sInvTypCd <> " + SQLUtil.toSQL(poMaster.getString("sInvTypCd"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Item Type Description.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        /*CHECK WHEN Item Type IS ALREADY LINKED TO INVENTORY Category*/
        lsSQL = getSQ_InvCategory();
        lsSQL = MiscUtil.addCondition(lsSQL, "sInvTypCd = " + SQLUtil.toSQL(poMaster.getString("sInvTypCd")) ); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Item Type is already used in Item Category. Please contact system administrator to address this issue.";
            MiscUtil.close(loRS);        
            return false;
        }
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
