/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.cashiering.base;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;
import org.rmj.auto.clients.base.CompareRows;
import org.rmj.auto.json.TabsStateManager;

/**
 *
 * @author MIS
 */
public class UnitSalesInvoice {
    //TODO ADD ACCESS ON BUTTONS AND FORM
    private final String MASTER_TABLE = "si_master";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autapp_json/" + TabsStateManager.getJsonFileName("Vehicle Sales Invoice");
   
    private final String CASHIER = "";
    private final String ACCOUNTING = "";
    private final String MIS = "";
    private final String MAIN_OFFICE = "";

    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    public CachedRowSet poMaster;
    private CachedRowSet poMasterOrig;
    public CachedRowSet poSiSource;
    
    public UnitSalesInvoice(GRider foGRider, String fsBranchCd, boolean fbWithParent) {

        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;
    }

    public int getEditMode() {
        return pnEditMode;
    }

    public String getMessage() {
        return psMessage;
    }

    public void setWithUI(boolean fbValue) {
        pbWithUI = fbValue;
    }

    public void setCallback(MasterCallback foValue) {
        poCallback = foValue;
    }
    
    
    private String toJSONString(){
        JSONParser loParser = new JSONParser();
        JSONArray laMaster = new JSONArray();
        JSONArray laAddress = new JSONArray();
        JSONArray laNumber= new JSONArray();
        JSONArray laEmail = new JSONArray();
        JSONArray laSocMedia = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;
        try {
            loJSON = new JSONObject();
            String lsValue =  CommonUtils.RS2JSON(poMaster).toJSONString();
            laMaster = (JSONArray) loParser.parse(lsValue);
            loMaster = (JSONObject) laMaster.get(0);
            loJSON.put("master", loMaster);
            
            //Populate mode with data
            JSONArray modeArray = new JSONArray();
            JSONObject modeJson = new JSONObject();
            modeJson.put("EditMode", String.valueOf(pnEditMode));
            modeJson.put("TransCod", (String) getMaster(1));
            modeArray.add(modeJson);
            loJSON.put("mode", modeArray);
            
            return loJSON.toJSONString();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(UnitSalesInvoice.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }
    
    private void saveState(String fsValue){
        if(pnEditMode == EditMode.UNKNOWN){
            return;
        }
        try {
            
            File Delfile = new File(FILE_PATH);
            if (Delfile.exists() && Delfile.isFile()) {
            } else {
                return;
            }
            
            // Write the JSON object to file
            try (FileWriter file = new FileWriter(FILE_PATH)) {
                file.write(fsValue); 
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("JSON file updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    
    }
    
    public boolean loadState() {
        try {
            String lsTransCd = "";
            String tempValue = "";
            
            File Delfile = new File(FILE_PATH);
            if (Delfile.exists() && Delfile.isFile()) {
            } else {
                psMessage   = "";
                pnEditMode = EditMode.UNKNOWN;
                return false;
            }
            
            // Parse the JSON file
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(FILE_PATH));
            JSONObject jsonObject = (JSONObject) obj;
            
            JSONArray modeArray = (JSONArray) jsonObject.get("mode");
            if(modeArray == null){
                psMessage = "";
                return false;
            }
            
            // Extract index and value from each object in the "master" array
            for (Object item : modeArray) {
                JSONObject mode = (JSONObject) item;
                lsTransCd = (String) mode.get("TransCod");
                pnEditMode = Integer.valueOf((String) mode.get("EditMode"));
            }
            
            if(modeArray.size() > 0){
                switch(pnEditMode){
                    case EditMode.ADDNEW:
                        if(NewRecord()){
                        } else {
                            psMessage = "Error while setting state to New Record.";
                            return false;
                        }
                        break; 
                    case EditMode.UPDATE:
                        if(OpenRecord(lsTransCd)){
                            if(UpdateRecord()){
                            } else {
                                psMessage = "Error while setting state to Update Record.";
                                return false;
                            }
                        } else {
                            psMessage = "Error while setting state to Ready.";
                            return false;
                        }
                        break; 
                    case EditMode.READY:
                        if(OpenRecord(lsTransCd)){
                        } else {
                            psMessage = "Error while setting state to Ready.";
                            return false;
                        }
                        break; 
                }

                if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
                    poMaster.first();
                    JSONObject masterObject = (JSONObject) jsonObject.get("master");
                    // Add a row to the CachedRowSet with the values from the masterObject
                    for (Object key : masterObject.keySet()) {
                        Object value = masterObject.get(key);
                        //System.out.println("MASTER value : " + value + " : key #" + Integer.valueOf(key.toString()) +" : "  + poVehicle.getMetaData().getColumnType(Integer.valueOf(key.toString())));
                        if(value == null){
                            tempValue = "";
                        } else {
                            tempValue = String.valueOf(value);
                        }
                        switch(poMaster.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                            case Types.CHAR:
                            case Types.VARCHAR:
                                poMaster.updateObject(Integer.valueOf(key.toString()), tempValue);
                                //setMaster(Integer.valueOf(key.toString()), tempValue);
                            break;
                            case Types.DATE:
                            case Types.TIMESTAMP:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = DEFAULT_DATE;
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poMaster.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                            
                                //setMaster(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE));
                            break;
                            case Types.INTEGER:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = "0";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poMaster.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue));
                                //setMaster(Integer.valueOf(key.toString()), Integer.valueOf(tempValue));
                            break;
                            case Types.DECIMAL:
                            case Types.DOUBLE:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = "0.00";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poMaster.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue));
                                //setMaster(Integer.valueOf(key.toString()), Double.valueOf(tempValue));
                            break;
                            default:
                                //System.out.println("MASTER value : " + tempValue + " negative key #" + Integer.valueOf(key.toString()) +" : "  + poVehicle.getMetaData().getColumnType(Integer.valueOf(key.toString())));
                                poMaster.updateObject(Integer.valueOf(key.toString()), tempValue);
                                //setMaster(Integer.valueOf(key.toString()), tempValue);
                            break;
                        }
                        tempValue = "";
                    }
                    poMaster.updateRow();
                }
            } else {
                psMessage = "";
                return false;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(UnitSalesInvoice.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poMaster.first();
        
        switch (fnIndex){                           
            case 1: //a.sTransNox
            case 2: //a.sBranchCd                        
            case 5: //a.sReferNox si no
            case 6: //a.sSourceNo udr no
            case 7: //a.sSourceCd udr code
            case 8: //a.sClientID    
            case 14: //a.cPrintedx
            case 16: //a.sEntryByx
            case 17: //a.dEntryDte
            case 18: //sDescript 
            case 19: //sCSNoxxxx                                                                                                                                                                                                                                                                        
            case 20: //sPlateNox 			 																																		                                                                                                                                                                                            
            case 21: //sFrameNox 			 																																		                                                                                                                                                                                            
            case 22: //sEngineNo 			 																																	                                                                                                                                                                                              
            case 23: //sColorDsc 			 																																	                                                                                                                                                                                              
            case 24: //sSalesExe                                                                                                                                                                                                                                                                       
            case 25: //sEmployID              
            case 30: //sCompnyNm buyer name
            case 31: //sAddressx                                                                                                                                                                                                                                                                   
            case 4:  //a.cFormType
            case 32: //b.cCustType
            case 33: //sTaxIDNox
            case 34: //sRemarksx
            case 35: //sCoCltIDx
            case 36: //sCoCltNmx co buyer name
            case 37: //cPayModex
            case 38: //sBankname
            case 39: //cTrStatus
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 3: //a.dTransact 
            
                 if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break; 
            
            case 15: //a.cTranStat           
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
                break;
            case 9: //a.nTranTotl
            case 10: //a.nDiscount
            case 11: //a.nVatRatex
            case 12: //a.nVatAmtxx
            case 13: //a.nAmtPaidx
            case 26: //nAddlDscx 
            case 27: //nPromoDsc 
            case 28: //nFleetDsc 
            case 29: //nUnitPrce 
                if (foValue instanceof Double)
                    poMaster.updateDouble(fnIndex, (Double) foValue);
                else 
                    poMaster.updateDouble(fnIndex, 0.00);                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break;                                                                
        }
        saveState(toJSONString());
    }
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poMaster, fsIndex), foValue);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    
    public Object getMaster(int fnIndex) throws SQLException{
        poMaster.first();
        return poMaster.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poMaster.absolute(fnRow);
        return poMaster.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        
        try {
            String lsSQL = (getSQ_Master()+  " WHERE 0=1");
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);   
            poMaster.updateString("cCustType", "0");    
            poMaster.updateString("cTranStat", RecordStatus.ACTIVE);
            poMaster.updateString("cPrintedx", "0");    
            poMaster.updateObject("dTransact", poGRider.getServerDate());  
            poMaster.insertRow();
            poMaster.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    public boolean UpdateRecord(){
        try {
            if (poMaster != null){
                poMasterOrig = (CachedRowSet) poMaster.createCopy();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UnitSalesInvoice.class.getName()).log(Level.SEVERE, null, ex);
        }
        pnEditMode = EditMode.UPDATE;
        saveState(toJSONString());
        return true; 
    }
    
    /**
    * Searches for a customer record based on certain criteria and opens the selected record.
    *
    * @return true if the operation is successful, false if there's an issue with the database query or if no record is found.
    */
    public boolean SearchRecord(){
        String lsSQL = getSQ_Master();
        
        System.out.println(lsSQL);
        ResultSet loRS;
        loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , ""
                                                    , "SI No»Customer Name»Cancelled"
                                                    , "sReferNox»sCompnyNm»cTrStatus»"
                                                    , "a.sReferNox»sCompnyNm"
                                                    , 0);
        
       
        if (loJSON == null){
            psMessage = "No record found/selected.";
            return false;
        } else {
            if (OpenRecord((String) loJSON.get("sTransNox")) ){
            }else {
                psMessage = "No record found/selected.";
                return false;
            }
        }
               
        return true;
    }
    
