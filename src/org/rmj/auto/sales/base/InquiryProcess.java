/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.sales.base;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.StringUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.auto.clients.base.CompareRows;
import org.rmj.auto.clients.base.CompareRows;

/**
 *
 * @author User
 */
public class InquiryProcess {
    private final String DEFAULT_DATE = "1900-01-01";
    private final String RESERVE_TABLE = "customer_inquiry_reservation";
    private final String REQUIREMENTS_TABLE = "customer_inquiry_requirements";
    
    private GRider poGRider;
    private String psBranchCd;
    private String psgetBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psTransNox;
    private String psCLientID;
    
    public CachedRowSet poInqReq;
    public CachedRowSet poInqReqSrc;
    public CachedRowSet poReserve;
    private CachedRowSet poOriginalReserve;
    private CachedRowSet poOriginalReq;
    
    public InquiryProcess(GRider foGRider, String fsBranchCd, boolean fbWithParent){                    
        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;                       
    }
    
    public int getEditMode(){
        return pnEditMode;
    }
    
    public void setTransNox(String fsValue) {
        psTransNox = fsValue;
    }
    
    public void setClientID(String fsValue) {
        psCLientID = fsValue;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    //--------------------------------INQUIRY REQUIREMENTS----------------------
    //TODO add setInqProc for Inquiry Process
    /**
        Sets the value of a specific field of a specific row in the Inquiry requirement table.

        @param fnRow the row number of the Inquiry requirement table to set the value for

        @param fnIndex the index of the field to set the value for

        @param foValue the new value to set for the field

        @throws SQLException if there is an error accessing the database
    */
    public void setInqReq(int fnRow, int fnIndex, Object foValue) throws SQLException{
        
        poInqReq.absolute(fnRow);        
        switch (fnIndex){          
            case 2://nEntryNox
            case 3://sRqrmtCde            
            case 5://sReceived            
            case 7://sDescript
            case 10://sCompnyNm
                poInqReq.updateObject(fnIndex, (String) foValue);
                poInqReq.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getInqReq(fnIndex));
                break;
            case 4://cSubmittd
            case 8://cPayModex
            case 9://cCustGrpx
                if (foValue instanceof Integer)
                    poInqReq.updateInt(fnIndex, (int) foValue);
                else 
                    poInqReq.updateInt(fnIndex, 0);
                
                poInqReq.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getInqReq(fnIndex));  
                break;
            case 6://dReceived
                if (foValue instanceof Date){
                    poInqReq.updateObject(fnIndex, foValue);
                } else {
                    poInqReq.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poInqReq.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getInqReq(fnIndex));
                break;
        }       
//        System.out.println("Updated row: " + poInqReq.getRow());
        System.out.println("Values: " + poInqReq.getString(2) + ", " + poInqReq.getString(3) + ", " +  foValue);
    }     
    //Inquiry req setter
    /**
    Sets the value of a specific field in the inquiry requirements record at the given row index using the provided value.
        @param fnRow The index of the row where the value will be updated.
        @param fsIndex The name of the field to be updated.
        @param foValue The new value to be set in the field.
        @throws SQLException if a database access error occurs, or this method is called on a closed result set, or the row index is not valid.
    */
    public void setInqReq(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setInqReq(fnRow, MiscUtil.getColumnIndex(poInqReq, fsIndex), foValue);
    }
       
    //Inquiry Process getter
    /**
        Returns the value of the specified field in the current row of the inquiry requirements ResultSet.
        @param fsIndex the name of the field to retrieve
        @return the value of the specified field in the current row
        @throws SQLException if a database access error occurs or if the column label is not found
    */
    public Object getInqReq(String fsIndex) throws SQLException{
        return getInqReq(MiscUtil.getColumnIndex(poInqReq, fsIndex));
    }
    
    
    //Inquiry Process getter
    /**
        Retrieves the value of the specified column from the first row of the Inquiry requirements ResultSet.
        @param fnIndex the index of the column to retrieve the value from
        @return the value of the specified column from the first row of the Inquiry Request ResultSet
        @throws SQLException if a database access error occurs or this method is called on a closed result set
    */
    public Object getInqReq(int fnIndex) throws SQLException{
        poInqReq.first();
        return poInqReq.getObject(fnIndex);
    }
    
    
    
    public Object getInqReq(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poInqReq.absolute(fnRow);
        return poInqReq.getObject(fnIndex);
    }
    
    public Object getInqReq(int fnRow, String fsIndex) throws SQLException{
        if (getInqReqCount()== 0 || fnRow > getInqReqCount()) return null;
        return getInqReq(fnRow, MiscUtil.getColumnIndex(poInqReq, fsIndex));
    }   
    
    //Inquiry requirements COUNT
    public int getInqReqCount() throws SQLException{
        if (poInqReq != null){
            poInqReq.last();
            return poInqReq.getRow();
        }else{
            return 0;
        }              
    }     
    
    //--------------------------------REQUIREMENT SOURCE------------------------
    
    //Inquiry Process getter
    public Object getInqReqSrc(String fsIndex) throws SQLException{
        return getInqReqSrc(MiscUtil.getColumnIndex(poInqReqSrc, fsIndex));
    }
    //Inquiry Process getter
    public Object getInqReqSrc(int fnIndex) throws SQLException{
        poInqReqSrc.first();
        return poInqReqSrc.getObject(fnIndex);
    }
    
    public Object getInqReqSrc(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poInqReqSrc.absolute(fnRow);
        return poInqReqSrc.getObject(fnIndex);
    }
    
    public Object getInqReqSrc(int fnRow, String fsIndex) throws SQLException{
        if (getInqReqSrcCount()== 0 || fnRow > getInqReqSrcCount()) return null;
        return getInqReqSrc(fnRow, MiscUtil.getColumnIndex(poInqReqSrc, fsIndex));
    }      
    
    //Inquiry requirements COUNT
    public int getInqReqSrcCount() throws SQLException{
        if (poInqReqSrc != null){
            poInqReqSrc.last();
            return poInqReqSrc.getRow();
        }else{
            return 0;
        }              
    }  
    
    //LOAD REQUIREMENTS SOURCE
    /**

    Loads the inquiry requirements source based on the given type and payment mode.

    @param fsType the customer group type

    @param fsPaymode the payment mode

    @return true if successful, false otherwise

    @throws SQLException if a database access error occurs
    */
    public boolean loadRequirementsSource(String fsType, String fsPaymode) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();                
        
        lsSQL = MiscUtil.addCondition(getSQ_InqReqSource(), "b.cPayModex = " + SQLUtil.toSQL(fsPaymode) +
                                                            " AND b.cCustGrpx = " + SQLUtil.toSQL(fsType));
        loRS = poGRider.executeQuery(lsSQL);
        poInqReqSrc = factory.createCachedRowSet();
        poInqReqSrc.populate(loRS);
        MiscUtil.close(loRS);
                
        return true;
    }
    
    //Load Requirements
    /**
    * Retrieves the inquiry requirements for a given transaction number and saves them in a cached row set.
    *
    * @param fsTransNox the transaction number of the inquiry requirements to load
    * @return {@code true} if the inquiry requirements are successfully loaded and saved in a cached row set, {@code false} otherwise
    * @throws SQLException if a database access error occurs
    */
    public boolean loadRequirements(String fsTransNox) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();                
        
        lsSQL = MiscUtil.addCondition(getSQ_InqRequirements(), "a.sTransNox = " + SQLUtil.toSQL(fsTransNox));
        
        loRS = poGRider.executeQuery(lsSQL);
        poInqReq = factory.createCachedRowSet();
        poInqReq.populate(loRS);
        MiscUtil.close(loRS);
                
        return true;
    }
    
    //TODO remove Inq requirements tableview   
    /**

    Removes the inquiry requirement row that has the specified value in the "sRqrmtCde" column.

    @param fsValue the value to search for in the "sRqrmtCde" column

    @return true if a row with the specified value was found and deleted, false otherwise

    @throws SQLException if a database access error occurs
    */
    public boolean removeInqReq(String fsValue) throws SQLException{
        if (getInqReqCount() == 0) {
            psMessage = "No data to delete.";
            return false;
        }
        int lnCtr = 1;
        poInqReq.beforeFirst();
        while (poInqReq.next()) {
            String sRqrmtCde = poInqReq.getString("sRqrmtCde");
            if (sRqrmtCde != null && sRqrmtCde.equals(fsValue)) {
                poInqReq.updateObject("cSubmittd", "0");
                poInqReq.updateRow();
                //poInqReq.deleteRow();
                return true;
            }
        }

        return false;
    }
    
    //TODO: add requirements when button is checked
    /**

    Adds a new row to the inquiry requirements data set.

    @return {@code true} if a new row was successfully added, {@code false} otherwise

    @throws SQLException if a database access error occurs
    */
    public boolean addRequirements() throws SQLException{
        
        if (poInqReq == null){
            String lsSQL = MiscUtil.addCondition(getSQ_InqRequirements(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poInqReq = factory.createCachedRowSet();
            poInqReq.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        poInqReq.last();
        poInqReq.moveToInsertRow();

        MiscUtil.initRowSet(poInqReq);                            
        poInqReq.insertRow();
        poInqReq.moveToCurrentRow();
        
        return true;                
    }
    
    //TODO query for sales executives
    private String getSQ_SalesExecutive(){
        return " SELECT " +
                    " a.sClientID " +
                    ", IFNULL(b.sCompnyNm, '') sCompnyNm " +
                " FROM sales_executive a " +
                " LEFT JOIN client_master b ON b.sClientID = a.sClientID " ;
    }
    /**
    * Searches for a sales executive based on the provided value and sets the corresponding information in the specified row.
    *
    * @param fnRow     The row where the information should be set.
    * @param fsValue   The value to search for.
    * @param fbByCode  Indicates whether the search is by code (true) or by name (false).
    * @return true if a record is found and information is set; false if no record is found.
    */
    public boolean searchSalesExec(int fnRow,String fsValue, boolean fbByCode) throws SQLException{
                        
        String lsSQL = MiscUtil.addCondition(getSQ_SalesExecutive(), " sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%"));            
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setInqReq(fnRow,"sReceived", loRS.getString("sClientID"));
                setInqReq(fnRow,"sCompnyNm", loRS.getString("sCompnyNm"));               
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
                                                        "Sales Executive Name", 
                                                        "sCompnyNm",
                                                        "sClientID»sCompnyNm",
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setInqReq(fnRow,"sReceived", (String) loJSON.get("sClientID"));
                setInqReq(fnRow,"sCompnyNm", (String) loJSON.get("sCompnyNm"));                
            }
        }
        
        return true;
    }
    
    //TODO add new record details
    /**
    * Sets the current mode to "Add New" for creating a new record.
    *
    * @return true to indicate the mode has been set to "Add New."
    */
    public boolean NewRecord(){
        pnEditMode = EditMode.ADDNEW;        
        return true;
    }
    
    //----------------------------INQUIRY RESERVATION---------------------------
    
    public void setInqRsv(int fnRow,int fnIndex, Object foValue) throws SQLException{
        poReserve.absolute(fnRow);          
        switch (fnIndex){                   
            case 3://sReferNox
            case 4://sClientID            
            case 6://sRemarksx            
            case 7://sDescript 
            case 8://sSourceNo
            case 11://sResrvCde            
            case 14://sApproved
            case 20://sCompnyNm              
            case 21://sDescript
            case 22://sSeNamexx
            case 23://sApprovBy
            case 24://sBranchNm
                poReserve.updateObject(fnIndex, (String) foValue);
                poReserve.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getInqRsv(fnIndex));
                break;
            case 12://cResrvTyp
            case 13://cTranStat           
                if (foValue instanceof Integer)
                    poReserve.updateInt(fnIndex, (int) foValue);
                else 
                    poReserve.updateInt(fnIndex, 0);
                
                poReserve.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getInqRsv(fnIndex));  
                break;            
            case 5: //nAmountxx
                poReserve.updateDouble(fnIndex, 0.00);
                
                if (StringUtil.isNumeric(String.valueOf(foValue))) 
                    poReserve.updateDouble(fnIndex, (double) foValue);
                
                poReserve.updateRow();   
                break;
            case 2://dTransact
            case 15://dApproved
                if (foValue instanceof Date){
                    poReserve.updateObject(fnIndex, foValue);
                } else {
                    poReserve.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poReserve.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getInqRsv(fnIndex));
                break;
        }
    }     
    //Inquiry reserve setter
    public void setInqRsv(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setInqRsv(fnRow,MiscUtil.getColumnIndex(poReserve, fsIndex), foValue);
    }
    
    //Inquiry reserve getter
    public Object getInqRsv(int fnIndex) throws SQLException{
        poReserve.first();
        return poReserve.getObject(fnIndex);
    }    
    
    //Inquiry reserve getter
    public Object getInqRsv(String fsIndex) throws SQLException{
        return getInqRsv(MiscUtil.getColumnIndex(poReserve, fsIndex));
    }
   
    public Object getInqRsv(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poReserve.absolute(fnRow);
        return poReserve.getObject(fnIndex);
    }
         
    public Object getInqRsv(int fnRow, String fsIndex) throws SQLException{
        if (getReserveCount()== 0 || fnRow > getReserveCount()) return null;
        return getInqRsv(fnRow, MiscUtil.getColumnIndex(poReserve, fsIndex));
    }               
        
    //Inquiry Reservation COUNT
    public int getReserveCount() throws SQLException{
        if (poReserve != null){
            poReserve.last();
            return poReserve.getRow();
        }else{
            return 0;
        }              
    }                          
    
    //TODO: add reservation
    /**
        Adds a new row to the reserve dataset.

        If poReserve is null, a new CachedRowSet with no data will be created.

        The new row will be added at the end of the dataset and the cursor will be moved to the insert row.

        Initializes the new row with default values by calling MiscUtil.initRowSet.

        @return true if a new row has been successfully added, false otherwise.

        @throws SQLException if a database access error occurs or the connection is closed.
    */
    public boolean addReserve() throws SQLException{
        
        if (poReserve == null){
            //String lsSQL = MiscUtil.addCondition(getSQ_Reserve(), "0=1");
            String lsSQL = getSQ_Reserve() + " WHERE 0=1";
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poReserve = factory.createCachedRowSet();
            poReserve.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        poReserve.last();
        poReserve.moveToInsertRow();

        MiscUtil.initRowSet(poReserve);                    
        poReserve.updateString("cTranStat", "0");
        poReserve.updateObject("dApproved", SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
        //poReserve.updateObject("dApproved", DEFAULT_DATE);        
        poReserve.insertRow();
        poReserve.moveToCurrentRow();
        
        return true;
    }
    
    /**
        Removes a reservation record at a specified row from the database.

        @param fnRow the row number of the record to be removed

        @return true if the reservation record is successfully removed, false otherwise

        @throws SQLException if a database access error occurs
    */
    public boolean removeInqRes(int fnRow) throws SQLException{
        if (getReserveCount() == 0) {
            psMessage = "No Reservation to delete.";
            return false;
        }
        
        poReserve.absolute(fnRow);
        if (!poReserve.getString("sTransNox").isEmpty()){
            psMessage = "Reservation already saved unable to delete, Please cancel instead";
            return false;
        }
        
        poReserve.deleteRow();
        return true;                
    }
        
    /**
        Loads reservation details based on the given search value and search type.

        @param fsValue the search value, either transaction number or reference number.

        @param fbByCode a boolean value, true if search is by transaction number, false if search is by reference number.

        @return a boolean value, true if reservation details were successfully loaded, false otherwise.
    */
//    public boolean loadReservation(String fsValue,boolean fbByCode){
//        try {
//            String lsSQL;
//            ResultSet loRS;
//            RowSetFactory factory = RowSetProvider.newFactory();
//            
//            if(fbByCode){
//                lsSQL = MiscUtil.addCondition(getSQ_Reserve(), "sSourceNo = " + SQLUtil.toSQL(fsValue));
//            }else{
//                lsSQL = MiscUtil.addCondition(getSQ_Reserve(), "sTransNox = " + SQLUtil.toSQL(fsValue));
//            }
//            
//            loRS = poGRider.executeQuery(lsSQL);
//            
////            if (MiscUtil.RecordCount(loRS) <= 0){
////                psMessage = "No record found.";
////                MiscUtil.close(loRS);        
////                return false;
////            }
//                        
//            poReserve = factory.createCachedRowSet();
//            poReserve.populate(loRS);
//            MiscUtil.close(loRS);
//        } catch (SQLException e) {
//            psMessage = e.getMessage();
//            return false;
//        }
//        
//        pnEditMode = EditMode.READY;
//        return true;
//    }
    
    
    /**
        Loads the reservation data from the database based on the given search criteria.

        @param fsValues the array of search criteria

        @param fbByCode the flag indicating whether to search by source code or transaction code

        @return {@code true} if the reservation data is successfully loaded; {@code false} otherwise

        @throws SQLException if an error occurs while executing the SQL query
    */
    public boolean loadReservation(String[] fsValues, boolean fbByCode) {
        try {
            String lsSQL = "";
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            String lsCondition = "";
            if (fbByCode){
                for (String fsValue : fsValues) {
                    if (lsSQL.isEmpty()) {
                        //lsSQL = MiscUtil.addCondition(getSQ_Reserve(), "a.sSourceNo = " + SQLUtil.toSQL(fsValue));
                        lsSQL = getSQ_Reserve() + " WHERE a.sSourceNo = " + SQLUtil.toSQL(fsValue);
                    } else {
                        lsSQL = lsSQL + " OR a.sSourceNo = " + SQLUtil.toSQL(fsValue);
                    }
                }
            }else{
                for (String fsValue : fsValues) {
                    if (lsSQL.isEmpty()) {
                        //lsSQL = MiscUtil.addCondition(getSQ_Reserve(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
                        lsSQL = getSQ_Reserve() + " WHERE a.sTransNox = " + SQLUtil.toSQL(fsValue);
                    } else {
                        lsSQL = lsSQL + " OR a.sTransNox = " + SQLUtil.toSQL(fsValue);
                    }
                }
            }

            //lsSQL = lsCondition;
            loRS = poGRider.executeQuery(lsSQL);

            poReserve = factory.createCachedRowSet();
            poReserve.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        pnEditMode = EditMode.READY;
        return true;
    }
    
    /**
    * Sets the current mode to "Update" for modifying an existing record.
    *
    * @return true to indicate the mode has been set to "Update."
    */
    public boolean UpdateRecord(){
        try {
        // Save the current state of the table as the original state
            if (poReserve != null){
                poOriginalReserve = (CachedRowSet) poReserve.createCopy();
            }
            
            if (poInqReq != null){
                poOriginalReq = (CachedRowSet) poInqReq.createCopy();
            }
        } catch (SQLException e) {
            // Handle exception
        }
        pnEditMode = EditMode.UPDATE;
        return true;        
    }
    
    /**
        Cancels a reservation transaction based on the provided row number.

        @param fnRow the row number of the reservation transaction to cancel

        @return true if the cancellation was successful, false otherwise

        @throws SQLException if a database error occurs
    */
    public boolean CancelReservation(int fnRow) throws SQLException{
//        if (pnEditMode != EditMode.READY){
//            psMessage = "Invalid update mode detected.";
//            return false;
//        }
        
        psMessage = "";
        String lsgetBranchCd = "";
        if (getInqBranchCd((String) getInqRsv(fnRow,"sSourceNo"))){
            if (!psgetBranchCd.equals(psBranchCd)){
                lsgetBranchCd = psgetBranchCd ;
            }
        } else {
            psMessage = "Error while geting inquiry branch code";
            return false;
        }
                        
        if (!((String) getInqRsv(fnRow,"cTranStat")).equals("0")){
            psMessage = "Unable to cancel transactions.";
            return false;
        }
                
        //validation for allowed employee to cancel
        if (((String) getInqRsv(fnRow,"cTranStat")).equals("0")){
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
        
        String lsTransNox = (String) getInqRsv(fnRow,"sTransNox");
        String lsSQL = "UPDATE " + RESERVE_TABLE + " SET" +
                            " cTranStat = '2'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(lsTransNox);
        
        if (poGRider.executeQuery(lsSQL, RESERVE_TABLE, psBranchCd,lsgetBranchCd) <= 0){
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }
        
        //pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    //TODO Saverecord for saving
    public boolean SaveRecord() throws SQLException{
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
                
        try {
            //dont save if no item              
            if (!isEntryOK()) return false;
                        
            String lsSQL = "";
            int lnCtr;  
            String lsgetBranchCd = "";
            if (getInqBranchCd(psTransNox)){
                lsgetBranchCd = psgetBranchCd ;
            } else {
                psMessage = "Error while geting inquiry branch code";
                return false;
            }

            if (pnEditMode == EditMode.ADDNEW){ //add                
                lnCtr = 0;                
                
                if (!pbWithParent) poGRider.beginTrans(); 
                //Reservation saving
                poReserve.beforeFirst();                
                while (poReserve.next()){
                //while (lnCtr <= getReserveCount()){
                    String lsTransNox = MiscUtil.getNextCode(RESERVE_TABLE, "sTransNox", true, poGRider.getConnection(), psBranchCd);
                    String lsReferNox = MiscUtil.getNextCode(RESERVE_TABLE, "sReferNox", false, poGRider.getConnection(), psBranchCd);                    
                    poReserve.updateString("sSourceNo", psTransNox);                    
                    poReserve.updateObject("sTransNox", lsTransNox); 
                    poReserve.updateObject("sReferNox", lsReferNox);
                    poReserve.updateString("sEntryByx", poGRider.getUserID());
                    poReserve.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                    poReserve.updateString("sModified", poGRider.getUserID());
                    poReserve.updateObject("dModified", (Date) poGRider.getServerDate());
                    poReserve.updateRow();

                    lsSQL = MiscUtil.rowset2SQL(poReserve, RESERVE_TABLE, "sCompnyNm»sClientID2»sDescript»sSeNamexx»sApprovby»sBranchNm");
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }
                    if (poGRider.executeQuery(lsSQL, RESERVE_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                        if (!pbWithParent) poGRider.rollbackTrans();
                        psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                        return false;
                    }
                    lnCtr++;
                }
                
//              //Requirements saving
                lnCtr = 0;
                poInqReq.beforeFirst();
                //while (lnCtr <= getInqReqCount()){                 
                while (poInqReq.next()){
                    poInqReq.updateString("sTransNox", psTransNox); 
                    poInqReq.updateObject("nEntryNox", lnCtr);
                    poInqReq.updateRow();
                                                                                
                    lsSQL = MiscUtil.rowset2SQL(poInqReq, REQUIREMENTS_TABLE, "sDescript»cPayModex»cCustGrpx»sCompnyNm");
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }
                    if (poGRider.executeQuery(lsSQL, REQUIREMENTS_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                        if (!pbWithParent) poGRider.rollbackTrans();
                        psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                        return false;
                    }
                    lnCtr++;
                }      
                //Update customer_inquiry status to on process
                lsSQL = "UPDATE customer_inquiry SET" +
                            " cTranStat = '1'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(psTransNox);

                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, lsgetBranchCd) <= 0){
                    psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                }      
                
                if (!pbWithParent) poGRider.commitTrans();               
                          
            } else { //update                                             
                //check if changes has been made                                  
                // Save the changes
                if (!pbWithParent) poGRider.beginTrans(); 
                lnCtr = 1;
                poReserve.beforeFirst();
                while (poReserve.next()){     
                    //check per row if changes has been made
                    if(!CompareRows.isRowEqual(poReserve, poOriginalReserve,lnCtr)) {
                        String lsTransNox = (String) getInqRsv(lnCtr, "sTransNox");// check if user added new address to insert
                        if (lsTransNox.equals("") || lsTransNox.isEmpty()){
                            lsTransNox = MiscUtil.getNextCode(RESERVE_TABLE, "sTransNox", true, poGRider.getConnection(), psBranchCd);
                            String lsReferNox = MiscUtil.getNextCode(RESERVE_TABLE, "sReferNox", true, poGRider.getConnection(), psBranchCd);
                            poReserve.updateString("sSourceNo", psTransNox);                 
                            poReserve.updateObject("sTransNox", lsTransNox);
                            poReserve.updateObject("sReferNox", lsReferNox);
                            poReserve.updateString("sEntryByx", poGRider.getUserID());
                            poReserve.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                            poReserve.updateString("sModified", poGRider.getUserID());
                            poReserve.updateObject("dModified", (Date) poGRider.getServerDate());
                            poReserve.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poReserve, RESERVE_TABLE, "sCompnyNm»sClientID2»sDescript»sSeNamexx»sApprovby»sBranchNm");

                            if (poGRider.executeQuery(lsSQL, RESERVE_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                                if (!pbWithParent) poGRider.rollbackTrans();
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }
                        }else{//if user modified already saved reservation   
                            poReserve.updateString("sModified", poGRider.getUserID());
                            poReserve.updateObject("dModified", (Date) poGRider.getServerDate());
                            poReserve.updateRow();
                            lsSQL = MiscUtil.rowset2SQL(poReserve, 
                                                        RESERVE_TABLE, 
                                                        "sCompnyNm»sClientID2»sDescript»sSeNamexx»sApprovby", 
                                                        "sTransNox = " + SQLUtil.toSQL(lsTransNox));                                                        

                            if (!lsSQL.isEmpty()){
                                if (poGRider.executeQuery(lsSQL, RESERVE_TABLE, psBranchCd, lsgetBranchCd) <= 0){
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
                poOriginalReserve = (CachedRowSet) poReserve.createCopy();
                
                //Updating Requirements                                                
                // Save the changes
                int lnRow = 1;
                lnCtr = 1;
                poInqReq.beforeFirst();
                while (poInqReq.next()){
                    String lsTransNox = (String) getInqReq(lnCtr, "sTransNox");// check if user added new requirements to insert
                    if (!lsTransNox.equals("") && !lsTransNox.isEmpty()){ 
                        //check if requirements was deselected, delete if found any
                        if (poInqReq.getString("cSubmittd").equals("0")){
                            lsSQL = "DELETE FROM customer_inquiry_requirements WHERE" +                                   
                                    " sTransNox = " + SQLUtil.toSQL(psTransNox) +
                                    " AND sRqrmtCde = " + SQLUtil.toSQL(poInqReq.getString("sRqrmtCde")); 
                                    //" AND nEntryNox = " + SQLUtil.toSQL(poInqReq.getInt("nEntryNox"));

                            if (poGRider.executeQuery(lsSQL, "customer_inquiry_requirements", psBranchCd, lsgetBranchCd) <= 0){
                                psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                                return false;
                            }

                        } 
                        if (poInqReq.getString("cSubmittd").equals("1")){
                            poInqReq.updateObject("nEntryNox", lnRow);
                            poInqReq.updateRow();
                            //if user modified already saved requirements  
                            //only update rows that have submitted requirements
                            lsSQL = MiscUtil.rowset2SQL(poInqReq, 
                                                        REQUIREMENTS_TABLE, 
                                                        "sDescript»cPayModex»cCustGrpx»sCompnyNm", 
                                                        "sTransNox = " + SQLUtil.toSQL(lsTransNox) +
                                                        " AND sRqrmtCde = " + SQLUtil.toSQL(poInqReq.getString("sRqrmtCde")));                                                        

                            if (!lsSQL.isEmpty()){
                                if (poGRider.executeQuery(lsSQL, REQUIREMENTS_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                                    if (!pbWithParent) poGRider.rollbackTrans();
                                    psMessage = poGRider.getMessage() + ";" + poGRider.getErrMsg();
                                    return false;
                                }
                            }
                            lnRow++;
                        }
                    } else {
                        /*INSERT STATEMENT*/
                        if (poInqReq.getString("cSubmittd").equals("1")){                                                                       
                            poInqReq.updateObject("sTransNox", psTransNox);
                            poInqReq.updateObject("nEntryNox", lnRow);
                            poInqReq.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poInqReq, REQUIREMENTS_TABLE, "sDescript»cPayModex»cCustGrpx»sCompnyNm");

                            if (poGRider.executeQuery(lsSQL, REQUIREMENTS_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                                if (!pbWithParent) poGRider.rollbackTrans();
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }
                            
                            lnRow++;
                        }
                    
                    }
                    //check any changes was made,insert if new row,update if modified
                    //if(!CompareRows.isRowEqual(poInqReq, poOriginalReq,lnCtr)) {
                        
                    //}
                    lnCtr++;
                    lsTransNox = "";
                }                    
                // Update the original state of the table
                poOriginalReq = (CachedRowSet) poInqReq.createCopy();            
            }

            String lsPaymodex = (String) getInqReq("cPayModex");
            String lsCustGrpx = (String) getInqReq("cCustGrpx");
            lsSQL = "UPDATE customer_inquiry SET" +
                                " cPayModex = " + SQLUtil.toSQL(lsPaymodex) +
                                ", cCustGrpx = " + SQLUtil.toSQL(lsCustGrpx) +
                            " WHERE sTransNox = " + SQLUtil.toSQL(psTransNox);

            if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, lsgetBranchCd) <= 0){
                psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
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
        * Returns the SQL query string for retrieving customer inquiry requirements.
        *
        * The returned query string joins the `customer_inquiry_requirements` table with
        * the `requirement_source` and `customer_inquiry` tables to retrieve the following
        * columns:
        *
        * 1. sTransNox - the transaction number
        * 2. nEntryNox - the entry number
        * 3. sRqrmtCde - the requirement code
        * 4. cSubmittd - the submission status
        * 5. sReceived - the receiving personnel
        * 6. dReceived - the receiving date
        * 7. sDescript - the description of the requirement (from the `requirement_source` table)
        * 8. cPayModex - the payment mode (from the `customer_inquiry` table)
        * 9. cCustGrpx - the customer group (from the `customer_inquiry` table)
        * 10.sCompnyNm - the name where requirements was submitted to
        * @return the SQL query string for retrieving customer inquiry requirements
    */
    private String getSQ_InqRequirements(){
        return "SELECT " + 
                    "a.sTransNox " +  //1
                    ", a.nEntryNox" +  //2
                    ", a.sRqrmtCde" +  //3
                    ", a.cSubmittd" +  //4
                    ", a.sReceived" +  //5
                    ", a.dReceived" +  //6
                    ", IFNULL(b.sDescript, '') sDescript" + //7
                    ", IFNULL(d.cPayModex, '') cPayModex" + //8
                    ", IFNULL(d.cCustGrpx, '') cCustGrpx" + //9
                    ", IFNULL(e.sCompnyNm, '') sCompnyNm" + //10
                " FROM " + REQUIREMENTS_TABLE + " a " +       
                " LEFT JOIN requirement_source b on a.sRqrmtCde = b.sRqrmtCde " +                 
                " LEFT JOIN customer_inquiry d ON d.sTransNox = a.sTransNox" +
                " LEFT JOIN client_master e on a.sReceived = e.sClientID";                                                                                                                                                                                                                                                                                                                                 
    }
    
    //TODO query for retrieving Inquiry requirement source
    private String getSQ_InqReqSource(){
        return "SELECT " +
                    " a.sRqrmtCde " +
                    ", a.sDescript" +
                    ", a.cRecdStat" +
                    ", a.dEntryDte" +
                    ", a.sEntryByx" +
                " FROM requirement_source a " +
                " LEFT JOIN requirement_source_pergroup b on a.sRqrmtCde = b.sRqrmtCde ";
    }    
    
    //TODO query for retrieving reservation
    private String getSQ_Reserve(){
        return " SELECT " +                           
                    " IFNULL(a.sTransNox, '') sTransNox " +  //1                      
                    " ,a.dTransact " + //2                   
                    " ,IFNULL(a.sReferNox, '') sReferNox " + //3                       
                    " ,IFNULL(a.sClientID, '') sClientID " + //4                   
                    " ,IFNULL(a.nAmountxx, 0) nAmountxx " + //5                    
                    " ,IFNULL(a.sRemarksx, '') sRemarksx " + //6                    
                    " ,IFNULL(a.sSourceCD, '') sSourceCD " + //7                   
                    " ,IFNULL(a.sSourceNo, '') sSourceNo " + //8                   
                    " ,IFNULL(a.nPrintedx, 0) nPrintedx " + //9                    
                    " ,IFNULL(a.nRowPosxx, 0) nRowPosxx " + //10                      
                    " ,IFNULL(a.sResrvCde, '') sResrvCde " + //11                      
                    " ,IFNULL(a.cResrvTyp, '') cResrvTyp " + //12                      
                    " ,IFNULL(a.cTranStat, '') cTranStat " + //13                      
                    " ,IFNULL(a.sApproved, '') sApproved " + //14                      
                    " ,a.dApproved " + //15                      
                    " ,a.sEntryByx " + //16                      
                    " ,a.dEntryDte " + //17                      
                    " ,a.sModified " + //18                      
                    " ,a.dModified " + //19
                    " ,IFNULL(c.sCompnyNm, '') sCompnyNm " +//20                    
                    " ,'' sDescript " + //21
                    //" ,'' sSeNamexx " + //22
                    ",IFNULL((SELECT IFNULL(b.sCompnyNm, '') sSeNamexx " +
                    " FROM ggc_isysdbf.employee_master001 " +
                    " LEFT JOIN ggc_isysdbf.client_master b ON b.sClientID = employee_master001.sEmployID " +
                    " LEFT JOIN ggc_isysdbf.department c ON c.sDeptIDxx = employee_master001.sDeptIDxx " +
                    " LEFT JOIN ggc_isysdbf.branch_others d ON d.sBranchCD = employee_master001.sBranchCd  " +
                    " WHERE (c.sDeptIDxx = 'a011' or c.sDeptIDxx = '015') AND ISNULL(employee_master001.dFiredxxx) AND " +
                    " d.cDivision = (SELECT cDivision FROM ggc_isysdbf.branch_others WHERE sBranchCd = b.sBranchCd" +  //SQLUtil.toSQL(psBranchCd) + 
                    ") AND employee_master001.sEmployID =  b.sEmployID), '') AS sSeNamexx" +//22 
                    " ,'' sApprovby " + //23
                    ", IFNULL((SELECT IFNULL(branch.sBranchNm, '') FROM branch WHERE branch.sBranchCd = b.sBranchCd), '') AS sBranchNm " + //24
                " FROM customer_inquiry_reservation a " +
                " LEFT JOIN customer_inquiry b on a.sSourceNo = b.sTransNox " +
                " LEFT JOIN client_master c on c.sClientID = b.sClientID ";
    }

    //-------------------- RESERVATION APPROVAL---------------------------------
    /**
        Loads reservations for approval.

        @return true if the data is successfully loaded, otherwise false

        @throws SQLException if an error occurs while executing the SQL statement
    */
    public boolean loadRsvForApproval() throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();                
        
        //lsSQL = MiscUtil.addCondition(getSQ_Reserve(), " a.cTranStat = '0'");
        lsSQL = getSQ_Reserve() + " WHERE a.cTranStat = '0'";
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        poReserve = factory.createCachedRowSet();
        poReserve.populate(loRS);
        MiscUtil.close(loRS);
                
        return true;
    }
    
    //FOR APPROVAL OF RESERVATION TO DO NEED TO ADD ACCESS VALIDATION
    /**
        Approves a reservation by updating the transaction status and approved details.

        @param fsTransNox the transaction number of the reservation to be approved

        @return {@code true} if the reservation is successfully approved, {@code false} otherwise

        @throws SQLException if a database access error occurs
    */    
    public boolean ApproveReservation(String fsTransNox, int fnRow) throws SQLException{
//        if (pnEditMode != EditMode.READY){
//            psMessage = "Invalid update mode detected.";
//            return false;
//        }
//        
//        psMessage = "";
//                
//        
//        if (!poGRider.getDepartment().equals("")){
//            psMessage = "Only Sales Manager can use this feature.";
//            return false;
//        }                                        
        String lsgetBranchCd = "";
        if (getInqBranchCd((String) getInqRsv(fnRow, "sSourceNo"))){
            if (!psgetBranchCd.equals(psBranchCd)){
                lsgetBranchCd = psgetBranchCd ;
            }
        } else {
            psMessage = "Error while geting inquiry branch code";
            return false;
        }      
        String lsTransNox = fsTransNox;
        String lsSQL = "UPDATE customer_inquiry_reservation SET" +
                            "  cTranStat = '1'" +
                            ", sApproved = " + SQLUtil.toSQL(poGRider.getUserID()) +
                            ", dApproved = " + SQLUtil.toSQL(poGRider.getServerDate()) +
                        " WHERE sTransNox = " + SQLUtil.toSQL(lsTransNox);
        
        if (poGRider.executeQuery(lsSQL, RESERVE_TABLE, psBranchCd, lsgetBranchCd) <= 0){
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }
        
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
        System.out.println(lsSQL);
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
    
    //----------------------------INQUIRY REQUIREMENTS--------------------------
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poInqReq.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poInqReq.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poInqReq.getMetaData().getColumnType(lnCtr));
            if (poInqReq.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poInqReq.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poInqReq.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }
    //----------------------------INQUIRY RESERVATION---------------------------
    public void displayReserveFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poReserve.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poReserve.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poReserve.getMetaData().getColumnType(lnCtr));
            if (poReserve.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poReserve.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poReserve.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: RESERVATION TABLE INFO");
        System.out.println("----------------------------------------");
    }
    
    //TODO validation for entries
    public boolean isEntryOK(){
        return true;
    }
    
    public static int getColumnIndex(CachedRowSet loRS, String fsValue) throws SQLException {
        int lnIndex = 0;
        int lnRow = loRS.getMetaData().getColumnCount();
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++) {
          if (fsValue.equals(loRS.getMetaData().getColumnLabel(lnCtr))) {
            lnIndex = lnCtr;
            break;
          } 
        } 
        return lnIndex;
    }
    
}
