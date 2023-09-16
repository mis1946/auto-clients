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
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author Arsiela
 * Date Created: 08-18-2023
 */
public class VehicleSalesProposalMaster {
    private final String MASTER_TABLE = "vsp_master";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
    private CachedRowSet poVSPFinance;
    private CachedRowSet poVSPLabor;
    private CachedRowSet poVSPParts;
    private CachedRowSet poBankApp;
    
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
    
    public int getItemCount() throws SQLException{
        poMaster.last();
        return poMaster.getRow();
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
            /*dTimeStmp*/
                if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break; 
            case 8:  // nUnitPrce
            case 10: // nAdvDwPmt 
            case 11: // nOthrDesc
            case 12: // nOthrChrg
            case 13: // nLaborAmt
            case 14: // nAccesAmt
            case 15: // nInsurAmt
            case 16: // nTPLAmtxx
            case 17: // nCompAmtx
            case 18: // nLTOAmtxx
            case 19: // nChmoAmtx
            case 25: // nInsurYrx
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
            case 50: // nDealrRte
            case 51: // nDealrAmt
            case 52: // nSlsInRte
            case 53: // nSlsInAmt
            case 34: // cPayModex
            case 54: // cIsVhclNw
            case 55: // cIsVIPxxx
            case 58: // cPrintedx
            case 60: // dLockedDt
            case 61: // cTranStat
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
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
    
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        
        try {
            String lsSQL = getSQ_Master() + " WHERE 0=1";
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);       
            poMaster.updateString("cTranStat", RecordStatus.ACTIVE);    
            poMaster.updateObject("dTransact", poGRider.getServerDate());  
            poMaster.updateString("cIsVIPxxx", "0"); 
            poMaster.updateString("cPrintedx", "0"); 
            poMaster.insertRow();
            poMaster.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    public boolean searchRecord() throws SQLException{
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , ""
                                                    , "VSP No»Customer Name"
                                                    , "sTransNox»sCompnyNm"
                                                    , "a.sTransNox"
                                                    , 0);
        
       
        if (loJSON == null){
            psMessage = "No record found/selected.";
            return false;
        } else {
            if (OpenRecord((String) loJSON.get("sTransNox")) ){
                if (loadVSPFinance()) {
                } else {
                    psMessage = "Error while loading VSP Finance.";
                    return false;
                }
            }else {
                psMessage = "No record found/selected.";
                return false;
            }
        }
               
        return true;
    }
    
