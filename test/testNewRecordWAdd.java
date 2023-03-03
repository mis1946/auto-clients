
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
import org.rmj.auto.clients.base.ClientAddress;
import org.rmj.auto.clients.base.ClientMaster;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testNewRecordWAdd {
    static GRider instance = new GRider();
    static ClientAddress trans;
    static ClientMaster poMaster;
    static MasterCallback callback;
    
    public testNewRecordWAdd(){}
    
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
        
        poMaster = new ClientMaster(instance, instance.getBranchCode(), true);
        poMaster.setWithUI(false);
        poMaster.setCallback(callback);
        
        trans = new ClientAddress(instance, instance.getBranchCode(), true);
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
                
                trans.setAddress("sAddressx", "Calasiao");
                trans.setAddress("sTownIDxx", "0332");
                trans.setAddress("sBrgyIDxx", "1100144");
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(trans.getMessage());
        }
        
        if (poMaster.NewRecord()){
            try {
                //trans.displayMasFields();
                //assertEquals("Cuison", (String) trans.getMaster("sLastName"));
                
                poMaster.setMaster("sLastName", "Arcilla");
                poMaster.setMaster("sFrstName", "Jahn");
                poMaster.setMaster("sMiddName", "Bron");
                poMaster.setMaster("dBirthDte", SQLUtil.toDate("1997-01-20", SQLUtil.FORMAT_SHORT_DATE));
                
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(poMaster.getMessage());
        }
    }
    
    @Test
    public void test02SaveRecord(){
        if (poMaster.SaveRecord()){
            System.out.println("Record saved successfully.");
        } else {
            fail(poMaster.getMessage());
        }        
    }
}
