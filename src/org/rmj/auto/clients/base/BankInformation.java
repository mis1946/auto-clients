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
public class BankInformation {
    private final String MASTER_TABLE = "Banks";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autoapp_json/" + TabsStateManager.getJsonFileName("Bank");
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psSourceID;
    
    private CachedRowSet poBankInfo;
    private CachedRowSet poBankInfoDetail;
    
    public BankInformation(GRider foGRider, String fsBranchCd, boolean fbWithParent){
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
        poBankInfo.last();
        return poBankInfo.getRow();
    }
    
    public int getDetailCount() throws SQLException{
        poBankInfoDetail.last();
        return poBankInfoDetail.getRow();
    }
    
    public String getSourceID(){
         return psSourceID;         
    }
    
    private String toJSONString(){
        JSONParser loParser = new JSONParser();
        JSONArray laMaster = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;
        
        try {
            loJSON = new JSONObject();
            String lsValue =  CommonUtils.RS2JSON(poBankInfo).toJSONString();
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
            Logger.getLogger(BankInformation.class.getName()).log(Level.SEVERE, null, ex);
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
                    poBankInfo.first();
                    JSONObject masterObject = (JSONObject) jsonObject.get("master");
                    // Add a row to the CachedRowSet with the values from the masterObject
                    for (Object key : masterObject.keySet()) {
                        Object value = masterObject.get(key);
                        if(value == null){
                            tempValue = "";
                        } else {
                            tempValue = String.valueOf(value);
                        }
                        switch(poBankInfo.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                            case Types.CHAR:
                            case Types.VARCHAR:
                                poBankInfo.updateObject(Integer.valueOf(key.toString()), tempValue);
                                break;
                            case Types.DATE:
                            case Types.TIMESTAMP:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = DEFAULT_DATE;
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poBankInfo.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                break;
                            case Types.INTEGER:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = "0";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poBankInfo.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue));
                                break;
                            case Types.DECIMAL:
                            case Types.DOUBLE:
                                if(String.valueOf(tempValue).isEmpty()){
                                    tempValue = "0.00";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poBankInfo.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue));
                                break;
                            default:
                                poBankInfo.updateObject(Integer.valueOf(key.toString()), tempValue);
                                break;
                        }
                        tempValue = "";
                    }
                    poBankInfo.updateRow();
                }
            } else {
                psMessage = "";
                return false;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(BankInformation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poBankInfo.first();
        
        switch (fnIndex){   
            case 2://sBankName
            case 3://sBankCode
            case 4://sContactP
            case 5://sAddressx           
            case 7://sZippCode
            case 8://sTelNoxxx
            case 9://sFaxNoxxx
            case 15://sProvName        
            case 16://sTownProv
            case 17://sBankBrch
            case 6: //sTownIDxx
            case 18://sTownName
            case 19://sProvIDxx
                poBankInfo.updateObject(fnIndex, (String) foValue);
                poBankInfo.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
              
            case 10://cRecdStat
                if (foValue instanceof Integer)
                    poBankInfo.updateInt(fnIndex, (int) foValue);
                else 
                    poBankInfo.updateInt(fnIndex, 0);
                
                poBankInfo.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break;                                                    
        }
        saveState(toJSONString());
    }
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poBankInfo, fsIndex), foValue);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poBankInfo, fsIndex));
    }
    
    public Object getMaster(int fnIndex) throws SQLException{
        poBankInfo.first();
        return poBankInfo.getObject(fnIndex);
    }
    
     public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poBankInfoDetail.absolute(fnRow);
        return poBankInfoDetail.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poBankInfoDetail, fsIndex));
    }           
    /**
     * Initializes the master data for adding a new entry.
     * @return {@code true} if the master data is successfully initialized,
     * {@code false} otherwise
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
            poBankInfo = factory.createCachedRowSet();
            poBankInfo.populate(loRS);
            MiscUtil.close(loRS);
            
            poBankInfo.last();
            poBankInfo.moveToInsertRow();
            
            MiscUtil.initRowSet(poBankInfo);       
            poBankInfo.updateString("cRecdStat", RecordStatus.ACTIVE);      
                     
            poBankInfo.insertRow();
            poBankInfo.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    /**
    * Searches for a bank record based on criteria and retrieves it.
    * This method is used to search for a bank record, either by bank code or bank name criteria. It allows both UI and non-UI search modes and opens the found record if available.
    * @param fsValue The search criteria, which can be a bank code or bank name.
    * @param fbByCode Set to true if searching by bank code, false if searching by bank name.
    * @return True if a bank record is found and successfully opened, otherwise false.
    * @throws SQLException if a database error occurs.
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
                                "Bank Name",
                                "sBankName",
                                "a.sBankName",
                                fbByCode ? 0 : 1);
            if (loJSON != null)
                return OpenRecord((String) loJSON.get("sBankIDxx"));
            else{
                psMessage = "No record selected.";
                return false;
            }                                        
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL,"a.sBankIDxx = " + SQLUtil.toSQL(fsValue));
        else {
            if (!fsValue.isEmpty()){
                lsSQL = MiscUtil.addCondition(lsSQL, "a.sBankName LIKE " + SQLUtil.toSQL(fsValue + "%"));
            }
        }            
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sBankIDxx");
        MiscUtil.close(loRS);
        
        //return OpenRecord(lsSQL, true);
        return OpenRecord(lsSQL);
                     
    }
    
    //for autoloading list of banks
    public boolean LoadList(String fsValue) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        loRS = poGRider.executeQuery(getSQ_Master());
        poBankInfoDetail = factory.createCachedRowSet();
        poBankInfoDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    /**
     *
     * Opens a record with the specified value.
     * @param fsValue the value used to open the record
     * @return {@code true} if the record is successfully opened, {@code false}
     * otherwise
     */  
    public boolean OpenRecord(String fsValue){
        if (poBankInfoDetail == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        psSourceID = fsValue;
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sBankIDxx = " + SQLUtil.toSQL(psSourceID));
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poBankInfo = factory.createCachedRowSet();
            poBankInfo.populate(loRS);
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
    * Prepares the record for update.
    * This method sets the edit mode to UPDATE, indicating that the record is ready for updating.
    * @return True to indicate that the record is prepared for updating.
    */
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        saveState(toJSONString());
        return true;        
    }
    
    /**
    * Saves or updates a bank record.
    * This method is responsible for saving a new bank record (in ADDNEW mode) or updating an existing one (in UPDATE mode). It performs data validation, generates SQL statements, and handles database transactions.
    * @return True if the bank record is successfully saved or updated, otherwise false.
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
                psSourceID =  MiscUtil.getNextCode(MASTER_TABLE, "sBankIDxx", true, poGRider.getConnection(), psBranchCd);
                poBankInfo.updateString("sBankIDxx", psSourceID);//MiscUtil.getNextCode(MASTER_TABLE, "sBankIDxx", true, poGRider.getConnection(), psBranchCd));                                                                             
                poBankInfo.updateString("sEntryByx", poGRider.getUserID());
                poBankInfo.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poBankInfo.updateString("sModified", poGRider.getUserID());
                poBankInfo.updateObject("dModified", (Date) poGRider.getServerDate());
                poBankInfo.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poBankInfo, MASTER_TABLE, "sProvName»sTownName»sTownProv»sProvIDxx");
            } else { //update            
                poBankInfo.updateString("sModified", poGRider.getUserID());
                poBankInfo.updateObject("dModified", (Date) poGRider.getServerDate());
                poBankInfo.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poBankInfo, 
                                            MASTER_TABLE, 
                                            "sProvName»sTownName»sTownProv»sProvIDxx", 
                                            "sBankIDxx = " + SQLUtil.toSQL(psSourceID));//(String) getMaster("sBankIDxx")));
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
                    " a.sBankIDxx" + //1
                    ", a.sBankName" + //2
                    ", a.sBankCode" + //3
                    ", a.sContactP" + //4
                    ", a.sAddressx" + //5
                    ", a.sTownIDxx" + //6
                    ", a.sZippCode" + //7
                    ", a.sTelNoxxx" + //8
                    ", a.sFaxNoxxx" + //9
                    ", a.cRecdStat" + //10
                    ", a.sEntryByx" + //11
                    ", a.dEntryDte" + //12
                    ", a.sModified" + //13
                    ", a.dModified" + //14
                    ", IFNULL(d.sProvName, '') sProvName" + //15                    
                    ", TRIM(CONCAT(b.sTownName, ', ', d.sProvName)) sTownProv" + //16
                    ", a.sBankBrch" + //17
                    ", IFNULL(b.sTownName, '') sTownName" + //18
                    ", IFNULL(b.sProvIDxx, '') sProvIDxx" + //19
                " FROM " + MASTER_TABLE + " a " +
                    " LEFT JOIN TownCity b ON b.sTownIDxx = a.sTownIDxx" +                    
                    " LEFT JOIN Province d on d.sProvIDxx = b.sProvIDxx" ;                                       
    } 
    
    //query for town
    private String getSQ_Town(){
        return "SELECT " +
                    "  IFNULL(sTownIDxx, '') sTownIDxx " +
                    ", IFNULL(sTownName, '') sTownName " +
                    ", IFNULL(sZippCode, '') sZippCode " +
                " FROM TownCity " ;                 
    }
    
    //search town (used when "Town" is double clicked or searched)
    /**
    * Searches for a town within a specified province based on criteria and retrieves town details.
    * This method is used to search for a town within a specified province, either by town code or town name criteria. It allows both UI and non-UI search modes and retrieves town details if found.
    * @param fsValue The search criteria, which can be a town code or town name.
    * @param fbByCode Set to true if searching by town code, false if searching by town name.
    * @return True if a town within the specified province is successfully found, and its details are retrieved, otherwise false.
    * @throws SQLException if a database error occurs.
    */
    public boolean searchTown(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Town();
        String lsProvIDxx = poBankInfo.getString("sProvIDxx");
        
        if (lsProvIDxx.isEmpty()) {
            psMessage = "Please Enter Province First.";
            return false;
        }
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownIDxx = " + SQLUtil.toSQL(fsValue) +
                                                   "AND sProvIDxx = " + SQLUtil.toSQL(lsProvIDxx));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownName LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                "AND sProvIDxx = " + SQLUtil.toSQL(lsProvIDxx));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sTownIDxx", loRS.getString("sTownIDxx"));
                setMaster("sTownName", loRS.getString("sTownName"));
                setMaster("sZippCode", loRS.getString("sZippCode"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
                       
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        lsSQL, 
                                                        fsValue,
                                                        "Code»Town", 
                                                        "sTownIDxx»sTownName",
                                                        "sTownIDxx»sTownName",
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster( "sTownIDxx", (String) loJSON.get("sTownIDxx"));
                setMaster( "sTownName", (String) loJSON.get("sTownName"));
                setMaster( "sZippCode", (String) loJSON.get("sZippCode"));
            }
        }
        
        return true;
    }
    //query for Province
    private String getSQ_Province(){
        return "SELECT " +
                    " sProvName " +
                    ", sProvIDxx " +                                        
                " FROM Province  " ;               
    }
    //search Province (used when "Province" is double clicked or searched)
    /**
    * Searches for a province based on criteria and retrieves its details.
    * This method is used to search for a province, either by province code or province name criteria. It allows both UI and non-UI search modes and retrieves the province's details if found.
    *
    * @param fsValue The search criteria, which can be a province code or province name.
    * @param fbByCode Set to true if searching by province code, false if searching by province name.
    * @return True if a province is successfully found, and its details are retrieved, otherwise false.
    * @throws SQLException if a database error occurs.
    */
    public boolean searchProvince(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Province();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sProvIDxx = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sProvName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster( "sProvName", loRS.getString("sProvName"));  
                setMaster( "sProvIDxx", loRS.getString("sProvIDxx"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
                        
            JSONObject loJSON = showFXDialog.jsonSearch(
                    poGRider, 
                    lsSQL,
                    fsValue,
                    "Code»Province", 
                    "sProvIDxx»sProvName",
                    "sProvIDxx»sProvName",
                    fbByCode ? 0 : 1);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {                
                setMaster("sProvName", (String) loJSON.get("sProvName"));
                setMaster("sProvIDxx", (String) loJSON.get("sProvIDxx"));
            }
        }
        
        return true;
    }
    
    /**
    * Validates the entry for bank information.
    * This method performs validation checks on the bank information fields to ensure they are not empty and that there are no duplicate records with the same bank name, address, and town ID. If validation fails, it sets an error message.
    * @return True if the entry is valid, otherwise false.
    * @throws SQLException if a database error occurs.
    */
    private boolean isEntryOK() throws SQLException{
        poBankInfo.first();

        if (poBankInfo.getString("sBankName").isEmpty()){
            psMessage = "Bank Name is not set.";
            return false;
        }
        
        if (poBankInfo.getString("sContactP").isEmpty()){
            psMessage = "Contact Person is not set.";
            return false;
        }
        
        if (poBankInfo.getString("sAddressx").isEmpty()){
            psMessage = "Address is not set.";
            return false;
        }
        
        if (poBankInfo.getString("sTownIdxx").isEmpty()){
            psMessage = "Town is not set.";
            return false;
        }                
        
        String lsSQL = getSQ_Master();
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sBankName = " + SQLUtil.toSQL(poBankInfo.getString("sBankName")) +
                                                " AND a.sAddressx = " + SQLUtil.toSQL(poBankInfo.getString("sAddressx")) + 
                                                " AND a.sTownIDxx = " + SQLUtil.toSQL(poBankInfo.getString("sTownIDxx")) +
                                                " AND a.sBankIDxx <> " + SQLUtil.toSQL(poBankInfo.getString("sBankIDxx")));                                          

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Bank Information.";
            MiscUtil.close(loRS);        
            return false;
        }
                                  
        return true;
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poBankInfo.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poBankInfo.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poBankInfo.getMetaData().getColumnType(lnCtr));
            if (poBankInfo.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poBankInfo.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poBankInfo.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }
}
