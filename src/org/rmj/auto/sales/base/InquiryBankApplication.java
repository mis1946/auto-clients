/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.sales.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
 * @author jahn april 15 2023
 */
public class InquiryBankApplication {
    private final String DEFAULT_DATE = "1900-01-01";
    private final String BANK_APPLICATION = "bank_application";
    
    private GRider poGRider;
    private String psBranchCd;
    private String psgetBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    public boolean pbWithUI;
    public String psMessage;
    public String psTransNox;
    public String psPayMode;
    
    public CachedRowSet poBankApp;    
    
    public InquiryBankApplication(GRider foGRider, String fsBranchCd, boolean fbWithParent){                    
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
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setTransNox(String fsValue) {
        psTransNox = fsValue;
    }
    
    public void setPayMode(String fsValue) {
        psPayMode = fsValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
//    //TODO add setBankApp for Bank Application
//    public void setBankApp(int fnIndex, Object foValue) throws SQLException{
//        poBankApp.first();
//    }     
//    //Bank Application setter
//    public void setMaster(String fsIndex, Object foValue) throws SQLException{
//        setBankApp(MiscUtil.getColumnIndex(poBankApp, fsIndex), foValue);
//    }
//    //Bank Application getter
//    public Object getBankApp(String fsIndex) throws SQLException{
//        return getBankApp(MiscUtil.getColumnIndex(poBankApp, fsIndex));
//    }
//    //Bank Application getter
//    public Object getBankApp(int fnIndex) throws SQLException{
//        poBankApp.first();
//        return poBankApp.getObject(fnIndex);
//    }
    
    //-----------------------------------
    
    public void setBankApp(int fnIndex, Object foValue) throws SQLException{
        poBankApp.first();        
        switch (fnIndex){                        
            case 1://sTransNox
            case 5://sSourceCD
            case 6://sSourceNo
            case 7://sBankCode
            case 8://sRemarksx
            case 14://sCancelld
            case 16://sBankname
            case 17://sPayment  
            case 18://sBankBrch
            case 19://sTownName            
                poBankApp.updateObject(fnIndex, (String) foValue);
                poBankApp.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getBankApp(fnIndex));
                break;
            case 4://cPayModex
            case 9://cTranStat
            case 10://cRecdStat
                if (foValue instanceof Integer)
                    poBankApp.updateInt(fnIndex, (int) foValue);
                else 
                    poBankApp.updateInt(fnIndex, 0);
                
                poBankApp.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getBankApp(fnIndex));  
                break;            
            case 2: //dAppliedx 
            case 3: //dApproved
            case 15://dCancelld
                 if (foValue instanceof Date){
                    poBankApp.updateObject(fnIndex, foValue);
                } else {
                    poBankApp.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poBankApp.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getBankApp(fnIndex));  
                break; 
        }
    }
    
    public void setBankApp(String fsIndex, Object foValue) throws SQLException{
        setBankApp(MiscUtil.getColumnIndex(poBankApp, fsIndex), foValue);
    }
    
    public Object getBankApp(String fsIndex) throws SQLException{
        return getBankApp(MiscUtil.getColumnIndex(poBankApp, fsIndex));
    }
    
    public Object getBankApp(int fnIndex) throws SQLException{
        poBankApp.first();
        return poBankApp.getObject(fnIndex);
    }
    
    public Object getBankAppDet(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poBankApp.absolute(fnRow);
        return poBankApp.getObject(fnIndex);
    }
    
    public Object getBankAppDet(int fnRow, String fsIndex) throws SQLException{
        return getBankAppDet(fnRow, MiscUtil.getColumnIndex(poBankApp, fsIndex));
    }
    
    //Bank Application COUNT
    public int getBankAppCount() throws SQLException{
        if (poBankApp != null){
            poBankApp.last();
            return poBankApp.getRow();
        }else{
            return 0;
        }              
    }
    
