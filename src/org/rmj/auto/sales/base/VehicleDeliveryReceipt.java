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
 * Date Created: 07-20-2023
 */
public class VehicleDeliveryReceipt {
    private final String MASTER_TABLE = "udr_master";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
    
    public VehicleDeliveryReceipt(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
            case 3://sReferNox  
            case 4://sClientID
            case 5://sSerialID                  
            case 6://sREmarksx  
            case 10://sPONoxxxx
            case 11://sSourceCD
            case 12://sSourceNo
            case 14://sPrepared
            case 15://sApproved
            case 22://sCompnyNm
            case 23://sAddressx
            case 24://sDescript
            case 25://sCSNoxxxx
            case 26://sPlateNox
            case 27://sFrameNox
            case 28://sEngineNo
            case 29://sVSPNOxxx 
            case 30://cIsVhclNw
            case 31://sInqryIDx
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 2://dTransact 
                 if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));  
                break; 
            case 17://cTranStat
            case 13://cPrintedx 
            case 16://cCallStat
            //case 30://cIsVhclNw
                if (foValue instanceof Integer)
                    poMaster.updateInt(fnIndex, (int) foValue);
                else 
                    poMaster.updateInt(fnIndex, 0);
                
                poMaster.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));               
                break;
            case 7: //nGrossAmt   
            case 8: //nDiscount  
            case 9: //nTranTotl  
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
            String lsSQL = (getSQ_Master()+  "WHERE 0=1");
            System.out.println(lsSQL);
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
            poMaster.insertRow();
            poMaster.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    public boolean searchRecord(String fsValue) throws SQLException{
//        String lsSQL = getSQ_Master();
//        ResultSet loRS;
//        //loRS = poGRider.executeQuery(lsSQL);
//        JSONObject loJSON = showFXDialog.jsonSearch(poGRider
//                                                    , lsSQL
//                                                    , fsValue
//                                                    , "UDR No»Customer Name"
//                                                    , "a.sReferNox»c.sCompnyNm"
//                                                    , "sReferNox»sCompnyNm"
//                                                    , 0);
//        
//       
//        if (loJSON == null){
//            psMessage = "No record found/selected.";
//            return false;
//        } else {
//            if (OpenRecord((String) loJSON.get("sTransNox")) ){
//            }else {
//                psMessage = "No record found/selected.";
//                return false;
//            }
//        }               
//        return true;
        if (poGRider == null) {
            psMessage = "Application driver is not set.";
            return false;
        }

        psMessage = "";

        String lsSQL = getSQ_Master();

        if (pbWithUI) {
            JSONObject loJSON = showFXDialog.jsonSearch(
                    poGRider,
                    lsSQL,
                    fsValue,
                    "UDR No»Customer Name",
                    "a.sReferNox»sCompnyNm",
                    "sReferNox»sCompnyNm",
                    0);

            if (loJSON != null) {
                return OpenRecord((String) loJSON.get("sTransNox"));
            } else {
                psMessage = "No record selected.";
                return false;
            }
        }
              
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (!loRS.next()) {
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }

        lsSQL = loRS.getString("sTransNox");
        MiscUtil.close(loRS);

        return OpenRecord(lsSQL);
    }
    
    public boolean OpenRecord(String fsValue){
        try {
            String lsSQL = "";
            lsSQL = (getSQ_Master()+ "WHERE a.sTransNox = " + SQLUtil.toSQL(fsValue));
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
            String lsTransNox = MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", false, poGRider.getConnection(), psBranchCd);
            if (pnEditMode == EditMode.ADDNEW){ //add                
                System.out.println(MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", false, poGRider.getConnection(), psBranchCd) );
                //return true;
                poMaster.updateString("sTransNox",lsTransNox);                  
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sPrepared", poGRider.getUserID());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sVSPNOxxx»cIsVhclNw»sInqryIDx");
                
            } else { //update  
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sCompnyNm»sAddressx»sDescript»sCSNoxxxx»sPlateNox»sFrameNox»sEngineNo»sVSPNOxxx»cIsVhclNw»sInqryIDx", 
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
            //Update customer_inquiry status to sold and vehicle status to sold
            if (pnEditMode == EditMode.ADDNEW){
                lsSQL = "UPDATE customer_inquiry SET" +
                            " cTranStat = '4'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL((String) getMaster("sInqryIDx"));

                if (poGRider.executeQuery(lsSQL, "customer_inquiry", psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
                } 
                
                lsSQL = "UPDATE vehicle_serial SET" +
                            " cSoldStat = '3'" +
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
    
    private String getSQ_Master(){
        return " SELECT " + 
                "  a.sTransNox " + //1
                ", a.dTransact " + //2
                ", a.sReferNox " + //3udrno
                ", a.sClientID " + //4
                ", a.sSerialID " + //5
                ", a.sRemarksx " + //6
                ", a.nGrossAmt " + //7
                ", a.nDiscount " + //8
                ", a.nTranTotl " + //9
                ", a.sPONoxxxx " + //10
                ", a.sSourceCD " + //11
                ", a.sSourceNo " + //12
                ", a.cPrintedx " + //13
                ", a.sPrepared " + //14
                ", a.sApproved " + //15
                ", a.cCallStat " + //16
                ", a.cTranStat " + //17
                ", a.sEntryByx " + //18
                ", a.dEntryDte " + //19
                ", a.sModified " + //20
                ", a.dModified " + //21
                ", IFNULL(c.sCompnyNm,'') as sCompnyNm " +//22
                ", (SELECT IFNULL(CONCAT( IFNULL( CONCAT(client_address.sAddressx,', ') , ''), IFNULL(CONCAT(barangay.sBrgyName,', '), ''), IFNULL(CONCAT(TownCity.sTownName, ', '),''), IFNULL(CONCAT(Province.sProvName, ', ' ),'')), '') FROM client_address" +
                                " LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx" +
                                " LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx" +
                                " LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx" +
                                " WHERE client_address.sClientID = a.sClientID and client_address.cPrimaryx = 1 and client_address.cRecdStat = 1" +                              
                                " limit 1) AS sAddressx " +//23
                ", IFNULL(f.sDescript,'') as sDescript " +//24
                ", IFNULL(d.sCSNoxxxx,'') as sCSNoxxxx " +//25
                ", IFNULL(e.sPlateNox,'') as sPlateNox " +//26
                ", IFNULL(d.sFrameNox,'') as sFrameNox " +//27
                ", IFNULL(d.sEngineNo,'') as sEngineNo " +//28
                ", b.sVSPNOxxx " + //29
                ", b.cIsVhclNw " + //30
                ", b.sInqryIDx " + //31
            " FROM udr_master a " +
            " LEFT JOIN vsp_master b ON b.sTransNox = a.sSourceCD " +
            " LEFT JOIN client_master c ON c.sClientID = a.sClientID " +
            " LEFT JOIN vehicle_serial d ON d.sSerialID = b.sSerialID " +
            " LEFT JOIN vehicle_serial_registration e ON e.sSerialID = b.sSerialID "+
            " LEFT JOIN vehicle_master f ON f.sVhclIDxx = d.sVhclIDxx  " ;
    }
    
    private String getSQ_searchVSP(){
        return " SELECT "																																						
                    + " a.sTransNox "																																					
                    + " , a.dTransact "																																				
                    + " , a.sVSPNOxxx "																																			
                    + " , IFNULL(b.sCompnyNm,'') AS sCompnyNm "																																					
                    + " , (SELECT IFNULL(TRIM(CONCAT(client_address.sAddressx, ', ',barangay.sBrgyName, ', ', TownCity.sTownName, ', ', Province.sProvName)), '') FROM client_address "
                    + " LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx "
                    + " LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx "
                    + " LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx "
                    + " WHERE client_address.sClientID = a.sClientID AND client_address.cPrimaryx = 1 AND client_address.cRecdStat = 1 "                             
                    + " LIMIT 1) AS sAddressx "																																						
                    + " , IFNULL(e.sDescript,'') AS sDescript "																																						
                    + " , IFNULL(c.sCSNoxxxx,'') AS sCSNoxxxx "																																						
                    + " , IFNULL(d.sPlateNox,'') AS sPlateNox "																																						
                    + " , IFNULL(c.sFrameNox,'') AS sFrameNox "																																						
                    + " , IFNULL(c.sEngineNo,'') AS sEngineNo "
                    + " , a.sClientID"
                    + " , a.sSerialID"
                    + " , a.cIsVhclNw"
                    + " , a.sInqryIDx"
                + " FROM vsp_master a " 																																			
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID " 																																					
                + " LEFT JOIN vehicle_serial c ON c.sSerialID = a.sSerialID "																																					
                + " LEFT JOIN vehicle_serial_registration d ON d.sSerialID = c.sSerialID " 
                + " LEFT JOIN vehicle_master e ON e.sVhclIDxx = c.sVhclIDxx "  ;

//        return  " SELECT " + 
//                " a.sTransNox " +
//                ", a.dTransact " +
//                ", a.sVSPNOxxx " +
//                ", IFNULL(d.sCompnyNm,'') as sCompnyNm " +
//                ", (SELECT IFNULL(TRIM(CONCAT(client_address.sAddressx, ', ', barangay.sBrgyName, ', ', TownCity.sTownName, ', ', Province.sProvName)), '') FROM client_address" +
//                                " LEFT JOIN TownCity ON TownCity.sTownIDxx = client_address.sTownIDxx" +
//                                " LEFT JOIN barangay ON barangay.sTownIDxx = TownCity.sTownIDxx" +
//                                " LEFT JOIN Province ON Province.sProvIDxx = TownCity.sProvIDxx" +
//                                " WHERE client_address.sClientID = a.sClientID and client_address.cPrimaryx = 1 and client_address.cRecdStat = 1" +                              
//                                " limit 1) AS sAddressx " +
//                ", IFNULL(g.sDescript,'') as sDescript " +
//                ", IFNULL(e.sCSNoxxxx,'') as sCSNoxxxx " +
//                ", IFNULL(f.sPlateNox,'') as sPlateNox " +
//                ", IFNULL(e.sFrameNox,'') as sFrameNox " +
//                ", IFNULL(e.sEngineNo,'') as sEngineNo " +
//                " FROM vsp_master a " +
//                " LEFT JOIN client_master b ON b.sClientID = a.sClientID " +
//                " LEFT JOIN vehicle_serial c ON c.sSerialID = a.sSerialID "+
//                " LEFT JOIN vehicle_serial_registration d ON d.sSerialID = c.sSerialID "+
//                " LEFT JOIN vehicle_master e ON e.sVhclIDxx = c.sVhclIDxx " ;
    }
    
    private String getSQ_searchAvlVhcl(){
        return  "SELECT" + //
                "   IFNULL(a.sSerialID,'') sSerialID " + //1
                " , IFNULL(a.sBranchCD,'') sBranchCD " + //2
                " , IFNULL(a.sFrameNox,'') sFrameNox " + //3
                " , IFNULL(a.sEngineNo,'') sEngineNo " + //4
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx " + //5
                " , IFNULL(a.sClientID,'') sClientID " + //6
                " , IFNULL(a.sCoCltIDx,'') sCoCltIDx " + //7
                " , IFNULL(a.sCSNoxxxx,'') sCSNoxxxx " + //8
                " , IFNULL(a.sDealerNm,'') sDealerNm " + //9
                " , IFNULL(a.sCompnyID,'') sCompnyID " + //10
                " , IFNULL(a.sKeyNoxxx,'') sKeyNoxxx " + //11
                " , IFNULL(a.cIsDemoxx,'') cIsDemoxx " + //12
                " , IFNULL(a.cLocation,'') cLocation " + //13
                " , IFNULL(a.cSoldStat,'') cSoldStat " + //14
                " , IFNULL(a.cVhclNewx,'') cVhclNewx " + //15
                " , a.sEntryByx " + //16
                " , a.dEntryDte " + //17
                " , a.sModified " + //18
                " , a.dModified " + //19
                " , IFNULL(b.sPlateNox,'') sPlateNox " + //20
                " , IFNULL(b.dRegister,CAST('1900-01-01' AS DATE)) dRegister " + //21
                " , IFNULL(b.sPlaceReg,'') sPlaceReg " + //22
                " , IFNULL(c.sMakeIDxx,'') sMakeIDxx " + //23
                " , IFNULL(d.sMakeDesc,'') sMakeDesc " + //24 
                " , IFNULL(c.sModelIDx,'') sModelIDx " + //25
                " , IFNULL(e.sModelDsc,'') sModelDsc " + //26   
                " , IFNULL(c.sTypeIDxx,'') sTypeIDxx " + //27 
                " , IFNULL(f.sTypeDesc,'') sTypeDesc " + //28   
                " , IFNULL(c.sColorIDx,'') sColorIDx " + //29 
                " , IFNULL(g.sColorDsc,'') sColorDsc " + //30
                " , IFNULL(c.sTransMsn,'') sTransMsn " + //31
                " , IFNULL(c.nYearModl,'') nYearModl " + //32
                " , IFNULL(c.sDescript,'') sDescript " + //33
                " , IFNULL(a.sRemarksx,'') sRemarksx " + //34
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
    public boolean searchAvailableVhcl(String fsValue, boolean fbValue) throws SQLException{
        String lsSQL = getSQ_searchAvlVhcl();
        
        lsSQL = MiscUtil.addCondition(lsSQL, " a.cSoldStat = '1' " +
                                                 " AND (a.sCSNoxxxx LIKE " + SQLUtil.toSQL(fsValue + "%")  +
                                                 " OR b.sPlateNox LIKE " + SQLUtil.toSQL(fsValue + "%") + " ) " );
        System.out.println(lsSQL);
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
                setMaster("sPlateNox", loRS.getString("sPlateNox"));
                setMaster("sFrameNox", loRS.getString("sFrameNox"));
                setMaster("sEngineNo", loRS.getString("sEngineNo"));
            } else {
                psMessage = "No record found.";
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle for Sale", "sSerialID");
            
            if (loJSON != null){
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                return false;    
            }
        } 
         
        return true;
    }
    
    public boolean searchVSP(String fsValue) throws SQLException{
        String lsSQL = getSQ_searchVSP();
//        lsSQL = (MiscUtil.addCondition(lsSQL, " sVSPNOxxx LIKE " + SQLUtil.toSQL(fsValue + "%"))  +
//                                                  " AND cRecdStat = '1'  "  +
//                                                  " GROUP BY sTransNox " );
        
        lsSQL = lsSQL + " WHERE a.sVSPNOxxx LIKE " + SQLUtil.toSQL(fsValue + "%") +
                        " AND a.cTranStat = '1'  "  +
                        " GROUP BY a.sTransNox " ;
        
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sVSPNOxxx", loRS.getString("sVSPNOxxx"));
                setMaster("sCompnyNm", loRS.getString("sCompnyNm"));
                setMaster("sAddressx", loRS.getString("sAddressx"));
                setMaster("sDescript", loRS.getString("sDescript"));
                setMaster("sCSNoxxxx", loRS.getString("sCSNoxxxx"));
                setMaster("sPlateNox", loRS.getString("sPlateNox"));
                setMaster("sFrameNox", loRS.getString("sFrameNox"));
                setMaster("sEngineNo", loRS.getString("sEngineNo"));
                setMaster("sClientID", loRS.getString("sClientID"));
                setMaster("sSerialID", loRS.getString("sSerialID"));
                setMaster("cIsVhclNw", loRS.getString("cIsVhclNw"));
                setMaster("sSourceCD", loRS.getString("sTransNox"));
                setMaster("sSourceNo", loRS.getString("sVSPNOxxx"));
                setMaster("sInqryIDx", loRS.getString("sInqryIDx"));                
            } else {
                psMessage = "No record found.";
                setMaster("sVSPNOxxx", "");
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sClientID", "");
                setMaster("sSerialID", "");
                setMaster("cIsVhclNw", "");
                setMaster("sSourceCD", "");
                setMaster("sSourceNo", "");
                setMaster("sInqryIDx", "");
                return false;
            }           
        } else {
            //loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonSearch(poGRider, 
                                             getSQ_searchVSP(),
                                             "%" + fsValue +"%",
                                             "VSP No»Customer Name»CS NO»Plate no", 
                                             "sVSPNOxxx»sCompnyNm»sCSNoxxxx»sPlateNox",
                                             "sVSPNOxxx»sCompnyNm»sCSNoxxxx»sPlateNox",
                                             0);
            
            if (loJSON != null){
                setMaster("sVSPNOxxx", (String) loJSON.get("sVSPNOxxx"));
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
                setMaster("sAddressx", (String) loJSON.get("sAddressx"));
                setMaster("sDescript", (String) loJSON.get("sDescript"));
                setMaster("sCSNoxxxx", (String) loJSON.get("sCSNoxxxx"));
                setMaster("sPlateNox", (String) loJSON.get("sPlateNox"));
                setMaster("sFrameNox", (String) loJSON.get("sFrameNox"));
                setMaster("sEngineNo", (String) loJSON.get("sEngineNo"));
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("sSerialID", (String) loJSON.get("sSerialID"));
                setMaster("cIsVhclNw", (String) loJSON.get("cIsVhclNw"));
                setMaster("sSourceCD", (String) loJSON.get("sTransNox"));
                setMaster("sSourceNo", (String) loJSON.get("sVSPNOxxx"));
                setMaster("sInqryIDx", (String) loJSON.get("sInqryIDx"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sVSPNOxxx", "");
                setMaster("sCompnyNm", "");
                setMaster("sAddressx", "");
                setMaster("sDescript", "");
                setMaster("sCSNoxxxx", "");
                setMaster("sPlateNox", "");
                setMaster("sFrameNox", "");
                setMaster("sEngineNo", "");
                setMaster("sClientID", "");
                setMaster("sSerialID", "");
                setMaster("cIsVhclNw", "");
                setMaster("sSourceCD", "");
                setMaster("sSourceNo", "");
                setMaster("sInqryIDx", "");
                return false;    
            }
        } 
        
        return true;
    }
    
    private boolean isEntryOK() throws SQLException{
        poMaster.first();
        
        if (poMaster.getString("sReferNox").isEmpty()){
            psMessage = "Delivery Receipt Number is not set.";
            return false;
        }
       
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = lsSQL + " WHERE a.sReferNox = " + SQLUtil.toSQL(poMaster.getString("sReferNox")) +
                                                " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox"));
//        lsSQL = MiscUtil.addCondition(lsSQL, "a.sReferNox = " + SQLUtil.toSQL(poMaster.getString("sReferNox")) +
//                                                " AND a.sTransNox <> " + SQLUtil.toSQL(poMaster.getString("sTransNox"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Delivery Receipt Number.";
            MiscUtil.close(loRS);        
            return false;
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