    public boolean OpenRecord(String fsValue){
        try {
            String lsSQL = getSQ_Master();
            lsSQL = lsSQL + " WHERE a.sTransNox = " + SQLUtil.toSQL(fsValue);
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
            if (pnEditMode == EditMode.ADDNEW){ //add
                poMaster.updateString("sTransNox",MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", false, poGRider.getConnection(), psBranchCd) );                                                             
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sSalesExe»sSalesAgn»sInqClntx»sUdrNoxxx»sBranchCD»dTimeStmp»dInqDatex»sInqTypex»sOnlStore»sRefTypex»sKeyNoxxx»sBranchNm");
            } else { //update  
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sSalesExe»sSalesAgn»sInqClntx»sUdrNoxxx»sBranchCD»dTimeStmp»dInqDatex»sInqTypex»sOnlStore»sRefTypex»sKeyNoxxx»sBranchNm", 
                                            "sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
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
            
            /*UPDATE INQUIRY*/
            //Update customer_inquiry status to with VSP
            if(((String) getMaster("sUdrNoxxx")).isEmpty()){
                lsSQL = "UPDATE customer_inquiry SET" +
                        " cTranStat = '3'" +
                    " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));
                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                } 

                lsSQL = "UPDATE vehicle_serial SET" +
                        " cSoldStat = '2'" +
                        ", sClientID = " + SQLUtil.toSQL((String) getMaster("sClientID")) +
                    " WHERE sSerialID = " + SQLUtil.toSQL((String) getMaster("sSerialID"));
                if (poGRider.executeQuery(lsSQL, "vehicle_serial", psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
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
    
    private boolean isEntryOK() throws SQLException{
        poMaster.first();
        
        if (poMaster.getString("sVSPNOxxx").isEmpty()){
            psMessage = "VSP Number is not set.";
            return false;
        }
       
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = lsSQL + " WHERE a.sVSPNOxxx = " + SQLUtil.toSQL(poMaster.getString("sVSPNOxxx")) +
                        " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox")); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing VSP Number.";
            MiscUtil.close(loRS);        
            return false;
        }
                   
        return true;
    }
    
    public boolean cancelVSP(boolean fbIsLostSale) {
        try {
            if (pnEditMode != EditMode.READY) {
                psMessage = "Invalid update mode detected.";
                return false;
            }
            
            psMessage = "";
            if (!isCancelOK()) return false;
            if (!pbWithParent) poGRider.beginTrans();
            
            String lsSQL = "UPDATE " + MASTER_TABLE + " SET"
                    + " cTranStat = '0'"
                    + ", sCancelld = " + SQLUtil.toSQL(poGRider.getUserID())
                    + ", dCancelld = " + SQLUtil.toSQL((Date) poGRider.getServerDate())
                    + " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox"));
            
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
                psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                return false;
            }
            
            //Update Inquiry to LOST SALE
            if(fbIsLostSale){
                lsSQL = "UPDATE customer_inquiry SET" +
                        " cTranStat = '2'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));
                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                }
                
            } else {
                //Update Inquiry to ON PROCESS
                lsSQL = "UPDATE customer_inquiry SET" +
                        " cTranStat = '1'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));
                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                }
            }
            
            //Update Vehicle Serial to AVAILABLE FOR SALE and SET NULL for Client ID
            lsSQL = "UPDATE vehicle_serial SET" +
                    " cSoldStat = '1'" +
                    ", sClientID = NULL " +
                    " WHERE sSerialID = " + SQLUtil.toSQL((String) getMaster("sSerialID"));
            if (poGRider.executeQuery(lsSQL, "vehicle_serial", psBranchCd, "") <= 0){
                psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                return false;
            } 
            
