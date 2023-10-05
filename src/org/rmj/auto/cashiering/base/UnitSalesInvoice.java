/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.cashiering.base;

import java.sql.ResultSet;
import java.sql.SQLException;
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
 * @author MIS
 */
public class UnitSalesInvoice {
    //TODO ADD ACCESS ON BUTTONS AND FORM
    private final String MASTER_TABLE = "si_master";
    private final String DEFAULT_DATE = "1900-01-01";

    private final String CASHIER = "";
    private final String ACCOUNTING = "";
    private final String MIS = "";
    private final String MAIN_OFFICE = "";

    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    public CachedRowSet poMaster;
    public CachedRowSet poSiSource;
    
    public UnitSalesInvoice(GRider foGRider, String fsBranchCd, boolean fbWithParent) {

        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;
    }

    public int getEditMode() {
        return pnEditMode;
    }

    public String getMessage() {
        return psMessage;
    }

    public void setWithUI(boolean fbValue) {
        pbWithUI = fbValue;
    }

    public void setCallback(MasterCallback foValue) {
        poCallback = foValue;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poMaster.first();
        
        switch (fnIndex){                           
            case 1: //a.sTransNox
            case 2: //a.sBranchCd                        
            case 5: //a.sReferNox
            case 6: //a.sSourceNo
            case 7: //a.sSourceCd
            case 8: //a.sClientID                  
            case 16: //a.sEntryByx
            case 17: //a.dEntryDte
            case 18: //sDescript 
            case 19: //sCSNoxxxx                                                                                                                                                                                                                                                                        
            case 20: //sPlateNox 			 																																		                                                                                                                                                                                            
            case 21: //sFrameNox 			 																																		                                                                                                                                                                                            
            case 22: //sEngineNo 			 																																	                                                                                                                                                                                              
            case 23: //sColorDsc 			 																																	                                                                                                                                                                                              
            case 24: //sSalesExe                                                                                                                                                                                                                                                                       
            case 25: //sEmployID              
            case 30: //sCompnyNm 
            case 31: //sAddressx                                                                                                                                                                                                                                                                   
            case 4:  //a.cFormType
            case 32: //b.cCustType
            case 33: //sTaxIDNox
            case 34: //sRemarksx
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 3: //a.dTransact 
            
                 if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break; 
            
            case 14: //a.cPrintedx
            case 15: //a.cTranStat           
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
                break;
            case 9: //a.nTranTotl
            case 10: //a.nDiscount
            case 11: //a.nVatRatex
            case 12: //a.nVatAmtxx
            case 13: //a.nAmtPaidx
            case 26: //nAddlDscx 
            case 27: //nPromoDsc 
            case 28: //nFleetDsc 
            case 29: //nUnitPrce 
                if (foValue instanceof Double)
                    poMaster.updateDouble(fnIndex, (Double) foValue);
                else 
                    poMaster.updateDouble(fnIndex, 0.00);                
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
            String lsSQL = (getSQ_Master()+  " WHERE 0=1");
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);   
            poMaster.updateString("cCustType", "1");    
            poMaster.updateString("cTranStat", RecordStatus.ACTIVE);    
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
    
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        return true; 
    }
    
