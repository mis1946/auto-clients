/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.clients.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.apache.commons.lang3.math.Fraction;
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
    private final String ADDRESS_TABLE = "Client_Address";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    //private ClientMaster poMaster;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psClientID;
    
    private CachedRowSet poAddress;
    
    public ClientAddress(GRider foGrider, String fsBranchCd, boolean fbWithparent){
        poGRider = foGrider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithparent;   
    // not yet finished with saving sClientID
    //   poMaster = new ClientMaster(poGRider, psBranchCd, true);
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
            case 24://sBrgyName
            case 25://sTownName
//            case 15:
//            case 16:
                poAddress.updateObject(fnIndex, (String) foValue);
                poAddress.updateRow();
                //if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));
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
                //if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));
                break;
        }               
    }
    
    //for setting address in the table
    public void setAddressTable(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setAddressTable(fnRow,MiscUtil.getColumnIndex(poAddress, fsIndex), foValue);
    }
    
    public int getItemCount() throws SQLException{
        poAddress.last();
        return poAddress.getRow();
    }
    
    public boolean removeAddress(int fnRow) throws SQLException{
        if (pnEditMode != EditMode.ADDNEW) {
            psMessage = "This feature was only for new entries.";
            return false;
        }
                
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
    
    public boolean deactivateAddress(int fnRow) throws SQLException{
        if (pnEditMode == EditMode.ADDNEW) {
            psMessage = "This feature is only for saved entries.";
            return false;
        }
        
        if (getItemCount() == 0) {
            psMessage = "No address to Deactivate.";
            return false;
        }
        poAddress.updateString("cRecdStat", RecordStatus.INACTIVE);
        return true;
    }
    
    //New record
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Address(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poAddress = factory.createCachedRowSet();
            poAddress.populate(loRS);
            MiscUtil.close(loRS);
            
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
           
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    //search record
//    public boolean SearchRecord(String fsValue){
//        return OpenRecord("", false);
//    }
    
    //open record
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
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
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
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        return true;
    }
    
    //save record
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            
            String lsSQL = "";
            int lnCtr;
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                lnCtr = 1;
                poAddress.beforeFirst();
//                while (poAddress.next()){
                while (lnCtr <= getItemCount()){
                    String lsAddrssID = MiscUtil.getNextCode(ADDRESS_TABLE, "sAddrssID", true, poGRider.getConnection(), psBranchCd);
                    poAddress.updateString("sClientID", psClientID);                    
                    poAddress.updateObject("sAddrssID", lsAddrssID);
//                    poAddress.updateObject("sAddressx", getAddress(lnCtr,"sAddressx"));
//                    poAddress.updateObject("sTownIDxx", getAddress(lnCtr,"sTownIDxx"));
//                    poAddress.updateObject("sBrgyIDxx", getAddress(lnCtr,"sBrgyIDxx"));
//                    poAddress.updateObject("sZippCode", getAddress(lnCtr,"sZippCode"));
                    poAddress.updateString("sEntryByx", poGRider.getUserID());
                    poAddress.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                    poAddress.updateString("sModified", poGRider.getUserID());
                    poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                    poAddress.updateRow();

                    lsSQL = MiscUtil.rowset2SQL(poAddress, ADDRESS_TABLE, "sProvName»sBrgyName»sTownName");

                    if (poGRider.executeQuery(lsSQL, ADDRESS_TABLE, psBranchCd, lsAddrssID.substring(0, 4)) <= 0){
                        if (!pbWithParent) poGRider.rollbackTrans();
                        psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                        return false;
                    }

                    lnCtr++;
                }
               
            } else { //update
               
                if (!pbWithParent) poGRider.beginTrans();
                
                lnCtr = 1;
                poAddress.beforeFirst();
                //while (poAddress.next()){
                while (lnCtr <= getItemCount()){
                    String lsAddrssID = (String) getAddress(lnCtr, "sAddrssID");
                    if (lsAddrssID.equals("") || lsAddrssID.isEmpty()){
                        lsAddrssID = MiscUtil.getNextCode(ADDRESS_TABLE, "sAddrssID", true, poGRider.getConnection(), psBranchCd);
                        poAddress.updateString("sClientID", getAddress("sClientID").toString());                    
                        poAddress.updateObject("sAddrssID", lsAddrssID);
                        poAddress.updateString("sEntryByx", poGRider.getUserID());
                        poAddress.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                        poAddress.updateString("sModified", poGRider.getUserID());
                        poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                        poAddress.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poAddress, ADDRESS_TABLE, "sProvName»sBrgyName»sTownName");

                        if (poGRider.executeQuery(lsSQL, ADDRESS_TABLE, psBranchCd, lsAddrssID.substring(0, 4)) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                    }else{
                        poAddress.updateString("sModified", poGRider.getUserID());
                        poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                        poAddress.updateRow();
                        lsSQL = MiscUtil.rowset2SQL(poAddress, 
                                                    ADDRESS_TABLE, 
                                                    "sProvName»sBrgyName»sTownName", 
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
                    lnCtr++;
                }
            }
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
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
    
    private String getSQ_Address(){
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
                    ", TRIM(CONCAT(b.sTownName, ', ', d.sProvName)) sTownName" + //25                
                    //", IFNULL(b.sZippCode, '') sZippCode" +
                " FROM  " + ADDRESS_TABLE + " a" +
                    " LEFT JOIN TownCity b ON a.sTownIDxx = b.sTownIDxx" +
                    " LEFT JOIN Barangay c ON a.sBrgyIDxx = c.sBrgyIDxx" + 
                    " LEFT JOIN Province d on b.sProvIDxx = d.sProvIDxx";
        return lsSQL;
    }
    //query for town
    private String getSQ_Town(){
        return "SELECT " +
                    "  IFNULL(sTownIDxx, '') sTownIDxx " +
                    ", IFNULL(sTownName, '') sTownName " +
                    ", IFNULL(sZippCode, '') sZippCode " +
                    
                " FROM TownCity " ;
    }
    
    //search town (used when "Town" is double clicked or searched)
    public boolean searchTown(int fnRow, String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Town();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownIDxx = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setAddressTable(fnRow, "sTownIDxx", loRS.getString("sTownIDxx"));
                setAddressTable(fnRow, "sTownName", loRS.getString("sTownName"));
                setAddressTable(fnRow, "sZippCode", loRS.getString("sZippCode"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Town", "sTownIDxx»sTownName");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setAddressTable(fnRow, "sTownIDxx", (String) loJSON.get("sTownIDxx"));
                setAddressTable(fnRow, "sTownName", (String) loJSON.get("sTownName"));
                setAddressTable(fnRow, "sZippCode", (String) loJSON.get("sZippCode"));
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
    public boolean searchBarangay(int fnRow, String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Barangay();
        
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
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Barangay", "sBrgyIDxx»sBrgyName");
            
            if (loJSON == null){
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
    public boolean addAddress() throws SQLException{
        int lnCtr;
        int lnRow = getItemCount();
        
        //validate if incentive is already added
//        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){
//            if (fsCode.equals((String) getAddress(lnCtr, "sAddrssID"))) return true;
//        }
        
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
    
    private boolean isEntryOK() throws SQLException{
//        poAddress.first();
//        
//        
//        if (poAddress.getString("sAddressx").isEmpty()){
//            psMessage = "Address is not set.";
//            return false;
//        }
//        
//        if (poAddress.getString("sTownIDxx").isEmpty()){
//            psMessage = "Town is not set.";
//            return false;
//        }
//        
//        if (poAddress.getString("sBrgyIDxx").isEmpty()){
//            psMessage = "Barangay is not set.";
//            return false;
//        }
        
        //validate detail
        if (getItemCount() == 0){
            psMessage = "No Address detected.";
            return false;
        }
        
        poAddress.beforeFirst();
        while (poAddress.next()){            
            if (poAddress.getString("sTownIDxx").isEmpty()){
                psMessage = "Town is not set.";
                return false;
            }
            if (poAddress.getString("sBrgyIDxx").isEmpty()){
                psMessage = "Barangay is not set.";
                return false;
            } 
            if (poAddress.getString("sAddressx").isEmpty()){
                psMessage = "Address is not set.";
                return false;
            } 
        }
        
        int lnCtr;
        int lnRow = getItemCount();
                
        boolean lbPrimary = false;
        String lsPrimary = "1";
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
