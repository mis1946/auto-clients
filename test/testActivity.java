
import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.clients.base.Activity;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testActivity {
    static GRider instance = new GRider();
    static Activity trans;
    static MasterCallback callback;
    
    public testActivity(){}
    
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
        
        trans = new Activity(instance, instance.getBranchCode(), false);
        trans.setWithUI(true);
        trans.setCallback(callback);
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Test
    public void test01NewRecord() throws SQLException{
        // Call the method under test
        boolean result = trans.NewRecord();
        //trans.displayMemberFields();
        trans.displayMasFields();
        trans.setMaster("sActTitle", "testtitle");
        trans.setMaster("sActDescx", "testdesc");
        trans.setMaster("sActTypID", "tes");
        trans.setMaster("sActSrcex", "testsource");
        trans.setMaster("sLocation", "tes");
        trans.setMaster("sCompnynx", "testestablishment");
        trans.setMaster("sLogRemrk", "testlogremarks");
        trans.setMaster("sRemarksx", "testremarks");
        trans.searchEmployee("arcilla");
        trans.searchDepartment("mis");
        trans.searchProvince("Pangasinan");
        // Verify the expected behavior and assertions
        assertTrue(result); // Assert that the method returns true
        //assertEquals(0, trans.poActMember.size()); 
    }
    
    @Test
    public void test02LoadEmployee() throws SQLException{
        trans.loadEmployee("A010",true);
        // Print the column names
        for (int i = 1; i <= trans.poEmployees.getMetaData().getColumnCount(); i++) {
            System.out.print(trans.poEmployees.getMetaData().getColumnName(i) + "\t");
        }
        System.out.println();

        // Print the data rows
        while (trans.poEmployees.next()) {
            for (int i = 1; i <= trans.poEmployees.getMetaData().getColumnCount(); i++) {
                System.out.print(trans.poEmployees.getString(i) + "\t");
            }
            System.out.println();
        }
    }
    
    @Test
    public void test03LoadDepartment() throws SQLException{
        trans.loadDepartment();
        // Print the column names
        for (int i = 1; i <= trans.poDepartment.getMetaData().getColumnCount(); i++) {
            System.out.print(trans.poDepartment.getMetaData().getColumnName(i) + "\t");
        }
        System.out.println();

        // Print the data rows
        while (trans.poDepartment.next()) {
            for (int i = 1; i <= trans.poDepartment.getMetaData().getColumnCount(); i++) {
                System.out.print(trans.poDepartment.getString(i) + "\t");
            }
            System.out.println();
        }
    }
    
    @Test
    public void test05searchDepartment() throws SQLException{
        boolean result = trans.searchDepartment("mis");

        // Verify the expected behavior and assertions
        assertTrue(result); // Assert that the method returns true
        // Assert any other expected behavior or assertions based on the mocked values
    }
    
    @Test
    public void test06searchEmployee() throws SQLException{
        
        boolean result = trans.searchEmployee("Arcilla");
        assertTrue(result);
    }
    
    @Test
    public void test07AddMember() throws SQLException {
        // Call the method under test
        boolean result = trans.addMember("A00118000198", "Arcilla, Jahn Randolph Bron", "MIS");
        result = trans.addMember("A00118000199", "Arcilla, Jahn Randolph Bron", "PARTS");
        
        // Verify the expected behavior and assertions
        //assertTrue(result); // Assert that the method returns true
        // Assert any other expected behavior or assertions based on the mocked values
        
        // Print the column names
        for (int i = 1; i <= trans.poActMember.getMetaData().getColumnCount(); i++) {
            System.out.print(trans.poActMember.getMetaData().getColumnName(i) + "\t");
        }
        System.out.println();

        // Print the data rows
        while (trans.poActMember.next()) {
            for (int i = 1; i <= trans.poActMember.getMetaData().getColumnCount(); i++) {
                System.out.print(trans.poActMember.getString(i) + "\t");
            }
            System.out.println();
        }
    }
    
    @Test
    public void test08searchProvince() throws SQLException{
        boolean result = trans.searchProvince("Pangasinan");
        assertTrue(result);
        
    }
    
    @Test
    public void test09loadTown() throws SQLException{
        boolean result = trans.loadTown("01",true);
    }
    
    @Test
    public void test10LoadAndAddTown() throws SQLException {
        String testProvince = "01"; // Replace with the desired test province
        String testTownID = "0314"; // Replace with the desired test town ID
        String testTownName = "Test Town"; // Replace with the desired test town name
        String testTownID2 = "0315"; // Replace with the desired test town ID
        String testTownName2 = "Lingayen"; // Replace with the desired test town name
        String testTownID3 = "0318"; // Replace with the desired test town ID
        String testTownName3 = "Alaminos City"; // Replace with the desired test town name
        
        // Load town
       // boolean loadResult = trans.loadTown(testProvince,true);
        //assertTrue("The town should be loaded successfully", loadResult);
        
        // Add town
       // boolean addResult = 
        trans.addActTown(testTownID, testTownName);
        trans.addActTown(testTownID2, testTownName2);
        trans.addActTown(testTownID3, testTownName3);
        //assertTrue("The town should be added successfully", addResult);
        
         //Print the column names
        for (int i = 1; i <= trans.poActTown.getMetaData().getColumnCount(); i++) {
            System.out.print(trans.poActTown.getMetaData().getColumnName(i) + "\t");
        }
        System.out.println();

        // Print the data rows
        while (trans.poActTown.next()) {
            for (int i = 1; i <= trans.poActTown.getMetaData().getColumnCount(); i++) {
                System.out.print(trans.poActTown.getString(i) + "\t");
            }
            System.out.println();
        }
        
    }
//    @Test
//    public void test11LoadVehicle() throws SQLException{
//        boolean result = trans.loadActVehicle("", false);
//        assertTrue(result);
//        
//        for (int i = 1; i <= trans.poVehicle.getMetaData().getColumnCount(); i++) {
//            System.out.print(trans.poVehicle.getMetaData().getColumnName(i) + "\t");
//        }
//        System.out.println();
//
//        // Print the data rows
//        while (trans.poVehicle.next()) {
//            for (int i = 1; i <= trans.poVehicle.getMetaData().getColumnCount(); i++) {
//                System.out.print(trans.poVehicle.getString(i) + "\t");
//            }
//            System.out.println();
//        }
//    }
    
    @Test
    public void test12AddVehicle() throws SQLException{
        String fsDescript = "HONDA HATCHBACK 1.4L AT BLUE 2023" ;
        String fsSerialID = "V00123000001"   ;  
        String fsCSNOxxxx = "EA5555"   ;  
        boolean result = trans.addActVehicle(fsSerialID,fsDescript,fsCSNOxxxx);
        //trans.addActVehicle(fsDescript, fsSerialID,fsCSNOxxxx);
        assertTrue(result);
        for (int i = 1; i <= trans.poActVehicle.getMetaData().getColumnCount(); i++) {
            System.out.print(trans.poActVehicle.getMetaData().getColumnName(i) + "\t");
        }
        System.out.println();

        // Print the data rows
        while (trans.poActVehicle.next()) {
            for (int i = 1; i <= trans.poActVehicle.getMetaData().getColumnCount(); i++) {
                System.out.print(trans.poActVehicle.getString(i) + "\t");
            }
            System.out.println();
        }       
    }
//    
//    @Test
//    public void test13SearchRecord() throws SQLException{
//        boolean result = trans.SearchRecord("1", true);
//        assertTrue(result);
//    }
//    
    @Test
    public void test13SearchBranch() throws SQLException{
         trans.searchBranch("MCC LUC");
    }
    
    @Test
    public void test14SaveRecord(){
        boolean result = trans.SaveRecord();
        assertTrue(result);
    }    
    
}
