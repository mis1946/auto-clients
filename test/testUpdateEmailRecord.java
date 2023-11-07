
import java.sql.SQLException;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.auto.clients.base.ClientEMail;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
public class testUpdateEmailRecord {
    static GRider instance = new GRider();
    static ClientEMail trans;
    static MasterCallback callback;
    
    public testUpdateEmailRecord(){}
    
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
        
        trans = new ClientEMail(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
        trans.setCallback(callback);
    }
    
    @Test
    public void test01OpenRecord(){
        if (trans.OpenRecord("V00123000001",true)){
            if (trans.UpdateRecord()){
                try {
                    trans.displayMasFields();
                    trans.setEmail("sEmailAdd", "jahnarcilla123@gmail.com");
                    trans.setEmail("cOwnerxxx", "2");
//                    trans.setMaster("sLastName", "Arcilla");
//                    trans.setMaster("sFrstName", "Jahn");
//                    trans.setMaster("dBirthDte", SQLUtil.toDate("1991-02-28", SQLUtil.FORMAT_SHORT_DATE));
//                    
//                    trans.searchCitizenship("Phil", false);
//                    trans.searchBirthplace("Dagup", false);
                    //trans.searchSpouse("Arcilla", false);
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
