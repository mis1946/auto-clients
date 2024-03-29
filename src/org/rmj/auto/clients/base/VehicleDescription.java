/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.clients.base;

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
import org.rmj.auto.json.TabsStateManager;

/**
 *
 * @author User
 */
public class VehicleDescription {
    private final String MASTER_TABLE = "vehicle_master";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autoapp_json/" + TabsStateManager.getJsonFileName("Vehicle Description");
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poVehicle;
    private CachedRowSet poVehicleDetail;
    
    public VehicleDescription(GRider foGRider, String fsBranchCd, boolean fbWithParent){
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
    
    public int getDetailCount() throws SQLException{
        poVehicleDetail.last();
        return poVehicleDetail.getRow();
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
            Logger.getLogger(VehicleDescription.class.getName()).log(Level.SEVERE, null, ex);
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
            int lnCtr = 1;
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
            Logger.getLogger(VehicleDescription.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poVehicle.first();
        
        switch (fnIndex){            
            case 2://sDescript
            case 3://sMakeIDxx
            case 4://sModelIDx
            case 5://sColorIDx
            case 6://sTypeIDxx
            case 7://sTransMsn
            case 15://sMakeDesc
            case 16://sModelDsc
            case 17://sColorDsc
            case 18://sTypeDesc
                poVehicle.updateObject(fnIndex, (String) foValue);
                poVehicle.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 8://nYearModl
            case 9://cVhclSize
            case 10://cRecdStat
                if (foValue instanceof Integer)
                    poVehicle.updateInt(fnIndex, (int) foValue);
                else 
                    poVehicle.updateInt(fnIndex, 0);
                
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
    
//    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
//        if (fnIndex == 0) return null;
//        
//        poVehicle.absolute(fnRow);
//        return poVehicle.getObject(fnIndex);
//    }
//    
//    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
//        return getDetail(fnRow, MiscUtil.getColumnIndex(poVehicle, fsIndex));
//    }
    
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVehicleDetail.absolute(fnRow);
        return poVehicleDetail.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poVehicleDetail, fsIndex));
    }
    
    public  String getDescription() throws SQLException {
        String fsDescription;// = fsLname;
        fsDescription = poVehicle.getString("sMakeDesc") + " " + 
                        poVehicle.getString("sModelDsc") + " " +
                        poVehicle.getString("sTypeDesc") + " " +
                        poVehicle.getString("sTransMsn") + " " +
                        poVehicle.getString("sColorDsc") + " " +
                        poVehicle.getString("nYearModl");
//        if (fsFrstNm != null && !fsFrstNm.isEmpty()) {
//            fsFullName += ", " + fsFrstNm;
//        }
//        if (fsSuffix != null && !fsSuffix.isEmpty()) {
//            fsFullName += " " + fsSuffix;
//        }
//        if (fsMiddnm != null && !fsMiddnm.isEmpty()) {
//            fsFullName += " " + fsMiddnm;
//        }
        return fsDescription;
    }
    
    /**
    * Creates a new vehicle record for data entry.
    *
    * @return True if a new record is successfully created, false otherwise.
    * @throws SQLException If a database error occurs.
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
            poVehicle.updateString("cRecdStat", RecordStatus.ACTIVE);      
            poVehicle.updateString("sTransMsn", "AT");   
            poVehicle.updateString("nYearModl", "0");   
            poVehicle.updateString("cVhclSize", "0");   
            //poVehicle.updateObject("dBirthDte", poGRider.getServerDate());
            
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
    * Searches for a vehicle record based on the given criteria.
    *
    * @param fsValue  The search criteria (either code or description).
    * @param fbByCode True if searching by code, false if searching by description.
    * @return True if a matching record is found and opened, false otherwise.
    * @throws SQLException If a database error occurs.
    */
    public boolean SearchRecord(String fsValue, boolean fbByCode) throws SQLException{
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
                                "Vehicle Description»Make»Model",
                                "sDescript»sMakeDesc»sModelDsc",
                                "a.sDescript»sMakeDesc»sModelDsc",
                                fbByCode ? 0 : 1);
            if (loJSON != null)
                return OpenRecord((String) loJSON.get("sVhclIDxx"));
            else{
                psMessage = "No record selected.";
                return false;
            }                                        
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL,"a.sVhclIDxx = " + SQLUtil.toSQL(fsValue));
        else {
            if (!fsValue.isEmpty()){
                lsSQL = MiscUtil.addCondition(lsSQL, "a.sDescript LIKE " + SQLUtil.toSQL(fsValue + "%"));
            }
        }            
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sVhclIDxx");
        MiscUtil.close(loRS);
        
