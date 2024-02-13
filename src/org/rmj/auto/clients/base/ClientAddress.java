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

/**
 *
 * @author User
 */
public class ClientAddress {
    private final String CLIENTADDRESS_TABLE = "Client_Address";
    private final String ADDRESS_TABLE = "addresses";
    private final String DEFAULT_DATE = "1900-01-01";
    
    List<Integer> index = new ArrayList<>();
    List<String> value = new ArrayList<>();
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psClientID;
    
    public CachedRowSet poClientAddress;
    private CachedRowSet poOriginalClientAddress;
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

    private Object getOriginalClientAddress(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        poOriginalClientAddress.absolute(fnRow);
        return poOriginalClientAddress.getObject(fnIndex);
    }
    
    private Object getOriginalClientAddress(int fnRow, String fsIndex) throws SQLException{
        if (getItemCount()== 0 || fnRow > getItemCount()) return null;
        return getOriginalClientAddress(fnRow, MiscUtil.getColumnIndex(poOriginalClientAddress, fsIndex));
    }
    
    private Object getOriginalClientAddress(String fsIndex) throws SQLException{
        return getOriginalClientAddress(MiscUtil.getColumnIndex(poOriginalClientAddress, fsIndex));
    }
    
    private Object getOriginalClientAddress(int fnIndex) throws SQLException{
        poOriginalClientAddress.first();
        return poOriginalClientAddress.getObject(fnIndex);
    }   
    
    public Object getAddress(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        poClientAddress.absolute(fnRow);
        return poClientAddress.getObject(fnIndex);
    }
    
    public Object getAddress(int fnRow, String fsIndex) throws SQLException{
        if (getItemCount()== 0 || fnRow > getItemCount()) return null;
        return getAddress(fnRow, MiscUtil.getColumnIndex(poClientAddress, fsIndex));
    }
    
    public Object getAddress(String fsIndex) throws SQLException{
        return getAddress(MiscUtil.getColumnIndex(poClientAddress, fsIndex));
    }
    
    public Object getAddress(int fnIndex) throws SQLException{
        poClientAddress.first();
        return poClientAddress.getObject(fnIndex);
    }
    
    //for setting address in the table
    public void setAddressTable(int fnRow, int fnIndex, Object foValue) throws SQLException{
        
        poClientAddress.absolute(fnRow);        
        switch (fnIndex){
            case 1://sAddrssID
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
                poClientAddress.updateObject(fnIndex, (String) foValue);
                poClientAddress.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));              
                break;
            case 12://cOfficexx
            case 13://cProvince
            case 14://cPrimaryx
            case 17://cCurrentx
            case 18://cRecdStat
                if (foValue instanceof Integer)
                    poClientAddress.updateInt(fnIndex, (int) foValue);
                else 
                    poClientAddress.updateInt(fnIndex, 0);
                