    /**
    * Opens a customer record for viewing or editing.
    *
    * @param fsValue The identifier of the customer record to open.
    * @return true if the customer record is successfully opened for viewing or editing.
    *         false if there's an issue with the database operation or if the record doesn't exist.
    */
    public boolean OpenRecord(String fsValue){
        try {
            String lsSQL = getSQ_Master();
            lsSQL = lsSQL + " WHERE a.sTransNox = " + SQLUtil.toSQL(fsValue);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (MiscUtil.RecordCount(loRS) <= 0){
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        try {
            if(getMaster("cTranStat").equals("0")){
                pnEditMode = EditMode.UNKNOWN;
            }else{
                pnEditMode = EditMode.READY;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UnitSalesInvoice.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        saveState(toJSONString());
        return true;
    }
     /**
    * Saves a customer record, either by adding a new record or updating an existing one.
    *
    * @return true if the operation is successful, false if there's an issue with the database operation or data validation.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            
            String lsSQL = "";
            if (pnEditMode == EditMode.ADDNEW){
                String lsTransNox = MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", true, poGRider.getConnection(), psBranchCd);
                poMaster.updateString("sTransNox",lsTransNox); 
            }
            
            if (!isEntryOK()) return false;
            if (pnEditMode == EditMode.ADDNEW){ //add 
                poMaster.updateString("cFormType","0");   //Vehicle Sales Invoice
                poMaster.updateString("sBranchCd",psBranchCd);                  
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());                
//                poMaster.updateString("sModified", poGRider.getUserID());
//                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                //TODO: need to remove »sRemarksx from exclude no column in table yet
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sColorDsc»sSalesExe»sEmployID»nAddlDscx»nPromoDsc»nFleetDsc»nUnitPrce»sCompnyNm»sAddressx»cCustType»sTaxIDNox»sRemarksx»sCoCltIDx»sCoCltNmx»cPayModex»sBankname»cTrStatus");
                
            } else { //update  
                boolean lbisModified = false;
                if (!CompareRows.isRowEqual(poMaster, poMasterOrig,1)) {
                    lbisModified = true;
                }
                if(lbisModified){
//                    poMaster.updateString("sModified", poGRider.getUserID());
//                    poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
//                    poMaster.updateRow();
                    //TODO: need to remove »sRemarksx from exclude no column in table yet
                    lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                                MASTER_TABLE, 
                                                "sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sColorDsc»sSalesExe»sEmployID»nAddlDscx»nPromoDsc»nFleetDsc»nUnitPrce»sCompnyNm»sAddressx»cCustType»sTaxIDNox»sRemarksx»sCoCltIDx»sCoCltNmx»cPayModex»sBankname»cTrStatus", 
                                                "sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
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
    
    private String getSQ_Print(){
        return " SELECT " +
                "  sDescript," +
                "  sValuexxx," +
                "  sRemarksx " +
                " FROM xxxstandard_sets ";
    }
    
    /**
     * GET JASPER REPORT
     * @param fsValue Form name
     * @return 
     */
    public String getReport(String fsValue){
        String sReport = "";
        try {
            System.out.println("poGRider.getBranchCode() " + poGRider.getBranchCode());
            System.out.println("poGRider.getBranchName() " + poGRider.getBranchName());
            
            String lsSQL = MiscUtil.addCondition(getSQ_Print(), " sDescript = " + SQLUtil.toSQL(poGRider.getBranchCode())
                                                                    + " AND sRemarksx = " + SQLUtil.toSQL(fsValue));
            System.out.println(lsSQL);
            ResultSet loRS;
            loRS = poGRider.executeQuery(lsSQL);
            if (loRS.next()){
                sReport = loRS.getString("sValuexxx");
            } else {
                sReport = "";
                psMessage = "Notify System Admin to verify jasper report for " + fsValue;
                return "";
            }
        } catch (SQLException ex) {
            Logger.getLogger(UnitSalesInvoice.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sReport;
    }
        
    private String getSQ_Master(){
        return " SELECT " +                                                                                                                                                                                                                                                                                                       
                    " IFNULL(a.sTransNox,'') as sTransNox " + //1                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(a.sBranchCd,'') as sBranchCd " +//2
                    " ,a.dTransact  " +//3                                                                                                                                                                                                                                                                                               
                    " ,IFNULL(a.cFormType,'') as cFormType " +//4                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(a.sReferNox,'') as sReferNox  " +//5                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(a.sSourceNo,'') as sSourceNo " +//6                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(a.sSourceCd,'') as sSourceCd " +//7                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(a.sClientID,'') as sClientID " +//8                                                                                                                                                                                                                                                                                                  
                    " ,a.nTranTotl " +//9                                                                                                                                                                                                                                                                                                  
                    " ,a.nDiscount " +//10                                                                                                                                                                                                                                                                                                  
                    " ,a.nVatRatex " +//11                                                                                                                                                                                                                                                                                                  
                    " ,a.nVatAmtxx " +//12                                                                                                                                                                                                                                                                                                  
                    " ,a.nAmtPaidx " +//13                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(a.cPrintedx,'') as cPrintedx " +//14                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(a.cTranStat,'') as cTranStat " +//15                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(a.sEntryByx,'') as sEntryByx " +//16                                                                                                                                                                                                                                                                                                  
                    " ,a.dEntryDte " +//17                                                                                                                                                                                                                                                                                                  
                    " , IFNULL(f.sDescript,'') as sDescript " +//18																																						                                                                                                                                                                                            
                    " , IFNULL(d.sCSNoxxxx,'') as sCSNoxxxx " +//19																																						                                                                                                                                                                                            
                    " , IFNULL(e.sPlateNox,'') as sPlateNox " +//20																																				                                                                                                                                                                                              
                    " , IFNULL(d.sFrameNox,'') as sFrameNox " +//21																																					                                                                                                                                                                                              
                    " , IFNULL(d.sEngineNo,'') as sEngineNo " +//22                                                                                                                                                                                                                                                                        
                    " , IFNULL(g.sColorDsc,'') AS sColorDsc " +//23                                                                                                                                                                                                                                                                        
//                    " ,IFNULL((SELECT IFNULL(cm.sCompnyNm, '') sCompnyNm " +                                                                                                                                                                                                                                                          
//                    "  FROM ggc_isysdbf.employee_master001 " +                                                                                                                                                                                                                                                                         
//                    "  LEFT JOIN ggc_isysdbf.client_master cm ON cm.sClientID = employee_master001.sEmployID " +                                                                                                                                                                                                                       
//                    "  LEFT JOIN ggc_isysdbf.department dep ON dep.sDeptIDxx = employee_master001.sDeptIDxx " +                                                                                                                                                                                                                         
//                    "  LEFT JOIN ggc_isysdbf.branch_others bo ON bo.sBranchCD = employee_master001.sBranchCd " +                                                                                                                                                                                                                        
//                    "  WHERE (dep.sDeptIDxx = 'a011' or dep.sDeptIDxx = '015') AND ISNULL(employee_master001.dFiredxxx) AND  " +                                                                                                                                                                                                        
//                    "  bo.cDivision = (SELECT cDivision FROM ggc_isysdbf.branch_others WHERE sBranchCd = a.sBranchCD " +                                                                                                                                                                                                                
//                    " ) AND employee_master001.sEmployID =  h.sEmployID), '') AS sSalesExe " + //24    
                    " ,IFNULL(j.sCompnyNm, '') as sSalesExe " + //24
                    " ,IFNULL(h.sEmployID, '') as sEmployID " +//25                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(c.nAddlDscx,0.00) as nAddlDscx " + //26  cash dsc                                                                                                                                                                                                                                                                     
                    " ,IFNULL(c.nPromoDsc,0.00) as nPromoDsc " + //27                                                                                                                                                                                                                                                                       
                    " ,IFNULL(c.nFleetDsc,0.00) as nFleetDsc " + //28                                                                                                                                                                                                                                                                       
                    " ,IFNULL(c.nUnitPrce,0.00) as nUnitPrce " + //29                                                                                                                                                                                                                                                                       
                    " ,IFNULL(i.sCompnyNm,'') AS sCompnyNm " +   //30                                                                                                                                                                                                                                                                      
//                    " ,IFNULL((SELECT CONCAT( IFNULL( CONCAT(client_address.sAddressx,', ') , ''), IFNULL(CONCAT(barangay.sBrgyName,', '), ''), IFNULL(CONCAT(TownCity.sTownName, ', '),''), IFNULL(CONCAT(Province.sProvName, ', ' ),'')) FROM client_address " +																																			
//                    "  LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx " +                                                                                                                                                                                                                                         
//                    "  LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx " +                                                                                                                                                                                                                                               
//                    "  LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx " +                                                                                                                                                                                                                                               
//                    "  WHERE client_address.sClientID = h.sClientID AND client_address.cPrimaryx = 1 AND client_address.cRecdStat = 1 " +                                                                                                                                                                                              
//                    "  LIMIT 1), '') as sAddressx " + //31  
                    " , IFNULL(CONCAT( IFNULL(CONCAT(k.sAddressx,', ') , ''), " +
                    "       IFNULL(CONCAT(m.sBrgyName,', '), '')," + 
                    "       IFNULL(CONCAT(l.sTownName, ', '),'')," + 
                    "       IFNULL(CONCAT(n.sProvName),'') )	, '') AS sAddressx" + //31
                    " , IFNULL(b.cCustType,'') as cCustType " + //32
                    " , IFNULL(i.sTaxIDNox,'') as sTaxIDNox " + //33
                    " ,'' as sRemarksx " + //34
                    ", IFNULL(c.sCoCltIDx,'') as sCoCltIDx " +//35
                    ", IFNULL(o.sCompnyNm,'') as sCoCltNmx " +//36
                    ", IFNULL(c.cPayModex,'') as cPayModex " +//37
                    ", IFNULL(p.sBankname,'') as sBankname " +//38
                    " , CASE WHEN a.cTranStat = '0' THEN 'Y' ELSE 'N' END AS cTrStatus " + //39 trans status
                " FROM si_master a " +                                                                                                                                                                                                                                                                                             
                " LEFT JOIN udr_master b ON b.sTransNox = a.sSourceCd " +                                                                                                                                                                                                                                                          
                " LEFT JOIN vsp_master c on c.sTransNox = b.sSourceCd " +                                                                                                                                                                                                                                                          
                " LEFT JOIN vehicle_serial d ON d.sSerialID = b.sSerialID  " +                                                                                                                                                                                                                                                     
                " LEFT JOIN vehicle_serial_registration e ON e.sSerialID = b.sSerialID  " +                                                                                                                                                                                                                                        
                " LEFT JOIN vehicle_master f on f.sVhclIDxx = d.sVhclIDxx " +                                                                                                                                                                                                                                                       
                " LEFT JOIN vehicle_color g ON G.sColorIDx = f.sColorIDx  " +                                                                                                                                                                                                                                                       
                " LEFT JOIN customer_inquiry h on h.sTransNox = c.sInqryIDx  " +                                                                                                                                                                                                                                                   
                " LEFT JOIN client_master i on i.sClientID = a.sClientID " +
                " LEFT JOIN ggc_isysdbf.client_master j ON j.sClientID = h.sEmployID " +
                " LEFT JOIN client_address k ON k.sClientID = i.sClientID AND k.cPrimaryx = '1' " +
                " LEFT JOIN TownCity l ON l.sTownIDxx = k.sTownIDxx " +  
                " LEFT JOIN barangay m ON m.sBrgyIDxx = k.sBrgyIDxx AND m.sTownIDxx = k.sTownIDxx " + 
                " LEFT JOIN Province n ON n.sProvIDxx = l.sProvIDxx " +                                                                                                                                                                                                                                                   
                " LEFT JOIN client_master o on o.sClientID = c.sCoCltIDx " +
                " LEFT JOIN vsp_finance p on p.sTransNox = c.sTransNox "; 
    }
    
    private String getSQ_SiSource(){
        return "";
    }
    
    private String getSQ_SearchUdr(){
        return " SELECT " + 
                    " a.sTransNox " + 
                    " ,a.dTransact " + 
                    " ,a.sReferNox " + 
                    " ,a.sClientID " + 
                    " ,a.sSerialID " + 
                    " ,a.sSourceCD " + 
                    " ,a.sSourceNo " + 
                    " ,a.cCallStat " + 
                    " ,a.cTranStat " + 
                    " , IFNULL(e.sDescript,'') as sDescript " +																																						
                    " , IFNULL(c.sCSNoxxxx,'') as sCSNoxxxx " +																																						
                    " , IFNULL(d.sPlateNox,'') as sPlateNox " +																																					
                    " , IFNULL(c.sFrameNox,'') as sFrameNox " +																																					
                    " , IFNULL(c.sEngineNo,'') as sEngineNo " +
//                    " ,IFNULL((SELECT IFNULL(cm.sCompnyNm, '') sCompnyNm " +
//                    "  FROM ggc_isysdbf.employee_master001  " +                                    
//                    "  LEFT JOIN ggc_isysdbf.client_master cm ON cm.sClientID = employee_master001.sEmployID " +                    
//                    "  LEFT JOIN ggc_isysdbf.department dep ON dep.sDeptIDxx = employee_master001.sDeptIDxx " +                        
//                    "  LEFT JOIN ggc_isysdbf.branch_others bo ON bo.sBranchCD = employee_master001.sBranchCd " +                    
//                    "  WHERE (dep.sDeptIDxx = 'a011' or dep.sDeptIDxx = '015') AND ISNULL(employee_master001.dFiredxxx) AND " +       
//                    "  bo.cDivision = (SELECT cDivision FROM ggc_isysdbf.branch_others WHERE sBranchCd = b.sBranchCD " +
//                    " ) AND employee_master001.sEmployID =  g.sEmployID), '') AS sSalesExe " +  
                    " ,IFNULL(i.sCompnyNm, '') as sSalesExe " +
                    " ,g.sEmployID " +
                    " ,IFNULL(b.nAddlDscx,0.00) as nAddlDscx " +
                    " ,IFNULL(b.nPromoDsc,0.00) as nPromoDsc " +
                    " ,IFNULL(b.nFleetDsc,0.00) as nFleetDsc " +
                    " ,IFNULL(b.nUnitPrce,0.00) as nUnitPrce " +
                    " ,IFNULL(h.sCompnyNm,'') AS sCompnyNm " +
//                    " ,IFNULL((SELECT CONCAT( IFNULL( CONCAT(client_address.sAddressx,', ') , ''), IFNULL(CONCAT(barangay.sBrgyName,', '), ''), IFNULL(CONCAT(TownCity.sTownName, ', '),''), IFNULL(CONCAT(Province.sProvName, ', ' ),'')) FROM client_address " +																																			
//                    "  LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx " +
//                    "  LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx " +
//                    "  LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx " +
//                    "  WHERE client_address.sClientID = h.sClientID AND client_address.cPrimaryx = 1 AND client_address.cRecdStat = 1  " +                            
//                    "  LIMIT 1), '') as sAddressx " +
                    "  , IFNULL(CONCAT( IFNULL(CONCAT(j.sAddressx,', ') , ''), " +
                    " IFNULL(CONCAT(l.sBrgyName,', '), ''), " +
                    " IFNULL(CONCAT(k.sTownName, ', '),''), " +
                    " IFNULL(CONCAT(m.sProvName),'') )	, '') AS sAddressx " +
                    " , IFNULL(f.sColorDsc ,'') as sColorDsc" +
                    " ,a.cCustType " +
                    " ,b.sBranchCD " +
                    " ,h.sTaxIDNox " +
                    ", IFNULL(b.sCoCltIDx,'') as sCoCltIDx " +
                    ", IFNULL(n.sCompnyNm,'') as sCoCltNmx " +
                " FROM udr_master a " +
                " LEFT JOIN vsp_master b on b.sTransNox = a.sSourceCd " +
                " LEFT JOIN vehicle_serial c ON c.sSerialID = a.sSerialID " +
                " LEFT JOIN vehicle_serial_registration d ON d.sSerialID = a.sSerialID " +
                " LEFT JOIN vehicle_master e on e.sVhclIDxx = c.sVhclIDxx " +
                " LEFT JOIN vehicle_color f ON f.sColorIDx = e.sColorIDx " +
                " LEFT JOIN customer_inquiry g on g.sTransNox = b.sInqryIDx " +
                " LEFT JOIN client_master h on h.sClientID = a.sClientID " +                
                " LEFT JOIN ggc_isysdbf.client_master i ON i.sClientID = g.sEmployID " +
                " LEFT JOIN client_address j ON j.sClientID = h.sClientID AND j.cPrimaryx = '1' " +
                " LEFT JOIN TownCity k ON k.sTownIDxx = j.sTownIDxx " +  
                " LEFT JOIN barangay l ON l.sBrgyIDxx = j.sBrgyIDxx AND l.sTownIDxx = j.sTownIDxx " +
                " LEFT JOIN Province m ON m.sProvIDxx = k.sProvIDxx " +
                " LEFT JOIN client_master n on n.sClientID = b.sCoCltIDx " ;
    }
    
    public boolean searchUDR (String fsValue, String fsType) throws SQLException{
        String lsSQL = getSQ_SearchUdr();
        
        lsSQL = lsSQL + " WHERE a.sReferNox LIKE " + SQLUtil.toSQL(fsValue + "%") +
                        " AND a.cTranStat = '1' AND cCustType = " + SQLUtil.toSQL(fsType)   +
                        " GROUP BY a.sTransNox " ;
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sSourceCd", loRS.getString("sTransNox"));
                setMaster("sSourceNo", loRS.getString("sReferNox"));
                setMaster("sClientID", loRS.getString("sClientID"));
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
                setMaster("sPlateNox", loRS.getString("sPlateNox"));
                setMaster("sFrameNox", loRS.getString("sFrameNox"));
                setMaster("sEngineNo", loRS.getString("sEngineNo"));
                setMaster("sColorDsc", loRS.getString("sColorDsc"));
                setMaster("sSalesExe", loRS.getString("sSalesExe"));                              
                setMaster("nAddlDscx", Double.valueOf(loRS.getString("nAddlDscx")));
                setMaster("nPromoDsc", Double.valueOf(loRS.getString("nPromoDsc")));
                setMaster("nFleetDsc", Double.valueOf(loRS.getString("nFleetDsc")));
                setMaster("nUnitPrce", Double.valueOf(loRS.getString("nUnitPrce")));                
                setMaster("nDiscount", (Double.valueOf(loRS.getString("nAddlDscx")) +
                                                Double.valueOf(loRS.getString("nPromoDsc")) +
                                                Double.valueOf(loRS.getString("nFleetDsc"))));
                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                setMaster("sAddressx", loRS.getString("sAddressx"));
                setMaster("sBranchCD", loRS.getString("sBranchCD"));
                setMaster("sTaxIDNox", loRS.getString("sTaxIDNox"));
                setMaster("sCoCltIDx", loRS.getString("sCoCltIDx"));
                setMaster("sCoCltNmx", loRS.getString("sCoCltNmx"));
            } else {
                psMessage = "No record found.";
                setMaster("sSourceCd", "");
                setMaster("sSourceNo", "");
                setMaster("sClientID", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sColorDsc", "");
                setMaster("sSalesExe", "");
                setMaster("nAddlDscx", "");
                setMaster("nPromoDsc", "");
                setMaster("nFleetDsc", "");
                setMaster("nUnitPrce", "");
                setMaster("nDiscount", "");
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sBranchCD", "");
                setMaster("sTaxIDNox", "");
                setMaster("sCoCltIDx", "");
                setMaster("sCoCltNmx", "");
                return false;
            }           
        } else {
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "",
                                             "UDR No»Customer Name", 
                                             "sReferNox»sCompnyNm",
                                             "sReferNox»sCompnyNm",
                                             0);
            
            if (loJSON != null){
                setMaster("sSourceCd", (String) loJSON.get("sTransNox"));
                setMaster("sSourceNo", (String) loJSON.get("sReferNox"));
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
                setMaster("sColorDsc", (String) loJSON.get("sColorDsc"));
                setMaster("sSalesExe", (String) loJSON.get("sSalesExe"));
                setMaster("nAddlDscx", Double.valueOf((String)  loJSON.get("nAddlDscx")));
                setMaster("nPromoDsc", Double.valueOf((String)  loJSON.get("nPromoDsc")));
                setMaster("nFleetDsc", Double.valueOf((String)  loJSON.get("nFleetDsc")));
                setMaster("nUnitPrce", Double.valueOf((String)  loJSON.get("nUnitPrce")));
                setMaster("nDiscount", (Double.valueOf((String)  loJSON.get("nAddlDscx")) +
                                                Double.valueOf((String)  loJSON.get("nPromoDsc")) +
                                                Double.valueOf((String)  loJSON.get("nFleetDsc"))));
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sAddressx", (String) loJSON.get("sAddressx"));
                setMaster("sBranchCD", (String) loJSON.get("sBranchCD"));
                setMaster("sTaxIDNox", (String) loJSON.get("sTaxIDNox"));
                setMaster("sCoCltIDx", (String) loJSON.get("sCoCltIDx"));
                setMaster("sCoCltNmx", (String) loJSON.get("sCoCltNmx"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sSourceCd", "");
                setMaster("sSourceNo", "");
                setMaster("sClientID", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sColorDsc", "");
                setMaster("sSalesExe", "");
                setMaster("nAddlDscx", "");
                setMaster("nPromoDsc", "");
                setMaster("nFleetDsc", "");
                setMaster("nUnitPrce", "");
                setMaster("nDiscount", "");
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sBranchCD", "");
                setMaster("sTaxIDNox", "");
                setMaster("sCoCltIDx", "");
                setMaster("sCoCltNmx", "");
                return false;    
            }
        } 
        
        return true;
    }
    
    public boolean computeAmount() throws SQLException{
        psMessage = "";
        String ls_FormType = (String) getMaster("cFormType");
        double ldbl_bpvatp = 0.00;
        double ldbl_VatRatex = 0.00;
        double ldbl_DiscAmt = 0.00; 
        double ldbl_BasePrice = 0.00;
        double ldbl_VatAmtxx = 0.00;
        double ldbl_TranTotl = 0.00;
        
        //get current vat rate from standard_sets
        String lsSQL = getSQ_StandardSets();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL, "sDescript = 'baseprice_with_vat_percent'");
        loRS = poGRider.executeQuery(lsSQL);
        if (loRS.next()) {               
            ldbl_bpvatp = loRS.getDouble("sValuexxx"); 
        }
        //get
        lsSQL = "";
        lsSQL = getSQ_StandardSets();
        lsSQL = MiscUtil.addCondition(lsSQL, "sDescript = 'vat_percent'");
        loRS = poGRider.executeQuery(lsSQL);
        if (loRS.next()) {               
            ldbl_VatRatex = loRS.getInt("sValuexxx");           
        }
        //set vatp value in si
        setMaster("nVatRatex",ldbl_VatRatex);
                                                             
        //Amount to be Pay
        double ldbl_UnitPrce = (Double) getMaster("nUnitPrce");
        
        //Discounted Amount
//        double ldblAddlDscx = (Double) getMaster("nAddlDscx");
//        double ldblPromoDsc = (Double) getMaster("nPromoDsc"); 
//        double ldblFleetDsc = (Double) getMaster("nFleetDsc");
        
        //ldbl_DiscAmt = ldblAddlDscx + ldblPromoDsc + ldblFleetDsc;
        ldbl_DiscAmt = (Double) getMaster("nDiscount");
        
        /*1. get final ldbl_UnitPrce value */
        if (ls_FormType == "1") {
            ldbl_UnitPrce = ldbl_UnitPrce - ldbl_DiscAmt;
        }
        /*2. Compute for the Base Price and VAT Amount 
		using ie vat of 12%
		
		given: ldbl_vhclsrp (vehicle srp) as vat inclusive srp
				112% as equivalent percentage value for this vat inclusive price
				100% as equivalent percentage value for base price (no vat yet)
				base price (no vat yet) = ? 
		principle of ratio: <base price> : 100% = <vat inclusive srp> : 112% (100% + 12% vat)*/
        if (ldbl_bpvatp > 0.00) {
            //compute the base price (no vat srp)
            ldbl_BasePrice = ldbl_UnitPrce / (ldbl_bpvatp/100); 
            //compute the ie 12% vat amount from the base price (no vat srp)
            ldbl_VatAmtxx = ldbl_BasePrice * (ldbl_VatRatex/100); 
        }
        
        /*3. Compute for Final Sales Amount 	
	 (base price (no vat srp) + vatamt) should be equal to original value of uprice uprice (vehicle srp) as vat inclusive price */	 	
	if (ls_FormType == "1"){  //computation for non-dealer/supplier sales
            ldbl_TranTotl = ldbl_BasePrice + ldbl_VatAmtxx;
        }else{
            //deduct discounts from end result only
            ldbl_TranTotl = (ldbl_BasePrice + ldbl_VatAmtxx) - ldbl_DiscAmt ;
        }
        
        setMaster("nVatAmtxx",ldbl_VatAmtxx);
        setMaster("nTranTotl",ldbl_TranTotl);
        return true;
    }
    
    private String getSQ_StandardSets(){
        return  "SELECT " +
                " IFNULL(sValuexxx,'') sValuexxx " +
                " FROM xxxstandard_sets ";
    }
    
    public boolean CancelInvoice(String fsValue) throws SQLException {
        if (pnEditMode != EditMode.READY) {
            psMessage = "Invalid update mode detected.";
            return false;
        }

        psMessage = "";

        if (((String) getMaster("cTranStat")).equals("0")) {
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

        String lsSQL = "UPDATE " + MASTER_TABLE + " SET"
                + " cTranStat = '0'"
                + " WHERE sTransNox = " + SQLUtil.toSQL(fsValue);

        if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }

        //pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    /**
    * Validates the data for a new sales invoice entry.
    *
    * @return true if the data is valid and no issues are found, false if there are validation issues.
    */
    private boolean isEntryOK() throws SQLException{
        poMaster.first();
        
        if (poMaster.getString("sReferNox").isEmpty()){
            psMessage = "Sales Invoice Number is not set.";
            return false;
        }
        
        if (poMaster.getString("sSourceNo").isEmpty()){
            psMessage = "UDR is not set.";
            return false;
        }
        
        String sSINo = "";
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        /*DO NOT ALLOW WHEN UDR HAS EXISTING VEHICLE SALES INVOICE*/
        lsSQL = lsSQL + " WHERE a.sSourceCd = " + SQLUtil.toSQL(poMaster.getString("sSourceCd")) +
                            " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox")) +
                            " AND a.cTranStat <> '0'";
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            if(loRS.next()){
                sSINo = loRS.getString("sReferNox");
            }
            psMessage = "Sales Invoice Found. Please check SI No. " + sSINo + ". ";
            MiscUtil.close(loRS);        
            return false;
        }
        
        /*DO NOT ALLOW WHEN VEHICLE SALES INVOICE NUMBER ALREADY EXIST*/
        lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sReferNox = " + SQLUtil.toSQL(poMaster.getString("sReferNox")) +
                                                " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox"))); 
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "SI No. " + poMaster.getString("sReferNox") + " already exist.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        /*DO NOT ALLOW WHEN VSP AND INQUIRY PAYMENT MODE IS NOT THE SAME*/
        lsSQL = getSQ_SearchUdr()+ " WHERE a.sTransNox = " + SQLUtil.toSQL(poMaster.getString("sSourceCd")) +
                        " AND b.cPayModex <> g.cPayModex" + 
                        " GROUP BY a.sTransNox " ;
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "VSP Payment Mode must be the same with Inquiry.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        return true;
    }
}
