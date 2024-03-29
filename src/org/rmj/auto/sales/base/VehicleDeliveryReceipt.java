/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.sales.base;

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
 * @author Arsiela
 * Date Created: 07-20-2023
 */
public class VehicleDeliveryReceipt {
    private final String MASTER_TABLE = "udr_master";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autoapp_json/" + TabsStateManager.getJsonFileName("Unit Delivery Receipt");
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psReport;
    
    private CachedRowSet poMaster;
    private CachedRowSet poMasterOrig;
    
    public VehicleDeliveryReceipt(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
        poMaster.last();
        return poMaster.getRow();
    }
    
    private String toJSONString(){
        JSONParser loParser = new JSONParser();
        JSONArray laMaster = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;
        
        try {
            loJSON = new JSONObject();
            String lsValue =  CommonUtils.RS2JSON(poMaster).toJSONString();
            laMaster = (JSONArray) loParser.parse(lsValue);
            loMaster = (JSONObject) laMaster.get(0);
            loJSON.put("master", loMaster);
            
            // Populate mode with data
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
            Logger.getLogger(VehicleDeliveryReceipt.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }
    
    private void saveState(String fsValue){
        if(pnEditMode == EditMode.UNKNOWN){
            return;
        }
        
        File Delfile = new File(FILE_PATH);
        if (Delfile.exists() && Delfile.isFile()) {
        } else {
            return;
        }
        
        try {
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
                        if(value == null){
                            tempValue = "";
                        } else {
                            tempValue = String.valueOf(value);
                        }
                        switch(poMaster.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                            case Types.CHAR:
                            case Types.VARCHAR:
                                poMaster.updateObject(Integer.valueOf(key.toString()), tempValue);
                                break;
                            case Types.DATE:
                            case Types.TIMESTAMP:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = DEFAULT_DATE;
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poMaster.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                break;
                            case Types.INTEGER:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = "0";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poMaster.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue));
                                break;
                            case Types.DECIMAL:
                            case Types.DOUBLE:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = "0.00";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poMaster.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue));
                                break;
                            default:
                                poMaster.updateObject(Integer.valueOf(key.toString()), tempValue);
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
            Logger.getLogger(VehicleDeliveryReceipt.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poMaster.first();
        
        switch (fnIndex){            
            case 3://sReferNox  
            case 4://sClientID
            case 5://sSerialID                  
            case 6://sREmarksx  
            case 10://sPONoxxxx
            case 11://sSourceCD
            case 12://sSourceNo
            case 13://cPrintedx
            case 14://sPrepared
            case 15://sApproved
            case 22://sCompnyNm
            case 23://sAddressx
            case 24://sDescript
            case 25://sCSNoxxxx
            case 26://sPlateNox
            case 27://sFrameNox
            case 28://sEngineNo
            case 29://sVSPNOxxx 
            case 30://cIsVhclNw
            case 31://sInqryIDx
            case 32://cCustType
            case 33://sCoCltIDx
            case 34://sCoCltNmx
            case 35://sPreparNm
            case 36://sBranchNm
            case 37://sBrnchAdd
            case 38://cPayModex
            case 39://sColorDsc
            case 40://cTrStatus
            case 41://sBranchCd
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 2://dTransact 
                 if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break; 
            case 17://cTranStat
            case 16://cCallStat
            //case 30://cIsVhclNw
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
                break;
            case 7: //nGrossAmt   
            case 8: //nDiscount  
            case 9: //nTranTotl  
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);                
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
            String lsSQL = (getSQ_Master()+  "WHERE 0=1");
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
    
    /**
    * Searches for a record based on the provided value. If the application driver is not set or if no record is found, it returns false.
    * @param fsValue The value used for searching.
    * @return True if a record is found and successfully opened, false if the application driver is not set or if no record is found.
    * @throws SQLException if a database access error occurs.
    */
    public boolean searchRecord(String fsValue) throws SQLException{
        if (poGRider == null) {
            psMessage = "Application driver is not set.";
            return false;
        }

        psMessage = "";

        String lsSQL = getSQ_Master();

        if (pbWithUI) {
            JSONObject loJSON = showFXDialog.jsonSearch(
                    poGRider,
                    lsSQL,
                    fsValue,
                    "UDR No»Customer Name»Cancelled",
                    "sReferNox»sCompnyNm»cTrStatus»",
                    "a.sReferNox»sCompnyNm",
                    0);

            if (loJSON != null) {
                return OpenRecord((String) loJSON.get("sTransNox"));
            } else {
                psMessage = "No record selected.";
                return false;
            }
        }
              
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (!loRS.next()) {
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }

        lsSQL = loRS.getString("sTransNox");
        MiscUtil.close(loRS);

        return OpenRecord(lsSQL);
    }
    
    public boolean OpenRecord(String fsValue){
        try {
            String lsSQL = "";
            lsSQL = (getSQ_Master()+ "WHERE a.sTransNox = " + SQLUtil.toSQL(fsValue));
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
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
        
        pnEditMode = EditMode.READY;
        saveState(toJSONString());
        return true;
    }    
    
    /**
    * Sets the edit mode to "Update" for the current record.
    * @return True to indicate that the record is now in "Update" mode.
    */
    public boolean UpdateRecord(){
        try {
            if (poMaster != null){
                poMasterOrig = (CachedRowSet) poMaster.createCopy();
            }
        } catch (SQLException ex) {
            Logger.getLogger(VehicleDeliveryReceipt.class.getName()).log(Level.SEVERE, null, ex);
        }
        pnEditMode = EditMode.UPDATE;
        saveState(toJSONString());
        return true;        
    }
    /**
    * Saves the record with the changes made.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            //set REFERNOX for auto generated UDR print
            String sReferNox = "";
            if (!isEntryOK()) return false;
            
            if (pnEditMode == EditMode.ADDNEW){ //add 
                sReferNox = MiscUtil.getNextCode(MASTER_TABLE, "sReferNox", false, poGRider.getConnection(), psBranchCd+"DR");
                setMaster("sReferNox",sReferNox);
            }
            
            if (poMaster.getString("sReferNox").isEmpty()){
                psMessage = "Delivery Receipt Number is not set.";
                return false;
            }
            
            boolean lbisModified = false;
            String lsSQL = "";
            String lsTransNox = MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", true, poGRider.getConnection(), psBranchCd);
            if (pnEditMode == EditMode.ADDNEW){ //add                
                System.out.println(MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", false, poGRider.getConnection(), psBranchCd) );
                //return true;
                //poMaster.updateString("sReferNox",MiscUtil.getNextCode(MASTER_TABLE, "sReferNox", false, poGRider.getConnection(), psBranchCd) );   
                poMaster.updateString("sTransNox",lsTransNox);                  
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sPrepared", poGRider.getUserID());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sVSPNOxxx»cIsVhclNw»sInqryIDx»sCoCltIDx»sCoCltNmx»sPreparNm»sBranchNm»sBrnchAdd»cPayModex»sColorDsc»cTrStatus»sBranchCd");
                
            } else { //update  
                if (!CompareRows.isRowEqual(poMaster, poMasterOrig,1)) {
                    lbisModified = true;
                }
                if(lbisModified){
                    poMaster.updateString("sModified", poGRider.getUserID());
                    poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                    poMaster.updateRow();

                    lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                                MASTER_TABLE, 
                                                "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sVSPNOxxx»cIsVhclNw»sInqryIDx»sCoCltIDx»sCoCltNmx»sPreparNm»sBranchNm»sBrnchAdd»cPayModex»sColorDsc»cTrStatus»sBranchCd", 
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
            
            if (pnEditMode == EditMode.ADDNEW || lbisModified){
                //Update customer_inquiry status to sold and vehicle status to sold
                if(((String) getMaster("cCustType")).equals("0")){
                    lsSQL = "UPDATE customer_inquiry SET" +
                                " cTranStat = '4'" +
                                " , dTargetDt = " + SQLUtil.toSQL((Date) getMaster("dTransact")) +
                            " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));

                    if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, "") <= 0){
                        psMessage = "UPDATE INQUIRY: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                        return false;
                    } 

                    lsSQL = "UPDATE vsp_master SET" +
                                " dDelvryDt = " + SQLUtil.toSQL((Date) getMaster("dTransact")) +
                            " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sSourceCD"));
                    System.out.println(lsSQL);
                    if (poGRider.executeQuery(lsSQL, "vsp_master", psBranchCd, "") <= 0){
                        psMessage = "UPDATE VSP: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                        return false;
                    } 
                }
                
                lsSQL = "UPDATE vehicle_serial SET" +
                            " cSoldStat = '3'" +
                            ", sDealerNm = " + SQLUtil.toSQL(((String) getMaster("sBranchNm")).toUpperCase()) +
                            ", sCompnyID = " + SQLUtil.toSQL((String) getMaster("sBranchCd")) +
                        " WHERE sSerialID = " + SQLUtil.toSQL((String) getMaster("sSerialID"));
                System.out.println(lsSQL);
                if (poGRider.executeQuery(lsSQL, "vehicle_serial", psBranchCd, "") <= 0){
                    psMessage = "UPDATE VEHICLE:" + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                } 
            }
            
            if (!pbWithParent) poGRider.commitTrans();
            
            if (poMaster != null){
                poMasterOrig = (CachedRowSet) poMaster.createCopy();
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        
        return true;
    }
    
    /**
     * Cancels an UDR record in the database.
     * This method is used to cancel an UDR record, with validation checks for the appropriate cancellation scenario. 
     * It updates the transaction status and handles database operations.
     * 
    */
    public boolean cancelUDR() {
        try {
            psMessage = "";
            //if (!isCancelOK()) return false;
            if (!pbWithParent) poGRider.beginTrans();
            
            String lsSQL = " UPDATE " + MASTER_TABLE + " SET "
                    + " cTranStat = '0' "
                    //+ ", sCancelld = " + SQLUtil.toSQL(poGRider.getUserID())
                    //+ ", dCancelld = " + SQLUtil.toSQL((Date) poGRider.getServerDate())
                    + " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox"));
            
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
                psMessage = "UPDATE UDR MASTER: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                return false;
            }
            
            String sStat = "";
            if(((String) getMaster("cCustType")).equals("0")){
                sStat = "2"; //vsp
                
                //Update Inquiry to VSP
                lsSQL = "UPDATE customer_inquiry SET" +
                        " cTranStat = '3'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));
                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, "") <= 0){
                    psMessage = "UPDATE CUSTOMER INQUIRY: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                }
                
            } else {
                sStat = "1"; //Available for sale
            }

            //Update Vehicle Serial to alloted status
            if (!((String) getMaster("sSerialID")).isEmpty()){
                lsSQL = "UPDATE vehicle_serial SET" +
                        " cSoldStat = '"+sStat+"'" +
                        " WHERE sSerialID = " + SQLUtil.toSQL((String) getMaster("sSerialID"));
                if (poGRider.executeQuery(lsSQL, "vehicle_serial", psBranchCd, "") <= 0){
                    psMessage = "UPDATE VEHICLE SERIAL: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                } 
            }
            
            if (!pbWithParent) poGRider.commitTrans();
            pnEditMode = EditMode.UNKNOWN;
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleDeliveryReceipt.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
            Logger.getLogger(VehicleDeliveryReceipt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sReport;
    }
    
    private String getSQ_Master(){
        return " SELECT " + 
                "  a.sTransNox " + //1
                ", a.dTransact " + //2
                ", a.sReferNox " + //3udrno
                ", a.sClientID " + //4
                ", a.sSerialID " + //5
                ", a.sRemarksx " + //6
                ", a.nGrossAmt " + //7
                ", a.nDiscount " + //8
                ", a.nTranTotl " + //9
                ", a.sPONoxxxx " + //10
                ", a.sSourceCD " + //11
                ", a.sSourceNo " + //12
                ", a.cPrintedx " + //13
                ", a.sPrepared " + //14
                ", a.sApproved " + //15
                ", a.cCallStat " + //16
                ", a.cTranStat " + //17
                ", a.sEntryByx " + //18
                ", a.dEntryDte " + //19
                ", a.sModified " + //20
                ", a.dModified " + //21
                ", IFNULL(c.sCompnyNm,'') as sCompnyNm " +//22
                " , IFNULL(CONCAT( IFNULL(CONCAT(gg.sHouseNox,' ') , ''), IFNULL(CONCAT(gg.sAddressx,' ') , ''), " +
                "     IFNULL(CONCAT(i.sBrgyName,' '), ''), " +
                "     IFNULL(CONCAT(h.sTownName, ', '),''), " +
                "     IFNULL(CONCAT(j.sProvName),'') )	, '') AS sAddressx " +//23	
                ", IFNULL(f.sDescript,'') as sDescript " +//24
                ", IFNULL(d.sCSNoxxxx,'') as sCSNoxxxx " +//25
                ", IFNULL(e.sPlateNox,'') as sPlateNox " +//26
                ", IFNULL(d.sFrameNox,'') as sFrameNox " +//27
                ", IFNULL(d.sEngineNo,'') as sEngineNo " +//28
                ", b.sVSPNOxxx " + //29
                ", b.cIsVhclNw " + //30
                ", b.sInqryIDx " + //31
                ", a.cCustType " + //32
                ", IFNULL(b.sCoCltIDx,'') as sCoCltIDx " +//33
                ", IFNULL(k.sCompnyNm,'') as sCoCltNmx " +//34
                ", IFNULL(l.sCompnyNm, '') as sPreparNm " + //35
                " , IFNULL(m.sBranchNm,'') AS sBranchNm " + //36 Branch Name
                " , IFNULL(CONCAT( IFNULL(CONCAT(m.sAddressx,' ') , ''), " +  
                "   IFNULL(CONCAT(n.sTownName, ', '),''),  " +
                "   IFNULL(CONCAT(o.sProvName),'') ), '') AS sBrnchAdd " + //37 Branch Address
                " , IFNULL(b.cPayModex,'') as cPayModex " +//38 Payment mode
                " , IFNULL(p.sColorDsc,'') as sColorDsc " +//39 Color
                " , CASE WHEN a.cTranStat = '0' THEN 'Y' ELSE 'N' END AS cTrStatus " + //40 trans status
                " , IFNULL(b.sBranchCd,'') as sBranchCd   " + //41 branchCD
            " FROM udr_master a " +
            " LEFT JOIN vsp_master b ON b.sTransNox = a.sSourceCD " +
            " LEFT JOIN client_master c ON c.sClientID = a.sClientID " +
            " LEFT JOIN vehicle_serial d ON d.sSerialID = b.sSerialID " +
            " LEFT JOIN vehicle_serial_registration e ON e.sSerialID = b.sSerialID "+
            " LEFT JOIN vehicle_master f ON f.sVhclIDxx = d.sVhclIDxx  " +
            " LEFT JOIN client_address g ON g.sClientID = c.sClientID AND g.cPrimaryx = '1' " +
            " LEFT JOIN addresses gg ON gg.sAddrssID = g.sAddrssID " +
            " LEFT JOIN TownCity h ON h.sTownIDxx = gg.sTownIDxx " + 
            " LEFT JOIN barangay i ON i.sBrgyIDxx = gg.sBrgyIDxx AND i.sTownIDxx = gg.sTownIDxx " +
            " LEFT JOIN Province j ON j.sProvIDxx = h.sProvIDxx " +
            " LEFT JOIN client_master k ON k.sClientID = b.sCoCltIDx " +
            " LEFT JOIN GGC_ISysDBF.Client_Master l ON l.sClientID = a.sPrepared  "  +
            " LEFT JOIN branch m on m.sBranchCd = b.sBranchCd " +
            " LEFT JOIN TownCity n ON n.sTownIDxx = m.sTownIDxx    "  +
            " LEFT JOIN Province o ON o.sProvIDxx = n.sProvIDxx "  +
            " LEFT JOIN vehicle_color p ON p.sColorIDx = f.sColorIDx  " ;
    }
    
    private String getSQ_searchVSP(){
        return " SELECT "																																						
                    + " a.sTransNox "																																					
                    + " , a.dTransact "																																				
                    + " , a.sVSPNOxxx "																																			
                    + " , IFNULL(b.sCompnyNm,'') AS sCompnyNm "	
                    + " , IFNULL(CONCAT( IFNULL(CONCAT(ff.sHouseNox,' ') , ''), IFNULL(CONCAT(ff.sAddressx,' ') , ''), "
                    + "     IFNULL(CONCAT(h.sBrgyName,' '), ''), "
                    + "     IFNULL(CONCAT(g.sTownName, ', '),''), "
                    + "     IFNULL(CONCAT(i.sProvName),'') )	, '') AS sAddressx "	
                    + " , IFNULL(e.sDescript,'') AS sDescript "																																						
                    + " , IFNULL(c.sCSNoxxxx,'') AS sCSNoxxxx "																																						
                    + " , IFNULL(d.sPlateNox,'') AS sPlateNox "																																						
                    + " , IFNULL(c.sFrameNox,'') AS sFrameNox "																																						
                    + " , IFNULL(c.sEngineNo,'') AS sEngineNo "
                    + " , a.sClientID"
                    + " , a.sSerialID"
                    + " , a.cIsVhclNw"
                    + " , a.sInqryIDx"
                    + ", IFNULL(a.sCoCltIDx,'') as sCoCltIDx " 
                    + ", IFNULL(k.sCompnyNm,'') as sCoCltNmx " 
                    + " , IFNULL(l.sBranchNm,'') AS sBranchNm " 
                    + " , IFNULL(CONCAT( IFNULL(CONCAT(l.sAddressx,' ') , ''), "   
                    + "   IFNULL(CONCAT(m.sTownName, ' '),''),  " 
                    + "   IFNULL(CONCAT(n.sProvName),'') ), '') AS sBrnchAdd " 
                    + ", IFNULL(a.cPayModex,'') as cPayModex " 
                    + ", IFNULL(o.sColorDsc,'') as sColorDsc " 
                    + ", IFNULL(a.sBranchCd,'') as sBranchCd " 
                + " FROM vsp_master a " 																																			
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID " 																																					
                + " LEFT JOIN vehicle_serial c ON c.sSerialID = a.sSerialID "																																					
                + " LEFT JOIN vehicle_serial_registration d ON d.sSerialID = c.sSerialID " 
                + " LEFT JOIN vehicle_master e ON e.sVhclIDxx = c.sVhclIDxx " 
                + " LEFT JOIN client_address f ON f.sClientID = b.sClientID AND f.cPrimaryx = '1' " 
                + " LEFT JOIN addresses ff ON ff.sAddrssID = f.sAddrssID" 
                + " LEFT JOIN TownCity g ON g.sTownIDxx = ff.sTownIDxx "  
                + " LEFT JOIN barangay h ON h.sBrgyIDxx = ff.sBrgyIDxx AND h.sTownIDxx = ff.sTownIDxx "
                + " LEFT JOIN Province i ON i.sProvIDxx = g.sProvIDxx " 																																		
                + " LEFT JOIN customer_inquiry j ON j.sTransNox = a.sInqryIDx "																															
                + " LEFT JOIN client_master k ON k.sClientID = a.sCoCltIDx "  
                + " LEFT JOIN branch l on l.sBranchCd = a.sBranchCd " 
                + " LEFT JOIN TownCity m ON m.sTownIDxx = l.sTownIDxx    "  
                + " LEFT JOIN Province n ON n.sProvIDxx = m.sProvIDxx "  
                + " LEFT JOIN vehicle_color o ON o.sColorIDx = e.sColorIDx  " ;
    }
    
    private String getSQ_searchAvlVhcl(){
        return  "SELECT" + //
                "   IFNULL(a.sSerialID,'') sSerialID " + //1
                " , IFNULL(a.sBranchCD,'') sBranchCD " + //2
                " , IFNULL(a.sFrameNox,'') sFrameNox " + //3
                " , IFNULL(a.sEngineNo,'') sEngineNo " + //4
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx " + //5
                " , IFNULL(a.sClientID,'') sClientID " + //6
                " , IFNULL(a.sCoCltIDx,'') sCoCltIDx " + //7
                " , IFNULL(a.sCSNoxxxx,'') sCSNoxxxx " + //8
                " , IFNULL(a.sDealerNm,'') sDealerNm " + //9
                " , IFNULL(a.sCompnyID,'') sCompnyID " + //10
                " , IFNULL(a.sKeyNoxxx,'') sKeyNoxxx " + //11
                " , IFNULL(a.cIsDemoxx,'') cIsDemoxx " + //12
                " , IFNULL(a.cLocation,'') cLocation " + //13
                " , IFNULL(a.cSoldStat,'') cSoldStat " + //14
                " , IFNULL(a.cVhclNewx,'') cVhclNewx " + //15
                " , a.sEntryByx " + //16
                " , a.dEntryDte " + //17
                " , a.sModified " + //18
                " , a.dModified " + //19
                " , IFNULL(b.sPlateNox,'') sPlateNox " + //20
                " , IFNULL(b.dRegister,CAST('1900-01-01' AS DATE)) dRegister " + //21
                " , IFNULL(b.sPlaceReg,'') sPlaceReg " + //22
                " , IFNULL(c.sMakeIDxx,'') sMakeIDxx " + //23
                " , IFNULL(d.sMakeDesc,'') sMakeDesc " + //24 
                " , IFNULL(c.sModelIDx,'') sModelIDx " + //25
                " , IFNULL(e.sModelDsc,'') sModelDsc " + //26   
                " , IFNULL(c.sTypeIDxx,'') sTypeIDxx " + //27 
                " , IFNULL(f.sTypeDesc,'') sTypeDesc " + //28   
                " , IFNULL(c.sColorIDx,'') sColorIDx " + //29 
                " , IFNULL(g.sColorDsc,'') sColorDsc " + //30
                " , IFNULL(c.sTransMsn,'') sTransMsn " + //31
                " , IFNULL(c.nYearModl,'') nYearModl " + //32
                " , IFNULL(c.sDescript,'') sDescript " + //33
                " , IFNULL(a.sRemarksx,'') sRemarksx " + //34
                "   FROM vehicle_serial a " + 
                "   LEFT JOIN vehicle_serial_registration b ON a.sSerialID = b.sSerialID  " +
                "   LEFT JOIN vehicle_master c ON c.sVhclIDxx = a.sVhclIDxx  " +
                "   LEFT JOIN vehicle_make d ON d.sMakeIDxx = c.sMakeIDxx  " +
                "   LEFT JOIN vehicle_model e ON e.sModelIDx = c.sModelIDx  " +
                "   LEFT JOIN vehicle_type f ON f.sTypeIDxx = c.sTypeIDxx  " +
                "   LEFT JOIN vehicle_color g ON g.sColorIDx = c.sColorIDx  " ;
    }
    
    /**
     * For searching available vehicle when key is pressed.
     * @return {@code true} if a matching available vehicle is found, {@code false} otherwise.
    */
    public boolean searchAvailableVhcl(String fsValue, boolean fbValue) throws SQLException{
        String lsSQL = getSQ_searchAvlVhcl();
        
        lsSQL = MiscUtil.addCondition(lsSQL, " a.cSoldStat = '1' " +
                                                 " AND (a.sCSNoxxxx LIKE " + SQLUtil.toSQL(fsValue + "%")  +
                                                 " OR b.sPlateNox LIKE " + SQLUtil.toSQL(fsValue + "%") + " ) " );
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
                setMaster("sPlateNox", loRS.getString("sPlateNox"));
                setMaster("sFrameNox", loRS.getString("sFrameNox"));
                setMaster("sEngineNo", loRS.getString("sEngineNo"));
            } else {
                psMessage = "No record found.";
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle for Sale", "sSerialID");
            
            if (loJSON != null){
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                return false;    
            }
        } 
         
        return true;
    }
    
    /**
    * Searches for a record using the provided value. If the application driver is not set or if no record is found, it returns false.
    *
    * @param fsValue The value used for searching.
    * @return True if a record is found and its details are set, false if the application driver is not set or if no record is found.
    * @throws SQLException if a database access error occurs.
    */
    public boolean searchVSP(String fsValue) throws SQLException{
        String lsSQL = getSQ_searchVSP();
        lsSQL = lsSQL + " WHERE a.sVSPNOxxx LIKE " + SQLUtil.toSQL(fsValue + "%") +
                        " AND (a.sSerialID <> NULL OR a.sSerialID <> '') " +
                        " AND a.cTranStat = '1' AND j.cTranStat <> '6' "  +
                        " GROUP BY a.sTransNox " ;
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sVSPNOxxx", loRS.getString("sVSPNOxxx"));
                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                setMaster("sAddressx", loRS.getString("sAddressx"));
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
                setMaster("sPlateNox", loRS.getString("sPlateNox"));
                setMaster("sFrameNox", loRS.getString("sFrameNox"));
                setMaster("sEngineNo", loRS.getString("sEngineNo"));
                setMaster("sClientID", loRS.getString("sClientID"));
                setMaster("sSerialID", loRS.getString("sSerialID"));
                setMaster("cIsVhclNw", loRS.getString("cIsVhclNw"));
                setMaster("sSourceCD", loRS.getString("sTransNox"));
                setMaster("sSourceNo", loRS.getString("sVSPNOxxx"));
                setMaster("sInqryIDx", loRS.getString("sInqryIDx"));  
                setMaster("sCoCltIDx", loRS.getString("sCoCltIDx"));
                setMaster("sCoCltNmx", loRS.getString("sCoCltNmx")); 
                setMaster("sBranchNm", loRS.getString("sBranchNm"));
                setMaster("sBrnchAdd", loRS.getString("sBrnchAdd"));  
                setMaster("cPayModex", loRS.getString("cPayModex"));
                setMaster("sColorDsc", loRS.getString("sColorDsc")); 
                setMaster("sBranchCd", loRS.getString("sBranchCd"));   
           
            } else {
                psMessage = "No record found.";
                setMaster("sVSPNOxxx", "");
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sClientID", "");
                setMaster("sSerialID", "");
                setMaster("cIsVhclNw", "");
                setMaster("sSourceCD", "");
                setMaster("sSourceNo", "");
                setMaster("sInqryIDx", ""); 
                setMaster("sCoCltIDx", "");
                setMaster("sCoCltNmx", "");  
                setMaster("sBranchNm", "");
                setMaster("sBrnchAdd", "");  
                setMaster("cPayModex", "");
                setMaster("sColorDsc", ""); 
                setMaster("sBranchCd", "");    
                return false;
            }           
        } else {
            //loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "VSP No»Customer Name»CS NO»Plate no", 
                                             "sVSPNOxxx»sCompnyNm»sCSNoxxxx»sPlateNox",
                                             "sVSPNOxxx»sCompnyNm»sCSNoxxxx»sPlateNox",
                                             0);
            
