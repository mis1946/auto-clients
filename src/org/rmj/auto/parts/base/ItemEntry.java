/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template
file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.parts.base;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneId;
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
import org.rmj.auto.clients.base.CompareRows;
import org.rmj.auto.json.TabsStateManager;

/**
 *
 * @author Jahn 
 * Continued by Arsiela - 08-01-2023
 */
public class ItemEntry {
    private final String MASTER_TABLE = "inventory";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autapp_json/" + TabsStateManager.getJsonFileName("Item Information");
    
    private final String SALES = "A011";
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
    private Integer pnDeletedInvModelRow[];
    private Integer pnDeletedInvModelYrRow[];
    
    private CachedRowSet poMaster;
    private CachedRowSet poDetail;
    private CachedRowSet poVhclModel;
    private CachedRowSet poInvModel;
    private CachedRowSet poInvModelOrig;
    private CachedRowSet poVhclModelYear;
    private CachedRowSet poInvModelYear;
    private CachedRowSet poInvModelYearOrig;
    private CachedRowSet poSuperSede;
    private CachedRowSet poInvSuperSede;
    private CachedRowSet poInvSuperSedeOrig;
    
    List<Integer> deletedRows = new ArrayList<>();
    List<Integer> deletedModelRows = new ArrayList<>();
    List<Integer> deletedMdlYrRows = new ArrayList<>();
            
