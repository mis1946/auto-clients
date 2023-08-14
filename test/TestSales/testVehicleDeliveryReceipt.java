/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestSales;

import static TestSales.testVehicleReleaseform.trans;
import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.sales.base.VehicleDeliveryReceipt;

/**
 *
 * @author Arsiela
 * Date Created: 07-20-2023
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testVehicleDeliveryReceipt {
    static GRider instance = new GRider();
    static VehicleDeliveryReceipt trans;
    static MasterCallback callback;
    
    public testVehicleDeliveryReceipt(){}
    
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
        
        trans = new VehicleDeliveryReceipt(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @AfterClass
    public static void tearDownClass() {
        
    }
    
    @Test
    public void test01NewRecord() throws SQLException{
        boolean result =(trans.NewRecord());
        if (result){
            try {
                //trans.displayMasFields();
                trans.setMaster("sTransNox", "LANG");
                     
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(trans.getMessage());
        }
        assertTrue(result);
        
    }
    
    @Test
    public void test02SaveRecord(){
        boolean result =(trans.SaveRecord());
        assertTrue(result);
        
        //assertFalse(result);
        //assertEquals("", trans.getMessage());
        
    }
    
    @Test
    public void test04DisplayList(){
        try {
            int lnRow = trans.getItemCount();
                
            System.out.println("----------------------------------------");
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                System.out.print(trans.getDetail(lnCtr, "sTransNox"));
                System.out.print("\t");
            }
            System.out.println("----------------------------------------");
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
}
