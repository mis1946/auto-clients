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
import org.rmj.auto.clients.base.CompareRows;

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
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private Integer pnDeletedVhclModelRow[];
    private Integer pnDeletedVhclModelYrRow[];
    
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
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sStockIDx = " + SQLUtil.toSQL(fsValue));
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);

            //open model list
            lsSQL = MiscUtil.addCondition(getInv_model(), "sStockIDx = " + SQLUtil.toSQL(fsValue))
                                          + " GROUP BY a.sModelCde, b.nYearModl ";
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
   
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        return true;        
    }
   
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            String lsSQL = "";
            String lsTransNox = "";
            Integer lnCtr = 0;
            if (!pbWithParent) poGRider.beginTrans();
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                /*************SAVE INVENTORY TABLE***************/
                poMaster.updateString("sStockIDx",MiscUtil.getNextCode(MASTER_TABLE, "sStockIDx", true, poGRider.getConnection(), psBranchCd) );                                                             
                poMaster.updateString("sTrimBCde", ((String) getMaster("sBarCodex")).replaceAll("\\s", ""));
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sBrandNme»sCategNme»sMeasurNm»sInvTypNm»sLocatnID»sLocatnDs");
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
                        lsSQL = MiscUtil.rowset2SQL(poInvModel, "inventory_model_year", "sMakeDesc»sModelDsc");
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
                        poInvModel.updateObject("nEntryNox", lnCtr);
                        poInvModel.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poInvModel, "inventory_model", "");
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
                                            "sBrandNme»sCategNme»sMeasurNm»sInvTypNm»sLocatnID»sLocatnDs", 
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
                if (pnDeletedVhclModelYrRow != null && pnDeletedVhclModelYrRow.length != 0) {
                    Arrays.sort(pnDeletedVhclModelYrRow, Collections.reverseOrder());
                    poInvModelYearOrig.beforeFirst();
                    for (int rowNum : pnDeletedVhclModelYrRow) {
                        poInvModelYearOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM inventory_model_year WHERE"
                                + " sStockIDx = " + SQLUtil.toSQL(poInvModelYearOrig.getString("sStockIDx"))
                                + " AND nYearModl = " + SQLUtil.toSQL(poInvModelYearOrig.getInt("nYearModl"))
                                + " AND sModelCde = " + SQLUtil.toSQL(poInvModelYearOrig.getString("sModelCde"));

                        if (poGRider.executeQuery(lsSQL, "inventory_model_year", psBranchCd, "") <= 0) {
                            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                if (getInvModelYrCount() > 0) {
                    lnCtr = 1;

                    poInvModelYear.beforeFirst();
                    while (lnCtr <= getInvModelYrCount()) {
                        if (!CompareRows.isRowEqual(poInvModelYear, poInvModelYearOrig, lnCtr)) {
                            String lsCode = (String) getInvModelYr(lnCtr, "sModelCde");// check if user added new VEHICLE MODEL YEAR to insert
                            if (lsCode.equals("") || lsCode.isEmpty()) {
                                lsSQL = MiscUtil.rowset2SQL(poInvModelYear, "inventory_model_year", "nYearModl");
                                //TODO what is substring(0,4)
                                if (poGRider.executeQuery(lsSQL, "inventory_model_year", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
                                    if (!pbWithParent) {
                                        poGRider.rollbackTrans();
                                    }
                                    psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                    return false;
                                }
                            } else {
                                lsSQL = MiscUtil.rowset2SQL(poInvModel,
                                        "inventory_model_year",
                                        "",
                                        " nYearModl = " + SQLUtil.toSQL(lsTransNox)
                                        + " AND sStockIDx = " + SQLUtil.toSQL(poInvModel.getString("sStockIDx")));

                                if (!lsSQL.isEmpty()) {
                                    if (poGRider.executeQuery(lsSQL, "inventory_model_year", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
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
                }
                
                /*************SAVE INVENTORY MODEL TABLE***************/
                if (pnDeletedVhclModelRow != null && pnDeletedVhclModelRow.length != 0) {
                    Arrays.sort(pnDeletedVhclModelRow, Collections.reverseOrder());
                    poInvModelOrig.beforeFirst();
                    for (int rowNum : pnDeletedVhclModelRow) {
                        poInvModelOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM inventory_model WHERE"
                                + " sStockIDx = " + SQLUtil.toSQL(poInvModelOrig.getString("sStockIDx"))
                                + " AND nEntryNox = " + SQLUtil.toSQL(poInvModelOrig.getInt("nEntryNox"))
                                + " AND sModelCde = " + SQLUtil.toSQL(poInvModelOrig.getString("sModelCde"));

                        if (poGRider.executeQuery(lsSQL, "inventory_model", psBranchCd, "") <= 0) {
                            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                if (getInvModelCount() > 0) {
                    lnCtr = 1;

                    poInvModel.beforeFirst();
                    while (lnCtr <= getInvModelCount()) {
                        if (!CompareRows.isRowEqual(poInvModel, poInvModelOrig, lnCtr)) {
                            String lsCode = (String) getInvModel(lnCtr, "sModelCde");// check if user added new VEHICLE MODEL to insert
                            if (lsCode.equals("") || lsCode.isEmpty()) {
                                poInvModel.updateObject("nEntryNox", lnCtr);
                                poInvModel.updateRow();

                                lsSQL = MiscUtil.rowset2SQL(poInvModel, "inventory_model", "nYearModl");
                                //TODO what is substring(0,4)
                                if (poGRider.executeQuery(lsSQL, "inventory_model", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
                                    if (!pbWithParent) {
                                        poGRider.rollbackTrans();
                                    }
                                    psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                    return false;
                                }
                            } else {
                                poInvModel.updateObject("nEntryNox", lnCtr);
                                poInvModel.updateRow();

                                lsSQL = MiscUtil.rowset2SQL(poInvModel,
                                        "inventory_model",
                                        "",
                                        " AND sStockIDx = " + SQLUtil.toSQL(poInvModel.getString("sStockIDx")));

                                if (!lsSQL.isEmpty()) {
                                    if (poGRider.executeQuery(lsSQL, "inventory_model", psBranchCd, lsTransNox.substring(0, 4)) <= 0) {
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
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sTrimBCde = " + SQLUtil.toSQL(poMaster.getString("sTrimBCde")) +
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
    
    private String getInv_model(){   
        return  " SELECT " 
                + " a.sStockIDx  " //1	
                + " ,a.nEntryNox " //2	
                + " ,a.sModelCde " //3
                + " ,a.dTimeStmp " //4
                + " FROM inventory_model a " ;
    }
    
    private String getInv_modelYear(){    
        return " SELECT "
               + " a.sStockIDx "
               + " , a.sModelCde "
               + " , a.nYearModl "
               + " , c.sMakeDesc "
               + " , b.sModelDsc "
               + " FROM inventory_model_year a "
               + " LEFT JOIN vehicle_model b ON b.sModelIDx = a.sModelCde "
               + " LEFT JOIN vehicle_make c ON c.sMakeIDxx = b.sMakeIDxx ";
    }
    
    public boolean loadInvModelYr(String fsValue, boolean fbLoadInvModelYr) {
        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            if (fbLoadInvModelYr) {
                lsSQL = MiscUtil.addCondition(getInv_modelYear(), "a.sStockIDx = " + SQLUtil.toSQL(fsValue));
                loRS = poGRider.executeQuery(lsSQL);
                poInvModelYear = factory.createCachedRowSet();
                poInvModelYear.populate(loRS);
                MiscUtil.close(loRS);
            } else {
                poVhclModelYear = factory.createCachedRowSet();
                for (int lnCtr = 1900; lnCtr <= 1; lnCtr++){
                    poVhclModelYear.moveToInsertRow();
                    poVhclModelYear.updateInt("nYearModl",lnCtr);
                    poVhclModelYear.insertRow();
                }
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        return true;
    }
    
    public boolean loadInvModel(String fsValue, boolean fbLoadbyInv) {
        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            if (fbLoadbyInv) {
                lsSQL = MiscUtil.addCondition(getInv_model(), "a.sStockIDx = " + SQLUtil.toSQL(fsValue));
                loRS = poGRider.executeQuery(lsSQL);

                poInvModel = factory.createCachedRowSet();
                poInvModel.populate(loRS);
                MiscUtil.close(loRS);
            } else {
                lsSQL = getSQ_VhclModel();
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
    
    public boolean addInvModelYr(String fsCode, Integer fnYear, boolean fbisCommon) throws SQLException{
        if (poInvModelYear == null) {
            String lsSQL = MiscUtil.addCondition(getInv_modelYear(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            System.out.println(lsSQL);
            RowSetFactory factory = RowSetProvider.newFactory();
            poInvModelYear = factory.createCachedRowSet();
            poInvModelYear.populate(loRS);
            MiscUtil.close(loRS);
        }
        
        addInvModel(fsCode);
        
        if (fbisCommon){ return true;}
        
        poInvModelYear.last();
        poInvModelYear.moveToInsertRow();
        MiscUtil.initRowSet(poInvModelYear);
        poInvModelYear.updateString("sModelCde", fsCode);
        poInvModelYear.updateInt("nYearModl", fnYear);
        poInvModelYear.insertRow();
        poInvModelYear.moveToCurrentRow();
        
        return true;
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
        
        int lnCtr;
        if (getInvModelCount() > 0) {
            lnCtr = 1;
            poInvModel.beforeFirst();
            while (poInvModel.next()) {
                if (fsValue.equals(getInvModel(lnCtr, "sModelCde"))){
                    return true;
                }
                lnCtr++;
            }
        }
        
        poInvModel.last();
        poInvModel.moveToInsertRow();
        MiscUtil.initRowSet(poInvModel);
        poInvModel.updateString("sModelCde", fsValue);
        poInvModel.insertRow();
        poInvModel.moveToCurrentRow();

        return true;
    }
    
    public boolean clearInvModelYr() throws SQLException {
        if (getInvModelYrCount() > 0) {
            poInvModelYear.beforeFirst();
            while (poInvModelYear.next()) {
                poInvModelYear.deleteRow();
            }
        }
        return true;
    }
    
    public boolean clearInvModel() throws SQLException {
        if (getInvModelYrCount() > 0) {
            poInvModel.beforeFirst();
            while (poInvModel.next()) {
                poInvModel.deleteRow();
            }
        }
        return true;
    }
    
    public boolean removeVhclModelYr(Integer fnRow[]) {
        try {
            if (getInvModelYrCount() == 0) {
                psMessage = "No Vehicle Model to delete.";
                return false;
            }

            Arrays.sort(fnRow, Collections.reverseOrder());

            for (int lnCtr : fnRow) {
                poInvModelYear.absolute(lnCtr);
                String lsFind = poInvModelYear.getString("sStockIDx");
                if (lsFind != null && !lsFind.isEmpty()) {
                    deletedRows.add(lnCtr);
                }
                poInvModelYear.deleteRow();
                System.out.println("success");
            }

            pnDeletedVhclModelYrRow = deletedRows.toArray(new Integer[deletedRows.size()]);
            
            deletedRows.clear();
            return true;
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
    }
    
    public boolean removeVhclModel(Integer fnRow[]) {
        try {
            if (getInvModelYrCount() == 0) {
                psMessage = "No Vehicle Model to delete.";
                return false;
            }

            Arrays.sort(fnRow, Collections.reverseOrder());

            for (int lnCtr : fnRow) {
                poInvModel.absolute(lnCtr);
                String lsFind = poInvModel.getString("sStockIDx");
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

    //------------------------------Vehicle Model YEAR-----------------------------
    //Vehicle Model Year Setter
    public void setInvModelYr(int fnRow, int fnIndex, Object foValue) throws SQLException {
        poInvModelYear.absolute(fnRow);
        switch (fnIndex) {
            case 1://sStockIDx
            case 2://sModelCde
                poInvModelYear.updateObject(fnIndex, (String) foValue);
                poInvModelYear.updateRow();

                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getInvModelYr(fnIndex));
                }
                break;
            case 3://nYearModl
                if (foValue instanceof Integer) {
                    poInvModelYear.updateInt(fnIndex, (int) foValue);
                } else {
                    poInvModelYear.updateInt(fnIndex, 0);
                }

                poInvModelYear.updateRow();
                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getInvModelYr(fnIndex));
                }
                break;
        }
    }

    public void setInvModelYr(int fnRow, String fsIndex, Object foValue) throws SQLException {
        setInvModelYr(fnRow, MiscUtil.getColumnIndex(poInvModelYear, fsIndex), foValue);
    }
    
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
