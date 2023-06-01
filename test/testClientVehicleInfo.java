
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
import org.rmj.auto.clients.base.ClientVehicleInfo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Arsiela
 * Date Created: 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testClientVehicleInfo {
    static GRider instance = new GRider();
    static ClientVehicleInfo trans;
    static MasterCallback callback;
    
    public testClientVehicleInfo(){}
    
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
        
        trans = new ClientVehicleInfo(instance, instance.getBranchCode(), false);
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
                trans.setMaster("sMakeIDxx", "01");
                trans.setMaster("sModelIDx", "01");
                trans.setMaster("sTypeIDxx", "05");
                trans.setMaster("sColorIDx", "10");
                trans.setMaster("sTransMsn", "AT");
                trans.setMaster("nYearModl", "1999");
                trans.setMaster("sFrameNox", "0123");
                trans.setMaster("sEngineNo", "0143");
                trans.setMaster("sVhclIDxx", "V00123000002");
                trans.setMaster("sClientID", "V00123000081");
                trans.setMaster("sCSNoxxxx", "T0J111");
                trans.setMaster("sDealerNm", "HONDA CARS PANGASINAN INC.");
                trans.setMaster("sCompnyID", "");
                trans.setMaster("sKeyNoxxx", "T0J111");
                trans.setMaster("cIsDemoxx", "n");
                trans.setMaster("cLocation", "");
                trans.setMaster("cSoldStat", "");
                trans.setMaster("cVhclNewx", "");
                trans.setMaster("sPlateNox", "TES143"); 
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
//        if (trans.SaveRecord()){
//            System.out.println("Record saved successfully.");
//        } else {
//            fail(trans.getMessage());
//        }
    }
    
    @Test
    public void test03LoadList() throws SQLException{
        boolean result =(trans.LoadList("V00123000081"));
        assertTrue(result);
    }
    
    @Test
    public void test04DisplayVehicelMake(){
        try {
            int lnRow = trans.getItemCount();
                
            System.out.println("----------------------------------------");
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                System.out.print(trans.getDetail(lnCtr, "sSerialID"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sFrameNox"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sEngineNo"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sVhclIDxx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sClientID"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sCSNoxxxx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sDealerNm"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sKeyNoxxx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "cIsDemoxx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "cLocation"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "cSoldStat"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "cVhclNewx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sPlateNox"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "dRegister"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sPlaceReg"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sMakeIDxx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sMakeDesc"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sModelIDx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sModelDsc"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sTypeIDxx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sColorIDx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sColorDsc"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sTransMsn"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "nYearModl"));
                System.out.print("\t");
            }
            System.out.println("----------------------------------------");
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
