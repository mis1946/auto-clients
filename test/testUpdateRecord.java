import java.awt.Desktop;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.clients.base.ClientMaster;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testUpdateRecord {
    static GRider instance = new GRider();
    static ClientMaster trans;
    static MasterCallback callback;
    
    public testUpdateRecord(){}
    
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
        
        trans = new ClientMaster(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @Test
    public void test01OpenRecord(){
        if (trans.OpenRecord("V00123000004")){
            if (trans.UpdateRecord()){
                try {
                    trans.setMaster("sLastName", "Arcilla");
                    trans.setMaster("sFrstName", "Jahn");
                    trans.setMaster("dBirthDte", SQLUtil.toDate("1991-02-28", SQLUtil.FORMAT_SHORT_DATE));
                    
                    trans.searchCitizenship("Phil", false);
                } catch (SQLException e) {
                    fail(e.getMessage());
                }
            } else {
                fail(trans.getMessage());
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
}
