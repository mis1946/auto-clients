
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
 * @author User
 */
public class ClientEMail {
    private final String EMAIL_TABLE = "client_email_address";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psClientID;
    
    private CachedRowSet poEmail;
    
    public ClientEMail(GRider foGRider, String fsBranchCd, boolean fbWithParent){
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
    
    public void setEmail(int fnRow, int fnIndex, Object foValue) throws SQLException{
        poEmail.absolute(fnRow);
        
        switch (fnIndex){
            case 3://sEmailAdd    
                poEmail.updateObject(fnIndex, (String) foValue);
                poEmail.updateRow();
                //if (poCallback != null) poCallback.onSuccess(fnIndex, getEmail(fnIndex));
                break;
            case 4://cOwnerxxx
            case 5://cPrimaryx  
            case 6://cRecdStat
                if (foValue instanceof Integer)
                    poEmail.updateInt(fnIndex, (int) foValue);
                else 
                    poEmail.updateInt(fnIndex, 0);
                
                poEmail.updateRow();
                //if (poCallback != null) poCallback.onSuccess(fnIndex, getEmail(fnIndex));
                break;
        }               
    }
    
    public void setEmail(int fnRow,String fsIndex, Object foValue) throws SQLException{
        setEmail(fnRow, MiscUtil.getColumnIndex(poEmail, fsIndex), foValue);
    }
     
    public Object getEmail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poEmail.absolute(fnRow);
        return poEmail.getObject(fnIndex);
    }
    
    public Object getEmail(int fnRow, String fsIndex) throws SQLException{
        return getEmail(fnRow, MiscUtil.getColumnIndex(poEmail, fsIndex));
    }
    
    public int getItemCount() throws SQLException{
        poEmail.last();
        return poEmail.getRow();
    }
    
