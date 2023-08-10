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
    
//    @Test
//    public void test01NewRecord() throws SQLException{
//        boolean result =(trans.NewRecord());
//        if (result){
//            try {
//                //trans.displayMasFields();
//                trans.setMaster("sBarCodex", "AUGUST-10");//2
//                trans.setMaster("sDescript", "TEST LANG AUGUST");//3
//                trans.setMaster("sBriefDsc", "TEST");//4
//                trans.setMaster( "sCategCd1","01");//5
//                trans.setMaster( "sBrandCde","01");//9
//                trans.setMaster("sMeasurID", "01");//11
//                trans.setMaster("sInvTypCd","01");//12
//                //trans.setMaster( "sTrimBCde","LANG");//27
//                trans.setMaster("sBrandNme","LANG");//32
//                trans.setMaster("sCategNme","LANG");//33
//                trans.setMaster( "sMeasurNm","LANG");//34
//                trans.setMaster( "sInvTypNm","LANG");//35
//                //trans.setMaster("sLocatnID", "LANG");//36
//                //trans.setMaster("sLocatnDs", "LANG");//37
//                     
//            } catch (SQLException e) {
//                fail(e.getMessage());
//            }
//        } else {
//            fail(trans.getMessage());
//        }
//        assertTrue(result);
//        
//    }
    
    @Test
    public void test01OpenRecord(){
        boolean result =(trans.OpenRecord("V00123000014"));
        assertTrue(result);
    }
    
    @Test
    public void test02UpdateRecord(){
        boolean result =(trans.UpdateRecord());
        if (result){
//            try {
                //trans.displayMasFields();
//                trans.setMaster("sBarCodex", "LANG - ITO TEST - 4");//2
//                trans.setMaster("sDescript", "TEST LANG");//3
//                trans.setMaster("sBriefDsc", "TEST");//4
//                trans.setMaster( "sCategCd1","01");//5
//                trans.setMaster( "sBrandCde","01");//9
//                trans.setMaster("sMeasurID", "01");//11
//                trans.setMaster("sInvTypCd","01");//12
//                //trans.setMaster( "sTrimBCde","LANG");//27
//                trans.setMaster("sBrandNme","LANG");//32
//                trans.setMaster("sCategNme","LANG");//33
//                trans.setMaster( "sMeasurNm","LANG");//34
//                trans.setMaster( "sInvTypNm","LANG");//35
                //trans.setMaster("sLocatnID", "LANG");//36
                //trans.setMaster("sLocatnDs", "LANG");//37
                     
//            } catch (SQLException e) {
//                fail(e.getMessage());
//            }
        } else {
            fail(trans.getMessage());
        }
        assertTrue(result);
    }
    
    @Test
    public void test03RemoveInvModel_Year(){
        Integer[] fnRowModel = new Integer[2];
        Integer[] fnRowModelYr = new Integer[1];
        fnRowModel[0] = 1;
        fnRowModel[1] = 2;
        fnRowModelYr[0] = 1;
        
        boolean result =(trans.removeInvModel_Year(fnRowModel, fnRowModelYr));
        assertTrue(result);
    }
    
    @Test
    public void test03AddInvModel_Year(){
        boolean result =(trans.addInvModel_Year("06", "ODYSSEY", "HONDA", 0, true));
       //result =(trans.addInvModel_Year("05", "HRV", "HONDA", 0, true));
       //result =(trans.addInvModel_Year("04", "HATCHBACK", "HONDA", 0, true));
        //result =(trans.addInvModel_Year("03", "CITY", "HONDA", 0, true));
//        trans.addInvModel_Year("02", "ACCORD", "HONDA", 0, true);
//        trans.addInvModel_Year("01", "CIVIC", "HONDA", 2021, false);
//        trans.addInvModel_Year("01", "CIVIC", "HONDA", 2022, false);
//        trans.addInvModel_Year("V00123000012", "COMMON", "COMMON", 0, true);
       
        assertTrue(result);
    }
    
    
    
    @Test
    public void test04DisplayInvModelList(){
        try {
            int lnRow = trans.getInvModelCount();
            System.out.println("---------INV MODEL COUNT >>>> " + lnRow);
            System.out.println("------------------INV MODEL----------------------");
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                //System.out.print(trans.getDetail(lnCtr, "sStockIDx"));
                for (int lnIndex = 1; lnIndex <= 6; lnIndex++){
                    System.out.print(trans.getInvModel(lnCtr, lnIndex));
                    System.out.print("\t");
                }
                System.out.println("\t");     
            }
            System.out.println("----------------------------------------");
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    
    @Test
    public void test05DisplayInvModelYrList(){
        try {
            int lnRow = trans.getInvModelYrCount();
            System.out.println("---------INV MODEL YEAR COUNT >>>> " + lnRow);
            System.out.println("-------------------INV MODEL YR---------------------");
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                //System.out.print(trans.getDetail(lnCtr, "sStockIDx"));
                for (int lnIndex = 1; lnIndex <= 5; lnIndex++){
                    System.out.print(trans.getInvModelYr(lnCtr, lnIndex));
                    System.out.print("\t");     
                }
                System.out.println("\t");  
            }
            System.out.println("----------------------------------------");
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void test06SaveRecord(){
        boolean result =(trans.SaveRecord());
        assertTrue(result);
        
//        assertFalse(result);
//        assertEquals("Part Number is not set.", trans.getMessage());
//        assertEquals("Part Description is not set.", trans.getMessage());
//        assertEquals("Part Brief Description is not set.", trans.getMessage());
//        assertEquals("Brand is not set.", trans.getMessage());
//        assertEquals("Inventory Type is not set.", trans.getMessage());
//        assertEquals("Category is not set.", trans.getMessage());
//        assertEquals("Measurement is not set.", trans.getMessage());
//        assertEquals("Existing Part Number.", trans.getMessage());
        
    }
    
//    @Test
//    public void test06LoadList() throws SQLException{
//        boolean result =(trans.LoadMasterList());
//        assertTrue(result);
//    }
//    
//    @Test
//    public void test07DisplayList(){
//        try {
//            int lnRow = trans.getMasterDetailCount();
//                
//            System.out.println("-------------------ITEM ENTRY LIST---------------------");
//            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
//                //System.out.print(trans.getDetail(lnCtr, "sStockIDx"));
//                for (int lnIndex = 1; lnIndex <= 37; lnIndex++){
//                    System.out.print(trans.getDetail(lnCtr, lnIndex));
//                    System.out.print("\t");     
//                }
//            }
//            System.out.println("----------------------------------------");
//            
//        } catch (Exception e) {
//            fail(e.getMessage());
//        }
//    }
//    
//    @Test
//    public void test08LoadVhclModelList() throws SQLException{
//        boolean result =(trans.loadVhclModel());
//        assertTrue(result);
//    }
//    
//    @Test
//    public void test09DisplayVhclModelList(){
//        try {
//            int lnRow = trans.getVhclModelCount();
//                
//            System.out.println("-------------------VEHICLE MODEL---------------------");
//            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
//                //System.out.print(trans.getDetail(lnCtr, "sStockIDx"));
//                for (int lnIndex = 1; lnIndex <= 4; lnIndex++){
//                    System.out.print(trans.getVhclModel(lnCtr, lnIndex));
//                    System.out.print("\t");
//                }
//                System.out.println("\t");     
//            }
//            System.out.println("----------------------------------------");
//            
//        } catch (Exception e) {
//            fail(e.getMessage());
//        }
//    }
//    
//    @Test
//    public void test10LoadVhclModelYrList() throws SQLException{
//        boolean result =(trans.loadVhclModelYr());
//        assertTrue(result);
//    }
//    
//    @Test
//    public void test11DisplayVhclModelYrList(){
//        try {
//            int lnRow = trans.getVhclModelYrCount();
//                
//            System.out.println("-------------------VEHICLE MODEL YR---------------------");
//            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
//                //System.out.print(trans.getDetail(lnCtr, "sStockIDx"));
//                //for (int lnIndex = 1; lnIndex <= 5; lnIndex++){
//                    System.out.print(trans.getVhclModelYr(lnCtr, "nYearModl"));
//                //    System.out.print("\t");     
//                //}
//                System.out.println("\t");  
//            }
//            System.out.println("----------------------------------------");
//            
//        } catch (Exception e) {
//            fail(e.getMessage());
//        }
//    }
    
}
