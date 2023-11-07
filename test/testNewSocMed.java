
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
import org.rmj.auto.clients.base.ClientSocMed;



@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testNewSocMed {
    static GRider instance = new GRider();
    static ClientSocMed trans;
    static MasterCallback callback;
    
    public testNewSocMed(){}
    
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
        
        trans = new ClientSocMed(instance, instance.getBranchCode(), false);
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
                //assertEquals("Cuison", (String) trans.getMaster("sLastName"));
                
                trans.setSocMed(1,"sAccountx", "jarcilla");
                trans.setSocMed(1,"cSocialTp", 3);               
               // trans.searchSpouse("arcil", false);
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(trans.getMessage());
        }
    }
    
    @Test
    public void test02NewRecord(){
//        if (trans.NewRecord()){
            try {
                trans.displayMasFields();
                //assertEquals("Cuison", (String) trans.getMaster("sLastName"));
                trans.addSocMed();
                int lnCtr = trans.getItemCount();
                trans.setSocMed(lnCtr,"sAccountx", "Jahn Arcilla");
                trans.setSocMed(lnCtr,"cSocialTp", 1);                
               
               // trans.searchSpouse("arcil", false);
            } catch (SQLException e) {
                fail(e.getMessage());
            }
//        } else {
//            fail(trans.getMessage());
//        }
    }
    
    @Test
    public void test03RemoveAddress(){
        try {
            //int lnRow = trans.getItemCount();
            if (trans.removeSocMed(2)){
                //assertEquals(2, trans.getItemCount());
            } else{
                fail(trans.getMessage());
            }
            
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void test04SaveRecord(){
        if (trans.SaveRecord()){
            System.out.println("Record saved successfully.");
        } else {
            fail(trans.getMessage());
        }
    }
           
}