    public boolean SearchRecord(){
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , ""
                                                    , "SI No»Customer Name"
                                                    , "a.sReferNox»sCompnyNm"
                                                    , "a.sReferNox»sCompnyNm"
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
        
        try {
            if(getMaster("cTranStat").equals("0")){
                pnEditMode = EditMode.UNKNOWN;
            }else{
                pnEditMode = EditMode.READY;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UnitSalesInvoice.class.getName()).log(Level.SEVERE, null, ex);
        }
    
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
            String lsTransNox = MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", false, poGRider.getConnection(), psBranchCd);
            if (pnEditMode == EditMode.ADDNEW){ //add                               
                //return true;
                poMaster.updateString("sTransNox",lsTransNox);                  
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());                
//                poMaster.updateString("sModified", poGRider.getUserID());
//                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                //TODO: need to remove »sRemarksx from exclude no column in table yet
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sColorDsc»sSalesExe»sEmployID»nAddlDscx»nPromoDsc»nFleetDsc»nUnitPrce»sCompnyNm»sAddressx»cCustType»sTaxIDNox»sRemarksx");
                
            } else { //update  
//                poMaster.updateString("sModified", poGRider.getUserID());
//                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                //TODO: need to remove »sRemarksx from exclude no column in table yet
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sColorDsc»sSalesExe»sEmployID»nAddlDscx»nPromoDsc»nFleetDsc»nUnitPrce»sCompnyNm»sAddressx»cCustType»sTaxIDNox»sRemarksx", 
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
            if (!pbWithParent) poGRider.commitTrans();
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        
        return true;
    }
    
        
    private String getSQ_Master(){
        return " SELECT " +                                                                                                                                                                                                                                                                                                       
                    " a.sTransNox " + //1                                                                                                                                                                                                                                                                                                  
                    " ,a.sBranchCd " +//2
                    " ,a.dTransact " +//3                                                                                                                                                                                                                                                                                               
                    " ,a.cFormType " +//4                                                                                                                                                                                                                                                                                                  
                    " ,a.sReferNox " +//5                                                                                                                                                                                                                                                                                                  
                    " ,a.sSourceNo " +//6                                                                                                                                                                                                                                                                                                  
                    " ,a.sSourceCd " +//7                                                                                                                                                                                                                                                                                                  
                    " ,a.sClientID " +//8                                                                                                                                                                                                                                                                                                  
                    " ,a.nTranTotl " +//9                                                                                                                                                                                                                                                                                                  
                    " ,a.nDiscount " +//10                                                                                                                                                                                                                                                                                                  
                    " ,a.nVatRatex " +//11                                                                                                                                                                                                                                                                                                  
                    " ,a.nVatAmtxx " +//12                                                                                                                                                                                                                                                                                                  
                    " ,a.nAmtPaidx " +//13                                                                                                                                                                                                                                                                                                  
                    " ,a.cPrintedx " +//14                                                                                                                                                                                                                                                                                                  
                    " ,a.cTranStat " +//15                                                                                                                                                                                                                                                                                                  
                    " ,a.sEntryByx " +//16                                                                                                                                                                                                                                                                                                  
                    " ,a.dEntryDte " +//17                                                                                                                                                                                                                                                                                                  
                    " , IFNULL(f.sDescript,'') as sDescript " +//18																																						                                                                                                                                                                                            
                    " , IFNULL(d.sCSNoxxxx,'') as sCSNoxxxx " +//19																																						                                                                                                                                                                                            
                    " , IFNULL(e.sPlateNox,'') as sPlateNox " +//20																																				                                                                                                                                                                                              
                    " , IFNULL(d.sFrameNox,'') as sFrameNox " +//21																																					                                                                                                                                                                                              
                    " , IFNULL(d.sEngineNo,'') as sEngineNo " +//22                                                                                                                                                                                                                                                                        
                    " , IFNULL(g.sColorDsc,'') AS sColorDsc " +//23                                                                                                                                                                                                                                                                        
                    " ,IFNULL((SELECT IFNULL(cm.sCompnyNm, '') sCompnyNm " +                                                                                                                                                                                                                                                          
                    "  FROM ggc_isysdbf.employee_master001 " +                                                                                                                                                                                                                                                                         
                    "  LEFT JOIN ggc_isysdbf.client_master cm ON cm.sClientID = employee_master001.sEmployID " +                                                                                                                                                                                                                       
                    "  LEFT JOIN ggc_isysdbf.department dep ON dep.sDeptIDxx = employee_master001.sDeptIDxx " +                                                                                                                                                                                                                         
                    "  LEFT JOIN ggc_isysdbf.branch_others bo ON bo.sBranchCD = employee_master001.sBranchCd " +                                                                                                                                                                                                                        
                    "  WHERE (dep.sDeptIDxx = 'a011' or dep.sDeptIDxx = '015') AND ISNULL(employee_master001.dFiredxxx) AND  " +                                                                                                                                                                                                        
                    "  bo.cDivision = (SELECT cDivision FROM ggc_isysdbf.branch_others WHERE sBranchCd = a.sBranchCD " +                                                                                                                                                                                                                
                    " ) AND employee_master001.sEmployID =  h.sEmployID), '') AS sSalesExe " + //24                                                                                                                                                                                                                                        
                    " ,h.sEmployID " +//25                                                                                                                                                                                                                                                                                                  
                    " ,IFNULL(c.nAddlDscx,0.00) as nAddlDscx " + //26                                                                                                                                                                                                                                                                       
                    " ,IFNULL(c.nPromoDsc,0.00) as nPromoDsc " + //27                                                                                                                                                                                                                                                                       
                    " ,IFNULL(c.nFleetDsc,0.00) as nFleetDsc " + //28                                                                                                                                                                                                                                                                       
                    " ,IFNULL(c.nUnitPrce,0.00) as nUnitPrce " + //29                                                                                                                                                                                                                                                                       
                    " ,IFNULL(i.sCompnyNm,'') AS sCompnyNm " +   //30                                                                                                                                                                                                                                                                      
                    " ,IFNULL((SELECT CONCAT( IFNULL( CONCAT(client_address.sAddressx,', ') , ''), IFNULL(CONCAT(barangay.sBrgyName,', '), ''), IFNULL(CONCAT(TownCity.sTownName, ', '),''), IFNULL(CONCAT(Province.sProvName, ', ' ),'')) FROM client_address " +																																			
                    "  LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx " +                                                                                                                                                                                                                                         
                    "  LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx " +                                                                                                                                                                                                                                               
                    "  LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx " +                                                                                                                                                                                                                                               
                    "  WHERE client_address.sClientID = h.sClientID AND client_address.cPrimaryx = 1 AND client_address.cRecdStat = 1 " +                                                                                                                                                                                              
                    "  LIMIT 1), '') as sAddressx " + //31  
                    " ,b.cCustType " + //32
                    " ,i.sTaxIDNox " + //33
                    " ,'' as sRemarksx " + //34
                " FROM si_master a " +                                                                                                                                                                                                                                                                                             
                " LEFT JOIN udr_master b ON b.sTransNox = a.sSourceCd " +                                                                                                                                                                                                                                                          
                " LEFT JOIN vsp_master c on c.sTransNox = b.sSourceCd " +                                                                                                                                                                                                                                                          
                " LEFT JOIN vehicle_serial d ON d.sSerialID = b.sSerialID  " +                                                                                                                                                                                                                                                     
                " LEFT JOIN vehicle_serial_registration e ON e.sSerialID = b.sSerialID  " +                                                                                                                                                                                                                                        
                " LEFT JOIN vehicle_master f on f.sVhclIDxx = d.sVhclIDxx " +                                                                                                                                                                                                                                                       
                " LEFT JOIN vehicle_color g ON G.sColorIDx = f.sColorIDx  " +                                                                                                                                                                                                                                                       
                " LEFT JOIN customer_inquiry h on h.sTransNox = c.sInqryIDx  " +                                                                                                                                                                                                                                                   
                " LEFT JOIN client_master i on i.sClientID = a.sClientID" ;
    }
    
