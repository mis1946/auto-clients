/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.clients.base;

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
import org.rmj.auto.json.TabsStateManager;

/**
 * 
 * @author Arsiela
 * Date Created: 05-31-2023
 */
public class ClientVehicleInfo {
    private final String MASTER_TABLE = "vehicle_serial";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autapp_json/" ;
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    private boolean pbisVhclSales;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private String psSerialID;
    private String psClientID;
    
    public CachedRowSet poVehicle;
    private CachedRowSet poVehicleDetail;
    private CachedRowSet poOriginalVehicle;
    
    public ClientVehicleInfo(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
        if (poVehicleDetail != null){
            poVehicleDetail.last();
            return poVehicleDetail.getRow();
        }else{
            return 0;
        }
    }
    
    public void setClientID(String fsValue) {
        psClientID = fsValue;
    }
    
    public void setFormType(boolean fbValue) {
        pbisVhclSales = fbValue;
    }
    
    
    private String toJSONString(){
        JSONParser loParser = new JSONParser();
        JSONArray laMaster = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;
        
        try {
            loJSON = new JSONObject();
            String lsValue =  CommonUtils.RS2JSON(poVehicle).toJSONString();
            laMaster = (JSONArray) loParser.parse(lsValue);
            loMaster = (JSONObject) laMaster.get(0);
            loJSON.put("master", loMaster);
            
            // Populate master2Array with data
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
            Logger.getLogger(ClientVehicleInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }
    
    private void saveState(String fsValue){
        if(pnEditMode == EditMode.UNKNOWN){
            return;
        }
        
        String sFile = "";
        if(pbisVhclSales){
            sFile = FILE_PATH + TabsStateManager.getJsonFileName("Vehicle Sales Information");
        } else {
            sFile = FILE_PATH + TabsStateManager.getJsonFileName("Customer Vehicle Information");
        }
        
        try {
            // Write the JSON object to file
            try (FileWriter file = new FileWriter(sFile)) {
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
            
            String sFile = "";
            if(pbisVhclSales){
                sFile = FILE_PATH + TabsStateManager.getJsonFileName("Vehicle Sales Information");
            } else {
                sFile = FILE_PATH + TabsStateManager.getJsonFileName("Customer Vehicle Information");
            }
            
            // Parse the JSON file
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(sFile));
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
                    poVehicle.first();
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
                        switch(poVehicle.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                            case Types.CHAR:
                            case Types.VARCHAR:
                                poVehicle.updateObject(Integer.valueOf(key.toString()), tempValue);
                                //setMaster(Integer.valueOf(key.toString()), tempValue);
                            break;
                            case Types.DATE:
                            case Types.TIMESTAMP:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = DEFAULT_DATE;
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poVehicle.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                //setMaster(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE));
                            break;
                            case Types.INTEGER:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = "0";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poVehicle.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue));
                                //setMaster(Integer.valueOf(key.toString()), Integer.valueOf(tempValue));
                            break;
                            case Types.DECIMAL:
                            case Types.DOUBLE:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = "0.00";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poVehicle.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue));
                                //setMaster(Integer.valueOf(key.toString()), Double.valueOf(tempValue));
                            break;
                            default:
                                //System.out.println("MASTER value : " + tempValue + " negative key #" + Integer.valueOf(key.toString()) +" : "  + poVehicle.getMetaData().getColumnType(Integer.valueOf(key.toString())));
                                poVehicle.updateObject(Integer.valueOf(key.toString()), tempValue);
                                //setMaster(Integer.valueOf(key.toString()), tempValue);
                            break;
                        }
                        tempValue = "";
                    }
                    poVehicle.updateRow();
                }
            } else {
                psMessage = "";
                return false;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(ClientVehicleInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poVehicle.first();
        
        switch (fnIndex){            
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 20:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
                poVehicle.updateObject(fnIndex, (String) foValue);
                poVehicle.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 21:
                if (foValue instanceof Date){
                    poVehicle.updateObject(fnIndex, foValue);
                } else {
                    poVehicle.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poVehicle.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break; 
                                                            
        }
        saveState(toJSONString());
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
        
        poVehicleDetail.absolute(fnRow);
        return poVehicleDetail.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poVehicleDetail, fsIndex));
    }
    
    /**
     * Creates a new record in the vehicle table.
     * @return {@code true} if a new record is successfully created, {@code false} otherwise.
    */
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
            if(pbisVhclSales){
                poVehicle.updateString("cSoldStat", "1");
            } else {
                poVehicle.updateString("cSoldStat", "0");
            }
            poVehicle.updateObject("dRegister", SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));    
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
     * Loads the list of vehicles from the database base of client ID.
     * @param fsValue Identify as the client ID
     * @param fbisOwner Identify who will be retrieve
     * @return {@code true} if the list is successfully loaded, {@code false} otherwise.
     * @throws SQLException if a database error occurs.
    */
    public boolean LoadList(String fsValue, boolean fbisOwner) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        if(fbisOwner){
            lsSQL =  MiscUtil.addCondition(lsSQL, "a.sClientID = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL =  MiscUtil.addCondition(lsSQL, "a.sCoCltIDx = " + SQLUtil.toSQL(fsValue));
        }
        
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        poVehicleDetail = factory.createCachedRowSet();
        poVehicleDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    /**
     *  Searches for a Customer Vehicle Information record.
     * 
    */
    public boolean searchRecord() throws SQLException{
        String lsSQL = getSQ_Master();
        if(pbisVhclSales){
            lsSQL = getSQ_Master() + " GROUP BY a.sSerialID " ;
        } else {
            lsSQL = getSQ_Master() + " WHERE (NOT ISNULL(a.sClientID) AND  TRIM(a.sClientID) != '' ) " +
                       " GROUP BY a.sSerialID " ;
        }
        System.out.println(lsSQL);
        JSONObject loJSON = null;
        if (pbWithUI){
            loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , "%"
                                                    , "CS No»Plate No»Owner»Co-Owner»Vehicle Status»"
                                                    , "sCSNoxxxx»sPlateNox»sOwnerNam»sCoOwnerN»sVhclStat"
                                                    , "a.sCSNoxxxx»b.sPlateNox»h.sCompnyNm»i.sCompnyNm»a.cSoldStat"
                                                    , 0);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                //if (LoadList((String) loJSON.get("sClientID")) ){
                if (OpenRecord((String) loJSON.get("sSerialID")) ){
                    
                }else {
                    psMessage = "No record found/selected.";
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Opens the record of a specific vehicle identified by the given serial ID.
     * @param fsValue the serial ID of the vehicle to open.
     * @return {@code true} if the record is successfully opened, {@code false} otherwise.
    */
    public boolean OpenRecord(String fsValue){
//        if (poVehicleDetail == null){
//            psMessage = "Application driver is not set.";
//            return false;
//        }
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sSerialID = " + SQLUtil.toSQL(fsValue));
            System.out.println(lsSQL);
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
    /**
     * Sets the edit mode to UPDATE and creates a copy of the current vehicle record for comparison.
     * @return {@code true} if the edit mode is successfully set to UPDATE, {@code false} otherwise.
    */
    public boolean UpdateRecord(){
        try {
            if (poVehicle != null){
                poOriginalVehicle = (CachedRowSet) poVehicle.createCopy();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientVehicleInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        pnEditMode = EditMode.UPDATE;
        return true;   
    }
    
    /**
     * Saves the record based on the current edit mode.
     * @return {@code true} if the record is successfully saved, {@code false} otherwise.
     * @throws SQLException if an error occurs while accessing the database.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            int lnCtr = 1;
            boolean lbisModified = false;
            String lsSQL = "";
            psSerialID = "";
            if (!pbWithParent) poGRider.beginTrans();
            if (pnEditMode == EditMode.ADDNEW){ //add
                psSerialID = MiscUtil.getNextCode(MASTER_TABLE, "sSerialID", true, poGRider.getConnection(), psBranchCd) ; 
                poVehicle.updateString("sSerialID", psSerialID );  
                poVehicle.updateString("sBranchCD", psBranchCd);  
                //poVehicle.updateString("sClientID", psClientID);
                poVehicle.updateString("sEntryByx", poGRider.getUserID());
                poVehicle.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, MASTER_TABLE, "sPlateNox»dRegister»sPlaceReg»sMakeIDxx»sMakeDesc»sModelIDx»sModelDsc»sTypeIDxx»sTypeDesc»sColorIDx»sColorDsc»sTransMsn»nYearModl»sDescript»sOwnerNam»sCoOwnerN»sOwnerAdd»sCoOwnerA»sVhclStat»sUdrNoxxx»sUdrDatex»sSoldToxx");
                if (lsSQL.isEmpty()){
                    psMessage = "No record to update.";
                    return false;
                }
                
                /*PROCEED TO: vehicle_serial saving*/
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                    return false;
                }
                
            } else { //update
                if (!CompareRows.isRowEqual(poVehicle, poOriginalVehicle,1)) {
                    lbisModified = true;
                }
                psSerialID = (String) getMaster("sSerialID") ;
                if(lbisModified){
                    poVehicle.updateString("sModified", poGRider.getUserID());
                    poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                    poVehicle.updateRow();

                    lsSQL = MiscUtil.rowset2SQL(poVehicle, 
                                                MASTER_TABLE, 
                                                "sPlateNox»dRegister»sPlaceReg»sMakeIDxx»sMakeDesc»sModelIDx»sModelDsc»sTypeIDxx»sTypeDesc»sColorIDx»sColorDsc»sTransMsn»nYearModl»sDescript»sOwnerNam»sCoOwnerN»sOwnerAdd»sCoOwnerA»sVhclStat»sUdrNoxxx»sUdrDatex»sSoldToxx", 
                                                "sSerialID = " + SQLUtil.toSQL(psSerialID));
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }

                    /*PROCEED TO: vehicle_serial saving*/                   
                    if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                        psMessage = poGRider.getErrMsg();
                        if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                    }
                }
            }
            
            /*PROCEED TO: vehicle_serial_registration saving*/
            String lsPlateNox, lsPlcRegs;
            Date ldDateRegs;
            lsPlateNox = (String) getMaster("sPlateNox");
            ldDateRegs = SQLUtil.toDate(CommonUtils.xsDateShort((Date) getMaster("dRegister")), SQLUtil.FORMAT_SHORT_DATE); //(Date) getMaster("dRegister");
            lsPlcRegs = (String) getMaster("sPlaceReg");
            
            //Proceed to ADD if pnEditMode is addnew and if the original cacherowset is empty
            if (pnEditMode == EditMode.ADDNEW || ((pnEditMode == EditMode.UPDATE) && (!vhclExistRegs()) )){ //add
                if  (   ((!lsPlateNox.isEmpty() && !lsPlateNox.equals(""))) 
                        //||  ((!ldDateRegs.equals(SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE))))
                        ||  ((!lsPlcRegs.isEmpty() && !lsPlcRegs.equals(""))) ) {
                
                    lsSQL = "INSERT INTO vehicle_serial_registration  " +
                            "(sSerialID,sPlateNox,dRegister,sPlaceReg,sEntryByx,dEntryDte,sModified,dModified)" +
                            " VALUES (" + SQLUtil.toSQL(psSerialID) +
                            "," + SQLUtil.toSQL(lsPlateNox) +
                            "," + SQLUtil.toSQL(ldDateRegs) + 
                            "," + SQLUtil.toSQL(lsPlcRegs) +
                            "," + SQLUtil.toSQL(poGRider.getUserID()) + 
                            ", " + SQLUtil.toSQL((Date) poGRider.getServerDate() ) + 
                            "," + SQLUtil.toSQL(poGRider.getUserID()) + 
                            "," + SQLUtil.toSQL((Date) poGRider.getServerDate() ) +
                            ")";
                    
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }
                    if (poGRider.executeQuery(lsSQL, "vehicle_serial_registration", psBranchCd, "") <= 0){
                        psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                        return false;
                    }
                }

            } else { //update
                    lsSQL = "UPDATE vehicle_serial_registration SET" +
                            "  sPlateNox = " + SQLUtil.toSQL(lsPlateNox) +
                            ", dRegister = " + SQLUtil.toSQL(ldDateRegs) +
                            ", sPlaceReg = " + SQLUtil.toSQL(lsPlcRegs) +
                            ", sModified = " + SQLUtil.toSQL(poGRider.getUserID() ) +
                            ", dModified = " + SQLUtil.toSQL((Date) poGRider.getServerDate() ) +
                            " WHERE sSerialID = " + SQLUtil.toSQL(psSerialID);
                    
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }
                    if (poGRider.executeQuery(lsSQL, "vehicle_serial_registration", psBranchCd, "") <= 0){
                        psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                        return false;
                    }
            }
            
            if (!pbWithParent) poGRider.commitTrans();
            
            // Update the original state of the table
            poOriginalVehicle = (CachedRowSet) poVehicle.createCopy();
                    
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        psMessage = "Vehicle saved successfully";
        return true;
    }
    
    public String getSQ_Master(){
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
                " , IFNULL(h.sCompnyNm,'') sOwnerNam " + //35
                " , IFNULL(i.sCompnyNm,'') sCoOwnerN " + //36
                " , IFNULL(CONCAT( IFNULL(CONCAT(j.sAddressx,', ') , ''), " +
                " 	IFNULL(CONCAT(l.sBrgyName,', '), ''), " +
                " 	IFNULL(CONCAT(k.sTownName, ', '),''), " +
                " 	IFNULL(CONCAT(m.sProvName),'') )	, '') AS sOwnerAdd " + //37 
                " , IFNULL(CONCAT( IFNULL(CONCAT(n.sAddressx,', ') , ''), " +
                " 	IFNULL(CONCAT(p.sBrgyName,', '), ''), " +
                " 	IFNULL(CONCAT(o.sTownName, ', '),''), " +
                " 	IFNULL(CONCAT(q.sProvName),'') )	, '') AS sCoOwnerA " + //38 
                " ,CASE " +
                    "    WHEN a.cSoldStat = '0' THEN 'NON SALES CUSTOMER' " +
                    "    WHEN a.cSoldStat = '1' THEN 'AVAILABLE FOR SALE' " +
                    "    WHEN a.cSoldStat = '2' THEN 'VSP' " +
                    "    WHEN a.cSoldStat = '3' THEN 'SOLD' " +
                    "    ELSE '' " +
                    " END AS sVhclStat  " + //39
                " , IFNULL(r.sReferNox,'') sUdrNoxxx " + //40
                " , IFNULL(r.dTransact,'') sUdrDatex " + //41
                " , IFNULL(s.sCompnyNm,'') sSoldToxx " + //42
                "   FROM vehicle_serial a " + 
                "   LEFT JOIN vehicle_serial_registration b ON a.sSerialID = b.sSerialID  " +
                "   LEFT JOIN vehicle_master c ON c.sVhclIDxx = a.sVhclIDxx  " +
                "   LEFT JOIN vehicle_make d ON d.sMakeIDxx = c.sMakeIDxx  " +
                "   LEFT JOIN vehicle_model e ON e.sModelIDx = c.sModelIDx  " +
                "   LEFT JOIN vehicle_type f ON f.sTypeIDxx = c.sTypeIDxx  " +
                "   LEFT JOIN vehicle_color g ON g.sColorIDx = c.sColorIDx  " +
                "   LEFT JOIN client_master h ON h.sClientID = a.sClientID  " +
                "   LEFT JOIN client_master i ON i.sClientID = a.sCoCltIDx  " +
                // Owner Address
                "   LEFT JOIN client_address j ON j.sClientID = a.sClientID AND j.cPrimaryx = '1' " + //AND h.cRecdStat = '1' " +
                "   LEFT JOIN TownCity k on k.sTownIDxx = j.sTownIDxx " + //AND i.cRecdStat = '1'
                "   LEFT JOIN barangay l ON l.sBrgyIDxx = j.sBrgyIDxx and l.sTownIDxx = j.sTownIDxx " + // AND j.cRecdStat = '1'  " +
                "   LEFT JOIN Province m ON m.sProvIDxx = k.sProvIDxx " + // and k.cRecdStat = '1' " +
                //Co Owner Address
                "   LEFT JOIN client_address n ON n.sClientID = a.sCoCltIDx AND n.cPrimaryx = '1' " + //AND h.cRecdStat = '1' " +
                "   LEFT JOIN TownCity o on o.sTownIDxx = n.sTownIDxx " + //AND i.cRecdStat = '1'
                "   LEFT JOIN barangay p ON p.sBrgyIDxx = n.sBrgyIDxx and p.sTownIDxx = n.sTownIDxx " + // AND j.cRecdStat = '1'  " +
                "   LEFT JOIN Province q ON q.sProvIDxx = o.sProvIDxx "  +// and k.cRecdStat = '1' " +
                //UDR INFO
                "   LEFT JOIN udr_master r ON r.sSerialID = a.sSerialID "  +
                "   LEFT JOIN client_master s ON s.sClientID = r.sClientID "  ;
    }
    
    private String getSQ_SearchVhclMake(){
        return  " SELECT " +  
                " IFNULL(a.sMakeIDxx,'') sMakeIDxx  " +   
                " , IFNULL(b.sMakeDesc,'') sMakeDesc " +   
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +   
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_make b ON b.sMakeIDxx = a.sMakeIDxx " ;
    }
    
    private String getSQ_SearchVhclModel(){
        return  " SELECT " +  
                " IFNULL(a.sModelIDx,'') sModelIDx  " +   
                " , IFNULL(b.sModelDsc,'') sModelDsc " +  
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_model b ON b.sModelIDx = a.sModelIDx " ;
    }
    
    private String getSQ_SearchVhclType(){
        return  " SELECT " +  
                " IFNULL(a.sTypeIDxx,'') sTypeIDxx  " +   
                " , IFNULL(b.sTypeDesc,'') sTypeDesc " +  
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_type b ON b.sTypeIDxx = a.sTypeIDxx " ;
    }
    
    private String getSQ_SearchVhclColor(){
        return  " SELECT " +  
                " IFNULL(a.sColorIDx,'') sColorIDx  " +   
                " , IFNULL(b.sColorDsc,'') sColorDsc " +  
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_color b ON b.sColorIDx = a.sColorIDx " ;
    }
    
    private String getSQ_SearchVhclTrnsMn(){
        return  " SELECT " +  
                " IFNULL(a.sTransMsn,'') sTransMsn  " +    
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +   
                " FROM vehicle_master a";
    }
    
    private String getSQ_SearchVhclYearMdl(){
        return  " SELECT " +  
                " IFNULL(a.nYearModl,'') nYearModl  " +    
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +
                " FROM vehicle_master a";
    }
    
    private String getSQ_SearchDealer(){
        return  " SELECT " +  
                " IFNULL(a.sClientID,'') sClientID  " + 
                ", IFNULL(a.sCompnyNm,'') sCompnyNm  " +     
                " FROM client_master a";
    }
    
    private String getSQ_Regsplace(){
        return  " SELECT " +
                " a.sTownName " +
                ", b.sProvName " +
                " FROM towncity a " +
                " LEFT JOIN province b ON b.sProvIDxx = a.sProvIDxx " ;
    }
    
    private String getSQ_Customer(){
        return "SELECT" +
                "  IFNULL(a.sClientID, '') as sClientID" + 
                ", IFNULL(a.sLastName, '') as sLastName" + 
                ", IFNULL(a.sFrstName, '') as sFrstName" +
                ", IFNULL(a.sMiddName, '') as sMiddName" + 
                ", IFNULL(a.sCompnyNm, '') as sCompnyNm" +
                ", IFNULL(a.sClientNo, '') as sClientNo" + 
                ", a.cClientTp" + 
                ", a.cRecdStat" + 
                ", IFNULL(CONCAT( IFNULL(CONCAT(b.sAddressx,', ') , ''), " +    
                "  IFNULL(CONCAT(d.sBrgyName,', '), ''),   " + 
                "  IFNULL(CONCAT(c.sTownName, ', '),''),   " + 
                "  IFNULL(e.sProvName,'') )	, '') AS sAddressx " + 
                " FROM client_master a" +
                " LEFT JOIN client_address b ON b.sClientID = a.sClientID AND b.cPrimaryx = '1' " + //AND c.cRecdStat = '1'  
                " LEFT JOIN TownCity c ON c.sTownIDxx = b.sTownIDxx " + //AND d.cRecdStat = '1'  
                " LEFT JOIN barangay d ON d.sBrgyIDxx = b.sBrgyIDxx AND d.sTownIDxx = b.sTownIDxx " + // AND e.cRecdStat = '1'   
                " LEFT JOIN Province e ON e.sProvIDxx = c.sProvIDxx  " ; //AND f.cRecdStat = '1' 

    }
    
    /**
     * Search for Vehicle Ownership.
     * @param fsValue The customer name
     * @param isOwner Identifier for owner or co-owner
     * 
    */
    public boolean searchCustomer (String fsValue, boolean isOwner, boolean isTransfer) throws SQLException{
        String lsSQL = getSQ_Customer() + " WHERE a.cRecdStat = '1' ";
        psMessage = "";
        //lsSQL = lsSQL + " WHERE a.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%") +
        //                " AND a.cRecdStat = '1'  "  ;
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL = lsSQL + " AND a.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%") ;
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                if(!checkCustomer(loRS.getString("sClientID"),isOwner,isTransfer)){
                    return false;
                }
                
                if (isOwner){
                    setMaster("sClientID", loRS.getString("sClientID"));
                    setMaster("sOwnerNam", loRS.getString("sCompnyNm"));
                    setMaster("sOwnerAdd", loRS.getString("sAddressx"));
                } else {
                    setMaster("sCoCltIDx", loRS.getString("sClientID"));
                    setMaster("sCoOwnerN", loRS.getString("sCompnyNm"));
                    setMaster("sCoOwnerA", loRS.getString("sAddressx"));
                }
                
            } else {
                psMessage = "No record found.";
                return false;
            }           
        } else {
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Customer ID»Customer Name", 
                                             "sClientID»sCompnyNm",
                                             "a.sClientID»a.sCompnyNm",
                                            1);
            
            if (loJSON != null){
                if(!checkCustomer((String) loJSON.get("sClientID"),isOwner,isTransfer)){
                    return false;
                }
                if (isOwner){
                    setMaster("sClientID", (String) loJSON.get("sClientID"));
                    setMaster("sOwnerNam", (String) loJSON.get("sCompnyNm"));
                    setMaster("sOwnerAdd", (String) loJSON.get("sAddressx"));
                } else {
                    setMaster("sCoCltIDx", (String) loJSON.get("sClientID"));
                    setMaster("sCoOwnerN", (String) loJSON.get("sCompnyNm"));
                    setMaster("sCoOwnerA", (String) loJSON.get("sAddressx"));
                }
                
            } else {
                psMessage = "No record found/selected.";
                return false;    
            }
        } 
        
        return true;
    }
    
    private boolean checkCustomer(String fsValue,boolean isOwner, boolean isTransfer){
        try {
            if (isOwner){
                if (((String) getMaster("sCoCltIDx")).equals(fsValue)){
                    psMessage = "Owner cannot be same with Co-Owner.";
                    return false;
                }
                
                if(isTransfer){
                    if (((String) getMaster("sClientID")).equals(fsValue)){
                        psMessage = "New Owner cannot be same with current Owner.";
                        return false;
                    }
                }
                
            } else {
                if (((String) getMaster("sClientID")).equals(fsValue)){
                    psMessage = "Co-Owner cannot be same with Owner.";
                    return false;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientVehicleInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    
        return true;
    }
    
    /**
     * For searching available vehicle when key is pressed.
     * @return {@code true} if a matching available vehicle is found, {@code false} otherwise.
    */
    public boolean searchAvailableVhcl() throws SQLException{
        String lsSQL = getSQ_Master();
        
        lsSQL = MiscUtil.addCondition(lsSQL, " a.cSoldStat = '1' AND (ISNULL(a.sClientID) OR  TRIM(a.sClientID) = '' )" );
        System.out.println(lsSQL);
        ResultSet loRS;
        loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , "%"
                                                    , "CS No»Vehicle Description»Plate No»Frame Number»Engine Number"
                                                    , "sCSNoxxxx»sDescript»sPlateNox»sFrameNox»sEngineNo"
                                                    , "sCSNoxxxx»sDescript»sPlateNox»sFrameNox»sEngineNo"
                                                    , 0);
        
        if (loJSON == null){
            psMessage = "No record found/selected.";
            return false;
        } else {
            if (OpenRecord((String) loJSON.get("sSerialID"))){
                if (poVehicle != null){
                    poOriginalVehicle = (CachedRowSet) poVehicle.createCopy();
                }
                pnEditMode = EditMode.UPDATE;
            }
        }
               
        return true;
    }
    /**
     * For searching vehicle make when key is pressed.
     * @param fsValue the search value for the vehicle make.
     * @return {@code true} if a matching vehicle make is found, {@code false} otherwise.
    */
    public boolean searchVehicleMake(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchVhclMake();
        String lsOrigVal = getMaster(23).toString();
        String lsNewVal = "";
        
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL = (MiscUtil.addCondition(lsSQL, " b.sMakeDesc LIKE " + SQLUtil.toSQL(fsValue + "%"))  +
                                                  " GROUP BY a.sMakeIDxx " );
            lsSQL += " LIMIT 1";
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sMakeIDxx");
                setMaster("sMakeIDxx", loRS.getString("sMakeIDxx"));
                setMaster("sMakeDesc", loRS.getString("sMakeDesc"));
            }
        } else {
            lsSQL = getSQ_SearchVhclMake() + " GROUP BY a.sMakeIDxx ";
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            //loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Make", "sMakeDesc");
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Make ID»Vehicle Make", 
                                             "sMakeIDxx»sMakeDesc",
                                             "a.sMakeIDxx»b.sMakeDesc",
                                            1);
            if (loJSON == null){
            } else {
                lsNewVal = (String) loJSON.get("sMakeIDxx");
                setMaster("sMakeIDxx", (String) loJSON.get("sMakeIDxx"));
                setMaster("sMakeDesc", (String) loJSON.get("sMakeDesc"));
            }
        }   
            
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("sModelIDx", "");
            setMaster("sModelDsc", "");
            setMaster("sTypeIDxx", "");
            setMaster("sTypeDesc", "");
            setMaster("sColorIDx", "");
            setMaster("sColorDsc", "");
            setMaster("sTransMsn", "");
            setMaster("nYearModl", "");
            setMaster("sFrameNox", "");
            setMaster("sEngineNo", "");
            
            if (!pbWithUI) {
                if (!loRS.next()){
                    psMessage = "No record found.";
                    setMaster("sMakeIDxx","");
                    return false;
                }
            } else {
                if (loJSON == null){
                    psMessage = "No record found/selected.";
                    setMaster("sMakeIDxx","");
                    return false;
                }
            }
            
        }     
        return true;
    }
    /**
     * For searching vehicle model when key is pressed.
     * @param fsValue the search value for the vehicle model.
     * @return {@code true} if a matching vehicle model is found, {@code false} otherwise.
    */
    public boolean searchVehicleModel(String fsValue) throws SQLException{
        String lsSQL = MiscUtil.addCondition( getSQ_SearchVhclModel(), " a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")));
        String lsOrigVal = getMaster(25).toString();
        String lsNewVal = "";

        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {  
            lsSQL = (MiscUtil.addCondition(lsSQL, " b.sModelDsc LIKE " + SQLUtil.toSQL(fsValue + "%") 
                                            )  +      " GROUP BY a.sModelIDx " );
            lsSQL += " LIMIT 1";
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sModelIDx");
                setMaster("sModelIDx", loRS.getString("sModelIDx"));
                setMaster("sModelDsc", loRS.getString("sModelDsc"));
            }
        } else {
            lsSQL = lsSQL  + " GROUP BY a.sModelIDx " ;
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            //loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Model", "sModelDsc");
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Model ID»Vehicle Model", 
                                             "sModelIDx»sModelDsc",
                                             "a.sModelIDx»b.sModelDsc",
                                            1);
            if (loJSON == null){
            } else {
                lsNewVal = (String) loJSON.get("sModelIDx");
                setMaster("sModelIDx", (String) loJSON.get("sModelIDx"));
                setMaster("sModelDsc", (String) loJSON.get("sModelDsc"));
            }
        } 
            
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("sTypeIDxx", "");
            setMaster("sTypeDesc", "");
            setMaster("sColorIDx", "");
            setMaster("sColorDsc", "");
            setMaster("sTransMsn", "");
            setMaster("nYearModl", "");
            setMaster("sFrameNox", "");
            setMaster("sEngineNo", "");
            
            if (!pbWithUI) {
                if (!loRS.next()){
                    psMessage = "No record found.";
                    setMaster("sModelIDx","");
                    return false;
                }
            } else {
                if (loJSON == null){
                    psMessage = "No record found/selected.";
                    setMaster("sModelIDx","");
                    return false;
                }
            }
            
        }       
        return true;
    }
    /**
     * For searching vehicle type when key is pressed.
     * @param fsValue the search value for the vehicle type.
     * @return {@code true} if a matching vehicle type is found, {@code false} otherwise.
    */
    public boolean searchVehicleType(String fsValue) throws SQLException{
//        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sTypeDesc LIKE " + SQLUtil.toSQL(fsValue + "%") +
//                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
//                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx"))
//                                        )  +      " GROUP BY a.sTypeIDxx " );

        String lsSQL = MiscUtil.addCondition( getSQ_SearchVhclType(), 
                                                " a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx"))
                                            );
        String lsOrigVal = getMaster(27).toString();
        String lsNewVal = "";
        
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL = (MiscUtil.addCondition(lsSQL, " b.sTypeDesc LIKE " + SQLUtil.toSQL(fsValue + "%") 
                                           )  +      " GROUP BY a.sTypeIDxx " );
            System.out.println(lsSQL);
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sTypeIDxx");
                setMaster("sTypeIDxx", loRS.getString("sTypeIDxx"));
                setMaster("sTypeDesc", loRS.getString("sTypeDesc"));
            }
        } else {
            lsSQL = lsSQL  +  " GROUP BY a.sTypeIDxx " ;
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            //loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Type", "sTypeDesc");
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Type ID»Vehicle Type", 
                                             "sTypeIDxx»sTypeDesc",
                                             "a.sTypeIDxx»b.sTypeDesc",
                                            1);
            if (loJSON == null){
            } else {
                lsNewVal = (String) loJSON.get("sTypeIDxx");
                setMaster("sTypeIDxx", (String) loJSON.get("sTypeIDxx"));
                setMaster("sTypeDesc", (String) loJSON.get("sTypeDesc"));
            }
        } 
        
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("sColorIDx", "");
            setMaster("sColorDsc", "");
            setMaster("sTransMsn", "");
            setMaster("nYearModl", "");
            
            if (!pbWithUI) {
                if (!loRS.next()){
                    psMessage = "No record found.";
                    setMaster("sTypeIDxx","");
                    return false;
                }
            } else {
                if (loJSON == null){
                    psMessage = "No record found/selected.";
                    setMaster("sTypeIDxx","");
                    return false;
                }
            }
        }     
        return true;
    }
    /**
     * For searching vehicle transmission when key is pressed.
     * @param fsValue the search value for the vehicle transmission.
     * @return {@code true} if a matching vehicle transmission is found, {@code false} otherwise.
    */
    public boolean searchVehicleTrnsMn(String fsValue) throws SQLException{
//        lsSQL = (MiscUtil.addCondition(lsSQL, " a.sTransMsn LIKE " + SQLUtil.toSQL(fsValue + "%") +
//                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
//                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
//                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) 
//                                        )  +      " GROUP BY a.sTransMsn " );
        String lsSQL = MiscUtil.addCondition( getSQ_SearchVhclTrnsMn(), " a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) 
                                            ) ;
        String lsOrigVal = getMaster(31).toString();
        String lsNewVal = "";
        
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL = (MiscUtil.addCondition(lsSQL, " a.sTransMsn LIKE " + SQLUtil.toSQL(fsValue + "%") 
                                            )  +      " GROUP BY a.sTransMsn " );
            lsSQL += " LIMIT 1";
            
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sTransMsn");
                setMaster("sTransMsn", loRS.getString("sTransMsn"));
            }
        } else {
            lsSQL = lsSQL +  " GROUP BY a.sTransMsn " ;
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            //loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Transmission", "sTransMsn");
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Vehicle Transmission", 
                                             "sTransMsn",
                                             "a.sTransMsn",
                                            0);
            if (loJSON == null){
            } else {
                lsNewVal = (String) loJSON.get("sTransMsn");
                setMaster("sTransMsn", (String) loJSON.get("sTransMsn"));
            }
        }  
        
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("sColorIDx", "");
            setMaster("sColorDsc", "");
            setMaster("nYearModl", "");
            
            if (!pbWithUI) {
                if (!loRS.next()){
                    psMessage = "No record found.";
                    setMaster("sTransMsn","");
                    return false;
                }
            } else {
                if (loJSON == null){
                    psMessage = "No record found/selected.";
                    setMaster("sTransMsn","");
                    return false;
                }
            }
            
        }      
        return true;
    }
    /**
     * For searching vehicle color when key is pressed.
     * @param fsValue the search value for the vehicle color.
     * @return {@code true} if a matching vehicle color is found, {@code false} otherwise.
    */
    public boolean searchVehicleColor(String fsValue) throws SQLException{
//        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sColorDsc LIKE " + SQLUtil.toSQL(fsValue + "%") +
//                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
//                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
//                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
//                                                  " AND a.sTransMsn = " + SQLUtil.toSQL((String) getMaster("sTransMsn"))
//                                        )  +      " GROUP BY a.sColorIDx " );
        String lsSQL = MiscUtil.addCondition( getSQ_SearchVhclColor(), " a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                  " AND a.sTransMsn = " + SQLUtil.toSQL((String) getMaster("sTransMsn"))
                                        );
        
        String lsOrigVal = getMaster(29).toString();
        String lsNewVal = "";
        
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL = (MiscUtil.addCondition(lsSQL, " b.sColorDsc LIKE " + SQLUtil.toSQL(fsValue + "%") 
                                          )  +      " GROUP BY a.sColorIDx " );
            lsSQL += " LIMIT 1";
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sColorIDx");
                setMaster("sColorIDx", loRS.getString("sColorIDx"));
                setMaster("sColorDsc", loRS.getString("sColorDsc"));
            }
        } else {
            lsSQL = lsSQL  +  " GROUP BY a.sColorIDx " ;
            System.out.println(lsSQL);
            
            loRS = poGRider.executeQuery(lsSQL);
            //loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Color", "sColorDsc");
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Color ID»Vehicle Color", 
                                             "sColorIDx»sColorDsc",
                                             "a.sColorIDx»b.sColorDsc",
                                            1);
            if (loJSON == null){
            } else {
                lsNewVal = (String) loJSON.get("sColorIDx");
                setMaster("sColorIDx", (String) loJSON.get("sColorIDx"));
                setMaster("sColorDsc", (String) loJSON.get("sColorDsc"));
            }
        }
        
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("nYearModl", "");
            
            if (!pbWithUI) {
                if (!loRS.next()){
                    psMessage = "No record found.";
                    setMaster("sColorIDx","");
                    return false;
                }
            } else {
                if (loJSON == null){
                    psMessage = "No record found/selected.";
                    setMaster("sColorIDx","");
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * For searching vehicle year model when key is pressed.
     * @param fsValue the search value for the vehicle year model.
     * @return {@code true} if a matching vehicle year model is found, {@code false} otherwise.
    */
    public boolean searchVehicleYearMdl(String fsValue) throws SQLException{
        String lsSQL = MiscUtil.addCondition( getSQ_SearchVhclYearMdl(), " a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                  " AND a.sColorIDx = " + SQLUtil.toSQL((String) getMaster("sColorIDx")) +
                                                  " AND a.sTransMsn = " + SQLUtil.toSQL((String) getMaster("sTransMsn"))
                                        ) ;
        
//        lsSQL = (MiscUtil.addCondition(lsSQL, " a.nYearModl LIKE " + SQLUtil.toSQL(fsValue + "%") +
//                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
//                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
//                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
//                                                  " AND a.sColorIDx = " + SQLUtil.toSQL((String) getMaster("sColorIDx")) +
//                                                  " AND a.sTransMsn = " + SQLUtil.toSQL((String) getMaster("sTransMsn"))
//                                        )  +      " GROUP BY a.nYearModl " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL = (MiscUtil.addCondition( lsSQL , " AND a.nYearModl LIKE " + SQLUtil.toSQL(fsValue + "%"))
                    )+  " GROUP BY a.nYearModl " ;
            lsSQL += " LIMIT 1";
            
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("nYearModl", loRS.getString("nYearModl"));
                setMaster("sVhclIDxx", loRS.getString("sVhclIDxx"));
                
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            lsSQL = lsSQL + " GROUP BY a.nYearModl " ;
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Year Model", "nYearModl");
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Vehicle Year Model", 
                                             "nYearModl",
                                             "a.nYearModl",
                                            0);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("nYearModl", (String) loJSON.get("nYearModl"));
                setMaster("sVhclIDxx", (String) loJSON.get("sVhclIDxx"));
            }
        }        
        return true;
    }
    
    /**
     * For searching dealership when key is pressed.
     * @param fsValue the search value for the dealership.
     * @return {@code true} if a matching dealership is found, {@code false} otherwise: set only for sDealerNm column.
    */
    public boolean searchDealer(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchDealer() + " WHERE a.cRecdStat = '1' ";
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sCompnyID", loRS.getString("sClientID")); //sCompnyID
                setMaster("sDealerNm", loRS.getString("sCompnyNm"));
            } else {
                setMaster("sCompnyID", "");
                setMaster("sDealerNm", fsValue);
                psMessage = "No record found.";
                //return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Dealership ID»Dealership", 
                                             "sClientID»sCompnyNm",
                                             "sClientID»sCompnyNm",
                                            1);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                setMaster("sCompnyID", "");
                setMaster("sDealerNm", fsValue);
                //return false;
            } else {
                setMaster("sCompnyID", (String) loJSON.get("sClientID")); //sCompnyID
                setMaster("sDealerNm", (String) loJSON.get("sCompnyNm"));
            }
        }        
        return true;
    }
    
    /**
     * For searching registered place when key is pressed.
     * @param fsValue the search value for the dealership.
     * @return {@code true} if a matching registered place is found, {@code false} otherwise: set only for sPlaceReg column.
    */
    public boolean searchRegsplace(String fsValue) throws SQLException{
        String lsSQL = getSQ_Regsplace();
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTownName LIKE " + SQLUtil.toSQL(fsValue + "%")
                                               + " OR b.sProvName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sPlaceReg", (loRS.getString("sTownName") + " " + loRS.getString("sProvName")));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Place of Registration", "sTownName");
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Town»Province", 
                                             "sTownName»sProvName",
                                             "a.sTownName»b.sProvName",
                                            0);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {    
                setMaster("sPlaceReg", ((String) loJSON.get("sTownName") + " " + (String) loJSON.get("sProvName")));
            }
        }
        
        return true;
    }
    
    private String getSQ_SearchVhclDsc(){
        return  "SELECT" +  
                " IFNULL(a.sMakeIDxx,'') sMakeIDxx " +   
                " , IFNULL(b.sMakeDesc,'') sMakeDesc  " + 
                " , IFNULL(a.sModelIDx,'') sModelIDx  " + 
                " , IFNULL(c.sModelDsc,'') sModelDsc  " + 
                " , IFNULL(a.sTypeIDxx,'') sTypeIDxx  " +
                " , IFNULL(d.sTypeDesc,'') sTypeDesc  " +  
                " , IFNULL(a.sColorIDx,'') sColorIDx  " + 
                " , IFNULL(e.sColorDsc,'') sColorDsc  " + 
                " , IFNULL(a.sTransMsn,'') sTransMsn  " + 
                " , IFNULL(a.nYearModl,'') nYearModl  " + 
                "   FROM vehicle_master a " +
                "   LEFT JOIN vehicle_make b ON b.sMakeIDxx = a.sMakeIDxx " +
                "   LEFT JOIN vehicle_model c ON c.sModelIDx = a.sModelIDx " +
                "   LEFT JOIN vehicle_type d ON d.sTypeIDxx = a.sTypeIDxx " +
                "   LEFT JOIN vehicle_color e ON e.sColorIDx = a.sColorIDx " ;
    }
    
    //for searching vehicle make when f3 is pressed
    /*public boolean searchVehicleDesc(String fsMake, String fsModel, String fsType, String fsColor, String fsTrnsMn, String fsYearMdl) throws SQLException{
        String lsSQL = getSQ_SearchVhclDsc();
        
        if(!fsMake.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeDesc LIKE " + SQLUtil.toSQL(fsMake + "%"));
        }
        
        if(!fsModel.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx LIKE " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelDsc LIKE " + SQLUtil.toSQL(fsModel + "%")
                                        );
        }
        
        if(!fsType.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                     "AND sTypeDesc LIKE " + SQLUtil.toSQL(fsType + "%")
                                        );
        }
        
        if(!fsColor.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                     "AND sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                     "AND sColorDsc LIKE " + SQLUtil.toSQL(fsColor + "%")
                                            );
        }
        
        if(!fsTrnsMn.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                     "AND sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                     "AND sColorIDx = " + SQLUtil.toSQL((String) getMaster("sColorIDx")) +
                                                     "AND sTransMsn LIKE " + SQLUtil.toSQL(fsTrnsMn + "%")
                                        );
        }
        
        if(!fsYearMdl.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                     "AND sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                     "AND sColorIDx = " + SQLUtil.toSQL((String) getMaster("sColorIDx")) +
                                                     "AND sTransMsn = " + SQLUtil.toSQL((String) getMaster("sTransMsn")) +
                                                     "AND nYearModl LIKE " + SQLUtil.toSQL(fsYearMdl + "%" ) 
                                        );
        }
             
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sMakeIDxx", loRS.getString("sMakeIDxx"));
                setMaster("sMakeDesc", loRS.getString("sMakeDesc"));
                setMaster("sModelIDx", loRS.getString("sModelIDx"));
                setMaster("sModelDsc", loRS.getString("sModelDsc"));
                setMaster("sTypeIDxx", loRS.getString("sTypeIDxx"));
                setMaster("sTypeDesc", loRS.getString("sTypeDesc"));
                setMaster("sColorIDx", loRS.getString("sColorIDx"));
                setMaster("sColorDsc", loRS.getString("sColorDsc"));
                setMaster("sTransMsn", loRS.getString("sTransMsn"));
                setMaster("nYearModl", loRS.getString("nYearModl"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Make", "sMakeDesc");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sMakeIDxx", (String) loJSON.get("sMakeIDxx"));
                setMaster("sMakeDesc", (String) loJSON.get("sMakeDesc"));
                setMaster("sModelIDx", (String) loJSON.get("sModelIDx"));
                setMaster("sModelDsc", (String) loJSON.get("sModelDsc"));
                setMaster("sTypeIDxx", (String) loJSON.get("sTypeIDxx"));
                setMaster("sTypeDesc", (String) loJSON.get("sTypeDesc"));
                setMaster("sColorIDx", (String) loJSON.get("sColorIDx"));
                setMaster("sColorDsc", (String) loJSON.get("sColorDsc"));
                setMaster("sTransMsn", (String) loJSON.get("sTransMsn"));
                setMaster("nYearModl", (String) loJSON.get("nYearModl"));
            }
        }        
        return true;
    }
    */
    private String getSQ_MakeFrame(){
        return  "SELECT " +
                " IFNULL(sFrmePtrn,'') sFrmePtrn " +
                " FROM vehicle_make_frame_pattern ";
                
    }
    
    private String getSQ_ModelFrame(){
        return  "SELECT " +
                " IFNULL(sFrmePtrn,'') sFrmePtrn " +
                " FROM vehicle_model_frame_pattern ";
    }
    
    private String getSQ_ModelEngine(){
        return  "SELECT " +
                " IFNULL(sEngnPtrn,'') sEngnPtrn " +
                " FROM vehicle_model_engine_pattern ";
    }
    
    private String getSQ_StandardSets(){
        return  "SELECT " +
                " IFNULL(sValuexxx,'') sValuexxx " +
                " FROM xxxstandard_sets ";
    }
    
    private String getSQ_VhchlRegs(){
        return  "SELECT " +
                " IFNULL(sSerialID,'') sSerialID " +
                " FROM vehicle_serial_registration ";
    }
    
    //Validate Engine Frame per Make based on standard sets
    public boolean vhclExistRegs(){
        try {
            String lsSQL = getSQ_VhchlRegs();
            ResultSet loRS;
            lsSQL = MiscUtil.addCondition(lsSQL, " sSerialID = " + SQLUtil.toSQL(poVehicle.getString("sSerialID")) );
            loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) == 0){
                MiscUtil.close(loRS);
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientVehicleInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    //Validate Engine Frame per Make based on standard sets
    public boolean valEngFrameMake(){
        try {
            String lsSQL = getSQ_StandardSets();
            ResultSet loRS;
            lsSQL = MiscUtil.addCondition(lsSQL, " sValuexxx  = " + SQLUtil.toSQL(poVehicle.getString("sMakeDesc")) +
                                                     " AND sDescript = 'engineframe_make'");
            loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) == 0){
                MiscUtil.close(loRS);
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientVehicleInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    //Validate Engine Frame per Model Body Type
    public boolean valEngFrameModel(){
        try {
            String lsSQL = getSQ_SearchVhclModel();
            ResultSet loRS;
            lsSQL = MiscUtil.addCondition(lsSQL, " b.sModelIDx  = " + SQLUtil.toSQL(poVehicle.getString("sModelIDx")) +
                                                     " AND b.sBodyType = 'MOTORCYCLE'");
            loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0){
                MiscUtil.close(loRS);
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientVehicleInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    //Validate Make Frame Number
    public boolean isMakeFrameOK(String fsValue) throws SQLException{
        //Validate Vehicle Make if it is exist in engineframe_make xxxstandrard_sets
        if(valEngFrameMake()){
            return true;
        }
        //Validate Vehicle Model it is motorcycle do not validate the vehicles engine frame value
        if(valEngFrameModel()){
            return true;
        }
        
        if (fsValue.isEmpty()){
            psMessage = "Frame Number is not set.";
            return false;
        }
        
        if (fsValue.length() < 3){
            psMessage = "Frame Number must not be less than 3 characters.";
            return false;
        }
        
        String lsSQL = getSQ_MakeFrame();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL," sFrmePtrn = "  + SQLUtil.toSQL( fsValue.substring(0, 3))
                                            +   "AND sMakeIDxx = "  + SQLUtil.toSQL( poVehicle.getString("sMakeIDxx") )); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) == 0){
            psMessage = "The first 3 characters of the Frame Number do not match the Frame Pattern. Please enter a new Pattern for the Make Frame.";
            MiscUtil.close(loRS);        
            return false;
        } 
        
        return true;
    }
    
    //Validate Model Frame Number
    public boolean isModelFrameOK(String fsValue) throws SQLException{
        
        //Validate Vehicle Make if it is exist in engineframe_make xxxstandrard_sets
        if(valEngFrameMake()){
            return true;
        }
        //Validate Vehicle Model it is motorcycle do not validate the vehicles engine frame value
        if(valEngFrameModel()){
            return true;
        }
        
        if (fsValue.isEmpty()){
            psMessage = "Frame Number is not set.";
            return false;
        }
        
        if (fsValue.length() < 5){
            psMessage = "Frame Number must not be less than 5 characters.";
            return false;
        }
        
        String lsSQL = getSQ_ModelFrame();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL," sFrmePtrn = "  + SQLUtil.toSQL( fsValue.substring(3, 5))
                                            +   " AND nFrmeLenx = "  + SQLUtil.toSQL( fsValue.length() )
                                            +   " AND sModelIDx = "  + SQLUtil.toSQL( poVehicle.getString("sModelIDx") )); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) == 0){
            psMessage = "The first 4 and 5 characters of the Frame Number do not match the Frame Pattern. Please enter a new Pattern for the Model Frame.";
            MiscUtil.close(loRS);        
            return false;
        }     
        
        return true;
    }
    
    //Validate Engine Number
    public boolean isModelEngineOK(String fsValue) throws SQLException{
        //Validate Vehicle Make if it is exist in engineframe_make xxxstandrard_sets
        if(valEngFrameMake()){
            return true;
        }
        //Validate Vehicle Model it is motorcycle do not validate the vehicles engine frame value
        if(valEngFrameModel()){
            return true;
        }
        
        if (fsValue.isEmpty()){
            psMessage = "Engine Number is not set.";
            return false;
        }
        
        if (fsValue.length() < 3){
            psMessage = "Engine Number must not be less than 3 characters.";
            return false;
        }
        
        String lsSQL = getSQ_ModelEngine();
        ResultSet loRS;
        
        lsSQL = MiscUtil.addCondition(lsSQL," sEngnPtrn = "  + SQLUtil.toSQL( fsValue.substring(0, 3))
                                            +   " AND nEngnLenx = "  + SQLUtil.toSQL( fsValue.length() )
                                            +   " AND sModelIDx = "  + SQLUtil.toSQL( poVehicle.getString("sModelIDx") )); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) == 0){
            psMessage = "The first 3 characters of the Engine Number do not match the Frame Pattern. Please enter a new Pattern for the Model Engine.";
            MiscUtil.close(loRS);        
            return false;
        } 
        return true;
    }
    
    private boolean isEntryOK() throws SQLException{
        poVehicle.first();

        if (poVehicle.getString("sVhclIDxx").isEmpty()){
            psMessage = "Vehicle is not set.";
            return false;
        }
        
        if(!pbisVhclSales){
            if (poVehicle.getString("sClientID").isEmpty()){
                psMessage = "Please select owner.";
                return false;
            }
        }
        
        if (poVehicle.getString("sCSNoxxxx").isEmpty() && poVehicle.getString("sPlateNox").isEmpty()){
            psMessage = "Plate / CS No. is not set.";
            return false;
        }
        
        //Validate if CS / Plate Number is exist.
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        String sPlateNo, sCsNo;
        
        if (poVehicle.getString("sCSNoxxxx").isEmpty()) {
            sCsNo = poVehicle.getString("sPlateNox");
        } else {
            sCsNo = poVehicle.getString("sCSNoxxxx");
        }
        if (poVehicle.getString("sPlateNox").isEmpty()) {
            sPlateNo = poVehicle.getString("sCSNoxxxx");
        } else {
            sPlateNo = poVehicle.getString("sPlateNox");
        }
        
        lsSQL = MiscUtil.addCondition(lsSQL," ( ( a.sCSNoxxxx = " + SQLUtil.toSQL(sCsNo) + 
                                                " OR b.sPlateNox = " + SQLUtil.toSQL(sCsNo) + " ) " +
                                                " OR ( a.sCSNoxxxx = " + SQLUtil.toSQL(sPlateNo) + 
                                                " OR b.sPlateNox = " + SQLUtil.toSQL(sPlateNo) + " ) )" +
                                                " AND a.sSerialID <> " + SQLUtil.toSQL(poVehicle.getString("sSerialID"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "CS / Plate Number already exist.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        //Validate if Engine / Frame Number
        lsSQL = getSQ_Master();
        String sFrameNo, sEngineNo;
        
        if (!poVehicle.getString("sFrameNox").isEmpty() || !poVehicle.getString("sEngineNo").isEmpty()) {
            if (poVehicle.getString("sFrameNox").isEmpty()) {
                sFrameNo = poVehicle.getString("sEngineNo");
            } else {
                sFrameNo = poVehicle.getString("sFrameNox");
            }
            if (poVehicle.getString("sEngineNo").isEmpty()) {
                sEngineNo = poVehicle.getString("sFrameNox");
            } else {
                sEngineNo = poVehicle.getString("sEngineNo");
            }
            lsSQL = MiscUtil.addCondition(lsSQL," ( ( a.sFrameNox  = " + SQLUtil.toSQL(sFrameNo) + 
                                                    " OR a.sEngineNo  = " + SQLUtil.toSQL(sFrameNo) + " ) " +
                                                    " OR ( a.sFrameNox = " + SQLUtil.toSQL(sEngineNo) + 
                                                    " OR a.sEngineNo = " + SQLUtil.toSQL(sEngineNo) + " ) )" +
                                                    " AND a.sSerialID <> " + SQLUtil.toSQL(poVehicle.getString("sSerialID"))); 
            loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0){
                psMessage = "Engine / Frame Number already exist.";
                MiscUtil.close(loRS);        
                return false;
            }
        }
        
        //Validate Engine Frame Pattern
        if (!isMakeFrameOK(poVehicle.getString("sFrameNox"))){
            return false;
        }
        if (!isModelFrameOK(poVehicle.getString("sFrameNox"))){
            return false;
        }
        if (!isModelEngineOK(poVehicle.getString("sEngineNo"))){
            return false;
        }
        
//        //Validate Vehicle Make if it is exist in engineframe_make xxxstandrard_sets
//        if(valEngFrameMake()){
//            return true;
//        }
//        //Validate Vehicle Model is motorcycle do not validate the vehicles engine frame value
//        if(valEngFrameModel()){
//            return true;
//        }
//            
//        if (poVehicle.getString("sFrameNox").isEmpty()){
//            psMessage = "Frame Number is not set.";
//            return false;
//        }
//
//        if (poVehicle.getString("sEngineNo").isEmpty()){
//            psMessage = "Engine Number is not set.";
//            return false;
//        }
        
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
