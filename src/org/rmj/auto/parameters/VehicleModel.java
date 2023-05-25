/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.parameters;

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
 * @author Arsiela
 * Date Created: 05-23-2023
 */
public class VehicleModel {
    private final String MASTER_TABLE = "vehicle_model";
    
    private GRider poGRider;
    private String psBranchCd;
    private String psSourceID;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poVehicle;
    
    public VehicleModel(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;                       
    }  
    
    public int getEditMode(){
        return pnEditMode;
    }
    
    public String getSourceID(){
        return psSourceID;
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
    
    public int getItemCount() throws SQLException{
        poVehicle.last();
        return poVehicle.getRow();
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poVehicle.first();
        
        switch (fnIndex){            
            case 2://a.sModelDsc
            case 3://a.sMakeIDxx
            case 4://a.sUnitType
            case 5://a.sBodyType
            case 13: //sMakeDesc
                poVehicle.updateObject(fnIndex, (String) foValue);
                poVehicle.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 6:
                if (foValue instanceof Integer)
                    poVehicle.updateInt(fnIndex, (int) foValue);
                else 
                    poVehicle.updateInt(fnIndex, 0);
                
                poVehicle.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break;                                           
        }
    }
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poVehicle, fsIndex), foValue);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poVehicle, fsIndex));
    }
    
    public Object getMaster(int fnIndex) throws SQLException{
        poVehicle.first();
        return poVehicle.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVehicle.absolute(fnRow);
        return poVehicle.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poVehicle, fsIndex));
    }
    
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poVehicle = factory.createCachedRowSet();
            poVehicle.populate(loRS);
            MiscUtil.close(loRS);
            
            poVehicle.last();
            poVehicle.moveToInsertRow();
            
            MiscUtil.initRowSet(poVehicle);       
            poVehicle.updateString("cRecdStat", RecordStatus.ACTIVE);   
            poVehicle.updateString("cVhclSize", "0");      
            poVehicle.insertRow();
            poVehicle.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    //for autoloading list of vehicle make
    public boolean LoadList() throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL ; //= MiscUtil.addCondition(getSQ_Master(), "a.sMakeIDxx = " + SQLUtil.toSQL(fsValue));
        ResultSet loRS; //= poGRider.executeQuery(lsSQL);
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        loRS = poGRider.executeQuery(getSQ_Master());
        poVehicle = factory.createCachedRowSet();
        poVehicle.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    public boolean OpenRecord(String fsValue){
        if (poVehicle == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        try {
            psSourceID = fsValue;
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sModelIDx = " + SQLUtil.toSQL(psSourceID));
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poVehicle = factory.createCachedRowSet();
            poVehicle.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
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
            String lsTransNox = "";
            if (pnEditMode == EditMode.ADDNEW){ //add
                psSourceID = MiscUtil.getNextCode(MASTER_TABLE, "sModelIDx", true, poGRider.getConnection(), psBranchCd);
                poVehicle.updateString("sModelIDx",psSourceID );                                                             
                poVehicle.updateString("sEntryByx", poGRider.getUserID());
                poVehicle.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, MASTER_TABLE, "sMakeDesc");
            } else { //update  
                //psSourceID =  SQLUtil.toSQL((String) getMaster("a.sModelIDx")) ;
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, 
                                            MASTER_TABLE, 
                                            "sMakeDesc", 
                                            "sModelIDx = " + SQLUtil.toSQL(psSourceID));
            }
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
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
    
    private String getSQ_Master(){
        return "SELECT" +
                    " a.sModelIDx" +    //1
                    ", a.sModelDsc" +   //2
                    ", a.sMakeIDxx" +   //3
                    ", a.sUnitType" +   //4
                    ", a.sBodyType" +   //5
                    ", a.cVhclSize" +   //6
                    ", a.sModelCde" +   //7
                    ", a.cRecdStat" +   //8
                    ", a.sEntryByx" +   //9
                    ", a.dEntryDte" +   //10
                    ", a.sModified" +   //11
                    ", a.dModified" +   //12
                    ", IFNULL(b.sMakeDesc, '') as sMakeDesc" +   //13
                " FROM  vehicle_model a " + 
                " LEFT JOIN vehicle_make b ON a.sMakeIDxx = b.sMakeIDxx ";
    }
    
    private String getSQ_VhclDesc(){
        return "SELECT" +
                    " sVhclIDxx" +   
                    " sDescript" +   
                " FROM vehicle_master ";
    }
    
    private boolean isEntryOK() throws SQLException{
        poVehicle.first();
        
        if (poVehicle.getString("sMakeIDxx").isEmpty()){
            psMessage = "Vehicle Make is not set.";
            return false;
        }
        
        if (poVehicle.getString("sModelDsc").isEmpty()){
            psMessage = "Vehicle Model is not set.";
            return false;
        }
        
        String lsSQL;
        ResultSet loRS;
        /*CHECK EXISTING MODEL*/
        lsSQL = getSQ_Master();
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sModelDsc = " + SQLUtil.toSQL(poVehicle.getString("sModelDsc")) +
                                                " AND a.sModelIDx <> " + SQLUtil.toSQL(poVehicle.getString("sModelIDx")) ); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Vehicle Model Description.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        /*CHECK WHEN VEHICLE MODEL IS ALREADY LINKED TO VEHICLE DESCRIPTION*/
        lsSQL = getSQ_VhclDesc();
        lsSQL = MiscUtil.addCondition(lsSQL, "sModelIDx = " + SQLUtil.toSQL(poVehicle.getString("sModelIDx")) ); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Vehicle Model is already used in Vehicle Description. Please contact system administrator to address this issue.";
            MiscUtil.close(loRS);        
            return false;
        }
                       
        return true;
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poVehicle.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poVehicle.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poVehicle.getMetaData().getColumnType(lnCtr));
            if (poVehicle.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poVehicle.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poVehicle.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    } 
    
}
