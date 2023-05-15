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
import org.rmj.auto.clients.base.BankInformation;

/**
 *
 * @author User
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBankInfo {
    
    static GRider instance = new GRider();
    static BankInformation trans;
    static MasterCallback callback;
    
    public TestBankInfo(){}
    
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
        
        trans = new BankInformation(instance, instance.getBranchCode(), false);
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
                //trans.displayMasFields();
                //assertEquals("Cuison", (String) trans.getMaster("sLastName"));
                trans.setMaster("sBankName", "BDO");
                trans.setMaster("sBankBrch", "Dagupan");
                trans.setMaster("sContactP", "Jahn Arcilla");
                trans.setMaster("sTelNoxxx", "1234567");
                trans.setMaster("sFaxNoxxx", "7654321");
                trans.setMaster("sAddressx", "Bued magno");
                //trans.setMaster("sMiddName", "Igarta");
                               
      
                trans.searchProvince("Panga", false);
                trans.searchTown("Labrador", false);
               // trans.searchSpouse("arcil", false);
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(trans.getMessage());
        }
    }
    
    @Test
    public void test02SaveRecord(){
        if (trans.SaveRecord()){
            System.out.println("Record saved successfully.");
        } else {
            fail(trans.getMessage());
        }
    }
    
    @Test
    public void test03LoadList() throws SQLException{
        trans.LoadList("");
    }
    
    @Test
    public void test04DisplayBank(){
        try {
            int lnRow = trans.getItemCount();
                
            System.out.println("----------------------------------------");
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                System.out.print(trans.getDetail(lnCtr, "sBankName"));
                    System.out.print("\t");
                    System.out.print(trans.getDetail(lnCtr, "sBankBrch"));
                    System.out.print("\t");
                    System.out.print(trans.getDetail(lnCtr, "sContactP"));
                    System.out.print("\t");
                    System.out.print(trans.getDetail(lnCtr, "sTelNoxxx"));
                    System.out.print("\t");
                    System.out.print(trans.getDetail(lnCtr, "sFaxNoxxx"));
                    System.out.print("\t");
                    System.out.print(trans.getDetail(lnCtr, "sAddressx"));
                    System.out.println("");
            }
            System.out.println("----------------------------------------");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
}