    private String getSQ_SiSource(){
        return "";
    }
    
    private String getSQ_SearchUdr(){
        return " SELECT " + 
                    " a.sTransNox " + 
                    " ,a.dTransact " + 
                    " ,a.sReferNox " + 
                    " ,a.sClientID " + 
                    " ,a.sSerialID " + 
                    " ,a.sSourceCD " + 
                    " ,a.sSourceNo " + 
                    " ,a.cCallStat " + 
                    " ,a.cTranStat " + 
                    " , IFNULL(e.sDescript,'') as sDescript " +																																						
                    " , IFNULL(c.sCSNoxxxx,'') as sCSNoxxxx " +																																						
                    " , IFNULL(d.sPlateNox,'') as sPlateNox " +																																					
                    " , IFNULL(c.sFrameNox,'') as sFrameNox " +																																					
                    " , IFNULL(c.sEngineNo,'') as sEngineNo " +
                    " ,IFNULL((SELECT IFNULL(cm.sCompnyNm, '') sCompnyNm " +
                    "  FROM ggc_isysdbf.employee_master001  " +                                    
                    "  LEFT JOIN ggc_isysdbf.client_master cm ON cm.sClientID = employee_master001.sEmployID " +                    
                    "  LEFT JOIN ggc_isysdbf.department dep ON dep.sDeptIDxx = employee_master001.sDeptIDxx " +                        
                    "  LEFT JOIN ggc_isysdbf.branch_others bo ON bo.sBranchCD = employee_master001.sBranchCd " +                    
                    "  WHERE (dep.sDeptIDxx = 'a011' or dep.sDeptIDxx = '015') AND ISNULL(employee_master001.dFiredxxx) AND " +       
                    "  bo.cDivision = (SELECT cDivision FROM ggc_isysdbf.branch_others WHERE sBranchCd = b.sBranchCD " +
                    " ) AND employee_master001.sEmployID =  g.sEmployID), '') AS sSalesExe " +  
                    " ,g.sEmployID " +
                    " ,IFNULL(b.nAddlDscx,0.00) as nAddlDscx " +
                    " ,IFNULL(b.nPromoDsc,0.00) as nPromoDsc " +
                    " ,IFNULL(b.nFleetDsc,0.00) as nFleetDsc " +
                    " ,IFNULL(b.nUnitPrce,0.00) as nUnitPrce " +
                    " ,IFNULL(h.sCompnyNm,'') AS sCompnyNm " +
                    " ,IFNULL((SELECT CONCAT( IFNULL( CONCAT(client_address.sAddressx,', ') , ''), IFNULL(CONCAT(barangay.sBrgyName,', '), ''), IFNULL(CONCAT(TownCity.sTownName, ', '),''), IFNULL(CONCAT(Province.sProvName, ', ' ),'')) FROM client_address " +																																			
                    "  LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx " +
                    "  LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx " +
                    "  LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx " +
                    "  WHERE client_address.sClientID = h.sClientID AND client_address.cPrimaryx = 1 AND client_address.cRecdStat = 1  " +                            
                    "  LIMIT 1), '') as sAddressx " +
                    " , IFNULL(f.sColorDsc ,'') as sColorDsc" +
                    " ,a.cCustType " +
                    " ,b.sBranchCD " +
                    " ,h.sTaxIDNox " +
                " FROM udr_master a " +
                " LEFT JOIN vsp_master b on b.sTransNox = a.sSourceCd " +
                " LEFT JOIN vehicle_serial c ON c.sSerialID = a.sSerialID " +
                " LEFT JOIN vehicle_serial_registration d ON d.sSerialID = a.sSerialID " +
                " LEFT JOIN vehicle_master e on e.sVhclIDxx = c.sVhclIDxx " +
                " LEFT JOIN vehicle_color f ON f.sColorIDx = e.sColorIDx " +
                " LEFT JOIN customer_inquiry g on g.sTransNox = b.sInqryIDx " +
                " LEFT JOIN client_master h on h.sClientID = a.sClientID ";
    }
    
