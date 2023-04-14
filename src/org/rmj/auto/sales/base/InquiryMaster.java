/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.sales.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
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
import org.rmj.appdriver.StringUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;

/**
 *
 * @author Jahn April 4 2023
 */
public class InquiryMaster {
    private final String MASTER_TABLE = "Customer_Inquiry";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
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
//-----------------------------------------Customer Inquiry--------------------------------------------------
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
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 5 ://cIsVhclNw
            case 11: //cIntrstLv 
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
        
        poMaster.absolute(fnRow);
        return poMaster.getObject(fnIndex);
    }
    
    //INQUIRY SEARCH GETTER
    public Object getInqDetail(int fnRow, String fsIndex) throws SQLException{
        return getInqDetail(fnRow, MiscUtil.getColumnIndex(poMaster, fsIndex));
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
//-----------------------------------------Vehicle Priority--------------------------------------------------
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
    
    //Call to move priority in target vehicle priority
    // TODO fix getters and setters when structure is done
    public boolean setVehiclePriority(int fnRow, boolean fbMoveUpxx) throws SQLException, ParseException{
        String lsVhcl = (String) getMaster("sVhclIDxx");
        
        JSONArray loArray;
        
        if (lsVhcl.isEmpty()) 
            return false;
        else {
            JSONParser loParser = new JSONParser();
            loArray = (JSONArray) loParser.parse(lsVhcl);
            
            if (fnRow > loArray.size()-1 || fnRow < 0) return false;
            
            if (fbMoveUpxx && fnRow == 0) return false;
            if (!fbMoveUpxx && fnRow == loArray.size()-1) return false;
            
            JSONObject loTemp = (JSONObject) loArray.get(fnRow);
            loArray.remove(fnRow);
            
            if (fbMoveUpxx)
                loArray.add(fnRow - 1, loTemp);
            else
                loArray.add(fnRow + 1, loTemp);
        }
            
        setMaster("sVhclIDxx", loArray.toJSONString());
        
        return true;
    }
    
    //TODO for Priority/Target Vehicle tableview
    public boolean removeTargetVehicle(int fnRow) throws SQLException{
        if (getVhclPrtyCount() == 0) {
            psMessage = "No address to delete.";
            return false;
        }
        
        poVhclPrty.absolute(fnRow);
        poVhclPrty.deleteRow();
        return true;
    }
//-----------------------------------------New Record--------------------------------------------------
    //TODO add new record details
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            //----------------------------------Inquiry Master------------------
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);       
            poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);
            poMaster.updateString("cIsVhclNw", "0");  
            poMaster.updateString("cIntrstLv", "0");  
            poMaster.updateString("cTranStat", "0"); 
            poMaster.updateObject("dTransact", poGRider.getServerDate());                       

            poMaster.insertRow();
            poMaster.moveToCurrentRow();
            
            //----------------------------------Inquiry Vehicle Priority--------            
            poVhclPrty = factory.createCachedRowSet();
            poVhclPrty.populate(loRS);
            MiscUtil.close(loRS);
            
            poVhclPrty.last();
            poVhclPrty.moveToInsertRow();
            
            MiscUtil.initRowSet(poVhclPrty);       
            //poVhclPrty.updateString("cRecdStat", RecordStatus.ACTIVE);
            //poVhclPrty.updateString("cOfficexx", "0");           

            poVhclPrty.insertRow();
            poVhclPrty.moveToCurrentRow();   
            
            //----------------------------------Inquiry Vehicle Promo-----------            
            poInqPromo = factory.createCachedRowSet();
            poInqPromo.populate(loRS);
            MiscUtil.close(loRS);
            
            poInqPromo.last();
            poInqPromo.moveToInsertRow();
            
            MiscUtil.initRowSet(poInqPromo);       
            //poInqPromo.updateString("cRecdStat", RecordStatus.ACTIVE);
            //poInqPromo.updateString("cOfficexx", "0");           

            poInqPromo.insertRow();
            poInqPromo.moveToCurrentRow();
                       
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
//-----------------------------------------Search Record--------------------------------------------------    
    //TODO Search Record for inquiry
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
    
