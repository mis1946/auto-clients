/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.sales.base;

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
 * Date Created: 08-18-2023
 */
public class VehicleSalesProposalMaster {
    private final String MASTER_TABLE = "vsp_master";
    private final String VSPFINANCE_TABLE = "vsp_finance";
    private final String VSPLABOR_TABLE = "vsp_labor";
    private final String VSPPARTS_TABLE = "vsp_parts";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    private CancellationMaster oTransCancel;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private Integer pnDeletedVSPLaborRow[];
    private Integer pnDeletedVSPPartsRow[];
    
    private CachedRowSet poMaster;
    private CachedRowSet poDetail;
    private CachedRowSet poMasterOrig;
    private CachedRowSet poVSPFinance;
    private CachedRowSet poVSPFinanceOrig;
    private CachedRowSet poVSPLabor;
    private CachedRowSet poVSPLaborOrig;
    private CachedRowSet poVSPParts;
    private CachedRowSet poVSPPartsOrig;
    
    List<Integer> deletedLaborRows = new ArrayList<>();
    List<Integer> deletedPartsRows = new ArrayList<>();
    
    public VehicleSalesProposalMaster(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
            case 1: // sTransNox
            case 3: // sVSPNOxxx 
            case 5: // sInqryIDx 
            case 6: // sClientID
            case 7: // sSerialID  
            case 9: // sRemarksx 
            case 11: // sOthrDesc
            case 20: // sChmoStat
            case 21: // sTPLStatx
            case 22: // sCompStat
            case 23: // sLTOStatx
            case 24: // sInsurTyp
            case 26: // sInsTplCd
            case 27: // sInsCodex
            case 35: // sBnkAppCD    
            case 48: // sEndPlate
            case 49: // sBranchCD
            case 56: // sDcStatCd
            case 59: // sLockedBy
            case 62: // sCancelld
            case 64: // sEntryByx
            case 66: // sModified
            case 68: // sCompnyNm
            case 69: // sAddressx	 																																				
            case 70: // sDescript																																					
            case 71: // sCSNoxxxx																																					
            case 72: // sPlateNox																																				
            case 73: // sFrameNox																																				
            case 74: // sEngineNo
            case 75: // sSalesExe
            case 76: // sSalesAgn
            case 77: // sInqClntx
            case 79: // sUdrNoxxx
            case 78: // dInqDatex
            case 80: // sInqTypex
            case 81: // sOnlStore
            case 82: // sRefTypex
            case 83: // sKeyNoxxx
            case 84: // sBranchNm
            case 34: // cPayModex
            case 54: // cIsVhclNw
            case 55: // cIsVIPxxx
            case 58: // cPrintedx
            case 61: // cTranStat
            case 85: // sInsTplNm
            case 86: // sInsComNm
                
            case 87: // sTaxIDNox 
            case 88: // sJobNoxxx 
            case 90: // sEmailAdd 
            case 91: // cOwnerxxx 
            case 92: // sMobileNo 
            case 93: // cOfficexx 
            case 94: // cTrStatus 
            case 95: // sBrnchAdd
            case 96: // sCoCltIDx
            case 97: // sCoBuyrNm
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 2:  // dTransact 
            case 4:  // dDelvryDt 
            case 57: // dDcStatDt
            case 63: // dCancelld
            case 65: // dEntryDte
            case 67: // dModified
            case 60: // dLockedDt
            /*dTimeStmp*/
            case 89: // dBirthDte
                if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break; 
            case 25: // nInsurYrx
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
                break;
            
            case 8:  // nUnitPrce
            case 10: // nAdvDwPmt 
            case 12: // nOthrChrg
            case 13: // nLaborAmt
            case 14: // nAccesAmt
            case 15: // nInsurAmt
            case 16: // nTPLAmtxx
            case 17: // nCompAmtx
            case 18: // nLTOAmtxx
            case 19: // nChmoAmtx
            case 28: // nPromoDsc
            case 29: // nFleetDsc
            case 30: // nSPFltDsc
            case 31: // nBndleDsc
            case 32: // nAddlDscx
            case 33: // nDealrInc
            case 36: // nTranTotl
            case 37: // nResrvFee
            case 38: // nDownPaym
            case 39: // nNetTTotl
            case 40: // nAmtPaidx
            case 41: // nFrgtChrg
            case 42: // nDue2Supx
            case 43: // nDue2Dlrx
            case 44: // nSPFD2Sup
            case 45: // nSPFD2Dlr
            case 46: // nPrmD2Sup
            case 47: // nPrmD2Dlr
            case 51: // nDealrAmt
            case 52: // nSlsInRte
            case 53: // nSlsInAmt
            case 50: // nDealrRte
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
            System.out.println("1st parts count >>>> " + getVSPPartsCount());
            System.out.println("1st labor count >>>> " + getVSPLaborCount());
            System.out.println("1st master count >>>> " + getItemCount());
            
            String lsSQL = getSQ_Master() + " WHERE 0=1 GROUP BY a.sTransNox ";
            System.out.println("Master " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            System.out.println("1st - parts count >>>> " + getVSPPartsCount());
            System.out.println("1st - labor count >>>> " + getVSPLaborCount());
            System.out.println("1st - master count >>>> " + getItemCount());
            
            poMaster.last();
            poMaster.moveToInsertRow(); 
            
            MiscUtil.initRowSet(poMaster);  
            poMaster.updateString("cTranStat", RecordStatus.ACTIVE);    
            poMaster.updateObject("dTransact", poGRider.getServerDate());   
            poMaster.updateObject("dDelvryDt", poGRider.getServerDate());   
            poMaster.updateString("cIsVIPxxx", "0"); 
            poMaster.updateString("cPrintedx", "0"); 
            poMaster.updateString("cPayModex", "0"); //should be load when inquiry has been selected
            poMaster.updateString("cIsVhclNw", "0"); //should be load when inquiry has been selected
            poMaster.updateString("sChmoStat", "0"); 
            poMaster.updateString("sTPLStatx", "0"); 
            poMaster.updateString("sCompStat", "0"); 
            poMaster.updateString("sLTOStatx", "0"); 
            poMaster.updateInt("nInsurYrx", 0);  //no value >> 09272023 may value na daw accd. to dave
            poMaster.updateString("sInsurTyp", "0");  //no value >> 09272023 may value na daw accd. to dave
            
            //Add Initial Value for double datatype
            poMaster.updateDouble("nUnitPrce", 0.00); 
            poMaster.updateDouble("nAdvDwPmt", 0.00); 
            poMaster.updateDouble("nOthrChrg", 0.00); 
            poMaster.updateDouble("nLaborAmt", 0.00); 
            poMaster.updateDouble("nAccesAmt", 0.00); 
            poMaster.updateDouble("nInsurAmt", 0.00); 
            poMaster.updateDouble("nTPLAmtxx", 0.00); 
            poMaster.updateDouble("nCompAmtx", 0.00); 
            poMaster.updateDouble("nLTOAmtxx", 0.00); 
            poMaster.updateDouble("nChmoAmtx", 0.00); 
            poMaster.updateDouble("nPromoDsc", 0.00);  
            poMaster.updateDouble("nFleetDsc", 0.00); 
            poMaster.updateDouble("nSPFltDsc", 0.00); 
            poMaster.updateDouble("nBndleDsc", 0.00); 
            poMaster.updateDouble("nAddlDscx", 0.00); 
            poMaster.updateDouble("nDealrInc", 0.00); 
            poMaster.updateDouble("nTranTotl", 0.00); 
            poMaster.updateDouble("nResrvFee", 0.00); 
            poMaster.updateDouble("nDownPaym", 0.00); 
            poMaster.updateDouble("nNetTTotl", 0.00); 
            poMaster.updateDouble("nAmtPaidx", 0.00); 
            poMaster.updateDouble("nFrgtChrg", 0.00); 
            poMaster.updateDouble("nDue2Supx", 0.00); 
            poMaster.updateDouble("nDue2Dlrx", 0.00); 
            poMaster.updateDouble("nSPFD2Sup", 0.00);  
            poMaster.updateDouble("nSPFD2Dlr", 0.00); 
            poMaster.updateDouble("nPrmD2Sup", 0.00); 
            poMaster.updateDouble("nPrmD2Dlr", 0.00); 
            poMaster.updateDouble("nDealrRte", 0.00); 
            poMaster.updateDouble("nDealrAmt", 0.00); 
            poMaster.updateDouble("nSlsInRte", 0.00); 
            poMaster.updateDouble("nSlsInAmt", 0.00);
            
            poMaster.insertRow(); //ERROR
            poMaster.moveToCurrentRow(); 
            
            if (!clearVSPFinance()){
                psMessage = "Error clear fields for VSP Finance.";
                return false;
            }
            
            if (!clearVSPLabor()){
                psMessage = "Error clear fields for VSP Labor.";
                return false;
            }
            if (!clearVSPParts()){
                psMessage = "Error clear fields for VSP Parts.";
                return false;
            }
            
            
            System.out.println("2nd parts count >>>> " + getVSPPartsCount());
            System.out.println("2nd labor count >>>> " + getVSPLaborCount());
            System.out.println("2nd master count >>>> " + getItemCount());
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    /**
     *  Searches for a Vehicle Sales Proposal (VSP) record.
     * 
    */
    public boolean searchRecord() throws SQLException{
        String lsSQL = getSQ_Master() + " WHERE a.sTransNox LIKE '%' AND b.cTranStat <> '6'" + 
                       " GROUP BY a.sTransNox " ;
        
        JSONObject loJSON = null;
        if (pbWithUI){
            loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , "%"
                                                    , "VSP No»Customer»CS No»Plate No»Cancelled»"
                                                    , "sVSPNOxxx»sCompnyNm»sCSNoxxxx»sPlateNox»cTrStatus"
                                                    , "a.sVSPNOxxx»c.sCompnyNm»d.sCSNoxxxx»e.sPlateNox"
                                                    , 0);
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                if (!clearVSPFinance()){
                    psMessage = "Error clear fields for VSP Finance.";
                    return false;
                }

                if (!clearVSPLabor()){
                    psMessage = "Error clear fields for VSP Labor.";
                    return false;
                }
                if (!clearVSPParts()){
                    psMessage = "Error clear fields for VSP Parts.";
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
     *  Load Vehicle Sales Proposal (VSP) List.
     *  @param fsDfrom the starting date range for filtering the data
     *  @param fsDto the ending date range for filtering the data
     * 
    */
    public boolean LoadVSPlist(String fsDfrom, String fsDto) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        String lsSQL = getSQ_Master() + " WHERE b.cTranStat <> '6' "  
                                      + " AND (DATE(a.dTransact) >= " + SQLUtil.toSQL(fsDfrom) 
                                      + " AND DATE(a.dTransact) <= " + SQLUtil.toSQL(fsDto) + ")" 
                                      + " GROUP BY a.sTransNox " 
                                      + " ORDER BY a.dTransact DESC " ;
        System.out.println(lsSQL);
        psMessage = "";
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        poDetail = factory.createCachedRowSet();
        poDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    /**
     * Opens a record with the specified value.
     * @param fsValue the value used to open the record
        * 
    */
    public boolean OpenRecord(String fsValue){
        try {
            String lsSQL = getSQ_Master() + " WHERE a.sTransNox = " + SQLUtil.toSQL(fsValue)
                                          + " GROUP BY a.sTransNox "; 
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
            
            if (loadVSPFinance()) {
            } else {
                psMessage = "Error while loading VSP Finance.";
                return false;
            }
            if (loadVSPLabor()) {
            } else {
                psMessage = "Error while loading VSP Labor.";
                return false;
            }
            if (loadVSPParts()) {
            } else {
                psMessage = "Error while loading VSP Parts.";
                return false;
            }
//            
//            System.out.println("nDue2Supx >>> " + getMaster(42));
//            System.out.println("nDue2Dlrx >>> " + getMaster(43));
//            System.out.println("nSPFD2Sup >>> " + getMaster(44));
//            System.out.println("nSPFD2Dlr >>> " + getMaster(45));
//            System.out.println("nPrmD2Sup >>> " + getMaster(46));
//            System.out.println("nPrmD2Dlr >>> " + getMaster(47));
            
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
            
            if (poVSPFinance != null){
                poVSPFinanceOrig = (CachedRowSet) poVSPFinance.createCopy();
            }
            if (poVSPLabor != null){
                poVSPLaborOrig = (CachedRowSet) poVSPLabor.createCopy();
                deletedLaborRows.clear();
            }
            if (poVSPParts != null){
                poVSPPartsOrig = (CachedRowSet) poVSPParts.createCopy();
                deletedPartsRows.clear();
            }
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
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
            String lsVSPNoxxx = "";
            String lsgetBranchCd = "";
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                lsVSPNoxxx = MiscUtil.getNextCode(MASTER_TABLE, "sVSPNOxxx", false, poGRider.getConnection(), psBranchCd + "VSP");
                setMaster("sVSPNOxxx", lsVSPNoxxx);
            }
            if (!computeAmount()) return false;
            if (!isEntryOK()) return false;
            
            lsgetBranchCd = (String) getMaster("sBranchCD");
            if (psBranchCd.equals(lsgetBranchCd)){
                lsgetBranchCd = "";
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            if (pnEditMode == EditMode.ADDNEW){ //add
                /*VSP MASTER*/
                lsTransNox = MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", true, poGRider.getConnection(), psBranchCd);
                poMaster.updateString("sTransNox",lsTransNox); 
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sSalesExe»sSalesAgn»sInqClntx»sUdrNoxxx»dInqDatex»sInqTypex»sOnlStore»sRefTypex»sKeyNoxxx»sBranchNm»sInsComNm»sInsTplNm»sTaxIDNox»sDSNoxxxx»dBirthDte»sEmailAdd»sMobileNo»cOwnerxxx»cOfficexx»sBrnchAdd»cTrStatus»sCoBuyrNm");
                
                if (lsSQL.isEmpty()){
                    psMessage = "No record to update in vsp master.";
                    return false;
                }
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                    psMessage = "ADD VSP MASTER: " + poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                    return false;
                }
                
                /*VSP FINANCE*/
                lsSQL = "";
                if (getVSPFinanceCount() > 0){
                    if (!((String) getMaster("sBnkAppCD")).isEmpty()){
                        poVSPFinance.updateString("sTransNox",lsTransNox);        
                        poVSPFinance.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poVSPFinance, VSPFINANCE_TABLE, "");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in vsp finance.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, VSPFINANCE_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            psMessage = "ADD VSP FINANCE: " + poGRider.getErrMsg();
                            if (!pbWithParent) poGRider.rollbackTrans();
                            return false;
                        }
                    }
                }
                
                /*VSP LABOR*/
                lsSQL = "";
                lnCtr = 1;
                if (getVSPLaborCount() > 0){
                    poVSPLabor.beforeFirst();                
                    while (poVSPLabor.next()){  
                        poVSPLabor.updateObject("sTransNox", lsTransNox); 
                        poVSPLabor.updateObject("nEntryNox", lnCtr);
                        poVSPLabor.updateString("cAddtlxxx", "0");
                        poVSPLabor.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poVSPLabor, VSPLABOR_TABLE, "sDSNoxxxx");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in vsp labor.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, VSPLABOR_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = "ADD VSP LABOR: " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }
                }
                
                /*VSP PARTS*/
                lsSQL = "";
                lnCtr = 1;
                if (getVSPPartsCount() > 0){
                    poVSPParts.beforeFirst();                
                    while (poVSPParts.next()){  
                        poVSPParts.updateObject("sTransNox", lsTransNox); 
                        poVSPParts.updateObject("nEntryNox", lnCtr);
                        poVSPParts.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poVSPParts, VSPPARTS_TABLE, "sBarCodex»sDSNoxxxx»sTotlAmtx»sQtyEstmt»sPartDesc");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in vsp parts.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, VSPPARTS_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = "ADD VSP PARTS: " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
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
                    /*VSP MASTER*/
                    poMaster.updateString("sModified", poGRider.getUserID());
                    poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                    poMaster.updateRow();
                    lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                                MASTER_TABLE, 
                                                "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sSalesExe»sSalesAgn»sInqClntx»sUdrNoxxx»dInqDatex»sInqTypex»sOnlStore»sRefTypex»sKeyNoxxx»sBranchNm»sInsComNm»sInsTplNm»sTaxIDNox»sDSNoxxxx»dBirthDte»sEmailAdd»sMobileNo»cOwnerxxx»cOfficexx»sBrnchAdd»cTrStatus»sCoBuyrNm", 
                                                "sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }
                    if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                        psMessage = "UPDATE VSP MASTER: " + poGRider.getErrMsg();
                        if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                    }
                }
                lbisModified = false;
                
                if(getVSPFinanceCountOrig() == 0){
                    if (!((String) getMaster("sBnkAppCD")).isEmpty()){
                        poVSPFinance.updateString("sTransNox",(String) getMaster("sTransNox"));        
                        poVSPFinance.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poVSPFinance, VSPFINANCE_TABLE, "");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in vsp finance.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, VSPFINANCE_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            psMessage = "ADD VSP FINANCE: " + poGRider.getErrMsg();
                            if (!pbWithParent) poGRider.rollbackTrans();
                            return false;
                        }
                    }
                } else { 
                    /*VSP FINANCE*/
                    lsSQL = "";
                    if (getVSPFinanceCount() > 0){
                        if (!CompareRows.isRowEqual(poVSPFinance, poVSPFinanceOrig,1)) {
                            lbisModified = true;
                        }

                        if(lbisModified){
                            if (!((String) getMaster("sBnkAppCD")).isEmpty()){
                                lsSQL = MiscUtil.rowset2SQL(poVSPFinance, 
                                                            VSPFINANCE_TABLE, 
                                                            "",
                                                            "sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
                                if (lsSQL.isEmpty()){
                                    psMessage = "No record to update.";
                                    return false;
                                }
                                if (poGRider.executeQuery(lsSQL, VSPFINANCE_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                                    psMessage = "UPDATE VSP FINANCE: " + poGRider.getErrMsg();
                                    if (!pbWithParent) poGRider.rollbackTrans();
                                    return false;
                                }
                            }
                        }
                    } else {
                        lsSQL = "DELETE FROM "+VSPFINANCE_TABLE+" WHERE "
                                + " sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox"));
                        if (poGRider.executeQuery(lsSQL, VSPFINANCE_TABLE, psBranchCd, lsgetBranchCd) <= 0) {
                            psMessage = "DELETE VSP FINANCE: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                
                /*VSP LABOR*/
                String lsfTransNox = "";
                int lnfRow = 0;
                lsSQL = "";
                lnCtr = 1;
                
                if (deletedLaborRows != null && !deletedLaborRows.isEmpty()) {
                    pnDeletedVSPLaborRow = deletedLaborRows.toArray(new Integer[deletedLaborRows.size()]);
                }
                
                if (pnDeletedVSPLaborRow != null && pnDeletedVSPLaborRow.length != 0) {
                    Arrays.sort(pnDeletedVSPLaborRow, Collections.reverseOrder());
                    poVSPLaborOrig.beforeFirst();
                    for (int rowNum : pnDeletedVSPLaborRow) {
                        poVSPLaborOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM "+VSPLABOR_TABLE+" WHERE"
                                + " sLaborCde = " + SQLUtil.toSQL(poVSPLaborOrig.getString("sLaborCde"))
                                + " AND sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox"))
                                + " AND nEntryNox = " + SQLUtil.toSQL(poVSPLaborOrig.getString("nEntryNox"));
                        if (poGRider.executeQuery(lsSQL, VSPLABOR_TABLE, psBranchCd, lsgetBranchCd) <= 0) {
                            psMessage = "DELETE VSP LABOR: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                
                if (getVSPLaborCount() > 0){
                    poVSPLabor.beforeFirst();                
                    while (poVSPLabor.next()){  
                        lsfTransNox = poVSPLabor.getString("sTransNox"); //getVSPLaborDetail(lnCtr, "sTransNox");// check if user added new 
                        lnfRow = poVSPLabor.getInt("nEntryNox");
                        
                        if (lsfTransNox.isEmpty()){ //ADD
                            poVSPLabor.updateObject("sTransNox", (String) getMaster("sTransNox")); 
                            poVSPLabor.updateObject("nEntryNox", lnCtr);
                            //poVSPLabor.updateString("cAddtlxxx", "1");
                            poVSPLabor.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poVSPLabor, VSPLABOR_TABLE, "sDSNoxxxx");
                        } else { // UPDATE
                            poVSPLabor.updateObject("nEntryNox", lnCtr);
                            poVSPLabor.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poVSPLabor, 
                                                        VSPLABOR_TABLE, 
                                                        "sDSNoxxxx", 
                                                        " sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")) + 
                                                        " AND nEntryNox = " + SQLUtil.toSQL(lnfRow)+
                                                        " AND sLaborCde = " + SQLUtil.toSQL(poVSPLabor.getString("sLaborCde")));
                        }
                        
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in vsp labor.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, VSPLABOR_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = "UPDATE VSP LABOR: " +poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }
                }
                
