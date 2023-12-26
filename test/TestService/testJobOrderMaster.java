/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestService;

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
import org.rmj.auto.service.base.JobOrderMaster;

/**
 *
 * @author Arsiela
 * Date Created: 12-18-2023
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testJobOrderMaster {
    static GRider instance = new GRider();
    static JobOrderMaster trans;
    static MasterCallback callback;
    
    public testJobOrderMaster(){}
    
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
        
        trans = new JobOrderMaster(instance, instance.getBranchCode(), false);
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
//            try {
//                
//            } catch (SQLException e) {
//                fail(e.getMessage());
//            }
        } else {
            fail(trans.getMessage());
        }
        assertTrue(result);
        
    }
    
//    @Test
//    public void test05AddVSPParts(){
//        try {
//            boolean result =(trans.AddVSPParts());
//            
//            trans.setVSPPartsDetail(1, "sDescript", "BUMPER");
//            trans.setVSPPartsDetail(1, "sChrgeTyp", "0");
//            trans.setVSPPartsDetail(1, "sChrgeTox", "0");
//            trans.setVSPPartsDetail(1, "nQuantity", 1);
//            trans.setVSPPartsDetail(1, "nSelPrice", 1500.00);
//            
//            result =(trans.AddVSPParts());
//            
//            trans.setVSPPartsDetail(2, "sDescript", "BUMPER2");
//            trans.setVSPPartsDetail(2, "sChrgeTyp", "0");
//            trans.setVSPPartsDetail(2, "sChrgeTox", "0");
//            trans.setVSPPartsDetail(2, "nQuantity", 1);
//            trans.setVSPPartsDetail(2, "nSelPrice", 1505.00);
//            assertTrue(result);
//        } catch (SQLException ex) {
//            Logger.getLogger(testVehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
    
//    @Test
//    public void test06RemoveVSPParts(){
//        try {
//            boolean result =(trans.removeVSPParts(1));
//            assertTrue(result);
//        } catch (SQLException ex) {
//            Logger.getLogger(testVehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
//    @Test
//    public void test07AddVSPLabor(){
//        try {
//            boolean result =(trans.addVSPLabor("",false));
//            
//            trans.setVSPLaborDetail(1, "sLaborDsc", "TINT");
//            trans.setVSPLaborDetail(1, "sChrgeTyp", "0");
//            trans.setVSPLaborDetail(1, "sChrgeTox", "0");
//            trans.setVSPLaborDetail(1, "sRemarksx", "SAMPLE LANG ITO");
//            trans.setVSPLaborDetail(1, "nLaborAmt", 1000.00);
//            
//            result =(trans.addVSPLabor("",false));
//            
//            trans.setVSPLaborDetail(2, "sLaborDsc", "TINT2");
//            trans.setVSPLaborDetail(2, "sChrgeTyp", "0");
//            trans.setVSPLaborDetail(2, "sChrgeTox", "0");
//            trans.setVSPLaborDetail(2, "sRemarksx", "SAMPLE LANG ITO num 2");
//            trans.setVSPLaborDetail(2, "nLaborAmt", 1022.00);
//            assertTrue(result);
//        } catch (SQLException ex) {
//            Logger.getLogger(testVehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
//    @Test
//    public void test08RemoveVSPLabor(){
//        try {
//            boolean result =(trans.removeVSPLabor(1));
//            assertTrue(result);
//        } catch (SQLException ex) {
//            Logger.getLogger(testVehicleSalesProposalMaster.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
    
//    @Test
//    public void test01OpenRecord(){
//        boolean result =(trans.OpenRecord("V00123000005"));
//        assertTrue(result);
//    }
    
//    @Test
//    public void test02UpdateRecord(){
//        boolean result =(trans.UpdateRecord());
//        if (result){
//            try {
//                trans.setMaster("sVSPNOxxx", "VSP082520233");
//                     
//            } catch (SQLException e) {
//                fail(e.getMessage());
//            }
//        } else {
//            fail(trans.getMessage());
//        }
//        assertTrue(result);
//    }
//    
    
//    @Test
//    public void test09SaveRecord(){
//        boolean result =(trans.SaveRecord());
//        assertTrue(result);
//        
//        //assertFalse(result);
//        //assertEquals("", trans.getMessage());
//        
//    }
    
//    @Test
//    public void test06CancelRecord(){
//        boolean result =(trans.cancelVSP(false));
//        assertTrue(result);
//        
//        //assertFalse(result);
//        //assertEquals("", trans.getMessage());
//    
//    }
    
    @Test
    public void test07CheckVSPJOParts() throws SQLException{
        boolean result =(trans.checkVSPJOParts("V00123000026", 9,false,0));
        assertTrue(result);
        
        //assertFalse(result);
        //assertEquals("", trans.getMessage());
    
    }
    
    @Test
    public void test10DisplayList(){
        try {
            int lnRow = trans.getItemCount();
            System.out.println("------------------LIST----------------------");
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                for (int lnIndex = 1; lnIndex <= 39; lnIndex++){
                    System.out.print(trans.getDetail(lnCtr, lnIndex));
                    System.out.print("\t");     
                }
                System.out.println("\t");
            }
            System.out.println("----------------------------------------");
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
}
