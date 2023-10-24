
import java.sql.SQLException;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.clients.base.VehicleDescription;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testNewVehicle {
    static GRider instance = new GRider();
    static VehicleDescription trans;
    static MasterCallback callback;
    
    public testNewVehicle(){};
    
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
        
        trans = new VehicleDescription(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @Test
    public void test01NewVehicle() throws SQLException{
        //trans.displayMasFields();
        if (trans.NewRecord()){
            trans.displayMasFields();
            
            trans.setMaster("sTransMsn", "MT");
            trans.setMaster("nYearModl", 2023);
            trans.setMaster("cVhclSize", 1);
            trans.searchVehicleMake("HONDA");
            trans.searchVehicleModel("CIVIC");            
            trans.searchVehicleColor("Blue");
            trans.searchVehicleType("1.5");
            System.out.println(trans.getMaster("sMakeDesc"));
            
        }
    }
    
    @Test
    public void test02SaveVehicle(){
        if (trans.SaveRecord()){
            System.out.println("Record saved successfully.");
        } else {
            fail(trans.getMessage());
        }
    }
}
