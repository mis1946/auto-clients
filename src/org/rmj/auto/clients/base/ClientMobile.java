
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
    
    private CachedRowSet poMobile;
    
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
        poMobile.last();
        return poMobile.getRow();
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
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
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
            int lnCtr;
            
            if (pnEditMode == EditMode.ADDNEW){ //add
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

                    if (poGRider.executeQuery(lsSQL, MOBILE_MASTER, psBranchCd, lsMobileID.substring(0, 4)) <= 0){
                        if (!pbWithParent) poGRider.rollbackTrans();
                        psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                        return false;
                    }
                    lnCtr++;
                }               
            } else { //update               
                if (!pbWithParent) poGRider.beginTrans();
                
                lnCtr = 1;
                poMobile.beforeFirst();
                while (poMobile.next()){
                    String lsMobileID = (String) getMobile(lnCtr, "sMobileID");
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
                    lnCtr++;
                }
            }            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
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
    
    //for adding new row in mobile
    public boolean addMobile() throws SQLException{
        int lnCtr;
        int lnRow = getItemCount();
        
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
    
     public boolean removeMobile(int fnRow) throws SQLException{
//        if (pnEditMode != EditMode.ADDNEW) {
//            psMessage = "This feature was only for new entries.";
//            return false;
//        }
                
        if (getItemCount() == 0) {
            psMessage = "No address to delete.";
            return false;
        }
        
        poMobile.absolute(fnRow);
        poMobile.deleteRow();  
//        int lnCtr;
//        int lnRow = getItemCount();
//        String lsPrimary = "1";
//                
//        boolean lbPrimary = false;
//        //check if there are other primary
//        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){            
//            if ((getMobile(lnCtr, "cPrimaryx").equals(lsPrimary)) && lnCtr != fnRow) {   
//                lbPrimary = true;
//                break;
//            }
//        }
//        //if true proceed to delete
//        if (lbPrimary){
//            poMobile.absolute(fnRow);
//            poMobile.deleteRow();       
//        }else{
//            psMessage = "Unable to delete row.";
//            return false;
//        }
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
             psMessage = "No Primary address found, please set a primary address.";
            return false;
        }
        
        if (getItemCount() == 0){
            psMessage = "No Address detected.";
            return false;
        }
        
        poMobile.beforeFirst();
        while (poMobile.next()){            
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
            if (poMobile.getString("cMobileTp").equals(1)){
                 if (CommonUtils.classifyNetwork(poMobile.getString( "sMobileNo")).isEmpty()){
                    psMessage = "Prefix not registered " + poMobile.getString("sMobileNo");
                    return false;
                }   
            }
        }                
        return true;        
    }
    
    private String getSQ_Mobile(){
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
