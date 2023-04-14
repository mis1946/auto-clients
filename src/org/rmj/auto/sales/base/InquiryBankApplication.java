/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.sales.base;

import java.sql.SQLException;
import java.sql.Types;
import javax.sql.rowset.CachedRowSet;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;

/**
 *
 * @author User
 */
public class InquiryBankApplication {
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poBankApp;
    
    public InquiryBankApplication(GRider foGRider, String fsBranchCd, boolean fbWithParent){                    
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
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    //TODO add setBankApp for Bank Application
    public void setBankApp(int fnIndex, Object foValue) throws SQLException{
        poBankApp.first();
    }     
    //Bank Application setter
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setBankApp(MiscUtil.getColumnIndex(poBankApp, fsIndex), foValue);
    }
    //Bank Application getter
    public Object getBankApp(String fsIndex) throws SQLException{
        return getBankApp(MiscUtil.getColumnIndex(poBankApp, fsIndex));
    }
    //Bank Application getter
    public Object getBankApp(int fnIndex) throws SQLException{
        poBankApp.first();
        return poBankApp.getObject(fnIndex);
    }
    
    //Bank Application COUNT
    public int getBankAppCount() throws SQLException{
        if (poBankApp != null){
            poBankApp.last();
            return poBankApp.getRow();
        }else{
            return 0;
        }              
    }
    
    //TODO add new record details
    public boolean NewRecord(){
        return true;
    }
    
    //TODO Search Record for Bank Application
    public boolean SearchRecord(){
        return true;
    }
    
    //TODO openrecord when Bank Application is double clicked
    public boolean OpenRecord(){
        return true;
    }
    
    public boolean UpdateRecord(){
        pnEditMode = EditMode.UPDATE;
        return true;        
    }
    
    //TODO Saverecord for saving
    public boolean SaveRecord(){
        return true;
    }
    
    //TODO query for retrieving bank application
    private String getSQ_BankApp(){
        return "";
    }
    
    //TODO query for retrieving bank
    private String getSQ_Bank(){
        return "";
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poBankApp.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poBankApp.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poBankApp.getMetaData().getColumnType(lnCtr));
            if (poBankApp.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poBankApp.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poBankApp.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }
    
    //TODO validation for entries
    public boolean isEntryOK(){
        return true;
    }    
}
