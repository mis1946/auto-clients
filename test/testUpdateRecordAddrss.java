
import java.sql.SQLException;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.auto.clients.base.ClientAddress;


public class testUpdateRecordAddrss {
    static GRider instance = new GRider();
    static ClientAddress trans;
    static MasterCallback callback;
    
    public testUpdateRecordAddrss(){}
    
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
        
        trans = new ClientAddress(instance, instance.getBranchCode(), true);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @Test
    public void test01OpenRecord() throws SQLException{
        if (trans.OpenRecord("V00123000002",false)){
            //getEditMode.EditMode.UPDATE();
            //trans.displayMasFields();
            if (trans.UpdateRecord()){
                try {
                    trans.setAddress("sAddressx", "San Carlos City");
                    trans.setAddress("sTownIDxx", "0334");
                    trans.setAddress("sBrgyIDxx", "1100041");
//                    trans.setAddress("sFrstName", "Jahn");
//                    trans.setAddress("dBirthDte", SQLUtil.toDate("1991-02-28", SQLUtil.FORMAT_SHORT_DATE));
                    
//                    trans.searchCitizenship("Phil", false);
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
