
package org.rmj.auto.clients.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;

/**
 *
 * @author jahn 03062023
 */
public class ClientMobile {
    
    private final String MOBILE_MASTER = "client_mobile";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psClientID;
    
    public CachedRowSet poMobile;
    private CachedRowSet poOriginalMobile;
    
    public ClientMobile(GRider foGRider, String fsBranchCd, boolean fbWithParent){
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
    
    public void setClientID(String fsValue){
        psClientID = fsValue;        
    }
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    public Object getMobile(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poMobile.absolute(fnRow);
        return poMobile.getObject(fnIndex);
    }
    
    public Object getMobile(int fnRow, String fsIndex) throws SQLException{
        return getMobile(fnRow, MiscUtil.getColumnIndex(poMobile, fsIndex));
    }
    
    public int getItemCount() throws SQLException{
        if (poMobile != null){
            poMobile.last();
            return poMobile.getRow();
        }else{
            return 0;
        }        
    }
    
    public void setMobile(int fnRow, int fnIndex, Object foValue) throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        poMobile.absolute(fnRow);
        
        switch (fnIndex){
            case 4:  //cMobileTp
            case 5:  //cOwnerxxx
            case 11: //cPrimaryx
            case 12: //cSubscrbr
            case 14: //cRecdStat
                if (foValue instanceof Integer)
                    poMobile.updateInt(fnIndex, (int) foValue);
                else 
                    poMobile.updateInt(fnIndex, 0);
                
                poMobile.updateRow();
                //if (poCallback != null) poCallback.onSuccess(fnIndex, getMobile(fnIndex));                
                break;
            case 3:  //sMobileNo
            case 13: //sRemarksx
                poMobile.updateString(fnIndex, (String) foValue);
                poMobile.updateRow();

                //if (poCallback != null) poCallback.onSuccess(fnIndex, getMobile(fnIndex));
                break;
            
        }
    }
    