                poClientAddress.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));               
                break;
        }               
    }
    
    //for setting address in the table
    public void setAddressTable(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setAddressTable(fnRow,MiscUtil.getColumnIndex(poClientAddress, fsIndex), foValue);
    }
    
    public int getItemCount() throws SQLException{
        if (poClientAddress != null){
            poClientAddress.last();
            return poClientAddress.getRow();
        }else{
            return 0;
        }              
    }
    
    /**
    * Removes a specific address, ensuring at least one primary address remains.
    * @param fnRow The row number of the address to be removed.
    * @return True if removed, false if it's the only primary address.
    */
    public boolean removeAddress(int fnRow) throws SQLException{
                
        if (getItemCount() == 0) {
            psMessage = "No address to delete.";
            return false;
        }
        
        poClientAddress.absolute(fnRow);
        
        String lsConcatClientAddr = poClientAddress.getString("sHouseNox") + poClientAddress.getString("sAddressx") + poClientAddress.getString("sBrgyName") + poClientAddress.getString("sTownName") + poClientAddress.getString("sProvName");
        lsConcatClientAddr = lsConcatClientAddr.replace(" ", "").toUpperCase();
        
        if (getAddressesCount() > 0) {
            String lsConcatAddresses = "";
            poAddress.beforeFirst();
            while (poAddress.next()) {
                lsConcatAddresses = poAddress.getString("sHouseNox") + poAddress.getString("sAddressx") + poAddress.getString("sBrgyName") + poAddress.getString("sTownName") + poAddress.getString("sProvName");
                lsConcatAddresses = lsConcatAddresses.replace(" ", "").toUpperCase();
                
                if(lsConcatClientAddr.equals(lsConcatAddresses)){
                    poAddress.deleteRow();
                    break;
                }
            }
        }
        
        poClientAddress.deleteRow();
        return true;
    }
    
    //New record
    /**
    * Initializes a new record for address information.
    * This method prepares a new record for address information by creating an empty row in the dataset. It sets default values for various fields and marks the record as active.
    * @return True if a new record is successfully initialized for address information, otherwise false.
    */
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {
            if (!clearList()){
                psMessage = "Error clear fields for Address.";
                return false;
            }
            
            if (!clearAddresses()){
                psMessage = "Error clear fields for Addresses.";
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
            poClientAddress.beforeFirst();
            while (poClientAddress.next()) {
                poClientAddress.deleteRow();
            }
        }
        return true;
    }
    
    /**
    * Retrieves address information for a specified client or address ID.
    * This method retrieves address information for a specified client or address ID, either by user ID or client ID. It populates the rowset with the retrieved data.
    * @param fsValue The user ID or client ID for which address information should be retrieved.
    * @param fbByUserID Set to true to search by user ID, false to search by client ID.
    * @return True if the address information is successfully retrieved and populated, otherwise false.
    */
    public boolean OpenRecord(String fsValue, boolean fbByUserID){
        try {
            String lsSQL;
            ResultSet loRS;
            if (fbByUserID)
                lsSQL = MiscUtil.addCondition(getSQ_ClientAddress(), "a.sAddrssID = " + SQLUtil.toSQL(fsValue));
            else 
                lsSQL = MiscUtil.addCondition(getSQ_ClientAddress(), "a.sClientID = " + SQLUtil.toSQL(fsValue));
            
            loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                MiscUtil.close(loRS);        
                return true;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poClientAddress = factory.createCachedRowSet();
            poClientAddress.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }
    
    /**
    * Sets the edit mode to UPDATE and captures the current state of address records.
    * This method changes the edit mode to UPDATE, indicating that address records are being edited. It also captures the current state of the address records to track any changes made during the update process.
    * @return True to confirm the edit mode change and capture the current state of records.
    */
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        try {
            if (!clearAddresses()){
                psMessage = "Error clear fields for Addresses.";
                return false;
            }
            
            // Save the current state of the table as the original state
            if (poClientAddress != null){
                poOriginalClientAddress = (CachedRowSet) poClientAddress.createCopy();
                
                for(int lnCtr = 1; lnCtr <= getItemCount(); lnCtr++){
                    addAddresses(lnCtr);
                }
            }
            
            if(poAddress != null){
                poOriginalAddress = (CachedRowSet) poAddress.createCopy();
            }
        } catch (SQLException e) {
            // Handle exception
        }
        return true;
    }
    
    /**
    * Saves address records in the database based on the current edit mode.
    * This method saves address records to the database. It checks the edit mode and processes the records accordingly, whether it's adding new records or updating existing ones. It also validates the records before saving and handles transactions when saving multiple records.
    * @return True if the records are successfully saved, otherwise false.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        try {
            //dont save if no item
            if (getItemCount() > 0){  
                if (!isEntryOK()) return false;
                if (!pbWithParent) poGRider.beginTrans();
                String lsSQL = "";
                int lnCtr = 1;   
                String lsConcatAddress;
                String lsAddrssID = "";
                if(getAddressesCount() > 0){
                    poAddress.beforeFirst();
                    while (poAddress.next()){
                        lsAddrssID = (String) getAddresses(lnCtr, "sAddrssID");
                        if(lsAddrssID.isEmpty()){
                            lsAddrssID = MiscUtil.getNextCode(ADDRESS_TABLE, "sAddrssID", true, poGRider.getConnection(), psBranchCd);
                            poAddress.updateObject("sAddrssID", lsAddrssID);
                            poAddress.updateString("sModified", poGRider.getUserID());
                            poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                            poAddress.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poAddress, ADDRESS_TABLE, "trimAddress»sProvName»sBrgyName»sTownName»sProvIDxx");
                            if (lsSQL.isEmpty()){
                                psMessage = "No record to update.";
                                return false;
                            }
                            if (poGRider.executeQuery(lsSQL, ADDRESS_TABLE, psBranchCd, lsAddrssID.substring(0, 4)) <= 0){
                                if (!pbWithParent) poGRider.rollbackTrans();
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }

                        } else {
                            poAddress.updateString("sModified", poGRider.getUserID());
                            poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                            poAddress.updateRow();
                            lsSQL = MiscUtil.rowset2SQL(poAddress, 
                                                        ADDRESS_TABLE, 
                                                        "trimAddress»sProvName»sBrgyName»sTownName»sProvIDxx", 
                                                        "sAddrssID = " + SQLUtil.toSQL(lsAddrssID));

                            if (!lsSQL.isEmpty()){
                                if (poGRider.executeQuery(lsSQL, ADDRESS_TABLE, psBranchCd, psClientID.substring(0, 4)) <= 0){
                                    if (!pbWithParent) poGRider.rollbackTrans();
                                    psMessage = poGRider.getMessage() + ";" + poGRider.getErrMsg();
                                    return false;
                                }
                            }
                        }
                        lnCtr++;
                    }
                }
                
                if (pnEditMode == EditMode.ADDNEW){ //add
                    lnCtr = 1;   
                    if (!pbWithParent) poGRider.beginTrans(); 
                    poClientAddress.beforeFirst();
                    while (poClientAddress.next()){
                        lsConcatAddress = poClientAddress.getString("sHouseNox") + poClientAddress.getString("sAddressx") + poClientAddress.getString("sBrgyName") + poClientAddress.getString("sTownName") + poClientAddress.getString("sProvName");
                        lsConcatAddress = lsConcatAddress.replace(" ", "").toUpperCase();
                        checkAddress(lsConcatAddress,lnCtr,false);
                        
                        poClientAddress.updateString("sClientID", psClientID); 
                        poClientAddress.updateString("sEntryByx", poGRider.getUserID());
                        poClientAddress.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                        poClientAddress.updateString("sModified", poGRider.getUserID());
                        poClientAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                        poClientAddress.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poClientAddress, CLIENTADDRESS_TABLE, "sProvName»sBrgyName»sTownName»sProvIDxx»sHouseNox»sAddressx»sTownIDxx»sBrgyIDxx»sZippCode»nPriority»nLatitude»nLongitud»sRemarksx");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, CLIENTADDRESS_TABLE, psBranchCd, psClientID.substring(0, 4)) <= 0){ 
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        
                        lnCtr++;
                    }

                } else { //update                    
                    // Save the changes
                    String lsOriginalAddrssID = "";
                    lnCtr = 1;
                    poClientAddress.beforeFirst();
                    while (lnCtr <= getItemCount()){

                        lsConcatAddress = (String) getAddress(lnCtr,"sHouseNox") + (String) getAddress(lnCtr,"sAddressx") + (String) getAddress(lnCtr,"sBrgyName") + (String) getAddress(lnCtr,"sTownName") + (String) getAddress(lnCtr,"sProvName");
                        lsConcatAddress = lsConcatAddress.replace(" ", "").toUpperCase();
                        checkAddress(lsConcatAddress,lnCtr,false);

                        if(!CompareRows.isRowEqual(poClientAddress, poOriginalClientAddress,lnCtr)) {                                
                            String lsEntryBy = (String) getAddress(lnCtr, "sEntryByx");// check if user added new address to insert
                            if (lsEntryBy.equals("") || lsEntryBy.isEmpty()){
                                poClientAddress.updateString("sClientID", psClientID);   
                                poClientAddress.updateString("sEntryByx", poGRider.getUserID());
                                poClientAddress.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                                poClientAddress.updateString("sModified", poGRider.getUserID());
                                poClientAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                                poClientAddress.updateRow();

                                lsSQL = MiscUtil.rowset2SQL(poClientAddress, CLIENTADDRESS_TABLE, "sProvName»sBrgyName»sTownName»sProvIDxx»sHouseNox»sAddressx»sTownIDxx»sBrgyIDxx»sZippCode»nPriority»nLatitude»nLongitud»sRemarksx");

                                if (poGRider.executeQuery(lsSQL, CLIENTADDRESS_TABLE, psBranchCd, psClientID.substring(0, 4)) <= 0){
                                    if (!pbWithParent) poGRider.rollbackTrans();
                                    psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                    return false;
                                }
                            }else{//if user modified already saved address   
                                lsOriginalAddrssID = (String) getOriginalClientAddress(lnCtr, "sAddrssID");
                                poClientAddress.updateString("sModified", poGRider.getUserID());
                                poClientAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                                poClientAddress.updateRow();
                                lsSQL = MiscUtil.rowset2SQL(poClientAddress, 
                                                            CLIENTADDRESS_TABLE, 
                                                            "sProvName»sBrgyName»sTownName»sProvIDxx»sHouseNox»sAddressx»sTownIDxx»sBrgyIDxx»sZippCode»nPriority»nLatitude»nLongitud»sRemarksx", 
                                                            "sAddrssID = " + SQLUtil.toSQL(lsOriginalAddrssID) +
                                                            " AND sClientID = " + SQLUtil.toSQL((String) getAddress(lnCtr,"sClientID")));

                                if (!lsSQL.isEmpty()){
                                    if (poGRider.executeQuery(lsSQL, CLIENTADDRESS_TABLE, psBranchCd, psClientID.substring(0, 4)) <= 0){
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
                    if(poClientAddress != null){
                        poOriginalClientAddress = (CachedRowSet) poClientAddress.createCopy();
                    }
                    if(poAddress != null){
                        poOriginalAddress = (CachedRowSet) poAddress.createCopy();
                    }
                }
                
                if (!clearAddresses()){
                    psMessage = "Error clear fields for Addresses.";
                    return false;
                }

                //if (!pbWithParent) poGRider.commitTrans();
                
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public String getSQ_ClientAddress(){
        return  " SELECT   "  +                                                 
                "   IFNULL(a.sAddrssID, '')  sAddrssID   " + //1                                     
                " , IFNULL(a.sClientID, '')  sClientID   " + //2                                     
                " , IFNULL(b.sHouseNox, '')  sHouseNox   " + //3                                     
                " , IFNULL(b.sAddressx, '')  sAddressx   " + //4                                     
                " , IFNULL(b.sTownIDxx, '')  sTownIDxx   " + //5                                     
                " , IFNULL(b.sBrgyIDxx, '')  sBrgyIDxx   " + //6                                     
                " , IFNULL(b.sZippCode, '')  sZippCode  " + //7                                     
                " , '' AS nPriority " + //8                                     
                " , b.nLatitude     " + //9                                     
                " , b.nLongitud     " + //10                                    
                " , IFNULL(b.sRemarksx, '')  sRemarksx   " + //11                                    
                " , IFNULL(a.cOfficexx, '')  cOfficexx   " + //12                                    
                " , IFNULL(a.cProvince, '')  cProvince   " + //13                                    
                " , IFNULL(a.cPrimaryx, '')  cPrimaryx   " + //14                                    
                " , IFNULL(a.cBillingx, '')  cBillingx   " + //15                                    
                " , IFNULL(a.cShipping, '')  cShipping   " + //16                                    
                " , IFNULL(a.cCurrentx, '')  cCurrentx   " + //17                                    
                " , IFNULL(a.cRecdStat, '')  cRecdStat   " + //18                                    
                " , IFNULL(a.sEntryByx, '')  sEntryByx   " + //19                                    
                " , a.dEntryDte     " + //20                                    
                " , IFNULL(a.sModified, '')  sModified   " + //21                                    
                " , a.dModified     " + //22                                    
                " , IFNULL(e.sProvName, '') sProvName " + //23                  
                " , IFNULL(d.sBrgyName, '') sBrgyName " + //24                  
                " , IFNULL(c.sTownName, '') sTownName " + //25                  
                " , IFNULL(e.sProvIDxx, '') sProvIDxx " + //26                  
                "  FROM client_address a              " +                       
                "  INNER JOIN addresses b ON b.sAddrssID = a.sAddrssID " +      
                "  LEFT JOIN TownCity c ON c.sTownIDxx = b.sTownIDxx   " +      
                "  LEFT JOIN Barangay d ON d.sBrgyIDxx = b.sBrgyIDxx AND d.sTownIDxx = b.sTownIDxx   " +      
                "  LEFT JOIN Province e ON e.sProvIDxx = c.sProvIDxx   " ;  
    }
    
    private String getSQ_Address() {
        return  "SELECT  " +                                              
                "   IFNULL(a.sAddrssID, '') sAddrssID" + //1                                   
                ",  IFNULL(a.sHouseNox, '') sHouseNox" + //2                                   
                ",  IFNULL(a.sAddressx, '') sAddressx" + //3                                   
                ",  IFNULL(a.sTownIDxx, '') sTownIDxx" + //4                                   
                ",  IFNULL(a.sZippCode, '') sZippCode" + //5                                   
                ",  IFNULL(a.sBrgyIDxx, '') sBrgyIDxx" + //6                                   
                ",  a.nLatitude " + //7                                   
                ",  a.nLongitud " + //8                                   
                ",  IFNULL(a.sRemarksx, '') sRemarksx" + //9                                   
                ",  IFNULL(a.sModified, '') sModified" + //10                                  
                ",  a.dModified " + //11                       
                ", IFNULL(d.sProvName, '') sProvName " + //12             
                ", IFNULL(c.sBrgyName, '') sBrgyName " + //13             
                ", IFNULL(b.sTownName, '') sTownName " + //14             
                ", IFNULL(d.sProvIDxx, '') sProvIDxx " + //15              
                ", REPLACE(CONCAT(IFNULL(a.sHouseNox,''), IFNULL(a.sAddressx,''),IFNULL(c.sBrgyName,''), IFNULL(b.sTownName,''), IFNULL(d.sProvName,'')), ' ', '') AS trimAddress" + //16
                " FROM addresses a                    " +                  
                " LEFT JOIN TownCity b ON b.sTownIDxx = a.sTownIDxx " +   
                " LEFT JOIN Barangay c ON c.sBrgyIDxx = a.sBrgyIDxx " +   
                " LEFT JOIN Province d ON d.sProvIDxx = b.sProvIDxx " ;   
    }
    
    /**
    * Adds a new address record to the list.
    * This method creates a new address record and adds it to the list of addresses. It initializes the record with default values and sets the record status to ACTIVE.
    * @return True to confirm the successful addition of the address record.
    */
    public boolean addAddress() throws SQLException{
        if (poClientAddress == null){
            String lsSQL = MiscUtil.addCondition(getSQ_ClientAddress(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poClientAddress = factory.createCachedRowSet();
            poClientAddress.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        poClientAddress.last();
        poClientAddress.moveToInsertRow();

        MiscUtil.initRowSet(poClientAddress);  
  
        poClientAddress.updateString("cRecdStat", RecordStatus.ACTIVE);
        poClientAddress.updateString("cOfficexx", "0");
        poClientAddress.updateString("cProvince", "0");
        poClientAddress.updateString("cPrimaryx", "0");
        poClientAddress.updateString("cCurrentx", "0");
        poClientAddress.insertRow();
        poClientAddress.moveToCurrentRow();
                
        return true;
    }
    
    /**
     * Add data to Addresses Table
     * @param fnRow Current row on ClientAddress
     * @return true
    */
    
    public boolean addAddresses(int fnRow){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory;

            if (poAddress == null) {
                lsSQL = MiscUtil.addCondition(getSQ_Address(), "0=1");
                loRS = poGRider.executeQuery(lsSQL);
                factory = RowSetProvider.newFactory();
                poAddress = factory.createCachedRowSet();
                poAddress.populate(loRS);
                MiscUtil.close(loRS);
            }
            poAddress.last();
            poAddress.moveToInsertRow();
            MiscUtil.initRowSet(poAddress);
            poAddress.updateString("sAddrssID", (String) getAddress(fnRow, "sAddrssID"));
            poAddress.updateString("sHouseNox", (String) getAddress(fnRow, "sHouseNox"));
            poAddress.updateString("sAddressx", (String) getAddress(fnRow, "sAddressx"));
            poAddress.updateString("sTownIDxx", (String) getAddress(fnRow, "sTownIDxx"));
            poAddress.updateString("sBrgyIDxx", (String) getAddress(fnRow, "sBrgyIDxx"));
            poAddress.updateString("sZippCode", (String) getAddress(fnRow, "sZippCode"));
            poAddress.updateString("sRemarksx", (String) getAddress(fnRow, "sRemarksx"));
            poAddress.insertRow();
            poAddress.moveToCurrentRow();
        } catch (SQLException ex) {
            Logger.getLogger(ClientAddress.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    /**
     * Updating of address per row
     * @param fnRow
     * @return 
     */
    public boolean UpdateAddresses(int fnRow){
        try {
            setAddressesTable(fnRow,"sHouseNox", (String) getAddress(fnRow, "sHouseNox"));
            setAddressesTable(fnRow,"sAddressx", (String) getAddress(fnRow, "sAddressx"));
            setAddressesTable(fnRow,"sTownIDxx", (String) getAddress(fnRow, "sTownIDxx"));
            setAddressesTable(fnRow,"sBrgyIDxx", (String) getAddress(fnRow, "sBrgyIDxx"));
            setAddressesTable(fnRow,"sZippCode", (String) getAddress(fnRow, "sZippCode"));
            setAddressesTable(fnRow,"sRemarksx", (String) getAddress(fnRow, "sRemarksx"));
            
            System.out.println("ROW " + getAddressesCount());
        } catch (SQLException ex) {
            Logger.getLogger(ClientAddress.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    //for setting address in the table
    private void setAddressesTable(int fnRow, int fnIndex, Object foValue) throws SQLException{
        poAddress.absolute(fnRow);        
        switch (fnIndex){
            case 1://sAddrssID
            case 2://sHouseNox
            case 3://sAddressx
            case 4://sTownIDxx
            case 6://sBrgyIDxx
            case 5://sZippCode
            case 9://sRemarksx
            case 12://sProvName
            case 13://sBrgyName
            case 14://sTownName
            case 15://sProvIDxx
                poAddress.updateObject(fnIndex, (String) foValue);
                poAddress.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getAddresses(fnIndex));              
                break;
        }               
    }
    
    //for setting address in the table
    private void setAddressesTable(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setAddressesTable(fnRow,MiscUtil.getColumnIndex(poAddress, fsIndex), foValue);
    }
    
    /**
     * Check Existence of Address in general address table
     * @param fsValue The concatenate address description without space
     * @param fnRow The Client Address current row
     * @param fbCheck Check when checking or setting address
     * @return 
     */
    public boolean checkAddress(String fsValue, int fnRow, boolean fbCheck){
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Address(), " UPPER(REPLACE(CONCAT(IFNULL(a.sHouseNox,''), IFNULL(a.sAddressx,''),IFNULL(c.sBrgyName,''), IFNULL(b.sTownName,''), IFNULL(d.sProvName,'')), ' ', '')) = " + SQLUtil.toSQL(fsValue)); //" CONCAT_WS(a.sHouseNox, REPLACE(a.sAddressx, ' ', ''),REPLACE(c.sBrgyName, ' ', ''), REPLACE(b.sTownName, ' ', ''), REPLACE(d.sProvName, ' ', '')) = "
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);

            if (loRS.next()) {
                if(!fbCheck){
                    setAddressTable(fnRow, "sAddrssID", loRS.getString("sAddrssID"));
                } else {
                    psMessage = "Existing same Address Information found.";
                    return false;
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(ClientAddress.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    /**
     * Check for Address that already linked thru other customer
     * @param fsAddressID The Address ID
     * @param fsValue The concatenated address description
     * @param fsClientID The current ClientID
     * @param fnRow The client address row
     * @return 
     */
    public boolean checkClientAddress(String fsAddressID,String fsValue, String fsClientID, int fnRow){
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_ClientAddress(), " a.sAddrssID = " + SQLUtil.toSQL(fsAddressID) +
                                                                            " AND a.sClientID <> " + SQLUtil.toSQL(fsClientID)); 
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (loRS.next()) {
                String lsConcatAddresses = loRS.getString("sHouseNox") + loRS.getString("sAddressx") + loRS.getString("sBrgyName") + loRS.getString("sTownName") + loRS.getString("sProvName");
                lsConcatAddresses = lsConcatAddresses.replace(" ", "").toUpperCase();
                
                if(!fsValue.equals(lsConcatAddresses)){
                    psMessage = "Existing Customer with the same Address Information found. You want to update Address?";
                    return false;
                }
            } 
            
        } catch (SQLException ex) {
            Logger.getLogger(ClientAddress.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    private int getAddressesCount() throws SQLException{
        if (poAddress != null){
            poAddress.last();
            return poAddress.getRow();
        }else{
            return 0;
        }              
    }
    
    private boolean clearAddresses() throws SQLException {
        if (getAddressesCount() > 0) {
            poAddress.beforeFirst();
            while (poAddress.next()) {
                poAddress.deleteRow();
            }
        }
        return true;
    }
    
    public Object getAddresses (int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        poAddress.absolute(fnRow);
        return poAddress.getObject(fnIndex);
    }
    
    public Object getAddresses(int fnRow, String fsIndex) throws SQLException{
        if (getAddressesCount()== 0 || fnRow > getAddressesCount()) return null;
        return getAddresses(fnRow, MiscUtil.getColumnIndex(poAddress, fsIndex));
    }
    
    public Object getAddresses(String fsIndex) throws SQLException{
        return getAddresses(MiscUtil.getColumnIndex(poAddress, fsIndex));
    }
    
    public Object getAddresses(int fnIndex) throws SQLException{
        poAddress.first();
        return poAddress.getObject(fnIndex);
    }
    
    //query for Province
    private String getSQ_Province() {
        return "SELECT "
                + " sProvName "
                + ", sProvIDxx "
                + " FROM Province  ";
    }

    /**
     * Searches for a province based on criteria and retrieves its details.
     * This method is used to search for a province, either by province code or
     * province name criteria. It allows both UI and non-UI search modes and
     * retrieves the province's details if found.
     * @param fnRow The row number in the address table.
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
    
    /**
    * Searches for a town and updates the address table with the result.
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
                " LEFT JOIN TownCity b ON a.sTownIDxx = b.sTownIDxx " ;                
    }
    
    /**
    * Searches for a barangay and updates the address table with the result.
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
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poClientAddress.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poClientAddress.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poClientAddress.getMetaData().getColumnType(lnCtr));
            if (poClientAddress.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poClientAddress.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poClientAddress.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    } 
    
    /**
    * Validates the entered address details for correctness.
    * This method checks the validity of the entered address details, including the town, barangay, and address fields. It also ensures that at least one address is marked as primary. Additionally, it validates the maximum size of string variables to prevent data errors.
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
                if (poClientAddress.getString("sTownIDxx").isEmpty()){
                    psMessage = "Town is not set.";
                    return false;
                }
                if (poClientAddress.getString("sBrgyIDxx").isEmpty()){
                    psMessage = "Barangay is not set.";
                    return false;
                } 
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
