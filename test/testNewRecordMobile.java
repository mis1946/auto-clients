
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
import org.rmj.auto.clients.base.ClientMaster;
import org.rmj.auto.clients.base.ClientMobile;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testNewRecordMobile {
    static GRider instance = new GRider();
    static ClientMobile trans;
    static MasterCallback callback;
    
    public testNewRecordMobile(){}
    
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
        
        trans = new ClientMobile(instance, instance.getBranchCode(), false);
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
                
                trans.setMobile(1,"sMobileNo", "09060051572");  
                trans.setMobile(1,"cMobileTp", 1);
                trans.setMobile(1,"cOwnerxxx", 1);
                trans.setMobile(1,"cPrimaryx", 1);
              //  trans.setMobile("cSubscrbr", "3");
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

        try {
            trans.displayMasFields();
            //assertEquals("Cuison", (String) trans.getMaster("sLastName"));
            trans.addMobile();
            int lnCtr = trans.getItemCount();
            trans.setMobile(lnCtr,"sMobileNo", "09700051572");  
            trans.setMobile(lnCtr,"cMobileTp", 1);
            trans.setMobile(lnCtr,"cOwnerxxx", 1);
            trans.setMobile(lnCtr,"cPrimaryx", 1);
          //  trans.setMobile("cSubscrbr", "3");     
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void test03RemoveAddress(){
        try {
            //int lnRow = trans.getItemCount();
            if (trans.removeMobile(2)){
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
