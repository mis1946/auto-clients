/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.sales.base;

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

/**
 *
 * @author User
 */
public class InquiryFollowUp {
    private final String INQUIRY_FOLLOWUP = "customer_inquiry_followup";
    private final String DEFAULT_DATE = "1900-00-00";
    private GRider poGRider;
    private String psBranchCd;
    private String psgetBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    public String psMessage;
    public String psTransNox;
    public String psVSPNox;
    private boolean pbisFollowUp;
    
    private CachedRowSet poFollowUp;
    
    public InquiryFollowUp(GRider foGRider, String fsBranchCd, boolean fbWithParent){
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
    
    public int getFollowUpCount() throws SQLException{
        if (poFollowUp != null){
            poFollowUp.last();
            return poFollowUp.getRow();
        }else{
            return 0;
        }              
    }
    
    public void setTransNox(String fsValue) {
        psTransNox = fsValue;
    }
    
    public void setVSPNox(String fsValue) {
        psVSPNox = fsValue;
    }
    
    public void setisFollowUp(boolean fbValue) {
        pbisFollowUp = fbValue;
    }
    
    public void setFollowUp(int fnIndex, Object foValue) throws SQLException{ 
        //poFollowUp.first();
        poFollowUp.first();
        
        switch (fnIndex){            
            case 2://sReferNox
            case 4://sRemarksx
            case 5://sMessagex
            case 6://sMethodCd
            case 7://sSclMedia
            case 9://tFollowUp
            case 10://sGdsCmptr
            case 11://sMkeCmptr
            case 12://sDlrCmptr
            case 13://sRspnseCd
            case 16://sPlatform
                poFollowUp.updateObject(fnIndex, (String) foValue);
                poFollowUp.updateRow();
                
                //if (poCallback != null) poCallback.onSuccess(fnIndex, getFollowUp(fnIndex));
                break;            
            case 3://dTransact
            case 8://dFollowUp
                if (foValue instanceof Date){
                    poFollowUp.updateObject(fnIndex, foValue);
                } else {
                    poFollowUp.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poFollowUp.updateRow();
                
                //if (poCallback != null) poCallback.onSuccess(fnIndex, getFollowUp(fnIndex));
                break;
        }
    }
    
    public void setFollowUp(String fsIndex, Object foValue) throws SQLException{
        setFollowUp(MiscUtil.getColumnIndex(poFollowUp, fsIndex), foValue);
    }
    
    public Object getFollowUp(String fsIndex) throws SQLException{
        return getFollowUp(MiscUtil.getColumnIndex(poFollowUp, fsIndex));
    }
    
    public Object getFollowUp(int fnIndex) throws SQLException{
        poFollowUp.first();
        return poFollowUp.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poFollowUp.absolute(fnRow);
        return poFollowUp.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poFollowUp, fsIndex));
    }
    
