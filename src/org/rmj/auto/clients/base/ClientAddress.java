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
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
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
    private ClientMaster poMaster;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poAddress;
    
    public ClientAddress(GRider foGrider, String fsBranchCd, boolean fbWithparent){
        poGRider = foGrider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithparent;   
        
//        poMaster = new ClientMaster(poGRider, psBranchCd, true);
    }
    
    public int getEditMode(){
        return pnEditMode;
    }
//    public ClientMaster getMaster(String fsValue) throws SQLException{
//        if (poMaster.OpenRecord(fsValue))
//            return poMaster;
//        else
//            return null;
//    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    public void setAddress(int fnIndex, Object foValue) throws SQLException{
        poAddress.first();
        
        switch (fnIndex){
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
                poAddress.updateObject(fnIndex, (String) foValue);
                poAddress.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));
                break;
//            case 11:
//                if (foValue instanceof Date){
//                    poAddress.updateObject(fnIndex, foValue);
//                } else {
//                    poAddress.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
//                }
//                poAddress.updateRow();
//                
//                if (poCallback != null) poCallback.onSuccess(fnIndex, getAddress(fnIndex));
//                break;
        }               
    }
    
    public void setAddress(String fsIndex, Object foValue) throws SQLException{
        setAddress(MiscUtil.getColumnIndex(poAddress, fsIndex), foValue);
    }
    
    public Object getAddress(String fsIndex) throws SQLException{
        return getAddress(MiscUtil.getColumnIndex(poAddress, fsIndex));
    }
    
    public Object getAddress(int fnIndex) throws SQLException{
        poAddress.first();
        return poAddress.getObject(fnIndex);
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
    public boolean SearchRecord(){
        return OpenRecord("", false);
    }
    
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
                
                poAddress.updateString("sClientID", (String)poMaster.getMaster("sClientID"));
                poAddress.updateString("sAddrssID", MiscUtil.getNextCode(ADDRESS_TABLE, "sAddrssID", true, poGRider.getConnection(), psBranchCd));
                poAddress.updateString("sEntryByx", poGRider.getUserID());
                poAddress.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poAddress.updateString("sModified", poGRider.getUserID());
                poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                poAddress.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poAddress, ADDRESS_TABLE, "");
            } else { //update
//              testing update with sclient id per row
                poAddress.updateString("sAddrssID", (String) getAddress("sAddrssID"));
                poAddress.updateString("sModified", poGRider.getUserID());
                poAddress.updateObject("dModified", (Date) poGRider.getServerDate());
                poAddress.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poAddress, 
                                            ADDRESS_TABLE, 
                                            "", 
                                            "sAddrssID = " + SQLUtil.toSQL((String) getAddress("sAddrssID")) + 
                                            " AND sClientID = " + SQLUtil.toSQL((String) getAddress("sClientID")));
            }
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            
            if (poGRider.executeQuery(lsSQL, ADDRESS_TABLE, psBranchCd, "") <= 0){
                psMessage = poGRider.getErrMsg();
                if (!pbWithParent) poGRider.rollbackTrans();
                return false;
            }
            
            //save mobile
            //save email
            //save address
            //save social media
            
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
                    " a.sAddrssID" +
                    ", a.sClientID" +
                    ", a.sHouseNox" +
                    ", a.sAddressx" +
                    ", a.sTownIDxx" +
                    ", a.sBrgyIDxx" +
                    ", a.sZippCode" +
                    ", a.nPriority" +
                    ", a.nLatitude" +
                    ", a.nLongitud" +
                    ", a.sRemarksx" +
                    ", a.cOfficexx" +
                    ", a.cProvince" +
                    ", a.cPrimaryx" +
                    ", a.cBillingx" +
                    ", a.cShipping" +
                    ", a.cCurrentx" +
                    ", a.cRecdStat" +
                    ", a.sEntryByx" +
                    ", a.dEntryDte" +
                    ", a.sModified" +
                    ", a.dModified" +                
                " FROM  " + ADDRESS_TABLE + " a" +
                    " LEFT JOIN TownCity b ON a.sTownIDxx = b.sTownIDxx" +
                    " LEFT JOIN Barangay c ON a.sBrgyIDxx = c.sBrgyIDxx" + 
                    " LEFT JOIN Province d on b.sProvIDxx = d.sProvIDxx";
        return lsSQL;
    }
    
//    private String getSQ_Barangay(){
//        String lsSQL = "";
//        
//        lsSQL = "SELECT " +
//                    "  IFNULL(a.sBrgyIDxx, '') sBrgyIDxx " +
//                    ", IFNULL(a.sBrgyName, '') sBrgyName " +
//                    ", IFNULL(b.sTownIDxx, '') sTownIDxx " +
//                    ", IFNULL(b.sTownName, '') sTownName " +
//                    
//                " FROM Barangay a " +
//                "   LEFT JOIN TownCity b " +
//                "     ON a.sTownIDxx = b.sTownIDxx " +
//                " WHERE ";
//        return lsSQL;
//    }
    
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
        poAddress.first();
        
        if (poAddress.getString("sAddressx").isEmpty()){
            psMessage = "Address is not set.";
            return false;
        }
        
        if (poAddress.getString("sTownIDxx").isEmpty()){
            psMessage = "Town is not set.";
            return false;
        }
        
        if (poAddress.getString("sBrgyIDxx").isEmpty()){
            psMessage = "Barangay is not set.";
            return false;
        }
        
        
        //validate max size of string variables
        
        return true;
    }
}