    //TODO add new record details
    public boolean NewRecord(){        
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {  
            String lsSQL = MiscUtil.addCondition(getSQ_BankApp(), "0=1");
            
            //String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1"); 
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poBankApp = factory.createCachedRowSet();
            poBankApp.populate(loRS);
            MiscUtil.close(loRS);
            
            poBankApp.last();
            poBankApp.moveToInsertRow();
            
            MiscUtil.initRowSet(poBankApp);                   
            poBankApp.updateString("cTranStat", "0");        
            poBankApp.updateObject("dAppliedx", poGRider.getServerDate());
            poBankApp.updateObject("dApproved", SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE)); 
            poBankApp.updateObject("dCancelld", SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));  

            poBankApp.insertRow();
            poBankApp.moveToCurrentRow();                                             
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    //TODO Search Record for Bank Application
    public boolean SearchRecord(){
        return true;
    }
    
    //TODO openrecord when Bank Application is double clicked
//    public boolean OpenRecord(String fsValue, boolean fbByUserID){
//        try {
//            String lsSQL;
//            ResultSet loRS;
//            //RowSetFactory factory = RowSetProvider.newFactory();
//            //open master
//            if (fbByUserID)
//                lsSQL = MiscUtil.addCondition(getSQ_BankApp(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
//            else 
//                lsSQL = MiscUtil.addCondition(getSQ_BankApp(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
//            
//            loRS = poGRider.executeQuery(lsSQL);
//            
//            if (MiscUtil.RecordCount(loRS) <= 0){
//                psMessage = "No record found.";
//                MiscUtil.close(loRS);        
//                return false;
//            }
//            
//            RowSetFactory factory = RowSetProvider.newFactory();
//            poBankApp = factory.createCachedRowSet();
//            poBankApp.populate(loRS);
//            MiscUtil.close(loRS);
//        } catch (SQLException e) {
//            psMessage = e.getMessage();
//            return false;
//        }
//        
//        pnEditMode = EditMode.READY;
//        return true;
//    }
    
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        return true;        
    }
    
    //TODO Saverecord for saving
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()){ 
                psMessage ="";
                return false;
            }
            
            String lsSQL = "";
            String lsgetBranchCd = "";
            int lnCtr;
            
            if (getInqBranchCd(psTransNox)){
                if (!psgetBranchCd.equals(psBranchCd)){
                    lsgetBranchCd = psgetBranchCd ;
                }
            } else {
                psMessage = "Error while geting inquiry branch code";
                return false;
            }
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                if (!pbWithParent) poGRider.beginTrans();
                //-------------------SAVE BANK APPLICATION------------------------
                String lsTransNox =  MiscUtil.getNextCode(BANK_APPLICATION, "sTransNox", true, poGRider.getConnection(), psBranchCd); 
                poBankApp.updateObject("sTransNox", lsTransNox);
                poBankApp.updateString("sSourceNo", psTransNox);
                poBankApp.updateString("sEntryByx", poGRider.getUserID());
                poBankApp.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poBankApp.updateString("sModified", poGRider.getUserID());
                poBankApp.updateObject("dModified", (Date) poGRider.getServerDate());
                poBankApp.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poBankApp, BANK_APPLICATION, "sBankname»sPayment»sBankBrch»sTownName");
                
                if (poGRider.executeQuery(lsSQL, BANK_APPLICATION, psBranchCd, lsgetBranchCd) <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                }
                                              
                if (!pbWithParent) poGRider.commitTrans();            
                pnEditMode = EditMode.READY;
                return true;
            } else { //update 
                if (!pbWithParent) poGRider.beginTrans();
                
                //set transaction number on records
                String lsTransNox = (String) getBankApp("sTransNox");
                
                poBankApp.updateString("sModified", poGRider.getUserID());
                poBankApp.updateObject("dModified", (Date) poGRider.getServerDate());
                poBankApp.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poBankApp, 
                                            BANK_APPLICATION, 
                                            "sBankname»sPayment»sBankBrch»sTownName", 
                                            "sTransNox = " + SQLUtil.toSQL(lsTransNox));
                if (poGRider.executeQuery(lsSQL, BANK_APPLICATION, psBranchCd, lsgetBranchCd) <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
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
        
        pnEditMode = EditMode.READY;
        return true;
    }
    
