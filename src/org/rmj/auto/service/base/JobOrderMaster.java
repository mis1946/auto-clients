/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.service.base;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.StringUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;
import org.rmj.auto.clients.base.CompareRows;
import org.rmj.auto.parameters.CancellationMaster;

/**
 *
 * @author Arsiela
 */
public class JobOrderMaster {
    private final String MASTER_TABLE = "diagnostic_master";
    private final String JOLABOR_TABLE = "diagnostic_labor";
    private final String JOPARTS_TABLE = "diagnostic_parts";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    private CancellationMaster oTransCancel;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private boolean pbisVhclSales;
    private String psMessage;
    private Integer pnDeletedLaborRow[];
    private Integer pnDeletedPartsRow[];
    
    private CachedRowSet poMaster;
    private CachedRowSet poMasterOrig;
    private CachedRowSet poJOLabor;
    private CachedRowSet poJOLaborOrig;
    private CachedRowSet poJOParts;
    private CachedRowSet poJOPartsOrig;
    
    private CachedRowSet poVSPLabor;
    private CachedRowSet poVSPLaborOrig;
    private CachedRowSet poVSPParts;
    private CachedRowSet poVSPPartsOrig;
    
    List<Integer> deletedLaborRows = new ArrayList<>();
    List<Integer> deletedPartsRows = new ArrayList<>();
    
    public JobOrderMaster(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
    
    public void setFormType(boolean fbValue) {
        pbisVhclSales = fbValue;
    }
    
    //FOR TESTING
    public int getItemCount() throws SQLException{
        if (poMaster != null){
            poMaster.last();
            return poMaster.getRow();
        }else{
            return 0;
        }
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poMaster.first();
        
        switch (fnIndex){     
            case 1:    //sTransNox 
            case 3:    //sDSNoxxxx 
            case 4:    //sSerialID 
            case 5:    //sClientID 
            case 6:    //sWorkCtgy 
            case 7:    //sJobTypex 
            case 8:    //sLaborTyp 
            case 10:   //sEmployID 
            case 11:   //sRemarksx 
            case 12:   //cPaySrcex 
            case 13:   //sSourceNo 
            case 14:   //sSourceCD 
            case 16:   //sInsurnce 
            case 17:   //cCompUnit 
            case 18:   //sActvtyID 
            case 24:   //cPrintedx 
            case 25:   //cTranStat 
            case 26:   //sEntryByx 
            case 27:   //dEntryDte 
            case 28:   //sModified  
            case 30:   //cTrStatus
            case 31:   //sCompnyNm /*CUTOMER NAME*/
            case 32:   //sAddressx /*CUSTOMER ADDRESS*/
            case 33:   //sCoOwnrNm /*CO-OWNER NAME*/
            case 34:   //sCoBuyrNm /*CO-BUYER NAME*/
            case 35:   //sDescript
            case 36:   //sCSNoxxxx
            case 37:   //sPlateNox
            case 38:   //sFrameNox
            case 39:   //sEngineNo
            case 40:   //sEmployNm /*SE OR SA NAME*/
            case 41:   //sBranchCD /*VSP BRANCH CODE*/ 
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            /*dTimeStmp*/
            case 2:    //dTransact 
            case 15:   //dPromised
            case 29:   //dModified
                if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break; 
                
//                if (foValue instanceof Integer)
//                    poMaster.updateInt(fnIndex, (int) foValue);
//                else 
//                    poMaster.updateInt(fnIndex, 0);
//                
//                poMaster.updateRow();
//                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
//                break;
            case 9:    //nKMReadng 
            case 19:   //nLaborAmt 
            case 20:   //nPartsAmt 
            case 21:   //nTranAmtx 
            case 22:   //nRcvryAmt 
            case 23:   //nPartFeex 
                poMaster.updateDouble(fnIndex, 0.00);
                if (StringUtil.isNumeric(String.valueOf(foValue))) {
                    //poMaster.updateDouble(fnIndex,(Double) foValue);
                    poMaster.updateObject(fnIndex,foValue);
                }
                
                poMaster.updateRow();   
                break;
        }
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
        
        poMaster.absolute(fnRow);
        return poMaster.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poMaster, fsIndex));
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
            poMaster.updateString("cTranStat", RecordStatus.ACTIVE);   //0 Cancelled, 1 Active
            poMaster.updateObject("dTransact", poGRider.getServerDate());   
            poMaster.updateObject("dPromised", poGRider.getServerDate());  
            if(pbisVhclSales){
                poMaster.updateString("sWorkCtgy", "2"); //mech, body, jobo
            } else {
                poMaster.updateString("sWorkCtgy", "0"); //mech, body, jobo
            }
            
            poMaster.updateString("sJobTypex", "0"); //new, bjob, jcon
            poMaster.updateString("sLaborTyp", "0"); //gr, pm, pmgr, body, pdi (if sales)
            poMaster.updateString("cPaySrcex", "0"); //ins, pa (default), cu (comp unit), nu (new unit)
            //poMaster.updateString("cCompUnit", "0");  is company unit? 0 or 1; move to cPaySrcex?
            poMaster.updateString("cPrintedx", "0");  //is printed? 0 or 1
            
            //Add Initial Value for double datatype
            poMaster.updateDouble("nKMReadng", 0.00); 
            poMaster.updateDouble("nLaborAmt", 0.00); 
            poMaster.updateDouble("nPartsAmt", 0.00); 
            poMaster.updateDouble("nTranAmtx", 0.00); 
            poMaster.updateDouble("nRcvryAmt", 0.00); 
            poMaster.updateDouble("nPartFeex", 0.00); 
            
            poMaster.insertRow();
            poMaster.moveToCurrentRow(); 
            
