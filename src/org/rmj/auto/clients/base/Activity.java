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
 * @author jahn 05-18-2023
 */
public class Activity {
    //TODO ADD ACCESS ON BUTTONS AND FORM
    private final String MASTER_TABLE = "activity_master";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private final String SALES = "";
    private final String SALES_ADMIN = "";
    private final String MIS = "";
    private final String MAIN_OFFICE = "M001»M0W1";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    public CachedRowSet poMaster;
    public CachedRowSet poDepartment;
    public CachedRowSet poActMember;
    public CachedRowSet poEmployees;
    public CachedRowSet poVehicle;
    public CachedRowSet poActVehicle;
    public CachedRowSet poTown;
    public CachedRowSet poActTown;
    
    
    public Activity(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
        
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
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    //TODO add setMaster for inquiry
    public void setMaster(int fnIndex, Object foValue) throws SQLException{        
        poMaster.first();
        
        
        switch (fnIndex){  
            case 1 :        //sActvtyID  
            case 2 :        //sActTitle 
            case 3 :        //sActDescx 
            case 4 :        //sActTypID 
            case 5 :        //sActSrcex                       
            case 8:        //sLocation 
            case 9:        //sCompnynx           
            case 13:        //sEmployID 
            case 14:        //sDeptIDxx
            case 15:        //sLogRemrk 
            case 16:        //sRemarksx             
            case 18:        //sEntryByx            
            case 20:        //sApproved           
            case 22:        //sModified            
            case 24:        //sDeptName
            case 25:        //sCompnyNm
            case 26:        //sBranchNm               
            case 27:        //sProvIDxx
            case 28:        //sProvName                                                                                                        
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;                 
            case 10:        //nPropBdgt 
            case 11:        //nRcvdBdgt 
            case 12:        //nTrgtClnt           
            case 17:        //cTranStat           
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break;              
            case 6 :        //dDateFrom 
            case 7 :        //dDateThru
            case 19:        //dEntryDte 
            case 21:        //dApproved 
            case 23:        //dModified 
                if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;      
        }            
    }   
    
    //Activity Master setter
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poMaster, fsIndex), foValue);
    }
    
    //Activity Master getter
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    //Activity Master getter
    public Object getMaster(int fnIndex) throws SQLException{
        poMaster.first();
        return poMaster.getObject(fnIndex);
    }
    
    public int getItemCount() throws SQLException{
        poMaster.last();
        return poMaster.getRow();
    }
    
    /**

    Initializes the master data for adding a new entry.

    @return {@code true} if the master data is successfully initialized, {@code false} otherwise
    */
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {       
            String lsSQL = getSQ_Master() + " WHERE 0=1";
            //String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1"); 
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            RowSetFactory factory = RowSetProvider.newFactory();
            //----------------------------------Inquiry Master------------------
            
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);       
            //poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);            
            poMaster.updateString("cTranStat", "0");
            poMaster.updateObject("dDateFrom", poGRider.getServerDate());    
            poMaster.updateObject("dDateThru", poGRider.getServerDate());                       

            poMaster.insertRow();
            poMaster.moveToCurrentRow();     
