
import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.clients.base.ClientSocMed;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class testUpdateRecordSocMed {
    static GRider instance = new GRider();
    static ClientSocMed trans;
    static MasterCallback callback;
    
    public testUpdateRecordSocMed(){}
    
    @BeforeClass
    public static void setUpClass(){
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
        
        trans = new ClientSocMed(instance,instance.getBranchCode(),false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @AfterClass
    public static void tearDownClass(){
        
    }
    
    public void test01OpenRecord(){
        if (trans.OpenRecord("V00123000001",true)){
            if (trans.UpdateRecord()){
                try {
//                    trans.displayMasFields();
//                    trans.setMaster("sLastName", "Arcilla");
//                    trans.setMaster("sFrstName", "Jahn");
//                    trans.setMaster("dBirthDte", SQLUtil.toDate("1991-02-28", SQLUtil.FORMAT_SHORT_DATE));
//                    
//                    trans.searchCitizenship("Phil", false);
//                    trans.searchBirthplace("Dagup", false);
                        trans.setSocMed("sAccountx", "Jahn Arcilla");
                        trans.setSocMed("cSocialTp", "Facebook");
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
    
}
