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
 * Date Created: 05-31-2023
 */
public class ClientVehicleInfo {
    private final String MASTER_TABLE = "vehicle_serial";
    private final String DEFAULT_DATE = "1900-01-01";
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private String psSerialID;
    private String psClientID;
    
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
    
    public void setClientID(String fsValue) {
        psClientID = fsValue;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poVehicle.first();
        
        switch (fnIndex){            
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 18:
            case 20:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
                poVehicle.updateObject(fnIndex, (String) foValue);
                poVehicle.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 17:
            case 19:
            case 21:
                if (foValue instanceof Date){
                    poVehicle.updateObject(fnIndex, foValue);
                } else {
                    poVehicle.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
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
    
    /**
     * Creates a new record in the vehicle table.
     * @return {@code true} if a new record is successfully created, {@code false} otherwise.
    */
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
            //poVehicle.updateString("cRecdStat", RecordStatus.ACTIVE);    
            poVehicle.insertRow();
            poVehicle.moveToCurrentRow();                        
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    /**
     * Loads the list of vehicles from the database base of client ID.
     * @return {@code true} if the list is successfully loaded, {@code false} otherwise.
     * @throws SQLException if a database error occurs.
    */
    public boolean LoadList(String fsValue) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL =  MiscUtil.addCondition(getSQ_Master(), "sClientID = " + SQLUtil.toSQL(fsValue));
        loRS = poGRider.executeQuery(lsSQL);
        poVehicle = factory.createCachedRowSet();
        poVehicle.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    /**
     * Opens the record of a specific vehicle identified by the given serial ID.
     * @param fsValue the serial ID of the vehicle to open.
     * @return {@code true} if the record is successfully opened, {@code false} otherwise.
    */
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
    /**
     * Sets the edit mode to UPDATE and creates a copy of the current vehicle record for comparison.
     * @return {@code true} if the edit mode is successfully set to UPDATE, {@code false} otherwise.
    */
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
    
    /**
     * Saves the record based on the current edit mode.
     * @return {@code true} if the record is successfully saved, {@code false} otherwise.
     * @throws SQLException if an error occurs while accessing the database.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            int lnCtr = 1;
            boolean lbisModified = false;
            String lsSQL = "";
            psSerialID = "";
            if (!pbWithParent) poGRider.beginTrans();
            if (pnEditMode == EditMode.ADDNEW){ //add
                psSerialID = MiscUtil.getNextCode(MASTER_TABLE, "sSerialID", true, poGRider.getConnection(), psBranchCd) ; 
                poVehicle.updateString("sSerialID", psSerialID );  
                poVehicle.updateString("sBranchCD", psBranchCd);  
                poVehicle.updateString("sClientID", psClientID);
                poVehicle.updateString("sEntryByx", poGRider.getUserID());
                poVehicle.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, MASTER_TABLE, "sPlateNox»dRegister»sPlaceReg»sMakeIDxx»sMakeDesc»sModelIDx»sModelDsc»sTypeIDxx»sTypeDesc»sColorIDx»sColorDsc»sTransMsn»nYearModl»sDescript");
                if (lsSQL.isEmpty()){
                    psMessage = "No record to update.";
                    return false;
                }
                
                /*PROCEED TO: vehicle_serial saving*/
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg();
                    if (!pbWithParent) poGRider.rollbackTrans();
                    return false;
                }
                
            } else { //update
                while (lnCtr <= getItemCount()){
                    if (!CompareRows.isRowEqual(poVehicle, poOriginalVehicle,lnCtr)) {
                        lbisModified = true;
                        break;
                    }
                    lnCtr++;
                }
                
                if(lbisModified){
                    psSerialID = (String) getMaster("sSerialID") ; 
                    poVehicle.updateString("sClientID", psClientID);
                    poVehicle.updateString("sModified", poGRider.getUserID());
                    poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                    poVehicle.updateRow();

                    lsSQL = MiscUtil.rowset2SQL(poVehicle, 
                                                MASTER_TABLE, 
                                                "sPlateNox»dRegister»sPlaceReg»sMakeIDxx»sMakeDesc»sModelIDx»sModelDsc»sTypeIDxx»sTypeDesc»sColorIDx»sColorDsc»sTransMsn»nYearModl»sDescript", 
                                                "sSerialID = " + SQLUtil.toSQL(psSerialID));
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }

                    /*PROCEED TO: vehicle_serial saving*/                   
                    if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                        psMessage = poGRider.getErrMsg();
                        if (!pbWithParent) poGRider.rollbackTrans();
                        return false;
                    }
                }
            }
            
            /*PROCEED TO: vehicle_serial_registration saving*/
            String lsPlateNox, lsPlcRegs;
            Date ldDateRegs;
            lsPlateNox = (String) getMaster("sPlateNox");
            ldDateRegs = (Date) getMaster("dRegister");
            lsPlcRegs = (String) getMaster("sPlaceReg");
            
            if ((lsPlateNox != null && !lsPlateNox.isEmpty() && !lsPlateNox.equals("")) || ((ldDateRegs != null) && !ldDateRegs.equals("1900-01-01")) || (lsPlcRegs != null && !lsPlcRegs.isEmpty() && !lsPlcRegs.equals("")) ) {
                if (pnEditMode == EditMode.ADDNEW){ //add
                    lsSQL = "INSERT INTO vehicle_serial_registration  " +
                            "(sSerialID,sPlateNox,dRegister,sPlaceReg,sEntryByx,dEntryDte,sModified,dModified)" +
                            " VALUES (" + SQLUtil.toSQL(psSerialID) +
                            "," + SQLUtil.toSQL(lsPlateNox) +
                            ", " + SQLUtil.toSQL(ldDateRegs) + 
                            "," + SQLUtil.toSQL(lsPlcRegs) +
                            "," + SQLUtil.toSQL(poGRider.getUserID()) + 
                            ", " + SQLUtil.toSQL((Date) poGRider.getServerDate() ) + 
                            "," + SQLUtil.toSQL(poGRider.getUserID()) + 
                            "," + SQLUtil.toSQL((Date) poGRider.getServerDate() ) +
                            ")";
                    if (lsSQL.isEmpty()){
                        psMessage = "No record to update.";
                        return false;
                    }
                    if (poGRider.executeQuery(lsSQL, "vehicle_serial_registration", psBranchCd, "") <= 0){
                        psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                        return false;
                    }           
                } else { //update  
                    if(lsPlateNox.equals((String) poOriginalVehicle.getObject("sPlateNox")) || ldDateRegs.equals((Date) poOriginalVehicle.getObject("dRegister")) || lsPlcRegs.equals((String) poOriginalVehicle.getObject("sPlaceReg"))) {
                    } else {
                        lsSQL = "UPDATE vehicle_serial_registration SET" +
                                "  sPlateNox = " + SQLUtil.toSQL(lsPlateNox) +
                                ", dRegister = " + SQLUtil.toSQL(ldDateRegs) +
                                ", sPlaceReg = " + SQLUtil.toSQL(lsPlcRegs) +
                                ", sModified = " + SQLUtil.toSQL(poGRider.getUserID() ) +
                                ", dModified = " + SQLUtil.toSQL((Date) poGRider.getServerDate() ) +
                                " WHERE sSerialID = " + SQLUtil.toSQL(psSerialID);
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update.";
                            return false;
                        }

                        if (poGRider.executeQuery(lsSQL, "vehicle_serial_registration", psBranchCd, "") <= 0){
                            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                            return false;
                        }      
                    }
                }
            }
            
            if (!pbWithParent) poGRider.commitTrans();
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
        return  "SELECT" + //
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
                " , IFNULL(c.sMakeIDxx,'') sMakeIDxx " + // 
                " , IFNULL(d.sMakeDesc,'') sMakeDesc " + //   
                " , IFNULL(c.sModelIDx,'') sModelIDx " + //  
                " , IFNULL(e.sModelDsc,'') sModelDsc " + //   
                " , IFNULL(c.sTypeIDxx,'') sTypeIDxx " + // 
                " , IFNULL(f.sTypeDesc,'') sTypeDesc " + //   
                " , IFNULL(c.sColorIDx,'') sColorIDx " + // 
                " , IFNULL(g.sColorDsc,'') sColorDsc " + //
                " , IFNULL(c.sTransMsn,'') sTransMsn " + //
                " , IFNULL(c.nYearModl,'') nYearModl " + //
                " , IFNULL(c.sDescript,'') sDescript " + //
                "   FROM vehicle_serial a " + 
                "   LEFT JOIN vehicle_serial_registration b ON a.sSerialID = b.sSerialID  " +
                "   LEFT JOIN vehicle_master c ON c.sVhclIDxx = a.sVhclIDxx  " +
                "   LEFT JOIN vehicle_make d ON d.sMakeIDxx = c.sMakeIDxx  " +
                "   LEFT JOIN vehicle_model e ON e.sModelIDx = c.sModelIDx  " +
                "   LEFT JOIN vehicle_type f ON f.sTypeIDxx = c.sTypeIDxx  " +
                "   LEFT JOIN vehicle_color g ON g.sColorIDx = c.sColorIDx  " ;
    }
    
    private String getSQ_SearchVhclMake(){
        return  " SELECT " +  
                " IFNULL(a.sMakeIDxx,'') sMakeIDxx  " +   
                " , IFNULL(b.sMakeDesc,'') sMakeDesc " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_make b ON b.sMakeIDxx = a.sMakeIDxx " ;
    }
    
    private String getSQ_SearchVhclModel(){
        return  " SELECT " +  
                " IFNULL(a.sModelIDx,'') sModelIDx  " +   
                " , IFNULL(b.sModelDsc,'') sModelDsc " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_model b ON b.sModelIDx = a.sModelIDx " ;
    }
    
    private String getSQ_SearchVhclType(){
        return  " SELECT " +  
                " IFNULL(a.sTypeIDxx,'') sTypeIDxx  " +   
                " , IFNULL(b.sTypeDesc,'') sTypeDesc " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_type b ON b.sTypeIDxx = a.sTypeIDxx " ;
    }
    
    private String getSQ_SearchVhclColor(){
        return  " SELECT " +  
                " IFNULL(a.sColorIDx,'') sColorIDx  " +   
                " , IFNULL(b.sColorDsc,'') sColorDsc " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_color b ON b.sColorIDx = a.sColorIDx " ;
    }
    
    private String getSQ_SearchVhclTrnsMn(){
        return  " SELECT " +  
                " IFNULL(a.sTransMsn,'') sTransMsn  " +     
                " FROM vehicle_master a";
    }
    
    private String getSQ_SearchVhclYearMdl(){
        return  " SELECT " +  
                " IFNULL(a.nYearModl,'') nYearModl  " +     
                " FROM vehicle_master a";
    }
    
    private String getSQ_SearchDealer(){
        return  " SELECT " +  
                " IFNULL(a.sCompnyNm,'') sCompnyNm  " +     
                " FROM client_master a";
    }
    
    /**
     * For searching available vehicle when key is pressed.
     * @return {@code true} if a matching available vehicle is found, {@code false} otherwise.
    */
    public boolean searchAvailableVhcl() throws SQLException{
        String lsSQL = getSQ_Master();
        
        lsSQL = MiscUtil.addCondition(lsSQL, " a.cSoldStat = '1' AND (ISNULL(a.sClientID) OR  TRIM(a.sClientID) <> '' " );
        
        ResultSet loRS;
        loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON = showFXDialog.jsonSearch(poGRider
                                                    , lsSQL
                                                    , ""
                                                    , "CS No»Plate No»Vehicle Description»Frame Number»Engine Number"
                                                    , "sCSNoxxxx»sPlateNox»sDescript»sFrameNox»sEngineNo"
                                                    , "sCSNoxxxx»sPlateNox»sDescript»sFrameNox»sEngineNo"
                                                    , 0);
        
        if (loJSON == null){
            psMessage = "No record found/selected.";
            return false;
        } else {
            if (OpenRecord((String) loJSON.get("sSerialID"))){
                pnEditMode = EditMode.UPDATE;
            }
        }
               
        return true;
    }
    /**
     * For searching vehicle make when key is pressed.
     * @param fsValue the search value for the vehicle make.
     * @return {@code true} if a matching vehicle make is found, {@code false} otherwise.
    */
    public boolean searchVehicleMake(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchVhclMake();
        
        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sMakeDesc LIKE " + SQLUtil.toSQL(fsValue + "%"))  +
                                                  " GROUP BY a.sMakeIDxx " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sMakeIDxx", loRS.getString("sMakeIDxx"));
                setMaster("sMakeDesc", loRS.getString("sMakeDesc"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Make", "sMakeDesc");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sMakeIDxx", (String) loJSON.get("sMakeIDxx"));
                setMaster("sMakeDesc", (String) loJSON.get("sMakeDesc"));
            }
        }        
        return true;
    }
    /**
     * For searching vehicle model when key is pressed.
     * @param fsValue the search value for the vehicle model.
     * @return {@code true} if a matching vehicle model is found, {@code false} otherwise.
    */
    public boolean searchVehicleModel(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchVhclModel();
        
        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sModelDsc LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx"))
                                        )  +      " GROUP BY a.sModelIDx " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sModelIDx", loRS.getString("sModelIDx"));
                setMaster("sModelDsc", loRS.getString("sModelDsc"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Model", "sModelDsc");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sModelIDx", (String) loJSON.get("sModelIDx"));
                setMaster("sModelDsc", (String) loJSON.get("sModelDsc"));
            }
        }        
        return true;
    }
    /**
     * For searching vehicle type when key is pressed.
     * @param fsValue the search value for the vehicle type.
     * @return {@code true} if a matching vehicle type is found, {@code false} otherwise.
    */
    public boolean searchVehicleType(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchVhclType();
        
        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sTypeDesc LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx"))
                                        )  +      " GROUP BY a.sTypeIDxx " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sTypeIDxx", loRS.getString("sTypeIDxx"));
                setMaster("sTypeDesc", loRS.getString("sTypeDesc"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Type", "sTypeDesc");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sTypeIDxx", (String) loJSON.get("sTypeIDxx"));
                setMaster("sTypeDesc", (String) loJSON.get("sTypeDesc"));
            }
        }        
        return true;
    }
    /**
     * For searching vehicle color when key is pressed.
     * @param fsValue the search value for the vehicle color.
     * @return {@code true} if a matching vehicle color is found, {@code false} otherwise.
    */
    public boolean searchVehicleColor(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchVhclColor();
        
        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sColorDsc LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx"))
                                        )  +      " GROUP BY a.sColorIDx " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sColorIDx", loRS.getString("sColorIDx"));
                setMaster("sColorDsc", loRS.getString("sColorDsc"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Color", "sColorDsc");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sColorIDx", (String) loJSON.get("sColorIDx"));
                setMaster("sColorDsc", (String) loJSON.get("sColorDsc"));
            }
        }        
        return true;
    }
    /**
     * For searching vehicle transmission when key is pressed.
     * @param fsValue the search value for the vehicle transmission.
     * @return {@code true} if a matching vehicle transmission is found, {@code false} otherwise.
    */
    public boolean searchVehicleTrnsMn(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchVhclTrnsMn();
        
        lsSQL = (MiscUtil.addCondition(lsSQL, " a.sTransMsn LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                  " AND a.sColorIDx = " + SQLUtil.toSQL((String) getMaster("sColorIDx"))
                                        )  +      " GROUP BY a.sTransMsn " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sTransMsn", loRS.getString("sTransMsn"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Transmission", "sTransMsn");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sTransMsn", (String) loJSON.get("sTransMsn"));
            }
        }        
        return true;
    }
    /**
     * For searching vehicle year model when key is pressed.
     * @param fsValue the search value for the vehicle year model.
     * @return {@code true} if a matching vehicle year model is found, {@code false} otherwise.
    */
    public boolean searchVehicleYearMdl(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchVhclYearMdl();
        
        lsSQL = (MiscUtil.addCondition(lsSQL, " a.nYearModl LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                  " AND a.sColorIDx = " + SQLUtil.toSQL((String) getMaster("sColorIDx")) +
                                                  " AND a.sTransMsn = " + SQLUtil.toSQL((String) getMaster("sTransMsn"))
                                        )  +      " GROUP BY a.nYearModl " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("nYearModl", loRS.getString("nYearModl"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Year Model", "nYearModl");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("nYearModl", (String) loJSON.get("nYearModl"));
            }
        }        
        return true;
    }
    
    /**
     * For searching dealership when key is pressed.
     * @param fsValue the search value for the dealership.
     * @return {@code true} if a matching dealership is found, {@code false} otherwise.
    */
    public boolean searchDealer(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchDealer();
        
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%") );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sCompnyID", loRS.getString("sCompnyID"));
                setMaster("sDealerNm", loRS.getString("sCompnyNm"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Dealership", "sCompnyNm");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sCompnyID", (String) loJSON.get("sCompnyID"));
                setMaster("sDealerNm", (String) loJSON.get("sCompnyNm"));
            }
        }        
        return true;
    }
    
    private String getSQ_SearchVhclDsc(){
        return  "SELECT" +  
                " IFNULL(a.sMakeIDxx,'') sMakeIDxx " +   
                " , IFNULL(b.sMakeDesc,'') sMakeDesc  " + 
                " , IFNULL(a.sModelIDx,'') sModelIDx  " + 
                " , IFNULL(c.sModelDsc,'') sModelDsc  " + 
                " , IFNULL(a.sTypeIDxx,'') sTypeIDxx  " +
                " , IFNULL(d.sTypeDesc,'') sTypeDesc  " +  
                " , IFNULL(a.sColorIDx,'') sColorIDx  " + 
                " , IFNULL(e.sColorDsc,'') sColorDsc  " + 
                " , IFNULL(a.sTransMsn,'') sTransMsn  " + 
                " , IFNULL(a.nYearModl,'') nYearModl  " + 
                "   FROM vehicle_master a " +
                "   LEFT JOIN vehicle_make b ON b.sMakeIDxx = a.sMakeIDxx " +
                "   LEFT JOIN vehicle_model c ON c.sModelIDx = a.sModelIDx " +
                "   LEFT JOIN vehicle_type d ON d.sTypeIDxx = a.sTypeIDxx " +
                "   LEFT JOIN vehicle_color e ON e.sColorIDx = a.sColorIDx " ;
    }
    
    //for searching vehicle make when f3 is pressed
    public boolean searchVehicleDesc(String fsMake, String fsModel, String fsType, String fsColor, String fsTrnsMn, String fsYearMdl) throws SQLException{
        String lsSQL = getSQ_SearchVhclDsc();
        
        if(!fsMake.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeDesc LIKE " + SQLUtil.toSQL(fsMake + "%"));
        }
        
        if(!fsModel.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx LIKE " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelDsc LIKE " + SQLUtil.toSQL(fsModel + "%")
                                        );
        }
        
        if(!fsType.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                     "AND sTypeDesc LIKE " + SQLUtil.toSQL(fsType + "%")
                                        );
        }
        
        if(!fsColor.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                     "AND sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                     "AND sColorDsc LIKE " + SQLUtil.toSQL(fsColor + "%")
                                            );
        }
        
        if(!fsTrnsMn.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                     "AND sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                     "AND sColorIDx = " + SQLUtil.toSQL((String) getMaster("sColorIDx")) +
                                                     "AND sTransMsn LIKE " + SQLUtil.toSQL(fsTrnsMn + "%")
                                        );
        }
        
        if(!fsYearMdl.equals("")){
            lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                     "AND sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                     "AND sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                     "AND sColorIDx = " + SQLUtil.toSQL((String) getMaster("sColorIDx")) +
                                                     "AND sTransMsn = " + SQLUtil.toSQL((String) getMaster("sTransMsn")) +
                                                     "AND nYearModl LIKE " + SQLUtil.toSQL(fsYearMdl + "%" ) 
                                        );
        }
             
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sMakeIDxx", loRS.getString("sMakeIDxx"));
                setMaster("sMakeDesc", loRS.getString("sMakeDesc"));
                setMaster("sModelIDx", loRS.getString("sModelIDx"));
                setMaster("sModelDsc", loRS.getString("sModelDsc"));
                setMaster("sTypeIDxx", loRS.getString("sTypeIDxx"));
                setMaster("sTypeDesc", loRS.getString("sTypeDesc"));
                setMaster("sColorIDx", loRS.getString("sColorIDx"));
                setMaster("sColorDsc", loRS.getString("sColorDsc"));
                setMaster("sTransMsn", loRS.getString("sTransMsn"));
                setMaster("nYearModl", loRS.getString("nYearModl"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Make", "sMakeDesc");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sMakeIDxx", (String) loJSON.get("sMakeIDxx"));
                setMaster("sMakeDesc", (String) loJSON.get("sMakeDesc"));
                setMaster("sModelIDx", (String) loJSON.get("sModelIDx"));
                setMaster("sModelDsc", (String) loJSON.get("sModelDsc"));
                setMaster("sTypeIDxx", (String) loJSON.get("sTypeIDxx"));
                setMaster("sTypeDesc", (String) loJSON.get("sTypeDesc"));
                setMaster("sColorIDx", (String) loJSON.get("sColorIDx"));
                setMaster("sColorDsc", (String) loJSON.get("sColorDsc"));
                setMaster("sTransMsn", (String) loJSON.get("sTransMsn"));
                setMaster("nYearModl", (String) loJSON.get("nYearModl"));
            }
        }        
        return true;
    }
    
    private String getSQ_MakeFrame(){
        return  "SELECT " +
                " IFNULL(sFrmePtrn,'') sFrmePtrn " +
                " FROM vehicle_make_frame_pattern ";
                
    }
    
    private String getSQ_ModelFrame(){
        return  "SELECT " +
                " IFNULL(sFrmePtrn,'') sFrmePtrn " +
                " FROM vehicle_model_frame_pattern ";
    }
    
    private String getSQ_ModelEngine(){
        return  "SELECT " +
                " IFNULL(sEngnPtrn,'') sEngnPtrn " +
                " FROM vehicle_model_engine_pattern ";
    }
    
    //Validate Make Frame Number
    public boolean isMakeFrameOK(String fsValue) throws SQLException{
        String lsSQL = getSQ_MakeFrame();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL," sFrmePtrn = "  + SQLUtil.toSQL( fsValue.substring(0, 3))
                                            +   "AND sMakeIDxx = "  + SQLUtil.toSQL( poVehicle.getString("sMakeIDxx") )); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) == 0){
            psMessage = "The first 3 characters of the Frame Number do not match the Frame Pattern. Please enter a new Pattern for the Make Frame.";
            MiscUtil.close(loRS);        
            return false;
        }                  
        return true;
    }
    
    //Validate Model Frame Number
    public boolean isModelFrameOK(String fsValue) throws SQLException{
        String lsSQL = getSQ_ModelFrame();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL," sFrmePtrn = "  + SQLUtil.toSQL( fsValue.substring(3, 5))
                                            +   " AND nFrmeLenx = "  + SQLUtil.toSQL( fsValue.length() )
                                            +   " AND sModelIDx = "  + SQLUtil.toSQL( poVehicle.getString("sModelIDx") )); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) == 0){
            psMessage = "The first 4 and 5 characters of the Frame Number do not match the Frame Pattern. Please enter a new Pattern for the Model Frame.";
            MiscUtil.close(loRS);        
            return false;
        }                  
        return true;
    }
    
    //Validate Engine Number
    public boolean isModelEngineOK(String fsValue) throws SQLException{
        String lsSQL = getSQ_ModelEngine();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL," sEngnPtrn = "  + SQLUtil.toSQL( fsValue.substring(0, 3))
                                            +   " AND nEngnLenx = "  + SQLUtil.toSQL( fsValue.length() )
                                            +   " AND sModelIDx = "  + SQLUtil.toSQL( poVehicle.getString("sModelIDx") )); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) == 0){
            psMessage = "The first 3 characters of the Engine Number do not match the Frame Pattern. Please enter a new Pattern for the Model Engine.";
            MiscUtil.close(loRS);        
            return false;
        }                  
        return true;
    }
    
    private boolean isEntryOK() throws SQLException{
        poVehicle.first();

        if (poVehicle.getString("sVhclIDxx").isEmpty()){
            psMessage = "Vehicle is not set.";
            return false;
        }
        
        if (poVehicle.getString("sFrameNox").isEmpty()){
            psMessage = "Frame Number is not set.";
            return false;
        }
        
        if (poVehicle.getString("sEngineNo").isEmpty()){
            psMessage = "Engine Number is not set.";
            return false;
        }
        
        if (!isMakeFrameOK(poVehicle.getString("sFrameNox"))){
            return false;
        }
        
        if (!isModelFrameOK(poVehicle.getString("sFrameNox"))){
            return false;
        }
        if (!isModelEngineOK(poVehicle.getString("sEngineNo"))){
            return false;
        }
        //Validate if CS / Plate Number is exist.
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL," ( ( a.sCSNoxxxx = " + SQLUtil.toSQL(poVehicle.getString("sCSNoxxxx")) + 
                                                " OR b.sPlateNox = " + SQLUtil.toSQL(poVehicle.getString("sCSNoxxxx")) + " ) " +
                                                " OR ( a.sCSNoxxxx = " + SQLUtil.toSQL(poVehicle.getString("sPlateNox")) + 
                                                " OR b.sPlateNox = " + SQLUtil.toSQL(poVehicle.getString("sPlateNox")) + " ) )" +
                                                " AND a.sSerialID <> " + SQLUtil.toSQL(poVehicle.getString("sSerialID"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "CS / Plate Number already exist.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        //Validate if Engine / Frame Number
        lsSQL = getSQ_Master();
        lsSQL = MiscUtil.addCondition(lsSQL," ( ( a.sFrameNox  = " + SQLUtil.toSQL(poVehicle.getString("sFrameNox")) + 
                                                " OR a.sEngineNo  = " + SQLUtil.toSQL(poVehicle.getString("sFrameNox")) + " ) " +
                                                " OR ( a.sFrameNox = " + SQLUtil.toSQL(poVehicle.getString("sEngineNo")) + 
                                                " OR a.sEngineNo = " + SQLUtil.toSQL(poVehicle.getString("sEngineNo")) + " ) )" +
                                                " AND a.sSerialID <> " + SQLUtil.toSQL(poVehicle.getString("sSerialID"))); 
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