    public void setMobile(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setMobile(fnRow, MiscUtil.getColumnIndex(poMobile, fsIndex), foValue);
    }
    
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Mobile(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMobile = factory.createCachedRowSet();
            poMobile.populate(loRS);
            MiscUtil.close(loRS);
            
            poMobile.last();
            poMobile.moveToInsertRow();
            
            MiscUtil.initRowSet(poMobile);       
            poMobile.updateString("cRecdStat", RecordStatus.ACTIVE);
            poMobile.updateString("cMobileTp", "0");
            poMobile.updateString("cOwnerxxx", "0");
            poMobile.updateString("cSubscrbr", "0");
            poMobile.updateString("cPrimaryx", "0");            

            poMobile.insertRow();
            poMobile.moveToCurrentRow();
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
//    public boolean SearchRecord(){
//        return true;
//    }
    /**
    * Opens a mobile record based on the given value.
    *
    * @param fsValue   The value to search for (either mobile ID or client ID).
    * @param fbByUserID True if searching by user ID, false if searching by client ID.
    * @return True if the record is successfully opened, false if no record is found.
    */
    public boolean OpenRecord(String fsValue, boolean fbByUserID){
        try {
            String lsSQL;
            ResultSet loRS;
            //RowSetFactory factory = RowSetProvider.newFactory();
            //open master
            if (fbByUserID)
                lsSQL = MiscUtil.addCondition(getSQ_Mobile(), "sMobileID = " + SQLUtil.toSQL(fsValue));
            else 
                lsSQL = MiscUtil.addCondition(getSQ_Mobile(), "sClientID = " + SQLUtil.toSQL(fsValue));
            
            loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                //psMessage = "No record found.";
                MiscUtil.close(loRS);        
                //return false;
                return true;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMobile = factory.createCachedRowSet();
            poMobile.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }
    
    /**
    * Sets the editing mode to update and saves the current state of the mobile records as the original state.
    *
    * @return True if the update mode is successfully set, false otherwise.
    */
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        try {
        // Save the current state of the table as the original state
            if (poMobile != null){
                poOriginalMobile = (CachedRowSet) poMobile.createCopy();
            }
        } catch (SQLException e) {
            // Handle exception
        }
        return true;
    }
    
    /**
    * Saves the mobile record. This method handles both adding new records and updating existing ones.
    *
    * @return True if the record is successfully saved, false otherwise.
    */
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        //boolean isModified = false;
        
        try {
            //dont save if no item
            if (getItemCount() > 0){  
                if (!isEntryOK()) return false;

                String lsSQL = "";
                int lnCtr;
                
                if (!pbWithParent) poGRider.beginTrans();
                if (pnEditMode == EditMode.ADDNEW){ //add
                    //isModified = true;
                    lnCtr = 1;
                    poMobile.beforeFirst();
                    while (poMobile.next()){
                        String lsMobileID = MiscUtil.getNextCode(MOBILE_MASTER, "sMobileID", true, poGRider.getConnection(), psBranchCd);
                        poMobile.updateString("sClientID", psClientID);
                        //poMobile.updateString("sClientID", lsMobileID);//temp only
                        poMobile.updateObject("sMobileID", lsMobileID);
                        poMobile.updateObject("cSubscrbr", CommonUtils.classifyNetwork((String)getMobile(lnCtr, "sMobileNo")));
                        poMobile.updateString("sEntryByx", poGRider.getUserID());
                        poMobile.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                        poMobile.updateString("sModified", poGRider.getUserID());
                        poMobile.updateObject("dModified", (Date) poGRider.getServerDate());
                        poMobile.updateRow();

                        lsSQL = MiscUtil.rowset2SQL(poMobile, MOBILE_MASTER, "");
                        if (lsSQL.isEmpty()){
                            psMessage = "No record to update.";
                            return false;
                        }
                        if (poGRider.executeQuery(lsSQL, MOBILE_MASTER, psBranchCd, lsMobileID.substring(0, 4)) <= 0){
                            if (!pbWithParent) poGRider.rollbackTrans();
                            psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                            return false;
                        }
                        lnCtr++;
                    }               
                } else { //update 
                    //check if changes has been made                
//                    lnCtr = 1;
//                    while (lnCtr <= getItemCount()){
//                        if (!CompareRows.isRowEqual(poMobile, poOriginalMobile)) {
//                            isModified = true;
//                            break;
//                        }
//                        lnCtr++;
//                    }
//
//                    if (isModified) {
                        // Save the changes
                        lnCtr = 1;
                        while (lnCtr <= getItemCount()){ 
                            if(!CompareRows.isRowEqual(poMobile, poOriginalMobile,lnCtr)) {
                                String lsMobileID = (String) getMobile(lnCtr, "sMobileID");
                                if (lsMobileID.equals("") || lsMobileID.isEmpty()){// check if user added new mobile to insert
                                    lsMobileID = MiscUtil.getNextCode(MOBILE_MASTER, "sMobileID", true, poGRider.getConnection(), psBranchCd);
                                    poMobile.updateString("sClientID", psClientID);
                                    //poMobile.updateString("sClientID", lsMobileID);//temp only
                                    poMobile.updateObject("sMobileID", lsMobileID);
                                    poMobile.updateObject("cSubscrbr", CommonUtils.classifyNetwork((String)getMobile(lnCtr, "sMobileNo")));
                                    poMobile.updateString("sEntryByx", poGRider.getUserID());
                                    poMobile.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                                    poMobile.updateString("sModified", poGRider.getUserID());
                                    poMobile.updateObject("dModified", (Date) poGRider.getServerDate());
                                    poMobile.updateRow();

                                    lsSQL = MiscUtil.rowset2SQL(poMobile, MOBILE_MASTER, "");

                                    if (poGRider.executeQuery(lsSQL, MOBILE_MASTER, psBranchCd, lsMobileID.substring(0, 4)) <= 0){
                                        if (!pbWithParent) poGRider.rollbackTrans();
                                        psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                                        return false;
                                    }
                                }else{//if user modified already saved address                
                                    poMobile.updateObject("cSubscrbr", CommonUtils.classifyNetwork((String)getMobile(lnCtr, "sMobileNo")));
                                    poMobile.updateString("sModified", poGRider.getUserID());
                                    poMobile.updateObject("dModified", (Date) poGRider.getServerDate());
                                    lsSQL = MiscUtil.rowset2SQL(poMobile, 
                                                                MOBILE_MASTER, 
                                                                "", 
                                                                "sMobileID = " + SQLUtil.toSQL(lsMobileID) +
                                                                " AND sClientID = " + SQLUtil.toSQL((String) getMobile(lnCtr,"sClientID")));

                                    if (!lsSQL.isEmpty()){
                                        if (poGRider.executeQuery(lsSQL, MOBILE_MASTER, psBranchCd, lsMobileID.substring(0, 4)) <= 0){
                                            if (!pbWithParent) poGRider.rollbackTrans();
                                            psMessage = poGRider.getMessage() + ";" + poGRider.getErrMsg();
                                            return false;
                                        }
                                    }
                                }
                            }
                        lnCtr++;
                        }
                        // Update the original state of the table
                        poOriginalMobile = (CachedRowSet) poMobile.createCopy();
                    //}
                }            
                
                if (!pbWithParent) poGRider.commitTrans();
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }        
        pnEditMode = EditMode.UNKNOWN;
        return true;    
    }
    
    //for adding new row in mobile
    /**
    * Adds a new mobile record to the mobile records list.
    *
    * @return True if the mobile record is successfully added, false otherwise.
    * @throws SQLException if a database access error occurs.
    */
    public boolean addMobile() throws SQLException{
        if (poMobile == null){
            String lsSQL = MiscUtil.addCondition(getSQ_Mobile(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMobile = factory.createCachedRowSet();
            poMobile.populate(loRS);
            MiscUtil.close(loRS);
        }
               
        poMobile.last();
        poMobile.moveToInsertRow();

        MiscUtil.initRowSet(poMobile);  
        poMobile.updateString("cRecdStat", RecordStatus.ACTIVE);
        poMobile.updateString("cMobileTp", "0");
        poMobile.updateString("cOwnerxxx", "0");
        poMobile.updateString("cSubscrbr", "0");
        poMobile.updateString("cPrimaryx", "0");   
        poMobile.insertRow();
        poMobile.moveToCurrentRow();
                
        return true;
    }
    //no need deactivate can already set to active yes/no in combo box
//    public boolean deactivateMobile(int fnRow) throws SQLException{
//        if (pnEditMode == EditMode.ADDNEW) {
//            psMessage = "This feature is only for saved entries.";
//            return false;
//        }
//        
//        if (getItemCount() == 0) {
//            psMessage = "No Mobile number to Deactivate.";
//            return false;
//        }
//        poMobile.updateString("cRecdStat", RecordStatus.INACTIVE);
//        return true;
//    }
    
        /**
     * Removes a mobile record at the specified row.
     *
     * @param fnRow The row index of the mobile record to be removed.
     * @return True if the mobile record is removed successfully, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean removeMobile(int fnRow) throws SQLException{
//        if (pnEditMode != EditMode.ADDNEW) {
//            psMessage = "This feature is only for new entries.";
//            return false;
//        }
                
        if (getItemCount() == 0) {
            psMessage = "No address to delete.";
            return false;
        }
        
        poMobile.absolute(fnRow);
        poMobile.deleteRow();  
        return true;
    }  
     
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poMobile.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poMobile.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poMobile.getMetaData().getColumnType(lnCtr));
            if (poMobile.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poMobile.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poMobile.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    } 
    
    /**
    * Checks if the mobile records entry is valid before saving.
    *
    * @return True if the mobile records entry is valid, false otherwise.
    * @throws SQLException if a database access error occurs.
    */
    public boolean isEntryOK() throws SQLException{
        int lnCtr;
        int lnRow = getItemCount();
                
        boolean lbPrimary = false;
        String lsPrimary = "1";
        //check if there are other primary
        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){            
            if ((getMobile(lnCtr, "cPrimaryx").equals(lsPrimary))) {   
                lbPrimary = true;
                break;
            }
        }
        //if false do not allow saving
        if (!lbPrimary){          
             psMessage = "No Primary Mobile found, please set a primary Mobile.";
            return false;
        }
        
        if (getItemCount() == 0){
            psMessage = "No Mobile detected.";
            return false;
        }
        
        if (getItemCount() > 0){            
            lnCtr = 1;
            while (lnCtr <= getItemCount()){            
                if (poMobile.getString("sMobileNo").isEmpty()){
                    psMessage = "Mobile Number is not set.";
                    return false;
                }
                if (poMobile.getString("cMobileTp").isEmpty()){
                    psMessage = "Mobile Type is not set.";
                    return false;
                } 
                if (poMobile.getString("cOwnerxxx").isEmpty()){
                    psMessage = "Owner is not set.";
                    return false;
                } 
                if (poMobile.getString("cMobileTp").equals(0)){
                     if (CommonUtils.classifyNetwork(poMobile.getString( "sMobileNo")).isEmpty()){
                        psMessage = "Prefix not registered " + poMobile.getString("sMobileNo");
                        return false;
                    }   
                }
                lnCtr++;
            }
        }
        return true;        
    }
    
    public String getSQ_Mobile(){
        return "SELECT" + 
                    " sMobileID " +//1
                    ", sClientID " +//2
                    ", sMobileNo " +//3
                    ", cMobileTp " +//4
                    ", cOwnerxxx " +//5
                    ", cIncdMktg " +//6
                    ", cVerified " +//7
                    ", dLastVeri " +//8
                    ", cInvalidx " +//9
                    ", dInvalidx " +//10
                    ", cPrimaryx " +//11
                    ", cSubscrbr " +//12
                    ", sRemarksx " +//13
                    ", cRecdStat " +//14
                    ", sEntryByx " +//15
                    ", dEntryDte " +//16
                    ", sModified " +//17
                    ", dModified " +//18
                " FROM " + MOBILE_MASTER;
    }            
}
