/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.parameters;

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
 * Date Created: 05-26-2023
 */
public class VehicleType {
    private final String MASTER_TABLE = "vehicle_type";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poVehicle;
    private CachedRowSet poVehicleDetail;
    private CachedRowSet poTypeFormat;
    
    public VehicleType(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
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
    
    public int getFormatCount() throws SQLException{
        poTypeFormat.last();
        return poTypeFormat.getRow();
    }
    
    public int getDetailCount() throws SQLException{
        poVehicleDetail.last();
        return poVehicleDetail.getRow();
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poVehicle.first();
        
        switch (fnIndex){            
            case 2://sTypeDesc
            case 9://sVhclSize
            case 10://sVariantx_a
            case 11://sTypeDesc_b
            case 12://sMakeDesc
            case 13://sMakeIDxx
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
        
        poVehicleDetail.absolute(fnRow);
        return poVehicleDetail.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poVehicleDetail, fsIndex));
    }
    
    /**
    * Initializes a new vehicle record in the CachedRowSet for data entry.
    * @return True if the new record setup is successful, or false if there are errors.
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
    
    /**
    * Loads a list of vehicle records into the 'poVehicleDetail' CachedRowSet.
    * @return True if the list is successfully loaded, false if there are errors or the application driver is not set.
    * @throws SQLException if there is an issue with the database interaction.
    */
    public boolean LoadList() throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL = getSQ_Master() + " ORDER BY sTypeDesc ASC ";
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        loRS = poGRider.executeQuery(lsSQL);
        poVehicleDetail = factory.createCachedRowSet();
        poVehicleDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    /**
    * Opens an existing vehicle record in the CachedRowSet for viewing or editing based on the provided type ID.
    * @param fsValue The type ID used to locate and open the specific vehicle record.
    * @return True if the record is successfully opened, or false if there are errors or the record doesn't exist.
    */
    public boolean OpenRecord(String fsValue){
        if (poVehicleDetail == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "sTypeIDxx = " + SQLUtil.toSQL(fsValue));
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
    * Sets the edit mode to UPDATE, indicating that the current vehicle record is being updated.
    * @return True to confirm the edit mode change to UPDATE.
    */
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        return true;        
    }
    
    /**
    * Saves the changes made to the current vehicle record, either as a new record (ADDNEW) or an update (UPDATE).
    * @return True if the record is successfully saved, false if there are errors, the update mode is invalid, or there is no record to update.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            String lsSQL = "";
            String lsTransNox = "";
            if (pnEditMode == EditMode.ADDNEW){ //add
                poVehicle.updateString("sTypeIDxx",MiscUtil.getNextCode(MASTER_TABLE, "sTypeIDxx", true, poGRider.getConnection(), psBranchCd) );                                                             
                poVehicle.updateString("sEntryByx", poGRider.getUserID());
                poVehicle.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, MASTER_TABLE, "sVhclSize»sVariantx_a»sVariantx_b»sMakeIDxx»sMakeDesc");
            } else { //update  
                poVehicle.updateString("sModified", poGRider.getUserID());
                poVehicle.updateObject("dModified", (Date) poGRider.getServerDate());
                poVehicle.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poVehicle, 
                                            MASTER_TABLE, 
                                            "sVhclSize»sVariantx_a»sVariantx_b»sMakeIDxx»sMakeDesc", 
                                            "sTypeIDxx = " + SQLUtil.toSQL((String) getMaster("sTypeIDxx")));
            }
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }
            
            if (!pbWithParent) poGRider.beginTrans();
            //TODO
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
        return "SELECT" + 
                    " sTypeIDxx" + //1
                    ", IFNULL(sTypeDesc, '') sTypeDesc" + //2
                    ", sTypeCode" + //3
                    ", cRecdStat" + //4
                    ", sEntryByx" + //5 
                    ", dEntryDte" + //6
                    ", sModified" + //7
                    ", dModified" + //8
                    ", '' as sVhclSize" + //9
                    ", '' as sVariantx_a" + //10
                    ", '' as sVariantx_b" + //11
                    ", '' as sMakeDesc" + //12
                    ", '' as sMakeIDxx" + //13
                " FROM vehicle_type ";
    }
    
    private String getSQ_TypeFormat(){
        return "SELECT" + 
                    "  sMakeIDxx" +
                    ", IFNULL(sFormula1, '') sFormula1" + 
                    ", IFNULL(sFormula2, '') sFormula2" + 
                " FROM vehicle_make ";
    }
    
    /**
    * Loads a list of type format records into the 'poTypeFormat' CachedRowSet based on the provided value.
    * @param fsValue The value used to filter the type format records.
    * @return True if the list is successfully loaded, false if there are errors, the application driver is not set, or no records are found.
    * @throws SQLException if there is an issue with the database interaction.
    */
    public boolean LoadTypeFormat(String fsValue) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";
        
