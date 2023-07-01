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
public class PartsItemLocation {
    private final String MASTER_TABLE = "item_location";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
    
    public PartsItemLocation(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
            case 2://sLocatnDs       
            case 6://sWHouseID       
            case 7://sWHouseNm       
            case 8://sSectnIDx       
            case 9://sSectnNme       
            case 10://sBinIDxxx       
            case 11://sBinNamex
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
                                                    , "Location ID»Item Location"
                                                    , "sLocatnID»sLocatnDs"
                                                    , "sLocatnID»sLocatnDs"
                                                    , 0);
        
       
        if (loJSON == null){
            psMessage = "No record found/selected.";
            return false;
        } else {
            if (OpenRecord((String) loJSON.get("sLocatnID")) ){
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
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "sLocatnID = " + SQLUtil.toSQL(fsValue));
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
    Updates the record status of a bin.
    @param fsValue The value to identify the bin record.
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
                                        "sLocatnID = " + SQLUtil.toSQL(fsValue));
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
            psMessage = "Item Location Successfully Activated.";
        } else {
            psMessage = "Item Location Successfully Deactivated.";
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
                System.out.println(MiscUtil.getNextCode(MASTER_TABLE, "sLocatnID", false, poGRider.getConnection(), psBranchCd) );
                //return true;
                poMaster.updateString("sLocatnID",MiscUtil.getNextCode(MASTER_TABLE, "sLocatnID", false, poGRider.getConnection(), psBranchCd) );                                                             
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sWHouseNm»sSectnNme»sBinNamex");
            } else { //update  
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sWHouseNm»sSectnNme»sBinNamex", 
                                            "sLocatnID = " + SQLUtil.toSQL((String) getMaster("sLocatnID")));
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
                    " a.sLocatnID" + //1
                    ", IFNULL(a.sLocatnDs,'') sLocatnDs" + //2
                    ", a.cRecdStat" + //3
                    ", a.sModified" + //4
                    ", a.dModified" + //5
                    ", IFNULL(a.sWHouseID,'') sWHouseID" + //6
                    ", IFNULL(b.sWHouseNm,'') sWHouseNm" + //7
                    ", IFNULL(a.sSectnIDx,'') sSectnIDx" + //8
                    ", IFNULL(c.sSectnNme,'') sSectnNme" + //9
                    ", IFNULL(a.sBinIDxxx,'') sBinIDxxx" + //10
                    ", IFNULL(d.sBinNamex,'') sBinNamex" + //11
                " FROM item_location a" +
                " LEFT JOIN warehouse b ON b.sWHouseID = a.sWHouseID " +
                " LEFT JOIN section c ON c.sSectnIDx = a.sSectnIDx " +
                " LEFT JOIN bin d ON d.sBinIDxxx = a.sBinIDxxx ";
    }
    
    private String getSQ_SearchWarehouse(){
        return "SELECT" + 
                    " sWHouseID" + //1
                    ", IFNULL(sWHouseNm,'') sWHouseNm" + //2
                    ", cRecdStat" + //3
                " FROM warehouse ";
    }
    
    private String getSQ_SearchSection(){
        return "SELECT" + 
                    " sSectnIDx" + //1
                    ", IFNULL(sSectnNme,'') sSectnNme" + //2
                    ", cRecdStat" + //3
                " FROM section ";
    }
    
    private String getSQ_SearchBin(){
        return "SELECT" + 
                    " sBinIDxxx" + //1
                    ", IFNULL(sBinNamex,'') sBinNamex" + //2
                    ", cRecdStat" + //3
                " FROM bin ";
    }
    
    /**
     * For searching Warehouse when key is pressed.
     * @param fsValue the search value for the Warehouse.
     * @return {@code true} if a matching Warehouse is found, {@code false} otherwise.
    */
    public boolean searchWarehouse(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchWarehouse();
        lsSQL = (MiscUtil.addCondition(lsSQL, " sWHouseNm LIKE " + SQLUtil.toSQL(fsValue + "%"))  +
                                                  " AND cRecdStat = '1'  "  +
                                                  " GROUP BY sWHouseID " );
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sWHouseID", loRS.getString("sWHouseID"));
                setMaster("sWHouseNm", loRS.getString("sWHouseNm"));
            } else {
                psMessage = "No record found.";
                setMaster("sWHouseID","");
                setMaster("sWHouseNm","");
                setMaster("sLocatnDs", "");
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Warehouse", "sWHouseNm");
            
            if (loJSON != null){
                setMaster("sWHouseID", (String) loJSON.get("sWHouseID"));
                setMaster("sWHouseNm", (String) loJSON.get("sWHouseNm"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sWHouseID","");
                setMaster("sWHouseNm","");
                setMaster("sLocatnDs", "");
                return false;    
            }
        } 
        
        return true;
    }
    
    /**
     * For searching Section when key is pressed.
     * @param fsValue the search value for the Section.
     * @return {@code true} if a matching Section is found, {@code false} otherwise.
    */
    public boolean searchSection(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchSection();
        lsSQL = (MiscUtil.addCondition(lsSQL, " sSectnNme LIKE " + SQLUtil.toSQL(fsValue + "%"))  +
                                                  " AND cRecdStat = '1'  "  +
                                                  " GROUP BY sSectnIDx " );
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sSectnIDx", loRS.getString("sSectnIDx"));
                setMaster("sSectnNme", loRS.getString("sSectnNme"));
            } else {
                psMessage = "No record found.";
                setMaster("sSectnIDx","");
                setMaster("sSectnNme","");
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Section", "sSectnNme");
            
            if (loJSON != null){
                //lsSectionNm = (String) loJSON.get("sSectnNme");
                setMaster("sSectnIDx", (String) loJSON.get("sSectnIDx"));
                setMaster("sSectnNme", (String) loJSON.get("sSectnNme"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sSectnIDx","");
                setMaster("sSectnNme","");
                //lsSectionNm = "";
                return false;    
            }
        }   
        
        return true;
    }
    
    /**
     * For searching Bin when key is pressed.
     * @param fsValue the search value for the Bin.
     * @return {@code true} if a matching Bin is found, {@code false} otherwise.
    */
    public boolean searchBin(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchBin();
        lsSQL = (MiscUtil.addCondition(lsSQL, " sBinNamex LIKE " + SQLUtil.toSQL(fsValue + "%"))  +
                                                  " AND cRecdStat = '1'  "  +
                                                  " GROUP BY sBinIDxxx " );
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sBinIDxxx", loRS.getString("sBinIDxxx"));
                setMaster("sBinNamex", loRS.getString("sBinNamex"));
            } else {
                psMessage = "No record found.";
                setMaster("sBinIDxxx","");
                setMaster("sBinNamex","");
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Bin", "sBinNamex");
            
            if (loJSON != null){
                //lsBinNm = (String) loJSON.get("sBinNamex");
                setMaster("sBinIDxxx", (String) loJSON.get("sBinIDxxx"));
                setMaster("sBinNamex", (String) loJSON.get("sBinNamex"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sBinIDxxx","");
                setMaster("sBinNamex","");
                //lsBinNm = "";
                return false;    
            }
        }   
        
        return true;
    }
    
    private boolean isEntryOK() throws SQLException{
        poMaster.first();
        
        if (poMaster.getString("sWHouseID").isEmpty()){
            psMessage = "Warehouse is not set.";
            return false;
        }
        
        if (poMaster.getString("sLocatnDs").isEmpty()){
            psMessage = "Location Description is not set.";
            return false;
        }
       
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sLocatnDs = " + SQLUtil.toSQL(poMaster.getString("sLocatnDs")) +
                                                " AND a.sLocatnID <> " + SQLUtil.toSQL(poMaster.getString("sLocatnID"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Item Location.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        /*CHECK WHEN Location IS ALREADY LINKED TO INVENTORY MASTER/ITEM INFORMATION*/
//        lsSQL = getSQ_VhclDesc();
//        lsSQL = MiscUtil.addCondition(lsSQL, "sLocatnID = " + SQLUtil.toSQL(poMaster.getString("sLocatnID")) ); 
//        loRS = poGRider.executeQuery(lsSQL);
//        if (MiscUtil.RecordCount(loRS) > 0){
//            psMessage = "Location is already used in Item Information. Please contact system administrator to address this issue.";
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