                /*VSP PARTS*/
                lsSQL = "";
                lnCtr = 1;
                lnfRow = 0;
                
                if (deletedPartsRows != null && !deletedPartsRows.isEmpty()) {
                    pnDeletedVSPPartsRow = deletedPartsRows.toArray(new Integer[deletedPartsRows.size()]);
                }
                
                if (pnDeletedVSPPartsRow != null && pnDeletedVSPPartsRow.length != 0) {
                    Arrays.sort(pnDeletedVSPPartsRow, Collections.reverseOrder());
                    poVSPPartsOrig.beforeFirst();
                    for (int rowNum : pnDeletedVSPPartsRow) {
                        poVSPPartsOrig.absolute(rowNum);
                        lsSQL = "DELETE FROM "+VSPPARTS_TABLE+" WHERE"
                                + " sStockIDx = " + SQLUtil.toSQL(poVSPPartsOrig.getString("sStockIDx"))
                                + " AND sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox"))
                                + " AND nEntryNox = " + SQLUtil.toSQL(poVSPPartsOrig.getString("nEntryNox"));
                        
                        if (poGRider.executeQuery(lsSQL, VSPPARTS_TABLE, psBranchCd, lsgetBranchCd) <= 0) {
                            psMessage = "DELETE VSP PARTS: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }
                    }
                }
                
                if (getVSPPartsCount() > 0){
                    poVSPParts.beforeFirst();                
                    while (poVSPParts.next()){  
                        //lsfTransNox = (String) getVSPPartsDetail(lnCtr, "sTransNox");// check if user added new 
                        lsfTransNox = poVSPParts.getString("sTransNox"); //getVSPLaborDetail(lnCtr, "sTransNox");// check if user added new 
                        lnfRow = poVSPParts.getInt("nEntryNox");
                        
                        if (lsfTransNox.isEmpty()){ //ADD
                            poVSPParts.updateObject("sTransNox", (String) getMaster("sTransNox")); 
                            poVSPParts.updateObject("nEntryNox", lnCtr);
                            poVSPParts.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poVSPParts, VSPPARTS_TABLE, "sBarCodex»sDSNoxxxx»sTotlAmtx»sQtyEstmt»sPartDesc");
                        } else { // UPDATE
                            poVSPParts.updateObject("nEntryNox", lnCtr);
                            poVSPParts.updateRow();

                            lsSQL = MiscUtil.rowset2SQL(poVSPParts, 
                                                        VSPPARTS_TABLE, 
                                                        "sBarCodex»sDSNoxxxx»sTotlAmtx»sQtyEstmt»sPartDesc", 
                                                        " sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")) + 
                                                        " AND sStockIDx = " + SQLUtil.toSQL(poVSPParts.getString("sStockIDx")) +
                                                        " AND nEntryNox = " + SQLUtil.toSQL(lnfRow));
                        }
                        //lsSQL = MiscUtil.rowset2SQL(poVSPParts, VSPPARTS_TABLE, "sBarCodex");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update in vsp parts.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, VSPPARTS_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = "UPDATE VSP PARTS: " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }
                }
                
                if (poVSPLabor != null){
                    poVSPLaborOrig = (CachedRowSet) poVSPLabor.createCopy();
                    deletedLaborRows.clear();
                }
                if (poVSPParts != null){
                    poVSPPartsOrig = (CachedRowSet) poVSPParts.createCopy();
                    deletedPartsRows.clear();
                }
                
                if (poVSPFinance != null){
                    poVSPFinanceOrig = (CachedRowSet) poVSPFinance.createCopy();
                }
                
                if (poMaster != null){
                    poMasterOrig = (CachedRowSet) poMaster.createCopy();
                }
                
