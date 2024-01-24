
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
import org.rmj.auto.clients.base.InsuranceInformation;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Arsiela
 * Date Created: 01-23-2024
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testInsuranceInformation {
    static GRider instance = new GRider();
    static InsuranceInformation trans;
    static MasterCallback callback;
    
    public testInsuranceInformation(){}
    
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
        
        trans = new InsuranceInformation(instance, instance.getBranchCode(), false);
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
                //trans.displayMasFields();
                trans.setMaster("sInsurNme", "TEST INSURANCE");
                     
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(trans.getMessage());
        }
        
    }
    
    @Test
    public void test02SaveRecord(){
        boolean result =(trans.SaveRecord());
        assertTrue(result);
//        if (trans.SaveRecord()){
//            System.out.println("Record saved successfully.");
//        } else {
//            fail(trans.getMessage());
//        }
    }
    
    @Test
    public void test03LoadList() throws SQLException{
        trans.loadList();
    }
    
    @Test
    public void test04Display(){
        try {
            int lnRow = trans.getItemCount();
                
            System.out.println("----------------------------------------");
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                System.out.print(trans.getDetail(lnCtr, "sInsurNme"));
                System.out.print("\t");
            }
            System.out.println("----------------------------------------");
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}

