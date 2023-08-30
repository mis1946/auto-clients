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
            case 3:   //sVSPNOxxx
            case 5:   //sInqryIDx
            case 6:   //sClientID
            case 7:   //sSerialID
            case 9:   //sRemarksx
            case 23:  //sBnkAppCD
            case 31:  //sEndPlate
            case 32:  //sBranchCd
            case 39:  //sDcStatCd
            case 42:  //sLockedBy
            case 45:  //sCancelld
//            case 47:  //sEntryByx
//            case 48:  //dEntryDte
//            case 49:  //sModified
//            case 50:  //dModified
            case 52:  //sCompnyNm
            case 53:  //sAddressx
            case 54:  //sDescript
            case 55:  //sCSNoxxxx
            case 56:  //sPlateNox
            case 57:  //sFrameNox
            case 58:  //sEngineNo
            case 59:  //sSalesExe
            case 60:  //sSalesAgn
            case 61:  //sInqClntx
            case 62:  //sUdrNoxxx
            case 63:  //sBranchCD
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 2:   //dTransact 
            case 4:   //dDelvryDt
            case 40:  //dDcStatDt
            case 43:  //dLockedDt
            case 46:  //dCancelld
            case 51:  //dTimeStmp
                 if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break; 
            
            case 41:  //cPrintedx
            case 33:  //nDealrRte
            case 34:  //nDealrAmt
            case 35:  //nSIsInRte
            case 36:  //nSIsInAmt
            case 37:  //cIsVhclNw
            case 38:  //cIsVIPxxx
            case 24:  //nTranTotl
            case 25:  //nAmtPaidx
            case 26:  //nFrgtChrg
            case 27:  //nDue2Supx
            case 28:  //nDue2Dlrx
            case 29:  //nSPFD2Sup
            case 30:  //nSPFD2Dlr
            case 10:  //nLaborAmt
            case 11:  //nAccesAmt
            case 12:  //nInsurAmt
            case 13:  //nResrvFee
            case 14:  //nDownPaym
            case 15:  //nOthrPaym
            case 16:  //nPromoDsc
            case 17:  //nFleetDsc
            case 18:  //nSPFltDsc
            case 19:  //nBndleDsc
            case 20:  //nAddlDscx
            case 21:  //nDealrInc
            case 22:  //cPayModex
            case 8:   //nUnitPrce
            case 44:  //cTranStat
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
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sSalesExe»sSalesAgn»sInqClntx»sUdrNoxxx»sBranchCD»dTimeStmp");
            } else { //update  
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sSalesExe»sSalesAgn»sInqClntx»sUdrNoxxx»sBranchCD»dTimeStmp", 
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
            if(((String) getMaster("cTranStat")).equals("1")){
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
            } else {
                //Update customer_inquiry when VSP is cancelled
                lsSQL = "UPDATE customer_inquiry SET" +
                            " cTranStat = '1'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));
                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, "") <= 0){
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
    
    private String getSQ_Master(){
        return  " SELECT " + 
                " IFNULL(a.sTransNox, '') as sTransNox" + //1
                ", a.dTransact " + //2
                ", IFNULL(a.sVSPNOxxx, '') as sVSPNOxxx" + //3
                ", a.dDelvryDt " + //4
                ", IFNULL(a.sInqryIDx, '') as sInqryIDx" + //5
                ", IFNULL(a.sClientID, '') as sClientID" + //6 buying cutomer
                ", IFNULL(a.sSerialID, '') as sSerialID" + //7
                ", a.nUnitPrce " + //8
                ", IFNULL(a.sRemarksx, '') as sRemarksx" + //9
                ", a.nLaborAmt " + //10
                ", a.nAccesAmt " + //11
                ", a.nInsurAmt " + //12
                ", a.nResrvFee " + //13
                ", a.nDownPaym " + //14
                ", a.nOthrPaym " + //15
                ", a.nPromoDsc " + //16
                ", a.nFleetDsc " + //17
                ", a.nSPFltDsc " + //18
                ", a.nBndleDsc " + //19
                ", a.nAddlDscx " + //20
                ", a.nDealrInc " + //21
                ", a.cPayModex " + //22
                ", IFNULL(a.sBnkAppCD, '') as sBnkAppCD" + //23
                ", a.nTranTotl " + //24
                ", a.nAmtPaidx " + //25
                ", a.nFrgtChrg " + //26
                ", 0 as nDue2Supx " + //27 NOT EXIST ON ACTUAL DB
                ", 0 as nDue2Dlrx " + //28 NOT EXIST ON ACTUAL DB
                ", 0 as nSPFD2Sup " + //29 NOT EXIST ON ACTUAL DB
                ", 0 as nSPFD2Dlr " + //30 NOT EXIST ON ACTUAL DB
                ", IFNULL(a.sEndPlate, '') as sEndPlate " + //31
                ", IFNULL(a.sBranchCd, '') as sBranchCd " + //32
                ", a.nDealrRte " + //33
                ", a.nDealrAmt " + //34
                ", a.nSIsInRte " + //35
                ", a.nSIsInAmt " + //36
                ", a.cIsVhclNw " + //37
                ", a.cIsVIPxxx " + //38
                ", IFNULL(a.sDcStatCd, '') as sDcStatCd " + //39
                ", a.dDcStatDt " + //40
                ", a.cPrintedx " + //41
                ", IFNULL(a.sLockedBy, '') as sLockedBy" + //42
                ", a.dLockedDt " + //43
                ", a.cTranStat " + //44
                ", IFNULL(a.sCancelld, '') as sCancelld " + //45
                ", a.dCancelld " + //46
                ", IFNULL(a.sEntryByx, '') as sEntryByx" + //47
                ", a.dEntryDte " + //48
                ", IFNULL(a.sModified, '') as sModified " + //49
                ", a.dModified " + //50
                ", a.dTimeStmp " + //51	 dTimeStmp																																
                ", IFNULL(c.sCompnyNm,'') as sCompnyNm " + //52
                ", IFNULL((SELECT CONCAT( ifnull( concat(client_address.sAddressx,', ') , ''), IFNULL(concat(barangay.sBrgyName,', '), ''), IFNULL(concat(TownCity.sTownName, ', '),''), IFNULL(concat(Province.sProvName, ', ' ),'')) FROM client_address "  +																																				
                //", (SELECT IFNULL(TRIM(CONCAT(client_address.sAddressx, ', ',barangay.sBrgyName, ', ', TownCity.sTownName, ', ', Province.sProvName)), '') FROM client_address " +
                "  LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx "  +
                "  LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx "  +
                "  LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx "  +
                "  WHERE client_address.sClientID = a.sClientID AND client_address.cPrimaryx = 1 AND client_address.cRecdStat = 1 " +                             
                "  LIMIT 1), '') as sAddressx " + //53																																					
                ", IFNULL(f.sDescript,'') as sDescript " + //54																																						
                ", IFNULL(d.sCSNoxxxx,'') as sCSNoxxxx " + //55																																						
                ", IFNULL(e.sPlateNox,'') as sPlateNox " + //56																																					
                ", IFNULL(d.sFrameNox,'') as sFrameNox " + //57																																					
                ", IFNULL(d.sEngineNo,'') as sEngineNo " + //58
                //TODO fix query when tables for sales agent and executive is active 04-27-2023
                ",IFNULL((SELECT sCompnyNm FROM client_master WHERE sClientID = b.sEmployID), '') AS sSalesExe" + //59
                ",IFNULL((SELECT sCompnyNm FROM client_master WHERE sClientID = b.sAgentIDx), '') AS sSalesAgn" + //60
                ",IFNULL((SELECT sCompnyNm FROM client_master WHERE sClientID = b.sClientID), '') AS sInqClntx" + //61 inquiring customer
                ",IFNULL((SELECT sReferNox FROM udr_master WHERE sClientID = a.sTransNox), '') AS sUdrNoxxx" + //62 udr no
                ", IFNULL(b.sBranchCD,'') as sBranchCD " + //63 branch code
                " FROM " + MASTER_TABLE + " a" +
                " LEFT JOIN customer_inquiry b ON b.sTransNox = a.sInqryIDx " + 
                " LEFT JOIN client_master c ON c.sClientID = a.sClientID " + 																																					
                " LEFT JOIN vehicle_serial d ON d.sSerialID = a.sSerialID " +																																					
                " LEFT JOIN vehicle_serial_registration e ON e.sSerialID = d.sSerialID " + 
                " LEFT JOIN vehicle_master f ON f.sVhclIDxx = d.sVhclIDxx "  ;
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
                    ",IFNULL((SELECT CONCAT( ifnull( concat(client_address.sAddressx,', ') , ''), IFNULL(concat(barangay.sBrgyName,', '), ''), IFNULL(concat(TownCity.sTownName, ', '),''), IFNULL(concat(Province.sProvName, ', ' ),'')) FROM client_address "  +
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
                setMaster("sBranchCD", loRS.getString("sBranchCD"));
                setMaster("sSalesExe", loRS.getString("sSalesExe"));
                setMaster("sSalesAgn", loRS.getString("sSalesAgn"));
                setMaster("sInqClntx", loRS.getString("sCompnyNm"));
                setMaster("cPayModex", loRS.getString("cPayModex"));
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
                setMaster("sBranchCD", (String) loJSON.get("sBranchCD"));
                setMaster("sSalesExe", (String) loJSON.get("sSalesExe"));
                setMaster("sSalesAgn", (String) loJSON.get("sSalesAgn"));
                setMaster("sInqClntx", (String) loJSON.get("sCompnyNm"));
                setMaster("cPayModex", (String) loJSON.get("cPayModex"));
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
                ", IFNULL((SELECT CONCAT( ifnull( concat(client_address.sAddressx,', ') , ''), IFNULL(concat(barangay.sBrgyName,', '), ''), IFNULL(concat(TownCity.sTownName, ', '),''), IFNULL(concat(Province.sProvName, ', ' ),'')) FROM client_address "  +
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
                                                        , "sCSNoxxxx»sDescript»sPlateNox»sFrameNox»sEngineNo"
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