//                poVSPLaborOrig = (CachedRowSet) poVSPLabor.createCopy();
//                poVSPPartsOrig = (CachedRowSet) poVSPParts.createCopy();
//                poVSPFinanceOrig = (CachedRowSet) poVSPFinance.createCopy();
                
                pnDeletedVSPLaborRow = null;
                deletedLaborRows.clear();
                pnDeletedVSPPartsRow = null;
                deletedPartsRows.clear();
            }
            
            /*UPDATE INQUIRY*/
            //Update customer_inquiry status to with VSP
            lsSQL = "";
            String lsSerialID = (String) getMaster("sSerialID");
            if(((String) getMaster("sUdrNoxxx")).isEmpty()){
                lsSQL = "UPDATE customer_inquiry SET" +
                        " cTranStat = '3'" +
                    " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));
                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, lsgetBranchCd) <= 0){
                    psMessage = "UPDATE CUSTOMER INQUIRY: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                } 
                
                if (!lsSerialID.isEmpty()){
                    if(((String) getMaster("sUdrNoxxx")).isEmpty()){
                        lsSQL = "UPDATE vehicle_serial SET" +
                                " cSoldStat = '2'" +
                                ", sClientID = " + SQLUtil.toSQL((String) getMaster("sClientID")) +
                                ", sCoCltIDx = " + SQLUtil.toSQL((String) getMaster("sCoCltIDx")) +
                                " WHERE sSerialID = " + SQLUtil.toSQL(lsSerialID);
                        if (poGRider.executeQuery(lsSQL, "vehicle_serial", psBranchCd, lsgetBranchCd) <= 0){
                            psMessage = "UPDATE VEHICLE SERIAL: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        } 
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
    
    /**
     * Saves a record to the database for updating of VSP Part Number.
     * @param fsValue the Sales Parts Description
     * @param fnRow the VSP Parts Row Number
     * 
    */
    public boolean updateVSPPartNumber(String fsValue, int fnRow){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            String lsSQL = "";
            String lsgetBranchCd = "";
            
            if (!isEntryOK()) return false;
            
            lsgetBranchCd = (String) getMaster("sBranchCD");
            if (psBranchCd.equals(lsgetBranchCd)){
                lsgetBranchCd = "";
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            /*UPDATE VSP PART NUMBER*/
            lsSQL = "";
            if (getVSPPartsCount() > 0){
                lsSQL = MiscUtil.rowset2SQL(poVSPParts, 
                                                VSPPARTS_TABLE, 
                                                "sBarCodex»sDSNoxxxx»sTotlAmtx»sQtyEstmt»sPartDesc", 
                                                " sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")) + 
                                                " AND sStockIDx = " + SQLUtil.toSQL(getVSPPartsDetail(fnRow,"sStockIDx")) +
                                                " AND nEntryNox = " + SQLUtil.toSQL(fnRow) +
                                                " AND sDescript = " + SQLUtil.toSQL(fsValue)) ;

                if (lsSQL.isEmpty()){
                     psMessage = "No record to update in vsp parts.";
                     return false;
                 }
                 if (poGRider.executeQuery(lsSQL, VSPPARTS_TABLE, psBranchCd, lsgetBranchCd) <= 0){
                     if (!pbWithParent) poGRider.rollbackTrans();
                     psMessage = "UPDATE VSP PARTS NUMBER : " + poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                     return false;
                 }
            }

            if (poVSPParts != null){
                poVSPPartsOrig = (CachedRowSet) poVSPParts.createCopy();
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
        
        if (poMaster.getString("sVSPNOxxx").isEmpty()){
            psMessage = "VSP Number is not set.";
            return false;
        }

        if (poMaster.getString("sInqryIDx").isEmpty()){
            psMessage = "Inquiry is not set.";
            return false;
        }

        if (poMaster.getString("sClientID").isEmpty()){
            psMessage = "Buying Customer is not set.";
            return false;
        }
        
        if (poMaster.getDouble("nTranTotl") == 0.00) {
            if (poMaster.getString("cPayModex").equals("0")){
                psMessage = "Please Enter Amount to be transact.";
                return false;
            } else {
                if (((Double) getVSPFinance("nFinAmtxx")) == 0.00){
                    psMessage = "Please Enter Amount to be transact.";
                    return false;
                }
            }
        }
        
        if (!poMaster.getString("cPayModex").equals("0")){
            if (poMaster.getString("sBnkAppCD").isEmpty()){
                //if (((Double) getVSPFinance("nFinAmtxx")) > 0.00){
                psMessage = "Please select Bank to be finance.";
                return false;
                //}
            }
        }
        
        if (poMaster.getDouble("nTranTotl") < 0.00){
            psMessage = "Invalid Gross Amount Total.";
            return false;
        }
        
        if (poMaster.getDouble("nNetTTotl") < 0.00){
            psMessage = "Invalid Net Amount Due.";
            return false;
        }
        
        if (poMaster.getDouble("nOthrChrg") > 0.00){
            if (poMaster.getString("sOthrDesc").isEmpty()){
                psMessage = "Please enter Miscellaneous Charge Description.";
                return false;
            }
        }
        
        //Validate Fleet Discount
        if (!poMaster.getString("cIsVIPxxx").equals("0")){
            if (poMaster.getDouble("nFleetDsc") > 0.00){
                if ((poMaster.getDouble("nDue2Supx") == 0.00) && (poMaster.getDouble("nDue2Dlrx") == 0.00)){
                    psMessage = "Please set amount of rate for Plant / Dealer.";
                    return false;
                }
            }
            
            if (poMaster.getDouble("nSPFltDsc") > 0.00){
                if ((poMaster.getDouble("nSPFD2Sup") == 0.00) && (poMaster.getDouble("nSPFD2Dlr") == 0.00)){
                    psMessage = "Please set amount of rate for Plant / Dealer.";
                    return false;
                }
            }
        }
        
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = lsSQL + " WHERE a.sVSPNOxxx = " + SQLUtil.toSQL(poMaster.getString("sVSPNOxxx")) +
                        " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox")) +
                        " GROUP BY a.sTransNox " ;
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing VSP Number.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        lsSQL = getSQ_Inquiry() + " WHERE a.sTransNox = " + SQLUtil.toSQL(poMaster.getString("sInqryIDx")) +
                        " AND a.cPayModex <> " + SQLUtil.toSQL(poMaster.getString("cPayModex")) +
                        " GROUP BY a.sTransNox " ;
        
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Payment Mode must be the same with inquiry.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        //Validate Insurance
        //TPL
        switch (poMaster.getString("sTPLStatx")){
            case "1":
                if (poMaster.getString("sInsTplCd").isEmpty()){
                    psMessage = "Please select Insurance Company.";
                    return false;
                }
                
                if (poMaster.getDouble("nTPLAmtxx") > 0.00){
                    psMessage = "Amount cannot be more than 0.00 if TPL status is FOC.";
                    return false;
                }
            break;
            case "3":
                if (poMaster.getString("sInsTplCd").isEmpty()){
                    psMessage = "Please select Insurance Company.";
                    return false;
                }
                
                if (poMaster.getDouble("nTPLAmtxx") <= 0.00){
                    psMessage = "Amount cannot be 0.00 if TPL status is not FOC.";
                    return false;
                }
            break;
        
        }
        
        //COMPRE
        switch(poMaster.getString("sCompStat")){
            case "1":
            case "3":
                if (poMaster.getString("sInsCodex").isEmpty()){
                    psMessage = "Please select Insurance Company.";
                    return false;
                }
                
                if (poMaster.getString("sCompStat").equals("1")){
                    if (poMaster.getDouble("nCompAmtx") > 0.00){
                        psMessage = "Amount cannot be more than 0.00 if COMPRE status is FOC.";
                        return false;
                    }
                } else {
                    if (poMaster.getDouble("nCompAmtx") <= 0.00){
                        psMessage = "Amount cannot be 0.00 if COMPRE status is not FOC.";
                        return false;
                    }
                }
                
                if (poMaster.getString("sCompStat").equals("3")){
                    if (poMaster.getString("sInsurTyp").equals("0")){
                        psMessage = "Please select Insurance Type.";
                        return false;

                    }

                    if (poMaster.getString("nInsurYrx").equals("0")){
                        psMessage = "Please select Insurance Year.";
                        return false;

                    }
                } 
                
                break;
        }
        
        //Validate VSP Labor
        String sValue = "";
        String sChrgeTyp = "";
        //Double ldblAmt = 0.00;
        BigDecimal ldblAmt = new BigDecimal("100");
        int lnRow = 0;
        int lnQty = 0;
        for (lnRow = 1;lnRow <= getVSPLaborCount(); lnRow++ ){
            sValue = (String) getVSPLaborDetail(lnRow, "sLaborCde");
            ldblAmt = new BigDecimal(String.valueOf(getVSPLaborDetail(lnRow, "nLaborAmt")));
            sChrgeTyp = (String) getVSPLaborDetail(lnRow, "sChrgeTyp");
            
            System.out.println("nLaborAmt >>> " + ldblAmt);
            System.out.println("sLaborCde >>> " + sValue);
            System.out.println("sChrgeTyp >>> " + sChrgeTyp);
            
            if (sValue.isEmpty()){
                psMessage = "Please select Labor on row " + lnRow;
                return false;
            }
            
            if (sChrgeTyp.isEmpty()){
                psMessage = "Please select Charge Type on row " + lnRow;
                return false;
            } 
            
            if (sChrgeTyp.equals("0")){
                //if (ldblAmt > 0.00) {
                if (ldblAmt.compareTo(BigDecimal.ZERO) != 0) {
                    psMessage = "Invalid Labor Amount on row " + lnRow + ". Charge type is FOC.";
                    return false;
                }
            } else {
                //if (ldblAmt <= 0.00) {
                if (ldblAmt.compareTo(BigDecimal.ZERO) <= 0) {
                    psMessage = "Invalid Labor Amount on row " + lnRow + ". Charge type is not FOC.";
                    return false;
                }
            }
            
        }
        sValue = "";
        sChrgeTyp = "";
        ldblAmt = new BigDecimal("100");
        //Validate VSP Parts
        for (lnRow = 1;lnRow <= getVSPPartsCount(); lnRow++ ){
            sValue = (String) getVSPPartsDetail(lnRow, "sDescript");
            //ldblAmt = (Double) getVSPPartsDetail(lnRow, "nUnitPrce");
            ldblAmt = new BigDecimal(String.valueOf(getVSPPartsDetail(lnRow, "nUnitPrce")));
            lnQty = (Integer) getVSPPartsDetail(lnRow, "nQuantity");
            sChrgeTyp = (String) getVSPPartsDetail(lnRow, "sChrgeTyp");
            System.out.println("nUnitPrce >>> " + ldblAmt);
            System.out.println("sDescript >>> " + sValue);
            System.out.println("nQuantity >>> " + lnQty);
            System.out.println("sChrgeTyp >>> " + sChrgeTyp);
            if (sValue.isEmpty()){
                psMessage = "Parts Description cannot be empty at row " + lnRow;
                return false;
            }
            
            if (lnQty <= 0){
                psMessage = "Invalid Parts Quantity at row " + lnRow;
                return false;
            }
            
            if (sChrgeTyp.isEmpty()){
                psMessage = "Please select Charge Type on row " + lnRow;
                return false;
            } 
            
            if (sChrgeTyp.equals("0")){
                //if (ldblAmt > 0.00) {
                if (ldblAmt.compareTo(BigDecimal.ZERO) != 0) {
                    psMessage = "Invalid Parts Amount on row " + lnRow + ". Charge type is FOC.";
                    return false;
                }
            } else {
                //if (ldblAmt <= 0.00) {
                if (ldblAmt.compareTo(BigDecimal.ZERO) <= 0) {
                    psMessage = "Invalid Parts Amount on row " + lnRow + ". Charge type is not FOC.";
                    return false;
                }
            }
             
        }
                
        return true;
    }
    
    /**
     * Cancels an VSP record in the database.
     * This method is used to cancel an VSP record, with validation checks for the appropriate cancellation scenario. 
     * It updates the transaction status and handles database operations.
     * 
    */
    public boolean cancelVSP() {
        try {
            psMessage = "";
            if (!isCancelOK()){ return false;}
            if (!pbWithParent) poGRider.beginTrans();
            
            String lsSQL = "UPDATE " + MASTER_TABLE + " SET"
                    + " cTranStat = '0'"
                    + ", sCancelld = " + SQLUtil.toSQL(poGRider.getUserID())
                    + ", dCancelld = " + SQLUtil.toSQL((Date) poGRider.getServerDate())
                    + " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox"));
            
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
                psMessage = "UPDATE VSP MASTER: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                return false;
            }
            
            //Update Inquiry to LOST SALE
//            if(fbIsLostSale){
//                lsSQL = "UPDATE customer_inquiry SET" +
//                        " cTranStat = '2'" +
//                        " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));
//                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, "") <= 0){
//                    psMessage = "UPDATE CUSTOMER INQUIRY: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
//                    return false;
//                }
//                
//            } else {
//                //Update Inquiry to ON PROCESS
//                lsSQL = "UPDATE customer_inquiry SET" +
//                        " cTranStat = '1'" +
//                        " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));
//                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, "") <= 0){
//                    psMessage = "UPDATE CUSTOMER INQUIRY: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
//                    return false;
//                }
//            }
            
            //Update Vehicle Serial to AVAILABLE FOR SALE and SET NULL for Client ID
            if (!((String) getMaster("sSerialID")).isEmpty()){
                lsSQL = "UPDATE vehicle_serial SET" +
                        " cSoldStat = '1'" +
                        //", sClientID = NULL " +
                        " WHERE sSerialID = " + SQLUtil.toSQL((String) getMaster("sSerialID"));
                if (poGRider.executeQuery(lsSQL, "vehicle_serial", psBranchCd, "") <= 0){
                    psMessage = "UPDATE VEHICLE SERIAL: " + poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                } 
            }
            
            if (!pbWithParent) poGRider.commitTrans();
            pnEditMode = EditMode.UNKNOWN;
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    /**
     * Validate data before cancellation of transaction.
     * 
    */
    private boolean isCancelOK() throws SQLException{
        poMaster.first();
        
        if (!getMaster("sUdrNoxxx").toString().isEmpty()){
            psMessage = "Existing UDR No. " + getMaster("sUdrNoxxx").toString() + ".";       
            return false;
        }
        
        if (!getMaster("sDSNoxxxx").toString().isEmpty()){
            psMessage = "You cannot cancel VSP that is linked to Job Order.";       
            return false;
        }
       
        String lsSQL = getSQ_Invoice();
        ResultSet loRS;
        lsSQL = lsSQL + " WHERE sSourceCd = " + SQLUtil.toSQL(poMaster.getString("sTransNox")) +
                        " AND cTranStat = '1' ";
        System.out.println(lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Please cancel Vehicle Sales Invoice before cancelling this VSP.";
            MiscUtil.close(loRS);        
            return false;
        }
        
//        String lsSQL = getSQ_Master();
//        ResultSet loRS;
//        lsSQL = lsSQL + " WHERE a.sVSPNOxxx = " + SQLUtil.toSQL(poMaster.getString("sVSPNOxxx")) +
//                        " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox")); 
//        loRS = poGRider.executeQuery(lsSQL);
//        if (MiscUtil.RecordCount(loRS) > 0){
//            psMessage = "Existing VSP Number.";
//            MiscUtil.close(loRS);        
//            return false;
//        }
                   
        return true;
    }
    
    private String getSQ_Master(){
    return  " SELECT " + 
            " IFNULL(a.sTransNox, '') AS sTransNox " + //1
            " ,a.dTransact " + //2
            " ,IFNULL(a.sVSPNOxxx, '') AS sVSPNOxxx " + //3
            " ,a.dDelvryDt " + //4
            " ,IFNULL(a.sInqryIDx, '') AS sInqryIDx " + //5
            " ,IFNULL(a.sClientID, '') AS sClientID " + //6
            " ,IFNULL(a.sSerialID, '') AS sSerialID " + //7
            " ,a.nUnitPrce " + //8
            " ,IFNULL(a.sRemarksx, '') AS sRemarksx " + //9
            " ,a.nAdvDwPmt " + //10
            " ,IFNULL(a.sOthrDesc, '') AS sOthrDesc" + //11
            " ,a.nOthrChrg " + //12
            " ,a.nLaborAmt " + //13
            " ,a.nAccesAmt " + //14
            " ,a.nInsurAmt " + //15
            " ,a.nTPLAmtxx " + //16
            " ,a.nCompAmtx " + //17
            " ,a.nLTOAmtxx " + //18
            " ,a.nChmoAmtx " + //19
            " ,IFNULL(a.sChmoStat, '') AS sChmoStat " + //20
            " ,IFNULL(a.sTPLStatx, '') AS sTPLStatx " + //21
            " ,IFNULL(a.sCompStat, '') AS sCompStat " + //22
            " ,IFNULL(a.sLTOStatx, '') AS sLTOStatx " + //23
            " ,IFNULL(a.sInsurTyp, '') AS sInsurTyp " + //24
            " ,a.nInsurYrx  " + //25
            " ,IFNULL(a.sInsTplCd, '') AS sInsTplCd " + //26
            " ,IFNULL(a.sInsCodex, '') AS sInsCodex " + //27
            " ,a.nPromoDsc " + //28
            " ,a.nFleetDsc " + //29
            " ,a.nSPFltDsc " + //30
            " ,a.nBndleDsc " + //31
            " ,a.nAddlDscx " + //32
            " ,a.nDealrInc " + //33
            " ,a.cPayModex " + //34
            " ,IFNULL(a.sBnkAppCD, '') AS sBnkAppCD " + //35
            " ,a.nTranTotl " + //36
            " ,a.nResrvFee " + //37
            " ,a.nDownPaym " + //38
            " ,a.nNetTTotl " + //39
            " ,a.nAmtPaidx " + //40
            " ,a.nFrgtChrg " + //41
            " ,a.nDue2Supx " + //42
            " ,a.nDue2Dlrx " + //43
            " ,a.nSPFD2Sup " + //44
            " ,a.nSPFD2Dlr " + //45
            " ,a.nPrmD2Sup " + //46
            " ,a.nPrmD2Dlr " + //47 
            " ,IFNULL(a.sEndPlate, '') AS sEndPlate " + //48
            " ,IFNULL(a.sBranchCD, '') AS sBranchCD " + //49
            " ,a.nDealrRte " + //50
            " ,a.nDealrAmt " + //51
            " ,a.nSlsInRte " + //52
            " ,a.nSlsInAmt " + //53
            " ,a.cIsVhclNw " + //54
            " ,a.cIsVIPxxx " + //55
            " ,IFNULL(a.sDcStatCd, '') AS sDcStatCd " + //56
            " ,a.dDcStatDt " + //57
            " ,a.cPrintedx " + //58
            " ,IFNULL(a.sLockedBy, '') AS sLockedBy " + //59
            " ,a.dLockedDt " + //60
            " ,a.cTranStat " + //61
            " ,IFNULL(a.sCancelld, '') AS sCancelld  " + //62
            " ,a.dCancelld " + //63
            " ,IFNULL(a.sEntryByx, '') AS sEntryByx " + //64
            " ,a.dEntryDte " + //65
            " ,IFNULL(a.sModified, '') AS sModified " + //66
            " ,a.dModified " + //67
             /*dTimeStmp*/
            " , IFNULL(c.sCompnyNm,'') AS sCompnyNm    " + //68
            " , IFNULL(CONCAT( IFNULL(CONCAT(h.sAddressx,', ') , ''), " +
            " 	IFNULL(CONCAT(j.sBrgyName,', '), ''), " +
            " 	IFNULL(CONCAT(i.sTownName, ', '),''), " +
            " 	IFNULL(CONCAT(k.sProvName),'') )	, '') AS sAddressx " + //69 
            " , IFNULL(f.sDescript,'') AS sDescript    " + //70																																						
            " , IFNULL(d.sCSNoxxxx,'') AS sCSNoxxxx    " + //71																																						
            " , IFNULL(e.sPlateNox,'') AS sPlateNox    " + //72																																					
            " , IFNULL(d.sFrameNox,'') AS sFrameNox    " + //73																																					
            ", IFNULL(d.sEngineNo,'') AS sEngineNo    " + //74
            " ,IFNULL(g.sCompnyNm, '') AS sSalesExe   " + //75
            " ,IFNULL(t.sCompnyNm, '') AS sSalesAgn  " + //76 /*TODO*/
            " ,IFNULL(q.sCompnyNm, '') AS sInqClntx " + //77
            " ,IFNULL (b.dTransact, '') AS  dInqDatex    " + //78
            " ,IFNULL(l.sReferNox, '') AS sUdrNoxxx " + //79
            " ,IFNULL (b.sSourceCD, '') AS  sInqTypex    " + //80
            " , IFNULL(m.sPlatform, '') AS sOnlStore " + //81
            " , '' AS  sRefTypex " + //82 /*TODO*/ 
            " , IFNULL(d.sKeyNoxxx,'') AS sKeyNoxxx    " + //83
            " , IFNULL(n.sBranchNm,'') AS sBranchNm " + //84
            " , IFNULL(u.sCompnyNm, '') AS sInsTplNm " + //85 /*TODO*/
            " , IFNULL(v.sCompnyNm, '') AS sInsComNm " + //86 /*TODO*/
            " , IFNULL(c.sTaxIDNox,'') AS sTaxIDNox " + //87
            //" , '' AS sJobNoxxx " + //88 /*TODO*/
            //" , GROUP_CONCAT(IFNULL(xx.sDSNoxxxx,'')) AS sDSNoxxxx   " + //88 /*TODO*/
            " , IFNULL(GROUP_CONCAT( DISTINCT xx.sDSNoxxxx),'') AS sDSNoxxxx   " + //88 
            //" , IFNULL(xx.sDSNoxxxx,'') AS sDSNoxxxx " + //88
            //" , (SELECT IFNULL(GROUP_CONCAT( DISTINCT sDSNoxxxx),'') FROM diagnostic_master WHERE diagnostic_master.sSourceCD = a.sTransNox AND diagnostic_master.cTranStat = '1') AS sDSNoxxxx " + //88 Added by Arsiela 12-28-2023 I added subquery because it conflict insert row like on NewRecord the condition WHERE 1=0 OKAY Arsiela 12-29-2023 USED GROUP BY instead.
            " , c.dBirthDte " + //89
            " , IFNULL(p.sEmailAdd, '') AS sEmailAdd " + //90 
            " , IFNULL(o.cOwnerxxx , '') AS cOwnerxxx " + //91 
            " , IFNULL(o.sMobileNo, '') AS sMobileNo " + //92 
            " , IFNULL(h.cOfficexx, '') AS cOfficexx " + //93
            " , CASE WHEN a.cTranStat = '0' THEN 'Y' ELSE 'N' END AS cTrStatus " + //94
            " , IFNULL(CONCAT( IFNULL(CONCAT(n.sAddressx,', ') , ''), " +  
            "   IFNULL(CONCAT(r.sTownName, ', '),''),  " +
            "   IFNULL(CONCAT(s.sProvName),'') ), '') AS sBrnchAdd " + //95
            " , IFNULL(a.sCoCltIDx,'') AS sCoCltIDx " + //96
            " , IFNULL(w.sCompnyNm,'') AS sCoBuyrNm    " + //97
            "   FROM vsp_master a " +   
            "   LEFT JOIN customer_inquiry b ON b.sTransNox = a.sInqryIDx " +    
            "   LEFT JOIN client_master c ON c.sClientID = a.sClientID  " +   																																			
            "   LEFT JOIN vehicle_serial d ON d.sSerialID = a.sSerialID  " +	   																																			
            "   LEFT JOIN vehicle_serial_registration e ON e.sSerialID = d.sSerialID   " +  
            "   LEFT JOIN vehicle_master f ON f.sVhclIDxx = d.sVhclIDxx   " +
            "   LEFT JOIN ggc_isysdbf.client_master g ON g.sClientID = b.sEmployID  " +
            "   LEFT JOIN client_address h ON h.sClientID = c.sClientID AND h.cPrimaryx = '1' " + //AND h.cRecdStat = '1' " +
            "   LEFT JOIN TownCity i on i.sTownIDxx = h.sTownIDxx " + //AND i.cRecdStat = '1'
            "   LEFT JOIN barangay j ON j.sBrgyIDxx = h.sBrgyIDxx and j.sTownIDxx = h.sTownIDxx " + // AND j.cRecdStat = '1'  " +
            "   LEFT JOIN Province k ON k.sProvIDxx = i.sProvIDxx " + // and k.cRecdStat = '1' " +
            "   left join udr_master l on l.sSourceCd = a.sTransNox and l.cTranStat = '1' " +
            "   LEFT JOIN online_platforms m on m.sTransNox = b.sSourceNo " +
            "   left join branch n on n.sBranchCd = b.sBranchCd " +
            "   LEFT JOIN client_mobile o on o.sClientID = c.sClientID AND o.cPrimaryx = '1' " + //AND o.cRecdStat = '1' " +
            "   LEFT JOIN client_email_address p on p.sClientID = c.sClientID AND p.cPrimaryx = '1' " + // AND p.cRecdStat = '1' " +
            "   LEFT JOIN client_master q ON q.sClientID = b.sClientID "  +
            "   LEFT JOIN TownCity r ON r.sTownIDxx = n.sTownIDxx    "  +
            "   LEFT JOIN Province s ON s.sProvIDxx = r.sProvIDxx "  +
            "   LEFT JOIN client_master t ON t.sClientID = b.sAgentIDx " +  /*TODO AGENT*/
            "   LEFT JOIN client_master u ON u.sClientID = a.sInsTplCd " +  /*TODO INSURANCE TPL*/
            "   LEFT JOIN client_master v ON v.sClientID = a.sInsCodex " +  /*TODO INSURANCE COMPRE*/ 
            "   LEFT JOIN client_master w ON W.sClientID = a.sCoCltIDx " +
            "   LEFT JOIN diagnostic_master xx ON xx.sSourceCD = a.sTransNox AND xx.cTranStat = '1'" ;
    }
    
    private String getSQ_Invoice(){
        return  " SELECT " +
                "  sTransNox, " +
                "  dTransact, " +
                "  sReferNox, " +
                "  sSourceNo, " +
                "  sSourceCd, " +
                "  sClientID, " +
                "  cTranStat " +
                "FROM si_master ";
    }
    
    private String getSQ_VSPFinance(){
        return " SELECT " +
            "  IFNULL(a.sTransNox, '') as sTransNox" + //1
            "  , a.cFinPromo" + //2
            "  , IFNULL(a.sBankIDxx, '') AS sBankIDxx" + //3
            "  , IFNULL(a.sBankname, '') AS sBankname" + //4
            "  , a.nFinAmtxx" + //5
            "  , a.nAcctTerm" + //6
            "  , a.nAcctRate" + //7
            "  , a.nRebatesx" + //8
            "  , a.nMonAmort" + //9
            "  , a.nPNValuex" + //10
            "  , a.nBnkPaidx" + //11
            "  , a.nGrsMonth" + //12
            "  , a.nNtDwnPmt" + //13
            "  , a.nDiscount" + //14
              /*dTimeStmp*/
            " FROM "+VSPFINANCE_TABLE+" a"  ;
    }
    
    /**
     * Load VSP Finance per VSP Transaction.
     * 
    */
    public boolean loadVSPFinance(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL = getSQ_VSPFinance();
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
            
            System.out.println(lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            loRS = poGRider.executeQuery(lsSQL);
            poVSPFinance = factory.createCachedRowSet();
            poVSPFinance.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    public void setVSPFinance(int fnIndex, Object foValue) throws SQLException{
        poVSPFinance.first();
        
        switch (fnIndex){ 
            case 1: // sTransNox
            case 3: // sBankIDxx
            case 4: // sBankname 
            case 2: // cFinPromo
                poVSPFinance.updateObject(fnIndex, (String) foValue);
                poVSPFinance.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 6: // nAcctTerm
                if (foValue instanceof Integer)
                    poVSPFinance.updateInt(fnIndex, (int) foValue);
                else 
                    poVSPFinance.updateInt(fnIndex, 0);
                
                poVSPFinance.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
                break;
            
            case 5: // nFinAmtxx 
            case 7: // nAcctRate 
            case 8: // nRebatesx
            case 9: // nMonAmort 
            case 10: // nPNValuex
            case 11: // nBnkPaidx
            case 12: // nGrsMonth
            case 13: // nNtDwnPmt
            case 14: // nDiscount    
                poVSPFinance.updateDouble(fnIndex, 0.00);
                if (StringUtil.isNumeric(String.valueOf(foValue))) {
                    poVSPFinance.updateObject(fnIndex, foValue);
                }
                
                poVSPFinance.updateRow();   
                break;
        }
    }
    
    public void setVSPFinance(String fsIndex, Object foValue) throws SQLException{
        setVSPFinance(MiscUtil.getColumnIndex(poVSPFinance, fsIndex), foValue);
    }
    
    public Object getVSPFinance(String fsIndex) throws SQLException{
        return getVSPFinance(MiscUtil.getColumnIndex(poVSPFinance, fsIndex));
    }
    
    public Object getVSPFinance(int fnIndex) throws SQLException{
        poVSPFinance.first();
        return poVSPFinance.getObject(fnIndex);
    }
    
    public int getVSPFinanceCount() throws SQLException{
        if (poVSPFinance != null){
            poVSPFinance.last();
            return poVSPFinance.getRow();
        }else{
            return 0;
        }
    }
    
    public int getVSPFinanceCountOrig() throws SQLException{
        if (poVSPFinanceOrig != null){
            poVSPFinanceOrig.last();
            return poVSPFinanceOrig.getRow();
        }else{
            return 0;
        }
    }
    
    public Object getVSPFinanceDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVSPFinance.absolute(fnRow);
        return poVSPFinance.getObject(fnIndex);
    }
    
    public Object getVSPFinanceDetail(int fnRow, String fsIndex) throws SQLException{
        return getVSPFinanceDetail(fnRow, MiscUtil.getColumnIndex(poVSPFinance, fsIndex));
    }
    
    /**
     * Add VSP Finance to the VSP Record.
     * 
    */
    public boolean AddVSPFinance(){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory;
                
            if (poVSPFinance == null) {
                lsSQL = MiscUtil.addCondition(getSQ_VSPFinance(), "0=1");
                loRS = poGRider.executeQuery(lsSQL);
                factory = RowSetProvider.newFactory();
                poVSPFinance = factory.createCachedRowSet();
                poVSPFinance.populate(loRS);
                MiscUtil.close(loRS);
            } 
                
            if (getVSPFinanceCount() == 0){
                poVSPFinance.last();
                poVSPFinance.moveToInsertRow();
                MiscUtil.initRowSet(poVSPFinance);
                poVSPFinance.insertRow();
                poVSPFinance.moveToCurrentRow();
            }
            
            setVSPFinance("sBankIDxx", "");
            setVSPFinance("sBankname", "");
            setVSPFinance("cFinPromo", "0");
            setVSPFinance("nFinAmtxx", 0.00); 
            setVSPFinance("nAcctTerm", 0); 
            setVSPFinance("nAcctRate", 0.00); 
            setVSPFinance("nRebatesx", 0.00); 
            setVSPFinance("nMonAmort", 0.00); 
            setVSPFinance("nPNValuex", 0.00); 
            setVSPFinance("nBnkPaidx", 0.00);  
            setVSPFinance("nGrsMonth", 0.00); 
            setVSPFinance("nNtDwnPmt", 0.00); 
            setVSPFinance("nDiscount", 0.00);
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    /**
     * Clears the VSP Finance from the data.
     * This method removes VSP Finance from the dataset.  
     * 
    */
    public boolean clearVSPFinance() throws SQLException {
        if (getVSPFinanceCount() > 0) {
            poVSPFinance.beforeFirst();
            while (poVSPFinance.next()) {
                poVSPFinance.deleteRow();
            }
        }
        return true;
    }
    
    /**
     * Compute amounts on VSP Record.
     * This method performs the computation of amount that has been input to the VSP Record.
     * 
    */
    public boolean computeAmount() throws SQLException{
        psMessage = "";
        
        System.out.println("master " + getItemCount());
        String lsPayModex = (String) getMaster("cPayModex");
        int lnCtr;
        String lsQty = ""; 
        BigDecimal ldblLaborAmt = new BigDecimal("0.00"); 
        BigDecimal ldblAccesAmt = new BigDecimal("0.00"); 
        BigDecimal ldblPartsAmt = new BigDecimal("0.00"); 
        /*Compute Labor Total*/
        for (lnCtr = 1; lnCtr <= getVSPLaborCount(); lnCtr++){
            //ldblLaborAmt = ldblLaborAmt + (Double) getVSPLaborDetail(lnCtr, "nLaborAmt");
            if(String.valueOf( getVSPLaborDetail(lnCtr, "nLaborAmt")) != null){
                ldblLaborAmt = ldblLaborAmt.add(new BigDecimal( String.valueOf( getVSPLaborDetail(lnCtr, "nLaborAmt")))).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }
        ldblLaborAmt = ldblLaborAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
        /*Compute Parts Total*/
        for (lnCtr = 1; lnCtr <= getVSPPartsCount(); lnCtr++){
            //ldblAccesAmt = ldblAccesAmt + ((Double) getVSPPartsDetail(lnCtr, "nSelPrice") * (Integer) getVSPPartsDetail(lnCtr, "nQuantity"));
            //ldblAccesAmt = ldblAccesAmt.add(new BigDecimal( String.valueOf( getVSPPartsDetail(lnCtr, "nSelPrice"))));
            //ldblAccesAmt = ldblAccesAmt.add(new BigDecimal( String.valueOf( getVSPPartsDetail(lnCtr, "sTotlAmtx")))); //nUnitPrce
            if(String.valueOf(getVSPPartsDetail(lnCtr, "nQuantity")) != null){
                lsQty = String.valueOf(getVSPPartsDetail(lnCtr, "nQuantity"));
            } else {
                lsQty = "0";
            }
            if(String.valueOf( getVSPPartsDetail(lnCtr, "nUnitPrce")) != null){
                ldblPartsAmt = new BigDecimal(lsQty).multiply(new BigDecimal( String.valueOf( getVSPPartsDetail(lnCtr, "nUnitPrce"))));
            }
            setVSPPartsDetail(lnCtr,"sTotlAmtx",String.valueOf(ldblPartsAmt));
            System.out.println(" ROW "+ lnCtr + " total amount >> " + getVSPPartsDetail(lnCtr, "sTotlAmtx"));
            ldblAccesAmt = ldblAccesAmt.add(ldblPartsAmt);
        }
        
        ldblAccesAmt = ldblAccesAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
        
        if (!computeTotlAmtPaid()){
            return false;
        }
        
        BigDecimal ldblTranTotl = new BigDecimal("0.00"); 
        BigDecimal ldblNetTTotl = new BigDecimal("0.00"); 
        BigDecimal ldblDiscntxx= new BigDecimal("0.00"); 
        
        //Amount to be Pay
        String lsUnitPrce = String.valueOf( getMaster("nUnitPrce"));   
        BigDecimal ldblUnitPrce =  new BigDecimal("0.00");
        if(lsUnitPrce != null && !lsUnitPrce.equals("null")){
            ldblUnitPrce = new BigDecimal(lsUnitPrce);
        }
        
        String lsTPLAmtxx = String.valueOf( getMaster("nTPLAmtxx"));
        BigDecimal ldblTPLAmtxx = new BigDecimal("0.00");
        if(lsTPLAmtxx != null && !lsTPLAmtxx.equals("null")){
            ldblTPLAmtxx = new BigDecimal(lsTPLAmtxx);
        }
        
        String lsCompAmtx = String.valueOf( getMaster("nCompAmtx"));
        BigDecimal ldblCompAmtx = new BigDecimal("0.00");
        if(lsCompAmtx != null && !lsCompAmtx.equals("null")){
            ldblCompAmtx = new BigDecimal(lsCompAmtx);
        }
        
        String lsLTOAmtxx = String.valueOf( getMaster("nLTOAmtxx")); 
        BigDecimal ldblLTOAmtxx = new BigDecimal("0.00");
        if(lsLTOAmtxx != null && !lsLTOAmtxx.equals("null")){
            ldblLTOAmtxx = new BigDecimal(lsLTOAmtxx);
        }
        
        String lsChmoAmtx = String.valueOf( getMaster("nChmoAmtx")); 
        BigDecimal ldblChmoAmtx = new BigDecimal("0.00");
        if(lsChmoAmtx != null && !lsChmoAmtx.equals("null")){
            ldblChmoAmtx = new BigDecimal(lsChmoAmtx);
        }
        
        String lsFrgtChrg = String.valueOf( getMaster("nFrgtChrg"));
        BigDecimal ldblFrgtChrg = new BigDecimal("0.00");
        if(lsFrgtChrg != null && !lsFrgtChrg.equals("null")){
            ldblFrgtChrg = new BigDecimal(lsFrgtChrg);
        }
        
        String lsOthrChrg = String.valueOf( getMaster("nOthrChrg")); 
        BigDecimal ldblOthrChrg = new BigDecimal("0.00");
        if(lsOthrChrg != null && !lsOthrChrg.equals("null")){
            ldblOthrChrg = new BigDecimal(lsOthrChrg);
        }
        
        String lsAdvDwPmt = String.valueOf( getMaster("nAdvDwPmt"));
        BigDecimal ldblAdvDwPmt = new BigDecimal("0.00");
        if(lsAdvDwPmt != null && !lsAdvDwPmt.equals("null")){
            ldblAdvDwPmt = new BigDecimal(lsAdvDwPmt);
        }
        //Discounted Amount                        
        String lsAddlDscx = String.valueOf( getMaster("nAddlDscx"));  
        BigDecimal ldblAddlDscx = new BigDecimal("0.00");
        if(lsAddlDscx != null && !lsAddlDscx.equals("null")){
            ldblAddlDscx = new BigDecimal(lsAddlDscx);
        }
        String lsPromoDsc = String.valueOf( getMaster("nPromoDsc"));
        BigDecimal ldblPromoDsc = new BigDecimal("0.00");
        if(lsPromoDsc != null && !lsPromoDsc.equals("null")){
            ldblPromoDsc = new BigDecimal(lsPromoDsc);
        }
        String lsFleetDsc = String.valueOf( getMaster("nFleetDsc"));
        BigDecimal ldblFleetDsc = new BigDecimal("0.00");
        if(lsFleetDsc != null && !lsFleetDsc.equals("null")){
            ldblFleetDsc = new BigDecimal(lsFleetDsc);
        }
        
        String lsSPFltDsc = String.valueOf( getMaster("nSPFltDsc"));
        BigDecimal ldblSPFltDsc = new BigDecimal("0.00");
        if(lsSPFltDsc != null && !lsSPFltDsc.equals("null")){
            ldblSPFltDsc = new BigDecimal(lsSPFltDsc);
        }
        
        String lsBndleDsc = String.valueOf( getMaster("nBndleDsc"));
        BigDecimal ldblBndleDsc = new BigDecimal("0.00");
        if(lsBndleDsc != null && !lsBndleDsc.equals("null")){
            ldblBndleDsc = new BigDecimal(lsBndleDsc);
        }
        
        //Paid Amount
        //double ldblDownPaym = (Double) getMaster("nDownPaym"); 
        BigDecimal ldblDownPaym = new BigDecimal("0.00"); 
        
        String lsResrvFee = String.valueOf( getMaster("nResrvFee")); 
        BigDecimal ldblResrvFee = new BigDecimal("0.00");
        if(lsResrvFee != null && !lsResrvFee.equals("null")){
            ldblResrvFee = new BigDecimal(lsResrvFee);
        }
        
        if (!lsPayModex.equals("0")){ 
            String lsDownPaym = String.valueOf( getMaster("nDownPaym")); 
            if(lsDownPaym != null && !lsDownPaym.equals("null")){
                ldblUnitPrce = new BigDecimal(lsDownPaym);
            }
            //ldblUnitPrce = new BigDecimal(String.valueOf( getMaster("nDownPaym")));
            ldblUnitPrce = ldblUnitPrce.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        
        //vsptotal = nUnitPrce + instpl + inscomp + lto  + chmo + freightchage + miscamt + omacmf + labtotal + partstotal //gross vsp tota;
        //ldblTranTotl = ldblUnitPrce + ldblTPLAmtxx + ldblCompAmtx + ldblLTOAmtxx + ldblChmoAmtx + ldblFrgtChrg + ldblOthrChrg + ldblAdvDwPmt + ldblLaborAmt + ldblAccesAmt;
        ldblTranTotl = ldblUnitPrce.add(ldblTPLAmtxx).add(ldblCompAmtx).add(ldblLTOAmtxx).add(ldblChmoAmtx).add(ldblFrgtChrg).add(ldblOthrChrg).add(ldblAdvDwPmt).add(ldblLaborAmt).add(ldblAccesAmt);
        ldblTranTotl = ldblTranTotl.setScale(2, BigDecimal.ROUND_HALF_UP);
//        
        //vsptotal = vsptotal - (cashdisc + promodisc + stdfleetdisc + splfleet disc + bundledisc)  //gross vsp total less discounts and other deductibles
        //ldblTranTotl = ldblTranTotl - (ldblAddlDscx + ldblPromoDsc + ldblFleetDsc + ldblSPFltDsc + ldblBndleDsc);
        ldblDiscntxx = ldblAddlDscx.add(ldblPromoDsc).add(ldblFleetDsc).add(ldblSPFltDsc).add(ldblBndleDsc);
        ldblDiscntxx = ldblDiscntxx.setScale(2, BigDecimal.ROUND_HALF_UP);
        
        ldblTranTotl = ldblTranTotl.subtract(ldblDiscntxx);
        ldblTranTotl = ldblTranTotl.setScale(2, BigDecimal.ROUND_HALF_UP);
        
        //Net Amount Due = vsp total -(rfee + dwntotal + otherpayment) 
        //To be continued no computation yet from receipt -jahn 09162023
        //ldblNetTTotl = ldblTranTotl - (ldblDownPaym + ldblResrvFee);
        ldblNetTTotl = ldblTranTotl.subtract(ldblDownPaym.add(ldblResrvFee));
        ldblNetTTotl = ldblNetTTotl.setScale(2, BigDecimal.ROUND_HALF_UP);
        //if (ldblTranTotl < 0.00){
        if (ldblTranTotl.compareTo(new BigDecimal("0.00")) < 0){
            psMessage = "Invalid Gross Amount Total: " + ldblTranTotl + " . ";
            return false;
        }
        
        //if (ldblNetTTotl < 0.00){
        if (ldblNetTTotl.compareTo(new BigDecimal("0.00")) < 0){
            psMessage = "Invalid Net Amount Due: " + ldblNetTTotl + " . ";
            return false;
        }
        
        setMaster("nTranTotl",ldblTranTotl);
        setMaster("nNetTTotl",ldblNetTTotl);
        setMaster("nLaborAmt",ldblLaborAmt);
        setMaster("nAccesAmt",ldblAccesAmt);
        
        //PO / FINANCING
        if (!lsPayModex.equals("0")){ 
            if (getVSPFinanceCount() > 0){
                BigDecimal ldblFinAmt = new BigDecimal("0.00"); 
                BigDecimal ldblRatexx = new BigDecimal("0.00"); 
                BigDecimal ldblMonAmort = new BigDecimal("0.00"); 
                BigDecimal ldblGrsMonth = new BigDecimal("0.00"); 
                BigDecimal ldblPNValuex = new BigDecimal("0.00"); 
                
                String lsDiscount = String.valueOf( getVSPFinance("nDiscount")); 
                BigDecimal ldblDiscount = new BigDecimal("0.00");
                if(lsDiscount != null && !lsDiscount.equals("null")){
                    ldblDiscount = new BigDecimal(lsDiscount);
                }
                
                
                String lsNtDwnPmt = String.valueOf( getVSPFinance("nNtDwnPmt")); 
                BigDecimal ldblNtDwnPmt = new BigDecimal("0.00");
                if(lsNtDwnPmt != null && !lsNtDwnPmt.equals("null")){
                    ldblNtDwnPmt = new BigDecimal(lsNtDwnPmt);
                }
                
                String lsRebatesx = String.valueOf( getVSPFinance("nRebatesx")); 
                BigDecimal ldblRebatesx = new BigDecimal("0.00");
                if(lsRebatesx != null && !lsRebatesx.equals("null")){
                    ldblRebatesx = new BigDecimal(lsRebatesx);
                }
                
                String lsAcctRate = String.valueOf( getVSPFinance("nAcctRate")); 
                BigDecimal ldblAcctRate = new BigDecimal("0.00");
                if(lsAcctRate != null && !lsAcctRate.equals("null")){
                    ldblAcctRate = new BigDecimal(lsAcctRate);
                }
                
                
                int lnAcctTerm = (Integer) getVSPFinance("nAcctTerm");
                
                lsUnitPrce = String.valueOf( getMaster("nUnitPrce"));
                if(lsUnitPrce != null && !lsUnitPrce.equals("null")){
                    ldblUnitPrce = new BigDecimal(lsUnitPrce);
                }
                ldblUnitPrce = ldblUnitPrce.setScale(2, BigDecimal.ROUND_HALF_UP);
                
                //-Amount Financed = nUnitPrce -(nDiscount + nNtDwnPmt)
                //ldblFinAmt = ldblUnitPrce - (ldblDiscount + ldblNtDwnPmt); 
                ldblFinAmt = ldblUnitPrce.subtract(ldblDiscount.add(ldblNtDwnPmt));
                ldblFinAmt = ldblFinAmt.setScale(2, BigDecimal.ROUND_HALF_UP); 
                if (ldblFinAmt.compareTo(new BigDecimal("0.00")) < 0){
                    psMessage = "Invalid Amount Finance : " + ldblFinAmt + " . ";
                    return false;
                }
                //-Rate = (nAcctRate/100) + 1
                //ldblRatexx = (ldblAcctRate / 100) + 1; 
                //ldblRatexx = (ldblAcctRate.divide(100)).add(new BigDecimal("1"));
                if (ldblAcctRate.compareTo(BigDecimal.ZERO) != 0) {
                    ldblRatexx = (ldblAcctRate.divide(new BigDecimal("100")));
                    ldblRatexx = ldblRatexx.setScale(2, BigDecimal.ROUND_HALF_UP); 
                    ldblRatexx = ldblRatexx.add(new BigDecimal("1"));
                    ldblRatexx = ldblRatexx.setScale(2, BigDecimal.ROUND_HALF_UP); 
                }
                
                //System.out.println("ldblRatexx " + ldblRatexx);
                //System.out.println("ldblFinAmt " + ldblFinAmt);
                //-net Monthly Inst = (Amount Financed * Rate)/Terms Rate
                //ldblMonAmort = (ldblFinAmt * ldblRatexx) / lnAcctTerm; 
                
                if (lnAcctTerm > 0) {
                    ldblMonAmort = (ldblFinAmt.multiply(ldblRatexx));
                    ldblMonAmort = ldblMonAmort.setScale(2, BigDecimal.ROUND_HALF_UP); 
                    //System.out.println("ldblMonAmort >>> " + ldblMonAmort);
                    //ldblMonAmort = ldblMonAmort.divide(new BigDecimal(String.valueOf(lnAcctTerm)), 2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal bgTerm = new BigDecimal(String.valueOf(lnAcctTerm));
                    ldblMonAmort = ldblMonAmort.divide(bgTerm, RoundingMode.HALF_UP);
                    ldblMonAmort = ldblMonAmort.setScale(2, BigDecimal.ROUND_HALF_UP); 
                    //System.out.println("lnAcctTerm " + lnAcctTerm);
                    //System.out.println("ldblMonAmort " + ldblMonAmort);
                }
                //-Gross Monthly Inst = Net Monthly Inst + Prompt Payment Disc
                //ldblGrsMonth = ldblMonAmort + ldblRebatesx; 
                ldblGrsMonth = ldblMonAmort.add(ldblRebatesx); 
                ldblGrsMonth = ldblGrsMonth.setScale(2, BigDecimal.ROUND_HALF_UP); 
                
                if (ldblGrsMonth.compareTo(new BigDecimal("0.00")) < 0){
                    psMessage = "Invalid Gross Monthly Installment: " + ldblGrsMonth + " . ";
                    return false;
                }
                //-Promisory Note Amount =Terms Rate * Gross Monthly Inst
                //ldblPNValuex = lnAcctTerm * ldblGrsMonth; 
                ldblPNValuex = ldblGrsMonth.multiply(new BigDecimal(String.valueOf(lnAcctTerm))); 
                ldblPNValuex = ldblPNValuex.setScale(2, BigDecimal.ROUND_HALF_UP); 
                if (ldblPNValuex.compareTo(new BigDecimal("0.00")) < 0){
                    psMessage = "Invalid Promissory Note Amount: " + ldblPNValuex + " . ";
                    return false;
                }
                
//                System.out.println("ldblRebatesx " + ldblRebatesx);
//                System.out.println("ldblMonAmort " + ldblMonAmort);
//                System.out.println("ldblGrsMonth " + ldblGrsMonth);
//                System.out.println("lnAcctTerm " + lnAcctTerm);
//                System.out.println("ldblPNValuex " + ldblPNValuex);
                
                setVSPFinance("nFinAmtxx",ldblFinAmt);
                setVSPFinance("nMonAmort",ldblMonAmort);
                setVSPFinance("nGrsMonth",ldblGrsMonth);
                setVSPFinance("nPNValuex",ldblPNValuex);
            }
        }
        
//        System.out.println("nTranTotl >>> " + String.valueOf( getMaster("nTranTotl")) ); //Gross Amount
//        System.out.println("nNetTTotl >>> " + String.valueOf(getMaster("nNetTTotl")) ); //Net Amount Due
        
        return true;
    }
    
//    /**
//     * Compute amounts on VSP Record.
//     * This method performs the computation of amount that has been input to the VSP Record.
//     * 
//    */
//    public boolean computeAmount() throws SQLException{
//        psMessage = "";
//        //value.setScale(2, BigDecimal.ROUND_HALF_UP)
//        String lsPayModex = (String) getMaster("cPayModex");
//        int lnCtr;
//        BigDecimal ldblLaborAmt = new BigDecimal("0.00"); 
//        BigDecimal ldblAccesAmt = new BigDecimal("0.00"); 
//        /*Compute Labor Total*/
//        for (lnCtr = 1; lnCtr <= getVSPLaborCount(); lnCtr++){
//            //ldblLaborAmt = ldblLaborAmt + (Double) getVSPLaborDetail(lnCtr, "nLaborAmt");
//            ldblLaborAmt = ldblLaborAmt.add(new BigDecimal( String.valueOf( getVSPLaborDetail(lnCtr, "nLaborAmt")))).setScale(2, BigDecimal.ROUND_HALF_UP);
//        }
//        ldblLaborAmt = ldblLaborAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
//        /*Compute Parts Total*/
//        for (lnCtr = 1; lnCtr <= getVSPPartsCount(); lnCtr++){
//            //ldblAccesAmt = ldblAccesAmt + ((Double) getVSPPartsDetail(lnCtr, "nSelPrice") * (Integer) getVSPPartsDetail(lnCtr, "nQuantity"));
//            ldblAccesAmt = ldblAccesAmt.add(new BigDecimal( String.valueOf( getVSPPartsDetail(lnCtr, "nSelPrice"))));
//        }
//        ldblAccesAmt = ldblAccesAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
//        
//        if (!computeTotlAmtPaid()){
//            return false;
//        }
//        
//        BigDecimal ldblTranTotl = new BigDecimal("0.00"); 
//        BigDecimal ldblNetTTotl = new BigDecimal("0.00"); 
//        BigDecimal ldblDiscntxx= new BigDecimal("0.00"); 
//        
//        //Amount to be Pay
//        BigDecimal ldblUnitPrce = new BigDecimal(String.valueOf( getMaster("nUnitPrce")));
//        BigDecimal ldblTPLAmtxx = new BigDecimal(String.valueOf( getMaster("nTPLAmtxx")));
//        BigDecimal ldblCompAmtx = new BigDecimal(String.valueOf( getMaster("nCompAmtx"))); 
//        BigDecimal ldblLTOAmtxx = new BigDecimal(String.valueOf( getMaster("nLTOAmtxx")));
//        BigDecimal ldblChmoAmtx = new BigDecimal(String.valueOf( getMaster("nChmoAmtx")));
//        BigDecimal ldblFrgtChrg = new BigDecimal(String.valueOf( getMaster("nFrgtChrg")));
//        BigDecimal ldblOthrChrg = new BigDecimal(String.valueOf( getMaster("nOthrChrg"))); 
//        BigDecimal ldblAdvDwPmt = new BigDecimal(String.valueOf( getMaster("nAdvDwPmt")));
//        //Discounted Amount
//        BigDecimal ldblAddlDscx = new BigDecimal(String.valueOf( getMaster("nAddlDscx")));
//        BigDecimal ldblPromoDsc = new BigDecimal(String.valueOf( getMaster("nPromoDsc"))); 
//        BigDecimal ldblFleetDsc = new BigDecimal(String.valueOf( getMaster("nFleetDsc")));
//        BigDecimal ldblSPFltDsc = new BigDecimal(String.valueOf( getMaster("nSPFltDsc")));
//        BigDecimal ldblBndleDsc = new BigDecimal(String.valueOf( getMaster("nBndleDsc")));
//        //Paid Amount
//        //double ldblDownPaym = (Double) getMaster("nDownPaym"); 
//        BigDecimal ldblDownPaym = new BigDecimal("0.00"); 
//        BigDecimal ldblResrvFee = new BigDecimal(String.valueOf( getMaster("nResrvFee")));
//        
//        if (!lsPayModex.equals("0")){ 
//            ldblUnitPrce = new BigDecimal(String.valueOf( getMaster("nDownPaym")));
//            ldblUnitPrce = ldblUnitPrce.setScale(2, BigDecimal.ROUND_HALF_UP);
//        }
//        
//        //vsptotal = nUnitPrce + instpl + inscomp + lto  + chmo + freightchage + miscamt + omacmf + labtotal + partstotal //gross vsp tota;
//        //ldblTranTotl = ldblUnitPrce + ldblTPLAmtxx + ldblCompAmtx + ldblLTOAmtxx + ldblChmoAmtx + ldblFrgtChrg + ldblOthrChrg + ldblAdvDwPmt + ldblLaborAmt + ldblAccesAmt;
//        ldblTranTotl = ldblUnitPrce.add(ldblTPLAmtxx).add(ldblCompAmtx).add(ldblLTOAmtxx).add(ldblChmoAmtx).add(ldblFrgtChrg).add(ldblOthrChrg).add(ldblAdvDwPmt).add(ldblLaborAmt).add(ldblAccesAmt);
//        ldblTranTotl = ldblTranTotl.setScale(2, BigDecimal.ROUND_HALF_UP);
////        ldblTranTotl = ldblUnitPrce.add(ldblTPLAmtxx);
////        ldblTranTotl = ldblTranTotl.add(ldblCompAmtx);
////        ldblTranTotl = ldblTranTotl.add(ldblLTOAmtxx);
////        ldblTranTotl = ldblTranTotl.add(ldblChmoAmtx);
////        ldblTranTotl = ldblTranTotl.add(ldblFrgtChrg);
////        ldblTranTotl = ldblTranTotl.add(ldblOthrChrg);
////        ldblTranTotl = ldblTranTotl.add(ldblAdvDwPmt);
////        ldblTranTotl = ldblTranTotl.add(ldblLaborAmt);
////        ldblTranTotl = ldblTranTotl.add(ldblAccesAmt);
////        
//        //vsptotal = vsptotal - (cashdisc + promodisc + stdfleetdisc + splfleet disc + bundledisc)  //gross vsp total less discounts and other deductibles
//        //ldblTranTotl = ldblTranTotl - (ldblAddlDscx + ldblPromoDsc + ldblFleetDsc + ldblSPFltDsc + ldblBndleDsc);
//        ldblDiscntxx = ldblAddlDscx.add(ldblPromoDsc).add(ldblFleetDsc).add(ldblSPFltDsc).add(ldblBndleDsc);
//        ldblDiscntxx = ldblDiscntxx.setScale(2, BigDecimal.ROUND_HALF_UP);
////        ldblDiscntxx = ldblAddlDscx.add(ldblPromoDsc);
////        ldblDiscntxx = ldblDiscntxx.add(ldblFleetDsc);
////        ldblDiscntxx = ldblDiscntxx.add(ldblSPFltDsc);
////        ldblDiscntxx = ldblDiscntxx.add(ldblBndleDsc);
//        
//        ldblTranTotl = ldblTranTotl.subtract(ldblDiscntxx);
//        ldblTranTotl = ldblTranTotl.setScale(2, BigDecimal.ROUND_HALF_UP);
//        
//        //Net Amount Due = vsp total -(rfee + dwntotal + otherpayment) 
//        //To be continued no computation yet from receipt -jahn 09162023
//        //ldblNetTTotl = ldblTranTotl - (ldblDownPaym + ldblResrvFee);
//        ldblNetTTotl = ldblTranTotl.subtract(ldblDownPaym.add(ldblResrvFee));
//        ldblNetTTotl = ldblNetTTotl.setScale(2, BigDecimal.ROUND_HALF_UP);
//        //if (ldblTranTotl < 0.00){
//        if (ldblTranTotl.compareTo(new BigDecimal("0.00")) < 0){
//            psMessage = "Invalid Gross Amount Total.";
//            return false;
//        }
//        
//        //if (ldblNetTTotl < 0.00){
//        if (ldblNetTTotl.compareTo(new BigDecimal("0.00")) < 0){
//            psMessage = "Invalid Net Amount Due.";
//            return false;
//        }
//        
//        setMaster("nTranTotl",ldblTranTotl);
//        setMaster("nNetTTotl",ldblNetTTotl);
//        setMaster("nLaborAmt",ldblLaborAmt);
//        setMaster("nAccesAmt",ldblAccesAmt);
//        
//        //PO / FINANCING
//        if (!lsPayModex.equals("0")){ 
//            if (getVSPFinanceCount() > 0){
//                BigDecimal ldblFinAmt = new BigDecimal("0.00"); 
//                BigDecimal ldblRatexx = new BigDecimal("0.00"); 
//                BigDecimal ldblMonAmort = new BigDecimal("0.00"); 
//                BigDecimal ldblGrsMonth = new BigDecimal("0.00"); 
//                BigDecimal ldblPNValuex = new BigDecimal("0.00"); 
//                BigDecimal ldblDiscount = new BigDecimal(String.valueOf( getVSPFinance("nDiscount")));
//                BigDecimal ldblNtDwnPmt = new BigDecimal(String.valueOf( getVSPFinance("nNtDwnPmt")));
//                BigDecimal ldblRebatesx = new BigDecimal(String.valueOf( getVSPFinance("nRebatesx")));
//                BigDecimal ldblAcctRate = new BigDecimal(String.valueOf( getVSPFinance("nAcctRate")));
//                int lnAcctTerm = (Integer) getVSPFinance("nAcctTerm");
//                
//                ldblUnitPrce = new BigDecimal(String.valueOf( getMaster("nUnitPrce")));
//                ldblUnitPrce = ldblUnitPrce.setScale(2, BigDecimal.ROUND_HALF_UP);
//                
//                //-Amount Financed = nUnitPrce -(nDiscount + nNtDwnPmt)
//                //ldblFinAmt = ldblUnitPrce - (ldblDiscount + ldblNtDwnPmt); 
//                ldblFinAmt = ldblUnitPrce.subtract(ldblDiscount.add(ldblNtDwnPmt));
//                ldblFinAmt = ldblFinAmt.setScale(2, BigDecimal.ROUND_HALF_UP); 
//                if (ldblFinAmt.compareTo(new BigDecimal("0.00")) < 0){
//                    psMessage = "Invalid Amount Finance.";
//                    return false;
//                }
//                //-Rate = (nAcctRate/100) + 1
//                //ldblRatexx = (ldblAcctRate / 100) + 1; 
//                //ldblRatexx = (ldblAcctRate.divide(100)).add(new BigDecimal("1"));
//                if (ldblAcctRate.compareTo(BigDecimal.ZERO) != 0) {
//                    ldblRatexx = (ldblAcctRate.divide(new BigDecimal("100")));
//                    ldblRatexx = ldblRatexx.setScale(2, BigDecimal.ROUND_HALF_UP); 
//                    ldblRatexx = ldblRatexx.add(new BigDecimal("1"));
//                    ldblRatexx = ldblRatexx.setScale(2, BigDecimal.ROUND_HALF_UP); 
//                }
//                
//                System.out.println("ldblRatexx " + ldblRatexx);
//                System.out.println("ldblFinAmt " + ldblFinAmt);
//                //-net Monthly Inst = (Amount Financed * Rate)/Terms Rate
//                //ldblMonAmort = (ldblFinAmt * ldblRatexx) / lnAcctTerm; 
//                
//                if (lnAcctTerm > 0) {
//                    ldblMonAmort = (ldblFinAmt.multiply(ldblRatexx));
//                    ldblMonAmort = ldblMonAmort.setScale(2, BigDecimal.ROUND_HALF_UP); 
//                    System.out.println("ldblMonAmort >>> " + ldblMonAmort);
//                    //ldblMonAmort = ldblMonAmort.divide(new BigDecimal(String.valueOf(lnAcctTerm)), 2, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal bgTerm = new BigDecimal(String.valueOf(lnAcctTerm));
//                    ldblMonAmort = ldblMonAmort.divide(bgTerm, RoundingMode.HALF_UP);
//                    ldblMonAmort = ldblMonAmort.setScale(2, BigDecimal.ROUND_HALF_UP); 
//                    System.out.println("lnAcctTerm " + lnAcctTerm);
//                    System.out.println("ldblMonAmort " + ldblMonAmort);
//                }
//                //-Gross Monthly Inst = Net Monthly Inst + Prompt Payment Disc
//                //ldblGrsMonth = ldblMonAmort + ldblRebatesx; 
//                ldblGrsMonth = ldblMonAmort.add(ldblRebatesx); 
//                ldblGrsMonth = ldblGrsMonth.setScale(2, BigDecimal.ROUND_HALF_UP); 
//                
//                if (ldblGrsMonth.compareTo(new BigDecimal("0.00")) < 0){
//                    psMessage = "Invalid Gross Monthly Installment.";
//                    return false;
//                }
//                //-Promisory Note Amount =Terms Rate * Gross Monthly Inst
//                //ldblPNValuex = lnAcctTerm * ldblGrsMonth; 
//                ldblPNValuex = ldblGrsMonth.multiply(new BigDecimal(String.valueOf(lnAcctTerm))); 
//                ldblPNValuex = ldblPNValuex.setScale(2, BigDecimal.ROUND_HALF_UP); 
//                if (ldblPNValuex.compareTo(new BigDecimal("0.00")) < 0){
//                    psMessage = "Invalid Promissory Note Amount.";
//                    return false;
//                }
//                
//                System.out.println("ldblRebatesx " + ldblRebatesx);
//                System.out.println("ldblMonAmort " + ldblMonAmort);
//                System.out.println("ldblGrsMonth " + ldblGrsMonth);
//                System.out.println("lnAcctTerm " + lnAcctTerm);
//                System.out.println("ldblPNValuex " + ldblPNValuex);
//                
//                setVSPFinance("nFinAmtxx",ldblFinAmt);
//                setVSPFinance("nMonAmort",ldblMonAmort);
//                setVSPFinance("nGrsMonth",ldblGrsMonth);
//                setVSPFinance("nPNValuex",ldblPNValuex);
//            }
//        }
//        
//        System.out.println("nTranTotl >>> " + String.valueOf( getMaster("nTranTotl")) ); //Gross Amount
//        System.out.println("nNetTTotl >>> " + String.valueOf(getMaster("nNetTTotl")) ); //Net Amount Due
//        
//        return true;
//    }
    
//    /**
//     * Compute amounts on VSP Record.
//     * This method performs the computation of amount that has been input to the VSP Record.
//     * 
//    */
//    public boolean computeAmount() throws SQLException{
//        psMessage = "";
//        String lsPayModex = (String) getMaster("cPayModex");
//        int lnCtr;
//        double ldblLaborAmt = 0.00;
//        double ldblAccesAmt = 0.00;
//        /*Compute Labor Total*/
//        for (lnCtr = 1; lnCtr <= getVSPLaborCount(); lnCtr++){
//            ldblLaborAmt = ldblLaborAmt + (Double) getVSPLaborDetail(lnCtr, "nLaborAmt");
//        }
//        /*Compute Parts Total*/
//        for (lnCtr = 1; lnCtr <= getVSPPartsCount(); lnCtr++){
//            ldblAccesAmt = ldblAccesAmt + ((Double) getVSPPartsDetail(lnCtr, "nSelPrice") * (Integer) getVSPPartsDetail(lnCtr, "nQuantity"));
//        }
//        
//        if (!computeTotlAmtPaid()){
//            return false;
//        }
//        
//        double ldblTranTotl = 0.00;
//        double ldblNetTTotl = 0.00;
//        //Amount to be Pay
//        double ldblUnitPrce = (Double) getMaster("nUnitPrce");
//        double ldblTPLAmtxx = (Double) getMaster("nTPLAmtxx");
//        double ldblCompAmtx = (Double) getMaster("nCompAmtx"); 
//        double ldblLTOAmtxx = (Double) getMaster("nLTOAmtxx");
//        double ldblChmoAmtx = (Double) getMaster("nChmoAmtx");
//        double ldblFrgtChrg = (Double) getMaster("nFrgtChrg");
//        double ldblOthrChrg = (Double) getMaster("nOthrChrg"); 
//        double ldblAdvDwPmt = (Double) getMaster("nAdvDwPmt");
//        //Discounted Amount
//        double ldblAddlDscx = (Double) getMaster("nAddlDscx");
//        double ldblPromoDsc = (Double) getMaster("nPromoDsc"); 
//        double ldblFleetDsc = (Double) getMaster("nFleetDsc");
//        double ldblSPFltDsc = (Double) getMaster("nSPFltDsc");
//        double ldblBndleDsc = (Double) getMaster("nBndleDsc");
//        //Paid Amount
//        //double ldblDownPaym = (Double) getMaster("nDownPaym"); 
//        double ldblDownPaym = 0.00; 
//        double ldblResrvFee = (Double) getMaster("nResrvFee");
//        
//        if (!lsPayModex.equals("0")){ 
//            ldblUnitPrce = (Double) getMaster("nDownPaym");
//        }
//        
//        //vsptotal = nUnitPrce + instpl + inscomp + lto  + chmo + freightchage + miscamt + omacmf + labtotal + partstotal //gross vsp tota;
//        ldblTranTotl = ldblUnitPrce + ldblTPLAmtxx + ldblCompAmtx + ldblLTOAmtxx + ldblChmoAmtx + ldblFrgtChrg + ldblOthrChrg + ldblAdvDwPmt + ldblLaborAmt + ldblAccesAmt;
//        //vsptotal = vsptotal - (cashdisc + promodisc + stdfleetdisc + splfleet disc + bundledisc)  //gross vsp total less discounts and other deductibles
//        ldblTranTotl = ldblTranTotl - (ldblAddlDscx + ldblPromoDsc + ldblFleetDsc + ldblSPFltDsc + ldblBndleDsc);
//        
//        //Net Amount Due = vsp total -(rfee + dwntotal + otherpayment) 
//        //To be continued no computation yet from receipt -jahn 09162023
//        ldblNetTTotl = ldblTranTotl - (ldblDownPaym + ldblResrvFee);
//        
//        if (ldblTranTotl < 0.00){
//            psMessage = "Invalid Gross Amount Total.";
//            return false;
//        }
//        
//        if (ldblNetTTotl < 0.00){
//            psMessage = "Invalid Net Amount Due.";
//            return false;
//        }
//        
//        setMaster("nTranTotl",ldblTranTotl);
//        setMaster("nNetTTotl",ldblNetTTotl);
//        setMaster("nLaborAmt",ldblLaborAmt);
//        setMaster("nAccesAmt",ldblAccesAmt);
//        
//        //PO / FINANCING
//        if (!lsPayModex.equals("0")){ 
//            if (getVSPFinanceCount() > 0){
//                double ldblFinAmt = 0.00;
//                double ldblRatexx = 0.00;
//                double ldblMonAmort = 0.00;
//                double ldblGrsMonth = 0.00;
//                double ldblPNValuex = 0.00;
//                double ldblDiscount = (Double) getVSPFinance("nDiscount");
//                double ldblNtDwnPmt = (Double) getVSPFinance("nNtDwnPmt");
//                double ldblRebatesx = (Double) getVSPFinance("nRebatesx");
//                double ldblAcctRate = (Double) getVSPFinance("nAcctRate");
//                int lnAcctTerm = (Integer) getVSPFinance("nAcctTerm");
//                ldblUnitPrce = (Double) getMaster("nUnitPrce");
//                //-Amount Financed = nUnitPrce -(nDiscount + nNtDwnPmt)
//                ldblFinAmt = ldblUnitPrce - (ldblDiscount + ldblNtDwnPmt); 
//                //-Rate = (nAcctRate/100) + 1
//                ldblRatexx = (ldblAcctRate / 100) + 1; 
//                //-net Monthly Inst = (Amount Financed * Rate)/Terms Rate
//                ldblMonAmort = (ldblFinAmt * ldblRatexx) / lnAcctTerm; 
//                //-Gross Monthly Inst = Net Monthly Inst + Prompt Payment Disc
//                ldblGrsMonth = ldblMonAmort + ldblRebatesx; 
//                //-Promisory Note Amount =Terms Rate * Gross Monthly Inst
//                ldblPNValuex = lnAcctTerm * ldblGrsMonth; 
//
//                setVSPFinance("nFinAmtxx",ldblFinAmt);
//                setVSPFinance("nMonAmort",ldblMonAmort);
//                setVSPFinance("nGrsMonth",ldblGrsMonth);
//                setVSPFinance("nPNValuex",ldblPNValuex);
//            }
//        }
//        
//        System.out.println("nTranTotl >>> " + (Double) getMaster("nTranTotl") ); //Gross Amount
//        System.out.println("nNetTTotl >>> " + (Double) getMaster("nNetTTotl") ); //Net Amount Due
//        
//        return true;
//    }
    
    private boolean computeTotlAmtPaid(){
        psMessage = "";
        return true;
    }
    
    private String getSQ_VSPPayment(){
        return "";
    
    }
    
    private String getSQ_InqPayment(){
        return "";
    
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
                " , IFNULL(GROUP_CONCAT( DISTINCT c.sDSNoxxxx), '') AS sDSNoxxxx " + //11
                " FROM "+VSPLABOR_TABLE+" a "+
                " LEFT JOIN diagnostic_labor b ON b.sLaborCde = a.sLaborCde " +
                " LEFT JOIN diagnostic_master c ON c.sTransNox = b.sTransNox and c.sSourceCD = a.sTransNox AND c.cTranStat = '1' " ;
    }
    
    /**
     * Load VSP Labor per VSP Transaction.
     * 
    */
    public boolean loadVSPLabor(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL = getSQ_VSPLabor();
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")))
                                                     + " GROUP BY a.sLaborCde " ;
            
            System.out.println(lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            loRS = poGRider.executeQuery(lsSQL);
            poVSPLabor = factory.createCachedRowSet();
            poVSPLabor.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public Object getVSPLabor(String fsIndex) throws SQLException{
        return getVSPLabor(MiscUtil.getColumnIndex(poVSPLabor, fsIndex));
    }
    
    public Object getVSPLabor(int fnIndex) throws SQLException{
        poVSPLabor.first();
        return poVSPLabor.getObject(fnIndex);
    }
    
    private int getOrigVSPLaborCount() throws SQLException{
        if (poVSPLaborOrig != null){
            poVSPLaborOrig.last();
            return poVSPLaborOrig.getRow();
        }else{
            return 0;
        }
    }
    
    private Object getOrigVSPLaborDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVSPLaborOrig.absolute(fnRow);
        return poVSPLaborOrig.getObject(fnIndex);
    }
    
    private Object getOrigVSPLaborDetail(int fnRow, String fsIndex) throws SQLException{
        return getOrigVSPLaborDetail(fnRow, MiscUtil.getColumnIndex(poVSPLaborOrig, fsIndex));
    }
    
    /**
     * Add VSP Labor to the VSP Record.
     * 
     * @param fsValue the employee ID of the member
     * @param withLaborDesc Determines whether to automatically load the labor data based on the labor description (true) or basically add row only (false).
     * 
    */
    public boolean addVSPLabor(String fsValue, boolean withLaborDesc){
        try {
            System.out.println("labor parts count >>>> " + getVSPPartsCount());
            System.out.println("labor labor count >>>> " + getVSPLaborCount());
            System.out.println("labor master count >>>> " + getItemCount());
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory;
            psMessage = "";
            if (withLaborDesc) {
                if (!checkLaborExist(fsValue,0)){
                    return false;
                } 
            }
            
            if (poVSPLabor == null) {
                lsSQL = MiscUtil.addCondition(getSQ_VSPLabor(), "0=1") + " GROUP BY a.sLaborCde ";
                loRS = poGRider.executeQuery(lsSQL);
                factory = RowSetProvider.newFactory();
                poVSPLabor = factory.createCachedRowSet();
                poVSPLabor.populate(loRS);
                MiscUtil.close(loRS);
            }
            poVSPLabor.last();
            poVSPLabor.moveToInsertRow();
            MiscUtil.initRowSet(poVSPLabor);
            
            String lsIsAddl = "0";
            if (!withLaborDesc){
                lsIsAddl = "1";
            }
            
            poVSPLabor.updateString("cAddtlxxx", lsIsAddl);
            poVSPLabor.updateObject("dAddDatex", (Date) poGRider.getServerDate());
            poVSPLabor.updateString("sAddByxxx", poGRider.getUserID());
            poVSPLabor.updateString("sDSNoxxxx", "");
            poVSPLabor.insertRow();
            poVSPLabor.moveToCurrentRow();
            
//            setVSPLaborDetail(getVSPLaborCount(), "cAddtlxxx", lsIsAddl);
//            setVSPLaborDetail(getVSPLaborCount(), "dAddDatex", (Date) poGRider.getServerDate());
//            setVSPLaborDetail(getVSPLaborCount(), "sAddByxxx", poGRider.getUserID());
//            setVSPLaborDetail(getVSPLaborCount(), "sDSNoxxxx", "");
            
            if (withLaborDesc){
                searchLabor(fsValue,getVSPLaborCount(),false);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private boolean checkLaborExist(String fsValue, int fnRow) throws SQLException{
        String lsDscExist = "";
        String lsValue = "";
        psMessage = "";
        
        if (fsValue.isEmpty()){ return false;}
        
        lsValue = fsValue.replace(" ", "").trim(); 
        
        for (int lnRow = 1; lnRow <= getVSPLaborCount(); lnRow++){
            lsDscExist = (String) getVSPLaborDetail(lnRow,"sLaborDsc");
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
     * Remove VSP Labor to the VSP Record.
     * @param fnRow specifies row index to be removed.
    */
    public boolean removeVSPLabor(Integer fnRow) throws SQLException{
        if (getVSPLaborCount()== 0) {
            psMessage = "No VSP Labor to delete.";
            return false;
        }
        
        if (!((String) getVSPLaborDetail(fnRow, "sDSNoxxxx")).isEmpty()){
            psMessage = "You cannot remove labor that already linked to Job Order.";
            return false;
        }
        
        poVSPLabor.absolute(fnRow);
        String lsFind = poVSPLabor.getString("sTransNox");
        if (lsFind != null && !lsFind.isEmpty()) {
            String lsLaborCde = poVSPLabor.getString("sLaborCde");
            
            if (!lsLaborCde.isEmpty()){
                for (int lnCtr = 1; lnCtr <= getOrigVSPLaborCount(); lnCtr++){
                    if (lsLaborCde.equals((String) getOrigVSPLaborDetail(lnCtr,"sLaborCde"))){
                        fnRow = lnCtr;
                        break;
                    }
                }
                
            }
            deletedLaborRows.add(fnRow);
        }
        poVSPLabor.deleteRow();
        System.out.println("success");
        
        return true;
    }
    /**
     * Clears the VSP Labor from the data.
     * This method removes VSP Labor from the dataset.  
     * 
    */
    private boolean clearVSPLabor() throws SQLException {
        if (getVSPLaborCount() > 0) {
            poVSPLabor.beforeFirst();
            while (poVSPLabor.next()) {
                poVSPLabor.deleteRow();
            }
        }
        return true;
    }
    
    public void setVSPLaborDetail(int fnRow, int fnIndex, Object foValue) throws SQLException{
        
        poVSPLabor.absolute(fnRow);        
        switch (fnIndex){       
            case 1://sTransNox            
            case 3://sLaborCde 
            case 5://sChrgeTyp
            case 6://sRemarksx
            case 7://sLaborDsc
            case 8://cAddtlxxx
            case 10://sAddByxxx
            case 11://sDSNoxxxx
                poVSPLabor.updateObject(fnIndex, (String) foValue);
                poVSPLabor.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getVSPLabor(fnIndex));
                break;
                       
            case 2://nEntryNox
                if (foValue instanceof Integer)
                    poVSPLabor.updateInt(fnIndex, (int) foValue);
                else 
                    poVSPLabor.updateInt(fnIndex, 0);
                
                poVSPLabor.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getVSPLabor(fnIndex));  
                break;
            
            case 4://nLaborAmt
                poVSPLabor.updateDouble(fnIndex, 0.00);
                if (StringUtil.isNumeric(String.valueOf(foValue))) {
                    poVSPLabor.updateObject(fnIndex, foValue);
                }
                
                poVSPLabor.updateRow();   
                break;
            case 9://dAddDatex
                if (foValue instanceof Date){
                    poVSPLabor.updateObject(fnIndex, foValue);
                } else {
                    poVSPLabor.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poVSPLabor.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getVSPLabor(fnIndex));
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
    public void setVSPLaborDetail(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setVSPLaborDetail(fnRow, MiscUtil.getColumnIndex(poVSPLabor, fsIndex), foValue);
    }
    
    private String getSQ_Labor(){
        return " SELECT " +
               " IFNULL(a.sLaborCde, '' ) AS sLaborCde" +
               " , IFNULL(a.sLaborDsc, '' ) AS sLaborDsc" +
               //" , IFNULL(TRIM(REPLACE(sLaborDsc, ' ', '')), '' ) AS sLaborDsc " +
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
     * @param withUI determines if the search is performed by display manual selection on jason search (True) or automatically set labor based on query result (False).
     * //@param byCode determines if the search is performed by code or not for updating correct labor description.
    */
    public boolean searchLabor(String fsValue, int fnRow, boolean withUI) throws SQLException{
        
        String lsSQL = getSQ_Labor();
        psMessage = "";
        fsValue = fsValue.replace(" ", "").trim();
        
        ResultSet loRS;
        JSONObject loJSON = null;   
        if (!withUI) { 
            lsSQL = lsSQL + " WHERE TRIM(REPLACE(a.sLaborDsc, ' ', '')) LIKE " + SQLUtil.toSQL( fsValue + "%") +
                        " AND a.cRecdStat = '1'  "  ;
        
            System.out.println(lsSQL);
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setVSPLaborDetail(fnRow,"sLaborCde", loRS.getString("sLaborCde"));
                setVSPLaborDetail(fnRow,"sLaborDsc", loRS.getString("sLaborDsc"));
                //setVSPLaborDetail(fnRow,"cAddtlxxx", "0");
                
            } else {
                psMessage = "No record found.";
                setVSPLaborDetail(fnRow,"sLaborCde", "");
                setVSPLaborDetail(fnRow,"sLaborDsc", "");
                //setVSPLaborDetail(fnRow,"cAddtlxxx", "");
                return false;
            }           
        } else {
            lsSQL = lsSQL + " WHERE a.cRecdStat = '1'  "  ;
        
            System.out.println(lsSQL);
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             //fsValue,
                                             "Labor ID»Labor Description", 
                                             "sLaborCde»sLaborDsc",
                                             "a.sLaborCde»a.sLaborDsc",
                                             1);
            
            if (loJSON != null){
                if (!checkLaborExist((String) loJSON.get("sLaborDsc"),fnRow)){ 
                    setVSPLaborDetail(fnRow,"sLaborCde", "");
                    setVSPLaborDetail(fnRow,"sLaborDsc", "");
                    //setVSPLaborDetail(fnRow,"cAddtlxxx", "");
                    return false;
                }
                
                setVSPLaborDetail(fnRow,"sLaborCde", (String) loJSON.get("sLaborCde"));
                setVSPLaborDetail(fnRow,"sLaborDsc", (String) loJSON.get("sLaborDsc"));
                setVSPLaborDetail(fnRow,"cAddtlxxx", "1");
            } else {
                psMessage = "No record found/selected.";
                setVSPLaborDetail(fnRow,"sLaborCde", "");
                setVSPLaborDetail(fnRow,"sLaborDsc", "");
                //setVSPLaborDetail(fnRow,"cAddtlxxx", "");
                return false;    
            }
        } 
        return true;
    }
    
    private String getSQ_VSPParts(){
        return " SELECT  " +
                " IFNULL(a.sTransNox, '') AS sTransNox" + //1
                "  , a.nEntryNox" + //2
                "  , IFNULL(a.sStockIDx, '') AS sStockIDx" + //3
                "  , a.nUnitPrce" + //4
                "  , a.nSelPrice" + //5
                "  , a.nQuantity" + //6
                "  , a.nReleased" + //7
                "  , IFNULL(a.sChrgeTyp, '') AS sChrgeTyp" + //8
                "  , IFNULL(a.sDescript, '') AS sDescript" + //9 Sales Parts Description
                "  , IFNULL(a.sPartStat, '') AS sPartStat" + //10
                //"  , IFNULL(a.cAddtlxxx, '') AS cAddtlxxx" + //11
                //"  , '' AS sJobNoxxx" + //11
                //"  , GROUP_CONCAT(DISTINCT IFNULL(d.sDSNoxxxx, '')) AS sDSNoxxxx" + //11
                "  , IFNULL(GROUP_CONCAT(DISTINCT d.sDSNoxxxx), '') AS sDSNoxxxx" + //11
                "  , a.dAddDatex" + //12
                "  , IFNULL(a.sAddByxxx, '') AS sAddByxxx" + //13
                "  , IFNULL(b.sBarCodex, '') AS sBarCodex" + //14 
                "  , IFNULL(a.nQuantity * a.nUnitPrce, '') AS sTotlAmtx " + //15
                "  , SUM(c.nQtyEstmt) AS sQtyEstmt " + //16 
                "  , IFNULL(b.sDescript, '') AS sPartDesc " + //17 Parts Description
                //"  , IFNULL(c.nQtyEstmt, '') AS sQtyEstmt " + //16 
                
                //  /*dTImeStmp*/
                " FROM "+VSPPARTS_TABLE+" a " +
                " LEFT JOIN inventory b ON b.sStockIDx = a.sStockIDx " +
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
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")))
                                                    + " GROUP BY a.sStockIDx " ;
            
            System.out.println(lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            loRS = poGRider.executeQuery(lsSQL);
            poVSPParts = factory.createCachedRowSet();
            poVSPParts.populate(loRS);
            MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public Object getVSPParts(String fsIndex) throws SQLException{
        return getVSPParts(MiscUtil.getColumnIndex(poVSPParts, fsIndex));
    }
    
    public Object getVSPParts(int fnIndex) throws SQLException{
        poVSPParts.first();
        return poVSPParts.getObject(fnIndex);
    }
    
    private int getOrigVSPPartsCount() throws SQLException{
        if (poVSPPartsOrig != null){
            poVSPPartsOrig.last();
            return poVSPPartsOrig.getRow();
        }else{
            return 0;
        }
    }
    
    private Object getOrigVSPPartsDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVSPPartsOrig.absolute(fnRow);
        return poVSPPartsOrig.getObject(fnIndex);
    }
    
    private Object getOrigVSPPartsDetail(int fnRow, String fsIndex) throws SQLException{
        return getOrigVSPPartsDetail(fnRow, MiscUtil.getColumnIndex(poVSPPartsOrig, fsIndex));
    }
    
    /**
     * Add VSP Parts to the VSP Record.
     * 
     * @return true
    */
    public boolean AddVSPParts(){
        try {
            System.out.println("parts parts count >>>> " + getVSPPartsCount());
            System.out.println("parts labor count >>>> " + getVSPLaborCount());
            System.out.println("parts master count >>>> " + getItemCount());
            
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory;
            
            if (poVSPParts == null) {
                lsSQL = MiscUtil.addCondition(getSQ_VSPParts(), "0=1") + " GROUP BY a.sStockIDx ";
                loRS = poGRider.executeQuery(lsSQL);
                factory = RowSetProvider.newFactory();
                poVSPParts = factory.createCachedRowSet();
                poVSPParts.populate(loRS);
                MiscUtil.close(loRS);
            }
            poVSPParts.last();
            poVSPParts.moveToInsertRow();
            MiscUtil.initRowSet(poVSPParts);
            poVSPParts.updateInt("nQuantity", 0);
            poVSPParts.updateObject("nUnitPrce", 0.00);
            poVSPParts.updateObject("nSelPrice", 0.00);
            poVSPParts.updateObject("dAddDatex", (Date) poGRider.getServerDate());
            poVSPParts.updateString("sAddByxxx", poGRider.getUserID());
            poVSPParts.updateString("sDSNoxxxx", "");
            
            poVSPParts.insertRow();
            poVSPParts.moveToCurrentRow();
//            
//            setVSPPartsDetail(getVSPPartsCount(), "nQuantity", 0);
//            setVSPPartsDetail(getVSPPartsCount(), "nUnitPrce", 0.00);
//            setVSPPartsDetail(getVSPPartsCount(), "nSelPrice", 0.00);
//            setVSPPartsDetail(getVSPPartsCount(), "dAddDatex", (Date) poGRider.getServerDate());
//            setVSPPartsDetail(getVSPPartsCount(), "sAddByxxx", poGRider.getUserID());
//            setVSPPartsDetail(getVSPPartsCount(), "sDSNoxxxx", "");
//            setVSPPartsDetail(getVSPPartsCount(), "sBarCodex", "");
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    /**
     * Check VSP Parts Quantity linked to JO
     * @param fsValue parts Stock ID
     * @param fnInputQty parts quantity to be input
    */
    public boolean checkVSPJOParts(String fsValue, int fnInputQty) throws SQLException{
        String lsSQL = getSQ_VSPParts();
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL(poMaster.getString("sTransNox")) +
                                              " AND a.sStockIDx = " + SQLUtil.toSQL(fsValue) ) 
                                            + " AND d.cTranStat = '1' " 
                                            + " GROUP BY a.sStockIDx " ;
        
//        TEST
//        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL("V00123000001") +
//                                                 " AND a.sStockIDx = " + SQLUtil.toSQL(fsValue) );
                                              
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            int nTotalQty = 0;
            while(loRS.next()){
                if(!loRS.getString("sQtyEstmt").isEmpty()){
                    nTotalQty = nTotalQty + Integer.valueOf(loRS.getString("sQtyEstmt")) ;
                }
            }
            if (fnInputQty < nTotalQty){
                psMessage = "Quantity input to JO Parts cannot be less than the VSP Parts Quantity.";
                return false;
            }
        }
        MiscUtil.close(loRS);        
        return true;
    }
    
    /**
     * Remove VSP Parts to the VSP Record.
     * @param fnRow specifies row index to be removed.
    */
    public boolean removeVSPParts(Integer fnRow) throws SQLException{
        if (getVSPPartsCount()== 0) {
            psMessage = "No VSP Parts to delete.";
            return false;
        }
        
        if (!((String) getVSPPartsDetail(fnRow, "sDSNoxxxx")).isEmpty()){
            psMessage = "You cannot remove parts that already linked to Job Order.";
            return false;
        }
        
        poVSPParts.absolute(fnRow);
        String lsFind = poVSPParts.getString("sTransNox");
        if (lsFind != null && !lsFind.isEmpty()) {
            String lsPartsCde = poVSPParts.getString("sDescript");
            
            if (!lsPartsCde.isEmpty()){
                for (int lnCtr = 1; lnCtr <= getOrigVSPPartsCount(); lnCtr++){
                    if (lsPartsCde.equals((String) getOrigVSPPartsDetail(lnCtr,"sDescript"))){
                        fnRow = lnCtr;
                        break;
                    }
                }
            }
            
            deletedPartsRows.add(fnRow);
        }
        poVSPParts.deleteRow();
        System.out.println("success");
        
        return true;
    }
    
    /**
     * Validate setting of Parts Description do not allow if parts description already exist on cacherowset.
     * @param fsValue the Parts description
     * @param fbisAdd the state identifies when user trigger add or update parts
     * @return
     * @throws SQLException 
     */
    public boolean checkPartsExist(String fsValue, boolean fbisAdd) throws SQLException{
        int nCnt = 0;
        if (fsValue.trim().isEmpty()){
            psMessage = "Parts Description cannot be Empty. Add parts aborted.";
            return false;
        }
        if (getVSPPartsCount() > 0 ){
            for (int lnCtr = 1; lnCtr <= getVSPPartsCount(); lnCtr++){
                if (((String) getVSPPartsDetail(lnCtr,"sDescript")).equals(fsValue)){
                    nCnt++;
                }
                
                psMessage = "Parts " +fsValue+ " already exist. Add parts aborted.";
                if (fbisAdd){
                    if (nCnt == 1){
                        return false;
                    }
                } else {
                    if (nCnt > 1){
                        return false;
                    }
                }
            }
        }
        psMessage = "";
        return true;
    }
    /**
     * Clears the VSP Parts from the data.
     * This method removes VSP Parts from the dataset.  
     * 
    */
    private boolean clearVSPParts() throws SQLException {
        if (getVSPPartsCount() > 0) {
            poVSPParts.beforeFirst();
            while (poVSPParts.next()) {
                poVSPParts.deleteRow();
            }
        }
        return true;
    }
    
    public void setVSPPartsDetail(int fnRow, int fnIndex, Object foValue) throws SQLException{
        
        poVSPParts.absolute(fnRow);        
        switch (fnIndex){   
            case 9://sDescript
//                if (!checkPartsExist((String) foValue,fnRow)){
//                    poVSPParts.updateObject(fnIndex, "");
//                } else {
//                    poVSPParts.updateObject(fnIndex, (String) foValue);
//                }
//                poVSPParts.updateRow();
//                if (poCallback != null) poCallback.onSuccess(fnIndex, getVSPParts(fnIndex));
//                break;
            case 1://sTransNox            
            case 3://sStockIDx 
            case 8://sChrgeTyp
            case 10://sPartStat
            case 11://sDSNoxxxx
            case 13://sAddByxxx
            case 14://sBarCodex
            case 15://sTotlAmtx
            case 16://sQtyEstmt
            case 17://sPartDesc
                poVSPParts.updateObject(fnIndex, (String) foValue);
                poVSPParts.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getVSPParts(fnIndex));
                break;
                       
            case 2://nEntryNox
            case 6://nQuantity
            case 7://nReleased
                if (foValue instanceof Integer)
                    poVSPParts.updateInt(fnIndex, (int) foValue);
                else 
                    poVSPParts.updateInt(fnIndex, 0);
                
                poVSPParts.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getVSPParts(fnIndex));  
                break;
            
            case 4://nUnitPrce
            case 5://nSelPrice
                poVSPParts.updateDouble(fnIndex, 0.00);
                if (StringUtil.isNumeric(String.valueOf(foValue))) {
                    poVSPParts.updateObject(fnIndex, foValue);
                }
                
                poVSPParts.updateRow();   
                break;
            case 12://dAddDatex
                if (foValue instanceof Date){
                    poVSPParts.updateObject(fnIndex, foValue);
                } else {
                    poVSPParts.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poVSPParts.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getVSPParts(fnIndex));
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
    public void setVSPPartsDetail(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setVSPPartsDetail(fnRow, MiscUtil.getColumnIndex(poVSPParts, fsIndex), foValue);
    }
    
    private String getSQ_Inventory(){
        return " SELECT "
                    + " a.sStockIDx " //1
                    + " ,a.sBarCodex "//2
                    + " ,a.sDescript "//3
                    + " , IFNULL(a.sBriefDsc, '') sBriefDsc "//4  
                    + " FROM inventory a ";		
    }
    
    public boolean searchInventory(String fsValue, int fnRow) throws SQLException{
        String lsSQL = getSQ_Inventory() + " WHERE a.sBarCodex LIKE "  + SQLUtil.toSQL(fsValue + "%") ;
        
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null; 
        if (pbWithUI) {   
//            lsSQL += " LIMIT 1";
//            loRS = poGRider.executeQuery(lsSQL);
//            
//            if (loRS.next()){
//                setVSPPartsDetail(fnRow,"sBarCodex", loRS.getString("sBarCodex"));
//            } else {
//                psMessage = "No record found.";
//                setVSPPartsDetail(fnRow,"sBarCodex", "");
//                return false;
//            }           
//        } else {
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue,
                                             "Part Number»Part Description", 
                                             "sBarCodex»sDescript",
                                             "a.sBarCodex»a.sDescript",
                                             0);
            
            if (loJSON != null){
                setVSPPartsDetail(fnRow,"sBarCodex", (String) loJSON.get("sBarCodex"));
            } else {
                psMessage = "No record found/selected.";
                setVSPPartsDetail(fnRow,"sBarCodex", "");
                return false;    
            }
        }
        return true;
    }
    
    private String getSQ_Inquiry(){
        return "SELECT " +
                    " IFNULL(a.sTransNox,'') as sTransNox"  + 
                    ", IFNULL(a.sBranchCD,'') as sBranchCD" + 
                    ", a.dTransact" + 
                    ", IFNULL(a.sEmployID,'') as sEmployID" + 
                    ", a.cIsVhclNw" + 
                    ", IFNULL(a.sVhclIDxx,'') as sVhclIDxx" + 
                    ", IFNULL(a.sClientID,'') as sClientID" + 
                    ", IFNULL(a.sAgentIDx,'') as sAgentIDx" + 
                    ", a.dTargetDt" + 
                    ", IFNULL(a.sSourceCD,'') as sSourceCD" + 
                    ", IFNULL(a.sSourceNo,'') as sSourceNo" + 
                    ",IFNULL( a.sActvtyID,'') as sActvtyID" + 
                    ", a.nReserved" + 
                    ", a.nRsrvTotl" + 
                    ", IFNULL(a.sSerialID,'') as sSerialID" + 
                    ", IFNULL(a.sInqryCde,'') as sInqryCde" + 
                    ", a.cTranStat" + 
                    ", IFNULL(b.sCompnyNm,'') as sCompnyNm" +
                    ", IFNULL(g.sMobileNo, '') AS sMobileNo " + 
                    ", IFNULL(k.sAccountx, '') AS sAccountx " + 
                    ", IFNULL(h.sEmailAdd, '') AS sEmailAdd " + 
                    ", IFNULL(CONCAT( IFNULL(CONCAT(c.sAddressx,', ') , ''), " +    
                    "  IFNULL(CONCAT(e.sBrgyName,', '), ''),   " + 
                    "  IFNULL(CONCAT(d.sTownName, ', '),''),   " + 
                    "  IFNULL(f.sProvName,'') )	, '') AS sAddressx " + 
                    " ,IFNULL(j.sCompnyNm, '') AS sSalesExe  " +
                    //" ,IFNULL((SELECT sCompnyNm FROM client_master WHERE sClientID = a.sAgentIDx), '') AS sSalesAgn" +
                    " ,IFNULL(n.sCompnyNm, '') AS sSalesAgn" +
                    " ,IFNULL(m.sPlatform, '') AS sPlatform " +
                    " ,IFNULL(l.sActTitle, '') as sActTitle " +
                    " ,IFNULL(a.cPayModex, '') as cPayModex " +
                    " ,IFNULL(i.sBranchNm,'') AS sBranchNm " +
                    " ,CASE " +
                    "    WHEN a.cPayModex = '0' THEN 'CASH' " +
                    "    WHEN a.cPayModex = '1' THEN 'BANK PURCHASE ORDER' " +
                    "    WHEN a.cPayModex = '2' THEN 'BANK FINANCING' " +
                    "    WHEN a.cPayModex = '3' THEN 'COMPANY PURCHASE ORDER' " +
                    "    WHEN a.cPayModex = '4' THEN 'COMPANY FINANCING' " +
                    "    ELSE '' " +
                    " END AS sPaymentM  " +
                    "FROM customer_inquiry a " +
                    "LEFT JOIN client_master b ON b.sClientID = a.sClientID " +
                    "LEFT JOIN client_address c ON c.sClientID = a.sClientID AND c.cPrimaryx = '1' " + //AND c.cRecdStat = '1'  
                    "LEFT JOIN TownCity d ON d.sTownIDxx = c.sTownIDxx " + //AND d.cRecdStat = '1'  
                    "LEFT JOIN barangay e ON e.sBrgyIDxx = c.sBrgyIDxx AND e.sTownIDxx = c.sTownIDxx " + // AND e.cRecdStat = '1'   
                    "LEFT JOIN Province f ON f.sProvIDxx = d.sProvIDxx  " + //AND f.cRecdStat = '1' 
                    "LEFT JOIN client_mobile g ON g.sClientID = c.sClientID AND g.cPrimaryx = '1' " + //AND g.cRecdStat = '1'  
                    "LEFT JOIN client_email_address h ON h.sClientID = c.sClientID AND h.cPrimaryx = '1' " + // AND h.cRecdStat = '1'
                    "LEFT JOIN branch i ON i.sBranchCd = a.sBranchCd   " +
                    "LEFT JOIN ggc_isysdbf.client_master j ON j.sClientID = a.sEmployID " +
                    "LEFT JOIN client_social_media k ON k.sClientID = a.sClientID " + //AND k.cRecdStat = '1'    " +
                    "LEFT JOIN activity_master l ON l.sActvtyID  AND l.cTranStat = '1' " +
                    "LEFT JOIN online_platforms m ON m.sTransNox = a.sSourceNo  " +
                    "LEFT JOIN client_master n ON n.sClientID = a.sAgentIDx " ;
    }
    
    /**
     * Search for On Process Inquiry.
     * This method performs a search for a inquiry by inquiring customer. It allows both UI and non-UI search modes and provides feedback if no records are found.
     * @param fsValue The Inquiring customer name
     * @param fbyCode determines if the search is performed by inquiry code or Inquiring customer name.
    */
    public boolean searchInquiry (String fsValue, boolean fbyCode) throws SQLException{
        String lsSQL = getSQ_Inquiry();
        
        if (fbyCode){
            lsSQL = lsSQL + " WHERE a.sTransNox = " + SQLUtil.toSQL(fsValue) +
                        " AND a.cTranStat = '1' AND (a.cPayModex <> NULL OR a.cPayModex <> '')"  +
                        " GROUP BY a.sTransNox " ;
        } else {
//            lsSQL = lsSQL + " WHERE b.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%") +
//                        " AND a.cTranStat = '1' AND (a.cPayModex <> NULL OR a.cPayModex <> '')"  +
//                        " GROUP BY a.sTransNox " ; // AND (a.cPayModex <> NULL OR a.cPayModex <> '') 
            lsSQL = lsSQL + " WHERE a.cTranStat = '1' AND (a.cPayModex <> NULL OR a.cPayModex <> '')"  +
                        " GROUP BY a.sTransNox " ; // AND (a.cPayModex <> NULL OR a.cPayModex <> '')  
        }
        
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null; 
        if (fbyCode) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                setMaster("sAddressx", loRS.getString("sAddressx"));
                setMaster("sClientID", loRS.getString("sClientID"));
                setMaster("cIsVhclNw", loRS.getString("cIsVhclNw"));
                setMaster("sInqryIDx", loRS.getString("sTransNox"));
                setMaster("sBranchCD", loRS.getString("sBranchCD"));
                setMaster("sSalesExe", loRS.getString("sSalesExe"));
                setMaster("sSalesAgn", loRS.getString("sSalesAgn"));
                setMaster("sInqClntx", loRS.getString("sCompnyNm"));
                setMaster("cPayModex", loRS.getString("cPayModex"));
                setMaster("dInqDatex", loRS.getString("dTransact"));
                setMaster("sInqTypex", loRS.getString("sSourceCD"));
                setMaster("sBranchNm", loRS.getString("sBranchNm"));
            } else {
                psMessage = "No record found.";
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sClientID", "");
                setMaster("cIsVhclNw", "");
                setMaster("sInqryIDx", "");
                setMaster("sBranchCD", "");
                setMaster("sSalesExe", "");
                setMaster("sSalesAgn", "");
                setMaster("sInqClntx", "");
                setMaster("cPayModex", "");
                setMaster("dInqDatex", "");
                setMaster("sInqTypex", "");
                setMaster("sBranchNm", "");
                return false;
            }           
        } else {
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             //"",
                                             "%" + fsValue +"%",
                                             "Customer ID»Customer Name»Address»Payment Mode»", 
                                             "sClientID»sCompnyNm»sAddressx»sPaymentM",
                                             "a.sClientID»b.sCompnyNm",
                                             1);
            
            if (loJSON != null){
                //System.out.println("Inquiry Type Master: " + (String) loJSON.get("sSourceCD"));
                //System.out.println("Inquiry Paymode: " + (String) loJSON.get("cPayModex"));
                
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sAddressx", (String) loJSON.get("sAddressx"));
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("cIsVhclNw", (String) loJSON.get("cIsVhclNw"));
                setMaster("sInqryIDx", (String) loJSON.get("sTransNox"));
                setMaster("sBranchCD", (String) loJSON.get("sBranchCD"));
                setMaster("sSalesExe", (String) loJSON.get("sSalesExe"));
                setMaster("sSalesAgn", (String) loJSON.get("sSalesAgn"));
                setMaster("sInqClntx", (String) loJSON.get("sCompnyNm"));
                setMaster("cPayModex", (String) loJSON.get("cPayModex"));
                setMaster("dInqDatex", (String) loJSON.get("dTransact"));
                setMaster("sInqTypex", (String) loJSON.get("sSourceCD"));
                setMaster("sBranchNm", (String) loJSON.get("sBranchNm"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sClientID", "");
                setMaster("cIsVhclNw", "");
                setMaster("sInqryIDx", "");
                setMaster("sBranchCD", "");
                setMaster("sSalesExe", "");
                setMaster("sSalesAgn", "");
                setMaster("sInqClntx", "");
                setMaster("cPayModex", "");
                setMaster("dInqDatex", "");
                setMaster("sInqTypex", "");
                setMaster("sBranchNm", "");
                return false;    
            }
        }
        
        //System.out.println("Inquiry Paymode Master: " + (String) getMaster("cPayModex"));
        if (!((String) getMaster("cPayModex")).equals("0")){
            if (AddVSPFinance()){
            } else {
                psMessage = "Error in adding VSP Finance.";
                return false;
            }
        } else {
            if (!clearVSPFinance()){
                psMessage = "Error clear fields for VSP Finance.";
                return false;
            }
        }
        
        return true;
    }
    
    private String getSQ_BuyingCustomer(){
        return "SELECT" +
                "  IFNULL(a.sClientID, '') as sClientID" + 
                ", IFNULL(a.sLastName, '') as sLastName" + 
                ", IFNULL(a.sFrstName, '') as sFrstName" +
                ", IFNULL(a.sMiddName, '') as sMiddName" + 
                ", IFNULL(a.sCompnyNm, '') as sCompnyNm" +
                ", IFNULL(a.sClientNo, '') as sClientNo" + 
                ", a.cClientTp" + 
                ", a.cRecdStat" + 
                ", IFNULL(CONCAT( IFNULL(CONCAT(b.sAddressx,', ') , ''), " +    
                "  IFNULL(CONCAT(d.sBrgyName,', '), ''),   " + 
                "  IFNULL(CONCAT(c.sTownName, ', '),''),   " + 
                "  IFNULL(e.sProvName,'') )	, '') AS sAddressx " + 
                " FROM client_master a" +
                " LEFT JOIN client_address b ON b.sClientID = a.sClientID AND b.cPrimaryx = '1' " + //AND c.cRecdStat = '1'  
                " LEFT JOIN TownCity c ON c.sTownIDxx = b.sTownIDxx " + //AND d.cRecdStat = '1'  
                " LEFT JOIN barangay d ON d.sBrgyIDxx = b.sBrgyIDxx AND d.sTownIDxx = b.sTownIDxx " + // AND e.cRecdStat = '1'   
                " LEFT JOIN Province e ON e.sProvIDxx = c.sProvIDxx  " ; //AND f.cRecdStat = '1' 

    }
    
    /**
     * Search for Buying Customer.
     * @param fsValue The Buying customer name
     * 
    */
    public boolean searchBuyingCustomer (String fsValue, boolean fbisBuyingCust) throws SQLException{
        String lsSQL = getSQ_BuyingCustomer();
        
//        lsSQL = lsSQL + " WHERE a.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%") +
//                        " AND a.cRecdStat = '1'  "  ;
        lsSQL = lsSQL + " WHERE a.cRecdStat = '1'  "  ;
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                if (fbisBuyingCust){
                    setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                    setMaster("sAddressx", loRS.getString("sAddressx"));
                    setMaster("sClientID", loRS.getString("sClientID"));
                } else {
                    setMaster("sCoBuyrNm", loRS.getString("sCompnyNm"));
                    setMaster("sCoCltIDx", loRS.getString("sClientID"));
                }
                
            } else {
                if (fbisBuyingCust){
                    setMaster("sCompnyNm","");
                    setMaster("sAddressx","");
                    setMaster("sClientID","");
                } else {
                    setMaster("sCoBuyrNm","");
                    setMaster("sCoCltIDx","");
                }
                psMessage = "No record found.";
                return false;
            }           
        } else {
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Customer ID»Customer Name", 
                                             "sClientID»sCompnyNm",
                                             "a.sClientID»a.sCompnyNm",
                                             1);
            
            if (loJSON != null){
                if (fbisBuyingCust){
                    setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                    setMaster("sAddressx", (String) loJSON.get("sAddressx"));
                    setMaster("sClientID", (String) loJSON.get("sClientID"));
                } else {
                    setMaster("sCoBuyrNm", (String) loJSON.get("sCompnyNm"));
                    setMaster("sCoCltIDx", (String) loJSON.get("sClientID"));
                }
                
            } else {
                if (fbisBuyingCust){
                    setMaster("sCompnyNm","");
                    setMaster("sAddressx","");
                    setMaster("sClientID","");
                } else {
                    setMaster("sCoBuyrNm","");
                    setMaster("sCoCltIDx","");
                }
                psMessage = "No record found/selected.";
                return false;    
            }
            
            if (fbisBuyingCust){
                if(!((String) getMaster("sSerialID")).isEmpty()){
                    if(!searchAvailableVhcl((String) getMaster("sSerialID"), true)){
                        setMaster("sCompnyNm", "");
                        setMaster("sAddressx", "");
                        setMaster("sClientID", "");
                        return false;
                    }
                }
            }
        } 
        
        return true;
    }
    
    private String getSQ_AvailableVhcl(){
        return  "SELECT" + 
                "   IFNULL(a.sSerialID,'') sSerialID " + 
                " , IFNULL(a.sFrameNox,'') sFrameNox " + 
                " , IFNULL(a.sEngineNo,'') sEngineNo " + 
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx " + 
                " , IFNULL(a.sClientID,'') sClientID " + 
                " , IFNULL(a.sCoCltIDx,'') sCoCltIDx " + 
                " , IFNULL(a.sCSNoxxxx,'') sCSNoxxxx " + 
                " , IFNULL(a.cVhclNewx,'') cVhclNewx " +
                " , IFNULL(a.cSoldStat,'') cSoldStat " +  
                " , IFNULL(b.sPlateNox,'') sPlateNox " + 
                " , IFNULL(c.sMakeIDxx,'') sMakeIDxx " + 
                " , IFNULL(d.sMakeDesc,'') sMakeDesc " + 
                " , IFNULL(c.sModelIDx,'') sModelIDx " + 
                " , IFNULL(e.sModelDsc,'') sModelDsc " +    
                " , IFNULL(c.sTypeIDxx,'') sTypeIDxx " + 
                " , IFNULL(f.sTypeDesc,'') sTypeDesc " +  
                " , IFNULL(c.sColorIDx,'') sColorIDx " + 
                " , IFNULL(g.sColorDsc,'') sColorDsc " + 
                " , IFNULL(c.sTransMsn,'') sTransMsn " + 
                " , IFNULL(c.nYearModl,'') nYearModl " + 
                " , IFNULL(c.sDescript,'') sDescript " + 
                " , IFNULL(a.sKeyNoxxx,'') sKeyNoxxx " + 
                "   FROM vehicle_serial a " + 
                "   LEFT JOIN vehicle_serial_registration b ON a.sSerialID = b.sSerialID  " +
                "   LEFT JOIN vehicle_master c ON c.sVhclIDxx = a.sVhclIDxx  " +
                "   LEFT JOIN vehicle_make d ON d.sMakeIDxx = c.sMakeIDxx  " +
                "   LEFT JOIN vehicle_model e ON e.sModelIDx = c.sModelIDx  " +
                "   LEFT JOIN vehicle_type f ON f.sTypeIDxx = c.sTypeIDxx  " +
                "   LEFT JOIN vehicle_color g ON g.sColorIDx = c.sColorIDx  " ;
    }
    
    /**
     * Search for available vehicle.
     * @param fsValue The vehicle plate number or cs number
     * @return {@code true} if a matching available vehicle is found, {@code false} otherwise.
    */
    public boolean searchAvailableVhcl(String fsValue, boolean fbisByCode) throws SQLException{
        /*-(to add in the future should unit should have a delivery instruction before being able to add)*/
        String lsSQL = getSQ_AvailableVhcl();
        if(fbisByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, " a.sSerialID = " + SQLUtil.toSQL(fsValue) ); 
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, " a.cSoldStat <> '0' " //AND (ISNULL(a.sClientID) OR  TRIM(a.sClientID) = '' )" 
                + " AND a.cVhclNewx = " + SQLUtil.toSQL((String) getMaster("cIsVhclNw")) 
                + " AND (a.sCSNoxxxx LIKE " + SQLUtil.toSQL(fsValue + "%") 
                + "     OR b.sPlateNox LIKE " + SQLUtil.toSQL(fsValue + "%") + " ) " ); 
        }
        
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        String sCSPlateNo = "";
        String sOwnerID = "";
        String sSoldStat = "";
        String sSerialID = "";
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                sCSPlateNo = loRS.getString("sPlateNox");
                if(sCSPlateNo.isEmpty()){
                    sCSPlateNo = loRS.getString("sCSNoxxxx");
                }
                sOwnerID = loRS.getString("sClientID");
                sSoldStat = loRS.getString("cSoldStat");
                sSerialID = loRS.getString("sSerialID");
                
                setMaster("sSerialID", loRS.getString("sSerialID"));
                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
                setMaster("sPlateNox", loRS.getString("sPlateNox"));
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sFrameNox", loRS.getString("sFrameNox"));
                setMaster("sEngineNo", loRS.getString("sEngineNo"));
                setMaster("sKeyNoxxx", loRS.getString("sKeyNoxxx"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sSerialID", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sDescript", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sKeyNoxxx", "");
                return false;    
            }        
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonSearch(poGRider
                                                        , lsSQL
                                                        , "%" + fsValue +"%"
                                                        //, "%"
                                                        , "Serial ID»CS No»Vehicle Description»Plate No»Frame Number»Engine Number"
                                                        , "sSerialID»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo"
                                                        , "a.sSerialID»c.sDescript»a.sCSNoxxxx»b.sPlateNox»a.sFrameNox»a.sEngineNo"
                                                        ,fbisByCode ? 0 : 2);

            if (loJSON != null){
                sCSPlateNo = (String) loJSON.get("sPlateNox");
                if(sCSPlateNo.isEmpty()){
                    sCSPlateNo = (String) loJSON.get("sCSNoxxxx");
                }
                
                sOwnerID = (String) loJSON.get("sClientID");
                sSoldStat =(String) loJSON.get("cSoldStat");
                sSerialID = (String) loJSON.get("sSerialID");
                
                setMaster("sSerialID", (String) loJSON.get("sSerialID"));
                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
                setMaster("sKeyNoxxx", (String) loJSON.get("sKeyNoxxx"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sSerialID", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sDescript", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sKeyNoxxx", "");
                return false;    
            }
        } 
        
        if(!checkUnitAvailability(!fbisByCode,sOwnerID, sSoldStat, sCSPlateNo,sSerialID)){
            if(!fbisByCode){
                setMaster("sSerialID", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sDescript", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sKeyNoxxx", "");
            }
            return false;
        }
        
        return true;
    }
    
    private String getSQ_UDR(){
        return " SELECT " + 
                "  a.sTransNox " + //
                ", a.dTransact " + //
                ", a.sReferNox " + //3udrno
                ", a.sClientID " + //
                ", a.sSerialID " + //
                ", a.cTranStat " + //
                ", b.sTransNox as sVSPCodex " + //
                ", b.sVSPNOxxx " + //
                ", b.cIsVhclNw " + //
                ", b.sInqryIDx " + //
                ", a.cCustType " + //
            " FROM udr_master a " +
            " LEFT JOIN vsp_master b ON b.sTransNox = a.sSourceCD "  ;
    }
    
    private boolean checkUnitAvailability(boolean bisSelUnit,String sClientID, String sUnitStat, String sCSPlateNo, String sSerialID){
        try {
            //0. CHECK FOR: DI EXIST TODO
            
            //1. CHECK FOR: UDR EXIST
            //If dr exist with the same goods category check for latest delivery instruction
            //Get Delivery Instruction for this unit with the same good category
            /* 1. If DI doesn't exist do not allow */
            String lsSQL = getSQ_UDR();
            ResultSet loRS;
            lsSQL = lsSQL + " WHERE a.sSerialID = " + SQLUtil.toSQL(sSerialID) +
                            " AND b.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox")) +
                            " AND b.cIsVhclNw = " + SQLUtil.toSQL(poMaster.getString("cIsVhclNw")) +
                            " AND a.cTranStat <> '0' "; 
            loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0){
                psMessage = "Plate No./CS No. " + sCSPlateNo+ " already linked on UDR. Please verify.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            //2. CHECK FOR: VSP EXIST
            lsSQL = getSQ_Master();
            lsSQL = lsSQL + " WHERE a.sSerialID = " + SQLUtil.toSQL(sSerialID) +
                            " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox")) +
                            " AND a.cIsVhclNw = " + SQLUtil.toSQL(poMaster.getString("cIsVhclNw")) +
                            " AND a.cTranStat <> '0' GROUP BY a.sTransNox "; 
            loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0){
                psMessage = "Plate No./CS No. " + sCSPlateNo+ " already linked on other VSP. Please verify.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            //3. CHECK FOR: VEHICLE CUSTOMER OWNERSHIP
            if(!sClientID.isEmpty()){
                if(!sClientID.equals((String) getMaster("sClientID"))){
                    psMessage = "Plate No./CS No. " +sCSPlateNo+" has different owner from buying customer. Please verify.";
                    return false;
                }
            }
            
            //4. CHECK FOR: AVAILABILITY BY VEHICLE STATUS
            if(bisSelUnit){
                if(sUnitStat.equals("2")){
                    psMessage= "Plate No./CS No. " + sCSPlateNo+ " has on-going Transaction in VSP.";
                    return false;
                } else if(sUnitStat.equals("3")){
                    psMessage=  "Plate No./CS No. " + sCSPlateNo+ " has been SOLD already.";
                    return false;
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    private String getSQ_BankApplication(){
        return "SELECT " +
                    " a.sTransNox " +//1                    
                    " ,a.dAppliedx " +//2
                    " ,a.dApproved " +//3
                    " ,a.cPayModex " +//4
                    " ,a.sBankIDxx " +//5
                    " ,a.cTranStat " +//6
                    " ,IFNULL(b.sBankName, '') as sBankName" +//7
                    " ,IFNULL(f.cPayModex, '') as sPayment" +//8
                    " ,IFNULL(b.sBankBrch, '') as sBankBrch" +//9
                    " ,TRIM(CONCAT(c.sTownName, ', ', e.sProvName)) sTownName" +//10
                " FROM bank_application a " +
                " LEFT JOIN banks b ON b.sBankIDxx = a.sBankIDxx " +
                " LEFT JOIN TownCity c ON c.sTownIDxx = b.sTownIDxx" +                
                " LEFT JOIN Province e on e.sProvIDxx = c.sProvIDxx" + 
                " LEFT JOIN customer_inquiry f ON f.sTransNox = a.sSourceCD"; //sSourceNo
    
    }
    
    /**
     * Search for Approved Bank Application base on inquiry payment mode
     * 
    */
    public boolean searchBankApplication(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            String lsSQL = MiscUtil.addCondition(getSQ_BankApplication(), " f.sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"))
                                                + " AND f.cPayModex = " + SQLUtil.toSQL((String) getMaster("cPayModex"))
                                                + " AND a.cTranStat = '2' ") 
                                                + " GROUP BY a.sTransNox";
            
            System.out.println(lsSQL);
            ResultSet loRS;
            JSONObject loJSON = null;
            if (!pbWithUI) {   
                lsSQL += " LIMIT 1";
                loRS = poGRider.executeQuery(lsSQL);

                if (loRS.next()){
                    setMaster("sBnkAppCD", loRS.getString("sTransNox"));
                    setVSPFinance("sBankIDxx", loRS.getString("sBankIDxx"));
                    setVSPFinance("sBankname", loRS.getString("sBankName") + " / " +loRS.getString("sBankBrch")  );
                } else {
                    psMessage = "No record found/selected.";
                    setMaster("sBnkAppCD", "");
                    setVSPFinance("sBankIDxx", "");
                    setVSPFinance("sBankname", "");
                    return false;    
                }        
            } else {
                loRS = poGRider.executeQuery(lsSQL);
                loJSON = showFXDialog.jsonSearch(poGRider
                                                            , lsSQL
                                                            , ""
                                                            , "Bank Name»Branch»Bank Address»Approved Date"
                                                            , "sBankName»sBankBrch»sTownName»dApproved"
                                                            , "b.sBankName»b.sBankBrch»a.dApproved"
                                                            , 0);

                if (loJSON != null){
                    setMaster("sBnkAppCD", (String) loJSON.get("sTransNox"));
                    setVSPFinance("sBankIDxx",(String) loJSON.get("sBankIDxx"));
                    setVSPFinance("sBankname",(String) loJSON.get("sBankName") + " / " + (String) loJSON.get("sBankBrch"));
                } else {
                    psMessage = "No record found/selected.";
                    setMaster("sBnkAppCD", "");
                    setVSPFinance("sBankIDxx", "");
                    setVSPFinance("sBankname", "");
                    return false;    
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    //TODO when insurance table is available
    private String getSQ_Insurance(){
        return "";
    
    }
    
    /**
     * Search for Insurance Company
     * @param fsValue for Insurance Company
     * @param fisTPL determines if the search is performed for TPL or Comprehensive Insurance
    */
    public boolean searchInsurance(String fsValue,boolean fisTPL){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            //String lsSQL = getSQ_Insurance();
//            lsSQL = MiscUtil.addCondition(lsSQL, " insNamexxx LIKE " + SQLUtil.toSQL("%" + fsValue)
//                                                + " AND a.cTranStat = '1' ") 
//                                                + " GROUP BY a.insNamexxx";
            String lsSQL = getSQ_BuyingCustomer() + " WHERE a.cRecdStat = '1'  "  ;
            
            System.out.println(lsSQL);
            ResultSet loRS;
            JSONObject loJSON = null;
            if (!pbWithUI) {   
                lsSQL += " LIMIT 1";
                loRS = poGRider.executeQuery(lsSQL);

                if (loRS.next()){
                    if (fisTPL){
                        setMaster("sInsTplCd", loRS.getString("sClientID"));
                        setMaster("sInsTplNm", loRS.getString("sCompnyNm"));
                    } else {
                        setMaster("sInsCodex", loRS.getString("sClientID"));
                        setMaster("sInsComNm", loRS.getString("sCompnyNm"));
                    }
                    
//                    if (fisTPL){
//                        setMaster("sInsTplCd", loRS.getString("inscode"));
//                        setMaster("sInsTplNm", loRS.getString("insname"));
//                    } else {
//                        setMaster("sInsCodex", loRS.getString("inscode"));
//                        setMaster("sInsComNm", loRS.getString("insname"));
//                    }
                } else {
                    psMessage = "No record found/selected.";
                    if (fisTPL){
                        setMaster("sInsTplCd", "");
                        setMaster("sInsTplNm", "");
                    } else {
                        setMaster("sInsCodex", "");
                        setMaster("sInsComNm", "");
                    }
                    return false;    
                }        
            } else {
//                loJSON = showFXDialog.jsonSearch(poGRider
//                                                            , getSQ_Insurance()
//                                                            , ""
//                                                            , "Insurance Name»Branch»Address Address"
//                                                            , "sBankName»sBankBrch»sTownName"
//                                                            , "b.sBankName»b.sBankBrch"
//                                                            , 0);
                loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "%" + fsValue +"%",
                                             "Insurance ID»Insurance Company", 
                                             "sClientID»sCompnyNm",
                                             "a.sClientID»a.sCompnyNm",
                                             1);

                if (loJSON != null){
                    if (fisTPL){
                        setMaster("sInsTplCd", (String) loJSON.get("sClientID"));
                        setMaster("sInsTplNm", (String) loJSON.get("sCompnyNm"));
                    } else {
                        setMaster("sInsCodex", (String) loJSON.get("sClientID"));
                        setMaster("sInsComNm", (String) loJSON.get("sCompnyNm"));
                    }
                
                } else {
                    psMessage = "No record found/selected.";
                    if (fisTPL){
                        setMaster("sInsTplCd", "");
                        setMaster("sInsTplNm", "");
                    } else {
                        setMaster("sInsCodex", "");
                        setMaster("sInsComNm", "");
                    }
                    return false;    
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    
}