//-----------------------------------------Open Record--------------------------------------------------    
    //TODO openrecord when inquiry is double clicked
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
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);

            //open detail
            lsSQL = MiscUtil.addCondition(getSQ_VhclPrty(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poVhclPrty = factory.createCachedRowSet();
            poVhclPrty.populate(loRS);
            MiscUtil.close(loRS);

            //open incentive
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
        return true;
    }
    
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
  
        poVhclPrty.updateString("cRecdStat", RecordStatus.ACTIVE);
        poVhclPrty.updateString("cOfficexx", "0");
        poVhclPrty.updateString("cProvince", "0");
        poVhclPrty.updateString("cPrimaryx", "0");
        poVhclPrty.updateString("cCurrentx", "0");
        poVhclPrty.insertRow();
        poVhclPrty.moveToCurrentRow();
                
        return true;
    }
//-----------------------------------------Update Record--------------------------------------------------    
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
//        try {
//        // Save the current state of the table as the original state
//            if (poAddress != null){
//                poOriginalAddress = (CachedRowSet) poAddress.createCopy();
//            }
//        } catch (SQLException e) {
//            // Handle exception
//        }
        return true;       
    }
//-----------------------------------------Save Record--------------------------------------------------    
    //TODO Saverecord for saving
    public boolean SaveRecord(){
        return true;
    }
    
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
                    ", IFNULL(b.sCompnyNm, '') sCompnyNm" +//29
                    ", IFNULL(c.sMobileNo, '') sMobileNo" +//30
                   // ", TRIM(CONCAT(d.sTownName, ', ', d.sProvName)) sTownName" +  
                    ", IFNULL(h.sAccountx, '') sAccountx" +//31
                    ", IFNULL(i.sEmailAdd, '') sEmailAdd" +//32
                " FROM  " + MASTER_TABLE + " a" +
                    " LEFT JOIN Client_master b ON a.sClientID = b.sClientID" +
                    " LEFT JOIN client_mobile c ON c.sClientID = b.sClientID" +
                    " LEFT JOIN client_address d ON d.sClientID = b.sClientID" + 
                    " LEFT JOIN TownCity e ON e.sTownIDxx = d.sTownIDxx" +
                    " LEFT JOIN Barangay f ON f.sBrgyIDxx = d.sBrgyIDxx" + 
                    " LEFT JOIN Province g on g.sProvIDxx = e.sProvIDxx" +
                    " LEFT JOIN client_social_media h ON h.sClientID = b.sClientID" +
                    " LEFT JOIN client_email_address i ON i.sClientID = b.sClientID";                    
    }
    
    //TODO query for retrieving customer info
    private String getSQ_Customerinfo(){
        return "SELECT " +
                    "sClientID " +
                    ", sLastName" +
                    ", sFrstName" +
                    ", sMiddName" +
                    ", sCompnyNm" +
                    //"IFNULL(b." customer address
                " FROM client_master a" +
                    " LEFT JOIN client_address b ON b.sClientID = a.sClientID" ;
    }
    
    //TODO query for promo
    private String getSQ_InqPromo(){
        return "SELECT " +
                    "sClientID " +
                    ", sLastName" +
                    ", sFrstName" +
                    ", sMiddName" +
                    ", sCompnyNm" +
                    //"IFNULL(b." customer address
                " FROM client_master a" +
                    " LEFT JOIN client_address b ON b.sClientID = a.sClientID" ;
    }
        
    //TODO query for sales executives
    private String getSQ_SalesExecutives(){
        return "";
    }
    
    //TODO query for referal agents
    private String getSQ_Agents(){
        return "";
    }
        
    //TODO query for activity events
    private String getSQ_ActivityEvent(){
        return "";
    }
    
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
    
    //search barangay (used when "barangay" is double clicked or searched)
    public boolean searchVhclPrty(int fnRow, String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Vehicle();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sVhclIDxx = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sDescript LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
//        if (!pbWithUI) {   
//            lsSQL += " LIMIT 1";
//            loRS = poGRider.executeQuery(lsSQL);
//            
//            if (loRS.next()){
//                setVhclPrty(fnRow, "sVhclIDxx", loRS.getString("sVhclIDxx"));
//                setVhclPrty(fnRow, fnRow, loRS.getString("nPriority"));
//            } else {
//                psMessage = "No record found.";
//                return false;
//            }
//        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Vehicle Description", "sVhclIDxx»sDescript");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setVhclPrty(fnRow, "sVhclIDxx", (String) loJSON.get("sVhclIDxx"));
                setVhclPrty(fnRow, fnRow, (String) loJSON.get("nPriority"));
            }
        //}
        
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
    
    //TODO validation for entries
    public boolean isEntryOK(){
        return true;
    }    
}

