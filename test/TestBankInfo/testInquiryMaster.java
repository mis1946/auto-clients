/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestBankInfo;

import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.sales.base.InquiryMaster;

/**
 *
 * @author User
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testInquiryMaster {
    static GRider instance = new GRider();
    static InquiryMaster trans;
    static MasterCallback callback;
    
    public testInquiryMaster(){}
    
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
        
        trans = new InquiryMaster(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Test
    public void test01NewRecord(){
        if (trans.NewRecord()){
            try {
                trans.displayMasFields();
                //trans.displayInqPromoFields();
                trans.addVhclPrty();
                trans.displayVhclPrtyFields();
                
                //trans.displayMasFields();
                //assertEquals("Cuison", (String) trans.getMaster("sLastName"));
//                trans.setMaster("sBankName", "BDO");
//                trans.setMaster("sBankBrch", "Dagupan");
//                trans.setMaster("sContactP", "Jahn Arcilla");
//                trans.setMaster("sTelNoxxx", "1234567");
//                trans.setMaster("sFaxNoxxx", "7654321");
//                trans.setMaster("sAddressx", "Bued magno");
                //trans.setMaster("sMiddName", "Igarta");
//                trans.OpenRecord("V00123000005");
//                trans.UpdateRecord();
                trans.searchCustomer("arcilla, jahn bron",false);
                trans.searchSalesExec("arcilla, jahn bron",false);
                trans.searchPlatform("facebook", true);
                //trans.setMaster("dTargetDt",SQLUtil.toDate("2023-05-01", SQLUtil.FORMAT_SHORT_DATE));
                //trans.searchCustomer("arcilla, jahn", false);
                //trans.searchVhclPrty(1,"GEELY EMGRAND 1.5 CVT WHITE 2000", false);
                //trans.searchVhclPrty(1,"GEELY EMGRAND 1.5 CVT WHITE 2000", false);
               // trans.searchSpouse("arcil", false);
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(trans.getMessage());
        }
    }
//    @Test
//    public void test02LoadCustomer() throws SQLException{
//        trans.loadCustomer("", "2021-01-01", "2023-04-27",false);
//        //trans.OpenRecord(fsValue)
//        System.out.println(trans.getInquiryMasterCount());
//    }
    
    @Test
    public void test03SaveRecord() throws SQLException{
        if (trans.SaveRecord()){
            System.out.println("Record saved successfully.");
        } else {
            fail(trans.getMessage());
        }
    }
    
}
