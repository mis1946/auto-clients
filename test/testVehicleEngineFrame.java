
import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.parameters.VehicleEngineFrame;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Arsiela
 * Date Created: 06-03-2023
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testVehicleEngineFrame {
    static GRider instance = new GRider();
    static VehicleEngineFrame trans;
    static MasterCallback callback;
    
    public testVehicleEngineFrame(){}
    
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
        
        trans = new VehicleEngineFrame(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @AfterClass
    public static void tearDownClass() {
        
    }
    
    @Test
    public void test01NewRecord() throws SQLException{
        trans.setCodeType(2);
        boolean result =(trans.NewRecord());
        if (result){
            try {
                //trans.displayMasFields();
                //trans.setMaster("sFrmePtrn", "L1N");
                //trans.setMaster("sFrmePtrn", "02");
                //trans.setMaster("nFrmeLenx", 6);
                trans.setMaster("sEngnPtrn", "J1N");
                trans.setMaster("nEngnLenx", 6);
                trans.setMaster("sMakeIDxx", "01");
                trans.setMaster("sMakeDesc", "01");
                trans.setMaster("sModelIDx", "01");
                trans.setMaster("sModelDsc", "01");
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
        //assertEquals("", trans.getMessage());
    }
    
    @Test
    public void test03LoadList() throws SQLException{
        boolean result =(trans.searchVhclEngineFrame(0));
        assertTrue(result);
    }

    @Test
    public void test04DisplayEngineFrame(){
        try {
            int lnRow = trans.getItemCount();
                
            System.out.println("----------------------------------------");
            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                System.out.print(trans.getDetail(lnCtr, "nEntryNox"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sPatternx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "nLengthxx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sEntryByx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "dEntryDte"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sMakeIDxx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sMakeDesc"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sModelIDx"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "sModelDsc"));
                System.out.print("\t");
                System.out.print(trans.getDetail(lnCtr, "nCodeType"));
                System.out.print("\t");
            }
            System.out.println("----------------------------------------");
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}