            if (!pbWithParent) poGRider.commitTrans();
            pnEditMode = EditMode.UNKNOWN;
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    private boolean isCancelOK() throws SQLException{
        poMaster.first();
       
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
            " ,a.nOthrDesc " + //11
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
            " , IFNULL((SELECT CONCAT( IFNULL( CONCAT(client_address.sAddressx,', ') , ''), IFNULL(CONCAT(barangay.sBrgyName,', '), ''), IFNULL(CONCAT(TownCity.sTownName, ', '),''), IFNULL(CONCAT(Province.sProvName, ', ' ),'')) FROM client_address  " + 																																				
                "   LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx   " +
                "   LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx   " +
                "   LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx   " +
                "   WHERE client_address.sClientID = a.sClientID AND client_address.cPrimaryx = 1 AND client_address.cRecdStat = 1   " +                            
                "   LIMIT 1), '') AS sAddressx   " + //69  	 																																				
            " , IFNULL(f.sDescript,'') AS sDescript    " + //70																																						
            " , IFNULL(d.sCSNoxxxx,'') AS sCSNoxxxx    " + //71																																						
            " , IFNULL(e.sPlateNox,'') AS sPlateNox    " + //72																																					
            " , IFNULL(d.sFrameNox,'') AS sFrameNox    " + //73																																					
            ", IFNULL(d.sEngineNo,'') AS sEngineNo    " + //74
            " ,IFNULL((SELECT IFNULL(b.sCompnyNm, '') sCompnyNm  " + 
                "  FROM ggc_isysdbf.employee_master001   " + 
                "  LEFT JOIN ggc_isysdbf.client_master b ON b.sClientID = employee_master001.sEmployID  " +
                "  LEFT JOIN ggc_isysdbf.department c ON c.sDeptIDxx = employee_master001.sDeptIDxx  " +
                "  LEFT JOIN ggc_isysdbf.branch_others d ON d.sBranchCD = employee_master001.sBranchCd   " +
                "  WHERE (c.sDeptIDxx = 'a011' OR c.sDeptIDxx = '015') AND ISNULL(employee_master001.dFiredxxx) AND  " +
                "  d.cDivision = (SELECT cDivision FROM ggc_isysdbf.branch_others WHERE sBranchCd = " + SQLUtil.toSQL(psBranchCd) +
                " ) AND employee_master001.sEmployID =  b.sEmployID), '') AS sSalesExe    " + //75
            " ,IFNULL((SELECT sCompnyNm FROM client_master WHERE sClientID = b.sAgentIDx), '') AS sSalesAgn  " + //76
            " ,IFNULL((SELECT sCompnyNm FROM client_master WHERE sClientID = b.sClientID), '') AS sInqClntx  " + //77
            " ,IFNULL (b.dTransact, '') AS  dInqDatex    " + //78
            " ,IFNULL((SELECT sReferNox FROM udr_master WHERE sClientID = a.sTransNox), '') AS sUdrNoxxx " +//79
            " ,IFNULL (b.sSourceCD, '') AS  sInqTypex    " + //80
            " ,IFNULL((SELECT sPlatform FROM online_platforms WHERE sTransNox = b.sSourceNo), '') AS sOnlStore  " + //81
            " , '' AS  sRefTypex " + //82
            " , IFNULL(d.sKeyNoxxx,'') AS sKeyNoxxx    " + //83
            " , (SELECT IFNULL(branch.sBranchNm, '') FROM branch WHERE branch.sBranchCd = b.sBranchCd) AS sBranchNm " + //84
            " FROM vsp_master a  " + 
            " LEFT JOIN customer_inquiry b ON b.sTransNox = a.sInqryIDx   " + 
            " LEFT JOIN client_master c ON c.sClientID = a.sClientID  	 " + 																																			
            " LEFT JOIN vehicle_serial d ON d.sSerialID = a.sSerialID  	 " + 																																			
            " LEFT JOIN vehicle_serial_registration e ON e.sSerialID = d.sSerialID   " + 
            " LEFT JOIN vehicle_master f ON f.sVhclIDxx = d.sVhclIDxx  " ;
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
            " FROM vsp_finance a"  ;
    }
    
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
        poMaster.first();
        
        switch (fnIndex){ 
            case 1: // sTransNox
            case 3: // sBankIDxx
            case 4: // sBankname 
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 2: // cFinPromo
            case 5: // nFinAmtxx 
            case 6: // nAcctTerm
            case 7: // nAcctRate 
            case 8: // nRebatesx
            case 9: // nMonAmort 
            case 10: // nPNValuex
            case 11: // nBnkPaidx
            case 12: // nGrsMonth
            case 13: // nNtDwnPmt
            case 14: // nDiscount
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
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
        poVSPFinance.last();
        return poVSPFinance.getRow();
    }
    
