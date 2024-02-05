/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.clients.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.rmj.auto.json.FormStateManager;

/**
 *
 * @author User
 */
public class ClientAddress {
    private final String ADDRESS_TABLE = "Client_Address";
    private final String DEFAULT_DATE = "1900-01-01";
    
    List<Integer> index = new ArrayList<>();
    List<String> value = new ArrayList<>();
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
//    private LMasDetTrans poListener;
    //private ClientMaster poMaster;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psClientID;
    
    public CachedRowSet poAddress;
    private CachedRowSet poOriginalAddress;
    
    public ClientAddress(GRider foGrider, String fsBranchCd, boolean fbWithparent){
        poGRider = foGrider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithparent;   
    }
    
    public int getEditMode(){
        return pnEditMode;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public void setClientID(String fsValue) {
        psClientID = fsValue;
    }
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
//    //for setting address in the text fields
//    public void setAddress(int fnIndex, Object foValue) throws SQLException{
//        poAddress.first();
//        
//        switch (fnIndex){
//            case 3://sHouseNox
//            case 4://sAddressx
//            case 5://sTownIDxx
//            case 6://sBrgyUDxx
//            case 7://sZippCode
////            case 11://sRemarksx
//            case 24://sBrgyName
//            case 25://sTownName
////            case 15:
////            case 16:
//                poAddress.updateObject(fnIndex, (String) foValue);
//                poAddress.updateRow();
//                //if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));
//                break;
//            case 12://cOfficexx
//            case 13://cProvince
//            case 14://cPrimaryx
//            case 17://cCurrentx
//            case 18://cRecdStat
//                if (foValue instanceof Integer)
//                    poAddress.updateInt(fnIndex, (int) foValue);
//                else 
//                    poAddress.updateInt(fnIndex, 0);
//                
//                poAddress.updateRow();
//                //if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));
//                break;
//        }               
//    }        
    
//    public void setAddress(String fsIndex, Object foValue) throws SQLException{
//        setAddress(MiscUtil.getColumnIndex(poAddress, fsIndex), foValue);
//    }
    
    public Object getAddress(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poAddress.absolute(fnRow);
        return poAddress.getObject(fnIndex);
    }
    
    public Object getAddress(int fnRow, String fsIndex) throws SQLException{
        if (getItemCount()== 0 || fnRow > getItemCount()) return null;
        return getAddress(fnRow, MiscUtil.getColumnIndex(poAddress, fsIndex));
    }
    
    public Object getAddress(String fsIndex) throws SQLException{
        
        return getAddress(MiscUtil.getColumnIndex(poAddress, fsIndex));
    }
    
    public Object getAddress(int fnIndex) throws SQLException{
        poAddress.first();
        return poAddress.getObject(fnIndex);
    }
    //for setting address in the table
    public void setAddressTable(int fnRow, int fnIndex, Object foValue) throws SQLException{
        
        poAddress.absolute(fnRow);        
        switch (fnIndex){
            case 3://sHouseNox
            case 4://sAddressx
            case 5://sTownIDxx
            case 6://sBrgyUDxx
            case 7://sZippCode
            case 11://sRemarksx
            case 23://sProvName
            case 24://sBrgyName
            case 25://sTownName
            case 26://sProvIDxx
//            case 15:
//            case 16:
                poAddress.updateObject(fnIndex, (String) foValue);
                poAddress.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));              
                break;
            case 12://cOfficexx
            case 13://cProvince
            case 14://cPrimaryx
            case 17://cCurrentx
            case 18://cRecdStat
                if (foValue instanceof Integer)
                    poAddress.updateInt(fnIndex, (int) foValue);
                else 
                    poAddress.updateInt(fnIndex, 0);
                
                poAddress.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));               
                break;
        }               
    }
    
    //for setting address in the table
    public void setAddressTable(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setAddressTable(fnRow,MiscUtil.getColumnIndex(poAddress, fsIndex), foValue);
    }
    
    public int getItemCount() throws SQLException{
        if (poAddress != null){
            poAddress.last();
            return poAddress.getRow();
        }else{
            return 0;
        }              
    }
    
    /**
    * Removes a specific address, ensuring at least one primary address remains.
    *
    * @param fnRow The row number of the address to be removed.
    * @return True if removed, false if it's the only primary address.
    */
    public boolean removeAddress(int fnRow) throws SQLException{
//        if (pnEditMode != EditMode.ADDNEW) {
//            psMessage = "This feature is only for new entries.";
//            return false;
//        }
                
        if (getItemCount() == 0) {
            psMessage = "No address to delete.";
            return false;
        }
        
        poAddress.absolute(fnRow);
        poAddress.deleteRow();
        
//        int lnCtr;
//        int lnRow = getItemCount();
//        String lsPrimary = "1";
//                
//        boolean lbPrimary = false;
        //check if there are other primary
//        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){            
//            if ((getAddress(lnCtr, "cPrimaryx").equals(lsPrimary)) && lnCtr != fnRow) {   
//                lbPrimary = true;
//                break;
//            }
//        }
        //if true proceed to delete
        //if (lbPrimary){
//            poAddress.absolute(fnRow);
//            poAddress.deleteRow();
//            for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){
//                if (fsValue.equals((String) getAddress(lnCtr, "sInctveCD"))){           
//                    poAddress.absolute(lnCtr);
//                    poAddress.deleteRow();
//                    break;                      
//                }                    
//            }        
//        }else{
//            psMessage = "Unable to delete row.";
//            return false;
//        }
        return true;
    }
    
    //no need deactivate can already set to active yes/no in combo box
