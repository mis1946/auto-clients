/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.parameters;

import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;

/**
 *
 * @author User
 */
public class CancellationMaster {
    private GRider poGRider;
    private String psBranchCd;
    public String psMessage;
    private int pnEditMode;
    private boolean pbWithParent;
    private MasterCallback poCallback;
       
    private boolean pbWithUI;

    public CancellationMaster(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
        
        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;                       
    }    
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    public int getEditMode(){
        return pnEditMode;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public boolean CancelForm(String fsReferNox,String fsRemarks,String fsSourceCD, String fsSourceNo){
        String lsSQL ="INSERT INTO cancellation_Master SET" +
                        " sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("cancellation_master", "sTransNox", true, poGRider.getConnection(), psBranchCd)) +
                        " ,dTransact = " + SQLUtil.toSQL(poGRider.getServerDate()) + 
                        " ,sRemarksx = " + SQLUtil.toSQL(fsRemarks) +
                        " ,sReferNox = " + SQLUtil.toSQL(fsReferNox) +
                        " ,sSourceCD = " + SQLUtil.toSQL(fsSourceCD) +
                        " ,sSourceNo = " + SQLUtil.toSQL(fsSourceNo) +                        
                        " ,sEntryByx = " + SQLUtil.toSQL(poGRider.getUserID()) +
                        " ,dEntryDte = " + SQLUtil.toSQL(poGRider.getServerDate());
        
        if (poGRider.executeQuery(lsSQL, "cancellation_master", psBranchCd,"") <= 0){
            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
            return false;
        }
                        
        psMessage = "Transaction successfully cancelled";
        pnEditMode = EditMode.UNKNOWN;
        return true;        
    }
    
}
