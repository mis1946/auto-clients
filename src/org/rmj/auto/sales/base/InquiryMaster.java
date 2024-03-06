/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.sales.base;

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
import org.rmj.auto.json.TabsStateManager;

/**
 *
 * @author Jahn April 13 2023
 */
public class InquiryMaster {
    private final String MASTER_TABLE = "Customer_Inquiry";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autoapp_json/" + TabsStateManager.getJsonFileName("Inquiry");
    
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
    
    private CachedRowSet poMaster;
    private CachedRowSet poDetail;
    private CachedRowSet poVhclPrty;
    private CachedRowSet poInqPromo;
    
    public InquiryMaster(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
        
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
            
            if(poVhclPrty != null){
                lsValue = CommonUtils.RS2JSON(poVhclPrty).toJSONString();
                laPriority = (JSONArray) loParser.parse(lsValue);
                loJSON.put("priority", laPriority);
            }
            
            if(poInqPromo != null){
                lsValue = CommonUtils.RS2JSON(poInqPromo).toJSONString();
                laPromo = (JSONArray) loParser.parse(lsValue);
                loJSON.put("promo", laPromo);
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
            Logger.getLogger(InquiryMaster.class.getName()).log(Level.SEVERE, null, ex);
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
                    
                    int row = 1;
                    int ctr = 1;
                    
                    // Extract the "priority" array from the JSON object
                    JSONArray priorityArray = (JSONArray) jsonObject.get("priority");
                    if(priorityArray != null) {
                        if(priorityArray.size()>0){
                            while (priorityArray.size() > getVhclPrtyCount()) {
                                if (addVhclPrty()){
                                } else {
                                    psMessage = "Error in  Vehicle Priority.";
                                    return false;
                                }
                            }
                            
                            // Extract index and value from each object in the "labor" array
                            for (Object item : priorityArray) { 
                                poVhclPrty.beforeFirst();
                                while (poVhclPrty.next()){
                                    if(ctr == row){
                                        JSONObject priority = (JSONObject) item;
                                        for (Object key : priority.keySet()) {
                                            Object value = priority.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(poVhclPrty.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    poVhclPrty.updateObject(Integer.valueOf(key.toString()), value );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poVhclPrty.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poVhclPrty.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poVhclPrty.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    poVhclPrty.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        poVhclPrty.updateRow();
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
                    // Extract the "promo" array from the JSON object
                    JSONArray promoArray = (JSONArray) jsonObject.get("promo");
                    if(promoArray != null) {
                        if(promoArray.size()>0){
                            while (promoArray.size() > getInqPromoCount()) {
                                if (addPromo()){
                                } else {
                                    psMessage = "Error in adding Promo.";
                                    return false;
                                }
                            }
                            // Extract index and value from each object in the "parts" array
                            for (Object item : promoArray) {
                                poInqPromo.beforeFirst(); 
                                while (poInqPromo.next()){  
                                    if(ctr == row){
                                        JSONObject parts = (JSONObject) item;
                                        for (Object key : parts.keySet()) {
                                            Object value = parts.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(poInqPromo.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    poInqPromo.updateObject(Integer.valueOf(key.toString()), tempValue );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInqPromo.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInqPromo.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    poInqPromo.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    poInqPromo.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        poInqPromo.updateRow();
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
            Logger.getLogger(InquiryMaster.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
//-----------------------------------------Customer Inquiry---------------------
    //TODO add setMaster for inquiry
    public void setMaster(int fnIndex, Object foValue) throws SQLException{        
        poMaster.first();
        
        switch (fnIndex){  
            case 1 ://sTransNox
            case 2 ://sBranchCD            
            case 4 ://sEmployID            
            case 6 ://sVhclIDxx
            case 7 ://sClientID
            case 8 ://sRemarksx
            case 9 ://sAgentIDx            
            case 12: //sSourceCD
            case 13: //sSourceNo
            case 14: //sTestModl
            case 15: //sActvtyID
            case 19: //sLockedBy
            case 20: //sLockedDt
            case 21: //sApproved
            case 22: //sSerialID
            case 23: //sInqryCde  
            case 29: //sCompnyNm 
            case 30: //sMobileNo 
            case 31: //sAccountx 
            case 32: //sEmailAdd  
            case 11: //cIntrstLv
            case 33: //sAddressx
            case 34: //sSalesExe
            case 35: //sSalesAgn
            case 36: //sPlatform
            case 37: //sActTitle
            case 38: //sBranchNm
            //case 39: //cMainOffc
            case 39: //cClientTp
            case 40: //sContctID   
            case 41: //sContctNm
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 5 ://cIsVhclNw            
            case 17: //nReserved
            case 18: //nRsrvTotl
            case 24: //cTranStat
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break;  
            case 3 ://dTransact
            case 10: //dTargetDt
            case 16: //dLastUpdt
                if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;      
        }     
        saveState(toJSONString());
    }   
    
    //Inquiry Master setter
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poMaster, fsIndex), foValue);
    }
    //Inquiry Master getter
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    //Inquiry Master getter
    public Object getMaster(int fnIndex) throws SQLException{
        poMaster.first();
        return poMaster.getObject(fnIndex);
    }
    
    //INQUIRY SEARCH GETTER
    public Object getInqDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poDetail.absolute(fnRow);
        
        //return "" instead of null since it cannot handle null values
        return poDetail.getObject(fnIndex) != null ? poDetail.getObject(fnIndex) : "";
    }
    
    //INQUIRY SEARCH GETTER
    public Object getInqDetail(int fnRow, String fsIndex) throws SQLException{
        return getInqDetail(fnRow, MiscUtil.getColumnIndex(poDetail, fsIndex));
    }
    
    //INQUIRY MASTER SEARCH COUNT
    public int getInquiryMasterCount() throws SQLException{
        if (poMaster != null){
            poMaster.last();
            return poMaster.getRow();
        }else{
            return 0;
        }              
    }
    
    public int getInquiryDetailCount() throws SQLException{
        if (poDetail != null){
            poDetail.last();
            return poDetail.getRow();
        }else{
            return 0;
        }              
    }
//-----------------------------------------Vehicle Priority---------------------
    //Target vehicle Priority Setter
    //TODO add setMaster for inquiry
    public void setVhclPrty(int fnRow, int fnIndex, Object foValue) throws SQLException{        
        poVhclPrty.absolute(fnRow); 
        
        switch (fnIndex){  
            case 1 ://sTransNox                      
            case 3 ://sVhclIDxx            
            case 6 ://sDescript             
                poVhclPrty.updateObject(fnIndex, (String) foValue);
                poVhclPrty.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 2 ://nPriority                            
                if (foValue instanceof Integer)
                    poVhclPrty.updateInt(fnIndex, (int) foValue);
                else 
                    poVhclPrty.updateInt(fnIndex, 0);
                
                poVhclPrty.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break;             
        }   
        saveState(toJSONString());
    }   
    
    public void setVhclPrty(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setVhclPrty(fnRow,MiscUtil.getColumnIndex(poVhclPrty, fsIndex), foValue);
    }
    //Target Vehicle Priority GETTER    
    public Object getVhclPrty(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVhclPrty.absolute(fnRow);
        return poVhclPrty.getObject(fnIndex);
    }
    
    //Target Vehicle Priority GETTER
    public Object getVhclPrty(int fnRow, String fsIndex) throws SQLException{
        return getVhclPrty(fnRow, MiscUtil.getColumnIndex(poVhclPrty, fsIndex));
    }        
    
    //get rowcount of Target priority Vehicle
    public int getVhclPrtyCount() throws SQLException{
        if (poVhclPrty != null){
            poVhclPrty.last();
            return poVhclPrty.getRow();
        }else{
            return 0;
        }              
    }   
    
    /**
    * Removes a target vehicle entry from the Vehicle Priority record set.
    * @param fnRow The row number of the target vehicle entry to be removed.
    * @return true to indicate the target vehicle entry has been removed successfully.
    * @throws SQLException if there's an issue with the database operation.
    */
    public boolean removeTargetVehicle(int fnRow) throws SQLException{
        if (getVhclPrtyCount() == 0) {
            psMessage = "No priority to delete.";
            return false;
        }
        
        poVhclPrty.absolute(fnRow);
        poVhclPrty.deleteRow();
        return true;
    }
//-----------------------------------------Vehicle Promo------------------------
    //Inquiry Promo Setter    
    public void setInqPromo(int fnRow, int fnIndex, Object foValue) throws SQLException{        
        poInqPromo.absolute(fnRow);        
        switch (fnIndex){  
            case 1 ://sTransNox                      
            case 2 ://sPromoIDx 
            case 5 ://sActTitle
                poInqPromo.updateObject(fnIndex, (String) foValue);
                poInqPromo.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break; 
            case 6: //dDateFrom
            case 7: //dDateThru
                if (foValue instanceof Date){
                    poInqPromo.updateObject(fnIndex, foValue);
                } else {
                    poInqPromo.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poInqPromo.updateRow();            
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;   
        }       
        saveState(toJSONString());
    }   
    //Inquiry Promo setter
    public void setInqPromo(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setInqPromo(fnRow,MiscUtil.getColumnIndex(poInqPromo, fsIndex), foValue);
    }
    //Inquiry PromoGETTER    
    public Object getInqPromo(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poInqPromo.absolute(fnRow);
        return poInqPromo.getObject(fnIndex);
    }
    
    //Inquiry Promo GETTER
    public Object getInqPromo(int fnRow, String fsIndex) throws SQLException{
        return getInqPromo(fnRow, MiscUtil.getColumnIndex(poInqPromo, fsIndex));
    }        
    
    //get rowcount of Target priority Vehicle
    public int getInqPromoCount() throws SQLException{
        if (poInqPromo != null){
            poInqPromo.last();
            return poInqPromo.getRow();
        }else{
            return 0;
        }              
    }
    
    /**
    * Removes a promotional entry from the InqPromo record set.
    *
    * @param fnRow The row number of the promotional entry to be removed.
    * @return true to indicate the promotional entry has been removed successfully.
    * @throws SQLException if there's an issue with the database operation.
    */
    public boolean removeInqPromo(int fnRow) throws SQLException{
        if (getInqPromoCount()== 0) {
            psMessage = "No address to delete.";
            return false;
        }
        
        poInqPromo.absolute(fnRow);
        poInqPromo.deleteRow();
        return true;
    }
//-----------------------------------------New Record---------------------------
    /**
     * Creates Inquiry Record.
     * @return 
     */
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {       
            String lsSQL = getSQ_Master() + " WHERE 0=1";
            //String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1"); 
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            //----------------------------------Inquiry Master------------------
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);       
            //poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);
            poMaster.updateString("cIsVhclNw", "0");  
            poMaster.updateString("cIntrstLv", "a");  
            poMaster.updateString("cTranStat", "0"); 
            poMaster.updateString("sSourceCD", "0");
            poMaster.updateObject("dTargetDt", poGRider.getServerDate());    
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
//-----------------------------------------Search Record------------------------   
    /**
    * Searches for a record in the master using the provided search criteria and opens it.
    * @param fsValue  The value used for searching, either code or customer name.
    * @param fbByCode Indicates whether the search should be performed by code (true) or by customer name (false).
    * @return true if a matching record is found and opened; false if no matching record is found.
    * @throws SQLException If a database error occurs during the search.
    */
    public boolean SearchRecord(String fsValue, boolean fbByCode)throws SQLException{
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
                                "Code»Customer Name»",
                                "sTransNox»sCompnyNm",
                                "a.sTransNox»sCompnyNm",
                                fbByCode ? 0 : 1);
            if (loJSON != null)
                return OpenRecord((String) loJSON.get("sTransNox"));
            else{
                psMessage = "No record selected.";
                return false;
            }                                        
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL,"a.sTransNox = " + SQLUtil.toSQL(fsValue));
        else {
            if (!fsValue.isEmpty()){
                lsSQL = MiscUtil.addCondition(lsSQL, "sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%"));
            }
        }            
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sTransNox");
        MiscUtil.close(loRS);
        
        //return OpenRecord(lsSQL, true);
        return OpenRecord(lsSQL);
    }
    
    /**

    Loads customer data based on search criteria.
    @param fsValue the search keyword to be used for searching customer data
    @param fsDfrom the starting date range for filtering the customer data
    @param fsDto the ending date range for filtering the customer data
    @param fbBySearch the flag indicating whether to filter the customer data by search or not
    @return true if the customer data was successfully loaded; false otherwise
    @throws SQLException if a database access error occurs
    */
    public boolean loadCustomer(String fsValue, String fsDfrom, String fsDto, boolean fbBySearch) throws SQLException {
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        //TODO LOAD ONLY INQUIRY OF USER THAT SEARCHED
        //String lsSQL = getSQ_Customerinfo() + "AND sCompnyNm LIKE '" + fsValue + "%'";
        if (fbBySearch) {
             lsSQL = getSQ_Master() + " WHERE b.sCompnyNm like " + SQLUtil.toSQL(fsValue + "%") +
                                                      " AND (a.dTransact >= " + SQLUtil.toSQL(fsDfrom) +
                                                      " AND a.dTransact <= " + SQLUtil.toSQL(fsDto) + ")" +
                                      " AND a.cTranStat <> '6' GROUP BY a.sClientID ORDER BY a.dTransact DESC "; //added by Arsiela 12-28-2023
        }else{
             lsSQL = getSQ_Master() + " WHERE (DATE(a.dTransact) >= " + SQLUtil.toSQL(fsDfrom) +
                                                      " AND DATE(a.dTransact) <= " + SQLUtil.toSQL(fsDto) + ")"
                                    + " AND a.cTranStat <> '6' GROUP BY a.sClientID ORDER BY a.dTransact DESC "; //added by Arsiela 12-28-2023
        }
        
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        poDetail = factory.createCachedRowSet();
        poDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
//-----------------------------------------Open Record--------------------------  
    /**
    * Opens a record in the master along with associated VHCL priority and Inq promo data using the provided transaction number.
    * @param fsValue The transaction number to be used to open the record.
    * @return true if the record is successfully opened; false if an error occurs.
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
            //lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
            lsSQL = getSQ_Master() + " WHERE a.sTransNox = " + SQLUtil.toSQL(fsValue);
            loRS = poGRider.executeQuery(lsSQL);
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);

            //open VHCL priority
            lsSQL = MiscUtil.addCondition(getSQ_VhclPrty(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poVhclPrty = factory.createCachedRowSet();
            poVhclPrty.populate(loRS);
            MiscUtil.close(loRS);

            //open Inq promo
            lsSQL = MiscUtil.addCondition(getSQ_InqPromo(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poInqPromo = factory.createCachedRowSet();
            poInqPromo.populate(loRS);
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
    Adds a new row to the poVhclPrty rowset.
    @return true if successful, false otherwise
    @throws SQLException if a database access error occurs
    */
    public boolean addVhclPrty() throws SQLException{
 
        if (poVhclPrty == null){
            String lsSQL = MiscUtil.addCondition(getSQ_VhclPrty(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poVhclPrty = factory.createCachedRowSet();
            poVhclPrty.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        poVhclPrty.last();
        poVhclPrty.moveToInsertRow();

        MiscUtil.initRowSet(poVhclPrty);  
  
        poVhclPrty.insertRow();
        poVhclPrty.moveToCurrentRow();
                
        return true;
    }
    /**
    * Adds a new promotional entry to the InqPromo record set.
    * @return true to indicate the promotional entry has been added successfully.
    * @throws SQLException if there's an issue with the database operation.
    */
    public boolean addPromo() throws SQLException{
 
        if (poInqPromo == null){
            String lsSQL = MiscUtil.addCondition(getSQ_InqPromo(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poInqPromo = factory.createCachedRowSet();
            poInqPromo.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        poInqPromo.last();
        poInqPromo.moveToInsertRow();

        MiscUtil.initRowSet(poInqPromo);  
  
        poInqPromo.insertRow();
        poInqPromo.moveToCurrentRow();
                
        return true;
    }
//-----------------------------------------Update Record------------------------  
    /**
    * Sets the edit mode to UPDATE, allowing changes to the current record.
    *
    * @return true to indicate the update mode has been set successfully.
    */
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        saveState(toJSONString());
        return true;       
    }
//-----------------------------------------Save Record--------------------------    
    //TODO Save record for saving
    public boolean SaveRecord() {
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            String lsTransNox = "";
            if (pnEditMode == EditMode.ADDNEW){
                lsTransNox =  MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", true, poGRider.getConnection(), psBranchCd); 
                setMaster("sTransNox", lsTransNox);
            }
            
            if (!isEntryOK()) return false;
            
            String lsSQL = "";
            String lsgetBranchCd = "";
            int lnCtr;
            
            if (!psBranchCd.equals(getMaster(2).toString())){
                lsgetBranchCd = getMaster(2).toString();
            }
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                if (!pbWithParent) poGRider.beginTrans();
                //-------------------SAVE INQUIRY MASTER------------------------
//                String lsTransNox =  MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", true, poGRider.getConnection(), psBranchCd); 
//                poMaster.updateObject("sTransNox", lsTransNox);
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sCompnyNm»sMobileNo»sAccountx»sEmailAdd»sAddressx»sSalesExe»sSalesAgn»sPlatform»sActTitle»sBranchNm»cClientTp»sContctNm");
                
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                }
                
                //-------------------SAVE VEHICLE PRIORITY----------------------
                
                if (getVhclPrtyCount()> 0){
                    lnCtr = 1;
                    poVhclPrty.beforeFirst();
                    while (poVhclPrty.next()){
                        poVhclPrty.updateObject("sTransNox", lsTransNox);
                        poVhclPrty.updateObject("sEntryByx", poGRider.getUserID());
                        poVhclPrty.updateObject("dEntryDte", (Date) poGRider.getServerDate());                    
                        poVhclPrty.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poVhclPrty, "Customer_Inquiry_Vehicle_Priority","sDescript");
                        //TODO what is substring(0,4)
                        if (poGRider.executeQuery(lsSQL, "Customer_Inquiry_Vehicle_Priority", psBranchCd, lsgetBranchCd) <= 0){ //lsTransNox.substring(0, 4)
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }

                        lnCtr++;
                    }
                }
                
                //-------------------SAVE INQUIRY PROMO-------------------------
                
                if(getInqPromoCount()> 0) {
                    lnCtr = 1;
                    poInqPromo.beforeFirst();
                    while (poInqPromo.next()){
                        poInqPromo.updateObject("sTransNox", lsTransNox);
                        poInqPromo.updateObject("sEntryByx", poGRider.getUserID());
                        poInqPromo.updateObject("dEntryDte", (Date) poGRider.getServerDate());                    
                        poInqPromo.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poInqPromo, "Customer_inquiry_promo", "sActTitle»dDateFrom»dDateThru");

                        if (poGRider.executeQuery(lsSQL, "Customer_inquiry_promo", psBranchCd, lsgetBranchCd) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }

                        lnCtr++;
                    }
                }
                if (!pbWithParent) poGRider.commitTrans();            
                pnEditMode = EditMode.READY;
                return true;
            } else { //update 
                if (!pbWithParent) poGRider.beginTrans();
                
                //set transaction number on records
                lsTransNox = (String) getMaster("sTransNox");
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sCompnyNm»sMobileNo»sAccountx»sEmailAdd»sAddressx»sSalesExe»sSalesAgn»sPlatform»sActTitle»sBranchNm»cClientTp»sContctNm", //cMainOffc
                                            "sTransNox = " + SQLUtil.toSQL(lsTransNox));
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                }                                                            
                //----------------------VEHICLE PRIORITY SAVE-------------------
                if (getVhclPrtyCount()> 0){
                    lnCtr = 1;
                    poVhclPrty.beforeFirst();
                    while (poVhclPrty.next()){
                        String lsfTransNox = (String) getVhclPrty(lnCtr, "sTransNox");// check if user added new VEHICLE PRIORITY to insert
                        if (lsfTransNox.equals("") || lsfTransNox.isEmpty()){
                            poVhclPrty.updateObject("sTransNox", lsTransNox);
                            poVhclPrty.updateObject("sEntryByx", poGRider.getUserID());
                            poVhclPrty.updateObject("dEntryDte", (Date) poGRider.getServerDate());                    
                            poVhclPrty.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poVhclPrty, "Customer_Inquiry_Vehicle_Priority","sDescript");
                            //TODO what is substring(0,4)
                            if (poGRider.executeQuery(lsSQL, "Customer_Inquiry_Vehicle_Priority", psBranchCd, lsgetBranchCd) <= 0){
                                if (!pbWithParent) poGRider.rollbackTrans();
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }
                        }else{
                            lsSQL = MiscUtil.rowset2SQL(poVhclPrty, 
                                                        "customer_inquiry_vehicle_priority", 
                                                        "sDescript", 
                                                        "sTransNox = " + SQLUtil.toSQL(lsTransNox) + 
                                                            " AND sVhclIDxx = " + SQLUtil.toSQL(poVhclPrty.getString("sVhclIDxx")));

                            if (!lsSQL.isEmpty()){
                                if (poGRider.executeQuery(lsSQL, "customer_inquiry_vehicle_priority", psBranchCd, lsgetBranchCd) <= 0){
                                    if (!pbWithParent) poGRider.rollbackTrans();
                                    psMessage = poGRider.getMessage() + ";" + poGRider.getErrMsg();
                                    return false;
                                }
                            }
                        }

                        lnCtr++;
                    }
                }
                //----------------------Inquiry PROMO Update-------------------
                if (getInqPromoCount()> 0){
                    lnCtr = 1;
                    poInqPromo.beforeFirst();
                    while (poInqPromo.next()){
                        String lsfTransNox = (String) getInqPromo(lnCtr, "sTransNox");// check if user added new VEHICLE Promo to insert
                        if (lsfTransNox.equals("") || lsfTransNox.isEmpty()){
                            poInqPromo.updateObject("sTransNox", lsTransNox);
                            poInqPromo.updateObject("sEntryByx", poGRider.getUserID());
                            poInqPromo.updateObject("dEntryDte", (Date) poGRider.getServerDate());                    
                            poInqPromo.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poInqPromo, "Customer_inquiry_promo","sActTitle»dDateFrom»dDateThru");
                            //TODO what is substring(0,4)
                            if (poGRider.executeQuery(lsSQL, "Customer_inquiry_promo", psBranchCd, lsgetBranchCd) <= 0){
                                if (!pbWithParent) poGRider.rollbackTrans();
                                psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                return false;
                            }                        
                        }
                        lnCtr++;
                    }
                }
            }
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }                                                       
            
            if (!pbWithParent) poGRider.commitTrans();
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }
//------------------------------------Inquiry Master----------------------------    
    //TODO query for retrieving inquiry
    private String getSQ_Master(){
        return "SELECT " +
                    " a.sTransNox"  + //1
                    ", a.sBranchCD" + //2
                    ", a.dTransact" + //3
                    ", a.sEmployID" + //4
                    ", a.cIsVhclNw" + //5
                    ", a.sVhclIDxx" + //6
                    ", a.sClientID" + //7
                    ", a.sRemarksx" + //8
                    ", a.sAgentIDx" + //9
                    ", a.dTargetDt" + //10
                    ", a.cIntrstLv" + //11
                    ", a.sSourceCD" + //12
                    ", a.sSourceNo" + //13
                    ", a.sTestModl" + //14
                    ", a.sActvtyID" + //15
                    ", a.dLastUpdt" + //16
                    ", a.nReserved" + //17
                    ", a.nRsrvTotl" + //18
                    ", a.sLockedBy" + //19
                    ", a.sLockedDt" + //20
                    ", a.sApproved" + //21
                    ", a.sSerialID" + //22
                    ", a.sInqryCde" + //23
                    ", a.cTranStat" + //24
                    ", a.sEntryByx" + //25
                    ", a.dEntryDte" + //26
                    ", a.sModified" + //27
                    ", a.dModified" + //28
                    ",IFNULL(b.sCompnyNm,'') as sCompnyNm " +//29
                    ",IFNULL(c.sMobileNo,'') as sMobileNo " +//30
                    ",IFNULL(h.sAccountx,'') as sAccountx " +//31
                    ",IFNULL(i.sEmailAdd,'') as sEmailAdd " +//32
                    ", IFNULL(CONCAT( IFNULL(CONCAT(dd.sHouseNox,' ') , ''), IFNULL(CONCAT(dd.sAddressx,' ') , ''), " +
                    " 	IFNULL(CONCAT(f.sBrgyName,' '), ''), " +
                    " 	IFNULL(CONCAT(e.sTownName, ', '),''), " +
                    " 	IFNULL(CONCAT(g.sProvName),'') )	, '') AS sAddressx " + //33
                    " ,IFNULL(j.sCompnyNm, '') AS sSalesExe   " + //34
                    " ,IFNULL(l.sCompnyNm, '') AS sSalesAgn   " + //35
                    " ,IFNULL(m.sPlatform, '') AS sPlatform   " + //36
                    " ,IFNULL(n.sActTitle, '') AS sActTitle   " + //37
                    " ,IFNULL(k.sBranchNm, '') AS sBranchNm   " + //38
                    " ,IFNULL(b.cClientTp,'') AS cClientTp" + //39
                    " ,IFNULL(a.sContctID,'') AS sContctID" + //40
                    " ,IFNULL(o.sCompnyNm,'') AS sContctNm" + //41
                " FROM  " + MASTER_TABLE + " a " +
                " LEFT JOIN client_master b ON b.sClientID = a.sClientID" +
                " LEFT JOIN client_mobile c ON c.sClientID = b.sClientID AND c.cPrimaryx = '1' " +
                " LEFT JOIN client_address d ON d.sClientID = b.sClientID AND d.cPrimaryx = '1' " + 
                " LEFT JOIN addresses dd ON dd.sAddrssID = d.sAddrssID" + 
                " LEFT JOIN TownCity e ON e.sTownIDxx = dd.sTownIDxx" +
                " LEFT JOIN Barangay f ON f.sBrgyIDxx = dd.sBrgyIDxx AND f.sTownIDxx = dd.sTownIDxx" + 
                " LEFT JOIN Province g on g.sProvIDxx = e.sProvIDxx" +
                " LEFT JOIN client_social_media h ON h.sClientID = b.sClientID" +
                " LEFT JOIN client_email_address i ON i.sClientID = b.sClientID AND i.cPrimaryx = '1' " +
                " LEFT JOIN ggc_isysdbf.client_master j ON j.sClientID = a.sEmployID  " +
                " LEFT JOIN branch k on k.sBranchCd = a.sBranchCd " +
                " LEFT JOIN client_master l ON l.sClientID = a.sAgentIDx" + 
                " LEFT JOIN online_platforms m ON m.sTransNox = a.sSourceNo" + 
                " LEFT JOIN activity_master n ON n.sActvtyID = a.sActvtyID"  +
                " LEFT JOIN client_master o ON o.sClientID = a.sContctID" ;                                 
    }

//------------------------------------Inquiry Customer--------------------------    
    //TODO query for retrieving customer info
    private String getSQ_Customerinfo(){ 
            return  "SELECT "
                        + " a.sClientID " 
                        + ", a.sLastName " 
                        + ", a.sFrstName "
                        + ", a.sMiddName "
                        + ", a.sCompnyNm "                  
                        + ", IFNULL(b.sMobileNo,'') sMobileNo "
                        + ", IFNULL(c.sAccountx,'') sAccountx "
                        + ", IFNULL(d.sEmailAdd,'') sEmailAdd "
                        + ", IFNULL(CONCAT( IFNULL(CONCAT(ee.sHouseNox,' ') , ''), IFNULL(CONCAT(ee.sAddressx,' ') , ''),  "
                        + "IFNULL(CONCAT(i.sBrgyName,' '), ''), " 
                        + "IFNULL(CONCAT(h.sTownName, ', '),''), " 
                        + "IFNULL(CONCAT(j.sProvName),'') )	, '') AS sAddressx "
                        + ", IFNULL(a.cClientTp,'') cClientTp "
                    + "FROM client_master a "
                        + "LEFT JOIN client_mobile b ON b.sClientID = a.sClientID and b.cPrimaryx = 1 and b.cRecdStat = 1  "
                        + "LEFT JOIN client_social_media c ON  c.sClientID = a.sClientID and c.cRecdStat = 1 "
                        + "LEFT JOIN client_email_address d ON  d.sClientID = a.sClientID AND d.cPrimaryx = 1 AND d.cRecdStat = 1 "
                        + "LEFT JOIN client_address e ON e.sClientID = a.sClientID AND e.cPrimaryx = '1' "
                        + "LEFT JOIN addresses ee ON ee.sAddrssID = e.sAddrssID "
                        + "LEFT JOIN TownCity h ON h.sTownIDxx = ee.sTownIDxx  "  
                        + "LEFT JOIN barangay i ON i.sBrgyIDxx = ee.sBrgyIDxx AND i.sTownIDxx = ee.sTownIDxx "  
                        + "LEFT JOIN Province j ON j.sProvIDxx = h.sProvIDxx "  
                    + "WHERE a.cRecdStat = '1' ";                        
    }
    
    /**

    Searches for a customer based on the given value.
    @param fsValue The value to search for.
    @param fbByCode True if searching by code, false if searching by customer name.
    @return True if a record is found, false otherwise.
    @throws SQLException If a database access error occurs.
    */
    public boolean searchCustomer(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Customerinfo() + " AND a.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%")
                                                + " AND a.cRecdStat = '1'"
                                                + " AND a.sClientID <> " + SQLUtil.toSQL((String) getMaster("sAgentIDx"))
                                                + " GROUP BY a.sClientID ";   
        System.out.println(lsSQL);
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            if (loRS.next()){
                setMaster("sClientID", loRS.getString("sClientID"));
                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                setMaster("sMobileNo", loRS.getString("sMobileNo"));
                setMaster("sAccountx", loRS.getString("sAccountx"));
                setMaster("sEmailAdd", loRS.getString("sEmailAdd"));
                setMaster("sAddressx", loRS.getString("sAddressx"));  
                setMaster("cClientTp", loRS.getString("cClientTp"));    
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        lsSQL, 
                                                        "%"+ fsValue + "%", 
                                                        "Code»Customer Name»Address", 
                                                        "sClientID»sCompnyNm»sAddressx",
                                                        "sClientID»sCompnyNm»sAddressx",
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sMobileNo", (String) loJSON.get("sMobileNo"));
                setMaster("sAccountx", (String) loJSON.get("sAccountx"));
                setMaster("sEmailAdd", (String) loJSON.get("sEmailAdd")); 
                setMaster("sAddressx", (String) loJSON.get("sAddressx")); 
                setMaster("cClientTp", (String) loJSON.get("cClientTp")); 
            }
        }
        
        return true;
    }
    
    /**

    Searches for a customer based on the given value.
    @param fsValue The value to search for.
    @param fbByCode True if searching by code, false if searching by customer name.
    @return True if a record is found, false otherwise.
    @throws SQLException If a database access error occurs.
    */
    public boolean searchContactPerson(String fsValue, boolean fbByCode) throws SQLException{           
        String lsSQL = getSQ_Customerinfo() + " AND a.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%")
                                            + " AND a.cRecdStat = '1' AND a.cClientTp = '0' "
                                            + " AND a.sClientID <> " + SQLUtil.toSQL((String) getMaster("sClientID"))
                                            + " GROUP BY a.sClientID ";   
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sContctID", loRS.getString("sClientID"));
                setMaster("sContctNm", loRS.getString("sCompnyNm"));   
            } else {
                setMaster("sContctID", "");
                setMaster("sContctNm", "");   
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        lsSQL, 
                                                        "%"+ fsValue + "%", 
                                                        "Code»Contact Person»Address", 
                                                        "sClientID»sCompnyNm»sAddressx",
                                                        "sClientID»sCompnyNm»sAddressx",
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                setMaster("sContctID", "");
                setMaster("sContctNm", "");   
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sContctID", (String) loJSON.get("sClientID"));
                setMaster("sContctNm", (String) loJSON.get("sCompnyNm"));
            }
        }
        
        return true;
    }
//------------------------------------Inquiry Promo-----------------------------    
    //TODO query for promo
    private String getSQ_InqPromo(){
        return " SELECT " +
                    " IFNULL(a.sTransNox,'') sTransNox " +  
                    " , IFNULL(a.sPromoIDx,'') sPromoIDx " + 
                    " , a.sEntryByx " +
                    " , a.dEntryDte " +
                    " , IFNULL(b.sActTitle,'') sActTitle " +
                    " , b.dDateFrom " +  
                    " , b.dDateThru " +                  
                "  FROM customer_inquiry_promo a " +  
                "  LEFT JOIN activity_master b ON b.sActvtyID = a.sPromoIDx";                 
    }
    
//------------------------------------Inquiry Sales Executive-------------------        
    //TODO query for sales executives
    private String getSQ_SalesExecutive(){
        return  " SELECT  "                                                                                                                                                                                    
                    + "  IFNULL(a.sClientID,'')    sClientID,  "                                                                                                                                                 
                    + "  IFNULL(a.cRecdStat,'')    cRecdStat,  "                                                                                                                                                 
                    + "  IFNULL(b.sLastName,'')    sLastName,  "                                                                                                                                                 
                    + "  IFNULL(b.sFrstName,'')    sFrstName,  "                                                                                                                                                 
                    + "  IFNULL(b.sMiddName,'')    sMiddName,  "                                                                                                                                                 
                    + "  IFNULL(b.sCompnyNm,'')    sCompnyNm  "                                                                                                                                                 
                    + "  FROM sales_executive a                "                                                                                                                                                 
                    + "  LEFT JOIN GGC_ISysDBF.Client_Master b ON b.sClientID = a.sClientID    "  ;
    }
    
    /**
        This method searches for a sales executive based on a given search value, which can either be the sales executive code or the name.
        @param fsValue the search value to look for, which can be the sales executive code or the name
        @param fbByCode a boolean flag that indicates whether the search should be done by code (true) or by name (false)
        @return a boolean value indicating whether the search was successful (true) or not (false)
        @throws SQLException if there is an error executing the SQL query
    */
    public boolean searchSalesExec(String fsValue, boolean fbByCode) throws SQLException{
                        
        String lsSQL = MiscUtil.addCondition(getSQ_SalesExecutive(), " b.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%")
                                                                        + " AND a.cRecdStat = '1' ");            
        System.out.println(lsSQL);
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            if (loRS.next()){
                setMaster("sEmployID", loRS.getString("sClientID"));
                setMaster("sSalesExe", loRS.getString("sCompnyNm"));               
            } else {
                psMessage = "No record found.";
                setMaster("sEmployID", "");
                setMaster("sSalesExe", ""); 
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        lsSQL, 
                                                        fsValue, 
                                                        "Employee ID»Sales Executive Name", 
                                                        "sClientID»sCompnyNm",
                                                        "a.sClientID»b.sCompnyNm",                                                        
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                setMaster("sEmployID", "");
                setMaster("sSalesExe", ""); 
                return false;
            } else {
                setMaster("sEmployID", (String) loJSON.get("sClientID"));
                setMaster("sSalesExe", (String) loJSON.get("sCompnyNm"));                
            }
        }
        
        return true;
    }
    
//-------------------------------------Inquiry Sales Agent----------------------   
    //TODO query for referal agents
    private String getSQ_Agents(){
        return " SELECT " +
                    " a.sClientID " +
                    ", IFNULL(b.sCompnyNm, '') sCompnyNm " +
                    ", a.cRecdStat " +
                " FROM sales_agent a " +
                " LEFT JOIN client_master b ON b.sClientID = a.sClientID "; 
    }
    
    //TODO NEED TO MODIFY WHEN ACTUAL SALES AGENT TABLE IS DONE
    /**
        Searches for a sales agent with a given search value.
        @param fsValue the search value for the sales agent
        @param fbByCode if true, the search will be done using the agent code, otherwise, the search will be done using the agent name
        @return true if the sales agent is found, false otherwise

        @throws SQLException if a database access error occurs
    */
    public boolean searchSalesAgent(String fsValue, boolean fbByCode) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getSQ_Agents(), " b.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%")
                                                                + " AND a.cRecdStat = '1'"
                                                                + " AND a.sClientID <> " + SQLUtil.toSQL((String) getMaster("sClientID")));            
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sAgentIDx", loRS.getString("sClientID"));
                setMaster("sSalesAgn", loRS.getString("sCompnyNm"));               
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
                                                        "Agent Name", 
                                                        "sCompnyNm",
                                                        "sClientID»sCompnyNm",
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sAgentIDx", (String) loJSON.get("sClientID"));
                setMaster("sSalesAgn", (String) loJSON.get("sCompnyNm"));                
            }
        }
        
        return true;
    }