            if (!clearJOLabor()){
                psMessage = "Error clear fields for JO Labor.";
                return false;
            }
            if (!clearJOParts()){
                psMessage = "Error clear fields for JO Parts.";
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
     *  Searches for a Job Order record.
     *  @param bisVhclSales Identifier on where the form has been executed.
    */
    public boolean SearchRecord() throws SQLException{
        String lsSQL = getSQ_Master();
        if(pbisVhclSales){
            lsSQL = lsSQL + " WHERE a.sTransNox LIKE '%' AND a.sWorkCtgy = '2' " +
                       " GROUP BY a.sTransNox " ;
        }else {
            lsSQL = lsSQL + " WHERE a.sTransNox LIKE '%' AND a.sWorkCtgy <> '2'" + 
                       " GROUP BY a.sTransNox " ;
        }
         
        JSONObject loJSON = null;
        if (pbWithUI){
            loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , "%"
                                                    , "Job Order No»Customer»CS No»Plate No»Cancelled»"
                                                    , "sDSNoxxxx»sCompnyNm»sCSNoxxxx»sPlateNox»cTrStatus"
                                                    , "a.sDSNoxxxx»c.sCompnyNm»d.sCSNoxxxx»e.sPlateNox"
                                                    , 0);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                
                if (!clearJOLabor()){
                    psMessage = "Error clear fields for JO Labor.";
                    return false;
                }
                if (!clearJOParts()){
                    psMessage = "Error clear fields for JO Parts.";
                    return false;
                }

                if (OpenRecord((String) loJSON.get("sTransNox")) ){
                    
                }else {
                    psMessage = "No record found/selected.";
                    return false;
                }
            }
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
            String lsSQL = getSQ_Master() + " WHERE a.sTransNox = " + SQLUtil.toSQL(fsValue); 
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
            
            if (loadJOLabor()) {
            } else {
                psMessage = "Error while loading JO Labor.";
                return false;
            }
            if (loadJOParts()) {
            } else {
                psMessage = "Error while loading JO Parts.";
                return false;
            }
            
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
     * 
    */
    public boolean UpdateRecord(){
        try {
            if (poMaster != null){
                poMasterOrig = (CachedRowSet) poMaster.createCopy();
            }
            
            if (poJOLabor != null){
                poJOLaborOrig = (CachedRowSet) poJOLabor.createCopy();
                deletedLaborRows.clear();
            }
            if (poJOParts != null){
                poJOPartsOrig = (CachedRowSet) poJOParts.createCopy();
                deletedPartsRows.clear();
            }
        } catch (SQLException ex) {
            Logger.getLogger(JobOrderMaster.class.getName()).log(Level.SEVERE, null, ex);
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
            int lnCtr = 1;
            String lsSQL = "";
            String lsTransNox = "";
            String lsDSNoxxxx = "";
            String lsgetBranchCd = "";
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                lsDSNoxxxx = MiscUtil.getNextCode(MASTER_TABLE, "sDSNoxxxx", false, poGRider.getConnection(), psBranchCd);
                setMaster("sDSNoxxxx", lsDSNoxxxx);
            }
            if (!isEntryOK()) return false;
            
            if (((String)getMaster("sWorkCtgy")).equals("jobo")){
                lsgetBranchCd = (String) getMaster("sBranchCD");
                if (psBranchCd.equals(lsgetBranchCd)){
                    lsgetBranchCd = "";
                }
            } else {
                lsgetBranchCd = "";
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            if (pnEditMode == EditMode.ADDNEW){ //add
                /*JO MASTER*/
                lsTransNox = MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", true, poGRider.getConnection(), psBranchCd);
                poMaster.updateString("sTransNox",lsTransNox); 
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();    
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»cTrStatus»sCoOwnrNm»sCoBuyrNm»sEmployNm»sBranchCD");
                
                if (lsSQL.isEmpty()){
                    psMessage = "No record to update in jo master.";
                    return false;
                }
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                    psMessage = "ADD JO MASTER: " + poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                    return false;
                }
                
                /*JO LABOR*/
                lsSQL = "";
                lnCtr = 1;
                if (getJOLaborCount() > 0){
                    poJOLabor.beforeFirst();                
                    while (poJOLabor.next()){  
                        poJOLabor.updateObject("sTransNox", lsTransNox); 
                        poJOLabor.updateObject("nEntryNox", lnCtr);
                        poJOLabor.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                        poJOLabor.updateString("sEntryByx", poGRider.getUserID());
                        poJOLabor.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poJOLabor, JOLABOR_TABLE, "sLaborDsc");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in JO labor.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, JOLABOR_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = "ADD JO LABOR: " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }
                }
                
                /*JO PARTS*/
                lsSQL = "";
                lnCtr = 1;
                if (getJOPartsCount() > 0){
                    poJOParts.beforeFirst();                
                    while (poJOParts.next()){  
                        poJOParts.updateObject("sTransNox", lsTransNox); 
                        poJOParts.updateObject("nEntryNox", lnCtr);
                        poJOParts.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                        poJOParts.updateString("sEntryByx", poGRider.getUserID());
                        poJOParts.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poJOParts, JOPARTS_TABLE, "sBarCodex»sTotlAmtx");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in JO parts.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, JOPARTS_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = "ADD JO PARTS: " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }
                }
            
            } else { //update  
                boolean lbisModified = false;
                if (!CompareRows.isRowEqual(poMaster, poMasterOrig,1)) {
                    lbisModified = true;
                }
                if(lbisModified){
                    /*JO MASTER*/
                    poMaster.updateString("sModified", poGRider.getUserID());
                    poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                    poMaster.updateRow();
                    lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                                MASTER_TABLE, 
                                                "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»cTrStatus»sCoOwnrNm»sCoBuyrNm»sEmployNm»sBranchCD", 
                                                "sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }
                    if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                        psMessage = "UPDATE JO MASTER: " + poGRider.getErrMsg();
                        if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                    }
                }
                lbisModified = false;
                
                /*JO LABOR*/
                String lsfTransNox = "";
                int lnfRow = 0;
                lsSQL = "";
                lnCtr = 1;
                
                if (deletedLaborRows != null && !deletedLaborRows.isEmpty()) {
                    pnDeletedLaborRow = deletedLaborRows.toArray(new Integer[deletedLaborRows.size()]);
                }
                
                if (pnDeletedLaborRow != null && pnDeletedLaborRow.length != 0) {
                    Arrays.sort(pnDeletedLaborRow, Collections.reverseOrder());
                    poJOLaborOrig.beforeFirst();
                    for (int rowNum : pnDeletedLaborRow) {
                        poJOLaborOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM "+JOLABOR_TABLE+" WHERE"
                                + " sLaborCde = " + SQLUtil.toSQL(poJOLaborOrig.getString("sLaborCde"))
                                + " AND sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox"))
                                + " AND nEntryNox = " + SQLUtil.toSQL(poJOLaborOrig.getString("nEntryNox"));
                        if (poGRider.executeQuery(lsSQL, JOLABOR_TABLE, psBranchCd, lsgetBranchCd) <= 0) {
                            psMessage = "DELETE JO LABOR: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                
                if (getJOLaborCount() > 0){
                    poJOLabor.beforeFirst();                
                    while (poJOLabor.next()){  
                        lsfTransNox = poJOLabor.getString("sTransNox"); 
                        lnfRow = poJOLabor.getInt("nEntryNox");
                        
                        if (lsfTransNox.isEmpty()){ //ADD
                            poJOLabor.updateObject("sTransNox", (String) getMaster("sTransNox")); 
                            poJOLabor.updateObject("nEntryNox", lnCtr);
                            poJOLabor.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                            poJOLabor.updateString("sEntryByx", poGRider.getUserID());
                            poJOLabor.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poJOLabor, JOLABOR_TABLE, "sLaborDsc");
                        } else { // UPDATE
                            poJOLabor.updateObject("nEntryNox", lnCtr);
                            poJOLabor.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poJOLabor, 
                                                        JOLABOR_TABLE, 
                                                        "sLaborDsc", 
                                                        " sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")) + 
                                                        " AND nEntryNox = " + SQLUtil.toSQL(lnfRow)+
                                                        " AND sLaborCde = " + SQLUtil.toSQL(poJOLabor.getString("sLaborCde")));
                        }
                        
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in JO labor.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, JOLABOR_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = "UPDATE JO LABOR: " +poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }
                }
                
                /*JO PARTS*/
                lsSQL = "";
                lnCtr = 1;
                lnfRow = 0;
                
                if (deletedPartsRows != null && !deletedPartsRows.isEmpty()) {
                    pnDeletedPartsRow = deletedPartsRows.toArray(new Integer[deletedPartsRows.size()]);
                }
                
                if (pnDeletedPartsRow != null && pnDeletedPartsRow.length != 0) {
                    Arrays.sort(pnDeletedPartsRow, Collections.reverseOrder());
                    poJOPartsOrig.beforeFirst();
                    for (int rowNum : pnDeletedPartsRow) {
                        poJOPartsOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM "+JOPARTS_TABLE+" WHERE"
                                + " sStockIDx = " + SQLUtil.toSQL(poJOPartsOrig.getString("sStockIDx"))
                                + " AND sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox"))
                                + " AND nEntryNox = " + SQLUtil.toSQL(poJOPartsOrig.getString("nEntryNox"));
                        
                        if (poGRider.executeQuery(lsSQL, JOPARTS_TABLE, psBranchCd, lsgetBranchCd) <= 0) {
                            psMessage = "DELETE JO PARTS: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                
                if (getJOPartsCount() > 0){
                    poJOParts.beforeFirst();                
                    while (poJOParts.next()){  
                        lsfTransNox = poJOParts.getString("sTransNox"); 
                        lnfRow = poJOParts.getInt("nEntryNox");
                        
                        if (lsfTransNox.isEmpty()){ //ADD
                            poJOParts.updateObject("sTransNox", (String) getMaster("sTransNox")); 
                            poJOParts.updateObject("nEntryNox", lnCtr);
                            poJOParts.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                            poJOParts.updateString("sEntryByx", poGRider.getUserID());
                            poJOParts.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poJOParts, JOPARTS_TABLE, "sBarCodex»sTotlAmtx");
                        } else { // UPDATE
                            poJOParts.updateObject("nEntryNox", lnCtr);
                            poJOParts.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poJOParts, 
                                                        JOPARTS_TABLE, 
                                                        "sBarCodex»sTotlAmtx", 
                                                        " sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")) + 
                                                        " AND sStockIDx = " + SQLUtil.toSQL(poJOParts.getString("sStockIDx")) +
                                                        " AND nEntryNox = " + SQLUtil.toSQL(lnfRow));
                        }
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in JO parts.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, JOPARTS_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = "UPDATE JO PARTS: " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }
                }
                
                if (poJOLabor != null){
                    poJOLaborOrig = (CachedRowSet) poJOLabor.createCopy();
                    deletedLaborRows.clear();
                }
                if (poJOParts != null){
                    poJOPartsOrig = (CachedRowSet) poJOParts.createCopy();
                    deletedPartsRows.clear();
                }
                
                if (poMaster != null){
                    poMasterOrig = (CachedRowSet) poMaster.createCopy();
                }
                
                pnDeletedLaborRow = null;
                deletedLaborRows.clear();
                pnDeletedPartsRow = null;
                deletedPartsRows.clear();
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
        
        if (poMaster.getString("sDSNoxxxx").isEmpty()){
            psMessage = "JO Number is not set.";
            return false;
        }

        if (poMaster.getString("sSourceNo").isEmpty()){
            psMessage = "Source Intake is not set."; //TODO
            return false;
        }

        if (poMaster.getString("sClientID").isEmpty()){
            psMessage = "Customer is not set.";
            return false;
        }
        
        if (getJOLaborCount() == 0 && getJOPartsCount() == 0){
            psMessage = "Please insert Labor or Parts details.";
            return false;
        }
        