//    public boolean deactivateAddress(int fnRow) throws SQLException{
//        if (pnEditMode == EditMode.ADDNEW) {
//            psMessage = "This feature is only for saved entries.";
//            return false;
//        }
//        
//        if (getItemCount() == 0) {
//            psMessage = "No address to Deactivate.";
//            return false;
//        }
//        poAddress.updateString("cRecdStat", RecordStatus.INACTIVE);
//        return true;
//    }
    
    //New record
    /**
    * Initializes a new record for address information.
    *
    * This method prepares a new record for address information by creating an empty row in the dataset. It sets default values for various fields and marks the record as active.
    *
    * @return True if a new record is successfully initialized for address information, otherwise false.
    */
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {
//            String lsSQL = MiscUtil.addCondition(getSQ_Address(), "0=1");
//            ResultSet loRS = poGRider.executeQuery(lsSQL);
//            
//            RowSetFactory factory = RowSetProvider.newFactory();
//            poAddress = factory.createCachedRowSet();
//            poAddress.populate(loRS);
//            MiscUtil.close(loRS);
//            
//            poAddress.last();
//            poAddress.moveToInsertRow();
//            
//            MiscUtil.initRowSet(poAddress);       
//            poAddress.updateString("cRecdStat", RecordStatus.ACTIVE);
//            poAddress.updateString("cOfficexx", "0");
//            poAddress.updateString("cProvince", "0");
//            poAddress.updateString("cPrimaryx", "0");
//            poAddress.updateString("cCurrentx", "0");
//
//            poAddress.insertRow();
//            poAddress.moveToCurrentRow();
            if (!clearList()){
                psMessage = "Error clear fields for Address.";
                return false;
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    public boolean clearList() throws SQLException {
        if (getItemCount()> 0) {
            poAddress.beforeFirst();
            while (poAddress.next()) {
                poAddress.deleteRow();
            }
        }
        return true;
    }
    
    //search record
//    public boolean SearchRecord(String fsValue){
//        return OpenRecord("", false);
//    }
    
    //open record
    /**
    * Retrieves address information for a specified client or address ID.
    *
    * This method retrieves address information for a specified client or address ID, either by user ID or client ID. It populates the rowset with the retrieved data.
    *
    * @param fsValue The user ID or client ID for which address information should be retrieved.
    * @param fbByUserID Set to true to search by user ID, false to search by client ID.
    * @return True if the address information is successfully retrieved and populated, otherwise false.
    */
    public boolean OpenRecord(String fsValue, boolean fbByUserID){
        try {
            String lsSQL;
            ResultSet loRS;
            //RowSetFactory factory = RowSetProvider.newFactory();
            //open master
            if (fbByUserID)
                lsSQL = MiscUtil.addCondition(getSQ_Address(), "a.sAddrssID = " + SQLUtil.toSQL(fsValue));
            else 
                lsSQL = MiscUtil.addCondition(getSQ_Address(), "a.sClientID = " + SQLUtil.toSQL(fsValue));
            
            loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                //psMessage = "No record found.";
                MiscUtil.close(loRS);        
                //return false;
                return true;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poAddress = factory.createCachedRowSet();
            poAddress.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }
    
    //update record
    /**
    * Sets the edit mode to UPDATE and captures the current state of address records.
    *
    * This method changes the edit mode to UPDATE, indicating that address records are being edited. It also captures the current state of the address records to track any changes made during the update process.
    *
    * @return True to confirm the edit mode change and capture the current state of records.
    */
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        try {
        // Save the current state of the table as the original state
            if (poAddress != null){
                poOriginalAddress = (CachedRowSet) poAddress.createCopy();
            }
        } catch (SQLException e) {
            // Handle exception
        }
        return true;
    }
    
    
    //save record
    /**
    * Saves address records in the database based on the current edit mode.
    *
    * This method saves address records to the database. It checks the edit mode and processes the records accordingly, whether it's adding new records or updating existing ones. It also validates the records before saving and handles transactions when saving multiple records.
    *
    * @return True if the records are successfully saved, otherwise false.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        //boolean isModified = false;
        try {
            //dont save if no item
            if (getItemCount() > 0){  
                if (!isEntryOK()) return false;
                if (!pbWithParent) poGRider.beginTrans();
                String lsSQL = "";
                int lnCtr;            
                if (pnEditMode == EditMode.ADDNEW){ //add
                    //isModified = true;
                    lnCtr = 1;                
                    
                    if (!pbWithParent) poGRider.beginTrans(); 
                    poAddress.beforeFirst();
                    while (poAddress.next()){
                    //while (lnCtr <= getItemCount()){
                        String lsAddrssID = MiscUtil.getNextCode(ADDRESS_TABLE, "sAddrssID", true, poGRider.getConnection(), psBranchCd);
                        poAddress.updateString("sClientID", psClientID);                    
                        poAddress.updateObject("sAddrssID", lsAddrssID);
                        poAddress.updateString("sEntryByx", poGRider.getUserID());
                        poAddress.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                        poAddress.updateString("sModified", poGRider.getUserID());
                        poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                        poAddress.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poAddress, ADDRESS_TABLE, "sProvName»sBrgyName»sTownName»sProvIDxx");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, ADDRESS_TABLE, psBranchCd, lsAddrssID.substring(0, 4)) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }

                } else { //update                                    
                    //check if changes has been made

//                    poAddress.beforeFirst();
//                    lnCtr = 1;
//                    while (lnCtr <= getItemCount()){
//                        if (!CompareRows.isRowEqual(poAddress, poOriginalAddress)) {
//                            isModified = true;
//                            break;
//                        }
//                        lnCtr++;
//                    }
                    //if (isModified) {                     
                        // Save the changes
                        lnCtr = 1;
                        poAddress.beforeFirst();
                        //while (poAddress.next()){
                        while (lnCtr <= getItemCount()){
                            if(!CompareRows.isRowEqual(poAddress, poOriginalAddress,lnCtr)) {                                
                                String lsAddrssID = (String) getAddress(lnCtr, "sAddrssID");// check if user added new address to insert
                                if (lsAddrssID.equals("") || lsAddrssID.isEmpty()){
                                    lsAddrssID = MiscUtil.getNextCode(ADDRESS_TABLE, "sAddrssID", true, poGRider.getConnection(), psBranchCd);
                                    poAddress.updateString("sClientID", psClientID);                 
                                    poAddress.updateObject("sAddrssID", lsAddrssID);
                                    poAddress.updateString("sEntryByx", poGRider.getUserID());
                                    poAddress.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                                    poAddress.updateString("sModified", poGRider.getUserID());
                                    poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                                    poAddress.updateRow();

                                    lsSQL = MiscUtil.rowset2SQL(poAddress, ADDRESS_TABLE, "sProvName»sBrgyName»sTownName»sProvIDxx");

                                    if (poGRider.executeQuery(lsSQL, ADDRESS_TABLE, psBranchCd, lsAddrssID.substring(0, 4)) <= 0){
                                        if (!pbWithParent) poGRider.rollbackTrans();
                                        psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                        return false;
                                    }
                                }else{//if user modified already saved address   
                                    poAddress.updateString("sModified", poGRider.getUserID());
                                    poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                                    poAddress.updateRow();
                                    lsSQL = MiscUtil.rowset2SQL(poAddress, 
                                                                ADDRESS_TABLE, 
                                                                "sProvName»sBrgyName»sTownName»sProvIDxx", 
                                                                "sAddrssID = " + SQLUtil.toSQL(lsAddrssID) +
                                                                " AND sClientID = " + SQLUtil.toSQL((String) getAddress(lnCtr,"sClientID")));

                                    if (!lsSQL.isEmpty()){
                                        if (poGRider.executeQuery(lsSQL, ADDRESS_TABLE, psBranchCd, lsAddrssID.substring(0, 4)) <= 0){
                                            if (!pbWithParent) poGRider.rollbackTrans();
                                            psMessage = poGRider.getMessage() + ";" + poGRider.getErrMsg();
                                            return false;
                                        }
                                    }
                                }
                            }
                        lnCtr++;
                        }                    
                        // Update the original state of the table
                        poOriginalAddress = (CachedRowSet) poAddress.createCopy();
                    //}
                }

//                if (lsSQL.isEmpty() && isModified == true){
//                    psMessage = "No record to update.";
//                    return false;
//                }

                //if (!pbWithParent) poGRider.commitTrans();
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public String getSQ_Address(){
        String lsSQL = "";
        
        lsSQL = "SELECT " +
                    " a.sAddrssID" + //1
                    ", a.sClientID" + //2
                    ", a.sHouseNox" + //3
                    ", a.sAddressx" + //4
                    ", a.sTownIDxx" + //5
                    ", a.sBrgyIDxx" + //6
                    ", a.sZippCode" + //7
                    ", a.nPriority" + //8
                    ", a.nLatitude" + //9
                    ", a.nLongitud" + //10
                    ", a.sRemarksx" + //11
                    ", a.cOfficexx" + //12
                    ", a.cProvince" + //13
                    ", a.cPrimaryx" + //14
                    ", a.cBillingx" + //15
                    ", a.cShipping" + //16
                    ", a.cCurrentx" + //17
                    ", a.cRecdStat" + //18
                    ", a.sEntryByx" + //19
                    ", a.dEntryDte" + //20
                    ", a.sModified" + //21
                    ", a.dModified" +  //22
                    ", IFNULL(d.sProvName, '') sProvName" + //23
                    ", IFNULL(c.sBrgyName, '') sBrgyName" + //24
                    //", TRIM(CONCAT(b.sTownName, ', ', d.sProvName)) sTownName" + //25
                    ", IFNULL(b.sTownName, '') sTownName" + //25
                    ", IFNULL(d.sProvIDxx, '') sProvIDxx" + //26
                    //", IFNULL(b.sZippCode, '') sZippCode" +
                " FROM  " + ADDRESS_TABLE + " a" +
                    " LEFT JOIN TownCity b ON a.sTownIDxx = b.sTownIDxx" +
                    " LEFT JOIN Barangay c ON a.sBrgyIDxx = c.sBrgyIDxx" + 
                    " LEFT JOIN Province d on b.sProvIDxx = d.sProvIDxx";
        return lsSQL;
    }
    
    //query for Province
    private String getSQ_Province() {
        return "SELECT "
                + " sProvName "
                + ", sProvIDxx "
                + " FROM Province  ";
    }

    //search Province (used when "Province" is double clicked or searched)
    /**
     * Searches for a province based on criteria and retrieves its details.
     *
     * This method is used to search for a province, either by province code or
     * province name criteria. It allows both UI and non-UI search modes and
     * retrieves the province's details if found.
     *
     * @param fsValue The search criteria, which can be a province code or
     * province name.
     * @param fbByCode Set to true if searching by province code, false if
     * searching by province name.
     * @return True if a province is successfully found, and its details are
     * retrieved, otherwise false.
     * @throws SQLException if a database error occurs.
     */
    public boolean searchProvince(int fnRow, String fsValue, boolean fbByCode) throws SQLException {
        String lsSQL = getSQ_Province();

        if (fbByCode) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sProvIDxx = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sProvName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }

        ResultSet loRS;
        if (!pbWithUI) {
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);

            if (loRS.next()) {
                setAddressTable(fnRow, "sProvName", loRS.getString("sProvName"));
                setAddressTable(fnRow, "sProvIDxx", loRS.getString("sProvIDxx"));
            } else {
                setAddressTable(fnRow, "sProvName", "");
                setAddressTable(fnRow, "sProvIDxx", "");
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);

            JSONObject loJSON = showFXDialog.jsonSearch(
                    poGRider,
                    lsSQL,
                    fsValue,
                    "Code»Province",
                    "sProvIDxx»sProvName",
                    "sProvIDxx»sProvName",
                    fbByCode ? 0 : 1);
            if (loJSON == null) {
                setAddressTable(fnRow, "sProvName", "");
                setAddressTable(fnRow, "sProvIDxx", "");
                psMessage = "No record found/selected.";
                return false;
            } else {
                setAddressTable(fnRow, "sProvName", (String) loJSON.get("sProvName"));
                setAddressTable(fnRow, "sProvIDxx", (String) loJSON.get("sProvIDxx"));
            }
        }

        return true;
    }
    
    //query for town
    private String getSQ_Town(){
        return "SELECT " +
                    "  IFNULL(a.sTownIDxx, '') sTownIDxx " +
                    ", IFNULL(a.sTownName, '') sTownName " +
                    ", IFNULL(a.sZippCode, '') sZippCode " +                      
                    ", IFNULL(b.sProvName, '') sProvName " + 
                " FROM TownCity a"  +
                " LEFT JOIN Province b on b.sProvIDxx = a.sProvIDxx";
    }
    
    //search town (used when "Town" is double clicked or searched)
    /**
    * Searches for a town and updates the address table with the result.
    *
    * @param fnRow    The row number in the address table.
    * @param fsValue  The search value.
    * @param fbByCode True if searching by code, false if searching by name.
    * @return True if a record is found and updated, false if not.
    */
    public boolean searchTown(int fnRow, String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Town();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sTownIDxx = " + SQLUtil.toSQL(fsValue)
                                                    + " AND  b.sProvIDxx = " + SQLUtil.toSQL(getAddress(fnRow,"sProvIDxx")));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sTownName LIKE " + SQLUtil.toSQL(fsValue + "%")
                                                    + " AND b.sProvIDxx = " + SQLUtil.toSQL(getAddress(fnRow,"sProvIDxx")));
        }
        System.out.println(lsSQL);
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setAddressTable(fnRow, "sTownIDxx", loRS.getString("sTownIDxx"));
                //setAddressTable(fnRow, "sTownName", loRS.getString("sTownName")+ " " + loRS.getString("sProvName"));
                setAddressTable(fnRow, "sTownName", loRS.getString("sTownName"));
                setAddressTable(fnRow, "sZippCode", loRS.getString("sZippCode"));
                setAddressTable(fnRow, "sProvName", loRS.getString("sProvName"));   
                setAddressTable(fnRow, "sBrgyIDxx", "");
                setAddressTable(fnRow, "sBrgyName", "");            
            } else {
                setAddressTable(fnRow, "sTownIDxx", "");
                setAddressTable(fnRow, "sTownName", "");
                setAddressTable(fnRow, "sZippCode", "");
                setAddressTable(fnRow, "sProvName", "");  
                setAddressTable(fnRow, "sBrgyIDxx", "");
                setAddressTable(fnRow, "sBrgyName", "");
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            //jahn 04/03/2023
            //changed search function allow searching by user, was previously disabled 
            //and can only search based on what you type on the text field that triggers this function
//            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Town", "sTownIDxx»sTownName");
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        lsSQL, 
                                                        fsValue,
                                                        "Code»Town", 
                                                        "sTownIDxx»sTownName",
                                                        "sTownIDxx»sTownName",
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                setAddressTable(fnRow, "sTownIDxx", "");
                setAddressTable(fnRow, "sTownName", "");
                setAddressTable(fnRow, "sZippCode", "");
                setAddressTable(fnRow, "sProvName", "");  
                setAddressTable(fnRow, "sBrgyIDxx", "");
                setAddressTable(fnRow, "sBrgyName", "");
                psMessage = "No record found/selected.";
                return false;
            } else {
                setAddressTable(fnRow, "sTownIDxx", (String) loJSON.get("sTownIDxx"));
                //setAddressTable(fnRow, "sTownName", (String) loJSON.get("sTownName")+ " " + (String) loJSON.get("sProvName"));
                setAddressTable(fnRow, "sTownName", (String) loJSON.get("sTownName"));
                setAddressTable(fnRow, "sZippCode", (String) loJSON.get("sZippCode"));
                setAddressTable(fnRow, "sProvName", (String) loJSON.get("sProvName")); 
                setAddressTable(fnRow, "sBrgyIDxx", "");
                setAddressTable(fnRow, "sBrgyName", "");
            }
        }
        
        return true;
    }
    //query for barangay
    private String getSQ_Barangay(){
        return "SELECT " +
                    "  IFNULL(a.sBrgyIDxx, '') sBrgyIDxx " +
                    ", IFNULL(a.sBrgyName, '') sBrgyName " +
                    ", IFNULL(b.sTownIDxx, '') sTownIDxx " +
                    ", IFNULL(b.sTownName, '') sTownName " +
                    
                " FROM Barangay a " +
                "   LEFT JOIN TownCity b " +
                "     ON a.sTownIDxx = b.sTownIDxx " ;                
    }
    //search barangay (used when "barangay" is double clicked or searched)
    /**
    * Searches for a barangay and updates the address table with the result.
    *
    * @param fnRow    The row number in the address table.
    * @param fsValue  The search value.
    * @param fbByCode True if searching by code, false if searching by name.
    * @return True if a record is found and updated, false if not.
    */
    public boolean searchBarangay(int fnRow, String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Barangay() + "WHERE a.sTownIDxx = " + getAddress(fnRow,"sTownIDxx");
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sBrgyIDxx = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sBrgyName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setAddressTable(fnRow, "sBrgyIDxx", loRS.getString("sBrgyIDxx"));
                setAddressTable(fnRow, "sBrgyName", loRS.getString("sBrgyName"));
            } else {
                setAddressTable(fnRow, "sBrgyIDxx", "");
                setAddressTable(fnRow, "sBrgyName", "");
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            //jahn 04/03/2023
            //changed search function allow searching by user, was previously disabled 
            //and can only search based on what you type on the text field that triggers this function
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Barangay", "sBrgyIDxx»sBrgyName");
            JSONObject loJSON = showFXDialog.jsonSearch(
                    poGRider, 
                    lsSQL,
                    fsValue,
                    "Code»Barangay", 
                    "sBrgyIDxx»sBrgyName",
                    "a.sBrgyIDxx»a.sBrgyName",
                    fbByCode ? 0 : 1);
            if (loJSON == null){
                setAddressTable(fnRow, "sBrgyIDxx", "");
                setAddressTable(fnRow, "sBrgyName", "");
                psMessage = "No record found/selected.";
                return false;
            } else {
                setAddressTable(fnRow, "sBrgyIDxx", (String) loJSON.get("sBrgyIDxx"));
                setAddressTable(fnRow, "sBrgyName", (String) loJSON.get("sBrgyName"));
            }
        }
        
        return true;
    }
    //for adding new row in address
    /**
    * Adds a new address record to the list.
    *
    * This method creates a new address record and adds it to the list of addresses. It initializes the record with default values and sets the record status to ACTIVE.
    *
    * @return True to confirm the successful addition of the address record.
    */
    public boolean addAddress() throws SQLException{
     //   int lnCtr;
    //    int lnRow = getItemCount();
        
        //validate if incentive is already added
//        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){
//            if (fsCode.equals((String) getAddress(lnCtr, "sAddrssID"))) return true;
//        }
        if (poAddress == null){
            String lsSQL = MiscUtil.addCondition(getSQ_Address(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poAddress = factory.createCachedRowSet();
            poAddress.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        poAddress.last();
        poAddress.moveToInsertRow();

        MiscUtil.initRowSet(poAddress);  
  
        poAddress.updateString("cRecdStat", RecordStatus.ACTIVE);
        poAddress.updateString("cOfficexx", "0");
        poAddress.updateString("cProvince", "0");
        poAddress.updateString("cPrimaryx", "0");
        poAddress.updateString("cCurrentx", "0");
        poAddress.insertRow();
        poAddress.moveToCurrentRow();
                
        return true;
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poAddress.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poAddress.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poAddress.getMetaData().getColumnType(lnCtr));
            if (poAddress.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poAddress.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poAddress.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    } 
    
    /**
    * Validates the entered address details for correctness.
    *
    * This method checks the validity of the entered address details, including the town, barangay, and address fields. It also ensures that at least one address is marked as primary. Additionally, it validates the maximum size of string variables to prevent data errors.
    *
    * @return True if all address details are valid and meet the required criteria.
    */
    private boolean isEntryOK() throws SQLException{
        int lnCtr = 1;
        int lnRow = getItemCount();
        boolean lbPrimary = false;
        
        //validate detail
        if (getItemCount() == 0){
            psMessage = "No Address detected.";
            return false;
        }
        
        if (getItemCount()> 0) {        
            while (lnCtr <= getItemCount()){            
                if (poAddress.getString("sTownIDxx").isEmpty()){
                    psMessage = "Town is not set.";
                    return false;
                }
                if (poAddress.getString("sBrgyIDxx").isEmpty()){
                    psMessage = "Barangay is not set.";
                    return false;
                } 
//                if (poAddress.getString("sAddressx").isEmpty()){
//                    psMessage = "Address is not set.";
//                    return false;
//                }
                lnCtr++;
            }
        }
                                
        
        String lsPrimary = "1";
        lnCtr = 1;
        //check if there are other primary
        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){            
            if ((getAddress(lnCtr, "cPrimaryx").equals(lsPrimary))) {   
                lbPrimary = true;
                break;
            }
        }
        //if false do not allow saving
        if (!lbPrimary){          
             psMessage = "No Primary address found, please set a primary address.";
            return false;
        }                      
        //validate max size of string variables
        
        return true;
    }
}
