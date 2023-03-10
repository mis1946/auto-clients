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
 * @author User
 */
public class ClientSocMed {
    private final String SOCMED_TABLE = "client_social_media";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psClientID;
    
    private CachedRowSet poSocMed;
    
    public ClientSocMed(GRider foGRider, String fsBranchCd, boolean fbWithParent){
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
    
    public void setClientID(String fsValue) {
        psClientID = fsValue;
    }
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    public void setSocMed(int fnIndex, Object foValue) throws SQLException{
        poSocMed.first();
        
        switch (fnIndex){
            case 3://sAccountx            
                poSocMed.updateObject(fnIndex, (String) foValue);
                poSocMed.updateRow();  
                //if (poCallback != null) poCallback.onSuccess(fnIndex, getSQ_SocMed(fnIndex));
                break;
            case 4://cSocialTp
            case 5://cRecdStat
                if (foValue instanceof Integer)
                    poSocMed.updateInt(fnIndex, (int) foValue);
                else 
                    poSocMed.updateInt(fnIndex, 0);
                
                poSocMed.updateRow();
                //if (poCallback != null) poCallback.onSuccess(fnIndex, getSQ_SocMed(fnIndex));
                break;
        }               
    }
    
    public void setSocMed(String fsIndex, Object foValue) throws SQLException{
        setSocMed(MiscUtil.getColumnIndex(poSocMed, fsIndex), foValue);
    }
     
    public Object getSocMed(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poSocMed.absolute(fnRow);
        return poSocMed.getObject(fnIndex);
    }
    
    public Object getSocMed(int fnRow, String fsIndex) throws SQLException{
        return getSocMed(fnRow, MiscUtil.getColumnIndex(poSocMed, fsIndex));
    }
    public int getItemCount() throws SQLException{
        poSocMed.last();
        return poSocMed.getRow();
    }
    
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_SocMed(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poSocMed = factory.createCachedRowSet();
            poSocMed.populate(loRS);
            MiscUtil.close(loRS);
            
            poSocMed.last();
            poSocMed.moveToInsertRow();
            
            MiscUtil.initRowSet(poSocMed);       
            poSocMed.updateString("cRecdStat", RecordStatus.ACTIVE);
            poSocMed.updateString("cSocialTp", "0");
            
            poSocMed.insertRow();
            poSocMed.moveToCurrentRow();           
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;      
    }
    
//    public boolean SearchRecord(){
//        return OpenRecord("");
//    }
    
    public boolean OpenRecord(String fsValue, boolean fbByUserID){
        try {
            String lsSQL;
            ResultSet loRS;
            //RowSetFactory factory = RowSetProvider.newFactory();
            //open master
            if (fbByUserID)
                lsSQL = MiscUtil.addCondition(getSQ_SocMed(), "sSocialID = " + SQLUtil.toSQL(fsValue));
            else 
                lsSQL = MiscUtil.addCondition(getSQ_SocMed(), "sClientID = " + SQLUtil.toSQL(fsValue));
            
            loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poSocMed = factory.createCachedRowSet();
            poSocMed.populate(loRS);
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
                poSocMed.beforeFirst();
                while (poSocMed.next()){
                    String lsSocialID = MiscUtil.getNextCode(SOCMED_TABLE, "sSocialID", true, poGRider.getConnection(), psBranchCd);
                    poSocMed.updateString("sClientID", psClientID);
                    poSocMed.updateObject("sSocialID", lsSocialID);
                    poSocMed.updateString("sEntryByx", poGRider.getUserID());
                    poSocMed.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                    poSocMed.updateString("sModified", poGRider.getUserID());
                    poSocMed.updateObject("dModified", (Date) poGRider.getServerDate());
                    poSocMed.updateRow();

                    lsSQL = MiscUtil.rowset2SQL(poSocMed, SOCMED_TABLE, "");

                    if (poGRider.executeQuery(lsSQL, SOCMED_TABLE, psBranchCd, lsSocialID.substring(0, 4)) <= 0){
                        if (!pbWithParent) poGRider.rollbackTrans();
                        psMessage = poGRider.getMessage() + " ; " + poGRider.getErrMsg();
                        return false;
                    }

                    lnCtr++;
                }
               
            } else { //update
               
                if (!pbWithParent) poGRider.beginTrans();
                
                lnCtr = 1;
                poSocMed.beforeFirst();
                while (poSocMed.next()){
                    String lsSocialID = (String) getSocMed(lnCtr, "sSocialID");
                    poSocMed.updateString("sModified", poGRider.getUserID());
                    poSocMed.updateObject("dModified", (Date) poGRider.getServerDate());
                    poSocMed.updateRow();
                    lsSQL = MiscUtil.rowset2SQL(poSocMed, 
                                                SOCMED_TABLE, 
                                                "", 
                                                "sSocialID = " + SQLUtil.toSQL(lsSocialID) +
                                                " AND sClientID = " + SQLUtil.toSQL((String) getSocMed(lnCtr,"sClientID")));

                    if (!lsSQL.isEmpty()){
                        if (poGRider.executeQuery(lsSQL, SOCMED_TABLE, psBranchCd, lsSocialID.substring(0, 4)) <= 0){
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
    
    private String getSQ_SocMed(){
        return "SELECT" +
                    " sSocialID "  + //1
                    ", sClientID " + //2
                    ", sAccountx " + //3
                    ", cSocialTp " + //4
                    ", cRecdStat " + //5
                    ", sEntryByx " + //6
                    ", dEntryDte " + //7
                    ", sModified " + //8
                    ", dModified " + //9
                " FROM " + SOCMED_TABLE;
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poSocMed.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poSocMed.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poSocMed.getMetaData().getColumnType(lnCtr));
            if (poSocMed.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poSocMed.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poSocMed.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    } 
    
    private boolean isEntryOK() throws SQLException{
        poSocMed.first();
        
        if (poSocMed.getString("sAccountx").isEmpty()){
            psMessage = "Account is not set.";
            return false;
        }
        
        if (poSocMed.getString("cSocialTp").isEmpty()){
            psMessage = "Social Type is not set.";
            return false;
        }
        
        //validate max size of string variables
        
        return true;
    }
}
