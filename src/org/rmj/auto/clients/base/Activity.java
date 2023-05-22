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
    public CachedRowSet poActVehicle;
    public CachedRowSet poTown;
    
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
            case 8 :        //sAddressx 
            case 9 :        //sTownIDxx 
            case 10:        //sLocation 
            case 11:        //sCompnynx           
            case 15:        //sEmployID 
            case 16:        //sDeptIDxx
            case 17:        //sLogRemrk 
            case 18:        //sRemarksx             
            case 20:        //sEntryByx            
            case 22:        //sApproved           
            case 24:        //sModified            
            case 26:        //sDeptName
            case 27:        //sCompnyNm
            case 28:        //sBranchNm   
            case 29:        //sTownxxxx
            case 30:        //sProvIDxx
            case 31:        //sProvName
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 12:        //nPropBdgt 
            case 13:        //nRcvdBdgt 
            case 14:        //nTrgtClnt           
            case 19:        //cTranStat           
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break;              
            case 6 :        //dDateFrom 
            case 7 :        //dDateThru
            case 21:        //dEntryDte 
            case 23:        //dApproved 
            case 25:        //dModified 
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
            //---------------------------------Activity Member------------------
            lsSQL = getSQ_ActMember()+ " WHERE 0=1";
            //String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1"); 
            //System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            
            poActMember = factory.createCachedRowSet();
            poActMember.populate(loRS);
            MiscUtil.close(loRS);
            
            poActMember.last();
            poActMember.moveToInsertRow();
            
            MiscUtil.initRowSet(poActMember);       
            //poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);            
//            poActMember.updateString("cTranStat", "0");
//            poActMember.updateObject("dDateFrom", poGRider.getServerDate());    
//            poActMember.updateObject("dDateThru", poGRider.getServerDate());                       

            poActMember.insertRow();
            poActMember.moveToCurrentRow();         
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
                    " ,a.sAddressx " + //8
                    " ,a.sTownIDxx " + //9
                    " ,a.sLocation " + //10
                    " ,a.sCompnynx " + //11
                    " ,a.nPropBdgt " + //12
                    " ,a.nRcvdBdgt " + //13
                    " ,a.nTrgtClnt " + //14
                    " ,a.sEmployID " + //15
                    " ,a.sDeptIDxx " + //16
                    " ,a.sLogRemrk " + //17
                    " ,a.sRemarksx " + //18 
                    " ,a.cTranStat " + //19
                    " ,a.sEntryByx " + //20
                    " ,a.dEntryDte " + //21
                    " ,a.sApproved " + //22
                    " ,a.dApproved " + //23
                    " ,a.sModified " + //24
                    " ,a.dModified " + //25
                    " ,IFNULL(b.sDeptName , '') sDeptName " + //26
                    " ,IFNULL(d.sCompnyNm , '') sCompnyNm " + //27
                    " ,IFNULL(e.sBranchNm , '') sBranchNm " + //28
                    " ,a.sTownxxxx " + //29
                    " ,a.sProvIDxx " + //30
                    " ,IFNULL(f.sProvName , '') sProvName " +//31
                " FROM ggc_anyautodbf.activity_master a " +
                " LEFT JOIN ggc_isysdbf.department b ON b.sDeptIDxx = a.sDeptIDxx " +
                " LEFT JOIN ggc_isysdbf.Employee_Master001 c ON c.sEmployID = a.sEmployID " +
                " LEFT JOIN ggc_isysdbf.client_master d ON d.sClientID = a.sEmployID " +
                " LEFT JOIN ggc_anyautodbf.branch e ON e.sBranchCd = a.sLocation "  +
                " LEFT JOIN province f ON f.sProvIDxx = a.sProvIDxx " +
                " LEFT JOIN towncity g on g.sTownIDxx = a.sTownIDxx ";       
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
        
        pnEditMode = EditMode.READY;
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
        return  "SELECT " + 
                    " sVhclIDxx " + //1
                    ", sDescript" + //2                   
                " FROM Vehicle_master " ;                    
    }
    
    private String getSQ_ActVehicle(){
        return  "SELECT " + 
                    " a.sTransNox" + //1
                    ", a.nEntryNox" + //2
                    ", a.sSerialID" + //3                   
                 //   ", IFNULL(c.sDescript, '') sDescript" + //6
                " FROM activity_vehicle a" ;
//                    " LEFT JOIN vehicle_serial b on b.sSerialID = a.sSerialID" +
//                    " LEFT JOIN vehicle_master c ON b.sVhclIDxx = c.sVhclIDxx";
    }
    
    public boolean loadActVehicle(){
        return true;
    }
    
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
        
        pnEditMode = EditMode.READY;
        return true;                                                
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
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 2 ://nEntryNox                            
                if (foValue instanceof Integer)
                    poActMember.updateInt(fnIndex, (int) foValue);
                else 
                    poActMember.updateInt(fnIndex, 0);
                
                poActMember.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break;             
        }            
    }   
    
    public void setActMember(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setActMember(fnRow,MiscUtil.getColumnIndex(poActMember, fsIndex), foValue);
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
                " From towncity a " +
                " LEFT JOIN province b on b.sProvIDxx = a.sProvIDxx " +
                " WHERE a.cRecdStat = '1' " ;
    }
    
    //Town GETTER    
    public Object getTown(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poTown.absolute(fnRow);
        return poTown.getObject(fnIndex);
    }
    
    //Town GETTER
    public Object getTown(int fnRow, String fsIndex) throws SQLException{
        return getTown(fnRow, MiscUtil.getColumnIndex(poTown, fsIndex));
    }        
    
    //get rowcount of Activity Member
    public int getTownCount() throws SQLException{
        try {
            if (poTown != null){
                poTown.last();
                return poTown.getRow();
            }else{
                return 0;
            }  
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return 0;
        }
    }   
    
    /**
        * Loads the town data based on the provided value.
        *
        * @param fsValue the value used to filter the town data
        * @return true if the town data was successfully loaded, false otherwise
    */
    public boolean loadTown(String fsValue){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
                        
            lsSQL = MiscUtil.addCondition(getSQ_Town(), "a.sProvIDxx = " + SQLUtil.toSQL(fsValue));                                                                       
             
            System.out.println(lsSQL);             
            loRS = poGRider.executeQuery(lsSQL);            
            
            poTown = factory.createCachedRowSet();
            poTown.populate(loRS);
            MiscUtil.close(loRS);
                        
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
                
        return true;            
    }
    
//    public boolean addTown(String fsTownName) throws SQLException{
//        String fsTown = poMaster.getString("sTownxxxx");
//        if (fsTown.isEmpty()){
//            
//        }
//        
//        return true;
//    }
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
                setMaster("a.sProvIDxx", loRS.getString("sProvIDxx"));
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
                setMaster("a.sProvIDxx", (String) loJSON.get("sProvIDxx"));
                setMaster("sProvName", (String) loJSON.get("sProvName"));                
            }
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
    
}