//        if (poMaster.getDouble("nTranTotl") < 0.00){
//            psMessage = "Invalid Gross Amount Total.";
//            return false;
//        }
//        
//        if (poMaster.getDouble("nNetTTotl") < 0.00){
//            psMessage = "Invalid Net Amount Due.";
//            return false;
//        }
        
                
        return true;
    }
    
    public boolean computeAmount() throws SQLException{
        psMessage = "";
        int lnCtr;
        String lsQty = "0";
        BigDecimal ldblLaborAmt = new BigDecimal("0.00"); 
        BigDecimal ldblPartsAmt = new BigDecimal("0.00"); 
        BigDecimal ldblPartsTtl = new BigDecimal("0.00");  
        /*Compute Labor Total*/
        for (lnCtr = 1; lnCtr <= getJOLaborCount(); lnCtr++){
            if(String.valueOf( getJOLaborDetail(lnCtr, "nUnitPrce")) != null){
                ldblLaborAmt = ldblLaborAmt.add(new BigDecimal( String.valueOf( getJOLaborDetail(lnCtr, "nUnitPrce")))).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }
        ldblLaborAmt = ldblLaborAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
        /*Compute Parts Total*/
        for (lnCtr = 1; lnCtr <= getJOPartsCount(); lnCtr++){
            //ldblPartsAmt = ldblPartsAmt.add(new BigDecimal( String.valueOf( getJOPartsDetail(lnCtr, "nUnitPrce"))));
            lsQty = String.valueOf(getJOPartsDetail(lnCtr, "nQtyEstmt"));
            if(lsQty != null){
            } else {
                lsQty = "0";
            }
            if(String.valueOf( getJOPartsDetail(lnCtr, "nUnitPrce")) != null){
                ldblPartsAmt = new BigDecimal(lsQty).multiply(new BigDecimal( String.valueOf( getJOPartsDetail(lnCtr, "nUnitPrce"))));
                setJOPartsDetail(lnCtr,"sTotlAmtx",String.valueOf(ldblPartsAmt));
                System.out.println(" ROW "+ lnCtr + " total amount >> " + getJOPartsDetail(lnCtr, "sTotlAmtx"));
                ldblPartsTtl = ldblPartsTtl.add(ldblPartsAmt);
            }
        }
        ldblPartsTtl = ldblPartsTtl.setScale(2, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal ldblTranTotl = new BigDecimal("0.00"); 
        
        ldblTranTotl = ldblLaborAmt.add(ldblPartsTtl);
        
        setMaster("nLaborAmt",ldblLaborAmt);
        setMaster("nPartsAmt",ldblPartsTtl);
        setMaster("nTranAmtx",ldblTranTotl);
        
        return true;
    }
    
    public boolean CancelJO(){
        try {
            psMessage = "";
            if (!isCancelOK()){ return false;}
            if (!pbWithParent) poGRider.beginTrans();
            
            String lsSQL = "UPDATE " + MASTER_TABLE + " SET"
                    + " cTranStat = '0'"
//                    + ", sCancelld = " + SQLUtil.toSQL(poGRider.getUserID())
//                    + ", dCancelld = " + SQLUtil.toSQL((Date) poGRider.getServerDate())
                    + " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox"));
            
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
                psMessage = "UPDATE JO MASTER: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                return false;
            }
            
            if (!pbWithParent) poGRider.commitTrans();
            pnEditMode = EditMode.UNKNOWN;
            
        } catch (SQLException ex) {
            Logger.getLogger(JobOrderMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private boolean isCancelOK(){
    
        return true;
    }
    
    private String getSQ_Master(){
        return  " SELECT        " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
                " IFNULL(a.sTransNox,'') AS sTransNox " +  //1                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
                ", a.dTransact  " +  //2                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
                ", IFNULL(a.sDSNoxxxx,'') AS sDSNoxxxx " +  //3                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
                ", IFNULL(a.sSerialID,'') AS sSerialID " +  //4                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
                ", IFNULL(a.sClientID,'') AS sClientID " +  //5                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
                ", IFNULL(a.sWorkCtgy,'') AS sWorkCtgy " +  //6                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
                ", IFNULL(a.sJobTypex,'') AS sJobTypex " +  //7                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
                ", IFNULL(a.sLaborTyp,'') AS sLaborTyp " +  //8                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
                ", a.nKMReadng  " +  //9                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
                ", IFNULL(a.sEmployID,'') AS sEmployID " +  //10                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", IFNULL(a.sRemarksx,'') AS sRemarksx " +  //11                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", IFNULL(a.cPaySrcex,'') AS cPaySrcex " +  //12                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", IFNULL(a.sSourceNo,'') AS sSourceNo " +  //13                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", IFNULL(a.sSourceCD,'') AS sSourceCD " +  //14                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", a.dPromised  " +  //15                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                ", IFNULL(a.sInsurnce,'') AS sInsurnce " +  //16                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", IFNULL(a.cCompUnit,'') AS cCompUnit " +  //17                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", IFNULL(a.sActvtyID,'') AS sActvtyID " +  //18                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", a.nLaborAmt  " +  //19                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                ", a.nPartsAmt  " +  //20                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                ", a.nTranAmtx  " +  //21                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                ", a.nRcvryAmt  " +  //22                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                ", a.nPartFeex  " +  //23                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                ", IFNULL(a.cPrintedx,'') AS cPrintedx " +  //24                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", IFNULL(a.cTranStat,'') AS cTranStat " +  //25                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", IFNULL(a.sEntryByx,'') AS sEntryByx " +  //26                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                ", a.dEntryDte  " +  //27                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                ", IFNULL(a.sModified,'') AS sModified " +  //28                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                ", a.dModified  " +  //29                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                " , CASE WHEN a.cTranStat = '0' THEN 'Y' ELSE 'N' END AS cTrStatus " + //30                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
                 /*a.dTimeStmp, */                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                ", IFNULL(b.sCompnyNm,'') AS sCompnyNm  " +   /*customer name*/  //31                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
                ", IFNULL(CONCAT( IFNULL(CONCAT(c.sAddressx,', ') , ''),  " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
                            " IFNULL(CONCAT(e.sBrgyName,', '), ''),       " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
                            " IFNULL(CONCAT(d.sTownName, ', '),''),       " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
                            " IFNULL(CONCAT(f.sProvName),'') )	, '') AS sAddressx " +    /*customer address*/ //32                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
                ", IFNULL(j.sCompnyNm,'') AS sCoOwnrNm  " +    /*co-owner*/   //33                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                ", IFNULL(l.sCompnyNm,'') AS sCoBuyrNm  " +    /*co-buyer*/   //34                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                ", IFNULL(i.sDescript,'') AS sDescript  " +   //35                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                ", IFNULL(g.sCSNoxxxx,'') AS sCSNoxxxx  " +   //36                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                ", IFNULL(h.sPlateNox,'') AS sPlateNox  " +   //37                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                ", IFNULL(g.sFrameNox,'') AS sFrameNox  " +   //38                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                ", IFNULL(g.sEngineNo,'') AS sEngineNo  " +   //39     
                ", IFNULL(m.sCompnyNm,'') AS sEmployNm  " +    /*SE / SA NAME*/    //40   
                ", IFNULL(k.sBranchCD,'') AS sBranchCD  " +    /*VSP BRANCH CODE*/ //41
                " FROM diagnostic_master a    " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
                " LEFT JOIN client_master b ON b.sClientID = a.sClientID  " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
                " LEFT JOIN client_address c ON c.sClientID = b.sClientID AND c.cPrimaryx = '1'  " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                " LEFT JOIN TownCity d ON d.sTownIDxx = c.sTownIDxx  " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
                " LEFT JOIN barangay e ON e.sBrgyIDxx = c.sBrgyIDxx AND e.sTownIDxx = c.sTownIDxx " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
                " LEFT JOIN Province f ON f.sProvIDxx = d.sProvIDxx   " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                " LEFT JOIN vehicle_serial g ON g.sSerialID = a.sSerialID " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
                " LEFT JOIN vehicle_serial_registration h ON h.sSerialID = g.sSerialID    " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
                " LEFT JOIN vehicle_master i ON i.sVhclIDxx = g.sVhclIDxx    " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
                " LEFT JOIN client_master j ON j.sClientID = g.sCoCltIDx " + /*co-owner*/                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                " LEFT JOIN vsp_master k ON k.sTransNox = a.sSourceCD    " +                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
                " LEFT JOIN client_master l ON l.sClientID = k.sCoCltIDx " + /*co-buyer*/                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                " LEFT JOIN ggc_isysdbf.client_master m ON m.sClientID = a.sEmployID "   ;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
         
    }
    
    private String getSQ_JOLabor(){
        return  " SELECT " +                                                   
                "   IFNULL(a.sTransNox, '') AS sTransNox " + //1       
                "  ,a.nEntryNox   " +                        //2       
                "  ,IFNULL(a.sPayChrge, '') AS sPayChrge " + //3       
                "  ,IFNULL(a.sLaborCde, '') AS sLaborCde " + //4       
                "  ,IFNULL(a.sLbrPckCd, '') AS sLbrPckCd " + //5       
                "  ,a.nUnitPrce   " +                        //6       
                "  ,a.nFRTxxxxx   " +                        //7       
                "  ,IFNULL(a.sEntryByx, '') AS sEntryByx " + //8       
                "  ,a.dEntryDte   " +                        //9       
                "  ,IFNULL(b.sLaborDsc, '') AS sLaborDsc " + //10      
                /* dTimeStmp  */                                       
                " FROM " + JOLABOR_TABLE + " a " +                     
                " LEFT JOIN labor b on b.sLaborCde = a.sLaborCde " ;   
                                                                       
 
    }
    
    public void setJOLaborDetail(int fnRow, int fnIndex, Object foValue) throws SQLException{
        poJOLabor.absolute(fnRow);        
        switch (fnIndex){       
            case 1:   //sTransNox 
            case 3:   //sPayChrge 
            case 4:   //sLaborCde 
            case 5:   //sLbrPckCd 
            case 8:   //sEntryByx 
            case 10:  //sLaborDsc 
                poJOLabor.updateObject(fnIndex, (String) foValue);
                poJOLabor.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getJOLabor(fnIndex));
                break;
                       
            case 2:   //nEntryNox 
                if (foValue instanceof Integer)
                    poJOLabor.updateInt(fnIndex, (int) foValue);
                else 
                    poJOLabor.updateInt(fnIndex, 0);
                
                poJOLabor.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getJOLabor(fnIndex));  
                break;
            case 6:   //nUnitPrce 
            case 7:   //nFRTxxxxx 
                poJOLabor.updateDouble(fnIndex, 0.00);
                if (StringUtil.isNumeric(String.valueOf(foValue))) {
                    poJOLabor.updateObject(fnIndex, foValue);
                }
                
                poJOLabor.updateRow();   
                break;
            case 9:   //dEntryDte
                if (foValue instanceof Date){
                    poJOLabor.updateObject(fnIndex, foValue);
                } else {
                    poJOLabor.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poJOLabor.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getJOLabor(fnIndex));
                break;
        }       
    }     
    /**
    Sets the value of a specific field at the given row index using the provided value.
        @param fnRow The index of the row where the value will be updated.
        @param fsIndex The name of the field to be updated.
        @param foValue The new value to be set in the field.
        @throws SQLException if a database access error occurs, or this method is called on a closed result set, or the row index is not valid.
    */
    public void setJOLaborDetail(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setJOLaborDetail(fnRow, MiscUtil.getColumnIndex(poJOLabor, fsIndex), foValue);
    }
    
    /**
     * Add JO Labor to the JO Record.
     * 
     * 
    */
    public boolean addJOLabor(){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory;
            psMessage = "";
            
            if (poJOLabor == null) {
                lsSQL = MiscUtil.addCondition(getSQ_JOLabor(), "0=1");
                loRS = poGRider.executeQuery(lsSQL);
                factory = RowSetProvider.newFactory();
                poJOLabor = factory.createCachedRowSet();
                poJOLabor.populate(loRS);
                MiscUtil.close(loRS);
            }
            poJOLabor.last();
            poJOLabor.moveToInsertRow();
            MiscUtil.initRowSet(poJOLabor);
            
            poJOLabor.updateObject("nUnitPrce", 0.00);
            poJOLabor.updateObject("nFRTxxxxx", 0.00);
                    
            poJOLabor.insertRow();
            poJOLabor.moveToCurrentRow();
            
        } catch (SQLException ex) {
            Logger.getLogger(JobOrderMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private boolean checkLaborExist(String fsValue, int fnRow) throws SQLException{
        String lsDscExist = "";
        String lsValue = "";
        psMessage = "";
        
        if (fsValue.isEmpty()){ return false;}
        
        lsValue = fsValue.replace(" ", "").trim(); 
        
        for (int lnRow = 1; lnRow <= getJOLaborCount(); lnRow++){
            lsDscExist = (String) getJOLaborDetail(lnRow,"sLaborDsc");
            if (!lsDscExist.isEmpty()){
                lsDscExist = lsDscExist.replace(" ", "").trim();
                if (lsDscExist.toUpperCase().equals(lsValue.toUpperCase())){
                    if (fnRow != lnRow){
                        psMessage = "Labor " +fsValue+ " already exist at row " + lnRow + " add labor aborted.";
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Remove JO Labor to the JO Record.
     * @param fnRow specifies row index to be removed.
    */
    public boolean removeJOLabor(Integer fnRow) throws SQLException{
        if (getJOLaborCount()== 0) {
            psMessage = "No JO Labor to delete.";
            return false;
        }
        
        poJOLabor.absolute(fnRow);
        String lsFind = poJOLabor.getString("sTransNox");
        if (lsFind != null && !lsFind.isEmpty()) {
            String lsLaborCde = poJOLabor.getString("sLaborCde");
            
            if (!lsLaborCde.isEmpty()){
                for (int lnCtr = 1; lnCtr <= getOrigJOLaborCount(); lnCtr++){
                    if (lsLaborCde.equals((String) getOrigJOLaborDetail(lnCtr,"sLaborCde"))){
                        fnRow = lnCtr;
                        break;
                    }
                }
                
            }
            deletedLaborRows.add(fnRow);
        }
        poJOLabor.deleteRow();
        System.out.println("success");
        
        return true;
    }
    
    /**
     * Load JO Labor per JO Transaction.
     * 
    */
    public boolean loadJOLabor(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL = getSQ_JOLabor();
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
            
            System.out.println(lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            loRS = poGRider.executeQuery(lsSQL);
            poJOLabor = factory.createCachedRowSet();
            poJOLabor.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(JobOrderMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    /**
     * Clears the JO Labor from the data.
     * This method removes JO Labor from the dataset.  
     * 
    */
    private boolean clearJOLabor() throws SQLException {
        if (getJOLaborCount() > 0) {
            poJOLabor.beforeFirst();
            while (poJOLabor.next()) {
                poJOLabor.deleteRow();
            }
        }
        return true;
    }
    
    public int getJOLaborCount() throws SQLException{
        if (poJOLabor != null){
            poJOLabor.last();
            return poJOLabor.getRow();
        }else{
            return 0;
        }
    }
    
    public Object getJOLaborDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poJOLabor.absolute(fnRow);
        return poJOLabor.getObject(fnIndex);
    }
    
    public Object getJOLaborDetail(int fnRow, String fsIndex) throws SQLException{
        return getJOLaborDetail(fnRow, MiscUtil.getColumnIndex(poJOLabor, fsIndex));
    }
    
    public Object getJOLabor(String fsIndex) throws SQLException{
        return getJOLabor(MiscUtil.getColumnIndex(poJOLabor, fsIndex));
    }
    
    public Object getJOLabor(int fnIndex) throws SQLException{
        poJOLabor.first();
        return poJOLabor.getObject(fnIndex);
    }
    
    private int getOrigJOLaborCount() throws SQLException{
        if (poJOLaborOrig != null){
            poJOLaborOrig.last();
            return poJOLaborOrig.getRow();
        }else{
            return 0;
        }
    }
    
    private Object getOrigJOLaborDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poJOLaborOrig.absolute(fnRow);
        return poJOLaborOrig.getObject(fnIndex);
    }
    
    private Object getOrigJOLaborDetail(int fnRow, String fsIndex) throws SQLException{
        return getOrigJOLaborDetail(fnRow, MiscUtil.getColumnIndex(poJOLaborOrig, fsIndex));
    }
    
    private String getSQ_Labor(){
        return " SELECT " +
               " IFNULL(a.sLaborCde, '' ) AS sLaborCde" +
               " , IFNULL(a.sLaborDsc, '' ) AS sLaborDsc" +
               " , IFNULL(a.sWorkCtgy, '' ) AS sWorkCtgy" +
               " , a.nFRTxxxxx " +
               " , a.nLabrPrce " +
               " , a.nDiscRte1 " +
               " , a.nDiscRte2 " +
               " , a.nDiscRte3 " +
               " , IFNULL(a.cAutoCrte, '' ) AS cAutoCrte" +
               " , IFNULL(a.cRecdStat, '' ) AS cRecdStat" +
               " , IFNULL(a.sEntryByx, '' ) AS sEntryByx" +
               " , a.dEntryDte " +
               " FROM labor a " ;
    }
    
    /**
     * Searches for a labor based on the specified value.
     * This method performs a search for a labor by labor description. It allows both UI and non-UI search modes and provides feedback if no records are found.
     * @param fsValue The labor description.
     * @param fnRow specifies row index to be inserted.
    */
    public boolean searchLabor(String fsValue, int fnRow) throws SQLException{
        
        String lsSQL = getSQ_Labor();
        psMessage = "";
        fsValue = fsValue.replace(" ", "").trim();
        
        ResultSet loRS;
        JSONObject loJSON = null;   
        lsSQL = lsSQL + " WHERE a.cRecdStat = '1'  "  ;
        System.out.println(lsSQL);
        loJSON = showFXDialog.jsonSearch(poGRider, 
                                         lsSQL,
                                         "%" + fsValue +"%",
                                         "Labor ID»Labor Description", 
                                         "sLaborCde»sLaborDsc",
                                         "a.sLaborCde»a.sLaborDsc",
                                         1);

        if (loJSON != null){
            if (!checkLaborExist((String) loJSON.get("sLaborDsc"),fnRow)){ 
                setJOLaborDetail(fnRow,"sLaborCde", "");
                setJOLaborDetail(fnRow,"sLaborDsc", "");
                return false;
            }

            setJOLaborDetail(fnRow,"sLaborCde", (String) loJSON.get("sLaborCde"));
            setJOLaborDetail(fnRow,"sLaborDsc", (String) loJSON.get("sLaborDsc"));
        } else {
            psMessage = "No record found/selected.";
            setJOLaborDetail(fnRow,"sLaborCde", "");
            setJOLaborDetail(fnRow,"sLaborDsc", "");
            return false;    
        } 
        return true;
    }
    
    private String getSQ_JOParts(){
        return  " SELECT " +                                           
                " IFNULL(a.sTransNox, '') AS sTransNox  " + //1        
                " ,a.nEntryNox " +                          //2        
                " ,IFNULL(a.sStockIDx, '') AS sStockIDx " + //3        
                " ,IFNULL(a.sDescript, '') AS sDescript " + //4        
                " ,IFNULL(a.sLbrPckCd, '') AS sLbrPckCd " + //5        
                " ,a.nQtyEstmt " +                          //6        
                " ,a.nQtyUsedx " +                          //7        
                " ,a.nQtyRecvd " +                          //8        
                " ,a.nQtyRtrnx " +                          //9        
                " ,a.nUnitPrce " +                          //10       
                " ,IFNULL(a.sPayChrge, '') AS sPayChrge " + //11       
                " ,IFNULL(a.sEntryByx, '') AS sEntryByx " + //12       
                " ,a.dEntryDte " +                          //13     
                " , IFNULL(b.sBarCodex, '') AS sBarCodex" + //14 
                " , IFNULL(a.nQtyEstmt * a.nUnitPrce, '') AS sTotlAmtx " + //15
                " FROM " + JOPARTS_TABLE + " a " +
                " LEFT JOIN inventory b ON b.sStockIDx = a.sStockIDx" +
                " LEFT JOIN " + MASTER_TABLE + " c ON c.sTransNox = a.sTransNox ";
    }
    
    public void setJOPartsDetail(int fnRow, int fnIndex, Object foValue) throws SQLException{
        poJOParts.absolute(fnRow);        
        switch (fnIndex){   
            case 1:  //sTransNox  
            case 3:  //sStockIDx  
            case 4:  //sDescript  
            case 5:  //sLbrPckCd  
            case 11: //sPayChrge  
            case 12: //sEntryByx  
            case 14: //sBarCodex  
            case 15: //sTotlAmtx  
                poJOParts.updateObject(fnIndex, (String) foValue);
                poJOParts.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getJOParts(fnIndex));
                break;
            case 2:  //nEntryNox  
            case 6:  //nQtyEstmt  
            case 7:  //nQtyUsedx  
            case 8:  //nQtyRecvd  
            case 9:  //nQtyRtrnx 
                if (foValue instanceof Integer)
                    poJOParts.updateInt(fnIndex, (int) foValue);
                else 
                    poJOParts.updateInt(fnIndex, 0);
                
                poJOParts.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getJOParts(fnIndex));  
                break;
            case 10: //nUnitPrce
                poJOParts.updateDouble(fnIndex, 0.00);
                if (StringUtil.isNumeric(String.valueOf(foValue))) {
                    poJOParts.updateObject(fnIndex, foValue);
                }
                
                poJOParts.updateRow();   
                break;
            case 13: //dEntryDte
                if (foValue instanceof Date){
                    poJOParts.updateObject(fnIndex, foValue);
                } else {
                    poJOParts.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poJOParts.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getJOParts(fnIndex));
                break;
        }       
    }     
    /**
    Sets the value of a specific field at the given row index using the provided value.
        @param fnRow The index of the row where the value will be updated.
        @param fsIndex The name of the field to be updated.
        @param foValue The new value to be set in the field.
        @throws SQLException if a database access error occurs, or this method is called on a closed result set, or the row index is not valid.
    */
    public void setJOPartsDetail(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setJOPartsDetail(fnRow, MiscUtil.getColumnIndex(poJOParts, fsIndex), foValue);
    }
    
    /**
     * Add JO Parts to the JO Record.
     * 
     * @return true
    */
    public boolean AddJOParts(){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory;
            
            if (poJOParts == null) {
                lsSQL = MiscUtil.addCondition(getSQ_JOParts(), "0=1");
                loRS = poGRider.executeQuery(lsSQL);
                factory = RowSetProvider.newFactory();
                poJOParts = factory.createCachedRowSet();
                poJOParts.populate(loRS);
                MiscUtil.close(loRS);
            }
            poJOParts.last();
            poJOParts.moveToInsertRow();
            MiscUtil.initRowSet(poJOParts);
            poJOParts.updateInt("nQtyEstmt",0);  
            poJOParts.updateInt("nQtyUsedx",0);  
            poJOParts.updateInt("nQtyRecvd",0);  
            poJOParts.updateInt("nQtyRtrnx",0); 
              
            poVSPParts.updateObject("nUnitPrce", 0.00);
            poJOParts.insertRow();
            poJOParts.moveToCurrentRow();
            
        } catch (SQLException ex) {
            Logger.getLogger(JobOrderMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    /**
     * Check VSP Parts linked to JO
     * @param fsValue parts Stock ID
     * @param fnInputQty parts quantity to be input
     * @param fbIsAdd Identifier when it is call at addJOParts.
     * @param fnJoRow JO Row.
    */
    public boolean checkVSPJOParts(String fsValue, int fnInputQty, boolean fbIsAdd, int fnJoRow) throws SQLException{
        int nVSPQty = 0;
        int nTotalQty = 0;
        
        if(loadVSPParts()){
            for (int lnCtr = 1; lnCtr <= getVSPPartsCount(); lnCtr++){
                if(((String) getVSPPartsDetail(lnCtr,"sStockIDx")).equals(fsValue)){
                    nVSPQty = (Integer) getVSPPartsDetail(lnCtr,"nQuantity");
                    break;
                }
            }
        }
        
        String lsSQL = getSQ_JOParts();
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox")) +
                                                " AND c.cTranStat = '1' " + 
                                                " AND c.sSourceCD = " + SQLUtil.toSQL(poMaster.getString("sSourceCD")) +
                                                " AND a.sStockIDx = " + SQLUtil.toSQL(fsValue) );
        
//        TEST
//        lsSQL = MiscUtil.addCondition(lsSQL, "a.sTransNox <> " + SQLUtil.toSQL("V00123000001") +
//                                                " AND c.cTranStat = '1' " + 
//                                                " AND c.sSourceCD = " + SQLUtil.toSQL("V00123000001") +
//                                                " AND a.sStockIDx = " + SQLUtil.toSQL(fsValue) );
                                                
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            while(loRS.next()){
                nTotalQty = nTotalQty +  loRS.getInt("nQtyEstmt") ;
            }
            
            if(fbIsAdd){
                fnInputQty = nVSPQty - nTotalQty;
            }
            
            nTotalQty = nTotalQty + fnInputQty;
        }
        
        if ((nTotalQty > nVSPQty) || (fnInputQty > nVSPQty)){
            psMessage = "Declared VSP Parts quantity must not be less than the quantity linked to JO Parts.";
            return false;
        }

        if (fnInputQty <= 0){
            if(fbIsAdd){
                psMessage = "All remaining VSP Parts Quantity already linked to JO.";
            }else{
                psMessage = "Please input valid Parts Quantity.";
            }
            return false;
        }
        
        if(fbIsAdd){
            setJOPartsDetail(fnJoRow, "nQtyEstmt", fnInputQty);
        }
        
        MiscUtil.close(loRS);        
        return true;
    }
    
    /**
     * Remove JO Parts to the JO Record.
     * @param fnRow specifies row index to be removed.
    */
    public boolean removeJOParts(Integer fnRow) throws SQLException{
        if (getJOPartsCount()== 0) {
            psMessage = "No JO Parts to delete.";
            return false;
        }
        
        poJOParts.absolute(fnRow);
        String lsFind = poJOParts.getString("sTransNox");
        if (lsFind != null && !lsFind.isEmpty()) {
            String lsPartsCde = poJOParts.getString("sDescript");
            
            if (!lsPartsCde.isEmpty()){
                for (int lnCtr = 1; lnCtr <= getOrigJOPartsCount(); lnCtr++){
                    if (lsPartsCde.equals((String) getOrigJOPartsDetail(lnCtr,"sDescript"))){
                        fnRow = lnCtr;
                        break;
                    }
                }
            }
            
            deletedPartsRows.add(fnRow);
        }
        poJOParts.deleteRow();
        System.out.println("success");
        
        return true;
    }
    
    /**
     * Load JO Parts per JO Transaction.
     * 
    */
    public boolean loadJOParts(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL = getSQ_JOParts();
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
            
            System.out.println(lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            loRS = poGRider.executeQuery(lsSQL);
            poJOParts = factory.createCachedRowSet();
            poJOParts.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(JobOrderMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    /**
     * Clears the JO Parts from the data.
     * This method removes JO Parts from the dataset.  
     * 
    */
    private boolean clearJOParts() throws SQLException {
        if (getJOPartsCount() > 0) {
            poJOParts.beforeFirst();
            while (poJOParts.next()) {
                poJOParts.deleteRow();
            }
        }
        return true;
    }
    
    public int getJOPartsCount() throws SQLException{
        if (poJOParts != null){
            poJOParts.last();
            return poJOParts.getRow();
        }else{
            return 0;
        }
    }
    
    public Object getJOPartsDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poJOParts.absolute(fnRow);
        return poJOParts.getObject(fnIndex);
    }
    
    public Object getJOPartsDetail(int fnRow, String fsIndex) throws SQLException{
        return getJOPartsDetail(fnRow, MiscUtil.getColumnIndex(poJOParts, fsIndex));
    }
    
    public Object getJOParts(String fsIndex) throws SQLException{
        return getJOParts(MiscUtil.getColumnIndex(poJOParts, fsIndex));
    }
    
    public Object getJOParts(int fnIndex) throws SQLException{
        poJOParts.first();
        return poJOParts.getObject(fnIndex);
    }
    
    private int getOrigJOPartsCount() throws SQLException{
        if (poJOPartsOrig != null){
            poJOPartsOrig.last();
            return poJOPartsOrig.getRow();
        }else{
            return 0;
        }
    }
    
    private Object getOrigJOPartsDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poJOPartsOrig.absolute(fnRow);
        return poJOPartsOrig.getObject(fnIndex);
    }
    
    private Object getOrigJOPartsDetail(int fnRow, String fsIndex) throws SQLException{
        return getOrigJOPartsDetail(fnRow, MiscUtil.getColumnIndex(poJOPartsOrig, fsIndex));
    }
    
    private String getSQ_searchVSP(){
        return  " SELECT  " +                                                                            
                " IFNULL(a.sTransNox, '') AS sTransNox   " +                                             
                " ,a.dTransact  " +                                                                      
                " ,IFNULL(a.sVSPNOxxx, '') AS sVSPNOxxx  " +                                             
                " ,IFNULL(a.sClientID, '') AS sClientID  " +                                             
                " ,IFNULL(a.sSerialID, '') AS sSerialID  " +                                             
                " ,IFNULL(a.sBranchCD, '') AS sBranchCD  " +                                             
                " ,a.cTranStat " +                                                                       
                " , IFNULL(c.sCompnyNm,'') AS sCompnyNm  " +                                             
                " , IFNULL(CONCAT( IFNULL(CONCAT(h.sAddressx,', ') , ''), " +                            
                "       IFNULL(CONCAT(j.sBrgyName,', '), ''),  " +                                             
                "       IFNULL(CONCAT(i.sTownName, ', '),''),  " +                                             
                "       IFNULL(CONCAT(k.sProvName),'') )	, '') AS sAddressx    " +                            
                " , IFNULL(f.sDescript,'') AS sDescript  " + 																							
                " , IFNULL(d.sCSNoxxxx,'') AS sCSNoxxxx  " + 																							
                " , IFNULL(e.sPlateNox,'') AS sPlateNox  " + 																							
                " , IFNULL(d.sFrameNox,'') AS sFrameNox  " + 																							
                " , IFNULL(d.sEngineNo,'') AS sEngineNo  " +                                             
                " , IFNULL(g.sCompnyNm, '') AS sSalesExe " +                                             
                " , CASE WHEN a.cTranStat = '0' THEN 'Y' ELSE 'N' END AS cTrStatus   " +                 
                " , IFNULL(a.sCoCltIDx,'') AS sCoCltIDx  " +                                             
                " , IFNULL(l.sCompnyNm,'') AS sCoBuyrNm  " +                                             
                " FROM vsp_master a     " +                                                              
                " LEFT JOIN customer_inquiry b ON b.sTransNox = a.sInqryIDx " +                          
                " LEFT JOIN client_master c ON c.sClientID = a.sClientID  	" +														
                " LEFT JOIN vehicle_serial d ON d.sSerialID = a.sSerialID   " +														
                " LEFT JOIN vehicle_serial_registration e ON e.sSerialID = d.sSerialID  " +              
                " LEFT JOIN vehicle_master f ON f.sVhclIDxx = d.sVhclIDxx   " +                          
                " LEFT JOIN ggc_isysdbf.client_master g ON g.sClientID = b.sEmployID  " +                
                " LEFT JOIN client_address h ON h.sClientID = c.sClientID AND h.cPrimaryx = '1'    " +   
                " LEFT JOIN TownCity i on i.sTownIDxx = h.sTownIDxx    " +                               
                " LEFT JOIN barangay j ON j.sBrgyIDxx = h.sBrgyIDxx and j.sTownIDxx = h.sTownIDxx  " +   
                " LEFT JOIN Province k ON k.sProvIDxx = i.sProvIDxx   "  +                
                " LEFT JOIN client_master l ON l.sClientID = a.sCoCltIDx  " ;	                             
 
    }
    
    public boolean searchVSP(String fsValue) throws SQLException{
        String lsSQL = getSQ_searchVSP();
        lsSQL = lsSQL + " WHERE a.sVSPNOxxx LIKE " + SQLUtil.toSQL(fsValue + "%") +
                        " AND (a.sSerialID <> NULL OR a.sSerialID <> '') " +
                        " AND a.cTranStat = '1'  "  +
                        " GROUP BY a.sTransNox " ;
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                setMaster("sAddressx", loRS.getString("sAddressx"));
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
                setMaster("sPlateNox", loRS.getString("sPlateNox"));
                setMaster("sFrameNox", loRS.getString("sFrameNox"));
                setMaster("sEngineNo", loRS.getString("sEngineNo"));
                setMaster("sClientID", loRS.getString("sClientID"));
                setMaster("sSerialID", loRS.getString("sSerialID"));
                setMaster("sSourceCD", loRS.getString("sTransNox"));
                setMaster("sSourceNo", loRS.getString("sVSPNOxxx")); 
                setMaster("sCoCltIDx", loRS.getString("sCoCltIDx"));
                setMaster("sCoCltNmx", loRS.getString("sCoBuyrNm"));  
                setMaster("sEmployNm", loRS.getString("sSalesExe")); 
           
            } else {
                psMessage = "No record found.";
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sClientID", "");
                setMaster("sSerialID", "");
                setMaster("sSourceCD", "");
                setMaster("sSourceNo", "");
                setMaster("sCoCltIDx", "");
                setMaster("sCoBuyrNm", "");  
                setMaster("sEmployNm", "");
                return false;
            }           
        } else {
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "VSP No»Customer Name»CS No»Plate No", 
                                             "sVSPNOxxx»sCompnyNm»sCSNoxxxx»sPlateNox",
                                             "sVSPNOxxx»sCompnyNm»sCSNoxxxx»sPlateNox",
                                             0);
            
            if (loJSON != null){
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sAddressx", (String) loJSON.get("sAddressx"));
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("sSerialID", (String) loJSON.get("sSerialID"));
                setMaster("sSourceCD", (String) loJSON.get("sTransNox"));
                setMaster("sSourceNo", (String) loJSON.get("sVSPNOxxx"));
                setMaster("sCoCltIDx", (String) loJSON.get("sCoCltIDx"));
                setMaster("sCoCltNmx", (String) loJSON.get("sCoBuyrNm"));   
                setMaster("sEmployNm", (String) loJSON.get("sSalesExe"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sClientID", "");
                setMaster("sSerialID", "");
                setMaster("sSourceCD", "");
                setMaster("sSourceNo", "");
                setMaster("sCoCltIDx", "");
                setMaster("sCoBuyrNm", "");  
                setMaster("sEmployNm", "");
                return false;    
            }
        } 
        return true;
    }
    
    private String getSQ_VSPLabor(){
        return " SELECT " +
                "  IFNULL(a.sTransNox, '') AS sTransNox" + //1
                " , a.nEntryNox" + //2
                " , IFNULL(a.sLaborCde, '') AS sLaborCde" + //3
                " , a.nLaborAmt" + //4
                " , IFNULL(a.sChrgeTyp, '') AS sChrgeTyp" + //5
                " , IFNULL(a.sRemarksx, '') AS sRemarksx" + //6
                " , IFNULL(a.sLaborDsc, '') AS sLaborDsc" + //7
                " , a.cAddtlxxx" + //8
                " , a.dAddDatex" + //9
                " , IFNULL(a.sAddByxxx, '') AS sAddByxxx" + //10
                //" , IFNULL(c.sDSNoxxxx, '') AS sDSNoxxxx" + //11 
                " , IFNULL(GROUP_CONCAT( DISTINCT c.sDSNoxxxx),'') AS sDSNoxxxx   " + //11
                " , IFNULL(c.sTransNox, '') AS sDSCodexx" + //12
                " FROM vsp_labor a " +
                " LEFT JOIN diagnostic_labor b ON b.sLaborCde = a.sLaborCde " +
                "  LEFT JOIN diagnostic_master c ON c.sTransNox = b.sTransNox AND c.sSourceCD = a.sTransNox AND c.cTranStat = '1' ";
    }
    
    public boolean loadVSPLabor(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL = getSQ_VSPLabor();
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL((String) getMaster("sSourceCD")))
                                                    + " GROUP BY a.sLaborCde " ;
            
            System.out.println(lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            loRS = poGRider.executeQuery(lsSQL);
            poVSPLabor = factory.createCachedRowSet();
            poVSPLabor.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(JobOrderMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    public int getVSPLaborCount() throws SQLException{
        if (poVSPLabor != null){
            poVSPLabor.last();
            return poVSPLabor.getRow();
        }else{
            return 0;
        }
    }
    
    public Object getVSPLaborDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVSPLabor.absolute(fnRow);
        return poVSPLabor.getObject(fnIndex);
    }
    
    public Object getVSPLaborDetail(int fnRow, String fsIndex) throws SQLException{
        return getVSPLaborDetail(fnRow, MiscUtil.getColumnIndex(poVSPLabor, fsIndex));
    }
    
    private String getSQ_VSPParts(){
        return  "  SELECT  " +
                "  IFNULL(a.sTransNox, '') AS sTransNox" + //1
                "  , a.nEntryNox" + //2
                "  , IFNULL(a.sStockIDx, '') AS sStockIDx" + //3
                "  , a.nUnitPrce" + //4
                "  , a.nSelPrice" + //5
                "  , a.nQuantity" + //6
                "  , a.nReleased" + //7
                "  , IFNULL(a.sChrgeTyp, '') AS sChrgeTyp" + //8
                "  , IFNULL(a.sDescript, '') AS sDescript" + //9
                "  , IFNULL(a.sPartStat, '') AS sPartStat" + //10
                "  , a.dAddDatex" + //11
                "  , IFNULL(a.sAddByxxx, '') AS sAddByxxx" + //12
                "  , IFNULL(b.sBarCodex, '') AS sBarCodex" + //13
                //"  , IFNULL(d.sDSNoxxxx, '') AS sDSNoxxxx" + //14 
                "  , IFNULL(GROUP_CONCAT( DISTINCT d.sDSNoxxxx),'') AS sDSNoxxxx   " + //14 
                "  , IFNULL(d.sTransNox, '') AS sDSCodexx" + //15 
                "  , IFNULL(a.nQuantity * a.nUnitPrce, '') AS sTotlAmtx" + //16
                " FROM vsp_parts a " +
                " LEFT JOIN inventory b ON b.sStockIDx = a.sStockIDx" +
                " LEFT JOIN diagnostic_parts c ON c.sStockIDx = a.sStockIDx  " +
                " LEFT JOIN diagnostic_master d ON d.sTransNox = c.sTransNox AND d.sSourceCD = a.sTransNox AND d.cTranStat = '1' " ;
    }
    
    /**
     * Load VSP Parts per VSP Record
     * 
    */
    public boolean loadVSPParts(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL = getSQ_VSPParts();
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL((String) getMaster("sSourceCD"))) 
                                                   + " AND (NOT ISNULL(a.sStockIDx) AND TRIM(a.sStockIDx) <> '') "         
                                                   + " GROUP BY a.sStockIDx " ;
            
            System.out.println(lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            loRS = poGRider.executeQuery(lsSQL);
            poVSPParts = factory.createCachedRowSet();
            poVSPParts.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(JobOrderMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    public int getVSPPartsCount() throws SQLException{
        if (poVSPParts != null){
            poVSPParts.last();
            return poVSPParts.getRow();
        }else{
            return 0;
        }
    }
    
    public Object getVSPPartsDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVSPParts.absolute(fnRow);
        return poVSPParts.getObject(fnIndex);
    }
    
    public Object getVSPPartsDetail(int fnRow, String fsIndex) throws SQLException{
        return getVSPPartsDetail(fnRow, MiscUtil.getColumnIndex(poVSPParts, fsIndex));
    }
    
    //TODO
    public boolean searchIntake(String fsValue) throws SQLException{
//        String lsSQL = getSQ_searchVSP();
//        lsSQL = lsSQL + " WHERE a.sVSPNOxxx LIKE " + SQLUtil.toSQL(fsValue + "%") +
//                        " AND (a.sSerialID <> NULL OR a.sSerialID <> '') " +
//                        " AND a.cTranStat = '1'  "  +
//                        " GROUP BY a.sTransNox " ;
//        System.out.println(lsSQL);
//        ResultSet loRS;
//        JSONObject loJSON = null;
//        if (!pbWithUI) {   
//            lsSQL += " LIMIT 1";
//            loRS = poGRider.executeQuery(lsSQL);
//            
//            if (loRS.next()){
//                setMaster("sVSPNOxxx", loRS.getString("sVSPNOxxx"));
//                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
//                setMaster("sAddressx", loRS.getString("sAddressx"));
//                setMaster("sDescript", loRS.getString("sDescript"));
//                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
//                setMaster("sPlateNox", loRS.getString("sPlateNox"));
//                setMaster("sFrameNox", loRS.getString("sFrameNox"));
//                setMaster("sEngineNo", loRS.getString("sEngineNo"));
//                setMaster("sClientID", loRS.getString("sClientID"));
//                setMaster("sSerialID", loRS.getString("sSerialID"));
//                setMaster("sSourceCD", loRS.getString("sTransNox"));
//                setMaster("sSourceNo", loRS.getString("sVSPNOxxx")); 
//                setMaster("sCoCltIDx", loRS.getString("sCoCltIDx"));
//                setMaster("sCoCltNmx", loRS.getString("sCoBuyrNm"));  
//           
//            } else {
//                psMessage = "No record found.";
//                setMaster("sVSPNOxxx", "");
//                setMaster("sCompnyNm", "");
//                setMaster("sAddressx", "");
//                setMaster("sDescript", "");
//                setMaster("sCSNoxxxx", "");
//                setMaster("sPlateNox", "");
//                setMaster("sFrameNox", "");
//                setMaster("sEngineNo", "");
//                setMaster("sClientID", "");
//                setMaster("sSerialID", "");
//                setMaster("sSourceCD", "");
//                setMaster("sSourceNo", "");
//                setMaster("sCoCltIDx", "");
//                setMaster("sCoBuyrNm", "");  
//                return false;
//            }           
//        } else {
//            loJSON = showFXDialog.jsonSearch(poGRider, 
//                                             lsSQL,
//                                             "%" + fsValue +"%",
//                                             "VSP No»Customer Name»CS NO»Plate no", 
//                                             "sVSPNOxxx»sCompnyNm»sCSNoxxxx»sPlateNox",
//                                             "sVSPNOxxx»sCompnyNm»sCSNoxxxx»sPlateNox",
//                                             0);
//            
//            if (loJSON != null){
//                setMaster("sVSPNOxxx", (String) loJSON.get("sVSPNOxxx"));
//                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
//                setMaster("sAddressx", (String) loJSON.get("sAddressx"));
//                setMaster("sDescript", (String) loJSON.get("sDescript"));
//                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
//                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
//                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
//                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
//                setMaster("sClientID", (String) loJSON.get("sClientID"));
//                setMaster("sSerialID", (String) loJSON.get("sSerialID"));
//                setMaster("sSourceCD", (String) loJSON.get("sTransNox"));
//                setMaster("sSourceNo", (String) loJSON.get("sVSPNOxxx"));
//                setMaster("sCoCltIDx", (String) loJSON.get("sCoCltIDx"));
//                setMaster("sCoCltNmx", (String) loJSON.get("sCoBuyrNm"));   
//            } else {
//                psMessage = "No record found/selected.";
//                setMaster("sVSPNOxxx", "");
//                setMaster("sCompnyNm", "");
//                setMaster("sAddressx", "");
//                setMaster("sDescript", "");
//                setMaster("sCSNoxxxx", "");
//                setMaster("sPlateNox", "");
//                setMaster("sFrameNox", "");
//                setMaster("sEngineNo", "");
//                setMaster("sClientID", "");
//                setMaster("sSerialID", "");
//                setMaster("sSourceCD", "");
//                setMaster("sSourceNo", "");
//                setMaster("sCoCltIDx", "");
//                setMaster("sCoBuyrNm", "");  
//                return false;    
//            }
//        } 
        return true;
    }
    
    
    private void displayMasFields() throws SQLException{
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