    public Object getVSPFinanceDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVSPFinance.absolute(fnRow);
        return poVSPFinance.getObject(fnIndex);
    }
    
    public Object getVSPFinanceDetail(int fnRow, String fsIndex) throws SQLException{
        return getVSPFinanceDetail(fnRow, MiscUtil.getColumnIndex(poVSPFinance, fsIndex));
    }
    
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
                
                poVSPFinance.last();
                poVSPFinance.moveToInsertRow();
                MiscUtil.initRowSet(poVSPFinance);
                poVSPFinance.insertRow();
                poVSPFinance.moveToCurrentRow();
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private String getSQ_VSPLabor(){
        return " SELECT "
                + " sTransNox " //1
                + " nEntryNox " //2
                + " IFNULL(sLaborCde, '') AS sLaborCde " //3
                + " nLaborAmt " //4
                + " IFNULL(sChrgeTyp, '') AS sChrgeTyp" //5
                + " IFNULL(sChrgeTox, '') AS sChrgeTox" //6
                + " IFNULL(sRemarksx, '') AS sRemarksx" //7
                + " IFNULL(sLaborDsc, '') AS sLaborDsc" //8
                + " cAddtlxxx " //9 
                + " dAddDatex " //10
                + " IFNULL(sAddByxxx, '') AS sAddByxxx" //11
                //+ " dTimeStmp " //12
                + " FROM vsp_labor ";
    }
    
    public boolean loadVSPLaborList(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL = getSQ_VSPLabor();
            lsSQL = MiscUtil.addCondition(lsSQL, " sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
            
            System.out.println(lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //open bank application
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
        poVSPLabor.last();
        return poVSPLabor.getRow();
    }
    
    public Object getVSPLaborDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVSPLabor.absolute(fnRow);
        return poVSPLabor.getObject(fnIndex);
    }
    
    public Object getVSPLaborDetail(int fnRow, String fsIndex) throws SQLException{
        return getVSPLaborDetail(fnRow, MiscUtil.getColumnIndex(poVSPLabor, fsIndex));
    }
    
    public boolean addVSPLabor( String fsCode, String fsDescription, Double fdblAmount, String fsChrgeTyp, String fsChrgeTox,  String fsRemarksx){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory;
                
            if (poVSPLabor == null) {
                lsSQL = MiscUtil.addCondition(getSQ_VSPLabor(), "0=1");
                loRS = poGRider.executeQuery(lsSQL);
                factory = RowSetProvider.newFactory();
                poVSPLabor = factory.createCachedRowSet();
                poVSPLabor.populate(loRS);
                MiscUtil.close(loRS);
            }
            poVSPLabor.last();
            poVSPLabor.moveToInsertRow();
            MiscUtil.initRowSet(poVSPLabor);
            poVSPLabor.updateString("sLaborCde", fsCode);
            poVSPLabor.updateDouble("nLaborAmt", fdblAmount);
            poVSPLabor.updateString("sChrgeTyp", fsChrgeTyp);
            poVSPLabor.updateString("sChrgeTox", fsChrgeTox);
            poVSPLabor.updateString("sRemarksx", fsRemarksx);
            poVSPLabor.updateString("sLaborDsc", fsDescription);
            
            String lsIsAddl = "0";
            if (pnEditMode != EditMode.UPDATE) {
                lsIsAddl = "1";
            }
            poVSPLabor.updateString("cAddtlxxx", lsIsAddl);
            poVSPLabor.updateObject("dAddDatex", (Date) poGRider.getServerDate());
            poVSPLabor.updateString("sAddByxxx", poGRider.getUserID());
            poVSPLabor.insertRow();
            poVSPLabor.moveToCurrentRow();
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private String getSQ_VSPParts(){
        return " SELECT "
                + " sTransNox " //1
                + " nEntryNox " //2
                + " IFNULL(sStockIDx, '') AS sStockIDx " //3
                + " nUnitPrce " //4
                + " nSelPrice " //5
                + " nQuantity " //6
                + " nReleased " //7
                + " IFNULL(sChrgeTyp, '') AS sChrgeTyp " //8
                + " IFNULL(sChrgeTox, '') AS sChrgeTox " //9
                + " IFNULL(sDescript, '') AS sDescript " //10
                + " IFNULL(sPartStat, '') AS sPartStat " //11
                + " cAddtlxxx " //12
                + " dAddDatex " //13
                + " IFNULL(sAddByxxx, '') AS sAddByxxx " //14
                //+ " dTimeStmp " //15
                + " FROM vsp_parts ";
    }
    
    public boolean loadVSPPartsList(){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            String lsSQL = getSQ_VSPParts();
            lsSQL = MiscUtil.addCondition(lsSQL, " sTransNox = " + SQLUtil.toSQL((String) getMaster("sTransNox")));
            
            System.out.println(lsSQL);
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //open bank application
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
        poVSPParts.last();
        return poVSPParts.getRow();
    }
    
    public Object getVSPPartsDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVSPParts.absolute(fnRow);
        return poVSPParts.getObject(fnIndex);
    }
    
    public Object getVSPPartsDetail(int fnRow, String fsIndex) throws SQLException{
        return getVSPPartsDetail(fnRow, MiscUtil.getColumnIndex(poVSPParts, fsIndex));
    }
    
    public boolean AddVSPParts(String fsCode, String fsDescription, Double fdblAmount, String fsChrgeTyp, String fsChrgeTox,  Integer fnQuantity){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory;
                
            if (poVSPParts == null) {
                lsSQL = MiscUtil.addCondition(getSQ_VSPParts(), "0=1");
                loRS = poGRider.executeQuery(lsSQL);
                factory = RowSetProvider.newFactory();
                poVSPParts = factory.createCachedRowSet();
                poVSPParts.populate(loRS);
                MiscUtil.close(loRS);
            }
            poVSPParts.last();
            poVSPParts.moveToInsertRow();
            MiscUtil.initRowSet(poVSPParts);
            poVSPParts.updateString("sStockIDx", fsCode);
            poVSPParts.updateString("sChrgeTyp", fsChrgeTyp);
            poVSPParts.updateString("sChrgeTox", fsChrgeTox);
            poVSPParts.updateString("sDescript", fsDescription);
            poVSPParts.updateInt("nQuantity", fnQuantity);
            String lsIsAddl = "0";
            if (pnEditMode != EditMode.UPDATE) {
                lsIsAddl = "1";
            }
            poVSPParts.updateString("cAddtlxxx", lsIsAddl);
            poVSPParts.updateObject("dAddDatex", (Date) poGRider.getServerDate());
            poVSPParts.updateString("sAddByxxx", poGRider.getUserID());
            
//            poVSPParts.updateString("sPartStat", );
//            poVSPParts.updateString("nUnitPrce", );
//            poVSPParts.updateString("nSelPrice", );
//            poVSPParts.updateString("nReleased", );
            
            poVSPParts.insertRow();
            poVSPParts.moveToCurrentRow();
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
//    private String getSQ_VSPFinance(){
//        return " SELECT "
//                + " sTransNox" 
//                + ", cFinPromo" 
//                + ", nBankIDxx" 
//                + ", sBankName" 
//                + ", nFinAmtxx" 
//                + ", nAcctTerm" 
//                + ", nAcctRate" 
//                + ", nRebatesx" 
//                + ", nMonAmort" 
//                + ", nPNValuex" 
//                + ", nBnkPaidx" 
//                //+ ", dTimeStmp"
//                + " FROM vsp_finance a"
//                + " LEFT JOIN bank b ON b.nBankIDxx = a.nBankIDxx ";
//    
//    }
    
    private String getSQ_JobOrder(){
        return " SELECT " ;
    
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
                    ",IFNULL(b.sCompnyNm,'') as sCompnyNm" +
                    //",(SELECT IFNULL(sCompnyNm, '') FROM client_master WHERE sClientID = a.sClientID) AS sCompnyNm" +
                    ",IFNULL((SELECT sMobileNo FROM client_mobile WHERE sClientID = a.sClientID AND cPrimaryx = '1'), '') AS sMobileNo" +
                    ",IFNULL((SELECT sAccountx FROM client_social_media WHERE sClientID = a.sClientID LIMIT 1), '') AS sAccountx" + 
                    ",IFNULL((SELECT sEmailAdd FROM client_email_address WHERE sClientID = a.sClientID and cPrimaryx = '1'), '') AS sEmailAdd" + 
                    ",IFNULL((SELECT CONCAT( IFNULL( CONCAT(client_address.sAddressx,', ') , ''), IFNULL(CONCAT(barangay.sBrgyName,', '), ''), IFNULL(CONCAT(TownCity.sTownName, ', '),''), IFNULL(CONCAT(Province.sProvName, ', ' ),'')) FROM client_address "  +
                    //",(SELECT IFNULL(TRIM(CONCAT(client_address.sAddressx, ', ', barangay.sBrgyName, ', ', TownCity.sTownName, ', ', Province.sProvName)), '') FROM client_address" +
                                " LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx" +
                                " LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx" +
                                " LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx" +
                                " WHERE client_address.sClientID = a.sClientID and client_address.cPrimaryx = 1 and client_address.cRecdStat = 1" +                              
                                " limit 1), '') AS sAddressx" +  
                    //TODO fix query when tables for sales agent and executive is active 08-29-2023
                    ",IFNULL((SELECT sCompnyNm FROM client_master WHERE sClientID = a.sEmployID), '') AS sSalesExe" +
                    ",IFNULL((SELECT sCompnyNm FROM client_master WHERE sClientID = a.sAgentIDx), '') AS sSalesAgn" +
                    ",IFNULL((SELECT sPlatform FROM online_platforms WHERE sTransNox = a.sSourceNo), '') as sPlatform" +
                    ",IFNULL((SELECT sActTitle FROM activity_master WHERE sActvtyID = a.sActvtyID), '') as sActTitle" +
                    ", a.cPayModex " +
                    " FROM customer_inquiry a" +
                    " LEFT JOIN client_master b ON b.sClientID = a.sClientID";   
    }
    
    public boolean searchInquiry (String fsValue) throws SQLException{
        String lsSQL = getSQ_Inquiry();
        
        lsSQL = lsSQL + " WHERE b.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%") +
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
                setMaster("sClientID", loRS.getString("sClientID"));
                setMaster("cIsVhclNw", loRS.getString("cIsVhclNw"));
                setMaster("sInqryIDx", loRS.getString("sTransNox"));
                setMaster("sBranchCd", loRS.getString("sBranchCD"));
                setMaster("sSalesExe", loRS.getString("sSalesExe"));
                setMaster("sSalesAgn", loRS.getString("sSalesAgn"));
                setMaster("sInqClntx", loRS.getString("sCompnyNm"));
                setMaster("cPayModex", loRS.getString("cPayModex"));
                setMaster("dInqDatex", loRS.getString("dTransact"));
            } else {
                psMessage = "No record found.";
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sClientID", "");
                setMaster("cIsVhclNw", "");
                setMaster("sInqryIDx", "");
                setMaster("sBranchCd", "");
                setMaster("sSalesExe", "");
                setMaster("sSalesAgn", "");
                setMaster("sInqClntx", "");
                setMaster("cPayModex", "");
                setMaster("dInqDatex", "");
                return false;
            }           
        } else {
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             getSQ_Inquiry(),
                                             "%" + fsValue +"%",
                                             "Customer ID»Customer Name", 
                                             "sClientID»sCompnyNm",
                                             "sClientID»sCompnyNm",
                                             0);
            
            if (loJSON != null){
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sAddressx", (String) loJSON.get("sAddressx"));
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("cIsVhclNw", (String) loJSON.get("cIsVhclNw"));
                setMaster("sInqryIDx", (String) loJSON.get("sTransNox"));
                setMaster("sBranchCd", (String) loJSON.get("sBranchCD"));
                setMaster("sSalesExe", (String) loJSON.get("sSalesExe"));
                setMaster("sSalesAgn", (String) loJSON.get("sSalesAgn"));
                setMaster("sInqClntx", (String) loJSON.get("sCompnyNm"));
                setMaster("cPayModex", (String) loJSON.get("cPayModex"));
                setMaster("dInqDatex", (String) loJSON.get("dTransact"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sClientID", "");
                setMaster("cIsVhclNw", "");
                setMaster("sInqryIDx", "");
                setMaster("sBranchCd", "");
                setMaster("sSalesExe", "");
                setMaster("sSalesAgn", "");
                setMaster("sInqClntx", "");
                setMaster("cPayModex", "");
                setMaster("dInqDatex", "");
                return false;    
            }
        } 
        
        return true;
    }
    
    private String getSQ_BuyingCutomer(){
        return "SELECT" +
                "  IFNULL(a.sClientID, '') as sClientID" + 
                ", IFNULL(a.sLastName, '') as sLastName" + 
                ", IFNULL(a.sFrstName, '') as sFrstName" +
                ", IFNULL(a.sMiddName, '') as sMiddName" + 
                ", IFNULL(a.sCompnyNm, '') as sCompnyNm" +
                ", IFNULL(a.sClientNo, '') as sClientNo" + 
                ", a.cClientTp" + 
                ", a.cRecdStat" + 
                ", IFNULL((SELECT CONCAT( IFNULL( CONCAT(client_address.sAddressx,', ') , ''), IFNULL(CONCAT(barangay.sBrgyName,', '), ''), IFNULL(CONCAT(TownCity.sTownName, ', '),''), IFNULL(CONCAT(Province.sProvName, ', ' ),'')) FROM client_address "  +
                            " LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx" +
                            " LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx" +
                            " LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx" +
                            " WHERE client_address.sClientID = a.sClientID and client_address.cPrimaryx = 1 and client_address.cRecdStat = 1" +                              
                            " limit 1), '') AS sAddressx" +  
                " FROM client_master a" ;  
    }
    
    public boolean searchBuyingCustomer (String fsValue) throws SQLException{
        String lsSQL = getSQ_BuyingCutomer();
        
        lsSQL = lsSQL + " WHERE a.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%") +
                        " AND a.cRecdStat = '1'  "  ;
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                setMaster("sAddressx", loRS.getString("sAddressx"));
                setMaster("sClientID", loRS.getString("sClientID"));
            } else {
                psMessage = "No record found.";
                //setMaster("sCompnyNm", "");
                //setMaster("sAddressx", "");
                //setMaster("sClientID", "");
                return false;
            }           
        } else {
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             getSQ_BuyingCutomer(),
                                             "%" + fsValue +"%",
                                             "Customer ID»Customer Name", 
                                             "sClientID»sCompnyNm",
                                             "sClientID»sCompnyNm",
                                             0);
            
            if (loJSON != null){
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sAddressx", (String) loJSON.get("sAddressx"));
                setMaster("sClientID", (String) loJSON.get("sClientID"));
            } else {
                psMessage = "No record found/selected.";
                //setMaster("sCompnyNm", "");
                //setMaster("sAddressx", "");
                //setMaster("sClientID", "");
                return false;    
            }
        } 
        
        return true;
    }
    
    private String getSQ_AvailableVhcl(){
        return  "SELECT" + //
                "   IFNULL(a.sSerialID,'') sSerialID " + 
                " , IFNULL(a.sFrameNox,'') sFrameNox " + 
                " , IFNULL(a.sEngineNo,'') sEngineNo " + 
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx " + 
                " , IFNULL(a.sClientID,'') sClientID " + 
                " , IFNULL(a.sCSNoxxxx,'') sCSNoxxxx " + 
                " , IFNULL(a.cVhclNewx,'') cVhclNewx " + 
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
                "   FROM vehicle_serial a " + 
                "   LEFT JOIN vehicle_serial_registration b ON a.sSerialID = b.sSerialID  " +
                "   LEFT JOIN vehicle_master c ON c.sVhclIDxx = a.sVhclIDxx  " +
                "   LEFT JOIN vehicle_make d ON d.sMakeIDxx = c.sMakeIDxx  " +
                "   LEFT JOIN vehicle_model e ON e.sModelIDx = c.sModelIDx  " +
                "   LEFT JOIN vehicle_type f ON f.sTypeIDxx = c.sTypeIDxx  " +
                "   LEFT JOIN vehicle_color g ON g.sColorIDx = c.sColorIDx  " ;
    }
    
    /**
     * For searching available vehicle when key is pressed.
     * @return {@code true} if a matching available vehicle is found, {@code false} otherwise.
    */
    public boolean searchAvailableVhcl(String fsValue) throws SQLException{
        String lsSQL = getSQ_AvailableVhcl();
        
        lsSQL = MiscUtil.addCondition(lsSQL, " a.cSoldStat = '1' AND (ISNULL(a.sClientID) OR  TRIM(a.sClientID) = '' )" 
                + " AND a.cVhclNewx = " + SQLUtil.toSQL((String) getMaster("cIsVhclNw")) 
                + " AND (a.sCSNoxxxx LIKE " + SQLUtil.toSQL(fsValue + "%") 
                + "     OR b.sPlateNox LIKE " + SQLUtil.toSQL(fsValue + "%") + " ) " ); 
        
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sSerialID", loRS.getString("sSerialID"));
                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
                setMaster("sPlateNox", loRS.getString("sPlateNox"));
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sFrameNox", loRS.getString("sFrameNox"));
                setMaster("sEngineNo", loRS.getString("sEngineNo"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sSerialID", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sDescript", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                return false;    
            }        
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonSearch(poGRider
                                                        , lsSQL
                                                        , ""
                                                        , "CS No»Vehicle Description»Plate No»Frame Number»Engine Number"
                                                        , "sCSNoxxxx»sDescript»sPlateNox»sFrameNox»sEngineNo"
                                                        , "a.sCSNoxxxx»c.sDescript»b.sPlateNox»a.sFrameNox»a.sEngineNo"
                                                        , 0);

            if (loJSON != null){
                setMaster("sSerialID", (String) loJSON.get("sSerialID"));
                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sSerialID", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sDescript", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                return false;    
            }
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
                " LEFT JOIN customer_inquiry f ON f.sTransNox = a.sSourceNo";
    
    }
    
    public boolean loadBankApplicationList(String fsValue){
        try {
            if (poGRider == null){
                psMessage = "Application driver is not set.";
                return false;
            }
            
            psMessage = "";
            
            if (AddVSPFinance()){
            } else {
                psMessage = "Error in adding VSP Finance.";
                return false;
            }
             
            String lsSQL = getSQ_BankApplication();
            lsSQL = MiscUtil.addCondition(lsSQL, " f.sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"))
                                                + " AND a.cPayModex = " + SQLUtil.toSQL((String) getMaster("cPayModex"))
                                                + " AND b.sBankName LIKE " + SQLUtil.toSQL("%" + fsValue)
                                                + " AND a.cTranStat = '2' ") 
                                                + " GROUP BY a.sTransNox";
            
            System.out.println(lsSQL);
            ResultSet loRS;
            JSONObject loJSON = null;
            if (!pbWithUI) {   
                lsSQL += " LIMIT 1";
                loRS = poGRider.executeQuery(lsSQL);

                if (loRS.next()){
                    setVSPFinance("sBankIDxx", loRS.getString("sBankIDxx"));
                    setVSPFinance("sBankname", loRS.getString("sBankName") + " / " +loRS.getString("sBankBrch")  );
                } else {
                    psMessage = "No record found/selected.";
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
                    setVSPFinance("sBankIDxx",(String) loJSON.get("sBankIDxx"));
                    setVSPFinance("sBankname",(String) loJSON.get("sBankName") + " / " + (String) loJSON.get("sBankBrch"));
                } else {
                    psMessage = "No record found/selected.";
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
    
    public int getBankAppCount() throws SQLException{
        poBankApp.last();
        return poBankApp.getRow();
    }
    
    public Object getBankAppDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poBankApp.absolute(fnRow);
        return poBankApp.getObject(fnIndex);
    }
    
    public Object getBankAppDetail(int fnRow, String fsIndex) throws SQLException{
        return getBankAppDetail(fnRow, MiscUtil.getColumnIndex(poBankApp, fsIndex));
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