//------------------------------------Inquiry Activity Event--------------------      
    //TODO query for activity events            
    private String getSQ_InqActivity(){
        return " SELECT " +
                    " a.sActvtyID " +
                    " ,a.sActTitle " +
                    " ,a.dDateFrom " +
                    " ,a.dDateThru " +
                    " ,b.sEventTyp " +
                " FROM activity_master a " +
                " LEFT JOIN event_type b ON b.sActTypID = a.sActTypID " +
                " WHERE a.cTranStat = '1' AND " +
                " (a.sApproved is not null OR a.sApproved <> '')";     
    }
    
    /**
    * Searches for an activity based on the provided criteria and populates the activity record set.
    * @param fnRow    The row number where the activity information should be populated.
    * @param fsValue  The value used to search for the activity (e.g., activity type or activity ID).
    * @param fbByCode Indicates whether the search is by activity code (true) or other criteria (false).
    * @return true to indicate that the activity has been found and populated successfully.
    * @throws SQLException if there's an issue with the database operation.
    */
    public boolean searchActivity(int fnRow,String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = "";
                                
        lsSQL = MiscUtil.addCondition(getSQ_InqActivity(), " b.sEventTyp = " + SQLUtil.toSQL(fsValue)) 
                                                              + " AND a.dDateThru >= " + SQLUtil.toSQL(CommonUtils.xsDateShort((Date) getMaster(3)) ) ;
        System.out.println(lsSQL);
        ResultSet loRS;
        loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                    lsSQL, 
                                                    "", 
                                                    "Activity ID»Activity Title»Activity Date From »Activity Date To", 
                                                    "sActvtyID»sActTitle»dDateFrom»dDateThru",
                                                    "sActvtyID»sActTitle»dDateFrom»dDateThru",                                                        
                                                    fbByCode ? 0 : 1);
        if (fbByCode){
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sActvtyID", (String) loJSON.get("sActvtyID"));
                setMaster("sActTitle", (String) loJSON.get("sActTitle"));                
            }
        }else{
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                System.out.println((String) loJSON.get("sActvtyID"));               
                if (getInqPromoCount()> 0 ) {
                    for (int lnCtr = 1; lnCtr <= getInqPromoCount(); lnCtr++) {
                        if (getInqPromo(lnCtr, "sPromoIDx").toString().equals((String) loJSON.get("sActvtyID"))) {
                            psMessage = "Promo already exist";
                            return false;
                        }
                    }
                }
                setInqPromo(fnRow,"sPromoIDx", (String) loJSON.get("sActvtyID"));
                setInqPromo(fnRow,"sActTitle", (String) loJSON.get("sActTitle"));
                setInqPromo(fnRow,"dDateFrom", SQLUtil.toDate((String) loJSON.get("dDateFrom"), SQLUtil.FORMAT_SHORT_DATE));
                setInqPromo(fnRow,"dDateThru", SQLUtil.toDate((String) loJSON.get("dDateThru"), SQLUtil.FORMAT_SHORT_DATE));
            }
                    
        }
        return true;
    }
