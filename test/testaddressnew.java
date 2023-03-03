//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.FixMethodOrder;
//import org.junit.runners.MethodSorters;
//import org.rmj.appdriver.GRider;
//import org.rmj.appdriver.callback.MasterCallback;
//import org.rmj.auto.clients.base.ClientAddress1;
//
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
///**
// *
// * @author User
// */
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class testaddressnew {
//    static GRider instance = new GRider();
//    static ClientAddress1 trans;
//    static MasterCallback callback;
//    
//    public testaddressnew(){}
//    
//    @BeforeClass
//    public static void setUpClass() {   
//        String path;
//        if(System.getProperty("os.name").toLowerCase().contains("win")){
//            path = "D:/GGC_Java_Systems";
//        }
//        else{
//            path = "/srv/GGC_Java_Systems";
//        }
//        System.setProperty("sys.default.path.config", path);
//        
//        if (!instance.logUser("AutoApp", "M001111122")){
//            System.err.println(instance.getMessage() + instance.getErrMsg());
//            System.exit(1);
//        }
//        
//        callback = new MasterCallback() {
//            @Override
//            public void onSuccess(int fnIndex, Object foValue) {
//                System.out.println(fnIndex + "-->>" + foValue);
//            }
//        };
//        trans = new ClientAddress1(instance, instance.getBranchCode(), true);
//        
//        trans.setWithUI(false);
//        trans.setCallback(callback);
//    }
//    
//    @AfterClass
//    public static void tearDownClass() {
//    }
//}
