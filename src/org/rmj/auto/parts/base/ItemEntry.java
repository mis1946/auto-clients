/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template
file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.parts.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;

/**
 *
 * @author User
 */
public class ItemEntry {
    private final String MASTER_TABLE = "Customer_Inquiry";
    private final String DEFAULT_DATE = "1900-01-01";
    
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
    private CachedRowSet poModel;
    private CachedRowSet poSuperSede;
            
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
            case 29: //sModified 
            case 30: //dModified 
            case 31: //dTimeStmp 	
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
    public Object getInqDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poDetail.absolute(fnRow);
        
        //return "" instead of null since it cannot handle null values
        return poDetail.getObject(fnIndex) != null ? poDetail.getObject(fnIndex) : "";
    }
    
    //Item Entry SEARCH GETTER
    public Object getInqDetail(int fnRow, String fsIndex) throws SQLException{
        return getInqDetail(fnRow, MiscUtil.getColumnIndex(poDetail, fsIndex));
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
    
    //-----------------------------------------New Record---------------------------
    //TODO add new record details
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
            
            //-------------------------------ITEM ENTRY Master------------------
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
    public boolean SearchRecord(String fsValue) throws SQLException{
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
                                "Part No.»Description", 
                                "sBarCodex»sDescript", 
                                "sBarCodex»sDescript", 
                                0);
            
            if (loJSON != null) 
                return OpenRecord((String) loJSON.get("sBarCodex"));
            else {
                psMessage = "No record selected.";
                return false;
            }
        }
                
        if (!fsValue.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sBarCodex LIKE " + SQLUtil.toSQL("%" + fsValue + "%")); 
            //lsSQL += " LIMIT 1";
        }
        
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sStockIDx");
        MiscUtil.close(loRS);
        
        //return OpenRecord(lsSQL, true);
        return OpenRecord(lsSQL);
    }
    
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
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "sStockIDx = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);

            //open VHCL priority
            lsSQL = MiscUtil.addCondition(getInv_model(), "sStockIDx = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poModel = factory.createCachedRowSet();
            poModel.populate(loRS);
            MiscUtil.close(loRS);
           
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }
    
    private String getSQ_Master() {
        return " SELECT "
                    + " sStockIDx " //1
                    + " ,sBarCodex "//2
                    + " ,sDescript "//3
                    + " ,sBriefDsc "//4                    
                    + " ,sCategCd1 "//5
                    + " ,sCategCd2 "//6
                    + " ,sCategCd3 "//7
                    + " ,sCategCd4 "//8
                    + " ,sBrandCde "//9
                    + " ,sModelCde "//10
                    + " ,sMeasurID "//11
                    + " ,sInvTypCd "//12
                    + " ,nUnitPrce "//13
                    + " ,nSelPrice "//14
                    + " ,nDiscLev1 "//15
                    + " ,nDiscLev2 "//16
                    + " ,nDiscLev3 "//17
                    + " ,nDealrDsc "//18
                    + " ,cComboInv "//19
                    + " ,cWthPromo "//20
                    + " ,cUnitType "//21
                    + " ,cInvStatx "//22
                    + " ,cGenuinex "//23
                    + " ,cReplacex "//24
                    + " ,sSupersed "//25
                    + " ,sFileName "//26
                    + " ,sTrimBCde "//27
                    + " ,cRecdStat "//28
                    + " ,sModified "//29
                    + " ,dModified "//30
                    + " ,dTimeStmp "//31
                    + " ,IFNULL(b.sLocatnDs , '' ) sLocatnDs"
                + " FROM INVENTORY ";		
    }
    
    private String getSQ_ItemLocation() {
        return " SELECT "																            
                    + " a.sLocatnID, " //1														    
                    + " a.sLocatnDs, " //2																
                    + " a.sWHouseID, " //3																
                    + " a.sSectnIDx, " //4																
                    + " a.sBinIDxxx, " //5																
                    + " a.cRecdStat, " //6																
                    + " b.sWHouseNm, " //7																
                    + " c.sSectnNme, " //8																
                    + " d.sBinNamex  " //9													      
                + " FROM item_location a "														
                + " LEFT JOIN warehouse b on b.sWHouseID = a.sWHouseID "
                + " LEFT JOIN section c	on c.sSectnIDx = a.sSectnIDx "
                + " LEFT JOIN bin d on d.sBinIDxxx = a.sBinIDxxx ";		
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
                                                        lsSQL, 
                                                        fsValue, 
                                                        "Measure ID»Measurement Name", 
                                                        "sMeasurID»sMeasurNm",
                                                        "sMeasurID»sMeasurNm",                                                        
                                                        0);            
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
    
    private String getInv_Master(){
         return " SELECT " 
                    + " a.sStockIDx " //1
                    + ", a.sBranchCd "//2
                    + ", a.sLocatnID "//3
                    + ", a.dAcquired "//4
                    + ", a.dBegInvxx "//5
                    + ", a.nBegQtyxx "//6
                    + ", a.nQtyOnHnd "//7
                    + ", a.nMinLevel "//8
                    + ", a.nMaxLevel "//9
                    + ", a.nAvgMonSl "//10
                    + ", a.nAvgCostx "//11
                    + ", a.cClassify "//12
                    + ", a.nBackOrdr "//13
                    + ", a.nResvOrdr "//14
                    + ", a.nFloatQty "//15
                    + ", a.nLedgerNo "//16
                    + ", a.dLastTran "//17
                    + ", a.cRecdStat "//18
                    + ", a.sModified "//19
                    + ", a.dModified "//20
                    + ", a.dTimeStmp "//21
                    + ", IFNULL(b.sLocatnDs, '') sLocatnDs" //22
                + " FROM inv_master a "
                + " LEFT JOIN item_location b on b.sLocatnID = a.sLocatnID ";
    }
    
    public boolean searchLocation(String fsValue) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getSQ_ItemLocation(), " sLocatnDs LIKE " + SQLUtil.toSQL(fsValue + "%"));            
      
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sLocatnDs", loRS.getString("sLocatnDs"));
                setMaster("sLocatnID", loRS.getString("sLocatnID"));               
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
                                                        "Location ID»Location", 
                                                        "sLocatnID»sLocatnDs",
                                                        "sLocatnID»sLocatnDs",                                                        
                                                        0);            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sLocatnDs", (String) loJSON.get("sLocatnDs"));
                setMaster("sLocatnID", (String) loJSON.get("sLocatnID"));                
            }
        }        
        return true;
    }
    
    
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
    
    public boolean searchInvType(String fsValue) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getInv_Type(), " sDescript LIKE " + SQLUtil.toSQL(fsValue + "%"));            
      
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sDescript", loRS.getString("sDescript"));
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
                setMaster("sDescript", (String) loJSON.get("sDescript"));
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
                + "  FROM inventory_category "	;
    }
    
    public boolean searchInvCategory(String fsValue, String fsType) throws SQLException{                        
        String lsSQL = MiscUtil.addCondition(getInv_Category(), " sDescript LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                                " AND sInvTypCd = " + SQLUtil.toSQL(fsType + "%"));            
      
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            if (loRS.next()){
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sCategrCd", loRS.getString("sCategrCd"));               
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
                                                        "Category ID»sDescript",
                                                        "Category ID»sDescript",                                                        
                                                        0);            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sCategrCd", (String) loJSON.get("sCategrCd"));                
            }
        }        
        return true;
    }
    
    private String getInv_model(){    
        return " SELECT "
                    + " sStockIDx " //1	
                    + " ,nEntryNox "//2	
                    + " ,sModelCde "//3	
                    + " ,dTimeStmp "//4
                + " FROM inventory_model ";	
    }

}