    /**
     * Initializes the master data for adding a new entry.	
     * @return 
     */
    public boolean NewRecord(){                       //      
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {  
            String lsSQL = MiscUtil.addCondition(getSQ_FollowUp(), "0=1");
            
            //String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1"); 
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poFollowUp = factory.createCachedRowSet();
            poFollowUp.populate(loRS);
            MiscUtil.close(loRS);
            
            poFollowUp.last();
            poFollowUp.moveToInsertRow();
            
            MiscUtil.initRowSet(poFollowUp);                   
            //poFollowUp.updateString("cTranStat", "0");  
            //poFollowUp.updateObject("sMethodCd", "0");
            poFollowUp.updateObject("dTransact", poGRider.getServerDate());
            poFollowUp.updateObject("dFollowUp", poGRider.getServerDate());  

            poFollowUp.insertRow();
            poFollowUp.moveToCurrentRow();  
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        pnEditMode = EditMode.ADDNEW;
        return true;
    
    }
    /**
     * Saves a record to the database.
     * This method is responsible for adding a new record or updating an existing one based on the edit mode. It performs data validation and handles database transactions.
     * @return 
     */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            
            String lsSQL = "";
            String lsgetBranchCd = "";
            if (getInqBranchCd(psTransNox)){
                if (!psgetBranchCd.equals(psBranchCd)){
                    lsgetBranchCd = psgetBranchCd ;
                }
            } else {
                psMessage = "Error while geting inquiry branch code";
                return false;
            }
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                poFollowUp.first();
                String lsTransNox = MiscUtil.getNextCode(INQUIRY_FOLLOWUP, "sTransNox", true, poGRider.getConnection(), psBranchCd);
                poFollowUp.updateString("sTransNox", lsTransNox);                                                                             
                poFollowUp.updateString("sReferNox", psTransNox);
                poFollowUp.updateString("sEntryByx", poGRider.getUserID());
                poFollowUp.updateObject("dEntryDte", (Date) poGRider.getServerDate());               
                poFollowUp.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poFollowUp, INQUIRY_FOLLOWUP, "sPlatform");
            }
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            
            if (poGRider.executeQuery(lsSQL, INQUIRY_FOLLOWUP, psBranchCd, lsgetBranchCd) <= 0){
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
     * Prepares to update a record in the data.
     * This method creates copies of the original data to be updated and sets the edit mode to UPDATE.
     * @return 
     */
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        return true; 
    }
    
    private String getSQ_FollowUp(){
        return "SELECT" +
                    " a.sTransNox  " + //1
                    " ,IFNULL(a.sReferNox,'')sReferNox " + //2
                    " ,a.dTransact " + //3
                    " ,IFNULL(a.sRemarksx,'')sRemarksx " + //4
                    " ,IFNULL(a.sMessagex,'')sMessagex " + //5
                    " ,IFNULL(a.sMethodCd,'')sMethodCd " + //6
                    " ,IFNULL(a.sSclMedia,'')sSclMedia " + //7
                    " ,a.dFollowUp " + //8
                    " ,IFNULL(a.tFollowUp,'')tFollowUp " + //9
                    " ,IFNULL(a.sGdsCmptr,'')sGdsCmptr " + //10
                    " ,IFNULL(a.sMkeCmptr,'')sMkeCmptr " + //11
                    " ,IFNULL(a.sDlrCmptr,'')sDlrCmptr " + //12
                    " ,IFNULL(a.sRspnseCd,'')sRspnseCd " + //13
                    " ,a.sEntryByx " + //14
                    " ,a.dEntryDte " + //15
                    " ,IFNULL(b.sPlatform,'') sPlatform" + //16
                " FROM " + INQUIRY_FOLLOWUP + " a " +
                " LEFT JOIN online_platforms b ON b.sTransNox = a.sSclMedia";
    }
    
    /**
     * Load list for a record based on the specified value.
     * @param fsValue the value used for loading data base on refer no or inquiry code
     * @param fbByCode determines if the load is performed by inquiry code or inquiry follow-up refer no.
     * @return 
     */
    public boolean loadFollowUp(String fsValue, boolean fbByCode){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            if (fbByCode){
                lsSQL = MiscUtil.addCondition(getSQ_FollowUp(), "sReferNox = " + SQLUtil.toSQL(fsValue));                
            }else{
                lsSQL = MiscUtil.addCondition(getSQ_FollowUp(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));                
            }  
            
            loRS = poGRider.executeQuery(lsSQL);
            
//            if (MiscUtil.RecordCount(loRS) <= 0){
//                psMessage = "No record found.";
//                MiscUtil.close(loRS);        
//                return false;
//            }
            
            poFollowUp = factory.createCachedRowSet();
            poFollowUp.populate(loRS);
            MiscUtil.close(loRS);
            
            psMessage = "Follow-Up successfully added.";
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }
    private String getSQ_Online() {
        return "SELECT " +
                    " sTransNox " +
                    " ,sPlatform " +
                    " ,sWebSitex " +
                "FROM online_platforms";
    }
    
    /**
     * Search for Online Platforms
     * @param fsValue the value used for loading data base on passed data
     * @param fbByCode determines if the search is performed by display manual selection on jason search (True) or automatically set data based on query result (False).
     * @return
     * @throws SQLException 
     */
    public boolean searchPlatform(String fsValue, boolean fbByCode) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getSQ_Online(), " sPlatform LIKE " + SQLUtil.toSQL(fsValue + "%"));            
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setFollowUp("sPlatform", loRS.getString("sPlatform"));
                setFollowUp("sSclMedia", loRS.getString("sTransNox"));               
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        lsSQL, 
                                                        fsValue, 
                                                        "Platform Name", 
                                                        "sPlatform",
                                                        "sPlatform»sWebSitex",
                                                        0);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setFollowUp("sPlatform", (String) loJSON.get("sPlatform"));
                setFollowUp("sSclMedia", (String) loJSON.get("sTransNox"));                
            }
        }
        
        return true;
    }
    
    /**
     * Validate data before saving.
     * @return 
     */
    public boolean isEntryOK(){
        try {
            poFollowUp.first();
            
            String ls_sMethodCd = poFollowUp.getString("sMethodCd");
            String ls_sSclMedia = poFollowUp.getString("sSclMedia");
            String ls_sRemarksx = poFollowUp.getString("sRemarksx");
            String ls_sMessagex = poFollowUp.getString("sMessagex");
            
            if (pbisFollowUp){
                if (ls_sMethodCd.isEmpty()){
                    psMessage = "Please Select Action Performed.";
                    return false;
                }

                if (ls_sMethodCd.equals("SOCIAL MEDIA")){
                    if(ls_sSclMedia.isEmpty()){
                        psMessage = "Please select your platform.";
                        return false;
                    }                
                }
                
                if (ls_sMessagex.isEmpty() || ls_sMessagex.length() < 20){
                    psMessage = "Please Enter Message. 20 characters minimum.";
                    return false;
                } 
            }
            
            if (ls_sRemarksx.isEmpty() || ls_sRemarksx.length() < 20){
                psMessage = "Please Enter Remarks. 20 characters minimum.";
                return false;
            }    
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        
        return true;
    }
    
    /**
     * Lost sale of Inquiry or VSP
     * 
     * @param fbIsLostSale identifies if inquiry will be lost sale or not.
     * @param fsState identifies what state belong in inquiry (True), else in VSP (False) has been used.
     * @return
     * @throws SQLException 
     */
    public boolean LostSale(boolean fbIsLostSale, boolean fsState) throws SQLException{
                
        psMessage = "";    
        String lsSource = "";
        String lsSourceNox = "";
        String lsgetBranchCd = "";
        if (getInqBranchCd(psTransNox)){
            if (!psgetBranchCd.equals(psBranchCd)){
                lsgetBranchCd = psgetBranchCd ;
            }
        } else {
            psMessage = "Error while geting inquiry branch code";
            return false;
        }
        
        if (fsState){
            lsSource = "INQUIRY";
            lsSourceNox = psTransNox;
        } else {
            lsSource = "VSP";
            lsSourceNox = psVSPNox;
        }
        
        String lsSQL = "";
        //Update Inquiry to LOST SALE
        if(fbIsLostSale){
            lsSQL = "UPDATE customer_inquiry SET" +
                    " cTranStat = '2'" +                           
                    " ,dLastUpdt = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                    " WHERE sTransNox = " + SQLUtil.toSQL(psTransNox);

        } else {
            //Update Inquiry to ON PROCESS
            lsSQL = "UPDATE customer_inquiry SET" +
                    " cTranStat = '1'" +
                    " WHERE sTransNox = " + SQLUtil.toSQL(psTransNox);
        }
        
        if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd,lsgetBranchCd) <= 0){
            psMessage = "UPDATE CUSTOMER INQUIRY: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }
        
        lsSQL = "INSERT INTO cancellation_master SET" +
                            " sTransNox = "  + SQLUtil.toSQL(MiscUtil.getNextCode("cancellation_master", "sTransNox", true, poGRider.getConnection(), psBranchCd)) +
                            " ,sRemarksx = " + SQLUtil.toSQL(getFollowUp("sRemarksx")) +
                            " ,sSourceCD = " + SQLUtil.toSQL(lsSource) +
                            " ,sSourceNo = " + SQLUtil.toSQL(lsSourceNox) + //vsp code or inquiry id
                            " ,sReferNox = " + SQLUtil.toSQL(psTransNox) + //inquiry  code
                            " ,dTransact = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                            " ,sEntryByx = " + SQLUtil.toSQL(poGRider.getUserID()) +
                            " ,dEntryDte = " + SQLUtil.toSQL(poGRider.getServerDate());                        
        
        if (poGRider.executeQuery(lsSQL, "cancellation_master", psBranchCd,lsgetBranchCd) <= 0){
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }
        
        psMessage = "Transaction successfully cancelled";
        pnEditMode = EditMode.UNKNOWN;        
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
        
        int lnRow = poFollowUp.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poFollowUp.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poFollowUp.getMetaData().getColumnType(lnCtr));
            if (poFollowUp.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poFollowUp.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poFollowUp.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }             
}