        String lsSQL = getSQ_TypeFormat();
        lsSQL = MiscUtil.addCondition(lsSQL, "sMakeIDxx LIKE " + SQLUtil.toSQL(fsValue));
         
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        loRS = poGRider.executeQuery(lsSQL);
        poTypeFormat = factory.createCachedRowSet();
        poTypeFormat.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    public Object getFormat(int fnIndex) throws SQLException{
        poTypeFormat.first();
        return poTypeFormat.getObject(fnIndex);
    }
    
    private String getSQ_TypeEng(){
        return "SELECT" +
                " IFNULL(sVhclSize, '') sVhclSize" +   
                " FROM vehicle_type_engine ";
    }
    
    /**
    * Searches for a vehicle type engine record based on the provided criteria.
    * @param fsValue The value to search for, used as a partial match for the 'sVhclSize' field.
    * @return True if a matching record is found, and the 'sVhclSize' field is set accordingly; false if no record is found.
    * @throws SQLException if there is an issue with the database interaction.
    */
    public boolean searchTypeEngine(String fsValue) throws SQLException{
        String lsSQL = getSQ_TypeEng();
        lsSQL = MiscUtil.addCondition(lsSQL, "sVhclSize LIKE " + SQLUtil.toSQL(fsValue + "%"));
                
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sVhclSize", loRS.getString("sVhclSize"));
            } else {
                psMessage = "No record found.";
                setMaster("sVhclSize", "");
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Type Engine", "sVhclSize");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                setMaster("sVhclSize", "");
                return false;
            } else {
                setMaster("sVhclSize", (String) loJSON.get("sVhclSize"));
            }
        }        
        return true;
    }
    
    private String getSQ_TypeVar(){
        return "SELECT" +
                " IFNULL(sVariantx, '') sVariantx" +   
                ", IFNULL(sVariantG, '') sVariantG" +   
                " FROM vehicle_type_variant ";
    }
    
    /**
    * Searches for a vehicle type variant record based on the provided criteria.
    * @param fsValue  The value to search for, used as a partial match for the 'sVariantx' field.
    * @param fsVarGrp The variant group to filter the search results.
    * @return True if a matching record is found, and the 'sVariantx_a' and 'sVariantx_b' fields are set accordingly; false if no record is found.
    * @throws SQLException if there is an issue with the database interaction.
    */
    public boolean searchTypeVariant(String fsValue, String fsVarGrp) throws SQLException{
        String lsSQL = getSQ_TypeVar();
        
        
        lsSQL = MiscUtil.addCondition(lsSQL, " sVariantx LIKE " + SQLUtil.toSQL(fsValue + "%") +
                                                " AND sVariantG LIKE " + SQLUtil.toSQL(fsVarGrp)
                                    );       
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sVariantx_a", loRS.getString("sVariantx"));
                setMaster("sVariantx_b", loRS.getString("sVariantx"));
            } else {
                psMessage = "No record found.";
                setMaster("sVariantx_a", "");
                setMaster("sVariantx_b", "");
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Type Variant", "sVariantx");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                setMaster("sVariantx_a", "");
                setMaster("sVariantx_b", "");
                return false;
            } else {
                setMaster("sVariantx_a", (String) loJSON.get("sVariantx"));
                setMaster("sVariantx_b", (String) loJSON.get("sVariantx"));
            }
        }        
        return true;
    }
    
    private String getSQ_VhclDesc(){
        return "SELECT" +
                " sVhclIDxx" +   
                ", IFNULL(sDescript, '') sDescript" +   
                " FROM vehicle_master ";
    }
    
    private String getSQ_SearchVhclMake(){
        return  " SELECT " +  
                " IFNULL(sMakeIDxx,'') sMakeIDxx  " +   
                " , IFNULL(sMakeDesc,'') sMakeDesc " +   
                " FROM vehicle_make " ;
    }
    
    /**
     * For searching vehicle make when key is pressed.
     * @param fsValue the search value for the vehicle make.
     * @return {@code true} if a matching vehicle make is found, {@code false} otherwise.
    */
    public boolean searchVehicleMake(String fsValue) throws SQLException{
        String lsSQL = getSQ_SearchVhclMake();
        String lsOrigVal = getMaster(13).toString();
        String lsNewVal = "";
        lsSQL = (MiscUtil.addCondition(lsSQL, " sMakeDesc LIKE " + SQLUtil.toSQL(fsValue + "%"))  +
                                                  " GROUP BY sMakeIDxx " );
        ResultSet loRS;
        JSONObject loJSON = null;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                lsNewVal = loRS.getString("sMakeIDxx");
                setMaster("sMakeIDxx", loRS.getString("sMakeIDxx"));
                setMaster("sMakeDesc", loRS.getString("sMakeDesc"));
            } else {
                psMessage = "No record found.";
                setMaster("sMakeIDxx","");
                setMaster("sMakeDesc","");
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Vehicle Make", "sMakeDesc");
            
            if (loJSON != null){
                lsNewVal = (String) loJSON.get("sMakeIDxx");
                setMaster("sMakeIDxx", (String) loJSON.get("sMakeIDxx"));
                setMaster("sMakeDesc", (String) loJSON.get("sMakeDesc"));
            } else {
                psMessage = "No record found/selected.";
                setMaster("sMakeIDxx","");
                setMaster("sMakeDesc","");
                return false;
            }
        }   
             
        return true;
    }
    
    /**
    * Validates the current vehicle type record for consistency and uniqueness.
    * @return True if the vehicle type is valid and doesn't conflict with existing records, false otherwise.
    * @throws SQLException if there is an issue with the database interaction.
    */
    private boolean isEntryOK() throws SQLException{
        poVehicle.first();

        if (poVehicle.getString("sTypeDesc").isEmpty()){
            psMessage = "Vehicle Type is not set.";
            return false;
        }
        
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = MiscUtil.addCondition(lsSQL, "sTypeDesc = " + SQLUtil.toSQL(poVehicle.getString("sTypeDesc")) +
                                                " AND sTypeIDxx <> " + SQLUtil.toSQL(poVehicle.getString("sTypeIDxx"))); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Existing Vehicle Type Description.";
            MiscUtil.close(loRS);        
            return false;
        }
        
        /*CHECK WHEN VEHICLE TYPE IS ALREADY LINKED TO VEHICLE DESCRIPTION*/
        lsSQL = getSQ_VhclDesc();
        lsSQL = MiscUtil.addCondition(lsSQL, "sTypeIDxx = " + SQLUtil.toSQL(poVehicle.getString("sTypeIDxx")) ); 
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0){
            psMessage = "Vehicle Type is already used in Vehicle Description. Please contact system administrator to address this issue.";
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