    public boolean searchUDR (String fsValue, String fsType) throws SQLException{
        String lsSQL = getSQ_SearchUdr();
        
        lsSQL = lsSQL + " WHERE a.sReferNox LIKE " + SQLUtil.toSQL(fsValue + "%") +
                        " AND a.cTranStat = '1' AND cCustType = " + SQLUtil.toSQL(fsType)   +
                        " GROUP BY a.sTransNox " ;
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sSourceCd", loRS.getString("sTransNox"));
                setMaster("sSourceNo", loRS.getString("sReferNox"));
                setMaster("sClientID", loRS.getString("sClientID"));
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
                setMaster("sPlateNox", loRS.getString("sPlateNox"));
                setMaster("sFrameNox", loRS.getString("sFrameNox"));
                setMaster("sEngineNo", loRS.getString("sEngineNo"));
                setMaster("sColorDsc", loRS.getString("sColorDsc"));
                setMaster("sSalesExe", loRS.getString("sSalesExe"));                              
                setMaster("nAddlDscx", Double.valueOf(loRS.getString("nAddlDscx")));
                setMaster("nPromoDsc", Double.valueOf(loRS.getString("nPromoDsc")));
                setMaster("nFleetDsc", Double.valueOf(loRS.getString("nFleetDsc")));
                setMaster("nUnitPrce", Double.valueOf(loRS.getString("nUnitPrce")));                
                setMaster("nDiscount", (Double.valueOf(loRS.getString("nAddlDscx")) +
                                                Double.valueOf(loRS.getString("nPromoDsc")) +
                                                Double.valueOf(loRS.getString("nFleetDsc"))));
                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                setMaster("sAddressx", loRS.getString("sAddressx"));
                setMaster("sBranchCD", loRS.getString("sBranchCD"));
                setMaster("sTaxIDNox", loRS.getString("sTaxIDNox"));
            } else {
                psMessage = "No record found.";
                setMaster("sSourceCd", "");
                setMaster("sSourceNo", "");
                setMaster("sClientID", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sColorDsc", "");
                setMaster("sSalesExe", "");
                setMaster("nAddlDscx", "");
                setMaster("nPromoDsc", "");
                setMaster("nFleetDsc", "");
                setMaster("nUnitPrce", "");
                setMaster("nDiscount", "");
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sBranchCD", "");
                setMaster("sTaxIDNox", "");
                return false;
            }           
        } else {
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             lsSQL,
                                             "",
                                             "UDR No»Customer Name", 
                                             "sReferNox»sCompnyNm",
                                             "sReferNox»sCompnyNm",
                                             0);
            
