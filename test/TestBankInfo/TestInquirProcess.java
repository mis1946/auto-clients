/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestBankInfo;

import static TestBankInfo.TestBankInfo.trans;
import com.sun.corba.se.impl.logging.POASystemException;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.sales.base.InquiryProcess;

/**
 *
 * @author User
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInquirProcess {
    static GRider instance = new GRider();
    static InquiryProcess trans;
    static MasterCallback callback;
    
    public TestInquirProcess(){}
    
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
        
        trans = new InquiryProcess(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
//    @Test
//    public void test01LoadReservation(){
//        String[] fsValues = {"V00123000002", "V00123000003", "V00123000004", "V00123000005", "V00123000006"};
//        trans.loadReservation(fsValues, false);
//    }
//    @Test
//    public void Test01LoadrequirementsSource() throws SQLException{
//        boolean result = trans.loadRequirementsSource("ofw", "f");
//        trans.setTransNox("V00123000001");
//        // Assert
//        assertTrue(result);
//        CachedRowSet rowSet = trans.poInqReqSrc;
//        assertNotNull(rowSet);
//        while (rowSet.next()) {
//            System.out.println("Requirement code " + rowSet.getString("sRqrmtCde"));
//            System.out.println("Description: " + rowSet.getString("sDescript"));
//            //System.out.println("Paymode: " + rowSet.getString("cPayModex"));
//            //System.out.println("Paygroup: " + rowSet.getString("cCustGrpx"));
//           // System.out.println("Is Required: " + rowSet.getBoolean("lRequired"));
//            System.out.println();
//        }
//    }
    
//    @Test
//    public void Test02Loadrequirements() throws SQLException{
//        boolean result = trans.loadRequirements("V00123000001");
//
//        // Assert
//        assertTrue(result);
//        CachedRowSet rowSet = trans.poInqReq;
//        assertNotNull(rowSet);
//        while (rowSet.next()) {
//            System.out.println("sTransno " + rowSet.getString("sTransNox"));
//            System.out.println("Description: " + rowSet.getString("sDescript"));
//            System.out.println("Paymode " + rowSet.getString("cPayModex"));
//            System.out.println("cCustGrpx: " + rowSet.getString("cCustGrpx"));
//            System.out.println("Submitted " + rowSet.getString("cSubmittd"));            
//            System.out.println();
//        }
//    }
    
    @Test
    public void Test01Addrequirements() throws SQLException{
        trans.NewRecord();
        trans.setTransNox("V00123000001");
        trans.addRequirements();
        trans.setInqReq(1,"sTransNox", "V00123000001");
        trans.setInqReq(1,"sRqrmtCde", "2");
        trans.setInqReq(1,"cSubmittd", "1");
        trans.addRequirements();
        trans.setInqReq(2,"sTransNox", "V00123000001");
        trans.setInqReq(2,"sRqrmtCde", "1");
        trans.setInqReq(2,"cSubmittd", "1");
        trans.addRequirements();
        trans.setInqReq(3,"sTransNox", "V00123000001");
        trans.setInqReq(3,"sRqrmtCde", "3");
        trans.setInqReq(3,"cSubmittd", "1");
        
    }
    
    @Test
    public void Test02addreserve() throws SQLException{
//        trans.NewRecord();
//        trans.setTransNox("V00123000001");
        trans.addReserve();
        //trans.setInqRsv(1,"sTransNox", "0505202301");
        trans.setInqRsv(1,"dTransact", instance.getServerDate());        
        //trans.setInqRsv(1,"cResrvTyp", 1);
        trans.setInqRsv(1,"sSourceNo", "V00123000001");
        trans.setInqRsv(1,"nAmountxx", 50000.00);
        
        trans.addReserve();
        //trans.setInqRsv(1,"sTransNox", "0505202301");
        
        trans.setInqRsv(2,"dTransact", instance.getServerDate());        
        //trans.setInqRsv(2,"cResrvTyp", 2);
        trans.setInqRsv(2,"sSourceNo", "V00123000002");
        trans.setInqRsv(2,"nAmountxx", 10000.00);
        
    }
        
    @Test
    public void Test03SaveRecord() throws SQLException{
        if (trans.SaveRecord()){
            System.out.println("Record saved successfully.");
        } else {
            fail(trans.getMessage());
        }
    }
      
    @Test
    public void test04LoadReservationByCode() {
        // Test loading reservation by code with single value
        String[] lsSrcCode = { "V00123000001" };
        boolean lbLoad = trans.loadReservation(lsSrcCode, true);
        assertTrue(lbLoad);

        // Test loading reservation by code with multiple values
        String[] lsSrcCode2 = { "V00123000001", "V00123000007" };
        lbLoad = trans.loadReservation(lsSrcCode2, true);
        assertTrue(lbLoad);

        // Test loading reservation by code with no values
        String[] lsSrcCode3 = {};
        lbLoad = trans.loadReservation(lsSrcCode3, true);
        assertFalse(lbLoad);
    }
    
    @Test
    public void test05LoadReservationByTransNox() {
        // Test loading reservation by trans nox with single value
        String[] lsTransNox = { "V00123000002" };
        boolean lbLoad = trans.loadReservation(lsTransNox, false);
        assertTrue(lbLoad);

        // Test loading reservation by trans nox with multiple values
        String[] lsTransNox2 = { "V00123000002", "V00123000003", "V00123000004", "V00123000005", "V00123000006" };
        lbLoad = trans.loadReservation(lsTransNox2, false);
        assertTrue(lbLoad);

        // Test loading reservation by trans nox with no values
        String[] lsTransNox3 = {};
        lbLoad = trans.loadReservation(lsTransNox3, false);
        assertFalse(lbLoad);
    }
//    @Test
//    public void Test03ApproveReservation() throws SQLException{
//        trans.ApproveReservation("01");
//    }
}
