/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.sales.base;

import java.io.File;
import java.io.FileNotFoundException;
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
 * Date Created : 01-17-2024
 */
public class SalesAgentExecutiveMaster {
    private String MASTER_TABLE = "sales_agent";
    private final String DEFAULT_DATE = "1900-01-01";
//    private final String psFile = TabsStateManager.getJsonFileName("Sales Agent Information");
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autapp_json/" ;
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private boolean pbisAgent;
    
    private CachedRowSet poMaster;
    private CachedRowSet poMasterOrig;
    private CachedRowSet poDetail;
    private CachedRowSet poTransactions;
    
    public SalesAgentExecutiveMaster(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
    
    public void setFormType (boolean bisAgent){
        pbisAgent = bisAgent;
        if(pbisAgent){
            MASTER_TABLE = "sales_agent";
        } else {
            MASTER_TABLE = "sales_executive";
        }
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
            Logger.getLogger(SalesAgentExecutiveMaster.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }
    
    private void saveState(String fsValue){
        if(pnEditMode == EditMode.UNKNOWN){
            return;
        }
        String sFile = "";
        if(pbisAgent){
            sFile = FILE_PATH + TabsStateManager.getJsonFileName("Sales Agent");
        } else {
            sFile = FILE_PATH + TabsStateManager.getJsonFileName("Sales Executive");
        }
        
        File Delfile = new File(FILE_PATH);
        if (Delfile.exists() && Delfile.isFile()) {
        } else {
            return;
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
    
    public boolean loadState() throws ParseException {
        try {
            String lsTransCd = "";
            String tempValue = "";
            String sFile = "";
            if(pbisAgent){
                sFile = FILE_PATH + TabsStateManager.getJsonFileName("Sales Agent");
            } else {
                sFile = FILE_PATH + TabsStateManager.getJsonFileName("Sales Executive");
            }
            
            File Delfile = new File(sFile);
            if (Delfile.exists() && Delfile.isFile()) {
            } else {
                psMessage   = "";
                pnEditMode = EditMode.UNKNOWN;
                return false;
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
       
        } catch (SQLException ex) {
            Logger.getLogger(SalesAgentExecutiveMaster.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SalesAgentExecutiveMaster.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SalesAgentExecutiveMaster.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poMaster.first();
        
        switch (fnIndex){     
            case 1:  //sClientID 
            case 2:  //cRecdStat 
            case 3:  //sLastName 
            case 4:  //sFrstName 
            case 5:  //sMiddName 
            case 6:  //sCompnyNm 
            case 7:  //sMobileNo 
            case 8:  //sAccountx 
            case 9:  //sEmailAdd 
            case 10:  //sAddressx
            case 11: //cClientTp 
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
        }
//        saveState(toJSONString());
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
        
        poDetail.absolute(fnRow);
        return poDetail.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poDetail, fsIndex));
    }
    
    public int getDetailCount() throws SQLException{
        if (poDetail != null){
            poDetail.last();
            return poDetail.getRow();
        }else{
            return 0;
        }
    }
    
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
            String lsSQL = getSQ_Master() + " WHERE 0=1";
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);    
            if(pbisAgent){
                poMaster.updateString("cRecdStat", "0");   //0 Inactve / For approval, 1 Active, 2 Dis Approve
            } else {
                poMaster.updateString("cRecdStat", "1"); 
            }
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
     *  Searches for a Job Order record.
    */
    public boolean SearchRecord() throws SQLException{
        String lsSQL = getSQ_Master();
        
        JSONObject loJSON = null;
        if (pbWithUI){
            loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , "%"
                                                    , "Sales ID»Sales Name»"
                                                    , "sClientID»sCompnyNm"
                                                    , "a.sClientID»b.sCompnyNm"
                                                    , 0);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                if (OpenRecord((String) loJSON.get("sClientID")) ){
                    
                }else {
                    psMessage = "No record found/selected.";
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean loadList(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            lsSQL = getSQ_Master() + " GROUP BY a.sClientID ";
//            if(fsByCode){
//                 lsSQL = getSQ_Master() + " WHERE a.sClientID LIKE " + SQLUtil.toSQL(fsValue + "%") + " GROUP BY a.sClientID ";
//            } else {
//                lsSQL = getSQ_Master() + " WHERE b.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%") + " GROUP BY a.sClientID ";
//            }
            
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            poDetail = factory.createCachedRowSet();
            poDetail.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException ex) {
            Logger.getLogger(SalesAgentExecutiveMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    /**
     * Opens a record with the specified value.
     * @param fsValue the value used to open the record
        * 
    */
    public boolean OpenRecord(String fsValue){
        try {
            String lsSQL = getSQ_Master() + " WHERE a.sClientID = " + SQLUtil.toSQL(fsValue)  + " GROUP BY a.sClientID " ;
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
        //            if(((String) getMaster(2)).equals("0")){
//                pnEditMode = EditMode.UNKNOWN;
//            } else {
//                pnEditMode = EditMode.READY;
//            }
        pnEditMode = EditMode.READY;
        return true;
    }    
    
    /**
     * Prepares to update a record in the data.
     * This method creates copies of the original data to be updated and sets the edit mode to UPDATE.
     * 
    */
    public boolean UpdateRecord(){
        try {
            if (poMaster != null){
                poMasterOrig = (CachedRowSet) poMaster.createCopy();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SalesAgentExecutiveMaster.class.getName()).log(Level.SEVERE, null, ex);
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
            String lsSQL = "";
            String lsExclude = "";
            if (!isEntryOK()) return false;
            
            if(pbisAgent){
                lsExclude = "sCompnyNm»sAddressx»sLastName»sFrstName»sMiddName»sMobileNo»sAccountx»sEmailAdd»cClientTp";
            } else {
                lsExclude = "sCompnyNm»sAddressx»sLastName»sFrstName»sMiddName»sMobileNo»sAccountx»sEmailAdd»cClientTp»sModified»dModified";
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            if (pnEditMode == EditMode.ADDNEW){ //add
                /*MASTER*/
//                poMaster.updateString("sEntryByx", poGRider.getUserID());
//                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                if(pbisAgent){
                    poMaster.updateString("sModified", poGRider.getUserID());
                    poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                    poMaster.updateRow();  
                }
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, lsExclude);
                
                
                if (lsSQL.isEmpty()){
                    psMessage = "No record to update in master.";
                    return false;
                }
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                    psMessage = "ADD MASTER: " + poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                    return false;
                }
            
            } else { //update  
                boolean lbisModified = false;
                if (!CompareRows.isRowEqual(poMaster, poMasterOrig,1)) {
                    lbisModified = true;
                }
                if(lbisModified){
                    /*MASTER*/
                    if(pbisAgent){
                        poMaster.updateString("sModified", poGRider.getUserID());
                        poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                        poMaster.updateRow();  
                    }
                    lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                                MASTER_TABLE, 
                                                lsExclude, 
                                                "sClientID = " + SQLUtil.toSQL((String) getMaster("sClientID")));
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }
                    if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                        psMessage = "UPDATE MASTER: " + poGRider.getErrMsg();
                        if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                    }
                }
                
                if (poMaster != null){
                    poMasterOrig = (CachedRowSet) poMaster.createCopy();
                }
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
     * 
    */
    private boolean isEntryOK() throws SQLException{
        poMaster.first();   
        
//        if(poMaster.getString("cRecdStat").equals("0")){
//            if(getVSPTransCount() > 0){
//                psMessage = "You cannot deactivate agent that already settled transaction.";
//                return false;
//            }
//        }
         
        
        
        return true;
    }
    
    private String getSQ_Master(){
        String lsSql = "";
        if(pbisAgent){
            lsSql =  " SELECT "                                                                                                                    
                + "   IFNULL(a.sClientID,'') sClientID "   //1  sClientID                                                                                             
                + " , IFNULL(a.cRecdStat,'') cRecdStat "   //2  sClientID                                                                                              
                + " , IFNULL(b.sLastName,'') sLastName "   //3  sLastName                                                                                                
                + " , IFNULL(b.sFrstName,'') sFrstName "   //4  sFrstName                                                                                               
                + " , IFNULL(b.sMiddName,'') sMiddName "   //5  sMiddName                                                                                                
                + " , IFNULL(b.sCompnyNm,'') sCompnyNm "   //6  sCompnyNm                                                                                                
                + " , IFNULL(c.sMobileNo,'') sMobileNo "  //7   sMobileNo                                                                          
                + " , IFNULL(d.sAccountx,'') sAccountx "  //8   sAccountx                                                                          
                + " , IFNULL(e.sEmailAdd,'') sEmailAdd "  //9   sEmailAdd                                                                          
                + " , IFNULL(CONCAT( IFNULL(CONCAT(f.sAddressx,', ') , ''), "                                                           
                + " IFNULL(CONCAT(h.sBrgyName,', '), ''),                   "                                                           
                + " IFNULL(CONCAT(g.sTownName, ', '),''),                   "                                                           
                + " IFNULL(CONCAT(i.sProvName),'') )	, '') AS sAddressx    " //10  sAddressx                                                       
                + " , IFNULL(b.cClientTp,'') cClientTp                      " //11 cClientTp                                                       
                + " , IFNULL(a.sModified,'') sModified                      " //12 sModified                                                       
                + " , a.dModified                     " //13 dModified  
                + " FROM sales_agent a                                      "                                                        
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID  "                                                        
                + " LEFT JOIN client_mobile c ON c.sClientID = a.sClientID AND c.cPrimaryx = '1' AND c.cRecdStat = '1'            "      
                + " LEFT JOIN client_social_media d ON  d.sClientID = a.sClientID AND d.cRecdStat = '1'                           "      
                + " LEFT JOIN client_email_address e ON  e.sClientID = a.sClientID AND e.cPrimaryx = '1' AND e.cRecdStat = '1'    "      
                + " LEFT JOIN client_address f ON f.sClientID = a.sClientID AND f.cPrimaryx = '1'                                 "      
                + " LEFT JOIN TownCity g ON g.sTownIDxx = f.sTownIDxx                                                             "      
                + " LEFT JOIN barangay h ON h.sBrgyIDxx = f.sBrgyIDxx AND h.sTownIDxx = f.sTownIDxx                               "      
                + " LEFT JOIN Province i ON i.sProvIDxx = g.sProvIDxx                                                             "   ;  
        } else {
            lsSql =   " SELECT  "                                                                                                                                                                                    
                    + "  IFNULL(a.sClientID,'')    sClientID,  "                                                                                                                                                 
                    + "  IFNULL(a.cRecdStat,'')    cRecdStat,  "                                                                                                                                                 
                    + "  IFNULL(b.sLastName,'')    sLastName,  "                                                                                                                                                 
                    + "  IFNULL(b.sFrstName,'')    sFrstName,  "                                                                                                                                                 
                    + "  IFNULL(b.sMiddName,'')    sMiddName,  "                                                                                                                                                 
                    + "  IFNULL(b.sCompnyNm,'')    sCompnyNm,  "                                                                                                                                                 
                    + "  IFNULL(c.sMobileNo,'')    sMobileNo,  "                                                                                                                                                 
                    + "  ''    		    	   sAccountx,  "                                                                                                                                                 
                    + "  IFNULL(e.sEmailAdd,'')    sEmailAdd,  "                                                                                                                                                 
                    + "  IFNULL(CONCAT( IFNULL(CONCAT(f.sAddressx,', '), ''), IFNULL(CONCAT(h.sBrgyName,', '), ''), IFNULL(CONCAT(g.sTownName, ', '),''), IFNULL(CONCAT(i.sProvName),'') ), '') AS sAddressx,  " 
                    + "  IFNULL(b.cClientTp,'')    cClientTp   "                                                                                                                                                  
                    + "  FROM sales_executive a                "                                                                                                                                                 
                    + "  LEFT JOIN GGC_ISysDBF.Client_Master b ON b.sClientID = a.sClientID    "                                                                                                                 
                    + "  LEFT JOIN GGC_ISysDBF.Client_Mobile c ON c.sClientID = a.sClientID AND c.nPriority = 1 AND c.cRecdStat = '1' "                                                                          
                    + "  LEFT JOIN GGC_ISysDBF.Client_eMail_Address e ON e.sClientID = a.sClientID AND e.nPriority = 1                "                                                                          
                    + "  LEFT JOIN GGC_ISysDBF.Client_Address f ON f.sClientID = a.sClientID AND f.nPriority = 1                      "                                                                          
                    + "  LEFT JOIN GGC_ISysDBF.TownCity g ON g.sTownIDxx = f.sTownIDxx                                                "                                                                          
                    + "  LEFT JOIN GGC_ISysDBF.barangay h ON h.sBrgyIDxx = f.sBrgyIDxx AND h.sTownIDxx = f.sTownIDxx                  "                                                                          
                    + "  LEFT JOIN GGC_ISysDBF.Province i ON i.sProvIDxx = g.sProvIDxx                                                " ;                                                                         

//            lsSql =  " SELECT "                                                                                                                    
//                + "   IFNULL(a.sClientID,'') sClientID "   //1  sClientID                                                                                             
//                + " , IFNULL(a.cRecdStat,'') cRecdStat "   //2  sClientID                                                                                              
//                + " , IFNULL(b.sLastName,'') sLastName "   //3  sLastName                                                                                                
//                + " , IFNULL(b.sFrstName,'') sFrstName "   //4  sFrstName                                                                                               
//                + " , IFNULL(b.sMiddName,'') sMiddName "   //5  sMiddName                                                                                                
//                + " , IFNULL(b.sCompnyNm,'') sCompnyNm "   //6  sCompnyNm                                                                                                
//                + " , IFNULL(c.sMobileNo,'') sMobileNo "  //7   sMobileNo                                                                          
//                + " , '' sAccountx "  //8   sAccountx                                                                          
//                + " , IFNULL(e.sEmailAdd,'') sEmailAdd "  //9   sEmailAdd                                                                          
//                + " , IFNULL(CONCAT( IFNULL(CONCAT(f.sAddressx,', ') , ''), "                                                           
//                + " IFNULL(CONCAT(h.sBrgyName,', '), ''),                   "                                                           
//                + " IFNULL(CONCAT(g.sTownName, ', '),''),                   "                                                           
//                + " IFNULL(CONCAT(i.sProvName),'') )	, '') AS sAddressx    " //10  sAddressx                                                       
//                + " , IFNULL(b.cClientTp,'') cClientTp                      " //11 cClientTp                                                         
//                + " FROM sales_executive a                                      "                                                        
//                + " LEFT JOIN ggc_isysdbf.client_master b ON b.sClientID = a.sClientID  "                                                        
//                + " LEFT JOIN ggc_isysdbf.client_mobile c ON c.sClientID = a.sClientID AND c.nPriority = 1 AND c.cRecdStat = '1'            "      
//                //+ " LEFT JOIN ggc_isysdbf.client_social_media d ON  d.sClientID = a.sClientID AND d.cRecdStat = '1'                           "      
//                + " LEFT JOIN ggc_isysdbf.client_email_address e ON  e.sClientID = a.sClientID AND e.nPriority = 1     "      //AND e.cRecdStat = '1'
//                + " LEFT JOIN ggc_isysdbf.client_address f ON f.sClientID = a.sClientID AND f.nPriority = 1                                 "      
//                + " LEFT JOIN ggc_isysdbf.TownCity g ON g.sTownIDxx = f.sTownIDxx                                                             "      
//                + " LEFT JOIN ggc_isysdbf.barangay h ON h.sBrgyIDxx = f.sBrgyIDxx AND h.sTownIDxx = f.sTownIDxx                               "      
//                + " LEFT JOIN ggc_isysdbf.Province i ON i.sProvIDxx = g.sProvIDxx                                                             "   ;  
        }
        
        return  lsSql ; 
    }   
    
    private String getSQ_VSPTransaction(){
        return    " SELECT "                                                                                 
                + " IFNULL(a.sTransNox,'') AS sTransNox	   "                                               
                + " ,IFNULL(a.sVSPNOxxx,'') AS sVSPNOxxx   "                                               
                + " , a.dTransact AS dTransact             "                                               
                + " , IFNULL(a.sBranchCd,'') AS sBranchCd  "                                               
                + " , IFNULL(q.sBranchNm,'') AS sBranchNm  "                                               
                + " , IFNULL(b.sClientID,'') AS  sInqCltID "                                               
                + " , IFNULL(UPPER(c.sCompnyNm),'') AS  sInqCltNm "                                               
                + " , IFNULL(UPPER(CONCAT( IFNULL(CONCAT(d.sAddressx,', ') , ''),     "                          
                + "     	IFNULL(CONCAT(f.sBrgyName,', '), ''),           "                          
                + "     	IFNULL(CONCAT(e.sTownName, ', '),''),           "                          
                + "     	IFNULL(CONCAT(g.sProvName),'') ))	, '') AS sInqCtAdd  "                          
                + " , IFNULL(UPPER(h.sCompnyNm),'') AS  sBuyCltNm                      "                          
                + " , IFNULL(UPPER(CONCAT( IFNULL(CONCAT(i.sAddressx,', ') , ''),     "                          
                + "     	IFNULL(CONCAT(k.sBrgyName,', '), ''),           "                          
                + "     	IFNULL(CONCAT(j.sTownName, ', '),''),           "                          
                + "     	IFNULL(CONCAT(l.sProvName),'') ))	, '') AS sBuyCtAdd  "						               
                + " , IFNULL(UPPER(p.sPlatform),'') AS  sPlatForm    "                                            
                + " , IFNULL(UPPER(r.sCompnyNm),'') AS  sSaleExNm    "                                            
                + " , IFNULL(UPPER(s.sCompnyNm),'') AS  sSalesAgn    "                                            
                + " , IFNULL(a.sTransNox,'') AS  sVSPCodex    "                                            
                + " , IFNULL(a.sVSPNOxxx,'') AS  sVSPNOxxx    "                                            
                + " , a.dTransact AS  dVSPDatex    	      "															               
                + " , IFNULL(m.sCSNoxxxx,'') AS sCSNoxxxx     "																						 
                + " , IFNULL(n.sPlateNox,'') AS sPlateNox     "																						 
                + " , IFNULL(UPPER(m.sFrameNox),'') AS sFrameNox     "																						 
                + " , IFNULL(UPPER(m.sEngineNo),'') AS sEngineNo     "                                            
                + " , IFNULL(UPPER(o.sDescript),'') AS sDescript     "                                            
                + " , IFNULL(t.sTransNox,'') AS sUDRCodex     "                                            
                + " , IFNULL(t.sReferNox,'') AS sUDRNoxxx     "                                            
                + " , t.dTransact AS dUDRDatex    	      "                                            
                + " FROM vsp_master a                         "                                            
                + " LEFT JOIN customer_inquiry b ON b.sTransNox = a.sInqryIDx  "                           
                /*inquiring customer*/                                                                     
                + " LEFT JOIN client_master c ON c.sClientID = b.sClientID     "                           
                + " LEFT JOIN client_address d ON d.sClientID = c.sClientID AND d.cPrimaryx = '1'     "    
                + " LEFT JOIN TownCity e ON e.sTownIDxx = d.sTownIDxx                                 "    
                + " LEFT JOIN barangay f ON f.sBrgyIDxx = d.sBrgyIDxx AND f.sTownIDxx = e.sTownIDxx   "    
                + " LEFT JOIN Province g ON g.sProvIDxx = e.sProvIDxx                                 "    
                /*buying customer*/                                                                        
                + " LEFT JOIN client_master h ON h.sClientID = a.sClientID                            "    
                + " LEFT JOIN client_address i ON i.sClientID = c.sClientID AND i.cPrimaryx = '1'     "    
                + " LEFT JOIN TownCity j ON j.sTownIDxx = i.sTownIDxx                                 "    
                + " LEFT JOIN barangay k ON k.sBrgyIDxx = i.sBrgyIDxx AND k.sTownIDxx = j.sTownIDxx   "    
                + " LEFT JOIN Province l ON l.sProvIDxx = j.sProvIDxx                                 "    
                /*vehicle information*/  								                                                   
                + " LEFT JOIN vehicle_serial m ON m.sSerialID = a.sSerialID       										"	   
                + " LEFT JOIN vehicle_serial_registration n ON n.sSerialID = m.sSerialID              "    
                + " LEFT JOIN vehicle_master o ON o.sVhclIDxx = m.sVhclIDxx                           "    
                /*inquiry information*/                                                                    
                + " LEFT JOIN online_platforms p ON p.sTransNox = b.sSourceNo                         "    
                + " LEFT JOIN branch q ON q.sBranchCd = a.sBranchCd                                   "    
                + " LEFT JOIN GGC_ISysDBF.Client_Master r ON r.sClientID = b.sEmployID                "    
                + " LEFT JOIN client_master s ON s.sClientID = b.sAgentIDx                            "    
                /*udr information*/                                                                        
                + " INNER JOIN udr_master t ON t.sSourceCd = a.sTransNox AND t.cTranStat = '1'        ";   

//        return    " SELECT "                                                                                           
//                + " IFNULL(a.sTransNox,'') AS sTransNox	   "                                                                           
//                + " IFNULL(b.sVSPNOxxx,'') AS sVSPNOxxx	   "                                                          
//                + " , a.dTransact AS dTransact             "                                                           
//                + " , IFNULL(a.sBranchCd,'') AS sBranchCd  "                                                           
//                + " , IFNULL(q.sBranchNm,'') AS sBranchNm  "                                                           
//                + " , IFNULL(a.sClientID,'') AS  sInqCltID "                                                           
//                + " , IFNULL(c.sCompnyNm,'') AS  sInqCltNm "                                                           
//                + " , IFNULL(CONCAT( IFNULL(CONCAT(d.sAddressx,', ') , ''),    "                                       
//                + "     	IFNULL(CONCAT(f.sBrgyName,', '), ''),                "                                       
//                + "     	IFNULL(CONCAT(e.sTownName, ', '),''),                "                                       
//                + "     	IFNULL(CONCAT(g.sProvName),'') )	, '') AS sInqCtAdd "                                       
//                + " , IFNULL(h.sCompnyNm,'') AS  sBuyCltNm                     "                                       
//                + " , IFNULL(CONCAT( IFNULL(CONCAT(i.sAddressx,', ') , ''),    "                                       
//                + "     	IFNULL(CONCAT(k.sBrgyName,', '), ''),                "                                       
//                + "     	IFNULL(CONCAT(j.sTownName, ', '),''),                "                                       
//                + "     	IFNULL(CONCAT(l.sProvName),'') )	, '') AS sBuyCtAdd "								                       
//                + " , IFNULL(p.sPlatform,'') AS  sPlatForm "                                                           
//                + " , IFNULL(r.sCompnyNm,'') AS  sSaleExNm "                                                           
//                + " , IFNULL(s.sCompnyNm,'') AS  sSalesAgn "                                                           
//                + " , IFNULL(b.sTransNox,'') AS  sVSPCodex "                                                           
//                + " , IFNULL(b.sVSPNOxxx,'') AS  sVSPNOxxx "                                                           
//                + " , b.dTransact AS  dVSPDatex    	   "																			                     
//                + " , IFNULL(m.sCSNoxxxx,'') AS sCSNoxxxx  "  																								         
//                + " , IFNULL(n.sPlateNox,'') AS sPlateNox  "  																								         
//                + " , IFNULL(m.sFrameNox,'') AS sFrameNox  "  																								         
//                + " , IFNULL(m.sEngineNo,'') AS sEngineNo  "                                                           
//                + " , IFNULL(o.sDescript,'') AS sDescript  "                                                           
//                + " , IFNULL(t.sTransNox,'') AS sUDRCodex  "                                                           
//                + " , IFNULL(t.sReferNox,'') AS sUDRNoxxx  "                                                           
//                + " , b.dTransact AS dUDRDatex    	   "                                                           
//                + " FROM customer_inquiry a                "                                                           
//                + " LEFT JOIN vsp_master b ON b.sInqryIDx = a.sTransNox  AND b.cTranStat = '1' "                       
//                /*inquiring customer*/                                                                                 
//                + " LEFT JOIN client_master c ON c.sClientID = a.sClientID  "                                          
//                + " LEFT JOIN client_address d ON d.sClientID = c.sClientID AND d.cPrimaryx = '1'   "                  
//                + " LEFT JOIN TownCity e ON e.sTownIDxx = d.sTownIDxx   "                                              
//                + " LEFT JOIN barangay f ON f.sBrgyIDxx = d.sBrgyIDxx AND f.sTownIDxx = e.sTownIDxx "                  
//                + " LEFT JOIN Province g ON g.sProvIDxx = e.sProvIDxx   "                                              
//                /*buying customer*/                                                                                    
//                + " LEFT JOIN client_master h ON h.sClientID = b.sClientID "                                           
//                + " LEFT JOIN client_address i ON i.sClientID = c.sClientID AND i.cPrimaryx = '1'   "                  
//                + " LEFT JOIN TownCity j ON j.sTownIDxx = i.sTownIDxx     "                                            
//                + " LEFT JOIN barangay k ON k.sBrgyIDxx = i.sBrgyIDxx AND k.sTownIDxx = j.sTownIDxx "                  
//                + " LEFT JOIN Province l ON l.sProvIDxx = j.sProvIDxx     "                                            
//                /*vehicle information*/  								                                                               
//                + " LEFT JOIN vehicle_serial m ON m.sSerialID = b.sSerialID  "      											             
//                + " LEFT JOIN vehicle_serial_registration n ON n.sSerialID = m.sSerialID  "                            
//                + " LEFT JOIN vehicle_master o ON o.sVhclIDxx = m.sVhclIDxx  "                                         
//                /*inquiry information*/                                                                                
//                + " LEFT JOIN online_platforms p ON p.sTransNox = a.sSourceNo "                                        
//                + " LEFT JOIN branch q ON q.sBranchCd = a.sBranchCd           "                                        
//                + " LEFT JOIN ggc_isysdbf.client_master r ON r.sClientID = a.sEmployID  "                              
//                + " LEFT JOIN client_master s ON s.sClientID = a.sAgentIDx   "                                         
//                /*udr information*/                                                                                    
//                + " LEFT JOIN udr_master t ON t.sSourceCd = b.sTransNox AND t.cTranStat = '1'  "  ;                     
    }
//        return    " SELECT  "                                                                                                             
//                + "   IFNULL(a.sTransNox, '') AS sTransNox      "                                                         
//                + "    ,a.dTransact                             "                                                         
//                + "    ,IFNULL(a.sVSPNOxxx, '') AS sVSPNOxxx    "                                                         
//                + "    ,a.dDelvryDt                             "                                                         
//                + "    ,IFNULL(a.sInqryIDx, '') AS sInqryIDx    "                                                         
//                + "    ,IFNULL(a.sClientID, '') AS sClientID    "                                                         
//                + "    ,IFNULL(a.sSerialID, '') AS sSerialID    "                                                         
//                + "    , IFNULL(c.sCompnyNm,'') AS sCompnyNm    "                                                         
//                + "    , IFNULL(CONCAT( IFNULL(CONCAT(h.sAddressx,', ') , ''),  "                                         
//                + "    	IFNULL(CONCAT(j.sBrgyName,', '), ''),   "                                                         
//                + "    	IFNULL(CONCAT(i.sTownName, ', '),''),   "                                                         
//                + "    	IFNULL(CONCAT(k.sProvName),'') )	, '') AS sAddressx  "                                           
//                + "    , IFNULL(f.sDescript,'') AS sDescript    "																										      
//                + "    , IFNULL(d.sCSNoxxxx,'') AS sCSNoxxxx    "																										      
//                + "    , IFNULL(e.sPlateNox,'') AS sPlateNox    "																										      
//                + "    , IFNULL(d.sFrameNox,'') AS sFrameNox    "																										      
//                + "    , IFNULL(d.sEngineNo,'') AS sEngineNo    "                                                         
//                + "    ,IFNULL(g.sClientID, '') AS sSalExeID    "                                                         
//                + "    ,IFNULL(s.sCompnyNm, '') AS sSalesExe    "                                                         
//                + "    ,IFNULL(q.sClientID, '') AS sAgentIDx    "                                                         
//                + "    ,IFNULL(r.sCompnyNm, '') AS sSalesAgn    "                                                         
//                + "    ,IFNULL (b.dTransact, '') AS  dInqDatex  "                                                         
//                + "    ,IFNULL(l.sReferNox, '') AS sUdrNoxxx    "                                                         
//                + "    ,IFNULL (b.sSourceCD, '') AS  sInqTypex  "                                                         
//                + "    , IFNULL(m.sPlatform, '') AS sOnlStore   "                                                         
//                + "    , '' AS  sRefTypex                       "                                                         
//                + "    , IFNULL(d.sKeyNoxxx,'') AS sKeyNoxxx    "                                                         
//                + "    , IFNULL(n.sBranchNm,'') AS sBranchNm    "                                                         
//                + "    , CASE WHEN a.cTranStat = '0' THEN 'Y' ELSE 'N' END AS cTrStatus "                                 
//                + "    , IFNULL(CONCAT( IFNULL(CONCAT(n.sAddressx,', ') , ''),          "                                 
//                + "      IFNULL(CONCAT(o.sTownName, ', '),''),                          "                                 
//                + "      IFNULL(CONCAT(p.sProvName),'') ), '') AS sBrnchAdd             "                                 
//                + "      FROM vsp_master a                                              "                                 
//                + "      LEFT JOIN customer_inquiry b ON b.sTransNox = a.sInqryIDx      "                                 
//                + "      LEFT JOIN client_master c ON c.sClientID = a.sClientID         "														      
//                + "      LEFT JOIN vehicle_serial d ON d.sSerialID = a.sSerialID        "													        
//                + "      LEFT JOIN vehicle_serial_registration e ON e.sSerialID = d.sSerialID "                           
//                + "      LEFT JOIN vehicle_master f ON f.sVhclIDxx = d.sVhclIDxx              "                           
//                + "      LEFT JOIN sales_executive g ON g.sClientID = b.sEmployID             "                           
//                + "      LEFT JOIN client_address h ON h.sClientID = c.sClientID AND h.cPrimaryx = '1' "                  
//                + "      LEFT JOIN TownCity i ON i.sTownIDxx = h.sTownIDxx                             "                  
//                + "      LEFT JOIN barangay j ON j.sBrgyIDxx = h.sBrgyIDxx AND j.sTownIDxx = h.sTownIDxx "                
//                + "      LEFT JOIN Province k ON k.sProvIDxx = i.sProvIDxx              "                                 
//                + "      LEFT JOIN udr_master l ON l.sSourceCd = a.sTransNox AND l.cTranStat = '1'  "                     
//                + "      LEFT JOIN online_platforms m ON m.sTransNox = b.sSourceNo      "                                 
//                + "      LEFT JOIN branch n ON n.sBranchCd = b.sBranchCd                "                                 
//                + "  	 LEFT JOIN TownCity o ON o.sTownIDxx = n.sTownIDxx              "                                 
//                + "      LEFT JOIN Province p ON p.sProvIDxx = o.sProvIDxx              "                                 
//                + "      LEFT JOIN sales_agent q ON q.sClientID = b.sAgentIDx           "                                 
//                + "      LEFT JOIN client_master r ON r.sClientID = q.sClientID         "                                 
//                + "      LEFT JOIN client_master s ON s.sClientID = g.sClientID         "     ;                           
    
    
    public boolean loadTransactions(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL = getSQ_VSPTransaction();
            if(pbisAgent){
                lsSQL = MiscUtil.addCondition(lsSQL, "b.sAgentIDx = " + SQLUtil.toSQL((String) getMaster(1)))
                                                    + " AND a.cTranStat = '1' "
                                                    + " GROUP BY a.sTransNox ORDER BY a.dTransact DESC " ;
            } else {
                lsSQL = MiscUtil.addCondition(lsSQL, " b.sEmployID = " + SQLUtil.toSQL((String) getMaster(1)))
                                                    + " AND a.cTranStat = '1'  "
                                                    + " GROUP BY a.sTransNox ORDER BY a.dTransact DESC " ;
            
            }
            
            System.out.println("VSP : "+ lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            loRS = poGRider.executeQuery(lsSQL);
            poTransactions = factory.createCachedRowSet();
            poTransactions.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(SalesAgentExecutiveMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    public int getVSPTransCount() throws SQLException{
        if (poTransactions != null){
            poTransactions.last();
            return poTransactions.getRow();
        }else{
            return 0;
        }
    }
    
    public Object getVSPTransDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poTransactions.absolute(fnRow);
        return poTransactions.getObject(fnIndex);
    }
    
    public Object getVSPTransDetail(int fnRow, String fsIndex) throws SQLException{
        return getVSPTransDetail(fnRow, MiscUtil.getColumnIndex(poTransactions, fsIndex));
    }
    
    private String getSQ_Agent(){
        return  "SELECT "
                + " a.sClientID " 
                + ", a.sLastName " 
                + ", a.sFrstName "
                + ", a.sMiddName "
                + ", a.sCompnyNm "                  
                + ", IFNULL(b.sMobileNo,'') sMobileNo "
                + ", IFNULL(c.sAccountx,'') sAccountx "
                + ", IFNULL(d.sEmailAdd,'') sEmailAdd "
                + ", IFNULL(CONCAT( IFNULL(CONCAT(e.sAddressx,', ') , ''),  "
                + "IFNULL(CONCAT(i.sBrgyName,', '), ''), " 
                + "IFNULL(CONCAT(h.sTownName, ', '),''), " 
                + "IFNULL(CONCAT(j.sProvName),'') )	, '') AS sAddressx "
                + ", IFNULL(a.cClientTp,'') cClientTp "
                + "FROM client_master a "
                + "LEFT JOIN client_mobile b ON b.sClientID = a.sClientID and b.cPrimaryx = 1 and b.cRecdStat = 1  "
                + "LEFT JOIN client_social_media c ON  c.sClientID = a.sClientID and c.cRecdStat = 1 "
                + "LEFT JOIN client_email_address d ON  d.sClientID = a.sClientID AND d.cPrimaryx = 1 AND d.cRecdStat = 1 "
                + "LEFT JOIN client_address e ON e.sClientID = a.sClientID AND e.cPrimaryx = '1' "
                + "LEFT JOIN TownCity h ON h.sTownIDxx = e.sTownIDxx  "  
                + "LEFT JOIN barangay i ON i.sBrgyIDxx = e.sBrgyIDxx AND i.sTownIDxx = e.sTownIDxx "  
                + "LEFT JOIN Province j ON j.sProvIDxx = h.sProvIDxx "  
                + "WHERE a.cRecdStat = '1' ";
    }
    
    private String getSQ_Executive(){
        return " SELECT  "                                                                                                                                                                                                      
                + "  IFNULL(b.sCompnyNm, '')    sCompnyNm,  "                                                                                                                                                                  
                + "  IFNULL(a.sEmployID, '')    sEmployID,  "                                                                                                                                                                  
                + "  IFNULL(c.sDeptName, '')    sDeptName,  "                                                                                                                                                                  
                + "  IFNULL(a.sBranchCd, '')    sBranchCd,  "                                                                                                                                                                  
                + "  IFNULL(b.sClientID, '')    sClientID,  "                                                                                                                                                                  
                + "  IFNULL(b.sLastName, '')    sLastName,  "                                                                                                                                                                  
                + "  IFNULL(b.sFrstName, '')    sFrstName,  "                                                                                                                                                                  
                + "  IFNULL(b.sMiddName, '')    sMiddName,  "                                                                                                                                                                  
                + "  IFNULL(b.sCompnyNm, '')    sCompnyNm,  "                                                                                                                                                                  
                + "  IFNULL(e.sMobileNo,'')     sMobileNo,  "                                                                                                                                                                  
                + "  ''                         sAccountx,  "                                                                                                                                                                  
                + "  IFNULL(g.sEmailAdd,'')     sEmailAdd,  "                                                                                                                                                                  
                + "  IFNULL(CONCAT( IFNULL(CONCAT(h.sAddressx,', '), ''), IFNULL(CONCAT(j.sBrgyName,', '), ''), IFNULL(CONCAT(i.sTownName, ', '),''), IFNULL(CONCAT(k.sProvName),'') ), '') AS sAddressx, "                    
                + "  IFNULL(b.cClientTp,'')     cClientTp   "                                                                                                                                                                  
                + " FROM GGC_ISysDBF.Employee_Master001 a   "                                                                                                                                                                   
                + "  LEFT JOIN GGC_ISysDBF.Client_Master b ON b.sClientID = a.sEmployID   "                                                                                                                                    
                + "  LEFT JOIN GGC_ISysDBF.Department c ON c.sDeptIDxx = a.sDeptIDxx      "                                                                                                                                    
                + "  LEFT JOIN GGC_ISysDBF.Branch_Others d ON d.sBranchCD = a.sBranchCd   "                                                                                                                                    
                + "  LEFT JOIN GGC_ISysDBF.Client_Mobile e ON e.sClientID = b.sClientID AND e.nPriority = 1 AND e.cRecdStat = 1 "                                                                                              
                + "  LEFT JOIN GGC_ISysDBF.Client_eMail_Address g ON g.sClientID = b.sClientID AND g.nPriority = 1              "                                                                                              
                + "  LEFT JOIN GGC_ISysDBF.Client_Address h ON h.sClientID = b.sClientID AND h.nPriority = 1                    "                                                                                              
                + "  LEFT JOIN GGC_ISysDBF.TownCity i ON i.sTownIDxx = h.sTownIDxx                                              "                                                                                              
                + "  LEFT JOIN GGC_ISysDBF.Barangay j ON j.sBrgyIDxx = h.sBrgyIDxx AND j.sTownIDxx = i.sTownIDxx                "                                                                                              
                + "  LEFT JOIN GGC_ISysDBF.Province k ON k.sProvIDxx = i.sProvIDxx                                              "                                                                                              
                + " WHERE b.cRecdStat = '1'                                                                                     "                                                                                              
                + "    AND (c.sDeptIDxx = 'a011'OR c.sDeptIDxx = '015') AND ISNULL(a.dFiredxxx)                                 "                                                                                              
                + "    AND d.cDivision = (SELECT cDivision FROM GGC_ISysDBF.Branch_Others WHERE sBranchCd = 'V001')             "  ;                                                                                        

//        return " SELECT " +
//                    " IFNULL(b.sCompnyNm, '') sCompnyNm " +
//                    " ,IFNULL(a.sEmployID, '') sEmployID " +
//                    " ,IFNULL(c.sDeptName, '') sDeptName " +
//                    " ,IFNULL(a.sBranchCd, '') sBranchCd " 
//                + ", b.sClientID " 
//                + ", b.sLastName " 
//                + ", b.sFrstName "
//                + ", b.sMiddName "
//                + ", b.sCompnyNm "                  
//                + ", IFNULL(e.sMobileNo,'') sMobileNo "
//                + ", '' sAccountx "
//                + ", IFNULL(g.sEmailAdd,'') sEmailAdd "
//                + ", IFNULL(CONCAT( IFNULL(CONCAT(h.sAddressx,', ') , ''),  "
//                + "IFNULL(CONCAT(j.sBrgyName,', '), ''), " 
//                + "IFNULL(CONCAT(i.sTownName, ', '),''), " 
//                + "IFNULL(CONCAT(k.sProvName),'') )	, '') AS sAddressx "
//                + ", IFNULL(b.cClientTp,'') cClientTp " +
//                " FROM ggc_isysdbf.employee_master001 a " +
//                " LEFT JOIN ggc_isysdbf.client_master b ON b.sClientID = a.sEmployID " +
//                " LEFT JOIN ggc_isysdbf.department c ON c.sDeptIDxx = a.sDeptIDxx " +
//                " LEFT JOIN ggc_isysdbf.branch_others d ON d.sBranchCD = a.sBranchCd  " 
//                
//                + "LEFT JOIN ggc_isysdbf.client_mobile e ON e.sClientID = b.sClientID and e.nPriority = 1 and e.cRecdStat = 1  "
//                //+ "LEFT JOIN client_social_media f ON  f.sClientID = b.sClientID and f.cRecdStat = 1 "
//                + "LEFT JOIN ggc_isysdbf.client_email_address g ON  g.sClientID = b.sClientID AND g.nPriority = 1 " //AND d.cRecdStat = 1 
//                + "LEFT JOIN ggc_isysdbf.client_address h ON h.sClientID = b.sClientID AND h.nPriority = 1 "
//                + "LEFT JOIN ggc_isysdbf.TownCity i ON i.sTownIDxx = h.sTownIDxx  "  
//                + "LEFT JOIN ggc_isysdbf.barangay j ON j.sBrgyIDxx = h.sBrgyIDxx AND j.sTownIDxx = i.sTownIDxx "  
//                + "LEFT JOIN ggc_isysdbf.Province k ON k.sProvIDxx = i.sProvIDxx "  
//                + " WHERE b.cRecdStat = '1' AND (c.sDeptIDxx = 'a011' or c.sDeptIDxx = '015') AND ISNULL(a.dFiredxxx) AND " 
//                + " d.cDivision = (SELECT cDivision FROM ggc_isysdbf.branch_others WHERE sBranchCd = " +  SQLUtil.toSQL(psBranchCd) + ")" ;
                
//        return  "SELECT "
//                + " a.sClientID " 
//                + ", a.sLastName " 
//                + ", a.sFrstName "
//                + ", a.sMiddName "
//                + ", a.sCompnyNm "                  
//                //+ ", IFNULL(b.sMobileNo,'') sMobileNo "
//                //+ ", '' sAccountx "
//                //+ ", IFNULL(d.sEmailAdd,'') sEmailAdd "
//                //+ ", IFNULL(CONCAT( IFNULL(CONCAT(e.sAddressx,', ') , ''),  "
//                //+ "IFNULL(CONCAT(i.sBrgyName,', '), ''), " 
//                //+ "IFNULL(CONCAT(h.sTownName, ', '),''), " 
//                //+ "IFNULL(CONCAT(j.sProvName),'') )	, '') AS sAddressx "
//                + ", IFNULL(a.cClientTp,'') cClientTp "
//                + "FROM ggc_isysdbf.client_master a "
//                //+ "LEFT JOIN ggc_isysdbf.client_mobile b ON b.sClientID = a.sClientID and b.nPriority = 1 and b.cRecdStat = 1  "
//                //+ "LEFT JOIN client_social_media c ON  c.sClientID = a.sClientID and c.cRecdStat = 1 "
//                //+ "LEFT JOIN ggc_isysdbf.client_email_address d ON  d.sClientID = a.sClientID AND d.nPriority = 1 " //AND d.cRecdStat = 1 
//                //+ "LEFT JOIN ggc_isysdbf.client_address e ON e.sClientID = a.sClientID AND e.nPriority = '1' "
//                //+ "LEFT JOIN ggc_isysdbf.TownCity h ON h.sTownIDxx = e.sTownIDxx  "  
//                //+ "LEFT JOIN ggc_isysdbf.barangay i ON i.sBrgyIDxx = e.sBrgyIDxx AND i.sTownIDxx = e.sTownIDxx "  
//                //+ "LEFT JOIN ggc_isysdbf.Province j ON j.sProvIDxx = h.sProvIDxx "  
//                + "WHERE a.cRecdStat = '1' ";
    }
    
    /**
    Searches for a customer based on the given value.
    @param fsValue The value to search for.
    @return True if a record is found, false otherwise.
    @throws SQLException If a database access error occurs.
    */
    public boolean searchPerson(String fsValue) throws SQLException{
        String lsSQL = "";
        if(pbisAgent){
            lsSQL = getSQ_Agent() + " AND a.cClientTp = '0' AND a.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%") + " GROUP BY a.sClientID";
        }else{
            lsSQL = getSQ_Executive() + " AND b.cClientTp = '0' AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%") + " GROUP BY b.sClientID";
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                if(!checkExist(loRS.getString("sClientID"))){
                    clearInfo(); 
                    return false;
                }
                
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
            //loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            JSONObject loJSON;
            String lsCriteria  = "";
            if(pbisAgent){
                lsCriteria =  "a.sClientID»a.sCompnyNm»e.sAddressx";
            }else{
                lsCriteria = "b.sClientID»b.sCompnyNm»h.sAddressx";
            }
            
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                                    lsSQL, 
                                                    "%"+ fsValue + "%", 
                                                    "ID»Name»Address", 
                                                    "sClientID»sCompnyNm»sAddressx",
                                                    lsCriteria,
                                                     1);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                
                if(!checkExist((String) loJSON.get("sClientID"))){
                    clearInfo();
                    return false;
                }
                
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
    
    private boolean checkExist(String fsValue){
        String lsSQL = getSQ_Master() + " WHERE a.sClientID = " + SQLUtil.toSQL( fsValue ) + " GROUP BY a.sClientID";
        
        System.out.println(lsSQL);
        ResultSet loRS;
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Sales Agent / Executive already exist.";
            MiscUtil.close(loRS);        
            return false;
        }
        return true;
    }
    
    private void clearInfo(){
        try {
            setMaster("sClientID", "");
            setMaster("sCompnyNm", "");
            setMaster("sMobileNo", "");
            setMaster("sAccountx", "");
            setMaster("sEmailAdd", "");
            setMaster("sAddressx", ""); 
            setMaster("cClientTp", "");
        } catch (SQLException ex) {
            Logger.getLogger(SalesAgentExecutiveMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