        //return OpenRecord(lsSQL, true);
        return OpenRecord(lsSQL);
                     
    }
    
    //for autoloading list of vehicle descriptions
    /**
    * Loads a list of vehicle details based on the given description.
    *
    * @param fsDescript The description to filter the list.
    * @return True if the list is successfully loaded, false otherwise.
    * @throws SQLException If a database error occurs.
    */
    public boolean LoadList(String fsDescript) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL = getSQ_Master() + " ORDER BY sMakeDesc, nYearModl DESC ";
        System.out.println(lsSQL);
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        loRS = poGRider.executeQuery(lsSQL);
        poVehicleDetail = factory.createCachedRowSet();
        poVehicleDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    /**
    * Opens an existing vehicle record for editing based on the provided identifier.
    *
    * @param fsValue The unique identifier of the vehicle record to open.
    * @return True if the record is successfully opened, false otherwise.
    * @throws SQLException If a database error occurs.
    */
    public boolean OpenRecord(String fsValue){
        if (poVehicleDetail == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sVhclIDxx = " + SQLUtil.toSQL(fsValue));
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
        saveState(toJSONString());
        return true;
    }    
    
    /**
    * Sets the edit mode to UPDATE for the current record.
    *
    * @return True to indicate that the edit mode has been set to UPDATE.
    */
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        saveState(toJSONString());
        return true;        
    }
    /**
    * Saves the current vehicle record, either by adding a new record or updating an existing one.
    *
    * @return True if the record is successfully saved, false otherwise.
    * @throws SQLException If a database error occurs.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            
            String lsSQL = "";
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                poVehicle.updateString("sVhclIDxx", MiscUtil.getNextCode(MASTER_TABLE, "sVhclIDxx", true, poGRider.getConnection(), psBranchCd));                                                             
                poVehicle.updateString("sDescript", getDescription());
                poVehicle.updateString("sEntryByx", poGRider.getUserID());
                poVehicle.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, MASTER_TABLE, "sMakeDesc»sModelDsc»sColorDsc»sTypeDesc");
            } else { //update         
                poVehicle.updateString("sDescript", getDescription());
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, 
                                            MASTER_TABLE, 
                                            "sMakeDesc»sModelDsc»sColorDsc»sTypeDesc", 
                                            "sVhclIDxx = " + SQLUtil.toSQL((String) getMaster("sVhclIDxx")));
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
                    " a.sVhclIDxx" +  //1
                    ", a.sDescript" + //2 
                    ", a.sMakeIDxx" + //3  
                    ", a.sModelIDx" + //4  
                    ", a.sColorIDx" + //5  
                    ", a.sTypeIDxx" + //6  
                    ", a.sTransMsn" + //7  
                    ", a.nYearModl" + //8  
                    ", a.cVhclSize" + //9  
                    ", a.cRecdStat" + //10  
                    ", a.sEntryByx" + //11  
                    ", a.dEntryDte" + //12  
                    ", a.sModified" + //13  
                    ", a.dModified" + //14  
                    ", IFNULL(c.sMakeDesc, '') sMakeDesc" + //15 
                    ", IFNULL(b.sModelDsc, '') sModelDsc" + //16 
                    ", IFNULL(d.sColorDsc, '') sColorDsc" + //17
                    ", IFNULL(e.sTypeDesc, '') sTypeDesc" + //18
                " FROM " + MASTER_TABLE + " a " +
                    " LEFT JOIN vehicle_model b ON a.sModelIDx = b.sModelIDx " +
                    " LEFT JOIN vehicle_make c ON a.sMakeIDxx = c.sMakeIDxx " +
                    " LEFT JOIN vehicle_color d ON a.sColorIDx = d.sColorIDx " +
                    " LEFT JOIN vehicle_type e on a.sTypeIDxx = e.sTypeIDxx ";                   
    } 
    
    private String getSQ_VehicleMake(){
        return "SELECT" +
                    " sMakeIDxx" +
                    ", sMakeDesc" +
                    ", sMakeCode" +
                    ", cRecdStat" +
                    ", sEntryByx" +
                    ", dEntryDte" +
                    ", sModified" +
                    ", dModified" +
                " FROM vehicle_make " ;
    }
    
    //for searching vehicle make when f3 is pressed
    /**
    * Searches for a vehicle make based on the provided search value.
    *
    * @param fsValue The search value to filter vehicle makes.
    * @return True if a matching make is found, sets the make details, and returns true. Otherwise, sets an error message and returns false.
    * @throws SQLException If there's an error in executing SQL queries.
    */
    public boolean searchVehicleMake(String fsValue) throws SQLException{
        String lsSQL = getSQ_VehicleMake();
        
        
        lsSQL = MiscUtil.addCondition(lsSQL, "sMakeDesc LIKE " + SQLUtil.toSQL(fsValue + "%"));
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sMakeIDxx", loRS.getString("sMakeIDxx"));
                setMaster("sMakeDesc", loRS.getString("sMakeDesc"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Make", "sMakeDesc");
            JSONObject loJSON = showFXDialog.jsonSearch(
                                poGRider, 
                                lsSQL,
                                fsValue ,
                                "Vehicle Make",
                                "sMakeDesc",
                                "sMakeDesc",
                                0);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sMakeIDxx", (String) loJSON.get("sMakeIDxx"));
                setMaster("sMakeDesc", (String) loJSON.get("sMakeDesc"));
            }
        }        
        return true;
    }
        
    private String getSQ_VehicleModel(){
        return "SELECT" +
                    " a.sModelIDx" +
                    ", a.sModelDsc" +
                    ", a.sMakeIDxx" +
                    ", a.sUnitType" +
                    ", a.sBodyType" +
                    ", a.cVhclSize" +
                    ", a.sModelCde" +
                    ", a.cRecdStat" +
                    ", a.sEntryByx" +
                    ", a.dEntryDte" +
                    ", a.sModified" +
                    ", a.dModified" +
                " FROM  vehicle_model a " + 
                " LEFT JOIN vehicle_make b ON a.sMakeIDxx = b.sMakeIDxx ";
    }
    
    //for searching vehicle model when f3 is pressed
    /**
    * Searches for a vehicle model based on the provided search value and the selected make.
    *
    * @param fsValue The search value to filter vehicle models.
    * @return True if a matching model is found, sets the model details, and returns true. Otherwise, sets an error message and returns false.
    * @throws SQLException If there's an error in executing SQL queries.
    */
    public boolean searchVehicleModel(String fsValue) throws SQLException{
        String lsSQL = getSQ_VehicleModel();
        
        
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sModelDsc LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                "AND a.sMakeIDxx = " + SQLUtil.toSQL(poVehicle.getString("sMakeIDxx")));
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sModelIDx", loRS.getString("sModelIDx"));
                setMaster("sModelDsc", loRS.getString("sModelDsc"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Model", "sModelDsc");
            JSONObject loJSON = showFXDialog.jsonSearch(
                                poGRider, 
                                lsSQL,
                                fsValue ,
                                "Vehicle Model",
                                "sModelDsc",
                                "a.sModelDsc",
                                0);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sModelIDx", (String) loJSON.get("sModelIDx"));
                setMaster("sModelDsc", (String) loJSON.get("sModelDsc"));
            }
        }        
        return true;
    }
    
    private String getSQ_VehicleColor(){
        return "SELECT" + 
                    " sColorIDx" +
                    ", sColorDsc" +
                    ", sColorCde" +
                    ", cRecdStat" +
                    ", sEntryByx" +
                    ", dEntryDte" +
                    ", sModified" +
                    ", dModified" +
                " FROM vehicle_color ";
    }
    
    //for searching vehicle color when f3 is pressed
    /**
    * Searches for a vehicle color based on the provided search value.
    *
    * @param fsValue The search value to filter vehicle colors.
    * @return True if a matching color is found, sets the color details, and returns true. Otherwise, sets an error message and returns false.
    * @throws SQLException If there's an error in executing SQL queries.
    */
    public boolean searchVehicleColor(String fsValue) throws SQLException{
        String lsSQL = getSQ_VehicleColor();
        
        
        lsSQL = MiscUtil.addCondition(lsSQL, "sColorDsc LIKE " + SQLUtil.toSQL(fsValue + "%"));
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sColorIDx", loRS.getString("sColorIDx"));
                setMaster("sColorDsc", loRS.getString("sColorDsc"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Color", "sColorDsc");
            JSONObject loJSON = showFXDialog.jsonSearch(
                                poGRider, 
                                lsSQL,
                                 fsValue ,
                                "Vehicle Color",
                                "sColorDsc",
                                "sColorDsc",
                                0);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sColorIDx", (String) loJSON.get("sColorIDx"));
                setMaster("sColorDsc", (String) loJSON.get("sColorDsc"));
            }
        }        
        return true;
    }
    
    private String getSQ_VehicleType(){
        return "SELECT" + 
                    " sTypeIDxx" +
                    ", sTypeDesc" +
                    ", sTypeCode" +
                    ", cRecdStat" +
                    ", sEntryByx" +
                    ", dEntryDte" +
                    ", sModified" +
                    ", dModified" +
                " FROM vehicle_type ";
    }
    
    //for searching vehicle type when f3 is pressed
    /**
    * Searches for a vehicle type based on the provided search value.
    *
    * @param fsValue The search value to filter vehicle types.
    * @return True if a matching type is found, sets the type details, and returns true. Otherwise, sets an error message and returns false.
    * @throws SQLException If there's an error in executing SQL queries.
    */
    public boolean searchVehicleType(String fsValue) throws SQLException{
        String lsSQL = getSQ_VehicleType();
                
        lsSQL = MiscUtil.addCondition(lsSQL, " sTypeDesc LIKE " + SQLUtil.toSQL(fsValue + "%"));
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sTypeIDxx", loRS.getString("sTypeIDxx"));
                setMaster("sTypeDesc", loRS.getString("sTypeDesc"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Type", "sTypeDesc");
            JSONObject loJSON = showFXDialog.jsonSearch(
                                poGRider, 
                                lsSQL,
                                fsValue,
                                "Vehicle Type",
                                "sTypeDesc",
                                "sTypeDesc",
                                0);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sTypeIDxx", (String) loJSON.get("sTypeIDxx"));
                setMaster("sTypeDesc", (String) loJSON.get("sTypeDesc"));
            }
        }        
        return true;
    }
    
    /**
    * Checks if the current vehicle record is valid for saving or updating.
    *
    * @return True if the record is valid, otherwise sets an error message and returns false.
    * @throws SQLException If there's an error in executing SQL queries.
    */
    private boolean isEntryOK() throws SQLException{
        poVehicle.first();

        if (poVehicle.getString("sMakeIDxx").isEmpty()){
            psMessage = "Vehicle Make is not set.";
            return false;
        }
        
        if (poVehicle.getString("sModelIDx").isEmpty()){
            psMessage = "Vehicle Model is not set.";
            return false;
        }
        
        if (poVehicle.getString("sColorIDx").isEmpty()){
            psMessage = "Vehicle Color is not set.";
            return false;
        }
        
        if (poVehicle.getString("sTypeIDxx").isEmpty()){
            psMessage = "Vehicle Type is not set.";
            return false;
        }
        
        if (poVehicle.getString("sTransMsn").isEmpty()){
            psMessage = "Vehicle Transmission is not set.";
            return false;
        }
          
        if (poVehicle.getString("nYearModl").isEmpty()){
            psMessage = "Vehicle Year is not set.";
            return false;
        }
        
        if (poVehicle.getString("cVhclSize").isEmpty()){
            psMessage = "Vehicle Size is not set.";
            return false;
        }
        
        String lsSQL = getSQ_Master();
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sMakeIDxx = " + SQLUtil.toSQL(poVehicle.getString("sMakeIDxx")) +
                                                " AND a.sModelIDx = " + SQLUtil.toSQL(poVehicle.getString("sModelIDx")) + 
                                                " AND a.sColorIDx = " + SQLUtil.toSQL(poVehicle.getString("sColorIDx"))+
                                                " AND a.sTypeIDxx = " + SQLUtil.toSQL(poVehicle.getString("sTypeIDxx")) + 
                                                " AND a.nYearModl = " + SQLUtil.toSQL(poVehicle.getString("nYearModl"))+
                                                " AND a.sTransMsn = " + SQLUtil.toSQL(poVehicle.getString("sTransMsn")) +
                                                " AND a.sVhclIDxx <> " + SQLUtil.toSQL(poVehicle.getString("sVhclIDxx"))); 
                                                //" AND a.dBirthDte = " + SQLUtil.toSQL(formattedDate));

        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Vehicle Description.";
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