            if (loJSON != null){
                setMaster("sVSPNOxxx", (String) loJSON.get("sVSPNOxxx"));
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sAddressx", (String) loJSON.get("sAddressx"));
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("sSerialID", (String) loJSON.get("sSerialID"));
                setMaster("cIsVhclNw", (String) loJSON.get("cIsVhclNw"));
                setMaster("sSourceCD", (String) loJSON.get("sTransNox"));
                setMaster("sSourceNo", (String) loJSON.get("sVSPNOxxx"));
                setMaster("sInqryIDx", (String) loJSON.get("sInqryIDx")); 
                setMaster("sCoCltIDx", (String) loJSON.get("sCoCltIDx"));
                setMaster("sCoCltNmx", (String) loJSON.get("sCoCltNmx"));   
                setMaster("sBranchNm", (String) loJSON.get("sBranchNm"));
                setMaster("sBrnchAdd", (String) loJSON.get("sBrnchAdd"));  
                setMaster("cPayModex", (String) loJSON.get("cPayModex"));
                setMaster("sColorDsc", (String) loJSON.get("sColorDsc"));
                setMaster("sBranchCd", (String) loJSON.get("sBranchCd"));     
            } else {
                psMessage = "No record found/selected.";
                setMaster("sVSPNOxxx", "");
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sClientID", "");
                setMaster("sSerialID", "");
                setMaster("cIsVhclNw", "");
                setMaster("sSourceCD", "");
                setMaster("sSourceNo", "");
                setMaster("sInqryIDx", "");
                setMaster("sCoCltIDx", "");
                setMaster("sCoCltNmx", "");   
                setMaster("sBranchNm", "");
                setMaster("sBrnchAdd", "");  
                setMaster("cPayModex", "");
                setMaster("sColorDsc", ""); 
                setMaster("sBranchCd", "");   
                return false;    
            }
        } 
        
        return true;
    }
    
    /**
    * Checks if the entry is valid for the current record. This method validates the "Delivery Receipt Number" field.
    * @return True if the entry is valid, false if there are errors or if the Delivery Receipt Number already exists in other records.
    * @throws SQLException if a database access error occurs.
    */
    private boolean isEntryOK() throws SQLException{
        poMaster.first();
        
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = lsSQL + " WHERE a.sReferNox = " + SQLUtil.toSQL(poMaster.getString("sReferNox")) +
                                                " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox"));
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Delivery Receipt Number.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        lsSQL = getSQ_searchVSP() + " WHERE a.sTransNox = " + SQLUtil.toSQL(poMaster.getString("sSourceCD")) +
                        " AND a.cPayModex <> j.cPayModex AND j.cTranStat <> '6' " + 
                        " GROUP BY a.sTransNox " ;
        
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "VSP Payment Mode must be the same with inquiry.";
            MiscUtil.close(loRS);        
            return false;
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
}