    public ItemEntry(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
    
    //Item Entry MASTER SEARCH COUNT
    public int getMasterCount() throws SQLException{
        if (poMaster != null){
            poMaster.last();
            return poMaster.getRow();
        }else{
            return 0;
        }              
    }
    
    public int getMasterDetailCount() throws SQLException{
        if (poDetail != null){
            poDetail.last();
            return poDetail.getRow();
        }else{
            return 0;
        }              
    }
    
    public int getSupersedeCount() throws SQLException{
        if (poSuperSede != null){
            poSuperSede.last();
            return poSuperSede.getRow();
        } else {
            return 0;
        }
    }
    
    private String toJSONString(){
        JSONParser loParser = new JSONParser();
        JSONArray laMaster = new JSONArray();
        JSONArray laPriority = new JSONArray();
        JSONArray laPromo = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;
        
        try {
            loJSON = new JSONObject();
            String lsValue =  CommonUtils.RS2JSON(poMaster).toJSONString();
            laMaster = (JSONArray) loParser.parse(lsValue);
            loMaster = (JSONObject) laMaster.get(0);
            loJSON.put("master", loMaster);
            
            if(poInvModel != null){
                lsValue = CommonUtils.RS2JSON(poInvModel).toJSONString();
                laPriority = (JSONArray) loParser.parse(lsValue);
                loJSON.put("model", laPriority);
            }
            
            if(poInvModelYear != null){
                lsValue = CommonUtils.RS2JSON(poInvModelYear).toJSONString();
                laPromo = (JSONArray) loParser.parse(lsValue);
                loJSON.put("modelyear", laPromo);
            }
            
            if(poInvSuperSede != null){
                lsValue = CommonUtils.RS2JSON(poInvSuperSede).toJSONString();
                laPromo = (JSONArray) loParser.parse(lsValue);
                loJSON.put("supersede", laPromo);
            }
            
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
            Logger.getLogger(ItemEntry.class.getName()).log(Level.SEVERE, null, ex);
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
                            case Types.DOUBLE:
                            case Types.DECIMAL:
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
                    
                    // Extract the "model" array from the JSON object
                    JSONArray modelArray = (JSONArray) jsonObject.get("model");
                    if(modelArray != null) {
                        if(modelArray.size()>0){
                            while (modelArray.size() > getInvModelCount()) {
                                String lsSQL;
                                ResultSet loRS;
                                RowSetFactory factory;
                                
                                if (poInvModel == null) {
                                    lsSQL = MiscUtil.addCondition(getInv_model(), "0=1");
                                    loRS = poGRider.executeQuery(lsSQL);
                                    factory = RowSetProvider.newFactory();
                                    poInvModel = factory.createCachedRowSet();
                                    poInvModel.populate(loRS);
                                    MiscUtil.close(loRS);
                                }
                                poInvModel.last();
                                poInvModel.moveToInsertRow();
                                MiscUtil.initRowSet(poInvModel);
                                poInvModel.insertRow();
                                poInvModel.moveToCurrentRow();
                                
                            }
                            
                            // Extract index and value from each object in the "labor" array
                            for (Object item : modelArray) { 
                                poInvModel.beforeFirst();
                                while (poInvModel.next()){
                                    if(ctr == row){
                                        JSONObject priority = (JSONObject) item;
                                        for (Object key : priority.keySet()) {
                                            Object value = priority.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(poInvModel.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    poInvModel.updateObject(Integer.valueOf(key.toString()), value );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInvModel.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInvModel.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInvModel.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    poInvModel.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        poInvModel.updateRow();
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
                    // Extract the "modelyear" array from the JSON object
                    JSONArray modelyearArray = (JSONArray) jsonObject.get("modelyear");
                    if(modelyearArray != null) {
                        if(modelyearArray.size()>0){
                            while (modelyearArray.size() > getInvModelYrCount()) {
                                String lsSQL;
                                ResultSet loRS;
                                RowSetFactory factory;
                                
                                if (poInvModelYear == null) {
                                    lsSQL = MiscUtil.addCondition(getInv_modelYear(), "0=1");
                                    loRS = poGRider.executeQuery(lsSQL);
                                    factory = RowSetProvider.newFactory();
                                    poInvModelYear = factory.createCachedRowSet();
                                    poInvModelYear.populate(loRS);
                                    MiscUtil.close(loRS);
                                }
                                poInvModelYear.last();
                                poInvModelYear.moveToInsertRow();
                                MiscUtil.initRowSet(poInvModelYear);
                                poInvModelYear.insertRow();
                                poInvModelYear.moveToCurrentRow();
                            }
                            // Extract index and value from each object in the "parts" array
                            for (Object item : modelyearArray) {
                                poInvModelYear.beforeFirst(); 
                                while (poInvModelYear.next()){  
                                    if(ctr == row){
                                        JSONObject parts = (JSONObject) item;
                                        for (Object key : parts.keySet()) {
                                            Object value = parts.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(poInvModelYear.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    poInvModelYear.updateObject(Integer.valueOf(key.toString()), tempValue );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInvModelYear.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInvModelYear.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInvModelYear.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    poInvModelYear.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        poInvModelYear.updateRow();
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
                    //TODO Arsiela 01-13-2024
                    // Extract the "supersede" array from the JSON object
                    JSONArray supersedeArray = (JSONArray) jsonObject.get("supersede");
                    if(supersedeArray != null) {
                        if(supersedeArray.size()>0){
                            while (supersedeArray.size() > getSupersedeCount()) {
                                String lsSQL;
                                ResultSet loRS;
                                RowSetFactory factory;
                                
                                if (poInvSuperSede == null) {
                                    lsSQL = MiscUtil.addCondition("", "0=1");
                                    loRS = poGRider.executeQuery(lsSQL);
                                    factory = RowSetProvider.newFactory();
                                    poInvSuperSede = factory.createCachedRowSet();
                                    poInvSuperSede.populate(loRS);
                                    MiscUtil.close(loRS);
                                }
                                poInvSuperSede.last();
                                poInvSuperSede.moveToInsertRow();
                                MiscUtil.initRowSet(poInvSuperSede);
                                poInvSuperSede.insertRow();
                                poInvSuperSede.moveToCurrentRow();
                            }
                            
                            // Extract index and value from each object in the "labor" array
                            for (Object item : modelArray) { 
                                poInvSuperSede.beforeFirst();
                                while (poInvSuperSede.next()){
                                    if(ctr == row){
                                        JSONObject priority = (JSONObject) item;
                                        for (Object key : priority.keySet()) {
                                            Object value = priority.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(poInvSuperSede.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    poInvSuperSede.updateObject(Integer.valueOf(key.toString()), value );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInvSuperSede.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInvSuperSede.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInvSuperSede.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    poInvSuperSede.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        poInvSuperSede.updateRow();
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
            Logger.getLogger(ItemEntry.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
   
    //------------------------------------ITEM ENTRY MASTER---------------------
    //TODO add setMaster for Item Entry
    public void setMaster(int fnIndex, Object foValue) throws SQLException{        
        poMaster.first();
        
        switch (fnIndex){  
            case 1 : //sStockIDx 
            case 2 : //sBarCodex 
            case 3 : //sDescript 
            case 4 : //sBriefDsc                   
            case 5 : //sCategCd1 
            case 6 : //sCategCd2 
            case 7 : //sCategCd3 
            case 8 : //sCategCd4 
            case 9 : //sBrandCde 
            case 10: //sModelCde 
            case 11: //sMeasurID 
            case 12: //sInvTypCd             
            case 19: //cComboInv 
            case 20: //cWthPromo 
            case 21: //cUnitType 
            case 22: //cInvStatx 
            case 23: //cGenuinex 
            case 24: //cReplacex 
            case 25: //sSupersed 
            case 26: //sFileName 
            case 27: //sTrimBCde 
            case 28: //cRecdStat 
            case 32: //sBrandNme
            case 33: //sCategNme
            case 34: //sMeasurNm
            case 35: //sInvTypNm
            case 36: //sLocatnID
            case 37: //sLocatnDs
//            case 29: //sModified 
//            case 30: //dModified 
//            case 31: //dTimeStmp 	
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 13: //nUnitPrce 
            case 14: //nSelPrice 
            case 15: //nDiscLev1 
            case 16: //nDiscLev2 
            case 17: //nDiscLev3 
            case 18: //nDealrDsc               
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
    
    //Item Entry Master setter
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poMaster, fsIndex), foValue);
    }
    //Item Entry Master getter
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    //Item Entry Master getter
    public Object getMaster(int fnIndex) throws SQLException{
        poMaster.first();
        return poMaster.getObject(fnIndex);
    }
    
    //Item Entry SEARCH GETTER
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        poDetail.absolute(fnRow);
        return poDetail.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poDetail, fsIndex));
    }
    
    /*ORIGINAL MODEL CACHEROWSET*/
    private int getOrigModelCount() throws SQLException{
        if (poInvModelOrig != null){
            poInvModelOrig.last();
            return poInvModelOrig.getRow();
        }else{
            return 0;
        }
    }
    
    private Object getOrigModelDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poInvModelOrig.absolute(fnRow);
        return poInvModelOrig.getObject(fnIndex);
    }
    
    private Object getOrigModelDetail(int fnRow, String fsIndex) throws SQLException{
        return getOrigModelDetail(fnRow, MiscUtil.getColumnIndex(poInvModelOrig, fsIndex));
    }
    
    /*ORIGINAL MODEL YEAR CACHEROWSET*/
    private int getOrigModelYrCount() throws SQLException{
        if (poInvModelYearOrig != null){
            poInvModelYearOrig.last();
            return poInvModelYearOrig.getRow();
        }else{
            return 0;
        }
    }
    
    private Object getOrigModelYrDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poInvModelYearOrig.absolute(fnRow);
        return poInvModelYearOrig.getObject(fnIndex);
    }
    
    private Object getOrigModelYrDetail(int fnRow, String fsIndex) throws SQLException{
        return getOrigModelYrDetail(fnRow, MiscUtil.getColumnIndex(poInvModelYearOrig, fsIndex));
    }
    
    //-----------------------------------------New Record---------------------------
    /**
     * Initializes the master data for adding a new entry.
     */
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {       
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1"); 
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            //-------------------------------ITEM ENTRY Master------------------
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);       
            poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);                     
            poMaster.insertRow();
            poMaster.moveToCurrentRow();   
            
            if (!clearInvModel()){
                psMessage = "Error clear fields for Inventory Model";
                return false;
            }
            if (!clearInvModelYr()){
                psMessage = "Error clear fields for Inventory Model Year";
                return false;
            }
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    /**
     * Load list for Item Information Records.
     * @return true
     * @throws SQLException 
     */
    public boolean LoadMasterList() throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL = getSQ_Master();
        System.out.println(lsSQL);
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        loRS = poGRider.executeQuery(lsSQL);
        poDetail = factory.createCachedRowSet();
        poDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    /**
     * Opens a record with the specified value.
     * @param fsValue the value used to open the record
     * 
     */
   public boolean OpenRecord(String fsValue) {
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
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sStockIDx = " + SQLUtil.toSQL(fsValue));
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            //open model / year list
            loadInvModel_year(fsValue);
            
            //TODO open supersede list
           
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }
    
    /**
     * Prepares to update a record in the data.
     * This method creates copies of the original data to be updated and sets the edit mode to UPDATE.
     * @return 
     */
    public boolean UpdateRecord(){
        try {
            if (poInvModel != null) {
                poInvModelOrig = (CachedRowSet) poInvModel.createCopy();
            }
            if (poInvModelYear != null) {
                poInvModelYearOrig = (CachedRowSet) poInvModelYear.createCopy();
            }
            if (poInvSuperSede != null) {
                poInvSuperSedeOrig = (CachedRowSet) poInvSuperSede.createCopy();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ItemEntry.class.getName()).log(Level.SEVERE, null, ex);
        }
        pnEditMode = EditMode.UPDATE;
        return true;        
    }
    /**
     * Saves a record to the database.
     * This method is responsible for adding a new record or updating an existing one based on the edit mode. It performs data validation and handles database transactions.
     * 
     */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            String lsSQL = "";
            String lsStockID = "";
            Integer lnCtr = 0;
            if (!pbWithParent) poGRider.beginTrans();
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                /*************SAVE INVENTORY TABLE***************/
                lsStockID = MiscUtil.getNextCode(MASTER_TABLE, "sStockIDx", true, poGRider.getConnection(), psBranchCd);
                poMaster.updateString("sStockIDx", lsStockID);                                                             
                poMaster.updateString("sTrimBCde", ((String) getMaster("sBarCodex")).replaceAll("\\s", ""));
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sBrandNme»sCategNme»sMeasurNm»sInvTypNm»sLocatnID»sLocatnDs"); //»nUnitPrce
                if (lsSQL.isEmpty()){
                    psMessage = "No record to update.";
                    return false;
                }
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                    return false;
                }
                
                /*************SAVE INVENTORY MODEL YEAR TABLE***************/
                if (getInvModelYrCount() > 0) {
                    lnCtr = 1;
                    poInvModelYear.beforeFirst();
                    while (poInvModelYear.next()) {
                        poInvModelYear.updateString("sStockIDx", lsStockID);
                        poInvModelYear.updateRow();
                        
                        lsSQL = MiscUtil.rowset2SQL(poInvModelYear, "inventory_model_year", "sMakeDesc»sModelDsc");
                        //TODO what is substring(0,4)
                        if (poGRider.executeQuery(lsSQL, "inventory_model_year", psBranchCd, "") <= 0) {
                            if (!pbWithParent) { poGRider.rollbackTrans(); }
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }
                }
                
                /*************SAVE INVENTORY MODEL TABLE***************/
                if (getInvModelCount() > 0) {
                    lnCtr = 1;
                    poInvModel.beforeFirst();
                    while (poInvModel.next()) {
                        poInvModel.updateString("sStockIDx", lsStockID);
                        poInvModel.updateObject("nEntryNox", lnCtr);
                        poInvModel.updateRow();
                        lsSQL = MiscUtil.rowset2SQL(poInvModel, "inventory_model", "sMakeDesc»sModelDsc");
                        //TODO what is substring(0,4)
                        if (poGRider.executeQuery(lsSQL, "inventory_model", psBranchCd, "") <= 0) {
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
                /*************SAVE INVENTORY TABLE***************/
                poMaster.updateString("sTrimBCde", ((String) getMaster("sBarCodex")).replaceAll("\\s", ""));
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sBrandNme»sCategNme»sMeasurNm»sInvTypNm»sLocatnID»sLocatnDs",  //»nUnitPrce
                                            "sStockIDx = " + SQLUtil.toSQL((String) getMaster("sStockIDx")));
                if (lsSQL.isEmpty()){
                    psMessage = "No record to update.";
                    return false;
                }
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                    return false;
                }
                
                /*************SAVE INVENTORY MODEL YEAR TABLE***************/
                if (deletedMdlYrRows != null && !deletedMdlYrRows.isEmpty()) {
                    pnDeletedInvModelYrRow = deletedMdlYrRows.toArray(new Integer[deletedMdlYrRows.size()]);
                }
                
                if (pnDeletedInvModelYrRow != null && pnDeletedInvModelYrRow.length != 0) {
                    Arrays.sort(pnDeletedInvModelYrRow, Collections.reverseOrder());
                    poInvModelYearOrig.beforeFirst();
                    for (int rowNum : pnDeletedInvModelYrRow) {
                        System.out.println("model year orig row >>> " + rowNum);
                        poInvModelYearOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM inventory_model_year WHERE"
                                + " sStockIDx = " + SQLUtil.toSQL((String) getMaster("sStockIDx"))
                                + " AND nYearModl = " + SQLUtil.toSQL(poInvModelYearOrig.getInt("nYearModl"))
                                + " AND sModelCde = " + SQLUtil.toSQL(poInvModelYearOrig.getString("sModelCde"));

                        if (poGRider.executeQuery(lsSQL, "inventory_model_year", psBranchCd, "") <= 0) {
                            psMessage = "DELETE at Inventory Model Year : " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                if (getInvModelYrCount() > 0) {
                    lnCtr = 1;
                    poInvModelYear.beforeFirst();
                    while (lnCtr <= getInvModelYrCount()) {
                        if (!CompareRows.isRowEqual(poInvModelYear, poInvModelYearOrig, lnCtr)) {
                            String lsCode = (String) getInvModelYr(lnCtr, "sStockIDx");// check if user added new VEHICLE MODEL YEAR to insert
                            if (lsCode.equals("") || lsCode.isEmpty()) {
                                poInvModelYear.updateString("sStockIDx", (String) getMaster("sStockIDx"));
                                poInvModelYear.updateRow();
                                
                                lsSQL = MiscUtil.rowset2SQL(poInvModelYear, "inventory_model_year", "sMakeDesc»sModelDsc");
                                //TODO what is substring(0,4)
                                if (poGRider.executeQuery(lsSQL, "inventory_model_year", psBranchCd, ((String) getMaster("sStockIDx")).substring(0, 4)) <= 0) {
                                    if (!pbWithParent) {
                                        poGRider.rollbackTrans();
                                    }
                                    psMessage = "ADD at Inventory Model Year : " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                    return false;
                                }
                            } else {
                                lsSQL = MiscUtil.rowset2SQL(poInvModelYear,
                                        "inventory_model_year",
                                        "sMakeDesc»sModelDsc",
                                        " sStockIDx = " + SQLUtil.toSQL((String) getMaster("sStockIDx"))
                                        + " AND nYearModl = " + SQLUtil.toSQL(poInvModelYear.getString("nYearModl"))
                                        + " AND sModelCde = " + SQLUtil.toSQL(poInvModelYear.getString("sModelCde")) );

                                if (!lsSQL.isEmpty()) {
                                    if (poGRider.executeQuery(lsSQL, "inventory_model_year", psBranchCd, ((String) getMaster("sStockIDx")).substring(0, 4)) <= 0) {
                                        if (!pbWithParent) {
                                            poGRider.rollbackTrans();
                                        }
                                        psMessage = "UPDATE at Inventory Model Year : " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                        return false;
                                    }
                                }
                            }
                        }
                        lnCtr++;
                    }
                }
                
                /*************SAVE INVENTORY MODEL TABLE***************/
                if (deletedModelRows != null && !deletedModelRows.isEmpty()) {
                    pnDeletedInvModelRow = deletedModelRows.toArray(new Integer[deletedModelRows.size()]);
                }
                
                if (pnDeletedInvModelRow != null && pnDeletedInvModelRow.length != 0) {
                    Arrays.sort(pnDeletedInvModelRow, Collections.reverseOrder());
                    poInvModelOrig.beforeFirst();
                    for (int rowNum : pnDeletedInvModelRow) {
                        poInvModelOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM inventory_model WHERE"
                                + " sStockIDx = " + SQLUtil.toSQL((String) getMaster("sStockIDx"))
                                + " AND nEntryNox = " + SQLUtil.toSQL(poInvModelOrig.getInt("nEntryNox"))
                                + " AND sModelCde = " + SQLUtil.toSQL(poInvModelOrig.getString("sModelCde"));

                        if (poGRider.executeQuery(lsSQL, "inventory_model", psBranchCd, "") <= 0) {
                            psMessage = "DELETE at Inventory Model : " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                
                if (getInvModelCount() > 0) {
                    lnCtr = 1;
                    poInvModel.beforeFirst();
                    while (lnCtr <= getInvModelCount()) {
                        if (!CompareRows.isRowEqual(poInvModel, poInvModelOrig, lnCtr)) {
                            String lsCode = (String) getInvModel(lnCtr, "sStockIDx");// check if user added new VEHICLE MODEL to insert
                            if (lsCode.equals("") || lsCode.isEmpty()) {
                                poInvModel.updateString("sStockIDx", (String) getMaster("sStockIDx"));
                                poInvModel.updateObject("nEntryNox", lnCtr);
                                poInvModel.updateRow();

                                lsSQL = MiscUtil.rowset2SQL(poInvModel, "inventory_model", "sMakeDesc»sModelDsc");
                                //TODO what is substring(0,4)
                                if (poGRider.executeQuery(lsSQL, "inventory_model", psBranchCd, ((String) getMaster("sStockIDx")).substring(0, 4)) <= 0) {
                                    if (!pbWithParent) {
                                        poGRider.rollbackTrans();
                                    }
                                    psMessage = "ADD at Inventory Model : " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                    return false;
                                }
                            } else {
                                poInvModel.updateObject("nEntryNox", lnCtr);
                                poInvModel.updateRow();

                                lsSQL = MiscUtil.rowset2SQL(poInvModel,
                                        "inventory_model",
                                        "sMakeDesc»sModelDsc",
                                        " sStockIDx = " + SQLUtil.toSQL((String) getMaster("sStockIDx")) 
                                        + " AND sModelCde = " + SQLUtil.toSQL(poInvModel.getString("sModelCde"))
                                        );

                                if (!lsSQL.isEmpty()) {
                                    if (poGRider.executeQuery(lsSQL, "inventory_model", psBranchCd, ((String) getMaster("sStockIDx")).substring(0, 4)) <= 0) {
                                        if (!pbWithParent) {
                                            poGRider.rollbackTrans();
                                        }
                                        psMessage = "UPDATE at Inventory Model : " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                        return false;
                                    }
                                }
                            }
                        }
                        lnCtr++;
                    }
                }
                poInvModelOrig = (CachedRowSet) poInvModel.createCopy();
                poInvModelYearOrig = (CachedRowSet) poInvModelYear.createCopy();
                //poSuperSedeOrig = (CachedRowSet) poSuperSede.createCopy();
                pnDeletedInvModelRow = null;
                deletedModelRows.clear();
                pnDeletedInvModelYrRow = null;
                deletedMdlYrRows.clear();
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
     * Validate data before saving.
     * @return {@code true} if data is valid, {@code false} otherwise.
     * @throws SQLException 
     */
    private Boolean isEntryOK() throws SQLException{
        poMaster.first();

        if (poMaster.getString("sBarCodex").isEmpty()){
            psMessage = "Part Number is not set.";
            return false;
        }
        
        if (poMaster.getString("sDescript").isEmpty()){
            psMessage = "Part Description is not set.";
            return false;
        }
        
        if (poMaster.getString("sBriefDsc").isEmpty()){
            psMessage = "Part Brief Description is not set.";
            return false;
        }
        
        if (poMaster.getString("sBrandCde").isEmpty()){
            psMessage = "Brand is not set.";
            return false;
        }
        
        if (poMaster.getString("sInvTypCd").isEmpty()){
            psMessage = "Inventory Type is not set.";
            return false;
        }
        
        if (poMaster.getString("sCategCd1").isEmpty()){
            psMessage = "Category is not set.";
            return false;
        }
        
        if (poMaster.getString("sMeasurID").isEmpty()){
            psMessage = "Measurement is not set.";
            return false;
        }
        
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sTrimBCde = " + SQLUtil.toSQL((poMaster.getString("sBarCodex")).replaceAll("\\s", ""))  + //poMaster.getString("sTrimBCde")
                                                " AND a.sStockIDx <> " + SQLUtil.toSQL(poMaster.getString("sStockIDx"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Part Number.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        return true;
    }
    
    private String getSQ_Master() {
        return " SELECT "
                    + " a.sStockIDx " //1
                    + " ,a.sBarCodex "//2
                    + " ,a.sDescript "//3
                    + " , IFNULL(a.sBriefDsc, '') sBriefDsc "//4                    
                    + " , IFNULL(a.sCategCd1, '') sCategCd1 "//5
                    + " , IFNULL(a.sCategCd2, '') sCategCd2 "//6
                    + " , IFNULL(a.sCategCd3, '') sCategCd3 "//7
                    + " , IFNULL(a.sCategCd4, '') sCategCd4 "//8
                    + " , IFNULL(a.sBrandCde, '') sBrandCde "//9
                    + " , IFNULL(a.sModelCde, '') sModelCde "//10
                    + " , IFNULL(a.sMeasurID, '') sMeasurID "//11
                    + " , IFNULL(a.sInvTypCd, '') sInvTypCd "//12
                    + " ,a.nUnitPrce "//13
                    + " ,a.nSelPrice "//14
                    + " ,a.nDiscLev1 "//15
                    + " ,a.nDiscLev2 "//16
                    + " ,a.nDiscLev3 "//17
                    + " ,a.nDealrDsc "//18
                    + " , IFNULL(a.cComboInv, '') cComboInv "//19
                    + " , IFNULL(a.cWthPromo, '') cWthPromo "//20
                    + " , IFNULL(a.cUnitType, '') cUnitType "//21
                    + " , IFNULL(a.cInvStatx, '') cInvStatx "//22
                    + " , IFNULL(a.cGenuinex, '') cGenuinex "//23
                    + " , IFNULL(a.cReplacex, '') cReplacex "//24
                    + " , IFNULL(a.sSupersed, '') sSupersed "//25
                    + " , IFNULL(a.sFileName, '') sFileName "//26
                    + " , IFNULL(a.sTrimBCde, '') sTrimBCde "//27
                    + " , IFNULL(a.cRecdStat, '') cRecdStat "//28
                    + " , IFNULL(a.sModified, '') sModified "//29
                    + " ,a.dModified "//30
                    + " ,a.dTimeStmp "//31
                    + " , IFNULL(b.sDescript, '') sBrandNme "//32
                    + " , IFNULL(c.sDescript, '') sCategNme "//33
                    + " , IFNULL(d.sMeasurNm, '') sMeasurNm "//34
                    + " , IFNULL(e.sDescript, '') sInvTypNm "//35
                    + " , IFNULL(f.sLocatnID, '') sLocatnID "//36
                    + " , IFNULL(g.sLocatnDs, '') sLocatnDs "//37
                    + " FROM inventory a "
                    + " LEFT JOIN brand b ON b.sBrandCde = a.sBrandCde  "
                    + " LEFT JOIN inventory_category c ON c.sCategrCd = a.sCategCd1 "
                    + " LEFT JOIN measure d ON d.sMeasurID = a.sMeasurID   "
                    + " LEFT JOIN inv_type e ON e.sInvTypCd = a.sInvTypCd  "
                    + " LEFT JOIN inv_master f on f.sStockIDx = a.sStockIDx  "
                    + " LEFT JOIN item_location g on g.sLocatnID = f.sLocatnID  ";		
    }
    
    /********************ITEM ENTRY MODEL**********************/
    private String getInv_model(){   
        return    " SELECT " 
                + "  IFNULL(a.sStockIDx,'') sStockIDx " //1	
                + " ,a.nEntryNox " //2	
                + " ,IFNULL(a.sModelCde,'') sModelCde " //3
                + " ,a.dTimeStmp " //4
                + " ,IFNULL(d.sMakeDesc,'') sMakeDesc " //5
                + " ,IFNULL(c.sModelDsc,'') sModelDsc " //6
                + " FROM inventory_model a " 
                + " LEFT JOIN vehicle_model c ON c.sModelIDx = a.sModelCde "
                + " LEFT JOIN vehicle_make d ON d.sMakeIDxx = c.sMakeIDxx " ;
    } 
   
    private String getInv_modelYear(){    
        return " SELECT "
               + "   IFNULL(a.sStockIDx,'') sStockIDx " //1
               + " , IFNULL(a.sModelCde,'') sModelCde " //2
               + " , a.nYearModl" //3
               + " , IFNULL(c.sMakeDesc,'') sMakeDesc " //4
               + " , IFNULL(b.sModelDsc,'') sModelDsc " //5
               + " FROM inventory_model_year a "
               + " LEFT JOIN vehicle_model b ON b.sModelIDx = a.sModelCde "
               + " LEFT JOIN vehicle_make c ON c.sMakeIDxx = b.sMakeIDxx ";
    }
    
    /**
    ***Loads the inventory model and its corresponding model years based on the given stock ID.
    *@param fsValue The stock ID of the inventory model to be loaded.
    *@return {@code true} if the inventory model and model years were successfully loaded, {@code false} otherwise.
    */
    public boolean loadInvModel_year(String fsValue){
        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }
            
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //Load Inventory Model
            lsSQL = MiscUtil.addCondition(getInv_model(), "a.sStockIDx = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poInvModel = factory.createCachedRowSet();
            poInvModel.populate(loRS);
            MiscUtil.close(loRS);
            
            //Load Inventory Model Year
            lsSQL = MiscUtil.addCondition(getInv_modelYear(), "a.sStockIDx = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poInvModelYear = factory.createCachedRowSet();
            poInvModelYear.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        return true;
    }
    
    /**
    ***Adds an inventory model or inventory model year to the database.
    *@param fsModelCode The code of the inventory model to be added.
    *@param fsModelDesc The description of the inventory model to be added.
    *@param fsMakeDesc The description of the make of the inventory model to be added.
    *@param fnYear The year of the model to be added (applicable if fbIsModelOnly is false).
    *@param fbIsModelOnly {@code true} if only the inventory model is to be added, {@code false} if the model year is to be added.
    *@return {@code true} if the inventory model or model year was successfully added, {@code false} otherwise.
    */
    public boolean addInvModel_Year(String fsModelCode, String fsModelDesc, String fsMakeDesc, Integer fnYear, boolean fbIsModelOnly){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory;
            int lnCtr;
            
            if (fsModelCode.equals("")){
                psMessage =  "Please select Vehicle Model." ;
                return false;
            }
            
            for (lnCtr = 1; lnCtr <= getInvModelCount(); lnCtr++){
                if (fsModelDesc.equals("COMMON") && getInvModel(lnCtr,"sModelDsc").equals("COMMON") ){
                    psMessage =  "COMMON already exist." ;
                    return false;
                }  
            }
            
            if (fsModelDesc.equals("COMMON") && (getInvModelCount() >= 1 || getInvModelYrCount() >= 1)){
                psMessage =  "Cannot add a common vehicle model when other models exist in Inventory Model/Year." ;
                return false;
            }
            
            if (fsModelDesc.equals("COMMON") && !fnYear.equals(0) ){
                psMessage =  "Cannot add a common vehicle model with Year Model." ;
                return false;
            }
            
            if(fbIsModelOnly){
                //Validate Model
                for (lnCtr = 1; lnCtr <= getInvModelCount(); lnCtr++){
                    
                    if (fsModelCode.equals(getInvModel(lnCtr,"sModelCde"))){
                        psMessage = "Skipping, Failed to add Vehicle Model " + fsModelDesc + " already exist.";
                        return false;
                    }
                    
                    if (getInvModel(lnCtr,"sModelDsc").equals("COMMON")){
                        psMessage = "You cannot add other vehicle model";
                        return false;
                    }
                }
                
                for (lnCtr = 1; lnCtr <= getInvModelYrCount(); lnCtr++){
                    if (fsModelCode.equals(getInvModelYr(lnCtr,"sModelCde"))){
                        psMessage = "Skipping, Failed to add Vehicle Model " + fsModelDesc + " already exist with Year Model.";
                        return false;
                    }
                }
                
                if (poInvModel == null) {
                    lsSQL = MiscUtil.addCondition(getInv_model(), "0=1");
                    loRS = poGRider.executeQuery(lsSQL);
                    factory = RowSetProvider.newFactory();
                    poInvModel = factory.createCachedRowSet();
                    poInvModel.populate(loRS);
                    MiscUtil.close(loRS);
                }
                poInvModel.last();
                poInvModel.moveToInsertRow();
                MiscUtil.initRowSet(poInvModel);
                poInvModel.updateString("sModelCde", fsModelCode);
                poInvModel.updateString("sModelDsc", fsModelDesc);
                poInvModel.updateString("sMakeDesc", fsMakeDesc);
                poInvModel.insertRow();
                poInvModel.moveToCurrentRow();
            } else {
                //Validate Model Year
                for (lnCtr = 1; lnCtr <= getInvModelYrCount(); lnCtr++){
                    if (fsModelCode.equals(getInvModelYr(lnCtr,"sModelCde")) 
                        && fnYear.equals(getInvModelYr(lnCtr,"nYearModl"))){
                        psMessage = "Skipping, Failed to add Vehicle Model " + fsModelDesc + " - " + String.valueOf(fnYear) + " already exist.";
                        return false;
                    }
                }
                
                for (lnCtr = 1; lnCtr <= getInvModelCount(); lnCtr++){
                    if (fsModelCode.equals(getInvModel(lnCtr,"sModelCde"))){
                        psMessage = "Skipping, Failed to add Year Model " + fsModelDesc + " - " + String.valueOf(fnYear) + " already exist without Year Model.";
                        return false;
                    }
                    
                    if (getInvModel(lnCtr,"sModelDsc").equals("COMMON")){
                        psMessage = "You cannot add other vehicle model";
                        return false;
                    } 
                }
                
                if (poInvModelYear == null) {
                    lsSQL = MiscUtil.addCondition(getInv_modelYear(), "0=1");
                    loRS = poGRider.executeQuery(lsSQL);
                    factory = RowSetProvider.newFactory();
                    poInvModelYear = factory.createCachedRowSet();
                    poInvModelYear.populate(loRS);
                    MiscUtil.close(loRS);
                }
                poInvModelYear.last();
                poInvModelYear.moveToInsertRow();
                MiscUtil.initRowSet(poInvModelYear);
                poInvModelYear.updateString("sModelDsc", fsModelDesc);
                poInvModelYear.updateString("sMakeDesc", fsMakeDesc);
                poInvModelYear.updateString("sModelCde", fsModelCode);
                poInvModelYear.updateInt("nYearModl", fnYear);
                poInvModelYear.insertRow();
                poInvModelYear.moveToCurrentRow();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ItemEntry.class.getName()).log(Level.SEVERE, null, ex);
        }
        saveState(toJSONString());
        return true;
    }
    
    /**
    * Removes selected inventory models and model years from the cache and database.
    * 
    * @param fnRowModel An array of row indices representing the inventory models to be removed.
    * @param fnRowModelYr An array of row indices representing the inventory model years to be removed.
    * @return {@code true} if the removal was successful, {@code false} otherwise.
    */
    public boolean removeInvModel_Year(Integer fnRowModel[], Integer fnRowModelYr[]) {
        try {
            
            if (getInvModelYrCount() == 0 && getInvModelCount() == 0) {
                psMessage = "No Vehicle Model / Year to delete.";
                return false;
            }
            
            int nRow = 0;
            int lsModelYr = 0;
            String lsModelCde = "";
            String lsFind = "";
            if(fnRowModel.length != 0 && fnRowModel != null){ 
                //Delete Inventory Model
                Arrays.sort(fnRowModel, Collections.reverseOrder());
                for (int lnCtr : fnRowModel) {
                    poInvModel.absolute(lnCtr);
                    lsFind = poInvModel.getString("sStockIDx");
                    if (lsFind != null && !lsFind.isEmpty()) {
                        lsModelCde = poInvModel.getString("sModelCde");
            
                        if (!lsModelCde.isEmpty()){
                            for (int lnRow = 1; lnRow <= getOrigModelCount(); lnRow++){
                                if (lsModelCde.equals((String) getOrigModelDetail(lnRow,"sModelCde"))){
                                    nRow = lnRow;
                                    break;
                                }
                            }
                        }
                        deletedModelRows.add(nRow);
                    }
                    poInvModel.deleteRow();
                    System.out.println("Removed model success");
                }
            }
            nRow = 0;
            lsModelCde = "";
            lsFind = "";
            //Delete Inventory Model Year
            if(fnRowModelYr.length != 0 && fnRowModelYr != null){
                Arrays.sort(fnRowModelYr, Collections.reverseOrder());
                for (int lnCtr : fnRowModelYr) {
                    poInvModelYear.absolute(lnCtr);
                    lsFind = poInvModelYear.getString("sStockIDx");
                    if (lsFind != null && !lsFind.isEmpty()) {
                        lsModelCde = poInvModelYear.getString("sModelCde");
                        lsModelYr = poInvModelYear.getInt("nYearModl");
            
                        if (!lsModelCde.isEmpty()){
                            for (int lnRow = 1; lnRow <= getOrigModelYrCount(); lnRow++){
                                if (lsModelCde.equals((String) getOrigModelYrDetail(lnRow,"sModelCde")) &&
                                    lsModelYr == (int) getOrigModelYrDetail(lnRow,"nYearModl")){
                                    nRow = lnRow;
                                    break;
                                }
                            }
                        }
                        
                        deletedMdlYrRows.add(nRow);
                    }
                    poInvModelYear.deleteRow();
                    System.out.println("Removed model year success");
                }
            }
            
//            if(fnRowModel.length != 0 && fnRowModel != null){ 
//                //Delete Inventory Model
//                Arrays.sort(fnRowModel, Collections.reverseOrder());
//                for (int lnCtr : fnRowModel) {
//                    poInvModel.absolute(lnCtr);
//                    String lsFind = poInvModel.getString("sStockIDx");
//                    if (lsFind != null && !lsFind.isEmpty()) {
//                        deletedRows.add(lnCtr);
//                    }
//                    poInvModel.deleteRow();
//                    System.out.println("success");
//                }
//                pnDeletedInvModelRow = deletedRows.toArray(new Integer[deletedRows.size()]);
//                deletedRows.clear();
//            }
//            
//            //Delete Inventory Model Year
//            if(fnRowModelYr.length != 0 && fnRowModelYr != null){
//                Arrays.sort(fnRowModelYr, Collections.reverseOrder());
//                for (int lnCtr : fnRowModelYr) {
//                    poInvModelYear.absolute(lnCtr);
//                    String lsFind = poInvModelYear.getString("sStockIDx");
//                    if (lsFind != null && !lsFind.isEmpty()) {
//                        deletedRows.add(lnCtr);
//                    }
//                    poInvModelYear.deleteRow();
//                    System.out.println("success");
//                }
//                pnDeletedInvModelYrRow = deletedRows.toArray(new Integer[deletedRows.size()]);
//                deletedRows.clear();
//            }
            
            return true;
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
    }
    
    /**
     * Clears the Inventory Model Year from the data.
     * This method removes Inventory Model Year from the dataset.  
     * @return
     * @throws SQLException 
     */
    public boolean clearInvModelYr() throws SQLException {
        if (getInvModelYrCount() > 0) {
            poInvModelYear.beforeFirst();
            while (poInvModelYear.next()) {
                poInvModelYear.deleteRow();
            }
        }
        return true;
    }
    /**
     * Clears the Inventory Model from the data.
     * This method removes Inventory Model from the dataset.  
     * @return
     * @throws SQLException 
     */
    public boolean clearInvModel() throws SQLException {
        if (getInvModelYrCount() > 0) {
            poInvModel.beforeFirst();
            while (poInvModel.next()) {
                poInvModel.deleteRow();
            }
        }
        return true;
    }
    
    //------------------------------Vehicle Model YEAR-----------------------------
//    //Vehicle Model Year Setter
//    public void setInvModelYr(int fnRow, int fnIndex, Object foValue) throws SQLException {
//        poInvModelYear.absolute(fnRow);
//        switch (fnIndex) {
//            case 1://sStockIDx
//            case 2://sModelCde
//            case 6://sMakeDesc
//            case 7://sModelDsc
//                poInvModelYear.updateObject(fnIndex, (String) foValue);
//                poInvModelYear.updateRow();
//
//                if (poCallback != null) {
//                    poCallback.onSuccess(fnIndex, getInvModelYr(fnIndex));
//                }
//                break;
//            case 3://nYearModl
//                if (foValue instanceof Integer) {
//                    poInvModelYear.updateInt(fnIndex, (int) foValue);
//                } else {
//                    poInvModelYear.updateInt(fnIndex, 0);
//                }
//
//                poInvModelYear.updateRow();
//                if (poCallback != null) {
//                    poCallback.onSuccess(fnIndex, getInvModelYr(fnIndex));
//                }
//                break;
//        }
//    }

//    public void setInvModelYr(int fnRow, String fsIndex, Object foValue) throws SQLException {
//        setInvModelYr(fnRow, MiscUtil.getColumnIndex(poInvModelYear, fsIndex), foValue);
//    }
    
    //Vehicle Model getter
    public Object getInvModelYr(String fsIndex) throws SQLException {
        return getInvModelYr(MiscUtil.getColumnIndex(poInvModelYear, fsIndex));
    }

    public Object getInvModelYr(int fnIndex) throws SQLException {
        poInvModelYear.first();
        return poInvModelYear.getObject(fnIndex);
    }

    public Object getInvModelYr(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }
        poInvModelYear.absolute(fnRow);
        return poInvModelYear.getObject(fnIndex);
    }

    public Object getInvModelYr(int fnRow, String fsIndex) throws SQLException {
        return getInvModelYr(fnRow, MiscUtil.getColumnIndex(poInvModelYear, fsIndex));
    }

    //get rowcount of Vehicle Model
    public int getInvModelYrCount() throws SQLException {
        try {
            if (poInvModelYear != null) {
                poInvModelYear.last();
                return poInvModelYear.getRow();
            } else {
                return 0;
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return 0;
        }
    }

    //Vehicle Model getter
    public Object getInvModel(String fsIndex) throws SQLException {
        return getInvModel(MiscUtil.getColumnIndex(poInvModel, fsIndex));
    }

    public Object getInvModel(int fnIndex) throws SQLException {
        poInvModel.first();
        return poInvModel.getObject(fnIndex);
    }

    public Object getInvModel(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }
        poInvModel.absolute(fnRow);
        return poInvModel.getObject(fnIndex);
    }

    public Object getInvModel(int fnRow, String fsIndex) throws SQLException {
        return getInvModel(fnRow, MiscUtil.getColumnIndex(poInvModel, fsIndex));
    }

    //get rowcount of Vehicle Model
    public int getInvModelCount() throws SQLException {
        try {
            if (poInvModel != null) {
                poInvModel.last();
                return poInvModel.getRow();
            } else {
                return 0;
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return 0;
        }
    }
    /**
    ***Loads the VEHICLE MODEL YEAR into the CachedRowSet poVhclModelYear.
    *The vehicle model years are generated from the current year down to the year 1900.
    *@return {@code true} if the vehicle model years were successfully loaded, {@code false} otherwise.
    */
    public boolean loadVhclModelYr() {
        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            if (poVhclModelYear == null){
                lsSQL = MiscUtil.addCondition(getInv_modelYear(), "0=1");
                loRS = poGRider.executeQuery(lsSQL);
                poVhclModelYear = factory.createCachedRowSet();
                poVhclModelYear.populate(loRS);
                MiscUtil.close(loRS);
            
                Date serverDate = poGRider.getServerDate();
                java.util.Date utilDate = new java.util.Date(serverDate.getTime());
                LocalDate localDate = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int lnYear = localDate.getYear();
                for (int lnCtr = lnYear; lnCtr >= 1900; lnCtr--){
                    poVhclModelYear.last();
                    poVhclModelYear.moveToInsertRow();
                    MiscUtil.initRowSet(poVhclModelYear);
                    poVhclModelYear.updateInt("nYearModl", lnCtr);
                    poVhclModelYear.insertRow();
                    poVhclModelYear.moveToCurrentRow();
                }
            }
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        return true;
    }
    
    /********VEHICLE MODEL********/
    private String getSQ_VhclModel(){
        return " SELECT " 
               + " a.sModelIDx " //1
               + " , a.sModelDsc " //2
               + " , a.sMakeIDxx " //3
               + " , b.sMakeDesc " //4
               + " FROM  vehicle_model a "
               + " LEFT JOIN vehicle_make b ON a.sMakeIDxx = b.sMakeIDxx "
               + " ORDER BY b.sMakeDesc ASC ";
    }
    
    /**
    ***Loads the VEHICLE MODEL into the CachedRowSet poVhclModel.
    *@return {@code true} if the vehicle models were successfully loaded, {@code false} otherwise.
    */
    public boolean loadVhclModel() {
        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            lsSQL = getSQ_VhclModel();
            loRS = poGRider.executeQuery(lsSQL);
            poVhclModel = factory.createCachedRowSet();
            poVhclModel.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        return true;
    }
    
    public int getVhclModelCount() throws SQLException{
        if (poVhclModel != null){
            poVhclModel.last();
            return poVhclModel.getRow();
        } else {
            return 0;
        }
    }
    
    public Object getVhclModel(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }
        poVhclModel.absolute(fnRow);
        return poVhclModel.getObject(fnIndex);
    }

    public Object getVhclModel(int fnRow, String fsIndex) throws SQLException {
        if (getVhclModelCount() == 0 || fnRow > getVhclModelCount()) {
            return null;
        }
        return getVhclModel(fnRow, MiscUtil.getColumnIndex(poVhclModel, fsIndex));
    }
    
    public int getVhclModelYrCount() throws SQLException{
        if (poVhclModelYear != null){
            poVhclModelYear.last();
            return poVhclModelYear.getRow();
        } else {
            return 0;
        }
    }
    
    public Object getVhclModelYr(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }
        poVhclModelYear.absolute(fnRow);
        return poVhclModelYear.getObject(fnIndex);
    }

    public Object getVhclModelYr(int fnRow, String fsIndex) throws SQLException {
        if (getVhclModelYrCount() == 0 || fnRow > getVhclModelYrCount()) {
            return null;
        }
        return getVhclModelYr(fnRow, MiscUtil.getColumnIndex(poVhclModelYear, fsIndex));
    }
    
    /******************ITEM ENTRY PARAMETER***************/
    private String getSQ_Brand(){    
        return " SELECT "
                + " sBrandCde " //1	
                + " ,sInvTypCd " //2 	
                + " ,sDescript " //3	
                + " ,cRecdStat " //4	
                + " ,sModified " //5	
                + " ,dModified " //6
            + " FROM brand ";
    }
    
    /**
     * Searches for a brand based on the specified value.
     * This method performs a search for a brand by its name. It allows both UI and non-UI search modes and provides feedback if no records are found.
     * @param fsValue The brand name.
     * @return
     * @throws SQLException 
     */
    public boolean searchBrand(String fsValue) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getSQ_Brand(), " sDescript LIKE " + SQLUtil.toSQL(fsValue + "%"));            
      
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sBrandCde", loRS.getString("sBrandCde"));
                setMaster("sBrandNme", loRS.getString("sDescript"));               
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
                                                        "Description", 
                                                        "sDescript",
                                                        "sDescript",                                                        
                                                        0);            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sBrandCde", (String) loJSON.get("sBrandCde"));
                setMaster("sBrandNme", (String) loJSON.get("sDescript"));                
            }
        }        
        return true;
    }
    
    private String getSQ_Measure(){
         return " SELECT "  
                    + " sMeasurID  "
                    + " ,sMeasurNm "
                    + " ,sShortDsc "
                    + " ,cRecdStat "
                    + " ,sModified "
                    + " ,dModified "
                    + " ,dTimeStmp "
                + " FROM measure ";

    }
    /**
     * Searches for a measure based on the specified value.
     * This method performs a search for a measure by its name. It allows both UI and non-UI search modes and provides feedback if no records are found.
     * @param fsValue The measure name.
     * @return
     * @throws SQLException 
     */
    public boolean searchMeasure(String fsValue) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getSQ_Measure(), " sMeasurNm LIKE " + SQLUtil.toSQL(fsValue + "%"));            
      
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sMeasurNm", loRS.getString("sMeasurNm"));
                setMaster("sMeasurID", loRS.getString("sMeasurID"));               
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        getSQ_Measure(), 
                                                        "%" + fsValue + "%", 
                                                        "Measure ID»Measurement Name", 
                                                        "sMeasurID»sMeasurNm",
                                                        "sMeasurID»sMeasurNm",                                                        
                                                        1);            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sMeasurNm", (String) loJSON.get("sMeasurNm"));
                setMaster("sMeasurID", (String) loJSON.get("sMeasurID"));                
            }
        }        
        return true;
    }
    
//    private String getInv_Master(){
//         return " SELECT " 
//                    + " a.sStockIDx " //1
//                    + ", a.sBranchCd "//2
//                    + ", a.sLocatnID "//3
//                    + ", a.dAcquired "//4
//                    + ", a.dBegInvxx "//5
//                    + ", a.nBegQtyxx "//6
//                    + ", a.nQtyOnHnd "//7
//                    + ", a.nMinLevel "//8
//                    + ", a.nMaxLevel "//9
//                    + ", a.nAvgMonSl "//10
//                    + ", a.nAvgCostx "//11
//                    + ", a.cClassify "//12
//                    + ", a.nBackOrdr "//13
//                    + ", a.nResvOrdr "//14
//                    + ", a.nFloatQty "//15
//                    + ", a.nLedgerNo "//16
//                    + ", a.dLastTran "//17
//                    + ", a.cRecdStat "//18
//                    + ", a.sModified "//19
//                    + ", a.dModified "//20
//                    + ", a.dTimeStmp "//21
//                    + ", IFNULL(b.sLocatnDs, '') sLocatnDs" //22
//                + " FROM inv_master a "
//                + " LEFT JOIN item_location b on b.sLocatnID = a.sLocatnID ";
//    }

//    Commented by Arsiela 08-01-2023 
//    private String getSQ_ItemLocation() {
//        return " SELECT "																            
//                    + " a.sLocatnID, " //1														    
//                    + " a.sLocatnDs, " //2																
//                    + " a.sWHouseID, " //3																
//                    + " a.sSectnIDx, " //4																
//                    + " a.sBinIDxxx, " //5																
//                    + " a.cRecdStat, " //6																
//                    + " b.sWHouseNm, " //7																
//                    + " c.sSectnNme, " //8																
//                    + " d.sBinNamex  " //9													      
//                + " FROM item_location a "														
//                + " LEFT JOIN warehouse b on b.sWHouseID = a.sWHouseID "
//                + " LEFT JOIN section c on c.sSectnIDx = a.sSectnIDx "
//                + " LEFT JOIN bin d on d.sBinIDxxx = a.sBinIDxxx ";		
//    }
//    
//    public boolean searchLocation(String fsValue) throws SQLException{                        
//        String lsSQL = MiscUtil.addCondition(getSQ_ItemLocation(), " sLocatnDs LIKE " + SQLUtil.toSQL(fsValue + "%"));            
//      
//        ResultSet loRS;
//        if (!pbWithUI) {   
//            lsSQL += " LIMIT 1";
//            loRS = poGRider.executeQuery(lsSQL);
//            System.out.println(lsSQL);
//            if (loRS.next()){
//                setMaster("sLocatnDs", loRS.getString("sLocatnDs"));
//                setMaster("sLocatnID", loRS.getString("sLocatnID"));               
//            } else {
//                psMessage = "No record found.";
//                return false;
//            }
//        } else {
//            loRS = poGRider.executeQuery(lsSQL);
//            System.out.println(lsSQL);
//            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
//                                                        lsSQL, 
//                                                        fsValue, 
//                                                        "Location ID»Location", 
//                                                        "sLocatnID»sLocatnDs",
//                                                        "sLocatnID»sLocatnDs",                                                        
//                                                        0);            
//            if (loJSON == null){
//                psMessage = "No record found/selected.";
//                return false;
//            } else {
//                setMaster("sLocatnDs", (String) loJSON.get("sLocatnDs"));
//                setMaster("sLocatnID", (String) loJSON.get("sLocatnID"));                
//            }
//        }        
//        return true;
//    }
    
    private String getInv_Type(){    
        return " SELECT "
                + " sInvTypCd " //1	
                + " ,sDescript " //2 	
                + " ,sItemType " //3	
                + " ,cRecdStat " //4	
                + " ,sModified " //5	
                + " ,dModified " //6	
                + " ,dTimeStmp " //7
            + " FROM inv_type ";
    }
    
    /**
     * Searches for a Inventory Type based on the specified value.
     * This method performs a search for a Inventory Type by its description. It allows both UI and non-UI search modes and provides feedback if no records are found.
     * @param fsValue The Inventory Type description.
     * @return
     * @throws SQLException 
     */
    public boolean searchInvType(String fsValue) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getInv_Type(), " sDescript LIKE " + SQLUtil.toSQL(fsValue + "%"));            
      
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sInvTypNm", loRS.getString("sDescript"));
                setMaster("sInvTypCd", loRS.getString("sInvTypCd"));               
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
                                                        "Inv Type ID»Description", 
                                                        "sInvTypCd»sDescript",
                                                        "sInvTypCd»sDescript",                                                        
                                                        0);            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sInvTypNm", (String) loJSON.get("sDescript"));
                setMaster("sInvTypCd", (String) loJSON.get("sInvTypCd"));                
            }
        }        
        return true;
    }
    
    private String getInv_Category(){    
        return " SELECT " 
                    + " sCategrCd " //1	
                    + " ,sDescript "//2	
                    + " ,sInvTypCd "//3	
                    + " ,cRecdStat "//4	
                    + " ,sModified "//5	
                    + " ,dModified "//6	
                    + " ,dTimeStmp "//7
                + "  FROM inventory_category ";
    }
    
    /**
     * Searches for a Inventory Category based on the specified value.
     * This method performs a search for a Inventory Category by its description. It allows both UI and non-UI search modes and provides feedback if no records are found.
     * @param fsValue The Inventory Category description.
     * @param fsType The Inventory Type description.
     * @return
     * @throws SQLException 
     */
    public boolean searchInvCategory(String fsValue, String fsType) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getInv_Category(), " sDescript LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                                    " AND sInvTypCd = " + SQLUtil.toSQL(fsType));            
        System.out.println(lsSQL);
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sCategNme", loRS.getString("sDescript"));
                setMaster("sCategCd1", loRS.getString("sCategrCd"));               
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
                                                        "Category ID»Description", 
                                                        "sCategrCd»sDescript",
                                                        "sCategrCd»sDescript",                                                        
                                                        0);            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sCategNme", (String) loJSON.get("sDescript"));
                setMaster("sCategCd1", (String) loJSON.get("sCategrCd"));                
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
    
}
