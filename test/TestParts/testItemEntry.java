/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestParts;

import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.parts.base.ItemEntry;

/**
 *
 * @author Arsiela
 * Date Created: 08-02-2023
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testItemEntry {
    static GRider instance = new GRider();
    static ItemEntry trans;
    static MasterCallback callback;
    
    public testItemEntry(){}
    
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
        
        trans = new ItemEntry(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @AfterClass
    public static void tearDownClass(){
    
    }
    
     @Test
    public void test01NewRecord() throws SQLException{
        boolean result =(trans.NewRecord());
        if (result){
            try {
                //trans.displayMasFields();
                trans.setMaster(2, "LANG3");
                trans.setMaster(3, "LANG");
                trans.setMaster(4, "LANG");
                trans.setMaster(5, "LANG");
                trans.setMaster(9, "LANG");
                trans.setMaster(11, "LANG");
                trans.setMaster(12, "LANG");
                trans.setMaster(27, "LANG");
                trans.setMaster(32, "LANG");
                trans.setMaster(33, "LANG");
                trans.setMaster(34, "LANG");
                trans.setMaster(35, "LANG");
                trans.setMaster(36, "LANG");
                trans.setMaster(37, "LANG");
                     
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
        //assertEquals("Existing Bin Description.", trans.getMessage());
        
    }
    
//    @Test
//    public void test02OpenRecord(){
//        boolean result =(trans.OpenRecord("002"));
//        assertTrue(result);
//    }
//    
    @Test
    public void test03LoadList() throws SQLException{
        boolean result =(trans.LoadMasterList());
        assertTrue(result);
    }
    
    @Test
    public void test04DisplayList(){
        try {
            int lnRow = trans.getMasterDetailCount();
                
            System.out.println("----------------------------------------");
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                //System.out.print(trans.getDetail(lnCtr, "sStockIDx"));
                for (int lnIndex = 1; lnIndex <= 37; lnIndex++){
                    System.out.print(trans.getDetail(lnCtr, lnIndex));
                    System.out.print("\t");     
                }
                
                
            }
            System.out.println("----------------------------------------");
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
}