            if (loJSON != null){
                setMaster("sSourceCd", (String) loJSON.get("sTransNox"));
                setMaster("sSourceNo", (String) loJSON.get("sReferNox"));
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
                setMaster("sColorDsc", (String) loJSON.get("sColorDsc"));
                setMaster("sSalesExe", (String) loJSON.get("sSalesExe"));
                setMaster("nAddlDscx", Double.valueOf((String)  loJSON.get("nAddlDscx")));
                setMaster("nPromoDsc", Double.valueOf((String)  loJSON.get("nPromoDsc")));
                setMaster("nFleetDsc", Double.valueOf((String)  loJSON.get("nFleetDsc")));
                setMaster("nUnitPrce", Double.valueOf((String)  loJSON.get("nUnitPrce")));
                setMaster("nDiscount", (Double.valueOf((String)  loJSON.get("nAddlDscx")) +
                                                Double.valueOf((String)  loJSON.get("nPromoDsc")) +
                                                Double.valueOf((String)  loJSON.get("nFleetDsc"))));
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sAddressx", (String) loJSON.get("sAddressx"));
                setMaster("sBranchCD", (String) loJSON.get("sBranchCD"));
                setMaster("sTaxIDNox", (String) loJSON.get("sTaxIDNox"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sSourceCd", "");
                setMaster("sSourceNo", "");
                setMaster("sClientID", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sColorDsc", "");
                setMaster("sSalesExe", "");
                setMaster("nAddlDscx", "");
                setMaster("nPromoDsc", "");
                setMaster("nFleetDsc", "");
                setMaster("nUnitPrce", "");
                setMaster("nDiscount", "");
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sBranchCD", "");
                setMaster("sTaxIDNox", "");
                return false;    
            }
        } 
        
        return true;
    }
    
    public boolean computeAmount() throws SQLException{
        psMessage = "";
        String ls_FormType = (String) getMaster("cFormType");
        double ldbl_bpvatp = 0.00;
        double ldbl_VatRatex = 0.00;
        double ldbl_DiscAmt = 0.00; 
        double ldbl_BasePrice = 0.00;
        double ldbl_VatAmtxx = 0.00;
        double ldbl_TranTotl = 0.00;
        
        //get current vat rate from standard_sets
        String lsSQL = getSQ_StandardSets();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL, "sDescript = 'baseprice_with_vat_percent'");
        loRS = poGRider.executeQuery(lsSQL);
        if (loRS.next()) {               
            ldbl_bpvatp = loRS.getDouble("sValuexxx"); 
        }
        //get
        lsSQL = "";
        lsSQL = getSQ_StandardSets();
        lsSQL = MiscUtil.addCondition(lsSQL, "sDescript = 'vat_percent'");
        loRS = poGRider.executeQuery(lsSQL);
        if (loRS.next()) {               
            ldbl_VatRatex = loRS.getInt("sValuexxx");           
        }
        //set vatp value in si
        setMaster("nVatRatex",ldbl_VatRatex);
                                                             
        //Amount to be Pay
        double ldbl_UnitPrce = (Double) getMaster("nUnitPrce");
        
        //Discounted Amount
//        double ldblAddlDscx = (Double) getMaster("nAddlDscx");
//        double ldblPromoDsc = (Double) getMaster("nPromoDsc"); 
//        double ldblFleetDsc = (Double) getMaster("nFleetDsc");
        
        //ldbl_DiscAmt = ldblAddlDscx + ldblPromoDsc + ldblFleetDsc;
        ldbl_DiscAmt = (Double) getMaster("nDiscount");
        
        /*1. get final ldbl_UnitPrce value */
        if (ls_FormType == "1") {
            ldbl_UnitPrce = ldbl_UnitPrce - ldbl_DiscAmt;
        }
        /*2. Compute for the Base Price and VAT Amount 
		using ie vat of 12%
		
		given: ldbl_vhclsrp (vehicle srp) as vat inclusive srp
				112% as equivalent percentage value for this vat inclusive price
				100% as equivalent percentage value for base price (no vat yet)
				base price (no vat yet) = ? 
		principle of ratio: <base price> : 100% = <vat inclusive srp> : 112% (100% + 12% vat)*/
        if (ldbl_bpvatp > 0.00) {
            //compute the base price (no vat srp)
            ldbl_BasePrice = ldbl_UnitPrce / (ldbl_bpvatp/100); 
            //compute the ie 12% vat amount from the base price (no vat srp)
            ldbl_VatAmtxx = ldbl_BasePrice * (ldbl_VatRatex/100); 
        }
        
        /*3. Compute for Final Sales Amount 	
	 (base price (no vat srp) + vatamt) should be equal to original value of uprice uprice (vehicle srp) as vat inclusive price */	 	
	if (ls_FormType == "1"){  //computation for non-dealer/supplier sales
            ldbl_TranTotl = ldbl_BasePrice + ldbl_VatAmtxx;
        }else{
            //deduct discounts from end result only
            ldbl_TranTotl = (ldbl_BasePrice + ldbl_VatAmtxx) - ldbl_DiscAmt ;
        }
        
        setMaster("nVatAmtxx",ldbl_VatAmtxx);
        setMaster("nTranTotl",ldbl_TranTotl);
        return true;
    }
    
