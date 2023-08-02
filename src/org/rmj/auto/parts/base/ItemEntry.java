/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template
file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.parts.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
import org.rmj.appdriver.constants.RecordStatus;

/**
 *
 * @author Jahn 
 * Continued by Arsiela - 08-01-2023
 */
public class ItemEntry {
    private final String MASTER_TABLE = "inventory";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private final String SALES = "A011";
    private final String SALES_ADMIN = "";
    private final String MIS = "";
    private final String MAIN_OFFICE = "M001»M0W1";
    private Integer pnDeletedVhclModelRow[];
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
    private CachedRowSet poDetail;
    private CachedRowSet poVhclModel;
    private CachedRowSet poInvModel;
    private CachedRowSet poSuperSede;
    private CachedRowSet poInvSuperSede;
    
    List<Integer> deletedRows = new ArrayList<>();
            
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
//        poMaster.last();
//        return poMaster.getRow();
        if (poMaster != null){
            poMaster.last();
            return poMaster.getRow();
        }else{
            return 0;
        }              
    }
    
    public int getMasterDetailCount() throws SQLException{
//        poDetail.last();
//        return poDetail.getRow();
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
    
    //-----------------------------------------New Record---------------------------
    //TODO add new record details
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {       
            //String lsSQL = getSQ_Master() + " WHERE 0=1";
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
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    //for autoloading list of vehicle make
    public boolean LoadMasterList() throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        //String lsSQL = getSQ_Master() + " WHERE f.sBranchCd = "  + SQLUtil.toSQL(psBranchCd) ;
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
    
//    //-----------------------------------------Search Record------------------------   
//    public boolean SearchRecord(String fsValue) throws SQLException{
//        if (poGRider == null){
//            psMessage = "Application driver is not set.";
//            return false;
//        }
//        
//        psMessage = "";    
//        
//        String lsSQL = getSQ_Master();
//        
//        if (pbWithUI){
//            JSONObject loJSON = showFXDialog.jsonSearch(
//                                poGRider, 
//                                lsSQL + " WHERE f.sBranchCd = "  + SQLUtil.toSQL(psBranchCd) , 
//                                fsValue, 
//                                "Part No.»Description", 
//                                "sBarCodex»sDescript", 
//                                "sBarCodex»sDescript", 
//                                0);
//            
//            if (loJSON != null) 
//                return OpenRecord((String) loJSON.get("sBarCodex"));
//            else {
//                psMessage = "No record selected.";
//                return false;
//            }
//        }
//                
//        if (!fsValue.isEmpty()) {
//            lsSQL = MiscUtil.addCondition(lsSQL, "sBarCodex LIKE " + SQLUtil.toSQL("%" + fsValue + "%") +
//                                                    " f.sBranchCd = "  + SQLUtil.toSQL(psBranchCd) ); 
//            //lsSQL += " LIMIT 1";
//        }
//        
//        ResultSet loRS = poGRider.executeQuery(lsSQL);
//        
//        if (!loRS.next()){
//            MiscUtil.close(loRS);
//            psMessage = "No record found for the given criteria.";
//            return false;
//        }
//        
//        lsSQL = loRS.getString("sStockIDx");
//        MiscUtil.close(loRS);
//        
//        //return OpenRecord(lsSQL, true);
//        return OpenRecord(lsSQL);
//    }
    
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

            //open model list
            lsSQL = MiscUtil.addCondition(getInv_model(), "sStockIDx = " + SQLUtil.toSQL(fsValue));
            loRS = poGRider.executeQuery(lsSQL);
            poInvModel = factory.createCachedRowSet();
            poInvModel.populate(loRS);
            MiscUtil.close(loRS);
            
            //TODO open supersede list
           
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }
   
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            //if (!isEntryOK()) return false;
            String lsSQL = "";
            String lsTransNox = "";
            if (pnEditMode == EditMode.ADDNEW){ //add
                poMaster.updateString("sStockIDx",MiscUtil.getNextCode(MASTER_TABLE, "sStockIDx", true, poGRider.getConnection(), psBranchCd) );                                                             
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sBrandNme»sCategNme»sMeasurNm»sInvTypNm»sLocatnID»sLocatnDs");
            } else { //update  
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sBrandNme»sCategNme»sMeasurNm»sInvTypNm»sLocatnID»sLocatnDs", 
                                            "sStockIDx = " + SQLUtil.toSQL((String) getMaster("sStockIDx")));
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
    
    private Boolean isEntryOK(){
        
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
                    + " , IFNULL(a.sSupersed, '') sSupersed  "//25
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
    
    public boolean addInvModel(String fsValue) throws SQLException {
        if (poInvModel == null) {
            String lsSQL = MiscUtil.addCondition(getInv_model(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            RowSetFactory factory = RowSetProvider.newFactory();
            poInvModel = factory.createCachedRowSet();
            poInvModel.populate(loRS);
            MiscUtil.close(loRS);
        }

        poInvModel.last();
        poInvModel.moveToInsertRow();

        MiscUtil.initRowSet(poInvModel);

        poInvModel.updateString("sModelCde", fsValue);
        poInvModel.insertRow();
        poInvModel.moveToCurrentRow();

        return true;
    }

    public boolean clearActVehicle() throws SQLException {
        if (getInvModelCount() > 0) {
            poInvModel.beforeFirst();
            while (poInvModel.next()) {
                poInvModel.deleteRow();
            }
        }
        return true;
    }
    
    public boolean loadInvModel(String fsValue, boolean fbLoadbyAct) {

        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            if (fbLoadbyAct) {
                lsSQL = MiscUtil.addCondition(getInv_model(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
                loRS = poGRider.executeQuery(lsSQL);

                poInvModel = factory.createCachedRowSet();
                poInvModel.populate(loRS);
                MiscUtil.close(loRS);
            } else {
                //lsSQL = MiscUtil.addCondition(getSQ_Vehicle(), "0=1");
                lsSQL = getInv_model();
                loRS = poGRider.executeQuery(lsSQL);

                poVhclModel = factory.createCachedRowSet();
                poVhclModel.populate(loRS);
                MiscUtil.close(loRS);
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        return true;
    }

    public boolean removeVhclModel(Integer fnRow[]) {
        try {
            if (getInvModelCount() == 0) {
                psMessage = "No Vehicle Model to delete.";
                return false;
            }

            Arrays.sort(fnRow, Collections.reverseOrder());

            for (int lnCtr : fnRow) {
                poInvModel.absolute(lnCtr);
                String lsFind = poInvModel.getString("sTransNox");
                if (lsFind != null && !lsFind.isEmpty()) {
                    deletedRows.add(lnCtr);
                }
                poInvModel.deleteRow();
                System.out.println("success");
            }

            pnDeletedVhclModelRow = deletedRows.toArray(new Integer[deletedRows.size()]);
            
            deletedRows.clear();
            return true;
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
    }

    //------------------------------Vehicle Model-----------------------------
    //Vehicle Model Setter
    public void setInvModel(int fnRow, int fnIndex, Object foValue) throws SQLException {
        poInvModel.absolute(fnRow);
        switch (fnIndex) {
            case 1://sStockIDx
            case 3://sModelCde
                poInvModel.updateObject(fnIndex, (String) foValue);
                poInvModel.updateRow();

                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getInvModel(fnIndex));
                }
                break;
            case 2://nEntryNox
                if (foValue instanceof Integer) {
                    poInvModel.updateInt(fnIndex, (int) foValue);
                } else {
                    poInvModel.updateInt(fnIndex, 0);
                }

                poInvModel.updateRow();
                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getInvModel(fnIndex));
                }
                break;
        }
    }

    public void setInvModel(int fnRow, String fsIndex, Object foValue) throws SQLException {
        setInvModel(fnRow, MiscUtil.getColumnIndex(poInvModel, fsIndex), foValue);
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

    //Vehicle Model GETTER
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
    
    /********VEHICLE MODEL********/
    private String getSQ_VhclModel(){
        return " SELECT ";
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

}
