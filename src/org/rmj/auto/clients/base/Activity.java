/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.clients.base;

import java.sql.ResultSet;
import java.sql.SQLException;
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
    
    private CachedRowSet poMaster;
    private CachedRowSet poDepartment;
    private CachedRowSet poActMember;
    
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
            case 1 ://sTransNox
           
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 5 ://cIsVhclNw            
                         
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break;  
            case 3 ://dTransact
           
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
    
    public boolean NewRecord(){
        return true;
    }
    
    public boolean UpdateRecord(){
        return true;
    }
    
    public boolean SaveRecord(){
        return true;
    }
    
    public boolean SearchRecord(){
        return true;
    }
    
    public boolean OpenRecord(){
        return true;
    }
    
    private String getSQ_Master(){
        return "" ;       
    }
    
    private String getSQ_Department(){
        return "SELECT " + 
                    "sDeptIDxx" +
                    " , sDeptName " +
                "FROM ggc_isysdbf.department ";                
    }
           
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
    
    
    private String getSQ_Employee(){
        return " SELECT " +                                                           
                    " c.sCompnyNm  " +                                                    
                    " ,a.sEmployID " +                                                    
                    " ,b.sDeptName " +                                                    
                    " ,a.sDeptIDxx " +                                                    
                " FROM ggc_isysdbf.employee_master001 a	" +                            
                " LEFT JOIN ggc_isysdbf.department b ON  b.sDeptIDxx = a.sDeptIDxx " + 
                " LEFT JOIN ggc_isysdbf.client_master c on c.sClientID = a.sEmployID " 
;
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
            }else{
                
                lsSQL = MiscUtil.addCondition(getSQ_ActMember(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));                
            }  
                                    
            loRS = poGRider.executeQuery(lsSQL);            
            
            poActMember = factory.createCachedRowSet();
            poActMember.populate(loRS);
            MiscUtil.close(loRS);
                        
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;                                                
    }
    
    public boolean addMember(String fsEmployID, String fsEmpName, String fsDept) throws SQLException{
     //   int lnCtr;
    //    int lnRow = getItemCount();
        
        //validate if incentive is already added
//        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){
//            if (fsCode.equals((String) getAddress(lnCtr, "sAddrssID"))) return true;
//        }
        if (poActMember == null){
            String lsSQL = MiscUtil.addCondition(getSQ_ActMember(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
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
        poActMember.updateObject("nEntryNox", getActMemberCount());        
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
                poActMember.updateObject(fnIndex, (String) foValue);
                poActMember.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 2 ://nPriority                            
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
    public boolean removeActMember(int fnRow){
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
    
}