    private String getSQ_StandardSets(){
        return  "SELECT " +
                " IFNULL(sValuexxx,'') sValuexxx " +
                " FROM xxxstandard_sets ";
    }
    
    public boolean CancelInvoice(String fsValue) throws SQLException {
        if (pnEditMode != EditMode.READY) {
            psMessage = "Invalid update mode detected.";
            return false;
        }

        psMessage = "";

        if (((String) getMaster("cTranStat")).equals("0")) {
//            if (!(MAIN_OFFICE.contains(p_oApp.getBranchCode()) &&
//                p_oApp.getDepartment().equals(AUDITOR))){
//                p_sMessage = "Only CM Department can cancel confirmed transactions.";
//                return false;
//            } else {
//                if ("1".equals((String) getMaster("cApprovd2"))){
//                    p_sMessage = "This transaction was already CM Confirmed. Unable to disapprove.";
//                    return false;
//                }
//            }
        }

        String lsSQL = "UPDATE " + MASTER_TABLE + " SET"
                + " cTranStat = '0'"
                + " WHERE sTransNox = " + SQLUtil.toSQL(fsValue);

        if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }

        //pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    private boolean isEntryOK() throws SQLException{
        poMaster.first();
        
        if (poMaster.getString("sReferNox").isEmpty()){
            psMessage = "Sales Invoice Number is not set.";
            return false;
        }
//       
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = lsSQL + " WHERE a.sSourceNo = " + SQLUtil.toSQL(poMaster.getString("sSourceNo")) +
                                                " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox")) +
                                                " AND a.cTranStat <> '0'";
//        lsSQL = MiscUtil.addCondition(lsSQL, "a.sReferNox = " + SQLUtil.toSQL(poMaster.getString("sReferNox")) +
//                                                " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox"))); 
        loRS = poGRider.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Sales Invoice Found.";
            MiscUtil.close(loRS);        
            return false;
        }
//                   
        return true;
    }
}