//    public boolean addBankApplication() throws SQLException{
//        if (psPayMode != "0") {
//            psMessage = "Unable to add bank application for Cash Payments.";                  
//            return false;
//        }
//        String lsSQL;
//        if (poBankApp == null){
//            lsSQL = MiscUtil.addCondition(getSQ_BankApp(), "0=1");
//            ResultSet loRS = poGRider.executeQuery(lsSQL);
//            
//            RowSetFactory factory = RowSetProvider.newFactory();
//            poBankApp = factory.createCachedRowSet();
//            poBankApp.populate(loRS);
//            MiscUtil.close(loRS);
//        }
//        
//        poBankApp.last();
//        poBankApp.moveToInsertRow();
//
//        MiscUtil.initRowSet(poBankApp);                    
//        poBankApp.updateString("cTranStat", "0");        
//        poBankApp.updateObject("dAppliedx", poGRider.getServerDate());
//        poBankApp.updateObject("dApproved", SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
//        //poBankApp.updateObject("dApproved", poGRider.getServerDate());        
//        poBankApp.insertRow();
//        poBankApp.moveToCurrentRow();
//        
//        pnEditMode = EditMode.ADDNEW;
//        return true;        
//    }
    
    public boolean CancelBankApp(String fsValue) throws SQLException{
        if (pnEditMode != EditMode.READY){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        psMessage = "";        
        if (((String) getBankApp("cTranStat")).equals("3")){
            psMessage = "Unable to cancel transactions.";
            return false;
        }
                
        //validation for allowed employee to cancel
        if (((String) getBankApp("cTranStat")).equals("0")){
//            if (!(MAIN_OFFICE.contains(p_oApp.getBranchCode()) &&
//                p_oApp.getDepartment().equals(AUDITOR))){
//                p_sMessage = "Only CM Department can cancel confirmed transactions.";
//                return false;
//            } else {
//                if ("1".equals((String) getMaster("cApprovd2"))){
//                    p_sMessage = "This transaction was already CM Confirmed. Unable to disapprove.";
//                    return false;
//                }
//            }
        }
        String lsgetBranchCd = "";
        if (getInqBranchCd(fsValue)){
            if (!psgetBranchCd.equals(psBranchCd)){
                lsgetBranchCd = psgetBranchCd ;
            }
        } else {
            psMessage = "Error while geting inquiry branch code";
            return false;
        }
        
        //String lsTransNox = (String) getBankApp("sTransNox");
        String lsSQL = "UPDATE " + BANK_APPLICATION + " SET" +
                            " cTranStat = '3'" +
                            " ,sCancelld = " + SQLUtil.toSQL(poGRider.getUserID()) +
                            " ,dCancelld = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                        " WHERE sTransNox = " + SQLUtil.toSQL(fsValue);
        
        if (poGRider.executeQuery(lsSQL, BANK_APPLICATION, psBranchCd,lsgetBranchCd) <= 0){
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }
                        
        psMessage = "Transaction successfully cancelled";
//        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean loadBankApplication(String fsValue, boolean fbByCode ){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            if (fbByCode){
                lsSQL = MiscUtil.addCondition(getSQ_BankApp(), "a.sSourceNo = " + SQLUtil.toSQL(fsValue));                
            }else{
                lsSQL = MiscUtil.addCondition(getSQ_BankApp(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));                
            }  
            
            loRS = poGRider.executeQuery(lsSQL);
            
//            if (MiscUtil.RecordCount(loRS) <= 0){
//                psMessage = "No record found.";
//                MiscUtil.close(loRS);        
//                return false;
//            }

            
            poBankApp = factory.createCachedRowSet();
            poBankApp.populate(loRS);
            MiscUtil.close(loRS);
            
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }        
    
    //TODO query for retrieving bank application
    private String getSQ_BankApp(){
        return "SELECT " +
                    " a.sTransNox " +//1                    
                    " ,a.dAppliedx " +//2
                    " ,a.dApproved " +//3
                    " ,a.cPayModex " +//4
                    " ,a.sSourceCD " +//5
                    " ,a.sSourceNo " +//6
                    " ,a.sBankIDxx " +//7
                    " ,a.sRemarksx " +//8
                    " ,a.cTranStat " +//9
                    " ,a.sEntryByx " +//10
                    " ,a.dEntryDte " +//11
                    " ,a.sModified " +//12
                    " ,a.dModified " +//13
                    " ,a.sCancelld " +//14
                    " ,a.dCancelld " +//15
                    " ,IFNULL(b.sBankName, '') as sBankName" +//16
                    " ,IFNULL(f.cPayModex, '') as sPayment" +//17
                    " ,IFNULL(b.sBankBrch, '') as sBankBrch" +//18
                    " ,TRIM(CONCAT(c.sTownName, ', ', e.sProvName)) sTownName" +//19
                    //" ,a.sBankIDxx " +//20
                " FROM bank_application a " +
                " LEFT JOIN banks b ON b.sBankIDxx = a.sBankIDxx " +
                " LEFT JOIN TownCity c ON c.sTownIDxx = b.sTownIDxx" +                
                " LEFT JOIN Province e on e.sProvIDxx = c.sProvIDxx" + 
                " LEFT JOIN customer_inquiry f ON f.sTransNox = a.sSourceNo";
    }
    
    //TODO query for retrieving bank
    private String getSQ_Bank(){
        return "SELECT" +
                    " a.sBankIDxx" + 
                    ", a.sBankName" +                                                                                                                              
                    ", TRIM(CONCAT(b.sTownName, ', ', d.sProvName)) sTownName" + 
                    ", a.sBankBrch" + 
                " FROM banks a " +
                    " LEFT JOIN TownCity b ON b.sTownIDxx = a.sTownIDxx" +                    
                    " LEFT JOIN Province d on d.sProvIDxx = b.sProvIDxx" ;     
    }
    
    public boolean searchBank(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Bank();
                            
        lsSQL = MiscUtil.addCondition(lsSQL, "sBankName LIKE " + SQLUtil.toSQL(fsValue + "%"));       
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                System.out.println(loRS.getString("sBankName"));
                setBankApp("sBankIDxx", loRS.getString("sBankIDxx"));
                setBankApp("sBankName", loRS.getString("sBankName")); 
                setBankApp("sBankBrch", loRS.getString("sBankBrch")); 
                setBankApp("sTownName", loRS.getString("sTownName"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
                       
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        lsSQL, 
                                                        fsValue,
                                                        "Code»Bank Name»Bank Branch»Address", 
                                                        "sBankIDxx»sBankName»sBankBrch»sTownName",
                                                        "sBankIDxx»sBankName»sBankBrch»sTownName",
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setBankApp("sBankIDxx", (String) loJSON.get("sBankIDxx"));
                setBankApp("sBankName", (String) loJSON.get("sBankName")); 
                setBankApp("sBankBrch", (String) loJSON.get("sBankBrch"));
                setBankApp("sTownName", (String) loJSON.get("sTownName"));        
            }
        }
        
        return true;
    }
    
    private String getSQ_InqBranchCd(){
        return " SELECT "
                + " IFNULL(sBranchCd, '') sBranchCd"
                + " FROM customer_Inquiry";
    }
    
    private boolean getInqBranchCd(String fsValue) throws SQLException{
        String lsSQL = MiscUtil.addCondition(getSQ_InqBranchCd(), " sTransNox = " + SQLUtil.toSQL(fsValue));            
                
        ResultSet loRS;
        lsSQL += " LIMIT 1";
        loRS = poGRider.executeQuery(lsSQL);
        System.out.println(lsSQL);
        if (loRS.next()){
             psgetBranchCd = loRS.getString("sBranchCd");            
        } else {
            psgetBranchCd = "";
            psMessage = "No record found.";
            return false;
        }
        return true;
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poBankApp.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poBankApp.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poBankApp.getMetaData().getColumnType(lnCtr));
            if (poBankApp.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poBankApp.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poBankApp.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }
    public LocalDate strToDate(String val){
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }
    
    //TODO validation for entries
    public boolean isEntryOK() {        
        try {
            poBankApp.first();
            
            if (poBankApp.getString("sBankIDxx").isEmpty()){
                psMessage = "Please Enter Bank Name.";
                return false;
            }

            if (poBankApp.getString("cPayModex").isEmpty()){
                psMessage = "Please enter Mode of Payment.";
                return false;
            } 
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        
        return true;
    }    
    
    
}