//            //---------------------------------Activity Member------------------            
//            lsSQL = MiscUtil.addCondition(getSQ_ActMember(), "0=1"); 
//            //System.out.println(lsSQL);
//            loRS = poGRider.executeQuery(lsSQL);
//            
//            poActMember = factory.createCachedRowSet();
//            poActMember.populate(loRS);
//            MiscUtil.close(loRS);
//            
//            poActMember.last();
//            poActMember.moveToInsertRow();
//            
//            MiscUtil.initRowSet(poActMember);       
//            //poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);            
////            poActMember.updateString("cTranStat", "0");
////            poActMember.updateObject("dDateFrom", poGRider.getServerDate());    
////            poActMember.updateObject("dDateThru", poGRider.getServerDate());                       
//
//            poActMember.insertRow();
//            poActMember.moveToCurrentRow();    
//            //---------------------------------Activity Town------------------            
//            lsSQL = MiscUtil.addCondition(getSQ_ActivityTown(), "0=1"); 
//            //System.out.println(lsSQL);
//            loRS = poGRider.executeQuery(lsSQL);
//            
//            poTown = factory.createCachedRowSet();
//            poTown.populate(loRS);
//            MiscUtil.close(loRS);
//            
//            poTown.last();
//            poTown.moveToInsertRow();
//            
//            MiscUtil.initRowSet(poTown);       
//
//            poTown.insertRow();
//            poTown.moveToCurrentRow();    
//            
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;        
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
            int lnCtr;
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                if (!pbWithParent) poGRider.beginTrans();
                //-------------------SAVE ACTIIVTY MASTER------------------------
                String lsTransNox =  MiscUtil.getNextCode(MASTER_TABLE, "sActvtyID", true, poGRider.getConnection(), psBranchCd); 
                poMaster.updateObject("sActvtyID", lsTransNox);
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sDeptName»sCompnyNm»sProvName»sBranchNm");
                
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                }
                
                //-------------------SAVE ACTIVITY MEMBER----------------------
                
                if (getActMemberCount()> 0){
                    lnCtr = 1;
                    poActMember.beforeFirst();
                    while (poActMember.next()){
                    //while (lnCtr < getActMemberCount()){
                        poActMember.updateObject("sTransNox", lsTransNox);
                        poActMember.updateObject("nEntryNox", lnCtr);
                        poActMember.updateObject("sEntryByx", poGRider.getUserID());
                        poActMember.updateObject("dEntryDte", (Date) poGRider.getServerDate());                    
                        poActMember.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poActMember, "activity_member","sCompnyNm»sDeptName");
                        //TODO what is substring(0,4)
                        if (poGRider.executeQuery(lsSQL, "activity_member", psBranchCd, lsTransNox.substring(0, 4)) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }

                        lnCtr++;
                    }
                }
                //-------------------SAVE ACTIVITY Vehicle----------------------
                if (getActVehicleCount()> 0){
                    lnCtr = 1;
                    System.out.println(getActVehicleCount());
                    poActVehicle.beforeFirst();
                    while (poActVehicle.next()){                    
                        poActVehicle.updateObject("sTransNox", lsTransNox);
                        poActVehicle.updateObject("nEntryNox", lnCtr);                                            
                        poActVehicle.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poActVehicle, "activity_vehicle","sDescript»sCSNoxxxx");
                        //TODO what is substring(0,4)
                        if (poGRider.executeQuery(lsSQL, "activity_vehicle", psBranchCd,"") <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }

                        lnCtr++;
                    }
                }
                //-------------------SAVE ACTIVITY Town----------------------
                
                if (getTownCount()> 0){
                    lnCtr = 1;
                    System.out.println(getTownCount());
                    poActTown.beforeFirst();
                    while (poActTown.next()){
                        poActTown.updateObject("sTransNox", lsTransNox);                                           
                        poActTown.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poActTown, "activity_town","sTownName");
                        //TODO what is substring(0,4)
                        if (poGRider.executeQuery(lsSQL, "activity_town", psBranchCd, "") <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }

                        lnCtr++;
                    }
                }
                                
            } else { //update 
                if (!pbWithParent) poGRider.beginTrans();
                
                //set transaction number on records
                String lsTransNox = (String) getMaster("sActvtyID");
                
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sDeptName»sCompnyNm»sProvName»sBranchNm",                                            
                                            "sActvtyID = " + SQLUtil.toSQL(lsTransNox));
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                }                                                            
                //----------------------Activity member update-------------------
                if (getActMemberCount()> 0){
                    lnCtr = 1;
                    poActMember.beforeFirst();
                    while (poActMember.next()){
                        String lsfTransNox = (String) getActMember(lnCtr, "sTransNox");// check if user added new act member to insert
                        if (lsfTransNox.equals("") || lsfTransNox.isEmpty()){
                            poActMember.updateObject("sTransNox", lsTransNox);
                            poActMember.updateObject("sEntryByx", poGRider.getUserID());
                            poActMember.updateObject("dEntryDte", (Date) poGRider.getServerDate());                    
                            poActMember.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poActMember, "activity_member","sCompnyNm»sDeptName");
                            //TODO what is substring(0,4)
                            if (poGRider.executeQuery(lsSQL, "activity_member", psBranchCd, lsTransNox.substring(0, 4)) <= 0){
                                if (!pbWithParent) poGRider.rollbackTrans();
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }
                        }else{
                            lsSQL = MiscUtil.rowset2SQL(poActMember, 
                                                        "activity_member", 
                                                        "sCompnyNm»sDeptName", 
                                                        "sTransNox = " + SQLUtil.toSQL(lsTransNox) + 
                                                            " AND sEmployID = " + SQLUtil.toSQL(poActMember.getString("sEmployID")));

                            if (!lsSQL.isEmpty()){
                                if (poGRider.executeQuery(lsSQL, "activity_member", psBranchCd, lsTransNox.substring(0, 4)) <= 0){
                                    if (!pbWithParent) poGRider.rollbackTrans();
                                    psMessage = poGRider.getMessage() + ";" + poGRider.getErrMsg();
                                    return false;
                                }
                            }
                        }

                        lnCtr++;
                    }
                }
                //----------------------Activity Vehicle update-_------------------
                if (getActVehicleCount()> 0){
                    lnCtr = 1;
                    poActVehicle.beforeFirst();
                    while (poActVehicle.next()){
                        String lsfTransNox = (String) getActVehicle(lnCtr, "sTransNox");// check if user added new VEHICLE to insert
                        if (lsfTransNox.equals("") || lsfTransNox.isEmpty()){
                            poActVehicle.updateObject("sTransNox", lsTransNox);                                           
                            poActVehicle.updateRow();
                                                        
                            lsSQL = MiscUtil.rowset2SQL(poActVehicle, "activity_vehicle","sDescript»sCSNoxxxx");
                            //TODO what is substring(0,4)
                            if (poGRider.executeQuery(lsSQL, "activity_vehicle", psBranchCd, lsTransNox.substring(0, 4)) <= 0){
                                if (!pbWithParent) poGRider.rollbackTrans();
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }
                        }
                        lnCtr++;
                    }
                }
                //----------------------Activity Town update-_------------------
                if (getTownCount()> 0){
                    lnCtr = 1;
                    poActTown.beforeFirst();
                    while (poActTown.next()){
                        String lsfTransNox = (String) getActMember(lnCtr, "sTransNox");// check if user added new VEHICLE PRIORITY to insert
                        if (lsfTransNox.equals("") || lsfTransNox.isEmpty()){
                            poActTown.updateObject("sTransNox", lsTransNox);                                           
                            poActTown.updateRow();

                            
                            
                            lsSQL = MiscUtil.rowset2SQL(poActTown, "activity_town","sTownName");
                            //TODO what is substring(0,4)
                            if (poGRider.executeQuery(lsSQL, "activity_town", psBranchCd, lsTransNox.substring(0, 4)) <= 0){
                                if (!pbWithParent) poGRider.rollbackTrans();
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }
                        }
                        lnCtr++;
                    }
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
    
    /**

    Searches for a record based on the specified value and search criteria.

    @param fsValue the value used for searching

    @param fbByCode determines if the search is performed by activity code or activity title

    @return {@code true} if a matching record is found and successfully opened, {@code false} otherwise

    @throws SQLException if an SQL exception occurs during the search
    */
    public boolean SearchRecord(String fsValue,boolean fbByCode) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";    
        
        String lsSQL = getSQ_Master();
        
        if (pbWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                poGRider, 
                                lsSQL, 
                                fsValue, 
                                "Activity ID»Activity Title»", 
                                "sActvtyID»sActTitle", 
                                "a.sActvtyID»a.sActTitle", 
                                fbByCode ? 0 : 1 );
            
            if (loJSON != null) 
                return OpenRecord((String) loJSON.get("sActvtyID"));
            else {
                psMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sActvtyID = " + SQLUtil.toSQL(fsValue));   
        else {
            if (!fsValue.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sActTitle LIKE " + SQLUtil.toSQL("%" + fsValue + "%"));                 
            }
        }
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sActvtyID");
        MiscUtil.close(loRS);
        
        
        return OpenRecord(lsSQL);
    }
    
    /**

    Opens a record with the specified value.

    @param fsValue the value used to open the record

    @return {@code true} if the record is successfully opened, {@code false} otherwise
    */
    public boolean OpenRecord(String fsValue){
        pnEditMode = EditMode.UNKNOWN;
        
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            //open master            
            //lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sActvtyID = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            System.out.println(lsSQL);
            //open Act Member
            lsSQL = MiscUtil.addCondition(getSQ_ActMember(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poActMember = factory.createCachedRowSet();
            poActMember.populate(loRS);
            MiscUtil.close(loRS);
            System.out.println(lsSQL);
            //open Act Town
            lsSQL = MiscUtil.addCondition(getSQ_ActivityTown(), "sTransNox = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poActTown = factory.createCachedRowSet();
            poActTown.populate(loRS);
            MiscUtil.close(loRS);
            System.out.println(lsSQL);
            //open Act Vehicle
            lsSQL = MiscUtil.addCondition(getSQ_ActVehicle(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poActVehicle = factory.createCachedRowSet();
            poActVehicle.populate(loRS);
            MiscUtil.close(loRS);
            System.out.println(lsSQL);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;        
    }
    
    private String getSQ_Master(){
        return  " SELECT " +
                    " a.sActvtyID  " + //1
                    " ,a.sActTitle " + //2
                    " ,a.sActDescx " + //3
                    " ,a.sActTypID " + //4
                    " ,a.sActSrcex " + //5
                    " ,a.dDateFrom " + //6
                    " ,a.dDateThru " + //7                   
                    " ,a.sLocation " + //8
                    " ,a.sCompnynx " + //9
                    " ,a.nPropBdgt " + //10
                    " ,a.nRcvdBdgt " + //11
                    " ,a.nTrgtClnt " + //12
                    " ,a.sEmployID " + //13
                    " ,a.sDeptIDxx " + //14
                    " ,a.sLogRemrk " + //15
                    " ,a.sRemarksx " + //16 
                    " ,a.cTranStat " + //17
                    " ,a.sEntryByx " + //18
                    " ,a.dEntryDte " + //19
                    " ,a.sApproved " + //20
                    " ,a.dApproved " + //21
                    " ,a.sModified " + //22
                    " ,a.dModified " + //23
                    " ,IFNULL(b.sDeptName , '') sDeptName " + //24
                    " ,IFNULL(d.sCompnyNm , '') sCompnyNm " + //25
                    " ,IFNULL(e.sBranchNm , '') sBranchNm " + //26                 
                    " ,a.sProvIDxx " + //27
                    " ,IFNULL(f.sProvName , '') sProvName " +//28              
                " FROM ggc_anyautodbf.activity_master a " +
                " LEFT JOIN ggc_isysdbf.department b ON b.sDeptIDxx = a.sDeptIDxx " +
                " LEFT JOIN ggc_isysdbf.Employee_Master001 c ON c.sEmployID = a.sEmployID " +
                " LEFT JOIN ggc_isysdbf.client_master d ON d.sClientID = a.sEmployID " +
                " LEFT JOIN ggc_anyautodbf.branch e ON e.sBranchCd = a.sLocation "  +
                " LEFT JOIN province f ON f.sProvIDxx = a.sProvIDxx ";                      
    }
    
    private String getSQ_Department(){
        return "SELECT " + 
                    "sDeptIDxx" +
                    " , sDeptName " +
                "FROM ggc_isysdbf.department ";                
    }
    
    /**

    Searches for a department based on the specified value.

    @param fsValue the value used for searching the department

    @return {@code true} if the department is found, {@code false} otherwise

    @throws SQLException if an SQL exception occurs
    */
    public boolean searchDepartment(String fsValue) throws SQLException{
        String lsSQL = MiscUtil.addCondition(getSQ_Department(), " sDeptName LIKE " + SQLUtil.toSQL(fsValue + "%"));            
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sDeptIDxx", loRS.getString("sDeptIDxx"));
                setMaster("sDeptName", loRS.getString("sDeptName"));               
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
                                                        "Department Name", 
                                                        "sDeptName",
                                                        "sDeptName",
                                                        0);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sDeptIDxx", (String) loJSON.get("sDeptIDxx"));
                setMaster("sDeptName", (String) loJSON.get("sDeptName"));                
            }
        }        
        return true;
    }
    
    /**

    Loads the department data.

    @return {@code true} if the department data is successfully loaded, {@code false} otherwise
    */
    public boolean loadDepartment(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
                            
            lsSQL = MiscUtil.addCondition(getSQ_Department(), "cRecdStat = " + SQLUtil.toSQL("1"));                
             
                                    
            loRS = poGRider.executeQuery(lsSQL);            
            
            poDepartment = factory.createCachedRowSet();
            poDepartment.populate(loRS);
            MiscUtil.close(loRS);
                        
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        //pnEditMode = EditMode.READY;
        return true;               
    }
            
    public Object getDepartment(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poDepartment.absolute(fnRow);
        return poDepartment.getObject(fnIndex);
    }
    
    public Object getDepartment(int fnRow, String fsIndex) throws SQLException{
        if (getDeptCount()== 0 || fnRow > getDeptCount()) return null;
        return getDepartment(fnRow, MiscUtil.getColumnIndex(poDepartment, fsIndex));
    }
    
    public int getDeptCount() throws SQLException{
        if (poDepartment != null){
            poDepartment.last();
            return poDepartment.getRow();
        }else{
            return 0;
        }      
    }
    
    //Query for searching vehicle
    private String getSQ_Vehicle(){
        return  " SELECT " +
                    " a.sSerialID " +
                    " ,b.sDescript " +
                    " ,a.sCSNoxxxx " +
                    " FROM vehicle_serial a " +
                    " LEFT JOIN vehicle_master b ON b.sVhclIDxx = a.sVhclIDxx " +
                    " WHERE a.cSoldStat = '1' " ;        
    }
    
    public Object getVehicle(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVehicle.absolute(fnRow);
        return poVehicle.getObject(fnIndex);
    }
    
    public Object getVehicle(int fnRow, String fsIndex) throws SQLException{
        if (getVehicleCount()== 0 || fnRow > getVehicleCount()) return null;
        return getVehicle(fnRow, MiscUtil.getColumnIndex(poVehicle, fsIndex));
    }
    
    public int getVehicleCount() throws SQLException{
        if (poVehicle != null){
            poVehicle.last();
            return poVehicle.getRow();
        }else{
            return 0;
        }      
    }
    private String getSQ_ActVehicle(){
        return  " SELECT " +
                   " IFNULL (a.sTransNox, '') sTransNox " +
                   " ,IFNULL (a.nEntryNox, '') nEntryNox " +
                   " ,IFNULL (a.sSerialID, '') sSerialID " +
                   " ,IFNULL (c.sDescript, '') sDescript " +
                   " ,IFNULL (b.sCSNoxxxx, '') sCSNoxxxx " +
                " FROM activity_vehicle a " +
                " LEFT JOIN vehicle_serial b ON b.sSerialID = a.sSerialID " +
                " LEFT JOIN vehicle_master c on c.sVhclIDxx = b.sVhclIDxx";
    }
    
    
    /**

        Adds a new vehicle to the activity vehicle data.

        @param fsSerialID The serial ID of the vehicle.

        @param fsDescript The description or name of the vehicle.

        @param fsCSNoxxxx The CS number of the vehicle.

        @return {@code true} if the vehicle was added successfully, {@code false} otherwise.

        @throws SQLException if a database access error occurs.
    */
    public boolean addActVehicle(String fsSerialID, String fsDescript, String fsCSNoxxxx) throws SQLException{   
        if (poActVehicle == null){
            String lsSQL = MiscUtil.addCondition(getSQ_ActVehicle(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            RowSetFactory factory = RowSetProvider.newFactory();
            poActVehicle = factory.createCachedRowSet();
            poActVehicle.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        poActVehicle.last();
        poActVehicle.moveToInsertRow();

        MiscUtil.initRowSet(poActVehicle);  
          
        poActVehicle.updateString("sSerialID", fsSerialID);
        poActVehicle.updateString("sDescript", fsDescript);
        //poActVehicle.updateObject("nEntryNox", getActVehicleCount());        
        poActVehicle.updateString("sCSNoxxxx", fsCSNoxxxx);        
        poActVehicle.insertRow();
        poActVehicle.moveToCurrentRow();
                
        return true;               
    }
    
    /**
        * Loads the vehicle data into the application based on the specified parameters.
        *
        
        * * @param fsValue     The value used to filter the data. If {@code fbLoadbyAct} is true,
        *                    it represents the transaction number. Otherwise, it is ignored.
        * @param fbLoadbyAct Determines whether to load the vehicle data based on the transaction number (true)
        *                    or load all vehicle data (false).
        * @return {@code true} if the vehicle data was loaded successfully, {@code false} otherwise.
    */
    public boolean loadActVehicle(String fsValue, boolean fbLoadbyAct){
        
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            if (fbLoadbyAct){                
                lsSQL = MiscUtil.addCondition(getSQ_ActVehicle(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));                     
                loRS = poGRider.executeQuery(lsSQL);            
            
                poActVehicle = factory.createCachedRowSet();
                poActVehicle.populate(loRS);
                MiscUtil.close(loRS);
            }else{
                
                lsSQL = getSQ_Vehicle(); 
                loRS = poGRider.executeQuery(lsSQL);            
            
                poVehicle = factory.createCachedRowSet();
                poVehicle.populate(loRS);
                MiscUtil.close(loRS);
            }                                                                          
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
                
        return true;               
    }
    
    /**
        Removes a vehicle from the activity vehicle data based on the specified row index.

        @param fnRow The index of the row representing the vehicle to be removed.

        @return {@code true} if the vehicle was successfully removed, {@code false} otherwise.
    */
    public boolean removeVehicle(int fnRow){
        try {
            if (getActVehicleCount()== 0) {
                psMessage = "No Activity Vehicle delete.";
                return false;
            }
            poActVehicle.absolute(fnRow);
            poActVehicle.deleteRow();
            return true;
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
    }
    
    //------------------------------Activity Vehicle-----------------------------
    //Activity Vehicle Setter
    public void setActVehicle(int fnRow, int fnIndex, Object foValue) throws SQLException{        
        poActVehicle.absolute(fnRow);         
        switch (fnIndex){  
            case 1 ://sTransNox  
            case 3 ://sSerialID
            case 4 ://sDescript
            case 5 ://sCSNoxxxx                    
                poActVehicle.updateObject(fnIndex, (String) foValue);
                poActVehicle.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getActVehicle(fnIndex));
                break;
            case 2 ://nEntryNox                            
                if (foValue instanceof Integer)
                    poActVehicle.updateInt(fnIndex, (int) foValue);
                else 
                    poActVehicle.updateInt(fnIndex, 0);
                
                poActVehicle.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getActVehicle(fnIndex));  
                break;             
        }            
    }   
    
    public void setActVehicle(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setActVehicle(fnRow,MiscUtil.getColumnIndex(poActVehicle, fsIndex), foValue);
    }
    
    //Activity Vehicle getter
    public Object getActVehicle(String fsIndex) throws SQLException{
        return getActVehicle(MiscUtil.getColumnIndex(poActVehicle, fsIndex));
    }
    //Activity Vehicle getter
    public Object getActVehicle(int fnIndex) throws SQLException{
        poActVehicle.first();
        return poActVehicle.getObject(fnIndex);
    }
        
    //Activity Vehicle GETTER    
    public Object getActVehicle(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poActVehicle.absolute(fnRow);
        return poActVehicle.getObject(fnIndex);
    }
    
    //Activity Vehicle GETTER
    public Object getActVehicle(int fnRow, String fsIndex) throws SQLException{
        return getActVehicle(fnRow, MiscUtil.getColumnIndex(poActVehicle, fsIndex));
    }        
    
    //get rowcount of Activity Vehicle
    public int getActVehicleCount() throws SQLException{
        try {
            if (poActVehicle != null){
                poActVehicle.last();
                return poActVehicle.getRow();
            }else{
                return 0;
            }  
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return 0;
        }
    }
    //--------------------------------------------------------------------------
    private String getSQ_Employee(){
        return " SELECT " +                                                           
                    " c.sCompnyNm  " +                                                    
                    " ,a.sEmployID " +                                                    
                    " ,b.sDeptName " +                                                    
                    " ,a.sDeptIDxx " +                                                    
                " FROM ggc_isysdbf.employee_master001 a	" +                            
                " LEFT JOIN ggc_isysdbf.department b ON  b.sDeptIDxx = a.sDeptIDxx " + 
                " LEFT JOIN ggc_isysdbf.client_master c on c.sClientID = a.sEmployID " +
                " WHERE a.cRecdStat = '1' " +
                " AND ISNULL(a.dFiredxxx) ";
    }
    
    private String getSQ_ActMember(){
        return " SELECT " +
                    " a.sTransNox  " +
                    " , a.nEntryNox " +
                    " , a.sEmployID " +
                    " , a.cOriginal " +
                    " , a.sEntryByx " +
                    " , a.dEntryDte " +
                    " , IFNULL(c.sCompnyNm, '') sCompnyNm " +
                    " , IFNULL(d.sDeptName, '') sDeptName " +
                " FROM ggc_anyautodbf.activity_member a " +
                " LEFT JOIN ggc_isysdbf.employee_master001 b ON b.sEmployID = a.sEmployID " +
                " LEFT JOIN ggc_isysdbf.client_master c ON c.sClientID = a.sEmployID " +
                " LEFT JOIN ggc_isysdbf.department d on d.sDeptIDxx = b.sDeptIDxx";
    }
    /**

    Searches for an employee based on the specified value.

    @param fsValue the value used for searching the employee

    @return {@code true} if the employee is found, {@code false} otherwise

    @throws SQLException if an SQL exception occurs
    */
    public boolean searchEmployee(String fsValue) throws SQLException {
        String lsSQL = MiscUtil.addCondition(getSQ_Employee(), " sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%"));            
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                setMaster("sEmployID", loRS.getString("sEmployID"));               
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
                                                        "Employee Name,Department Name", 
                                                        "sCompnyNm»sDeptName",
                                                        "sCompnyNm»sDeptName",
                                                        0);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sEmployID", (String) loJSON.get("sEmployID"));                
            }
        }        
        return true;
    }
    
    /**

    Loads employee data based on the specified value and load mode.

    @param fsValue the value used for loading employee data

    @param fbLoadEmp the load mode indicating whether to load employees or activity members

    @return {@code true} if the employee data is successfully loaded, {@code false} otherwise

    @throws SQLException if an SQL exception occurs
    */
    public boolean loadEmployee(String fsValue, boolean fbLoadEmp) throws SQLException{
        
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            if (fbLoadEmp){
                //For Searching when adding activity member when department is clicked
                lsSQL = MiscUtil.addCondition(getSQ_Employee(), "b.sDeptIDxx = " + SQLUtil.toSQL(fsValue));                     
                loRS = poGRider.executeQuery(lsSQL);            
            
                poEmployees = factory.createCachedRowSet();
                poEmployees.populate(loRS);
                MiscUtil.close(loRS);
            }else{
                
                lsSQL = MiscUtil.addCondition(getSQ_ActMember(), "a.sTransNox = " + SQLUtil.toSQL(fsValue)); 
                loRS = poGRider.executeQuery(lsSQL);            
            
                poActMember = factory.createCachedRowSet();
                poActMember.populate(loRS);
                MiscUtil.close(loRS);
            }  
                                    
            
                        
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        //pnEditMode = EditMode.READY;
        return true;                                                
    }
    
    public Object getEmployee(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poEmployees.absolute(fnRow);
        return poEmployees.getObject(fnIndex);
    }
    
    public Object getEmployee(int fnRow, String fsIndex) throws SQLException{
        if (getEmpCount()== 0 || fnRow > getEmpCount()) return null;
        return getEmployee(fnRow, MiscUtil.getColumnIndex(poEmployees, fsIndex));
    }
    
    public int getEmpCount() throws SQLException{
        if (poEmployees != null){
            poEmployees.last();
            return poEmployees.getRow();
        }else{
            return 0;
        }      
    }
    /**

    Adds a member to the system.

    @param fsEmployID the employee ID of the member

    @param fsEmpName the name of the member

    @param fsDept the department of the member

    @return {@code true} if the member is successfully added, {@code false} otherwise

    @throws SQLException if an SQL exception occurs
    */
    public boolean addMember(String fsEmployID, String fsEmpName, String fsDept) throws SQLException{   
        if (poActMember == null){
            String lsSQL = MiscUtil.addCondition(getSQ_ActMember(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            RowSetFactory factory = RowSetProvider.newFactory();
            poActMember = factory.createCachedRowSet();
            poActMember.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        poActMember.last();
        poActMember.moveToInsertRow();

        MiscUtil.initRowSet(poActMember);  
          
        poActMember.updateString("sEmployID", fsEmployID);
        poActMember.updateString("sCompnyNm", fsEmpName);
//        poActMember.updateObject("nEntryNox", getActMemberCount());        
        poActMember.updateString("sDeptName", fsDept);        
        poActMember.insertRow();
        poActMember.moveToCurrentRow();
                
        return true;               
    }
    
    //------------------------------Activity Member-----------------------------
    //Activity Member Setter
    public void setActMember(int fnRow, int fnIndex, Object foValue) throws SQLException{        
        poActMember.absolute(fnRow);         
        switch (fnIndex){  
            case 1 ://sTransNox  
            case 3 ://sEmployID
            case 7 ://sCompnyNm
            case 8 ://sDeptName
            //case 4 ://cOriginal               
                poActMember.updateObject(fnIndex, (String) foValue);
                poActMember.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getActMember(fnIndex));
                break;
            case 2 ://nEntryNox                            
                if (foValue instanceof Integer)
                    poActMember.updateInt(fnIndex, (int) foValue);
                else 
                    poActMember.updateInt(fnIndex, 0);
                
                poActMember.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getActMember(fnIndex));  
                break;             
        }            
    }   
    
    public void setActMember(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setActMember(fnRow,MiscUtil.getColumnIndex(poActMember, fsIndex), foValue);
    }
    
    //Activity member getter
    public Object getActMember(String fsIndex) throws SQLException{
        return getActMember(MiscUtil.getColumnIndex(poActMember, fsIndex));
    }
    //Activity member getter
    public Object getActMember(int fnIndex) throws SQLException{
        poActMember.first();
        return poActMember.getObject(fnIndex);
    }
        
    //Activity Member GETTER    
    public Object getActMember(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poActMember.absolute(fnRow);
        return poActMember.getObject(fnIndex);
    }
    
    //Activity Member GETTER
    public Object getActMember(int fnRow, String fsIndex) throws SQLException{
        return getActMember(fnRow, MiscUtil.getColumnIndex(poActMember, fsIndex));
    }        
    
    //get rowcount of Activity Member
    public int getActMemberCount() throws SQLException{
        try {
            if (poActMember != null){
                poActMember.last();
                return poActMember.getRow();
            }else{
                return 0;
            }  
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return 0;
        }
    }   
    
    //Remove activity member
    /**

    Removes an activity member at the specified row index.

    @param fnRow The row index of the activity member to be removed.

    @return {@code true} if the activity member was successfully removed, {@code false} otherwise.
    */
    public boolean removeMember(int fnRow){
        try {
            if (getActMemberCount() == 0) {
                psMessage = "No Activity Member delete.";
                return false;
            }
            poActMember.absolute(fnRow);
            poActMember.deleteRow();
            return true;
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
    }
    
    private String getSQ_Town(){
        return  " SELECT " +
                    " a.sTownName " +
                    " ,a.sTownIDxx " +
                " FROM towncity a " +
                " LEFT JOIN province b ON b.sProvIDxx = a.sProvIDxx " +
                " WHERE a.cRecdStat = '1' " ;
    }
    
    private String getSQ_ActivityTown(){
        return  " SELECT " +
                    " a.sTransNox " +
                    " ,a.sTownIDxx " +
                    " ,a.sAddressx " +
                    " ,IFNULL(b.sTownName, '') sTownName " +                    
                " FROM activity_town a " +
                " LEFT JOIN towncity b ON b.sTownIDxx = a.sTownIDxx ";                
    }
    
    //Activity Town Setter
    public void setActTown(int fnRow, int fnIndex, Object foValue) throws SQLException{        
        poActTown.absolute(fnRow);         
        switch (fnIndex){  
            case 1 ://sTransNox  
            case 2 ://sTownIDxx
            case 3 ://sAddressx  
            case 4: //sTownName
                poActTown.updateObject(fnIndex, (String) foValue);
                poActTown.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getActTown(fnIndex));
                break;                      
        }            
    }   
    
    public void setActTown(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setActTown(fnRow,MiscUtil.getColumnIndex(poActTown, fsIndex), foValue);
    }
    
    //Activity Town getter
    public Object getActTown(String fsIndex) throws SQLException{
        return getActTown(MiscUtil.getColumnIndex(poActTown, fsIndex));
    }
    //Activity Town getter
    public Object getActTown(int fnIndex) throws SQLException{
        poActTown.first();
        return poActTown.getObject(fnIndex);
    }
    
    //Town GETTER    
    public Object getActTown(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poActTown.absolute(fnRow);
        return poActTown.getObject(fnIndex);
    }
    
    //Town GETTER
    public Object getActTown(int fnRow, String fsIndex) throws SQLException{
        return getActTown(fnRow, MiscUtil.getColumnIndex(poActTown, fsIndex));
    }        
    
    //get rowcount of Activity Town
    public int getActTownCount() throws SQLException{
        try {
            if (poActTown != null){
                poActTown.last();
                return poActTown.getRow();
            }else{
                return 0;
            }  
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return 0;
        }
    }   
    
    public boolean addActTown(String fsTownID, String fsTownName) throws SQLException{   
        if (poActTown == null){
            String lsSQL = MiscUtil.addCondition(getSQ_ActivityTown(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            RowSetFactory factory = RowSetProvider.newFactory();
            poActTown = factory.createCachedRowSet();
            poActTown.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        poActTown.last();
        poActTown.moveToInsertRow();

        MiscUtil.initRowSet(poActTown);  
          
        poActTown.updateString("sTownIDxx", fsTownID);
        poActTown.updateString("sTownName", fsTownName);    
        poActTown.insertRow();
        poActTown.moveToCurrentRow();
                
        return true;               
    }
    
    /**
        * Loads town data based on the provided value.
        *
        * @param fsValue   The value to use for loading the town data.
        * @param fbByLoad  Determines whether to load the town data by a specific condition or activity town.
        *                  - If true, the town data will be loaded based on the condition "a.sProvIDxx = fsValue".
        *                  - If false, the town data will be loaded based on the activity town condition "a.sTransNox = fsValue".
        * @return True if the town data was successfully loaded, False otherwise.
    */
    public boolean loadTown(String fsValue, boolean fbByLoad){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            if (fbByLoad)  {         
                lsSQL = MiscUtil.addCondition(getSQ_Town(), "a.sProvIDxx = " + SQLUtil.toSQL(fsValue));   
                System.out.println(lsSQL);             
                loRS = poGRider.executeQuery(lsSQL);            

                poTown = factory.createCachedRowSet();
                poTown.populate(loRS);
                MiscUtil.close(loRS);
            }else{
                lsSQL = MiscUtil.addCondition(getSQ_ActivityTown(), "a.sTransNox = " + SQLUtil.toSQL(fsValue)); 
                System.out.println(lsSQL);             
                loRS = poGRider.executeQuery(lsSQL);            

                poActTown = factory.createCachedRowSet();
                poActTown.populate(loRS);
                MiscUtil.close(loRS);
            }
             
//            System.out.println(lsSQL);             
//            loRS = poGRider.executeQuery(lsSQL);            
//            
//            poTown = factory.createCachedRowSet();
//            poTown.populate(loRS);
//            MiscUtil.close(loRS);
                        
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
                
        return true;            
    }
    
    public boolean removeTown(int fnRow){
        try {
            if (getActTownCount()== 0) {
                psMessage = "No Activity Town to delete.";
                return false;
            }
            poActTown.absolute(fnRow);
            poActTown.deleteRow();            
            return true;
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
    }
        
    public Object getTown(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poTown.absolute(fnRow);
        return poTown.getObject(fnIndex);
    }
    
    public Object getTown(int fnRow, String fsIndex) throws SQLException{
        if (getTownCount()== 0 || fnRow > getTownCount()) return null;
        return getTown(fnRow, MiscUtil.getColumnIndex(poTown, fsIndex));
    }
    
    public int getTownCount() throws SQLException{
        if (poTown != null){
            poTown.last();
            return poTown.getRow();
        }else{
            return 0;
        }      
    }
    
    private String getSQ_Province(){
        return  " SELECT " +                    
                    " sProvIDxx " + 
                    " ,sProvName " +
                " From province " +                
                " WHERE cRecdStat = '1' " ;
    }
    
    /**
        * Searches for a province based on the provided value.
        *
        * @param fsValue the value used to search for a province
        * @return true if the province is found and set as the master record, false otherwise
        * @throws SQLException if an error occurs while executing the SQL query
    */
    public boolean searchProvince(String fsValue) throws SQLException{
        String lsSQL = MiscUtil.addCondition(getSQ_Province(), " sProvName LIKE " + SQLUtil.toSQL(fsValue + "%"));            
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sProvIDxx", loRS.getString("sProvIDxx"));
                setMaster("sProvName", loRS.getString("sProvName"));               
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
                                                        "Province", 
                                                        "sProvName",
                                                        "sProvName",
                                                        0);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sProvIDxx", (String) loJSON.get("sProvIDxx"));
                setMaster("sProvName", (String) loJSON.get("sProvName"));                
            }
        }        
        return true;        
    }
    //TODO fix getting of branch
    public String getSQ_Branch(){
        return " SELECT " +
                    " a.sBranchCD " +							
                    " , a.sBranchNm " + 							
                    " , b.cDivision " +						
                " FROM branch a " +
                " LEFT JOIN branch_others b ON a.sBranchCd = b.sBranchCD  " +
                " WHERE a.cRecdStat = '1'  " +
                " AND b.cDivision = (SELECT cDivision FROM branch_others WHERE sBranchCd = " + SQLUtil.toSQL(psBranchCd) + ")" ;
    }
    
    
    public boolean searchBranch() throws SQLException{
        String lsSQL = getSQ_Branch();       
        //poGRider.
        ResultSet loRS;
        
        loRS = poGRider.executeQuery(lsSQL);
        System.out.println(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                    lsSQL, 
                                                    "", 
                                                    "Branch Name, Branch Code", 
                                                    "sBranchNm»a.sBranchCd",
                                                    "sBranchNm»a.sBranchCd",
                                                    0);

        if (loJSON == null){
            psMessage = "No record found/selected.";
            return false;
        } else {
            setMaster("sLocation", (String) loJSON.get("sLocation"));
            setMaster("sBranchNm", (String) loJSON.get("sBranchNm"));                
        }
               
        return true;        
        
    }
    
    public boolean CancelActivity(String fsValue) throws SQLException{
        if (pnEditMode != EditMode.READY){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        psMessage = "";
                        
        if (!((String) getMaster("cTranStat")).equals("0")){
            psMessage = "Unable to cancel transactions.";
            return false;
        }
        //TODO ADD VALIDATION FOR CANCELLING
        //validation for allowed employee to cancel
        if (((String) getMaster("cTranStat")).equals("0")){
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
                
        String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                            " cTranStat = '2'" +
                        " WHERE sActvtyID = " + SQLUtil.toSQL(fsValue);
        
        if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd,"") <= 0){
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }
        
        //pnEditMode = EditMode.UNKNOWN;
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
    
    public void displayMemberFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poActMember.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("ACTIVITY MEMBER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poActMember.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poActMember.getMetaData().getColumnType(lnCtr));
            if (poActMember.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poActMember.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poActMember.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: ACTIVITY MEMBER TABLE INFO");
        System.out.println("----------------------------------------");
    } 
    
    public boolean isEntryOK(){
        return true;
    }
}
