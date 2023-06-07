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
import org.rmj.appdriver.agentfx.CommonUtils;
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
    private CachedRowSet poVehicleDetail;
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
        poVehicleDetail.last();
        return poVehicleDetail.getRow();
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
            case 33:
            case 34:
                poVehicle.updateObject(fnIndex, (String) foValue);
                poVehicle.updateRow();
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
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
        
        poVehicleDetail.absolute(fnRow);
        return poVehicleDetail.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poVehicleDetail, fsIndex));
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
            poVehicle.updateObject("dRegister", SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));    
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
        poVehicleDetail = factory.createCachedRowSet();
        poVehicleDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    /**
     * Opens the record of a specific vehicle identified by the given serial ID.
     * @param fsValue the serial ID of the vehicle to open.
     * @return {@code true} if the record is successfully opened, {@code false} otherwise.
    */
    public boolean OpenRecord(String fsValue){
        if (poVehicleDetail == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sSerialID = " + SQLUtil.toSQL(fsValue));
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
                psSerialID = (String) getMaster("sSerialID") ;
                if(lbisModified){
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
            ldDateRegs = SQLUtil.toDate(CommonUtils.xsDateShort((Date) getMaster("dRegister")), SQLUtil.FORMAT_SHORT_DATE); //(Date) getMaster("dRegister");
            lsPlcRegs = (String) getMaster("sPlaceReg");
            
            if  (   ((!lsPlateNox.isEmpty() && !lsPlateNox.equals(""))) 
                ||  ((!ldDateRegs.equals(SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE))))
                ||  ((!lsPlcRegs.isEmpty() && !lsPlcRegs.equals(""))) ) {
                
                //Proceed to ADD if pnEditMode is addnew and if the original cacherowset is empty
                if (pnEditMode == EditMode.ADDNEW 
                    || ((pnEditMode == EditMode.UPDATE) &&
                        (  ((String) poOriginalVehicle.getObject("sPlateNox")).equals("")
                        || ((Date) poOriginalVehicle.getObject("dRegister")) == SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE) 
                        || ((String) poOriginalVehicle.getObject("sPlaceReg")).equals("")))){ //add
                    
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
                             
                } else { //update
                        lsSQL = "UPDATE vehicle_serial_registration SET" +
                                "  sPlateNox = " + SQLUtil.toSQL(lsPlateNox) +
                                ", dRegister = " + SQLUtil.toSQL(ldDateRegs) +
                                ", sPlaceReg = " + SQLUtil.toSQL(lsPlcRegs) +
                                ", sModified = " + SQLUtil.toSQL(poGRider.getUserID() ) +
                                ", dModified = " + SQLUtil.toSQL((Date) poGRider.getServerDate() ) +
                                " WHERE sSerialID = " + SQLUtil.toSQL(psSerialID);
                }
                    
                if (lsSQL.isEmpty()){
                    psMessage = "No record to update.";
                    return false;
                }
                if (poGRider.executeQuery(lsSQL, "vehicle_serial_registration", psBranchCd, "") <= 0){
                    psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
                    return false;
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
        psMessage = "Vehicle saved Successfully";
        return true;
    }
    
    private String getSQ_Master(){
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
    
    private String getSQ_SearchVhclMake(){
        return  " SELECT " +  
                " IFNULL(a.sMakeIDxx,'') sMakeIDxx  " +   
                " , IFNULL(b.sMakeDesc,'') sMakeDesc " +   
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +   
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_make b ON b.sMakeIDxx = a.sMakeIDxx " ;
    }
    
    private String getSQ_SearchVhclModel(){
        return  " SELECT " +  
                " IFNULL(a.sModelIDx,'') sModelIDx  " +   
                " , IFNULL(b.sModelDsc,'') sModelDsc " +  
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_model b ON b.sModelIDx = a.sModelIDx " ;
    }
    
    private String getSQ_SearchVhclType(){
        return  " SELECT " +  
                " IFNULL(a.sTypeIDxx,'') sTypeIDxx  " +   
                " , IFNULL(b.sTypeDesc,'') sTypeDesc " +  
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_type b ON b.sTypeIDxx = a.sTypeIDxx " ;
    }
    
    private String getSQ_SearchVhclColor(){
        return  " SELECT " +  
                " IFNULL(a.sColorIDx,'') sColorIDx  " +   
                " , IFNULL(b.sColorDsc,'') sColorDsc " +  
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +    
                " FROM vehicle_master a " + 
                " LEFT JOIN vehicle_color b ON b.sColorIDx = a.sColorIDx " ;
    }
    
    private String getSQ_SearchVhclTrnsMn(){
        return  " SELECT " +  
                " IFNULL(a.sTransMsn,'') sTransMsn  " +    
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +   
                " FROM vehicle_master a";
    }
    
    private String getSQ_SearchVhclYearMdl(){
        return  " SELECT " +  
                " IFNULL(a.nYearModl,'') nYearModl  " +    
                " , IFNULL(a.sVhclIDxx,'') sVhclIDxx  " +
                " FROM vehicle_master a";
    }
    
    private String getSQ_SearchDealer(){
        return  " SELECT " +  
                " IFNULL(a.sClientID,'') sClientID  " + 
                ", IFNULL(a.sCompnyNm,'') sCompnyNm  " +     
                " FROM client_master a";
    }
    
    private String getSQ_Regsplace(){
        return  " SELECT " +
                " a.sTownName " +
                ", b.sProvName " +
                " FROM towncity a " +
                " LEFT JOIN province b ON b.sProvIDxx = a.sProvIDxx " ;
    }
    
    /**
     * For searching available vehicle when key is pressed.
     * @return {@code true} if a matching available vehicle is found, {@code false} otherwise.
    */
    public boolean searchAvailableVhcl() throws SQLException{
        String lsSQL = getSQ_Master();
        
        lsSQL = MiscUtil.addCondition(lsSQL, " a.cSoldStat = '1' AND (ISNULL(a.sClientID) OR  TRIM(a.sClientID) = '' )" );
        
        System.out.println(lsSQL);
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
                if (poVehicle != null){
                    poOriginalVehicle = (CachedRowSet) poVehicle.createCopy();
                }
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
        String lsOrigVal = getMaster(23).toString();
        String lsNewVal = "";
        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sMakeDesc LIKE " + SQLUtil.toSQL(fsValue + "%"))  +
                                                  " GROUP BY a.sMakeIDxx " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sMakeIDxx");
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
                lsNewVal = (String) loJSON.get("sMakeIDxx");
                setMaster("sMakeIDxx", (String) loJSON.get("sMakeIDxx"));
                setMaster("sMakeDesc", (String) loJSON.get("sMakeDesc"));
            }
        }   
            
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("sModelIDx", "");
            setMaster("sModelDsc", "");
            setMaster("sTypeIDxx", "");
            setMaster("sTypeDesc", "");
            setMaster("sColorIDx", "");
            setMaster("sColorDsc", "");
            setMaster("sTransMsn", "");
            setMaster("nYearModl", "");
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
        String lsOrigVal = getMaster(25).toString();
        String lsNewVal = "";
        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sModelDsc LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx"))
                                        )  +      " GROUP BY a.sModelIDx " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sModelIDx");
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
                lsNewVal = (String) loJSON.get("sModelIDx");
                setMaster("sModelIDx", (String) loJSON.get("sModelIDx"));
                setMaster("sModelDsc", (String) loJSON.get("sModelDsc"));
            }
        } 
            
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("sTypeIDxx", "");
            setMaster("sTypeDesc", "");
            setMaster("sColorIDx", "");
            setMaster("sColorDsc", "");
            setMaster("sTransMsn", "");
            setMaster("nYearModl", "");
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
        String lsOrigVal = getMaster(27).toString();
        String lsNewVal = "";
        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sTypeDesc LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx"))
                                        )  +      " GROUP BY a.sTypeIDxx " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sTypeIDxx");
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
                lsNewVal = (String) loJSON.get("sTypeIDxx");
                setMaster("sTypeIDxx", (String) loJSON.get("sTypeIDxx"));
                setMaster("sTypeDesc", (String) loJSON.get("sTypeDesc"));
            }
        } 
        
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("sColorIDx", "");
            setMaster("sColorDsc", "");
            setMaster("sTransMsn", "");
            setMaster("nYearModl", "");
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
        String lsOrigVal = getMaster(31).toString();
        String lsNewVal = "";
        lsSQL = (MiscUtil.addCondition(lsSQL, " a.sTransMsn LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) 
                                        )  +      " GROUP BY a.sTransMsn " );
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sTransMsn");
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
                lsNewVal = (String) loJSON.get("sTransMsn");
                setMaster("sTransMsn", (String) loJSON.get("sTransMsn"));
            }
        }  
        
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("sColorIDx", "");
            setMaster("sColorDsc", "");
            setMaster("nYearModl", "");
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
        String lsOrigVal = getMaster(29).toString();
        String lsNewVal = "";
        lsSQL = (MiscUtil.addCondition(lsSQL, " b.sColorDsc LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                  " AND a.sMakeIDxx = " + SQLUtil.toSQL((String) getMaster("sMakeIDxx")) +
                                                  " AND a.sModelIDx = " + SQLUtil.toSQL((String) getMaster("sModelIDx")) +
                                                  " AND a.sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")) +
                                                  " AND a.sTransMsn = " + SQLUtil.toSQL((String) getMaster("sTransMsn"))
                                        )  +      " GROUP BY a.sColorIDx " );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sColorIDx");
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
                lsNewVal = (String) loJSON.get("sColorIDx");
                setMaster("sColorIDx", (String) loJSON.get("sColorIDx"));
                setMaster("sColorDsc", (String) loJSON.get("sColorDsc"));
            }
        }
        
        if(!lsNewVal.equals(lsOrigVal)){
            setMaster("sVhclIDxx", "");
            setMaster("nYearModl", "");
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
                setMaster("sVhclIDxx", loRS.getString("sVhclIDxx"));
                
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
                setMaster("sVhclIDxx", (String) loJSON.get("sVhclIDxx"));
            }
        }        
        return true;
    }
    
    /**
     * For searching dealership when key is pressed.
     * @param fsValue the search value for the dealership.
     * @return {@code true} if a matching dealership is found, {@code false} otherwise: set only for sDealerNm column.
    */
    public boolean searchDealer(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchDealer();
        
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%") );
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sCompnyID", loRS.getString("sClientID")); //sCompnyID
                setMaster("sDealerNm", loRS.getString("sCompnyNm"));
            } else {
                setMaster("sCompnyID", "");
                setMaster("sDealerNm", fsValue);
                psMessage = "No record found.";
                //return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Dealership", "sCompnyNm");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                setMaster("sCompnyID", "");
                setMaster("sDealerNm", fsValue);
                //return false;
            } else {
                setMaster("sCompnyID", (String) loJSON.get("sClientID")); //sCompnyID
                setMaster("sDealerNm", (String) loJSON.get("sCompnyNm"));
            }
        }        
        return true;
    }
    
    /**
     * For searching registered place when key is pressed.
     * @param fsValue the search value for the dealership.
     * @return {@code true} if a matching registered place is found, {@code false} otherwise: set only for sPlaceReg column.
    */
    public boolean searchRegsplace(String fsValue) throws SQLException{
        String lsSQL = getSQ_Regsplace();
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sTownName LIKE " + SQLUtil.toSQL(fsValue + "%")
                                               + "OR b.sProvName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sPlaceReg", (loRS.getString("sTownName") + " " + loRS.getString("sProvName")));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Place of Registration", "sTownName");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {    
                setMaster("sPlaceReg", ((String) loJSON.get("sTownName") + " " + (String) loJSON.get("sProvName")));
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
