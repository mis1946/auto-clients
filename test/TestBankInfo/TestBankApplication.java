/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestBankInfo;

import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.sales.base.InquiryBankApplication;

/**
 *
 * @author User
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBankApplication {
     static GRider instance = new GRider();
    static InquiryBankApplication trans;
    static MasterCallback callback;
    
    public TestBankApplication(){}
    
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
        
        trans = new InquiryBankApplication(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @AfterClass
    public static void tearDownClass() {
        
    }
    
    @Test
    public void test01searchbank() throws SQLException{
        
        trans.NewRecord();
        trans.displayMasFields();
        trans.pbWithUI = true;
        trans.setBankApp("cPayModex", "1" );
        trans.setBankApp("dAppliedx", "2023-05-01");
//        trans.setBankApp("cPayModex", "1" );
//        trans.setBankApp("cPayModex", "1" );
        trans.searchBank("LandBank", false);
        //trans.SaveRecord();
        
    }
    
    @Test
    public void test02SaveRecord(){
         trans.SaveRecord();
        //assertTrue(result);
        //assertEquals("",trans.psMessage);
    }
    
    //@Test
    public void test03CancelBankApp_successfulCancellation() throws SQLException {
        // Arrange
        //int fnRow = 1;
        // Set up mock data or insert test data here
        //trans.NewRecord();
        //trans.setBankApp("a.cTranStat", "1");
        //trans.setBankApp("a.sTransNox", "01");
        String fsValue = (String)trans.getBankApp("sTransNox");
        System.out.println(fsValue);
        //trans.CancelBankApp("01");
        // Act
        boolean result = trans.CancelBankApp(fsValue);
        
        // Assert
        assertTrue(result);
        assertEquals("Transaction successfully cancelled", trans.psMessage);
        // Add additional assertions as needed
    }
    
//    @Test
//    public void test03CancelBankApp_invalidTransactionStatus() throws SQLException {
//        // Arrange
//        int fnRow = 1;
//        // Set up mock data or insert test data here
//        // In this test case, set a transaction status that is not equal to "0"
//        trans.setBankApp("cTranStat", "1");
//        
//        // Act
//        boolean result = trans.CancelBankApp(fnRow);
//        
//        // Assert
//        assertFalse(result);
//        assertEquals("Unable to cancel transactions.", trans.psMessage);
//        // Add additional assertions as needed
//    }
//    
//    @Test
//    public void test04CancelReservation_invalidUpdateMode() throws SQLException {
//        // Arrange
//        int fnRow = 1;
//        // Set up mock data or insert test data here
//        // In this test case, set an invalid update mode
//        //yourObj.pnEditMode = EditMode.ADDNEW;
//        
//        // Act
//        boolean result = trans.CancelBankApp(fnRow);
//        
//        // Assert
//        assertFalse(result);
//        assertEquals("Invalid update mode detected.", trans.psMessage);
//        // Add additional assertions as needed
//    }
//    
//    // Add more test cases for other scenarios as needed
    
}
