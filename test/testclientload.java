
import java.sql.SQLException;
import java.util.Date;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.clients.base.ClientAddress1;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testclientload {
    static GRider instance = new GRider();
    static ClientAddress1 trans;
    static MasterCallback callback;
    
    public testclientload(){}
    
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
        trans = new ClientAddress1(instance, instance.getBranchCode(), true);
        
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @Test
    public void test02LoadClient() throws SQLException {
        try {
            if (trans.OpenRecord("V00123000004",true)){
                System.out.println("Address count -->" + trans.getItemCount());
                for (int lnCtr = 1; lnCtr <= trans.getItemCount(); lnCtr++){
                    System.out.println("Address. --> " + (String) trans.getAddress(lnCtr, 4));
                    System.out.println("Town ID --> " + (String) trans.getAddress(lnCtr, 5));
                    System.out.println("Barangay --> " + (String) trans.getAddress(lnCtr, 6));
                }
            } else
                fail(trans.getMessage());
        } catch (SQLException e) {
            fail(e.getMessage());
        }   
    }
    
}
