/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.clients.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;

/**
 *
 * @author Arsiela
 * Date Created: 05-31-2023
 */
public class ClientVehicleInfo {
    private final String MASTER_TABLE = "vehicle_serial";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private String psSerialID;
    
    private CachedRowSet poVehicle;
    private CachedRowSet poOriginalVehicle;
    
    public ClientVehicleInfo(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
        poVehicle.last();
        return poVehicle.getRow();
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poVehicle.first();
        
        switch (fnIndex){            
            case 2://
                poVehicle.updateObject(fnIndex, (String) foValue);
                poVehicle.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
                                                            
        }
    }
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poVehicle, fsIndex), foValue);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poVehicle, fsIndex));
    }
    
    public Object getMaster(int fnIndex) throws SQLException{
        poVehicle.first();
        return poVehicle.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poVehicle.absolute(fnRow);
        return poVehicle.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poVehicle, fsIndex));
    }
    
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poVehicle = factory.createCachedRowSet();
            poVehicle.populate(loRS);
            MiscUtil.close(loRS);
            
            poVehicle.last();
            poVehicle.moveToInsertRow();
            
            MiscUtil.initRowSet(poVehicle);       
            poVehicle.updateString("cRecdStat", RecordStatus.ACTIVE);    
            poVehicle.insertRow();
            poVehicle.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    //for autoloading list of vehicle make
    public boolean LoadList() throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        loRS = poGRider.executeQuery(getSQ_Master());
        poVehicle = factory.createCachedRowSet();
        poVehicle.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    public boolean OpenRecord(String fsValue){
        if (poVehicle == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "sSerialID = " + SQLUtil.toSQL(fsValue));
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poVehicle = factory.createCachedRowSet();
            poVehicle.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }    
    
    public boolean UpdateRecord(){
        try {
            if (poVehicle != null){
                poOriginalVehicle = (CachedRowSet) poVehicle.createCopy();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientVehicleInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            psSerialID = "";
            if (pnEditMode == EditMode.ADDNEW){ //add
                psSerialID = MiscUtil.getNextCode(MASTER_TABLE, "sSerialID", true, poGRider.getConnection(), psBranchCd) ; 
                poVehicle.updateString("sSerialID", psSerialID );                                                             
                poVehicle.updateString("sEntryByx", poGRider.getUserID());
                poVehicle.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, MASTER_TABLE, "");
            } else { //update  
                psSerialID = (String) getMaster("sSerialID") ;
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, 
                                            MASTER_TABLE, 
                                            "", 
                                            "sSerialID = " + SQLUtil.toSQL(psSerialID));
            }
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }
            
            /*PROCEED TO: vehicle_serial saving*/
            if (!pbWithParent) poGRider.beginTrans();
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                psMessage = poGRider.getErrMsg();
                if (!pbWithParent) poGRider.rollbackTrans();
                return false;
            }
            if (!pbWithParent) poGRider.commitTrans();
            
            /*PROCEED TO: vehicle_serial_registration saving*/
            String lsPlateNox, lsDRegs, lsPlcRegs;
            lsPlateNox = (String) getMaster("sPlateNox");
            lsDRegs = (String) getMaster("dRegister");
            lsPlcRegs = (String) getMaster("sPlaceReg");
            
            if ((lsPlateNox != null) || ((lsDRegs != null) && !lsDRegs.equals("1900-01-01")) || (lsPlcRegs != null) ) {
                if (pnEditMode == EditMode.ADDNEW){ //add
                    lsSQL = "INSERT INTO vehicle_serial_registration  " +
                            "(sSerialID,sPlateNox,dRegister,sPlaceReg,sEntryByx,dEntryDte,sModified,dModified)" +
                            " VALUES (" + SQLUtil.toSQL(psSerialID) +
                            "," + SQLUtil.toSQL(lsPlateNox) + ", " + SQLUtil.toSQL(lsDRegs) + "," + SQLUtil.toSQL(lsPlcRegs) +
                            "," + SQLUtil.toSQL((String) getMaster("sEntryByx")) + ", " + SQLUtil.toSQL((String) getMaster("dEntryDte")) + "," + SQLUtil.toSQL((String) getMaster("sModified")) + "," + SQLUtil.toSQL((String) getMaster("dModified")) +
                            ")";
                    if (poGRider.executeQuery(lsSQL, "vehicle_serial_registration", psBranchCd, "") <= 0){
                        psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                        return false;
                    }      
                    if (!pbWithParent) poGRider.commitTrans();     
                } else { //update  
                    lsSQL = "UPDATE vehicle_serial_registration SET" +
                            "  sPlateNox = " + SQLUtil.toSQL(lsPlateNox) + 
                            ", dRegister = " + SQLUtil.toSQL(lsDRegs) + 
                            ", sPlaceReg = " + SQLUtil.toSQL(lsPlcRegs) + 
                            ", sModified = " + SQLUtil.toSQL(poGRider.getUserID() ) + 
                            ", dModified = " + SQLUtil.toSQL((Date) poGRider.getServerDate() ) + 
                            " WHERE sSerialID = " + SQLUtil.toSQL(psSerialID);

                    if (poGRider.executeQuery(lsSQL, "vehicle_serial_registration", psBranchCd, "") <= 0){
                        psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                        return false;
                    }      
                    if (!pbWithParent) poGRider.commitTrans(); 
                }
            }
            
            // Update the original state of the table
            poOriginalVehicle = (CachedRowSet) poVehicle.createCopy();
                    
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    private String getSQ_Master(){
        return  "SELECT" + //1
                "   IFNULL(a.sSerialID,'') sSerialID " + //
                " , IFNULL(a.sBranchCD,'') sBranchCD " + //
                " , IFNULL(a.sFrameNox,'') sFrameNox " + //
                " , IFNULL(a.sEngineNo,'') sEngineNo " + //
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx " + //
                " , IFNULL(a.sClientID,'') sClientID " + //
                " , IFNULL(a.sCoCltIDx,'') sCoCltIDx " + //
                " , IFNULL(a.sCSNoxxxx,'') sCSNoxxxx " + //
                " , IFNULL(a.sDealerNm,'') sDealerNm " + //
                " , IFNULL(a.sCompnyID,'') sCompnyID " + //
                " , IFNULL(a.sKeyNoxxx,'') sKeyNoxxx " + //
                " , IFNULL(a.cIsDemoxx,'') cIsDemoxx " + //
                " , IFNULL(a.cLocation,'') cLocation " + //
                " , IFNULL(a.cSoldStat,'') cSoldStat " + //
                " , IFNULL(a.cVhclNewx,'') cVhclNewx " + //
                " , a.sEntryByx " + //
                " , a.dEntryDte " + //
                " , a.sModified " + //
                " , a.dModified " + //
                " , IFNULL(b.sPlateNox,'') sPlateNox " + //
                " , b.dRegister " + //
                " , IFNULL(b.sPlaceReg,'') sPlaceReg " + //
                "   FROM vehicle_serial a " + //
                "   LEFT JOIN vehicle_serial_registration b ON a.sSerialID = b.sSerialID ; " ;
    }
    
    private String getSQ_VhclReg(){
        return  "SELECT " +
                " sSerialID " +
                " , IFNULL(sCSRValNo,'') sCSRValNo " +
                " , IFNULL(sPNPClrNo,'') sPNPClrNo " +
                " , IFNULL(sCRNoxxxx,'') sCRNoxxxx " +
                " , IFNULL(sCRENoxxx,'') sCRENoxxx " +
                " , IFNULL(sRegORNox,'') sRegORNox " +
                " , IFNULL(sFileNoxx,'') sFileNoxx " +
                " , IFNULL(sPlateNox,'') sPlateNox " +
                " , dRegister " +
                " , IFNULL(sPlaceReg,'') sPlaceReg " +
                " , sEntryByx " +
                " , dEntryDte " +
                " , sModified " +
                " , dModified " +
                " FROM vehicle_serial_registration; " ;
    }
    private boolean isEntryOK() throws SQLException{
        poVehicle.first();

        if (poVehicle.getString("sVhclIDxx").isEmpty()){
            psMessage = "Vehicle is not set.";
            return false;
        }
        
        if (poVehicle.getString("sFrameNox ").isEmpty()){
            psMessage = "Frame Number is not set.";
            return false;
        }
        
        if (poVehicle.getString("sEngineNo").isEmpty()){
            psMessage = "Engine Number is not set.";
            return false;
        }
        
        //Validate if CS / Plate No is exist.
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL," ( ( sCSNoxxxx = " + SQLUtil.toSQL(poVehicle.getString("sCSNoxxxx")) + 
                                                " OR sPlateNox = " + SQLUtil.toSQL(poVehicle.getString("sCSNoxxxx")) + " ) " +
                                                " OR ( sCSNoxxxx = " + SQLUtil.toSQL(poVehicle.getString("sPlateNox")) + 
                                                " OR sPlateNox = " + SQLUtil.toSQL(poVehicle.getString("sPlateNox")) + " ) )" +
                                                " AND sSerialID <> " + SQLUtil.toSQL(poVehicle.getString("sSerialID"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "CS / Plate Number already exist.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        //Validate if Engine / Frame Number
        lsSQL = getSQ_Master();
        lsSQL = MiscUtil.addCondition(lsSQL," ( ( sFrameNox  = " + SQLUtil.toSQL(poVehicle.getString("sFrameNox")) + 
                                                " OR sEngineNo  = " + SQLUtil.toSQL(poVehicle.getString("sFrameNox")) + " ) " +
                                                " OR ( sFrameNox = " + SQLUtil.toSQL(poVehicle.getString("sEngineNo")) + 
                                                " OR sEngineNo = " + SQLUtil.toSQL(poVehicle.getString("sEngineNo")) + " ) )" +
                                                " AND sSerialID <> " + SQLUtil.toSQL(poVehicle.getString("sSerialID"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Engine / Frame Number already exist.";
            MiscUtil.close(loRS);        
            return false;
        }
                       
        return true;
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poVehicle.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poVehicle.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poVehicle.getMetaData().getColumnType(lnCtr));
            if (poVehicle.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poVehicle.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poVehicle.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    } 
    
    
}
