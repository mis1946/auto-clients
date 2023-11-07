import java.sql.SQLException;
import java.util.Date;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import org.rmj.auto.clients.base.LMasDetTrans;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class textNewRecordAll {
    static GRider instance = new GRider();
    static ClientMaster trans;
    static ClientAddress transadd;
    static MasterCallback callback;
    static LMasDetTrans listener;
    
    public textNewRecordAll(){}
    
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
        
        trans = new ClientMaster(instance, instance.getBranchCode(), true);
        trans.setWithUI(false);
        trans.setCallback(callback);
        
        transadd = new ClientAddress(instance, instance.getBranchCode(),true);
        transadd.setWithUI(false);
        assertNotNull(transadd);
        transadd.setCallback(callback);
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
//                int lnCtr = transadd.getItemCount();
//                System.out.println(transadd.getItemCount());               
                trans.setMaster("sLastName", "Arcilla");
                trans.setMaster("sFrstName", "Johanne");
                trans.setMaster("sMiddName", "Igarta");
                //trans.setMaster("sMiddName", "Igarta");
                //trans.setMaster("dBirthDte",SQLUtil.toDate("1997-01-20",SQLUtil.FORMAT_SHORT_DATE));
                trans.searchCitizenship("Phil", false);
                trans.searchBirthplace("calasiao", false);
                
               // trans.searchSpouse("arcil", false);
//                transadd.NewRecord();
//                transadd.addAddress();
                String sTownIDxx = "0332";
                String sAddressx = "Magno Subdivision";
                String sBrgyIDxx = "1100144";
                String sAddrssID = "0001" ;   
                String sClientID = "0001";
                String sHouseNox = "123";
                int cPrimaryx = 1;
                
                //transadd.NewRecord();
//                transadd.addAddress();
//                System.out.println(transadd.getItemCount());   
                int lnCtr = transadd.getItemCount();
                transadd.displayMasFields();
                transadd.setAddressTable(1, "sHouseNox", sHouseNox);
                transadd.setAddressTable(1, "sAddrssID", sAddrssID);
                transadd.setAddressTable(1, "sAddressx", sAddressx);
                transadd.searchTown(1,"Calasi", false);
                transadd.searchBarangay(1,"Bued", false);
                transadd.setAddressTable(1, "sTownIDxx", sTownIDxx);
                transadd.setAddressTable(1, "sBrgyIDxx", sBrgyIDxx);
                transadd.setAddressTable(1, "sClientID", sClientID);
                transadd.setAddressTable(1, "cPrimaryx", cPrimaryx);
                System.out.println(transadd.getAddress(1,"sHouseNox"));
                System.out.println(transadd.getAddress(1,"sAddressx"));
                System.out.println(transadd.getAddress(1,"sTownIDxx"));
                System.out.println(transadd.getAddress(1,"sBrgyIDxx"));
                System.out.println(transadd.getAddress(1,"sZippCode"));
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        } else {
            fail(trans.getMessage());
        }
    }
    
    @Test
    public void test02SaveRecord(){        
        if (trans.SaveRecord()){          
           // transadd.SaveRecord();
            System.out.println("Record saved successfully.");
        } else {
//            fail(transadd.getMessage());
            fail(trans.getMessage());
        }
    }
}