//------------------------------------Vehicle Priority--------------------------   
    //TODO query for activity events
    private String getSQ_VhclPrty(){
        return  "SELECT " + 
                    " a.sTransNox " + //1
                    ", a.nPriority" + //2
                    ", a.sVhclIDxx" + //3
                    ", a.sEntryByx" + //4
                    ", a.dEntryDte" + //5
                    ", IFNULL(b.sDescript, '') sDescript" + //6
                " FROM Customer_Inquiry_Vehicle_Priority a" +
                    " LEFT JOIN vehicle_master b ON a.sVhclIDxx = b.sVhclIDxx";
    }
    
    //Query for searching vehicle
    private String getSQ_Vehicle(){
        return  "SELECT " + 
                    " sVhclIDxx " + //1
                    ", sDescript" + //2                   
                " FROM Vehicle_master " ;                    
    }        
    
    //search Vehicle Priority (used when addbtn in Vehicle Priority is pressed)
    /**
        This method searches for a vehicle based on the given criteria and updates the vehicle priority table if needed.
        @param fnRow the row number where the vehicle should be updated, or 0 if this method is used for test model inquiry only.
        @param fsValue the value to be searched. If fbByCode is true, this is the vehicle code; otherwise, it is the vehicle description.
        @param fbByCode a flag that indicates whether to search by vehicle code (true) or vehicle description (false).
        @return true if the search is successful and the vehicle priority table is updated; false otherwise.
        @throws SQLException if a database access error occurs.
    */
    public boolean searchVhclPrty(int fnRow, String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Vehicle();
        int lnCtr = 1;
        int lnRow = getVhclPrtyCount();
        boolean lbVhclExst = false;
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sVhclIDxx = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sDescript LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            
            if (loRS.next()){
                if (fnRow == 0){//used in testmodel in inquiry                    
                    setMaster("sTestModl", loRS.getString("sDescript"));  
                }else{  //used in vehicle priority                    
                    //check if unit selected has been inserted already
                    if (getVhclPrtyCount() > 0 ){
                        lnCtr = 1;
                        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){                                                       
                            if ((getVhclPrty(lnCtr, "sVhclIDxx").equals(loRS.getString("sVhclIDxx")))) {   
                                lbVhclExst = true;
                                break;
                            }                            
                        }
                    }
                
                    if (lbVhclExst) {          
                        psMessage = "Unit description already inserted. Please select a different unit.";
                       return false;
                    }else{
                        setVhclPrty(fnRow, "sVhclIDxx", loRS.getString("sVhclIDxx"));
                        setVhclPrty(fnRow, "sDescript", loRS.getString("sDescript"));
                        setVhclPrty(fnRow, "nPriority", fnRow);
                    }
                }
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
                                                        "Code»Vehicle Description", 
                                                        "sVhclIDxx»sDescript",
                                                        "sVhclIDxx»sDescript",
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                if (fnRow == 0){//used in testmodel in inquiry
                    setMaster("sTestModl", (String) loJSON.get("sDescript"));  
                }
                else{  //used in vehicle priority
                    //check if unit selected has been inserted already
                    if (getVhclPrtyCount() > 0 ){
                        lnCtr = 1;
                        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){            
                            if ((getVhclPrty(lnCtr, "sVhclIDxx").equals((String) loJSON.get("sVhclIDxx")))) {   
                                lbVhclExst = true;
                                break;
                            }
                        }
                    }
                
                    if (lbVhclExst) {          
                        psMessage = "Unit description already inserted. Please select a different unit.";
                       return false;
                    }else{
                        setVhclPrty(fnRow, "sVhclIDxx", (String) loJSON.get("sVhclIDxx"));
                        setVhclPrty(fnRow, "sDescript", (String) loJSON.get("sDescript"));
                        setVhclPrty(fnRow, "nPriority", fnRow);
                    }
                }
            }
        }
        
        return true;
    }
    private String getSQ_Online() {
        return "SELECT " +
                    " sTransNox " +
                    " ,sPlatform " +
                    " ,sWebSitex " +
                "FROM online_platforms";
    }
    
    /**
    * Searches for a platform based on the provided criteria and populates the platform information.
    *
    * @param fsValue  The value used to search for the platform (either platform name or source number).
    * @param fbByCode Indicates whether the search is by platform name (false) or source number (true).
    * @return true to indicate that the platform has been found, and its information has been populated successfully.
    * @throws SQLException if there's an issue with the database operation.
    */
    public boolean searchPlatform(String fsValue, boolean fbByCode) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getSQ_Online(), " sPlatform LIKE " + SQLUtil.toSQL(fsValue + "%"));            
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sPlatform", loRS.getString("sPlatform"));
                setMaster("sSourceNo", loRS.getString("sTransNox"));               
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
                                                        "Platform Name", 
                                                        "sPlatform",
                                                        "sPlatform»sWebSitex",
                                                        0);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sPlatform", (String) loJSON.get("sPlatform"));
                setMaster("sSourceNo", (String) loJSON.get("sTransNox"));                
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
                //+ " , IFNULL(a.cMainOffc, '') cMainOffc "
                + " FROM branch a "
                + " LEFT JOIN branch_others b ON a.sBranchCd = b.sBranchCd  "
                + " WHERE a.cRecdStat = '1'  "
                + " AND b.cDivision = (SELECT cDivision FROM branch_others WHERE branch_others.sBranchCd = " + SQLUtil.toSQL(psBranchCd) + ")";
    }
    
    /**
    * Searches for a branch based on the provided criteria and populates the branch information.
    *
    * @param fsValue  The value used to search for the branch (either branch code or branch name).
    * @param fbByCode Indicates whether the search is by branch code (true) or branch name (false).
    * @return true to indicate that the branch has been found and its information has been populated successfully.
    * @throws SQLException if there's an issue with the database operation.
    */
    public boolean searchBranch(String fsValue, boolean fbByCode) throws SQLException {
        
        String lsSQL = "";
        if (fbByCode){
            lsSQL = (getSQ_Branch() + " AND a.sBranchCd = " + SQLUtil.toSQL(psBranchCd));
        } else {
            lsSQL = (getSQ_Branch() + " AND a.sBranchNm LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }

        ResultSet loRS;
        System.out.println(lsSQL);
        if (!pbWithUI) {
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            if (loRS.next()) {
                setMaster("sBranchCD", loRS.getString("sBranchCd"));
                setMaster("sBranchNm", loRS.getString("sBranchNm"));
                //setMaster("cMainOffc", loRS.getString("cMainOffc"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        lsSQL,
                                                        fsValue,
                                                        "Branch Code»Branch Name",
                                                        "sBranchCd»sBranchNm",
                                                        "a.sBranchCd»a.sBranchNm",
                                                        fbByCode ? 0 : 1);

            if (loJSON == null) {
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sBranchCD", (String) loJSON.get("sBranchCd"));
                setMaster("sBranchNm", (String) loJSON.get("sBranchNm"));
                //setMaster("cMainOffc", (String) loJSON.get("cMainOffc"));
            }
        }
        return true;
    }
           
    //-----------------------------Display Inquiry Master Fields----------------
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
    
    //-----------------------------Display Vehicle Priority Fields--------------
    public void displayVhclPrtyFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poVhclPrty.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("VEHICLE PRIORITY INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poVhclPrty.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poVhclPrty.getMetaData().getColumnType(lnCtr));
            if (poVhclPrty.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poVhclPrty.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poVhclPrty.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: VEHICLE PRIORITY INFO");
        System.out.println("----------------------------------------");
    }
    
    //-----------------------------Display Inquiry Promo Fields-----------------
    public void displayInqPromoFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poInqPromo.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("INQUIRY PROMO INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poInqPromo.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poInqPromo.getMetaData().getColumnType(lnCtr));
            if (poInqPromo.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poInqPromo.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poInqPromo.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: INQUIRY PROMO INFO");
        System.out.println("----------------------------------------");
    }
    
    //TODO validation for entries
    public boolean isEntryOK() throws SQLException{
        poMaster.first();

        if (poMaster.getString("sClientID").isEmpty()){
            psMessage = "Customer is not set.";
            return false;
        }
        
        if (poMaster.getString("sEmployID").isEmpty()){
            psMessage = "Sales Executive is not set.";
            return false;
        }
        
        if (poMaster.getString("sSourceCD").equals("3")){
            if (poMaster.getString("sAgentIDx").isEmpty()){
                psMessage = "Referral Agent is not set.";
                return false;
            }
        }
        
        if (poMaster.getString("sSourceCD").equals("1")){
            if (poMaster.getString("sSourceNo").isEmpty()){
                psMessage = "Online Store is not set.";
                return false;
            }
        }
        
        if (poMaster.getString("cIntrstLv").isEmpty()){
            psMessage = "Interest Level is not set.";
            return false;
        }
        
        if(getVhclPrtyCount()<= 0 ){
            psMessage = "Target Vehicle cannot be Empty.";
            return false;
        }
        
        /*
        -cTranStat	
        0	For Follow-up
	1	On Process
	2	Lost Sale
	3	VSP
	4	Sold
	5	Retired
	6	Cancelled
        */
        
        String lsSQL = getSQ_Master();
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox")) +
                                                " AND a.sClientID = " + SQLUtil.toSQL(poMaster.getString("sClientID")) +                                                
                                                //" AND a.sEmployID = " + SQLUtil.toSQL(poMaster.getString("sEmployID"))+
                                                " AND (a.cTranStat = '0' or a.cTranStat = '1' or a.cTranStat = '3' or a.cTranStat = '5') "); 
                                                //" AND (a.cTranStat <> '4' or a.cTranStat <> '3' or a.cTranStat <> '5') "); 
                                                //" AND a.dBirthDte = " + SQLUtil.toSQL(formattedDate));
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
                String sStatus = "";
                psMessage = "Existing Inquiry of customer.";
                if(loRS.next()){
                    switch(loRS.getString("cTranStat")){
                    case "0"://For Follow-up
                        sStatus = "FOR FOLLOW-UP";
                        break;
                    case "1"://On Process
                        sStatus = "ON PROCESS";
                        break;
                    case "3"://VSP
                        sStatus = "VSP";
                        break;
                    case "5"://Retired
                        sStatus = "RETIRED";
                        break;
                }
                psMessage = (String) getMaster("sCompnyNm") + " has existing Inquiry with the " + sStatus + " status.";
                
                if(!((String) getMaster("sSalesExe")).toUpperCase().equals(loRS.getString("sSalesExe").toUpperCase())){
                    psMessage = "Existing Inquiry of Customer with Sales Executive: " + loRS.getString("sSalesExe") + ".";
                } 
                
            }
            
            MiscUtil.close(loRS);        
            return false;
        }
                                  
        return true;
    }
}