    public boolean NewRecord(){
        if(poGRider == null){
            psMessage= "Application driver is not set";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Email(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poEmail = factory.createCachedRowSet();
            poEmail.populate(loRS);
            MiscUtil.close(loRS);
            
            poEmail.last();
            poEmail.moveToInsertRow();
            
            MiscUtil.initRowSet(poEmail);       
            poEmail.updateString("cRecdStat", RecordStatus.ACTIVE);
            poEmail.updateString("cOwnerxxx", "0");
            poEmail.updateString("cPrimaryx", "0");
            
            poEmail.insertRow();
            poEmail.moveToCurrentRow();           
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
    
    public boolean OpenRecord(String fsValue, boolean fbByEmailID){
        try {
            String lsSQL;
            ResultSet loRS;
            //open master
            if (fbByEmailID)
                lsSQL = MiscUtil.addCondition(getSQ_Email(), "sEmailIDx = " + SQLUtil.toSQL(fsValue));
            else 
                lsSQL = MiscUtil.addCondition(getSQ_Email(), "sClientID = " + SQLUtil.toSQL(fsValue));
            
            loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poEmail = factory.createCachedRowSet();
            poEmail.populate(loRS);
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
                poEmail.beforeFirst();
                while (poEmail.next()){
                    String lsEmailID = MiscUtil.getNextCode(EMAIL_TABLE, "sEmailIDx", true, poGRider.getConnection(), psBranchCd);
                    poEmail.updateString("sClientID", psClientID);
                    //poEmail.updateString("sClientID", MiscUtil.getNextCode("Client_master", "sClientID", true, poGRider.getConnection(), psBranchCd));
                    poEmail.updateObject("sEmailIDx", lsEmailID);
                    poEmail.updateString("sEntryByx", poGRider.getUserID());
                    poEmail.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                    poEmail.updateString("sModified", poGRider.getUserID());
                    poEmail.updateObject("dModified", (Date) poGRider.getServerDate());
                    poEmail.updateRow();

                    lsSQL = MiscUtil.rowset2SQL(poEmail, EMAIL_TABLE, "");

                    if (poGRider.executeQuery(lsSQL, EMAIL_TABLE, psBranchCd, lsEmailID.substring(0, 4)) <= 0){
                        if (!pbWithParent) poGRider.rollbackTrans();
                        psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                        return false;
                    }

                    lnCtr++;
                }
               
            } else { //update
               
                if (!pbWithParent) poGRider.beginTrans();
                
                lnCtr = 1;
                poEmail.beforeFirst();
                while (poEmail.next()){
                    String lsEmailID = (String) getEmail(lnCtr, "sEmailIDx");
                    poEmail.updateString("sModified", poGRider.getUserID());
                    poEmail.updateObject("dModified", (Date) poGRider.getServerDate());
                    poEmail.updateRow();
                    lsSQL = MiscUtil.rowset2SQL(poEmail, 
                                                EMAIL_TABLE, 
                                                "", 
                                                "sEmailIDx = " + SQLUtil.toSQL(lsEmailID) +
                                                " AND sClientID = " + SQLUtil.toSQL((String) getEmail(lnCtr,"sClientID")));

                    if (!lsSQL.isEmpty()){
                        if (poGRider.executeQuery(lsSQL, EMAIL_TABLE, psBranchCd, lsEmailID.substring(0, 4)) <= 0){
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
    
    private String getSQ_Email(){
        return "SELECT" + 
                    " sEmailIDx " + //1
                    ", sClientID " + //2
                    ", sEmailAdd " + //3
                    ", cOwnerxxx " + //4
                    ", cPrimaryx " + //5
                    ", cRecdStat " + //6
                    ", sEntryByx " + //7
                    ", dEntryDte " + //8
                    ", sModified " + //9
                    ", dModified " + //10
                " FROM " + EMAIL_TABLE;
    }
    
    //for adding new row in address
    public boolean addEmail() throws SQLException{
        int lnCtr;
        int lnRow = getItemCount();
        
        //validate if incentive is already added
//        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){
//            if (fsCode.equals((String) getAddress(lnCtr, "sAddrssID"))) return true;
//        }
        
        poEmail.last();
        poEmail.moveToInsertRow();

        MiscUtil.initRowSet(poEmail);  
  
        poEmail.updateString("cRecdStat", RecordStatus.ACTIVE);
        poEmail.updateString("cOwnerxxx", "0");
        poEmail.updateString("cPrimaryx", "0");
        poEmail.insertRow();
        poEmail.moveToCurrentRow();
                
        return true;
    }
    
    public boolean removeEmail(int fnRow) throws SQLException{
//        if (pnEditMode != EditMode.ADDNEW) {
//            psMessage = "This feature was only for new entries.";
//            return false;
//        }
                
        if (getItemCount() == 0) {
            psMessage = "No address to delete.";
            return false;
        }
        
        int lnCtr;
        int lnRow = getItemCount();
        String lsPrimary = "1";
                
        boolean lbPrimary = false;
        //check if there are other primary
        for (lnCtr = 1; lnCtr <= lnRow; lnCtr++){            
            if ((getEmail(lnCtr, "cPrimaryx").equals(lsPrimary)) && lnCtr != fnRow) {   
                lbPrimary = true;
                break;
            }
        }
        //if true proceed to delete
        if (lbPrimary){
            poEmail.absolute(fnRow);
            poEmail.deleteRow();       
        }else{
            psMessage = "Unable to delete row.";
            return false;
        }
        return true;
    }  
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poEmail.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poEmail.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poEmail.getMetaData().getColumnType(lnCtr));
            if (poEmail.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poEmail.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poEmail.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }     
    private boolean isEntryOK() throws SQLException{
//        poEmail.first();
//        
//        if (poEmail.getString("sEmailAdd").isEmpty()){
//            psMessage = "Email is not set.";
//            return false;
//        }
//        
//        if (!CommonUtils.isValidEmail("sEmailAdd")){
//            psMessage = "Please enter valid Email Address.";
//            return false;
//        }
//        
//        if (poEmail.getString("cOwnerxxx").isEmpty()){
//            psMessage = "Owner Type is not set.";
//            return false;
//        }
//        
//        if (poEmail.getString("cPrimaryx").isEmpty()){
//            psMessage = "Primary is not set.";
//            return false;
//        }
//        
//        if (getItemCount() > 0){
//            poEmail.beforeFirst();
//            while (poEmail.next()){            
//                if (poEmail.getString("cPrimaryx") != null && poEmail.getString("cPrimaryx").equalsIgnoreCase("Y")){
//                    return true;
//                }    
//            }
//            psMessage = "No Primary email has been set.";
//            return false;
//        }
        
        return true;
    }
}
