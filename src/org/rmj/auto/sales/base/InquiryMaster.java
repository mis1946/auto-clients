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
 * @author Jahn April 4 2023
 */
public class InquiryMaster {
    //private final String MASTER_TABLE = "Inquiry_Master";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
    private CachedRowSet poTargVhcl;
    
    public InquiryMaster(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
        
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
    //TODO add setMaster for inquiry
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poMaster.first();
    }     
    //Inquiry Master setter
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poMaster, fsIndex), foValue);
    }
    //Inquiry Master getter
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    //Inquiry Master getter
    public Object getMaster(int fnIndex) throws SQLException{
        poMaster.first();
        return poMaster.getObject(fnIndex);
    }
    
    //INQUIRY SEARCH GETTER
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        poMaster.absolute(fnRow);
        return poMaster.getObject(fnIndex);
    }
    
    //INQUIRY SEARCH GETTER
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    
    //INQUIRY MASTER SEARCH COUNT
    public int getInquiryMasterCount() throws SQLException{
        if (poMaster != null){
            poMaster.last();
            return poMaster.getRow();
        }else{
            return 0;
        }              
    }
    
    public int getTargVhclCount() throws SQLException{
        if (poMaster != null){
            poMaster.last();
            return poMaster.getRow();
        }else{
            return 0;
        }              
    }
    
    //TODO for Priority/Target Vehicle tableview
    public boolean removeTargetVehicle(int fnRow) throws SQLException{
        if (getTargVhclCount() == 0) {
            psMessage = "No address to delete.";
            return false;
        }
        
        poTargVhcl.absolute(fnRow);
        poTargVhcl.deleteRow();
        return true;
    }
    
    //TODO add new record details
    public boolean NewRecord(){
        return true;
    }
    
    //TODO Search Record for inquiry
    public boolean SearchRecord(){
        return true;
    }
    
    //TODO openrecord when inquiry is double clicked
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
    
    //TODO query for retrieving inquiry
    private String getSQ_Master(){
        return "";
    }
    
    //TODO query for retrieving customer info
    private String getSQ_Customerinfo(){
        return "";
    }
        
    //TODO query for sales executives
    private String getSQ_SalesExecutives(){
        return "";
    }
    
    //TODO query for referal agents
    private String getSQ_Agents(){
        return "";
    }
        
    //TODO query for activity events
    private String getSQ_ActivityEvent(){
        return "";
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poMaster.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poMaster.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poMaster.getMetaData().getColumnType(lnCtr));
            if (poMaster.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poMaster.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poMaster.getMetaData().getColumnDisplaySize(lnCtr));
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

