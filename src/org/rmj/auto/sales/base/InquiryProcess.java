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
public class InquiryProcess {
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poInqProc;
    private CachedRowSet poReserve;
    
    public InquiryProcess(GRider foGRider, String fsBranchCd, boolean fbWithParent){                    
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
    
    //TODO add setInqProc for Inquiry Process
    public void setInqProc(int fnIndex, Object foValue) throws SQLException{
        poInqProc.first();
    }     
    //Inquiry Process setter
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setInqProc(MiscUtil.getColumnIndex(poInqProc, fsIndex), foValue);
    }
    //Inquiry Process getter
    public Object getIncProc(String fsIndex) throws SQLException{
        return getIncProc(MiscUtil.getColumnIndex(poInqProc, fsIndex));
    }
    //Inquiry Process getter
    public Object getIncProc(int fnIndex) throws SQLException{
        poInqProc.first();
        return poInqProc.getObject(fnIndex);
    }
    
    //Inquiry Process COUNT
    public int getBankAppCount() throws SQLException{
        if (poInqProc != null){
            poInqProc.last();
            return poInqProc.getRow();
        }else{
            return 0;
        }              
    }
    
    //Inquiry Reservation COUNT
    public int getReserveCount() throws SQLException{
        if (poInqProc != null){
            poInqProc.last();
            return poInqProc.getRow();
        }else{
            return 0;
        }              
    }
    
    //TODO add new record details
    public boolean NewRecord(){
        return true;
    }
    
    //TODO: add reservation
    private boolean addReserve(String fsCode) throws SQLException{
        int lnCtr;
        int lnRow = getReserveCount();
        
        
        poReserve.last();
        poReserve.moveToInsertRow();

        MiscUtil.initRowSet(poReserve);     
        poReserve.updateString("", fsCode);          

        poReserve.insertRow();
        poReserve.moveToCurrentRow();
        
        return true;
    }
    
    //TODO Search Record for Inquiry Process
    public boolean SearchRecord(){
        return true;
    }
    
    //TODO openrecord when Inquiry Process is double clicked
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
    
    //TODO query for retrieving Inquiry Process
    private String getSQ_InqProc(){
        return "";
    }
    
    //TODO query for retrieving reservation
    private String getSQ_Reserve(){
        return "";
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poInqProc.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poInqProc.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poInqProc.getMetaData().getColumnType(lnCtr));
            if (poInqProc.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poInqProc.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poInqProc.getMetaData().getColumnDisplaySize(lnCtr));
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
