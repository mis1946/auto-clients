package org.rmj.auto.clients.base;

import com.mysql.jdbc.SQLError;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
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

public class ClientMaster {
    private final String MASTER_TABLE = "Client_Master";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autapp_json/" + TabsStateManager.getJsonFileName("Customer");
   
//    List<Integer> index = new ArrayList<>();
//    List<String> value = new ArrayList<>();
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
    
    public ClientAddress oTransAddress;
    public ClientSocMed oTransSocMed;
    public ClientEMail oTransEmail;
    public ClientMobile oTransMobile;
    public ClientVehicleInfo oTransVhclInfo;
        
    public ClientMaster(GRider foGRider, String fsBranchCd, boolean fbWithParent){ //, ClientAddress foAddress, ClientMobile foMobile, ClientEMail foEmail, ClientSocMed foSocMed){            
        
        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;
        
//        oTransAddress = new ClientAddress(poGRider, psBranchCd, true);
//        oTransSocMed = new ClientSocMed(poGRider, psBranchCd, true);
//        oTransEmail = new ClientEMail(poGRider, psBranchCd, true);
//        oTransMobile = new ClientMobile(poGRider, psBranchCd, true); 
//        oTransVhclInfo = new ClientVehicleInfo(poGRider, psBranchCd, true); 
        
//        poAddress = new ClientAddress(poGRider, psBranchCd, true);
//        poSocMed = new ClientSocMed(poGRider, psBranchCd, true);
//        poEmail = new ClientEMail(poGRider, psBranchCd, true);
//        poMobile = new ClientMobile(poGRider, psBranchCd, true);         
//        poMobile.setWithUI(false);
//        poAddress.setWithUI(false);
//        poEmail.setWithUI(false);
//        poSocMed.setWithUI(false);
            
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
    
    public void setAddressObject(ClientAddress foValue) {
        oTransAddress = foValue;
    }
    
    public void setMobileObject(ClientMobile foValue) {
        oTransMobile = foValue;
    }
    
    public void setEmailObject(ClientEMail foValue) {
        oTransEmail = foValue;
    }
    
    public void setSocMedObject(ClientSocMed foValue) {
        oTransSocMed = foValue;
    }
    
     public void setVhclInfoObject(ClientVehicleInfo foValue) {
        oTransVhclInfo = foValue;
    }
    
    public String toJSONString(){
        JSONParser loParser = new JSONParser();
        JSONArray laMaster = new JSONArray();
        JSONArray laAddress = new JSONArray();
        JSONArray laMobile = new JSONArray();
        JSONArray laEmail = new JSONArray();
        JSONArray laSocMedia = new JSONArray();
        JSONArray laVhclInfo = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;
        try {
            loJSON = new JSONObject();
            String lsValue =  CommonUtils.RS2JSON(poMaster).toJSONString();
            laMaster = (JSONArray) loParser.parse(lsValue);
            loMaster = (JSONObject) laMaster.get(0);
            loJSON.put("master", loMaster);
            
            if(oTransAddress.poAddress != null){
                lsValue = CommonUtils.RS2JSON(oTransAddress.poAddress).toJSONString();
                laAddress = (JSONArray) loParser.parse(lsValue);
                loJSON.put("address", laAddress);
            }
            
            if(oTransMobile.poMobile != null){
                lsValue = CommonUtils.RS2JSON(oTransMobile.poMobile).toJSONString();
                laMobile = (JSONArray) loParser.parse(lsValue);
                loJSON.put("mobile", laMobile);
            }
            
            if(oTransEmail.poEmail != null){
                lsValue = CommonUtils.RS2JSON(oTransEmail.poEmail).toJSONString();
                laEmail = (JSONArray) loParser.parse(lsValue);
                loJSON.put("email", laEmail);
            }
            
            if(oTransSocMed.poSocMed != null){
                lsValue = CommonUtils.RS2JSON(oTransSocMed.poSocMed).toJSONString();
                laSocMedia = (JSONArray) loParser.parse(lsValue);
                loJSON.put("socmed", laSocMedia);
            }
            
            if(oTransVhclInfo.poVehicle != null){
                lsValue = CommonUtils.RS2JSON(oTransVhclInfo.poVehicle).toJSONString();
                laVhclInfo = (JSONArray) loParser.parse(lsValue);
                loJSON.put("vhclinfo", laVhclInfo);
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
            Logger.getLogger(ClientMaster.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }
    
    public void saveState(String fsValue){
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
                            oTransAddress.NewRecord();
                        } else {
                            psMessage = "Error while setting state to New Record.";
                            return false;
                        }
                        break; 
                    case EditMode.UPDATE:
                        if(OpenRecord(lsTransCd,true)){
                            oTransAddress.OpenRecord(lsTransCd, true);
                            oTransMobile.OpenRecord(lsTransCd, true);
                            oTransEmail.OpenRecord(lsTransCd, true);
                            oTransSocMed.OpenRecord(lsTransCd, true);
                            oTransVhclInfo.OpenRecord(lsTransCd);
                            
                            if(UpdateRecord()){
                                oTransAddress.UpdateRecord();
                                oTransMobile.UpdateRecord();
                                oTransEmail.UpdateRecord();
                                oTransSocMed.UpdateRecord();
                                oTransVhclInfo.UpdateRecord();
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
                        if(OpenRecord(lsTransCd,true)){
                            oTransAddress.OpenRecord(lsTransCd, true);
                            oTransMobile.OpenRecord(lsTransCd, true);
                            oTransEmail.OpenRecord(lsTransCd, true);
                            oTransSocMed.OpenRecord(lsTransCd, true);
                            oTransVhclInfo.OpenRecord(lsTransCd);
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
                        
                        System.out.println(key.toString() + " : " + tempValue);
                        tempValue = "";
                    }
                    poMaster.updateRow();
                    
                    /*ADDRESS*/
                    int row = 1;
                    int ctr = 1;
                    
                    // Extract the "address" array from the JSON object
                    JSONArray addressArray = (JSONArray) jsonObject.get("address");
                    if(addressArray != null) {
                        if(addressArray.size()>0){
                            while (addressArray.size() > oTransAddress.getItemCount()) {
                                String lsSQL;
                                ResultSet loRS;
                                RowSetFactory factory;
                                if (oTransAddress.poAddress == null) {
                                    lsSQL = MiscUtil.addCondition(oTransAddress.getSQ_Address(), "0=1");
                                    loRS = poGRider.executeQuery(lsSQL);
                                    factory = RowSetProvider.newFactory();
                                    oTransAddress.poAddress = factory.createCachedRowSet();
                                    oTransAddress.poAddress.populate(loRS);
                                    MiscUtil.close(loRS);
                                }
                                oTransAddress.poAddress.last();
                                oTransAddress.poAddress.moveToInsertRow();
                                MiscUtil.initRowSet(oTransAddress.poAddress);
                                oTransAddress.poAddress.insertRow();
                                oTransAddress.poAddress.moveToCurrentRow();
                            }
                            
                            // Extract index and value from each object in the "member" array
                            for (Object item : addressArray) { 
                                oTransAddress.poAddress.beforeFirst();
                                while (oTransAddress.poAddress.next()){
                                    if(ctr == row){
                                        JSONObject priority = (JSONObject) item;
                                        for (Object key : priority.keySet()) {
                                            Object value = priority.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(oTransAddress.poAddress.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    oTransAddress.poAddress.updateObject(Integer.valueOf(key.toString()), value );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransAddress.poAddress.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransAddress.poAddress.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransAddress.poAddress.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    oTransAddress.poAddress.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        oTransAddress.poAddress.updateRow();
                                    }
                                    row++;
                                }
                                row = 1;
                                ctr++;
                            }
                        }
                    }
                    
                    /*MOBILE*/
                    ctr = 1;
                    row = 1;
                    // Extract the "mobile" array from the JSON object
                    JSONArray mobileArray = (JSONArray) jsonObject.get("mobile");
                    if(mobileArray != null) {
                        if(mobileArray.size()>0){
                            while (mobileArray.size() > oTransMobile.getItemCount()) {
                                String lsSQL;
                                ResultSet loRS;
                                RowSetFactory factory;
                                if (oTransMobile.poMobile == null) {
                                    lsSQL = MiscUtil.addCondition(oTransMobile.getSQ_Mobile(), "0=1");
                                    loRS = poGRider.executeQuery(lsSQL);
                                    factory = RowSetProvider.newFactory();
                                    oTransMobile.poMobile = factory.createCachedRowSet();
                                    oTransMobile.poMobile.populate(loRS);
                                    MiscUtil.close(loRS);
                                }
                                oTransMobile.poMobile.last();
                                oTransMobile.poMobile.moveToInsertRow();
                                MiscUtil.initRowSet(oTransMobile.poMobile);
                                oTransMobile.poMobile.insertRow();
                                oTransMobile.poMobile.moveToCurrentRow();
                            }
                            // Extract index and value from each object in the "mobile" array
                            for (Object item : mobileArray) {
                                oTransMobile.poMobile.beforeFirst(); 
                                while (oTransMobile.poMobile.next()){  
                                    if(ctr == row){
                                        JSONObject parts = (JSONObject) item;
                                        for (Object key : parts.keySet()) {
                                            Object value = parts.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(oTransMobile.poMobile.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    oTransMobile.poMobile.updateObject(Integer.valueOf(key.toString()), tempValue );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransMobile.poMobile.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransMobile.poMobile.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransMobile.poMobile.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    oTransMobile.poMobile.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        oTransMobile.poMobile.updateRow();
                                    }
                                    row++;
                                }
                                row = 1;
                                ctr++;
                            }
                        }
                    }
                    /*EMAIL*/
                    ctr = 1;
                    row = 1;
                    // Extract the "email" array from the JSON object
                    JSONArray emailArray = (JSONArray) jsonObject.get("email");
                    if(emailArray != null) {
                        if(emailArray.size()>0){
                            while (emailArray.size() > oTransEmail.getItemCount()) {
                                String lsSQL;
                                ResultSet loRS;
                                RowSetFactory factory;
                                if (oTransEmail.poEmail == null) {
                                    lsSQL = MiscUtil.addCondition(oTransEmail.getSQ_Email(), "0=1");
                                    loRS = poGRider.executeQuery(lsSQL);
                                    factory = RowSetProvider.newFactory();
                                    oTransEmail.poEmail = factory.createCachedRowSet();
                                    oTransEmail.poEmail.populate(loRS);
                                    MiscUtil.close(loRS);
                                }
                                oTransEmail.poEmail.last();
                                oTransEmail.poEmail.moveToInsertRow();
                                MiscUtil.initRowSet(oTransEmail.poEmail);
                                oTransEmail.poEmail.insertRow();
                                oTransEmail.poEmail.moveToCurrentRow();
                            }
                            // Extract index and value from each object in the "mobile" array
                            for (Object item : emailArray) {
                                oTransEmail.poEmail.beforeFirst(); 
                                while (oTransEmail.poEmail.next()){  
                                    if(ctr == row){
                                        JSONObject parts = (JSONObject) item;
                                        for (Object key : parts.keySet()) {
                                            Object value = parts.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(oTransEmail.poEmail.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    oTransEmail.poEmail.updateObject(Integer.valueOf(key.toString()), tempValue );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransEmail.poEmail.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransEmail.poEmail.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransEmail.poEmail.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    oTransEmail.poEmail.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        oTransEmail.poEmail.updateRow();
                                    }
                                    row++;
                                }
                                row = 1;
                                ctr++;
                            }
                        }
                    }
                    
                    /*SOCIAL MEDIA*/
                    ctr = 1;
                    row = 1;
                    // Extract the "socmed" array from the JSON object
                    JSONArray socmedArray = (JSONArray) jsonObject.get("socmed");
                    if(socmedArray != null) {
                        if(socmedArray.size()>0){
                            while (socmedArray.size() > oTransSocMed.getItemCount()) {
                                String lsSQL;
                                ResultSet loRS;
                                RowSetFactory factory;
                                if (oTransSocMed.poSocMed == null) {
                                    lsSQL = MiscUtil.addCondition(oTransSocMed.getSQ_SocMed(), "0=1");
                                    loRS = poGRider.executeQuery(lsSQL);
                                    factory = RowSetProvider.newFactory();
                                    oTransSocMed.poSocMed = factory.createCachedRowSet();
                                    oTransSocMed.poSocMed.populate(loRS);
                                    MiscUtil.close(loRS);
                                }
                                oTransSocMed.poSocMed.last();
                                oTransSocMed.poSocMed.moveToInsertRow();
                                MiscUtil.initRowSet(oTransSocMed.poSocMed);
                                oTransSocMed.poSocMed.insertRow();
                                oTransSocMed.poSocMed.moveToCurrentRow();
                            }
                            // Extract index and value from each object in the "socmed" array
                            for (Object item : socmedArray) {
                                oTransSocMed.poSocMed.beforeFirst(); 
                                while (oTransSocMed.poSocMed.next()){  
                                    if(ctr == row){
                                        JSONObject parts = (JSONObject) item;
                                        for (Object key : parts.keySet()) {
                                            Object value = parts.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(oTransSocMed.poSocMed.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    oTransSocMed.poSocMed.updateObject(Integer.valueOf(key.toString()), tempValue );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransSocMed.poSocMed.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransSocMed.poSocMed.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransSocMed.poSocMed.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    oTransSocMed.poSocMed.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        oTransSocMed.poSocMed.updateRow();
                                    }
                                    row++;
                                }
                                row = 1;
                                ctr++;
                            }
                        }
                    }
                    /*VEHICLE INFORMATION*/
                    ctr = 1;
                    row = 1;
                    // Extract the "vhclinfo" array from the JSON object
                    JSONArray vhclinfoArray = (JSONArray) jsonObject.get("vhclinfo");
                    if(vhclinfoArray != null) {
                        if(vhclinfoArray.size()>0){
                            while (vhclinfoArray.size() > oTransVhclInfo.getItemCount()) {
                                String lsSQL;
                                ResultSet loRS;
                                RowSetFactory factory;
                                if (oTransVhclInfo.poVehicle == null) {
                                    lsSQL = MiscUtil.addCondition(oTransVhclInfo.getSQ_Master(), "0=1");
                                    loRS = poGRider.executeQuery(lsSQL);
                                    factory = RowSetProvider.newFactory();
                                    oTransVhclInfo.poVehicle = factory.createCachedRowSet();
                                    oTransVhclInfo.poVehicle.populate(loRS);
                                    MiscUtil.close(loRS);
                                }
                                oTransVhclInfo.poVehicle.last();
                                oTransVhclInfo.poVehicle.moveToInsertRow();
                                MiscUtil.initRowSet(oTransVhclInfo.poVehicle);
                                oTransVhclInfo.poVehicle.insertRow();
                                oTransVhclInfo.poVehicle.moveToCurrentRow();
                            }
                            // Extract index and value from each object in the "vhclinfo" array
                            for (Object item : vhclinfoArray) {
                                oTransVhclInfo.poVehicle.beforeFirst(); 
                                while (oTransVhclInfo.poVehicle.next()){  
                                    if(ctr == row){
                                        JSONObject parts = (JSONObject) item;
                                        for (Object key : parts.keySet()) {
                                            Object value = parts.get(key);
                                            if(value == null){
                                                tempValue = "";
                                            }else{
                                                tempValue = String.valueOf(value);
                                            }
                                            switch(oTransVhclInfo.poVehicle.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
                                                case Types.CHAR:
                                                case Types.VARCHAR:
                                                    oTransVhclInfo.poVehicle.updateObject(Integer.valueOf(key.toString()), tempValue );
                                                break;
                                                case Types.DATE:
                                                case Types.TIMESTAMP:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = DEFAULT_DATE;
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransVhclInfo.poVehicle.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
                                                break;
                                                case Types.INTEGER:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransVhclInfo.poVehicle.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
                                                break;
                                                case Types.DECIMAL:
                                                case Types.DOUBLE:
                                                    if(String.valueOf(tempValue).isEmpty()){
                                                        tempValue = "0.00";
                                                    } else {
                                                        tempValue = String.valueOf(value);
                                                    }
                                                    oTransVhclInfo.poVehicle.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
                                                break;
                                                default:
                                                    oTransVhclInfo.poVehicle.updateObject(Integer.valueOf(key.toString()), tempValue);
                                                break;
                                            }
                                            tempValue = "";
                                        }
                                        oTransVhclInfo.poVehicle.updateRow();
                                    }
                                    row++;
                                }
                                row = 1;
                                ctr++;
                            }
                        }
                    }
                    
//                    row = 1;
//                    ctr = 1;
//                    
//                    // Extract the "town" array from the JSON object
//                    JSONArray townArray = (JSONArray) jsonObject.get("town");
//                    if(townArray != null) {
//                        if(townArray.size()>0){
//                            while (townArray.size() > getActTownCount()) {
//                                if (addActTown("", "")){
//                                } else {
//                                    psMessage = "Error in Adding ClientMaster Town";
//                                    return false;
//                                }
//                            }
//                            
//                            // Extract index and value from each object in the "town" array
//                            for (Object item : townArray) { 
//                                poActTown.beforeFirst();
//                                while (poActTown.next()){
//                                    if(ctr == row){
//                                        JSONObject priority = (JSONObject) item;
//                                        for (Object key : priority.keySet()) {
//                                            Object value = priority.get(key);
//                                            if(value == null){
//                                                tempValue = "";
//                                            }else{
//                                                tempValue = String.valueOf(value);
//                                            }
//                                            switch(poActTown.getMetaData().getColumnType(Integer.valueOf(key.toString()))){
//                                                case Types.CHAR:
//                                                case Types.VARCHAR:
//                                                    poActTown.updateObject(Integer.valueOf(key.toString()), value );
//                                                break;
//                                                case Types.DATE:
//                                                case Types.TIMESTAMP:
//                                                    if(String.valueOf(tempValue).isEmpty()){
//                                                        tempValue = DEFAULT_DATE;
//                                                    } else {
//                                                        tempValue = String.valueOf(value);
//                                                    }
//                                                    poActTown.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE) );
//                                                break;
//                                                case Types.INTEGER:
//                                                    if(String.valueOf(tempValue).isEmpty()){
//                                                        tempValue = "0";
//                                                    } else {
//                                                        tempValue = String.valueOf(value);
//                                                    }
//                                                    poActTown.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue) );
//                                                break;
//                                                case Types.DOUBLE:
//                                                    if(String.valueOf(tempValue).isEmpty()){
//                                                        tempValue = "0.00";
//                                                    } else {
//                                                        tempValue = String.valueOf(value);
//                                                    }
//                                                    poActTown.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue) );
//                                                break;
//                                                default:
//                                                    poActTown.updateObject(Integer.valueOf(key.toString()), tempValue);
//                                                break;
//                                            }
//                                            tempValue = "";
//                                        }
//                                        poActTown.updateRow();
//                                    }
//                                    row++;
//                                }
//                                row = 1;
//                                ctr++;
//                            }
//                        }
//                    }
                }
            } else {
                psMessage = "";
                return false;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(ClientMaster.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poMaster.first();
        
        switch (fnIndex){
            case 1://sClientID
            case 2://sLastName
            case 3://sFrstName
            case 4://sMiddName
            case 5://sMaidenNm
            case 6://sSuffixNm
            case 7://sTitlexxx
            case 8://cGenderCd
            case 9://cCvilStat
            case 10://sCitizenx
            case 12://sBirthPlc
            case 13://sTaxIDNox
            case 14://sLTOIDxxx
            case 15://sAddlInfo
            case 16://sCompnyNm
            case 17://sClientNo
            case 18://cClientTp
            case 19://cRecdStat
            case 24://sCntryNme
            case 25://sTownName
            case 27://sSpouseID
            case 28://sSpouseNm                
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 11: //dBirthDte
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
        //updateState();
//        FormStateManager.saveState("Customer", toJSONString());
    }
    
    public String getFullName(String fsLname, String fsFrstNm, String fsSuffix, String fsMiddnm) throws SQLException {
        String fsFullName = fsLname;
        if (fsFrstNm != null && !fsFrstNm.isEmpty()) {
            fsFullName += ", " + fsFrstNm;
        }
        if (fsSuffix != null && !fsSuffix.isEmpty()) {
            fsFullName += " " + fsSuffix;
        }
        if (fsMiddnm != null && !fsMiddnm.isEmpty()) {
            fsFullName += " " + fsMiddnm;
        }        
        return fsFullName;
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
    
    /**
    * Creates a new client record.
    *
    * @return True if the new record is successfully created, false otherwise.
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
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);       
            poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);
            poMaster.updateString("sTitlexxx", "0");
            poMaster.updateString("cGenderCd", "0");
            poMaster.updateString("cCvilStat", "0");
            poMaster.updateString("cClientTp", "0");
            poMaster.updateObject("dBirthDte", poGRider.getServerDate());
            
            poMaster.insertRow();
            poMaster.moveToCurrentRow();
            
            //mobile new record
            //poMobile.NewRecord();
            //address new record
            //poAddress.NewRecord();
            //email new record
            //poEmail.NewRecord();
            //social media new record
            //poSocMed.NewRecord();
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    /**
    * Searches for a client record.
    *
    * @param fsValue  The search value.
    * @param fbByCode True if searching by client code, false if searching by name.
    * @return True if a matching record is found, false otherwise.
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
                                "Client ID»Customer Name»", 
                                "sClientID»sCompnyNm", 
                                "a.sClientID»a.sCompnyNm", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenRecord((String) loJSON.get("sClientID"),true);
            else {
                psMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sClientID = " + SQLUtil.toSQL(fsValue));   
        else {
            if (!fsValue.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%")); 
                //lsSQL += " LIMIT 1";
            }
        }
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sClientID");
        MiscUtil.close(loRS);
        
        //return OpenRecord(lsSQL, true);
        return OpenRecord(lsSQL, true);
    }
    
    /**
    * Opens a client record by ID.
    *
    * @param clientId The ID of the client to open.
    * @param byCode   True if searching by client code, false if searching by other criteria.
    * @return True if the client record is successfully opened, false otherwise.
    */
    public boolean OpenRecord(String fsValue,boolean fbByCode){        
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sClientID = " + SQLUtil.toSQL(fsValue));
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
        return true;
    }
    /**
    * Sets the record to update mode.
    *
    * @return True if the record is in update mode; otherwise, false.
    */
    public boolean UpdateRecord(){
        
        pnEditMode = EditMode.UPDATE;
        return true;
    }
    
    /**
    * Saves the client record.
    *
    * @return True if the client record is successfully saved, false otherwise.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (pnEditMode == EditMode.ADDNEW){ //add
                setMaster("sClientID", MiscUtil.getNextCode(MASTER_TABLE, "sClientID", true, poGRider.getConnection(), psBranchCd));
            }
            
            if (!isEntryOK()) return false;
            
            String lsSQL = "";
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                //poMaster.updateString("sClientID", MiscUtil.getNextCode(MASTER_TABLE, "sClientID", true, poGRider.getConnection(), psBranchCd));
                if(poMaster.getString("cClientTp").equals("0")){
                    poMaster.updateString("sCompnyNm", getFullName( (String)getMaster("sLastName"), 
                                                                (String)getMaster("sFrstName"), 
                                                                (String)getMaster("sSuffixNm"),
                                                                (String)getMaster("sMiddName")));// concat for sCompnyNm                                              
                }
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sCntryNme»sTownName»sCustName»sSpouseNm");
            } else { //update
                if(poMaster.getString("cClientTp").equals("0")){
                    poMaster.updateString("sCompnyNm", getFullName( (String)getMaster("sLastName"), 
                                                                (String)getMaster("sFrstName"), 
                                                                (String)getMaster("sSuffixNm"),
                                                                (String)getMaster("sMiddName")));// concat for sCompnyNm
                }
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sCntryNme»sTownName»sCustName»sSpouseNm", 
                                            "sClientID = " + SQLUtil.toSQL((String) getMaster("sClientID")));
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
            //not working yet need to fix -jahn 03212023
            //save mobile
//            poMobile.setClientID((String) getMaster("sClientID"));
//            if (!poMobile.SaveRecord()) {
//                psMessage = poGRider.getErrMsg();
//                return false;
//            }
////            //save email
//            poEmail.setClientID((String) getMaster("sClientID"));
//            if (!poEmail.SaveRecord()){
//                psMessage = poGRider.getErrMsg();
//                return false;
//            }
            //save address
//            poAddress.setClientID((String) getMaster("sClientID"));            
//            if (!poAddress.SaveRecord()){
//                psMessage = poGRider.getErrMsg();
//                return false;
//            }
//            //save social media
//            poSocMed.setClientID((String) getMaster("sClientID"));
//            if (!poSocMed.SaveRecord()){
//                psMessage = poGRider.getErrMsg();
//                return false;
//            }
            
            if (!pbWithParent) poGRider.commitTrans();
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean ActivateRecord(){
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean DeactivateRecord(){
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    private String getSQ_Citizen(){
        return "SELECT" +
                    "  sCntryCde" +
                    ", sCntryNme" +
                " FROM Country";
    }
    
    private String getSQ_Birthplace(){
//        return "SELECT" +
//                    "  sTownIDxx" +
//                    ", sTownName" +
//                " FROM towncity";
         return "SELECT " +
                    "  IFNULL(a.sTownIDxx, '') sTownIDxx " +
                    ", IFNULL(a.sTownName, '') sTownName " +
                    ", IFNULL(a.sZippCode, '') sZippCode " +                      
                    ", IFNULL(b.sProvName, '') sProvName " + 
                " FROM TownCity a"  +
                " LEFT JOIN Province b on b.sProvIDxx = a.sProvIDxx";
    }
    
    private String getSQ_Spouse(){
        return "SELECT" +
                    " sClientID" +
                    ", sLastName" +
                    ", sFrstName" +
                    ", sCompnyNm" +
                    ", TRIM(CONCAT(sLastName, ', ', sFrstName)) sSpouseNm"+
                " FROM client_master";
    }
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sClientID" + //1
                    ", a.sLastName" + //2
                    ", a.sFrstName" + //3
                    ", a.sMiddName" + //4
                    ", a.sMaidenNm" + //5
                    ", a.sSuffixNm" + //6
                    ", a.sTitlexxx" + //7
                    ", a.cGenderCd" + //8
                    ", a.cCvilStat" + //9
                    ", a.sCitizenx" + //10
                    ", a.dBirthDte" + //11
                    ", a.sBirthPlc" + //12
                    ", a.sTaxIDNox" + //13
                    ", a.sLTOIDxxx" + //14
                    ", a.sAddlInfo" + //15
                    ", a.sCompnyNm" + //16
                    ", a.sClientNo" + //17
                    ", a.cClientTp" + //18
                    ", a.cRecdStat" + //19
                    ", a.sEntryByx" + //20
                    ", a.dEntryDte" + //21
                    ", a.sModified" + //22
                    ", a.dModified" + //23
                    ", IFNULL(b.sCntryNme, '') sCntryNme" +  //24
                    ", TRIM(CONCAT(c.sTownName, ', ', d.sProvName, ' ', c.sZippCode)) sTownName" +  //25
                    ", TRIM(CONCAT(a.sLastName, ', ', a.sFrstName, ' ', a.sSuffixNm, ' ', a.sMiddName)) sCustName" +  //26
                    ", a.sSpouseID" +  //27
                    ", TRIM(CONCAT(e.sLastName, ', ', e.sFrstName)) sSpouseNm"+  //28
                " FROM " + MASTER_TABLE + " a" +
                    " LEFT JOIN Country b ON a.sCitizenx = b.sCntryCde" +
                    " LEFT JOIN TownCity c ON a.sBirthPlc = c.sTownIDxx" +
                    " LEFT JOIN Province d ON c.sProvIDxx = d.sProvIDxx" +
                    " LEFT JOIN Client_Master e ON e.sClientID = a.sSpouseID";
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
    
    /**
    * Validates client information.
    *
    * @return True if the client information is valid, false otherwise.
    * @throws SQLException If a SQL error occurs.
    */
    private boolean isEntryOK() throws SQLException{
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//        // Format the dBirthDte value into the desired format
//        String formattedDate = sdf.format(poMaster.getDate("dBirthDte"));
        poMaster.first();
        
        //validate first name and last name if client type is customer
//        0 Client
//        1 Company
//        2 Institutional *EXCLUDED* 
        if(poMaster.getString("cClientTp").equals("0")){
            if (poMaster.getString("sLastName").isEmpty()){
                psMessage = "Customer last name is not set.";
                return false;
            }

            if (poMaster.getString("sFrstName").isEmpty()){
                psMessage = "Customer first name is not set.";
                return false;
            }
            
            if (poMaster.getString("cGenderCd").isEmpty()){
                psMessage = "Gender is not set.";
                return false;
            }
            
            if (poMaster.getString("cCvilStat").isEmpty()){
                psMessage = "Civil Status is not set.";
                return false;
            }
            
            String lsSQL = getSQ_Master();
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sFrstName = " + SQLUtil.toSQL(poMaster.getString("sFrstName")) +
                                                    " AND a.sLastName = " + SQLUtil.toSQL(poMaster.getString("sLastName")) + 
                                                    " AND a.sBirthPlc = " + SQLUtil.toSQL(poMaster.getString("sBirthPlc")) + 
                                                    " AND a.sClientID <> " + SQLUtil.toSQL(poMaster.getString("sClientID"))); 
                                                    //" AND a.dBirthDte = " + SQLUtil.toSQL(formattedDate));

            ResultSet loRS = poGRider.executeQuery(lsSQL);

            if (MiscUtil.RecordCount(loRS) > 0){
                psMessage = "Existing Customer Record.";
                MiscUtil.close(loRS);        
                return false;
            }
        } else {
            if (poMaster.getString("sCompnyNm").isEmpty()){
                psMessage = "Company Name cannot be Empty.";
                return false;
            }
        }                     
        return true;
    }
    
    /**
    * Searches for a citizenship based on the provided value.
    *
    * @param fsValue   The value to search for, which can be a code or a name.
    * @param fbByCode  If true, search by citizenship code; if false, search by citizenship name.
    * @return True if a matching citizenship is found, false if no record is found.
    * @throws SQLException If a SQL error occurs.
    */
    public boolean searchCitizenship(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Citizen();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sCntryCde = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sCntryNme LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sCitizenx", loRS.getString("sCntryCde"));
                setMaster("sCntryNme", loRS.getString("sCntryNme"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            //jahn 04/03/2023
            //changed search function allow searching by user, was previously disabled 
            //and can only search based on what you type on the text field that triggers this function
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Country", "sCntryCde»sCntryNme");
            JSONObject loJSON = showFXDialog.jsonSearch(
                                                poGRider, 
                                                lsSQL, 
                                                fsValue,
                                                "Code»Country",
                                                "sCntryCde»sCntryNme",
                                                "sCntryCde»sCntryNme",
                                                fbByCode ? 0 : 1);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sCitizenx", (String) loJSON.get("sCntryCde"));
                setMaster("sCntryNme", (String) loJSON.get("sCntryNme"));
            }
        }
        
        return true;
    }
    
    /**
    * Searches for a birthplace based on the provided value.
    *
    * @param fsValue   The value to search for, which can be a code or a name.
    * @param fbByCode  If true, search by code; if false, search by name.
    * @return True if a matching birthplace is found, false if no record is found.
    * @throws SQLException If a SQL error occurs.
    */
    public boolean searchBirthplace(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Birthplace();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownIDxx = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sBirthPlc", loRS.getString("sTownIDxx"));
                //setMaster("sTownName", loRS.getString("sTownName")); 
                setMaster("sTownName",loRS.getString("sTownName")+ " " + loRS.getString("sProvName"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            //jahn 04/03/2023
            //changed search function allow searching by user, was previously disabled 
            //and can only search based on what you type on the text field that triggers this function
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Town", "sTownIDxx»sTownName");
            
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
                setMaster("sBirthPlc", (String) loJSON.get("sTownIDxx"));
                setMaster("sTownName", (String) loJSON.get("sTownName"));
                setMaster("sTownName",(String) loJSON.get("sTownName")+ " " + (String) loJSON.get("sProvName"));
            }
        }
        
        return true;
    }
    
    /**
    * Searches for a spouse based on the provided value.
    *
    * @param fsValue   The value to search for, which can be a client ID or a company name.
    * @param fbByCode  If true, search by client ID; if false, search by company name.
    * @return True if a matching spouse is found, false if no record is found.
    * @throws SQLException If a SQL error occurs.
    */
    public boolean searchSpouse(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Spouse();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sClientID = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sSpouseID", loRS.getString("sClientID"));
                setMaster("sSpouseNm", loRS.getString("sSpouseNm"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            //jahn 04/03/2023
            //changed search function allow searching by user, was previously disabled 
            //and can only search based on what you type on the text field that triggers this function
            //JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Customer Name", "sClientID»sSpouseNm");
            JSONObject loJSON = showFXDialog.jsonSearch(poGRider, 
                                                        lsSQL, 
                                                        fsValue, 
                                                        "Code»Customer Name", 
                                                        "sClientID»sCompnyNm",
                                                        "sClientID»sCompnyNm",
                                                        fbByCode ? 0 : 1);
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sSpouseID", (String) loJSON.get("sClientID"));
                setMaster("sSpouseNm", (String) loJSON.get("sSpouseNm"));
            }
        }
        
        return true;
    }
}
