/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestCashiering;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.cashiering.base.UnitSalesInvoice;

/**
 *
 * @author MIS
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testUnitSalesInvoice {    
    static GRider instance = new GRider();
    static UnitSalesInvoice trans;
    static MasterCallback callback;
    
    public testUnitSalesInvoice(){}
    
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
        
        trans = new UnitSalesInvoice(instance, instance.getBranchCode(), false);
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
                trans.setMaster("sReferNox", "09272023");
                trans.setMaster("cFormType", "1");
                trans.setMaster("dTransact", instance.getServerDate()); 
                     
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(trans.getMessage());
        }
        assertTrue(result);
        
    }
    
    @Test
    public void test02SearchUdr(){
        try {
            boolean result =(trans.searchUDR("09272023","1"));
            assertTrue(result);
        } catch (SQLException ex) {
            Logger.getLogger(testUnitSalesInvoice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
       
    @Test      
    public void test03ComputeAmount() throws SQLException{
        try {
            boolean result = trans.computeAmount();
            System.out.println("Vat Rate : " + trans.getMaster("nVatRatex"));
            System.out.println("Vat Amount : " + trans.getMaster("nVatAmtxx"));
            System.out.println("Trans Total : " + trans.getMaster("nTranTotl"));
            assertTrue(result);
        } catch (SQLException ex) {
            Logger.getLogger(testUnitSalesInvoice.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    @Test
    public void test04SaveRecord(){
        boolean result =(trans.SaveRecord());
        assertTrue(result);
        
        //assertFalse(result);
        //assertEquals("", trans.getMessage());
        
    }  
//    @Test
//    public void test05SearchRecord(){
//        boolean result = trans.SearchRecord();
//        assertTrue(result);
//    }
}
