/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestBankInfo;

import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.sales.base.InquiryFollowUp;

/**
 *
 * @author User
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInquiryFollowup {
    static GRider instance = new GRider();
    static InquiryFollowUp trans;
    static MasterCallback callback;
    
    public TestInquiryFollowup(){}
    
    @BeforeClass
    public static void setUpClass() {   
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        if (!instance.logUser("AutoApp", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        callback = new MasterCallback() {
            @Override
            public void onSuccess(int fnIndex, Object foValue) {
                System.out.println(fnIndex + "-->>" + foValue);
            }
        };
        
        trans = new InquiryFollowUp(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Test
    public void test01NewRecord() throws SQLException{
        if (trans.NewRecord()){
            try {
                trans.displayMasFields();
                
                trans.setTransNox("V00123000010");
                trans.setFollowUp("dTransact", instance.getServerDate());
                //trans.setFollowUp("dTransact", instance.getServerDate());
                trans.setFollowUp("sRemarksx", "12345678912345678912");
                trans.setFollowUp("sMessagex", "TESTING TESTING MESSAGE");
                trans.setFollowUp("sMethodCd", "METHODCD");
                trans.setFollowUp("sSclMedia", "TEST SOCIALMEDIA");
                trans.setFollowUp("dFollowUp", instance.getServerDate());
               
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(trans.getMessage());
        }
       
        
        //assertTrue(result);
    }
    
    @Test
    public void test02SaveRecord() throws SQLException{        
        if (trans.SaveRecord()){
            System.out.println("Record saved successfully.");
        } else {
            fail(trans.getMessage());
        }
    }
    @Test
    public void test03LoadFollowUp() throws SQLException{
        boolean result = trans.loadFollowUp((String)trans.getFollowUp("sTransNox"), false);       
        assertTrue(result);
        assertEquals("SUCCESS",trans.psMessage);
    }
    
}
