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
 * @author Arsiela
 * Date Created: 06-03-2023
 */
public class VehicleEngineFrame {
    private String MASTER_TABLE = "";
    private final String MAKEFRAME_TABLE = "vehicle_make_frame_pattern";
    private final String MODELFRAME_TABLE = "vehicle_model_frame_pattern";
    private final String MODELENGINE_TABLE = "vehicle_model_engine_pattern";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poVehicle;
    
    private int pnCodeType;
    
    public VehicleEngineFrame(GRider foGRider, String fsBranchCd, boolean fbWithParent){
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
    
    public int getItemCount() throws SQLException{
        poVehicle.last();
        return poVehicle.getRow();
    }
    
    public void setCodeType(int fnValue) {
        pnCodeType = fnValue;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poVehicle.first();
        
        switch (fnIndex){ 
            case 2:
            case 6:
            case 7:
            case 8:
            case 9:
                poVehicle.updateObject(fnIndex, (String) foValue);
                poVehicle.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 3:
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
            String lsSQL = "";
            switch (pnCodeType) {
                case 0:
                    lsSQL = MiscUtil.addCondition(getSQ_MakeFrame(), "0=1");
                    break;
                case 1:
                    lsSQL = MiscUtil.addCondition(getSQ_ModelFrame(), "0=1");
                    break;
                case 2:
                    lsSQL = MiscUtil.addCondition(getSQ_ModelEngine(), "0=1");
                    break;
                default:
                    psMessage = "Invalid Code Type.";
                    pnEditMode = EditMode.UNKNOWN;
                    return false;
            }
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poVehicle = factory.createCachedRowSet();
            poVehicle.populate(loRS);
            MiscUtil.close(loRS);
            
            poVehicle.last();
            poVehicle.moveToInsertRow();
            
            MiscUtil.initRowSet(poVehicle); 
            
            poVehicle.insertRow();
            poVehicle.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    /**
     * For searching vehicle engine frame.
     * @return {@code true} if a matching available vehicle engine frame is found, {@code false} otherwise.
    */
    public boolean searchVhclEngineFrame() throws SQLException{
        String lsSQL = getSQ_Master();
        
        ResultSet loRS;
        loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , ""
                                                    , "Make»Model»Engine / Frame Pattern"
                                                    , "sMakeDesc»sModelDsc»sPattern"
                                                    , "sMakeDesc»sModelDsc»sPattern"
                                                    , 0);
        
        if (loJSON == null){
            psMessage = "No record found/selected.";
            return false;
        } else {
            if (OpenRecord((String) loJSON.get("sPattern"),Integer.parseInt((String) loJSON.get("sCodeType"))) ){
                
            }
        }
               
        return true;
    }
    
    public boolean OpenRecord(String fsValue, int fnValue){
        if (poVehicle == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        try {
            pnCodeType = fnValue;
            String lsSQL = "";
            switch (fnValue) {
                case 0:
                    lsSQL = MiscUtil.addCondition(getSQ_MakeFrame(), "sFrmePtrn = " + SQLUtil.toSQL(fsValue));
                    break;
                case 1:
                    lsSQL = MiscUtil.addCondition(getSQ_ModelFrame(), "sFrmePtrn = " + SQLUtil.toSQL(fsValue));
                    break;
                case 2:
                    lsSQL = MiscUtil.addCondition(getSQ_ModelEngine(), "sEngnPtrn = " + SQLUtil.toSQL(fsValue));
                    break;
                default:
                    psMessage = "Invalid Code Type.";
                    pnEditMode = EditMode.UNKNOWN;
                    return false;
            }
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
            if (pnEditMode == EditMode.ADDNEW){ //add
                
                switch(pnCodeType){
                    case 0:
                        poVehicle.updateObject("nEntryNox", MiscUtil.getNextCode(MAKEFRAME_TABLE, "nEntryNox", false, poGRider.getConnection(), ""));
                    break;
                    case 1:
                        poVehicle.updateObject("nEntryNox", MiscUtil.getNextCode(MODELFRAME_TABLE, "nEntryNox", false, poGRider.getConnection(), ""));
                    break;
                    case 2:
                        poVehicle.updateObject("nEntryNox", MiscUtil.getNextCode(MODELENGINE_TABLE, "nEntryNox", false, poGRider.getConnection(), ""));
                    break;
                    default:
                    psMessage = "Invalid Code Type.";
                    return false;
                }
                
                poVehicle.updateString("sEntryByx", poGRider.getUserID());
                poVehicle.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                switch(pnCodeType){
                    case 0:
                        lsSQL = MiscUtil.rowset2SQL(poVehicle, MAKEFRAME_TABLE, "nFrmeLenx»sMakeDesc»sModelIDx»sModelDsc");
                    break;
                    case 1:
                        lsSQL = MiscUtil.rowset2SQL(poVehicle, MODELFRAME_TABLE, "sMakeIDxx»sMakeDesc»sModelDsc");
                    break;
                    case 2:
                        lsSQL = MiscUtil.rowset2SQL(poVehicle, MODELENGINE_TABLE, "sMakeIDxx»sMakeDesc»sModelDsc");
                    break;
                    default:
                    psMessage = "Invalid Code Type.";
                    return false;
                }
                
            } else { //update         
//                poVehicle.updateString("sModified", poGRider.getUserID());
//                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
//                poVehicle.updateRow();
                
                switch(pnCodeType){
                    case 0:
                        MASTER_TABLE = MAKEFRAME_TABLE;
                        lsSQL = MiscUtil.rowset2SQL(poVehicle
                                                    ,MAKEFRAME_TABLE
                                                    ,"nFrmeLenx»sMakeDesc»sModelIDx»sModelDsc"
                                                    , "sFrmePtrn = " + SQLUtil.toSQL((String) getMaster("sFrmePtrn")));
                    break;
                    case 1:
                        MASTER_TABLE = MODELFRAME_TABLE;
                        lsSQL = MiscUtil.rowset2SQL(poVehicle
                                                    ,MODELFRAME_TABLE
                                                    ,"sMakeIDxx»sMakeDesc»sModelDsc"
                                                    , "sFrmePtrn = " + SQLUtil.toSQL((String) getMaster("sFrmePtrn")));
                    break;
                    case 2:
                        MASTER_TABLE = MODELENGINE_TABLE;
                        lsSQL = MiscUtil.rowset2SQL(poVehicle
                                                    ,MODELENGINE_TABLE
                                                    ,"sMakeIDxx»sMakeDesc»sModelDsc"
                                                    , "sEngnPtrn = " + SQLUtil.toSQL((String) getMaster("sEngnPtrn")));
                    break;
                    default:
                    psMessage = "Invalid Code Type.";
                    return false;
                }
            
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
        return  " SELECT " +
                "  a.nEntryNox " + 
                " , IFNULL(a.sFrmePtrn, '') sPatternx " + 
                " , '' AS nLengthxx " + 
                " , a.sEntryByx " + 
                " , a.dEntryDte " + 
                " , IFNULL(a.sMakeIDxx, '') sMakeIDxx " + 
                " , IFNULL(b.sMakeDesc, '') sMakeDesc " + 
                " , '' AS sModelIDx " + 
                " , '' AS sModelDsc " +
                " , 0 AS nCodeType " + 
                " FROM vehicle_make_frame_pattern a " +
                " LEFT JOIN vehicle_make b ON b.sMakeIDxx = a.sMakeIDxx " +
                " UNION " +
                " SELECT " + 
                " a.nEntryNox " + 
                " , IFNULL(a.sFrmePtrn, '') sPattern " + 
                " , nFrmeLenx nLengthxx " +
                " , a.sEntryByx " + 
                " , a.dEntryDte " + 
                " , IFNULL(c.sMakeIDxx, '') sMakeIDxx " + 
                " , IFNULL(c.sMakeDesc, '') sMakeDesc " + 
                " , IFNULL(b.sModelIDx, '') AS sModelIDx " + 
                " , IFNULL(b.sModelDsc, '') AS sModelDsc " + 
                " , 1 AS nCodeType " + 
                " FROM vehicle_model_frame_pattern a " +
                " LEFT JOIN vehicle_model b ON b.sModelIDx = a.sModelIDx " + 
                " LEFT JOIN vehicle_make c ON c.sMakeIDxx = b.sMakeIDxx " + 
                " UNION " +
                " SELECT " + 
                " a.nEntryNox " + 
                " , IFNULL(a.sEngnPtrn, '') sPattern " + 
                " , nEngnLenx nLengthxx " +
                " , a.sEntryByx " + 
                " , a.dEntryDte " + 
                " , IFNULL(c.sMakeIDxx, '') sMakeIDxx " + 
                " , IFNULL(c.sMakeDesc, '') sMakeDesc " + 
                " , IFNULL(b.sModelIDx, '') AS sModelIDx " + 
                " , IFNULL(b.sModelDsc, '') AS sModelDsc " + 
                " , 2 AS nCodeType " + 
                " FROM vehicle_model_engine_pattern a " +
                " LEFT JOIN vehicle_model b ON b.sModelIDx = a.sModelIDx " + 
                " LEFT JOIN vehicle_make c ON c.sMakeIDxx = b.sMakeIDxx " ;
    }
    
    private String getSQ_MakeFrame(){
        return  " SELECT " +
                "  a.nEntryNox " +
                " , IFNULL(a.sFrmePtrn, '') sFrmePtrn " +
                " , '' AS nFrmeLenx " +
                " , a.sEntryByx " +
                " , a.dEntryDte " +
                " , IFNULL(a.sMakeIDxx, '') sMakeIDxx " +
                " , IFNULL(b.sMakeDesc, '') sMakeDesc " +
                " , '' as sModelIDx " +
                " , '' as sModelDsc " +
                " FROM vehicle_make_frame_pattern a" +
                " LEFT JOIN vehicle_make b ON b.sMakeIDxx = a.sMakeIDxx";
    }
    
    private String getSQ_ModelFrame(){
        return  " SELECT " +
                "   a.nEntryNox " +
                " , IFNULL(a.sFrmePtrn, '') sFrmePtrn " +
                " , a.nFrmeLenx " +
                " , a.sEntryByx " +
                " , a.dEntryDte " +
                " , IFNULL(c.sMakeIDxx, '') sMakeIDxx " +
                " , IFNULL(c.sMakeDesc, '') sMakeDesc " +
                " , IFNULL(a.sModelIDx, '') sModelIDx " +
                " , IFNULL(b.sModelDsc, '') sModelDsc " +
                " FROM vehicle_model_frame_pattern a" +
                " LEFT JOIN vehicle_model b ON b.sModelIDx = a.sModelIDx" +
                " LEFT JOIN vehicle_make c ON c.sMakeIDxx = b.sMakeIDxx" ;
    }
    
    private String getSQ_ModelEngine(){
        return  " SELECT " +
                "   a.nEntryNox " +
                " , IFNULL(a.sEngnPtrn, '') sEngnPtrn " +
                " , a.nEngnLenx  " +
                " , a.sEntryByx " +
                " , a.dEntryDte " +
                " , IFNULL(c.sMakeIDxx, '') sMakeIDxx " +
                " , IFNULL(c.sMakeDesc, '') sMakeDesc " +
                " , IFNULL(a.sModelIDx, '') sModelIDx " +
                " , IFNULL(b.sModelDsc, '') sModelDsc " +
                " FROM vehicle_model_engine_pattern a" +
                " LEFT JOIN vehicle_model b ON b.sModelIDx = a.sModelIDx" +
                " LEFT JOIN vehicle_make c ON c.sMakeIDxx = b.sMakeIDxx" ;
    }
    
    private boolean isEntryOK() throws SQLException{
        poVehicle.first();
        String lsSQL = "";
        ResultSet loRS;
        switch(pnCodeType){
            case 0:
                if (poVehicle.getString("sFrmePtrn").isEmpty()){
                    psMessage = "Frame Pattern is not set.";
                    return false;
                }
                lsSQL = getSQ_MakeFrame();
                lsSQL = MiscUtil.addCondition(lsSQL," a.sFrmePtrn = " + SQLUtil.toSQL(poVehicle.getString("sFrmePtrn")) +
                                                        " AND a.sMakeIDxx = " + SQLUtil.toSQL(poVehicle.getString("sMakeIDxx"))); 
                loRS = poGRider.executeQuery(lsSQL);
                if (MiscUtil.RecordCount(loRS) > 0){
                    psMessage = "Frame Pattern already exists";
                    MiscUtil.close(loRS);        
                    return false;
                }
            break;
            case 1:
                if (poVehicle.getString("sFrmePtrn").isEmpty()){
                    psMessage = "Frame Pattern is not set.";
                    return false;
                }
                if (poVehicle.getString("nFrmeLenx").isEmpty()){
                    psMessage = "Frame Length is not set.";
                    return false;
                }
                if (poVehicle.getInt("nFrmeLenx") <= 0){
                    psMessage = "Invalid Frame Length.";
                    return false;
                }
                lsSQL = getSQ_ModelFrame();
                lsSQL = MiscUtil.addCondition(lsSQL," a.sFrmePtrn = " + SQLUtil.toSQL(poVehicle.getString("sFrmePtrn")) +
                                                        "AND a.sModelIDx = " + SQLUtil.toSQL(poVehicle.getString("sModelIDx"))); 
                loRS = poGRider.executeQuery(lsSQL);
                if (MiscUtil.RecordCount(loRS) > 0){
                    psMessage = "Frame Pattern already exists";
                    MiscUtil.close(loRS);        
                    return false;
                }
            break;
            case 2:
                if (poVehicle.getString("sEngnPtrn").isEmpty()){
                    psMessage = "Engine Pattern is not set.";
                    return false;
                }
                if (poVehicle.getString("nEngnLenx").isEmpty()){
                    psMessage = "Engine Length is not set.";
                    return false;
                }
                if (poVehicle.getInt("nEngnLenx") <= 0){
                    psMessage = "Invalid Engine Length.";
                    return false;
                }
                lsSQL = getSQ_ModelEngine();
                lsSQL = MiscUtil.addCondition(lsSQL," a.sEngnPtrn = " + SQLUtil.toSQL(poVehicle.getString("sEngnPtrn")) +
                                                        "AND a.sModelIDx = " + SQLUtil.toSQL(poVehicle.getString("sModelIDx"))); 
                loRS = poGRider.executeQuery(lsSQL);
                if (MiscUtil.RecordCount(loRS) > 0){
                    psMessage = "Engine Pattern already exists";
                    MiscUtil.close(loRS);        
                    return false;
                }
            break;
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