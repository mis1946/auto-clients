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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
 * @author jahn 05-18-2023
 */
public class Activity {

    //TODO ADD ACCESS ON BUTTONS AND FORM
    private final String MASTER_TABLE = "activity_master";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autapp_json/" + TabsStateManager.getJsonFileName("Activity");
    
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
    private Integer pnDeletedVhclRow[];
    private Integer pnDeletedTownRow[];
    private Integer pnDeletedEmpRow[];

    public CachedRowSet poMaster;
    public CachedRowSet poDepartment;
    public CachedRowSet poActMember;
    public CachedRowSet poEmployees;
    public CachedRowSet poVehicle;
    public CachedRowSet poActVehicle;
    public CachedRowSet poActVehicleOrig;
    public CachedRowSet poTown;
    public CachedRowSet poActTown;
    public CachedRowSet poActTownOrig;

    List<Integer> deletedRows = new ArrayList<>();

    public Activity(GRider foGRider, String fsBranchCd, boolean fbWithParent) {

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
        JSONArray laActMember = new JSONArray();
        JSONArray laActVehicle = new JSONArray();
        JSONArray laActTown = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;
        try {
            loJSON = new JSONObject();
            String lsValue =  CommonUtils.RS2JSON(poMaster).toJSONString();
            laMaster = (JSONArray) loParser.parse(lsValue);
            loMaster = (JSONObject) laMaster.get(0);
            loJSON.put("master", loMaster);
            
            if(poActMember != null){
                lsValue = CommonUtils.RS2JSON(poActMember).toJSONString();
                laActMember = (JSONArray) loParser.parse(lsValue);
                loJSON.put("member", laActMember);
            }
            
            if(poActVehicle != null){
                lsValue = CommonUtils.RS2JSON(poActVehicle).toJSONString();
                laActVehicle = (JSONArray) loParser.parse(lsValue);
                loJSON.put("vehicle", laActVehicle);
            }
            
            if(poActTown != null){
                lsValue = CommonUtils.RS2JSON(poActTown).toJSONString();
                laActTown = (JSONArray) loParser.parse(lsValue);
                loJSON.put("town", laActTown);
            }
            
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
            Logger.getLogger(Activity.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }
    
    private void saveState(String fsValue){
        if(pnEditMode == EditMode.UNKNOWN){
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
                    
                    int row = 1;
                    int ctr = 1;
                    
                    // Extract the "member" array from the JSON object
                    JSONArray memberArray = (JSONArray) jsonObject.get("member");
                    if(memberArray != null) {
                        if(memberArray.size()>0){
                            while (memberArray.size() > getActMemberCount()) {
                                String lsSQL;
                                ResultSet loRS;
                                RowSetFactory factory;
                                if (poActMember == null) {
                                    lsSQL = MiscUtil.addCondition(getSQ_ActMember(), "0=1");
                                    loRS = poGRider.executeQuery(lsSQL);
                                    factory = RowSetProvider.newFactory();
                                    poActMember = factory.createCachedRowSet();
                                    poActMember.populate(loRS);
                                    MiscUtil.close(loRS);
                                }
                                poActMember.last();
                                poActMember.moveToInsertRow();
                                MiscUtil.initRowSet(poActMember);
                                poActMember.insertRow();
                                poActMember.moveToCurrentRow();
                            }
                            
                            // Extract index and value from each object in the "member" array
                            for (Object item : memberArray) { 
                                poActMember.beforeFirst();
                                while (poActMember.next()){
                                    if(ctr == row){
                                        JSONObject priority = (JSONObject) item;
                                        for (Object key : priority.keySet()) {
                                            Object value = priority.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(poActMember.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    poActMember.updateObject(Integer.valueOf(key.toString()), value );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poActMember.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poActMember.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poActMember.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    poActMember.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        poActMember.updateRow();
                                    }
                                    row++;
                                }
                                row = 1;
                                ctr++;
                            }
                        }
                    }
                    
                    ctr = 1;
                    row = 1;
                    // Extract the "vehicle" array from the JSON object
                    JSONArray vehicleArray = (JSONArray) jsonObject.get("vehicle");
                    if(vehicleArray != null) {
                        if(vehicleArray.size()>0){
                            while (vehicleArray.size() > getActVehicleCount()) {
                                if (addActVehicle("", "","")){
                                } else {
                                    psMessage = "Error in Adding Activity Vehicle";
                                    return false;
                                }
                            }
                            // Extract index and value from each object in the "vehicle" array
                            for (Object item : vehicleArray) {
                                poActVehicle.beforeFirst(); 
                                while (poActVehicle.next()){  
                                    if(ctr == row){
                                        JSONObject parts = (JSONObject) item;
                                        for (Object key : parts.keySet()) {
                                            Object value = parts.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(poActVehicle.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    poActVehicle.updateObject(Integer.valueOf(key.toString()), tempValue );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poActVehicle.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poActVehicle.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poActVehicle.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    poActVehicle.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        poActVehicle.updateRow();
                                    }
                                    row++;
                                }
                                row = 1;
                                ctr++;
                            }
                        }
                    }
                    
                    row = 1;
                    ctr = 1;
                    
                    // Extract the "town" array from the JSON object
                    JSONArray townArray = (JSONArray) jsonObject.get("town");
                    if(townArray != null) {
                        if(townArray.size()>0){
                            while (townArray.size() > getActTownCount()) {
                                if (addActTown("", "")){
                                } else {
                                    psMessage = "Error in Adding Activity Town";
                                    return false;
                                }
                            }
                            
                            // Extract index and value from each object in the "town" array
                            for (Object item : townArray) { 
                                poActTown.beforeFirst();
                                while (poActTown.next()){
                                    if(ctr == row){
                                        JSONObject priority = (JSONObject) item;
                                        for (Object key : priority.keySet()) {
                                            Object value = priority.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(poActTown.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    poActTown.updateObject(Integer.valueOf(key.toString()), value );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poActTown.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poActTown.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poActTown.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    poActTown.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        poActTown.updateRow();
                                    }
                                    row++;
                                }
                                row = 1;
                                ctr++;
                            }
                        }
                    }
                }
            } else {
                psMessage = "";
                return false;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(Activity.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
   
    //TODO add setMaster for inquiry
    public void setMaster(int fnIndex, Object foValue) throws SQLException {
        poMaster.first();
        switch (fnIndex) {
            case 1:        //sActvtyID
            case 2:        //sActTitle
            case 3:        //sActDescx
            case 4:        //sActTypID
            case 5:        //sActSrcex
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
            case 29:        //sEventTyp
            case 30:        //sActNoxxx
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();

                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                }
                break;
            case 10:        //nPropBdgt
            case 11:        //nRcvdBdgt
            case 12:        //nTrgtClnt
            case 17:        //cTranStat
                if (foValue instanceof Integer) {
                    poMaster.updateInt(fnIndex, (int) foValue);
                } else {
                    poMaster.updateInt(fnIndex, 0);
                }

                poMaster.updateRow();
                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                }
                break;
            case 6:        //dDateFrom
            case 7:        //dDateThru
            case 19:        //dEntryDte
            case 21:        //dApproved
            case 23:        //dModified
                if (foValue instanceof Date) {
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                }
                break;
        }
        saveState(toJSONString());
    }

    //Activity Master setter
    public void setMaster(String fsIndex, Object foValue) throws SQLException {
        setMaster(MiscUtil.getColumnIndex(poMaster, fsIndex), foValue);
    }

    //Activity Master getter
    public Object getMaster(String fsIndex) throws SQLException {
        return getMaster(MiscUtil.getColumnIndex(poMaster, fsIndex));
    }

    //Activity Master getter
    public Object getMaster(int fnIndex) throws SQLException {
        poMaster.first();
        return poMaster.getObject(fnIndex);
    }

    public Object getDetail(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }

        poMaster.absolute(fnRow);
        return poMaster.getObject(fnIndex);
    }

    public Object getDetail(int fnRow, String fsIndex) throws SQLException {
        return getDetail(fnRow, MiscUtil.getColumnIndex(poMaster, fsIndex));
    }

    public int getItemCount() throws SQLException {
//        poMaster.last();
//        return poMaster.getRow();
        if (poMaster != null) {
            poMaster.last();
            return poMaster.getRow();
        } else {
            return 0;
        }
    }

    /**
     *
     * Initializes the master data for adding a new entry.
     *
     * @return {@code true} if the master data is successfully initialized,
     * {@code false} otherwise
     */
    public boolean NewRecord() {
        if (poGRider == null) {
            psMessage = "Application driver is not set.";
            return false;
        }

        if (psBranchCd.isEmpty()) {
            psBranchCd = poGRider.getBranchCode();
        }
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

        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        pnEditMode = EditMode.ADDNEW;
        return true;
    }

    public boolean UpdateRecord() {
        if (poActVehicle != null) {
            try {
                poActVehicleOrig = (CachedRowSet) poActVehicle.createCopy();
                poActTownOrig = (CachedRowSet) poActTown.createCopy();
            } catch (SQLException ex) {
                Logger.getLogger(Activity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        pnEditMode = EditMode.UPDATE;
        return true;
    }
    
    /**
    * Saves a record to the database.
    *
    * This method is responsible for adding a new record or updating an existing one based on the edit mode. It performs data validation and handles database transactions.
    *
    * @return True if the record is successfully saved, otherwise false.
    */
    public boolean SaveRecord() {
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            psMessage = "Invalid update mode detected.";
            return false;
        }

        try {
            if (!isEntryOK()) {
                return false;
            }

            String lsSQL = "";
            int lnCtr;

            if (pnEditMode == EditMode.ADDNEW) { //add
                if (!pbWithParent) {
                    poGRider.beginTrans();
                }
                //-------------------SAVE ACTIIVTY MASTER------------------------
                String lsTransNox = MiscUtil.getNextCode(MASTER_TABLE, "sActvtyID", true, poGRider.getConnection(), psBranchCd);
                poMaster.updateObject("sActvtyID", lsTransNox);
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                
                String lsActNox = MiscUtil.getNextCode(MASTER_TABLE, "sActNoxxx", false, poGRider.getConnection(), psBranchCd + "ACT");
                poMaster.updateObject("sActNoxxx", lsActNox);
                poMaster.updateRow();

                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sDeptName»sCompnyNm»sProvName»sBranchNm»sActTypDs»sEventTyp");

                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) {
                        poGRider.rollbackTrans();
                    }
                    return false;
                }

                //-------------------SAVE ACTIVITY MEMBER----------------------
                if (getActMemberCount() > 0) {
                    lnCtr = 1;
                    poActMember.beforeFirst();
                    while (poActMember.next()) {
                        //while (lnCtr < getActMemberCount()){
                        poActMember.updateObject("sTransNox", lsTransNox);
                        poActMember.updateObject("nEntryNox", lnCtr);
                        poActMember.updateObject("sEntryByx", poGRider.getUserID());
                        poActMember.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                        poActMember.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poActMember, "activity_member", "sCompnyNm»sDeptName");
                        //TODO what is substring(0,4)
                        if (poGRider.executeQuery(lsSQL, "activity_member", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
                            if (!pbWithParent) {
                                poGRider.rollbackTrans();
                            }
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }

                        lnCtr++;
                    }
                }
                //-------------------SAVE ACTIVITY Vehicle----------------------
                if (getActVehicleCount() > 0) {
                    lnCtr = 1;
                    System.out.println(getActVehicleCount());
                    poActVehicle.beforeFirst();
                    while (poActVehicle.next()) {
                        poActVehicle.updateObject("sTransNox", lsTransNox);
                        poActVehicle.updateObject("nEntryNox", lnCtr);
                        poActVehicle.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poActVehicle, "activity_vehicle", "sDescript»sCSNoxxxx");
                        //TODO what is substring(0,4)
                        if (poGRider.executeQuery(lsSQL, "activity_vehicle", psBranchCd, "") <= 0) {
                            if (!pbWithParent) {
                                poGRider.rollbackTrans();
                            }
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }

                        lnCtr++;
                    }
                }
                //-------------------SAVE ACTIVITY Town----------------------

                if (getActTownCount() > 0) {
                    lnCtr = 1;
                    System.out.println(getActTownCount());
                    poActTown.beforeFirst();
                    while (poActTown.next()) {
                        poActTown.updateObject("sTransNox", lsTransNox);
                        poActTown.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poActTown, "activity_town", "sTownName");
                        //TODO what is substring(0,4)
                        if (poGRider.executeQuery(lsSQL, "activity_town", psBranchCd, "") <= 0) {
                            if (!pbWithParent) {
                                poGRider.rollbackTrans();
                            }
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }
                }
            } else { //update
                if (!pbWithParent) {
                    poGRider.beginTrans();
                }

                //set transaction number on records
                String lsTransNox = (String) getMaster("sActvtyID");

                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();

                lsSQL = MiscUtil.rowset2SQL(poMaster,
                        MASTER_TABLE,
                        "sDeptName»sCompnyNm»sProvName»sBranchNm»sActTypDs»sEventTyp",
                        "sActvtyID = " + SQLUtil.toSQL(lsTransNox));
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) {
                        poGRider.rollbackTrans();
                    }
                    return false;
                }
                //----------------------Activity member update-------------------
                if (getActMemberCount() > 0) {
                    lnCtr = 1;
                    poActMember.beforeFirst();
                    while (poActMember.next()) {
                        String lsfTransNox = (String) getActMember(lnCtr, "sTransNox");// check if user added new act member to insert
                        if (lsfTransNox.equals("") || lsfTransNox.isEmpty()) {
                            poActMember.updateObject("sTransNox", lsTransNox);
                            poActMember.updateObject("sEntryByx", poGRider.getUserID());
                            poActMember.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                            poActMember.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poActMember, "activity_member", "sCompnyNm»sDeptName");
                            //TODO what is substring(0,4)
                            if (poGRider.executeQuery(lsSQL, "activity_member", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
                                if (!pbWithParent) {
                                    poGRider.rollbackTrans();
                                }
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }
                        } else {
                            lsSQL = MiscUtil.rowset2SQL(poActMember,
                                    "activity_member",
                                    "sCompnyNm»sDeptName",
                                    "sTransNox = " + SQLUtil.toSQL(lsTransNox)
                                    + " AND sEmployID = " + SQLUtil.toSQL(poActMember.getString("sEmployID")));

                            if (!lsSQL.isEmpty()) {
                                if (poGRider.executeQuery(lsSQL, "activity_member", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
                                    if (!pbWithParent) {
                                        poGRider.rollbackTrans();
                                    }
                                    psMessage = poGRider.getMessage() + ";" + poGRider.getErrMsg();
                                    return false;
                                }
                            }
                        }
                        lnCtr++;
                    }
                }
                //----------------------Activity Vehicle update-_------------------
                if (pnDeletedVhclRow != null && pnDeletedVhclRow.length != 0) {
                    Arrays.sort(pnDeletedVhclRow, Collections.reverseOrder());
                    poActVehicleOrig.beforeFirst();
                    for (int rowNum : pnDeletedVhclRow) {
                        poActVehicleOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM activity_vehicle WHERE"
                                + " sTransNox = " + SQLUtil.toSQL(poActVehicleOrig.getString("sTransNox"))
                                + " AND nEntryNox = " + SQLUtil.toSQL(poActVehicleOrig.getInt("nEntryNox"))
                                + " AND sSerialID = " + SQLUtil.toSQL(poActVehicleOrig.getString("sSerialID"));

                        if (poGRider.executeQuery(lsSQL, "activity_vehicle", psBranchCd, "") <= 0) {
                            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                if (getActVehicleCount() > 0) {
                    lnCtr = 1;
                    poActVehicle.beforeFirst();
                    while (poActVehicle.next()) {
                    //while (lnCtr <= getActVehicleCount()) {
                        if (!CompareRows.isRowEqual(poActVehicle, poActVehicleOrig, lnCtr)) {
                            String lsfTransNox = (String) getActVehicle(lnCtr, "sTransNox");// check if user added new VEHICLE to insert
                            if (lsfTransNox.equals("") || lsfTransNox.isEmpty()) {
                                poActVehicle.updateObject("nEntryNox", lnCtr);
                                poActVehicle.updateObject("sTransNox", lsTransNox);
                                poActVehicle.updateRow();

                                lsSQL = MiscUtil.rowset2SQL(poActVehicle, "activity_vehicle", "sDescript»sCSNoxxxx");
                                //TODO what is substring(0,4)
                                if (poGRider.executeQuery(lsSQL, "activity_vehicle", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
                                    if (!pbWithParent) {
                                        poGRider.rollbackTrans();
                                    }
                                    psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                    return false;
                                }
                            } else {
                                poActVehicle.updateObject("nEntryNox", lnCtr);
                                poActVehicle.updateRow();

                                lsSQL = MiscUtil.rowset2SQL(poActVehicle,
                                        "activity_vehicle",
                                        "sDescript»sCSNoxxxx",
                                        "sTransNox = " + SQLUtil.toSQL(lsTransNox)
                                        + " AND sSerialID = " + SQLUtil.toSQL(poActVehicle.getString("sSerialID")));

                                if (!lsSQL.isEmpty()) {
                                    if (poGRider.executeQuery(lsSQL, "activity_vehicle", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
                                        if (!pbWithParent) {
                                            poGRider.rollbackTrans();
                                        }
                                        psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                        return false;
                                    }
                                }
                            }
                        }
                        lnCtr++;
                    }

//                    poActVehicleOrig.beforeFirst();
//                    int rowNum = 1;
//
//                    while (poActVehicleOrig.next()) {
//                        boolean rowExistsInUpdatedRowSet = CompareRows.isRowEqual(poActVehicleOrig, poActVehicle, rowNum);
//                        if (!rowExistsInUpdatedRowSet) {
//                            lsSQL = "DELETE FROM activity_vehicle WHERE" +
//                                " sTransNox = " + SQLUtil.toSQL(poActVehicleOrig.getString("sTransNox")) +
//                                " AND nEntryNox = " + SQLUtil.toSQL(poActVehicleOrig.getInt("nEntryNox")) +
//                                " AND sSerialID = " + SQLUtil.toSQL(poActVehicleOrig.getString("sSerialID"));
//
//                            if (poGRider.executeQuery(lsSQL, "activity_vehicle", psBranchCd, "") <= 0){
//                                psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
//                                return false;
//                            }
//                        }
//                        rowNum++;
//                    }
                }

                //----------------------Activity Town update--------------------
                
                if (deletedRows != null && !deletedRows.isEmpty()) {
                    pnDeletedTownRow = deletedRows.toArray(new Integer[deletedRows.size()]);
                }
                
                if (pnDeletedTownRow != null && pnDeletedTownRow.length != 0) {
                    
                    Arrays.sort(pnDeletedTownRow, Collections.reverseOrder());
                    poActTownOrig.beforeFirst();

                    for (Integer rowNum : pnDeletedTownRow) {
                        poActTownOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM activity_town WHERE"
                                + " sTransNox = " + SQLUtil.toSQL(poActTownOrig.getString("sTransNox"))
                                + " AND sTownIDxx = " + SQLUtil.toSQL(poActTownOrig.getString("sTownIDxx"));
                        if (!lsSQL.isEmpty()) {
                            if (poGRider.executeQuery(lsSQL, "activity_town", psBranchCd, "") <= 0) {
                                psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                                return false;
                            }
                        }
                    }
                }

                if (getActTownCount() > 0) {

                    lnCtr = 1;
                    poActTown.beforeFirst();
                    while (poActTown.next()) {
                        String lsfTransNox = (String) getActTown(lnCtr, "sTransNox");// check if user added new VEHICLE PRIORITY to insert
                        if (lsfTransNox.equals("") || lsfTransNox.isEmpty()) {
                            poActTown.updateObject("sTransNox", lsTransNox);
                            poActTown.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poActTown, "activity_town", "sTownName");
                            //TODO what is substring(0,4)
                            if (!lsSQL.isEmpty()) {
                                if (poGRider.executeQuery(lsSQL, "activity_town", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
                                    if (!pbWithParent) {
                                        poGRider.rollbackTrans();
                                    }
                                    psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                    return false;
                                }
                            }
                        }
                        lnCtr++;
                    }

                    if (getActTownCount() == 1) {

                        poActTown.updateRow();
                        lsSQL = MiscUtil.rowset2SQL(poActTown,
                                "activity_town",
                                "sTownName",
                                "sTransNox = " + SQLUtil.toSQL(lsTransNox));
                        if (!lsSQL.isEmpty()) {
                            if (poGRider.executeQuery(lsSQL, "activity_town", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
                                if (!pbWithParent) {
                                    poGRider.rollbackTrans();
                                }
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }
                        }
                    }
                }
            }
            if (poActTown != null){
                poActTownOrig = (CachedRowSet) poActTown.createCopy();
            }
            if (poActVehicle != null){
                poActVehicleOrig = (CachedRowSet) poActVehicle.createCopy();
            }
            pnDeletedTownRow = null;
            pnDeletedVhclRow = null;
            pnDeletedEmpRow = null;
//            if (lsSQL.isEmpty()) {
//                psMessage = "No record to update.";
//                return false;
//            }

            if (!pbWithParent) {
                poGRider.commitTrans();
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        pnEditMode = EditMode.READY;
        return true;
    }

    /**
     *
     * Searches for a record based on the specified value and search criteria.
     *
     * @param fsValue the value used for searching
     *
     * @param fbByCode determines if the search is performed by activity code or
     * activity title
     *
     * @return {@code true} if a matching record is found and successfully
     * opened, {@code false} otherwise
     *
     * @throws SQLException if an SQL exception occurs during the search
     */
    public boolean SearchRecord(String fsValue, boolean fbByCode) throws SQLException {
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
                    "Activity No»Activity Title»",
                    "sActNoxxx»sActTitle",
                    "a.sActNoxxx»a.sActTitle",
                    0);
                    //fbByCode ? 0 : 1);

            if (loJSON != null) {
                return OpenRecord((String) loJSON.get("sActvtyID"));
            } else {
                psMessage = "No record selected.";
                return false;
            }
        }

        if (fbByCode) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sActvtyID = " + SQLUtil.toSQL(fsValue));
        } else {
            if (!fsValue.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sActTitle LIKE " + SQLUtil.toSQL("%" + fsValue + "%"));
            }
        }
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (!loRS.next()) {
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }

        lsSQL = loRS.getString("sActvtyID");
        MiscUtil.close(loRS);

        return OpenRecord(lsSQL);
    }

    /**
     *
     * Opens a record with the specified value.
     *
     * @param fsValue the value used to open the record
     *
     * @return {@code true} if the record is successfully opened, {@code false}
     * otherwise
     */
    public boolean OpenRecord(String fsValue) {
        pnEditMode = EditMode.UNKNOWN;

        if (poGRider == null) {
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

    private String getSQ_Master() {
        return " SELECT "
                + " a.sActvtyID  "
                + //1
                " ,a.sActTitle "
                + //2
                " ,a.sActDescx "
                + //3
                " ,a.sActTypID "
                + //4
                " ,IFNULL(g.sActTypDs,'') sActTypDs "
                + //5
                " ,a.dDateFrom "
                + //6
                " ,a.dDateThru "
                + //7
                " ,a.sLocation "
                + //8
                " ,a.sCompnynx "
                + //9
                " ,a.nPropBdgt "
                + //10
                " ,a.nRcvdBdgt "
                + //11
                " ,a.nTrgtClnt "
                + //12
                " ,a.sEmployID "
                + //13
                " ,a.sDeptIDxx "
                + //14
                " ,a.sLogRemrk "
                + //15
                " ,a.sRemarksx "
                + //16
                " ,a.cTranStat "
                + //17
                " ,a.sEntryByx "
                + //18
                " ,a.dEntryDte "
                + //19
                " ,IFNULL(a.sApproved, '') sApproved  "
                + //20
                " ,a.dApproved "
                + //21
                " ,a.sModified "
                + //22
                " ,a.dModified "
                + //23
                " ,IFNULL(b.sDeptName , '') sDeptName "
                + //24
                " ,IFNULL(d.sCompnyNm , '') sCompnyNm "
                + //25
                " ,IFNULL(e.sBranchNm , '') sBranchNm "
                + //26
                " ,a.sProvIDxx "
                + //27
                " ,IFNULL(f.sProvName , '') sProvName "
                +//28
                " ,IFNULL(g.sEventTyp , '') sEventTyp "
                +//29
                " ,IFNULL(a.sActNoxxx , '') sActNoxxx "
                +//30
                " FROM ggc_anyautodbf.activity_master a "
                + " LEFT JOIN ggc_isysdbf.department b ON b.sDeptIDxx = a.sDeptIDxx "
                + " LEFT JOIN ggc_isysdbf.Employee_Master001 c ON c.sEmployID = a.sEmployID "
                + " LEFT JOIN ggc_isysdbf.client_master d ON d.sClientID = a.sEmployID "
                + " LEFT JOIN ggc_anyautodbf.branch e ON e.sBranchCd = a.sLocation "
                + " LEFT JOIN province f ON f.sProvIDxx = a.sProvIDxx "
                + " LEFT JOIN event_type g ON g.sActTypID = a.sActTypID ";
    }

    private String getSQ_Department() {
        return "SELECT "
                + "sDeptIDxx"
                + " , sDeptName "
                + "FROM ggc_isysdbf.department ";
    }

    /**
     *
     * Searches for a department based on the specified value.
     *
     * @param fsValue the value used for searching the department
     *
     * @return {@code true} if the department is found, {@code false} otherwise
     *
     * @throws SQLException if an SQL exception occurs
     */
    public boolean searchDepartment(String fsValue) throws SQLException {
        String lsSQL = MiscUtil.addCondition(getSQ_Department(), " sDeptName LIKE " + SQLUtil.toSQL(fsValue + "%"));

        ResultSet loRS;
        if (!pbWithUI) {
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()) {
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

            if (loJSON == null) {
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
     *
     * Loads the department data.
     *
     * @return {@code true} if the department data is successfully loaded,
     * {@code false} otherwise
     */
    public boolean loadDepartment() {
        try {
            if (poGRider == null) {
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

    public Object getDepartment(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }

        poDepartment.absolute(fnRow);
        return poDepartment.getObject(fnIndex);
    }

    public Object getDepartment(int fnRow, String fsIndex) throws SQLException {
        if (getDeptCount() == 0 || fnRow > getDeptCount()) {
            return null;
        }
        return getDepartment(fnRow, MiscUtil.getColumnIndex(poDepartment, fsIndex));
    }

    public int getDeptCount() throws SQLException {
        if (poDepartment != null) {
            poDepartment.last();
            return poDepartment.getRow();
        } else {
            return 0;
        }
    }

    //Query for searching vehicle
    private String getSQ_Vehicle() {
        return " SELECT "
                + " a.sSerialID "
                + " ,b.sDescript "
                + " ,a.sCSNoxxxx "
                + " FROM vehicle_serial a "
                + " LEFT JOIN vehicle_master b ON b.sVhclIDxx = a.sVhclIDxx "
                + " WHERE a.cSoldStat = '1' ";
    }

    public Object getVehicle(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }

        poVehicle.absolute(fnRow);
        return poVehicle.getObject(fnIndex);
    }

    public Object getVehicle(int fnRow, String fsIndex) throws SQLException {
        if (getVehicleCount() == 0 || fnRow > getVehicleCount()) {
            return null;
        }
        return getVehicle(fnRow, MiscUtil.getColumnIndex(poVehicle, fsIndex));
    }

    public int getVehicleCount() throws SQLException {
        if (poVehicle != null) {
            poVehicle.last();
            return poVehicle.getRow();
        } else {
            return 0;
        }
    }

    private String getSQ_ActVehicle() {
        return " SELECT "
                + " IFNULL (a.sTransNox, '') sTransNox "
                + " ,IFNULL (a.nEntryNox, '') nEntryNox "
                + " ,IFNULL (a.sSerialID, '') sSerialID "
                + " ,IFNULL (c.sDescript, '') sDescript "
                + " ,IFNULL (b.sCSNoxxxx, '') sCSNoxxxx "
                + " FROM activity_vehicle a "
                + " LEFT JOIN vehicle_serial b ON b.sSerialID = a.sSerialID "
                + " LEFT JOIN vehicle_master c on c.sVhclIDxx = b.sVhclIDxx";
    }

    /**
     *
     * Adds a new vehicle to the activity vehicle data.
     *
     * @param fsSerialID The serial ID of the vehicle.
     *
     * @param fsDescript The description or name of the vehicle.
     *
     * @param fsCSNoxxxx The CS number of the vehicle.
     *
     * @return {@code true} if the vehicle was added successfully, {@code false}
     * otherwise.
     *
     * @throws SQLException if a database access error occurs.
     */
    public boolean addActVehicle(String fsSerialID, String fsDescript, String fsCSNoxxxx) throws SQLException {
        if (poActVehicle == null) {
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
        saveState(toJSONString());
        return true;
    }
    
    /**
    * Clears the activity vehicles from the data.
    *
    * This method removes all activity vehicles from the dataset. It checks if there are vehicles and deletes each vehicle's record.
    *
    * @return True if the activity vehicles are successfully cleared, otherwise false.
    * @throws SQLException if a database error occurs.
    */
    public boolean clearActVehicle() throws SQLException {
        if (getActVehicleCount() > 0) {
            poActVehicle.beforeFirst();
            while (poActVehicle.next()) {
                poActVehicle.deleteRow();
            }
        }
        return true;
    }

    /**
     * Loads the vehicle data into the application based on the specified
     * parameters.
     *
     *
     * * @param fsValue The value used to filter the data. If
     * {@code fbLoadbyAct} is true, it represents the transaction number.
     * Otherwise, it is ignored.
     * @param fbLoadbyAct Determines whether to load the vehicle data based on
     * the transaction number (true) or load all vehicle data (false).
     * @return {@code true} if the vehicle data was loaded successfully,
     * {@code false} otherwise.
     */
    public boolean loadActVehicle(String fsValue, boolean fbLoadbyAct) {

        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            if (fbLoadbyAct) {
                lsSQL = MiscUtil.addCondition(getSQ_ActVehicle(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
                loRS = poGRider.executeQuery(lsSQL);

                poActVehicle = factory.createCachedRowSet();
                poActVehicle.populate(loRS);
                MiscUtil.close(loRS);
            } else {
                //lsSQL = MiscUtil.addCondition(getSQ_Vehicle(), "0=1");
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
     * Removes a vehicle from the activity vehicle data based on the specified
     * row index.
     *
     * @param fnRow The index of the row representing the vehicle to be removed.
     *
     * @return {@code true} if the vehicle was successfully removed,
     * {@code false} otherwise.
     */
    public boolean removeVehicle(Integer fnRow[]) {
        try {
            if (getActVehicleCount() == 0) {
                psMessage = "No Activity Vehicle to delete.";
                return false;
            }

            Arrays.sort(fnRow, Collections.reverseOrder());

            for (int lnCtr : fnRow) {
                poActVehicle.absolute(lnCtr);
                String lsFind = poActVehicle.getString("sTransNox");
                if (lsFind != null && !lsFind.isEmpty()) {
                    deletedRows.add(lnCtr);
                }
                poActVehicle.deleteRow();
                System.out.println("success");
            }

            //if (deletedTownRows != null) {
            pnDeletedVhclRow = deletedRows.toArray(new Integer[deletedRows.size()]);
            //}
            deletedRows.clear();
            return true;
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
    }

    //------------------------------Activity Vehicle-----------------------------
    //Activity Vehicle Setter
    public void setActVehicle(int fnRow, int fnIndex, Object foValue) throws SQLException {
        poActVehicle.absolute(fnRow);
        switch (fnIndex) {
            case 1://sTransNox
            case 3://sSerialID
            case 4://sDescript
            case 5://sCSNoxxxx
                poActVehicle.updateObject(fnIndex, (String) foValue);
                poActVehicle.updateRow();

                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getActVehicle(fnIndex));
                }
                break;
            case 2://nEntryNox
                if (foValue instanceof Integer) {
                    poActVehicle.updateInt(fnIndex, (int) foValue);
                } else {
                    poActVehicle.updateInt(fnIndex, 0);
                }

                poActVehicle.updateRow();
                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getActVehicle(fnIndex));
                }
                break;
        }
        saveState(toJSONString());
    }

    public void setActVehicle(int fnRow, String fsIndex, Object foValue) throws SQLException {
        setActVehicle(fnRow, MiscUtil.getColumnIndex(poActVehicle, fsIndex), foValue);
    }

    //Activity Vehicle getter
    public Object getActVehicle(String fsIndex) throws SQLException {
        return getActVehicle(MiscUtil.getColumnIndex(poActVehicle, fsIndex));
    }

    //Activity Vehicle getter
    public Object getActVehicle(int fnIndex) throws SQLException {
        poActVehicle.first();
        return poActVehicle.getObject(fnIndex);
    }

    //Activity Vehicle GETTER
    public Object getActVehicle(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }

        poActVehicle.absolute(fnRow);
        return poActVehicle.getObject(fnIndex);
    }

    //Activity Vehicle GETTER
    public Object getActVehicle(int fnRow, String fsIndex) throws SQLException {
        return getActVehicle(fnRow, MiscUtil.getColumnIndex(poActVehicle, fsIndex));
    }

    //get rowcount of Activity Vehicle
    public int getActVehicleCount() throws SQLException {
        try {
            if (poActVehicle != null) {
                poActVehicle.last();
                return poActVehicle.getRow();
            } else {
                return 0;
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return 0;
        }
    }

    //--------------------------------------------------------------------------
    private String getSQ_Employee() {
        return " SELECT "
                + " c.sCompnyNm  "
                + " ,a.sEmployID "
                + " ,b.sDeptName "
                + " ,a.sDeptIDxx "
                + " ,e.sBranchNm "
                + " FROM GGC_ISysDBF.Employee_Master001 a	"
                + " LEFT JOIN GGC_ISysDBF.Department b ON  b.sDeptIDxx = a.sDeptIDxx "
                + " LEFT JOIN GGC_ISysDBF.Client_Master c on c.sClientID = a.sEmployID "
                + " LEFT JOIN GGC_ISysDBF.Branch_Others d ON d.sBranchCD = a.sBranchCd "
                + " LEFT JOIN GGC_ISysDBF.Branch e ON e.sBranchCd = a.sBranchCd "
                + " WHERE a.cRecdStat = '1' "
                + " AND ISNULL(a.dFiredxxx) " 
                + " AND d.cDivision = (SELECT cDivision "
                                        + "  FROM GGC_ISysDBF.Branch_Others "
                                        + "  WHERE sBranchCd = " +  SQLUtil.toSQL(psBranchCd) + ")";
    }

    private String getSQ_ActMember() {
        return " SELECT "
                + " a.sTransNox  "
                + " , a.nEntryNox "
                + " , a.sEmployID "
                + " , a.cOriginal "
                + " , a.sEntryByx "
                + " , a.dEntryDte "
                + " , IFNULL(c.sCompnyNm, '') sCompnyNm "
                + " , IFNULL(d.sDeptName, '') sDeptName "
                + " FROM ggc_anyautodbf.activity_member a "
                + " LEFT JOIN GGC_ISysDBF.Employee_Master001 b ON b.sEmployID = a.sEmployID "
                + " LEFT JOIN GGC_ISysDBF.Client_Master c ON c.sClientID = a.sEmployID "
                + " LEFT JOIN GGC_ISysDBF.Department d on d.sDeptIDxx = b.sDeptIDxx"
                + " WHERE a.cOriginal = '1'";
    }

    /**
     *
     * Searches for an employee based on the specified value.
     *
     * @param fsValue the value used for searching the employee
     *
     * @return {@code true} if the employee is found, {@code false} otherwise
     *
     * @throws SQLException if an SQL exception occurs
     */
    public boolean searchEmployee(String fsValue) throws SQLException {
        String lsSQL = MiscUtil.addCondition(getSQ_Employee(), " sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%"));

        ResultSet loRS;
        if (!pbWithUI) {
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()) {
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
                    "Employee Name»Department Name»Branch",
                    "sCompnyNm»sDeptName»sBranchNm",
                    "sCompnyNm»sDeptName»sBranchNm",
                    0);

            if (loJSON == null) {
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
     *
     * Loads employee data based on the specified value and load mode.
     *
     * @param fsValue the value used for loading employee data
     *
     * @param fbLoadEmp the load mode indicating whether to load employees or
     * activity members
     *
     * @return {@code true} if the employee data is successfully loaded,
     * {@code false} otherwise
     *
     * @throws SQLException if an SQL exception occurs
     */
    public boolean loadEmployee(String fsValue, boolean fbLoadEmp) throws SQLException {

        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            if (fbLoadEmp) {
                //For Searching when adding activity member when department is clicked
                lsSQL = MiscUtil.addCondition(getSQ_Employee(), "b.sDeptIDxx = " + SQLUtil.toSQL(fsValue));
                loRS = poGRider.executeQuery(lsSQL);

                poEmployees = factory.createCachedRowSet();
                poEmployees.populate(loRS);
                MiscUtil.close(loRS);
            } else {

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
    
    /**
    * Clears the activity members from the data.
    *
    * This method removes all activity members from the dataset. It checks if there are members and deletes each member's record.
    *
    * @return True if the activity members are successfully cleared, otherwise false.
    * @throws SQLException if a database error occurs.
    */
    public boolean clearActMember() throws SQLException {
        if (getActMemberCount() > 0) {
            poActMember.beforeFirst();
            while (poActMember.next()) {
                poActMember.deleteRow();
            }
        }
        return true;
    }

    public Object getEmployee(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }

        poEmployees.absolute(fnRow);
        return poEmployees.getObject(fnIndex);
    }

    public Object getEmployee(int fnRow, String fsIndex) throws SQLException {
        if (getEmpCount() == 0 || fnRow > getEmpCount()) {
            return null;
        }
        return getEmployee(fnRow, MiscUtil.getColumnIndex(poEmployees, fsIndex));
    }

    public int getEmpCount() throws SQLException {
        if (poEmployees != null) {
            poEmployees.last();
            return poEmployees.getRow();
        } else {
            return 0;
        }
    }

    /**
     *
     * Adds a member to the system.
     *
     * @param fsEmployID the employee ID of the member
     *
     * @param fsEmpName the name of the member
     *
     * @param fsDept the department of the member
     *
     * @return {@code true} if the member is successfully added, {@code false}
     * otherwise
     *
     * @throws SQLException if an SQL exception occurs
     */
    public boolean addMember(String fsEmployID, String fsEmpName, String fsDept) throws SQLException {
        if (poActMember == null) {
            String lsSQL = MiscUtil.addCondition(getSQ_ActMember(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            RowSetFactory factory = RowSetProvider.newFactory();
            poActMember = factory.createCachedRowSet();
            poActMember.populate(loRS);
            MiscUtil.close(loRS);
        }       
        
        boolean memberExists = false;
        int existingRow = -1;
        if (getActMemberCount() > 0 ) {
            for (int lnCtr = 1; lnCtr <= getActMemberCount(); lnCtr++) {
                if (getActMember(lnCtr, "sEmployID").toString().equals(fsEmployID)) {
                    memberExists = true;
                    existingRow = lnCtr;
                    break;
                }
            }
        }
        
        if (memberExists) {
            poActMember.absolute(existingRow);
            if (poActMember.getString("cOriginal").equals("0")) {
                poActMember.updateString("cOriginal", "1");
            }
        } else {
            poActMember.last();
            poActMember.moveToInsertRow();
            MiscUtil.initRowSet(poActMember);
            poActMember.updateString("sEmployID", fsEmployID);
            poActMember.updateString("sCompnyNm", fsEmpName);
            poActMember.updateString("cOriginal", "1");
            poActMember.updateString("sDeptName", fsDept);
            poActMember.insertRow();
            poActMember.moveToCurrentRow();
        }
        saveState(toJSONString());
        return true;
    }

    //------------------------------Activity Member-----------------------------
    //Activity Member Setter
    public void setActMember(int fnRow, int fnIndex, Object foValue) throws SQLException {
        poActMember.absolute(fnRow);
        switch (fnIndex) {
            case 1://sTransNox
            case 3://sEmployID
            case 7://sCompnyNm
            case 8://sDeptName
                //case 4 ://cOriginal
                poActMember.updateObject(fnIndex, (String) foValue);
                poActMember.updateRow();

                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getActMember(fnIndex));
                }
                break;
            case 2://nEntryNox
                if (foValue instanceof Integer) {
                    poActMember.updateInt(fnIndex, (int) foValue);
                } else {
                    poActMember.updateInt(fnIndex, 0);
                }

                poActMember.updateRow();
                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getActMember(fnIndex));
                }
                break;
        }
        saveState(toJSONString());
    }
    
   

    public void setActMember(int fnRow, String fsIndex, Object foValue) throws SQLException {
        setActMember(fnRow, MiscUtil.getColumnIndex(poActMember, fsIndex), foValue);
    }

    //Activity member getter
    public Object getActMember(String fsIndex) throws SQLException {
        return getActMember(MiscUtil.getColumnIndex(poActMember, fsIndex));
    }

    //Activity member getter
    public Object getActMember(int fnIndex) throws SQLException {
        poActMember.first();
        return poActMember.getObject(fnIndex);
    }

    //Activity Member GETTER
    public Object getActMember(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }

        poActMember.absolute(fnRow);
        return poActMember.getObject(fnIndex);
    }

    //Activity Member GETTER
    public Object getActMember(int fnRow, String fsIndex) throws SQLException {
        return getActMember(fnRow, MiscUtil.getColumnIndex(poActMember, fsIndex));
    }

    //get rowcount of Activity Member
    public int getActMemberCount() throws SQLException {
        try {
            if (poActMember != null) {
                poActMember.last();
                return poActMember.getRow();
            } else {
                return 0;
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return 0;
        }
    }

    //Remove activity member
    /**
     *
     * Removes an activity member at the specified row index.
     *
     * @param fnRow The row index of the activity member to be removed.
     *
     * @return {@code true} if the activity member was successfully removed,
     * {@code false} otherwise.
     */
    public boolean removeMember(Integer[] fnRow) {
        try {
            if (getActMemberCount() == 0) {
                psMessage = "No Activity Member delete.";
                return false;
            }

            Arrays.sort(fnRow, Collections.reverseOrder());

            for (int lnCtr : fnRow) {
                poActMember.absolute(lnCtr);
                poActMember.updateString("cOriginal", "0");

                System.out.println("success");
            }

            return true;
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
    }

    private String getSQ_Town() {
        return " SELECT "
                + " a.sTownName "
                + " ,a.sTownIDxx "
                + " FROM towncity a "
                + " LEFT JOIN province b ON b.sProvIDxx = a.sProvIDxx "
                + " WHERE a.cRecdStat = '1' ";
    }

    private String getSQ_ActivityTown() {
        return " SELECT "
                + " a.sTransNox "
                + " ,a.sTownIDxx "
                + " ,IFNULL(a.sAddressx, '') sAddressx "
                + " ,IFNULL(b.sTownName, '') sTownName "
                + " FROM activity_town a "
                + " LEFT JOIN towncity b ON b.sTownIDxx = a.sTownIDxx ";
    }

    //Activity Town Setter
    public void setActTown(int fnRow, int fnIndex, Object foValue) throws SQLException {
        poActTown.absolute(fnRow);
        switch (fnIndex) {
            case 1://sTransNox
            case 2://sTownIDxx
            case 3://sAddressx
            case 4: //sTownName
                poActTown.updateObject(fnIndex, (String) foValue);
                poActTown.updateRow();

                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getActTown(fnIndex));
                }
                break;
        }
        saveState(toJSONString());
    }

    public void setActTown(int fnRow, String fsIndex, Object foValue) throws SQLException {
        setActTown(fnRow, MiscUtil.getColumnIndex(poActTown, fsIndex), foValue);
    }

    //Activity Town getter
    public Object getActTown(String fsIndex) throws SQLException {
        return getActTown(MiscUtil.getColumnIndex(poActTown, fsIndex));
    }

    //Activity Town getter
    public Object getActTown(int fnIndex) throws SQLException {
        poActTown.first();
        return poActTown.getObject(fnIndex);
    }

    //Town GETTER
    public Object getActTown(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }

        poActTown.absolute(fnRow);
        return poActTown.getObject(fnIndex);
    }

    //Town GETTER
    public Object getActTown(int fnRow, String fsIndex) throws SQLException {
        return getActTown(fnRow, MiscUtil.getColumnIndex(poActTown, fsIndex));
    }

    //get rowcount of Activity Town
    public int getActTownCount() throws SQLException {
        try {
            if (poActTown != null) {
                poActTown.last();
                return poActTown.getRow();
            } else {
                return 0;
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return 0;
        }
    }

    public boolean addActTown(String fsTownID, String fsTownName) throws SQLException {
        if (poActTown == null) {
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
        saveState(toJSONString());
        return true;
    }
    
    /**
    * Clears the activity towns from the data.
    *
    * This method removes all activity towns from the dataset. It checks if there are towns and deletes each town's record.
    *
    * @return True if the activity towns are successfully cleared, otherwise false.
    * @throws SQLException if a database error occurs.
    */
    public boolean clearActTown() throws SQLException {
        if (getActTownCount() > 0) {
            poActTown.beforeFirst();
            while (poActTown.next()) {
                poActTown.deleteRow();
            }
        }
        return true;
    }

    /**
     * Loads town data based on the provided value.
     *
     * @param fsValue The value to use for loading the town data.
     * @param fbByLoad Determines whether to load the town data by a specific
     * condition or activity town. - If true, the town data will be loaded based
     * on the condition "a.sProvIDxx = fsValue". - If false, the town data will
     * be loaded based on the activity town condition "a.sTransNox = fsValue".
     * @return True if the town data was successfully loaded, False otherwise.
     */
    public boolean loadTown(String fsValue, boolean fbByLoad) {
        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            if (fbByLoad) {
                lsSQL = MiscUtil.addCondition(getSQ_Town(), "a.sProvIDxx = " + SQLUtil.toSQL(fsValue));
                System.out.println(lsSQL);
                loRS = poGRider.executeQuery(lsSQL);

                poTown = factory.createCachedRowSet();
                poTown.populate(loRS);
                MiscUtil.close(loRS);
            } else {
                lsSQL = MiscUtil.addCondition(getSQ_ActivityTown(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
                System.out.println(lsSQL);
                loRS = poGRider.executeQuery(lsSQL);

                poActTown = factory.createCachedRowSet();
                poActTown.populate(loRS);
                MiscUtil.close(loRS);
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        return true;
    }
    
    /**
    * Removes selected activity towns.
    *
    * This method removes activity towns based on the provided array of row indices. It also performs checks to ensure valid deletions and keeps track of deleted rows.
    *
    * @param fnRow An array of row indices to be removed.
    * @return True if the selected activity towns are successfully removed, otherwise false.
    */
    public boolean removeTown(Integer[] fnRow) {
        try {
            if (getActTownCount() == 0) {
                psMessage = "No Activity Town to delete.";
                return false;
            }            
            Arrays.sort(fnRow, Collections.reverseOrder());

            for (int lnCtr : fnRow) {
                poActTown.absolute(lnCtr);
                String lsFind = poActTown.getString("sTransNox");
                if (lsFind != null && !lsFind.isEmpty()) {
                    String lsActTown = poActTown.getString("sTownIDxx");                    
                    for (int lnCtr2 = 1; lnCtr2 <= getOrigActTownCount(); lnCtr2++){                                        
                        if (lsActTown.equals((String) getOrigActTownDetail(lnCtr2,"sTownIDxx"))){
                            deletedRows.add(lnCtr2);                            
                            break;
                        }
                    }                                            
                }
                poActTown.deleteRow();
                System.out.println("success");
            }

            //if (deletedTownRows != null) {
            //pnDeletedTownRow = deletedRows.toArray(new Integer[deletedRows.size()]);
            //}
            //deletedRows.clear();
            return true;
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
    }

    public Object getTown(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }

        poTown.absolute(fnRow);
        return poTown.getObject(fnIndex);
    }

    public Object getTown(int fnRow, String fsIndex) throws SQLException {
        if (getTownCount() == 0 || fnRow > getTownCount()) {
            return null;
        }
        return getTown(fnRow, MiscUtil.getColumnIndex(poTown, fsIndex));
    }

    public int getTownCount() throws SQLException {
        if (poTown != null) {
            poTown.last();
            return poTown.getRow();
        } else {
            return 0;
        }
    }
    
    private int getOrigActTownCount() throws SQLException{
        if (poActTownOrig != null){
            poActTownOrig.last();
            return poActTownOrig.getRow();
        }else{
            return 0;
        }
    }
    
    private Object getOrigActTownDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poActTownOrig.absolute(fnRow);
        return poActTownOrig.getObject(fnIndex);
    }
    
    private Object getOrigActTownDetail(int fnRow, String fsIndex) throws SQLException{
        return getOrigActTownDetail(fnRow, MiscUtil.getColumnIndex(poActTownOrig, fsIndex));
    }
    
    private String getSQ_Province() {
        return " SELECT "
                + " sProvIDxx "
                + " ,sProvName "
                + " From province "
                + " WHERE cRecdStat = '1' ";
    }

    /**
     * Searches for a province based on the provided value.
     *
     * @param fsValue the value used to search for a province
     * @return true if the province is found and set as the master record, false
     * otherwise
     * @throws SQLException if an error occurs while executing the SQL query
     */
    public boolean searchProvince(String fsValue) throws SQLException {
        String lsSQL = MiscUtil.addCondition(getSQ_Province(), " sProvName LIKE " + SQLUtil.toSQL(fsValue + "%"));

        ResultSet loRS;
        if (!pbWithUI) {
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()) {
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

            if (loJSON == null) {
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
    public String getSQ_Branch() {
        return " SELECT "
                + " IFNULL(a.sBranchCd, '') sBranchCd "
                + " , IFNULL(a.sBranchNm, '') sBranchNm "
                + " , IFNULL(b.cDivision, '') cDivision "
                + " FROM branch a "
                + " LEFT JOIN branch_others b ON a.sBranchCd = b.sBranchCd  "
                + " WHERE a.cRecdStat = '1'  "
                + " AND b.cDivision = (SELECT cDivision FROM branch_others WHERE sBranchCd = " + SQLUtil.toSQL(psBranchCd) + ")";
    }
    
    /**
    * Searches for a branch by name and retrieves branch details.
    *
    * This method performs a search for a branch by name and retrieves branch details such as location and branch name. It allows both UI and non-UI search modes and provides feedback if no records are found.
    *
    * @param fsValue The branch name or a search query.
    * @return True if the branch is successfully found and details are retrieved, otherwise false.
    * @throws SQLException if a database error occurs.
    */
    public boolean searchBranch(String fsValue) throws SQLException {
        String lsSQL = (getSQ_Branch() + " AND sBranchNm LIKE " + SQLUtil.toSQL(fsValue + "%"));

        ResultSet loRS;
        if (!pbWithUI) {
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            if (loRS.next()) {
                setMaster("sLocation", loRS.getString("sBranchCd"));
                setMaster("sBranchNm", loRS.getString("sBranchNm"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider,
                    lsSQL,
                    fsValue,
                    "Branch Name»Branch Code",
                    "sBranchNm»sBranchCd",
                    "sBranchNm»sBranchCd",
                    0);

            if (loJSON == null) {
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sLocation", (String) loJSON.get("sBranchCd"));
                setMaster("sBranchNm", (String) loJSON.get("sBranchNm"));
            }
        }
        return true;
    }
    
    /**
    * Cancels an activity record in the database.
    *
    * This method is used to cancel an activity record, with validation checks for the appropriate cancellation scenario. It updates the transaction status and handles database operations.
    *
    * @param fsValue The identifier of the activity record to be canceled.
    * @return True if the activity record is successfully canceled, otherwise false.
    * @throws SQLException if a database error occurs.
    */
    public boolean CancelActivity(String fsValue) throws SQLException {
        if (pnEditMode != EditMode.READY) {
            psMessage = "Invalid update mode detected.";
            return false;
        }

        psMessage = "";

//        if (!((String) getMaster("cTranStat")).equals("0")) {
//            psMessage = "Unable to cancel transactions.";
//            return false;
//        }
        //TODO ADD VALIDATION FOR CANCELLING
        //validation for allowed employee to cancel
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
                + " cTranStat = '2'"
                + " WHERE sActvtyID = " + SQLUtil.toSQL(fsValue);

        if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }

        //pnEditMode = EditMode.UNKNOWN;
        return true;
    }

    private String getSQ_ActEvent() {
        return " SELECT"
                + " sActTypID "
                + " ,sActTypDs "
                + " ,sEventTyp "
                + " ,cRecdStat "
                + " ,sEntryByx "
                + " ,dEntryDte "
                + " FROM event_type";

    }
    //moved to ActivitySource parameter
    /**
     * Saves an event type to the database.
     *
     * @param fsType The type of the event to be saved.
     * @param fsSource The source description of the event to be saved.
     * @return {@code true} if the event type is saved successfully,
     * {@code false} otherwise.
     * @throws SQLException if there is an error executing the SQL query.
     */
//    public boolean SaveEventType(String fsType, String fsSource) throws SQLException {
//        String lsFind = getSQ_ActEvent();
//        lsFind = MiscUtil.addCondition(lsFind, "sActTypDs = " + SQLUtil.toSQL(fsSource)
//                + " AND sEventTyp = " + SQLUtil.toSQL(fsType));
//       
//        ResultSet loRS = poGRider.executeQuery(lsFind);
//        System.out.println(lsFind);
//        if (MiscUtil.RecordCount(loRS) > 0) {
//            psMessage = "Existing Activity Event.";
//            MiscUtil.close(loRS);
//            return false;
//        }
//
//        String lsSQL = "INSERT INTO event_type  "
//                + "(sActTypID,sActTypDs,sEventTyp,cRecdStat,sEntryByx,dEntryDte)"
//                + " VALUES (" + SQLUtil.toSQL(MiscUtil.getNextCode("event_type", "sActTypID", true, poGRider.getConnection(), psBranchCd))
//                + "," + SQLUtil.toSQL(fsSource) + ", " + SQLUtil.toSQL(fsType) + "," + "1," + SQLUtil.toSQL(poGRider.getUserID()) + ", " + SQLUtil.toSQL(poGRider.getServerDate()) + ")";
//        if (!lsSQL.isEmpty()) {
//            if (poGRider.executeQuery(lsSQL, "event_type", psBranchCd, "") <= 0) {
//                psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
//                return false;
//            }
//        }
//
//        //pnEditMode = EditMode.UNKNOWN;
//        return true;
//    }

    /**
     * Searches for an event type based on the provided value.
     *
     * @param fsValue The value to search for in the event type.
     * @return {@code true} if a matching event type is found, {@code false}
     * otherwise.
     * @throws SQLException if there is an error executing the SQL query.
     */
    public boolean searchEventType(String fsValue) throws SQLException {
        
        String lsSQL = MiscUtil.addCondition(getSQ_ActEvent(), " sEventTyp LIKE " + SQLUtil.toSQL(fsValue + "%"));

        ResultSet loRS;
        if (!pbWithUI) {
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()) {
                setMaster("sActTypDs", loRS.getString("sActTypDs"));
                setMaster("sActTypID", loRS.getString("sActTypID"));
                setMaster("sEventTyp", loRS.getString("sEventTyp"));
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
                    "Event Type»Source",
                    "sEventTyp»sActTypDs",
                    "sEventTyp»sActTypDs",
                    0);

            if (loJSON == null) {
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sActTypDs", (String) loJSON.get("sActTypDs"));
                setMaster("sActTypID", (String) loJSON.get("sActTypID"));
                setMaster("sEventTyp", (String) loJSON.get("sEventTyp"));
            }
        }
        return true;
    }
    
    /**
    * Loads activity records for approval.
    *
    * This method retrieves activity records that are pending approval. It checks for conditions such as the transaction status and approval status, and then populates a RowSet with the results.
    *
    * @return True if the activity records are successfully loaded for approval, otherwise false.
    * @throws SQLException if a database error occurs.
    */
    public boolean loadActForApproval() throws SQLException {
        if (poGRider == null) {
            psMessage = "Application driver is not set.";
            return false;
        }

        psMessage = "";

        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();

        lsSQL = MiscUtil.addCondition(getSQ_Master(), " a.cTranStat <> '1'"
                + "AND (a.sApproved IS NULL OR a.sApproved = '') ORDER BY dDateFrom DESC ");
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        poMaster = factory.createCachedRowSet();
        poMaster.populate(loRS);
        MiscUtil.close(loRS);

        return true;
    }

    //TODO ADD VALIDATION FOR ACCESS
    /**
     * Approves an activity with the specified value.
     *
     * @param fsValue The value of the activity to be approved.
     * @return {@code true} if the activity is approved successfully,
     * {@code false} otherwise.
     */
    public boolean ApproveActivity(String fsValue) {
        String lsTransNox = fsValue;
        String lsSQL = "UPDATE activity_master SET"
                + "  cTranStat = '1'"
                + ", sApproved = " + SQLUtil.toSQL(poGRider.getUserID())
                + ", dApproved = " + SQLUtil.toSQL(poGRider.getServerDate())
                + " WHERE sActvtyID = " + SQLUtil.toSQL(lsTransNox);

        if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }

        pnEditMode = EditMode.UNKNOWN;
        return true;

    }

    public void displayMasFields() throws SQLException {
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) {
            return;
        }

        int lnRow = poMaster.getMetaData().getColumnCount();

        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");

        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++) {
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poMaster.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poMaster.getMetaData().getColumnType(lnCtr));
            if (poMaster.getMetaData().getColumnType(lnCtr) == Types.CHAR
                    || poMaster.getMetaData().getColumnType(lnCtr) == Types.VARCHAR) {

                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poMaster.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }

        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }

    public void displayMemberFields() throws SQLException {
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) {
            return;
        }

        int lnRow = poActMember.getMetaData().getColumnCount();

        System.out.println("----------------------------------------");
        System.out.println("ACTIVITY MEMBER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");

        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++) {
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poActMember.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poActMember.getMetaData().getColumnType(lnCtr));
            if (poActMember.getMetaData().getColumnType(lnCtr) == Types.CHAR
                    || poActMember.getMetaData().getColumnType(lnCtr) == Types.VARCHAR) {

                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poActMember.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }

        System.out.println("----------------------------------------");
        System.out.println("END: ACTIVITY MEMBER TABLE INFO");
        System.out.println("----------------------------------------");
    }

    public boolean isEntryOK() {        
        return true;
    }
}
