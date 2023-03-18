package org.rmj.auto.clients.base;

import com.mysql.jdbc.SQLError;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;

public class ClientMaster{
    private final String MASTER_TABLE = "Client_Master";
    private final String DEFAULT_DATE = "1900-01-01";
    
    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;
    
    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    
    private CachedRowSet poMaster;
    
    private ClientAddress poAddress;
    private ClientSocMed poSocMed;
    private ClientEMail poEmail;
    private ClientMobile poMobile;
        
    public ClientMaster(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
        
        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;
        
        poAddress = new ClientAddress(poGRider, psBranchCd, false);
        poSocMed = new ClientSocMed(poGRider, psBranchCd, true);
        poEmail = new ClientEMail(poGRider, psBranchCd, true);
        poMobile = new ClientMobile(poGRider, psBranchCd, true); 
        poMobile.setWithUI(false);
        poAddress.setWithUI(false);
        poEmail.setWithUI(false);
        poSocMed.setWithUI(false);
        
    }
    
    public int getEditMode(){
        return pnEditMode;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public void setCallback(MasterCallback foValue){
        poCallback = foValue;
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        poMaster.first();
        
        switch (fnIndex){            
            case 2://sLastName
            case 3://sFrstName
            case 4://sMiddName
            case 5://sMaidenNm
            case 6://sSuffixNm
            case 7://sTitlexxx
            case 8://cGenderCd
            case 9://cCvilStat
            case 10://sCitizenx
            case 12://sBirthPlc
            case 13://sTaxIDNox
            case 14://sLTOIDxxx
            case 15://sAddlInfo
            case 16://sCompnyNm
            case 17://sClientNo
            case 18://cClientTp
            case 19://cRecdStat
            case 24://sCntryNme
            case 25://sTownName
            case 27://sSpouseID
            case 28://sSpouseNm
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
            case 11: //dBirthDte
                if (foValue instanceof Date){
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                
                if (poCallback != null) poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                break;
        }                
    }
    
    public static String getFullName(String fsLname, String fsFrstNm, String fsSuffix, String fsMiddnm) {
        String fsFullName = fsLname;
        if (fsFrstNm != null && !fsFrstNm.isEmpty()) {
            fsFullName += ", " + fsFrstNm;
        }
        if (fsSuffix != null && !fsSuffix.isEmpty()) {
            fsFullName += " " + fsSuffix;
        }
        if (fsMiddnm != null && !fsMiddnm.isEmpty()) {
            fsFullName += " " + fsMiddnm;
        }
        return fsFullName;
    }
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(MiscUtil.getColumnIndex(poMaster, fsIndex), foValue);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(MiscUtil.getColumnIndex(poMaster, fsIndex));
    }
    
    public Object getMaster(int fnIndex) throws SQLException{
        poMaster.first();
        return poMaster.getObject(fnIndex);
    }
    
    public boolean NewRecord(){
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        if (psBranchCd.isEmpty()) psBranchCd = poGRider.getBranchCode();
        
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
            
            poMaster.last();
            poMaster.moveToInsertRow();
            
            MiscUtil.initRowSet(poMaster);       
            poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);
            poMaster.updateString("sTitlexxx", "0");
            poMaster.updateString("cGenderCd", "0");
            poMaster.updateString("cCvilStat", "0");
            poMaster.updateString("cClientTp", "0");
            poMaster.updateObject("dBirthDte", poGRider.getServerDate());
            
            poMaster.insertRow();
            poMaster.moveToCurrentRow();
            
            //mobile new record
            //poMobile.NewRecord();
            //address new record
            //poAddress.NewRecord();
            //email new record
            //poEmail.NewRecord();
            //social media new record
            //poSocMed.NewRecord();
            
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.ADDNEW;
        return true;
    }
    
    public boolean SearchRecord(String fsValue, boolean fbByCode) throws SQLException{
        if (poGRider == null){
            psMessage = "Application driver is not set.";
            return false;
        }
        
        psMessage = "";    
        
        String lsSQL = getSQ_Master();
        
        if (pbWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                poGRider, 
                                lsSQL, 
                                fsValue, 
                                "Client ID»Customer Name»", 
                                "sClientID»sCompnyNm", 
                                "a.sClientID»a.sCompnyNm", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenRecord((String) loJSON.get("sClientID"),true);
            else {
                psMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sClientID = " + SQLUtil.toSQL(fsValue));   
        else {
            if (!fsValue.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%")); 
                //lsSQL += " LIMIT 1";
            }
        }
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            psMessage = "No record found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sClientID");
        MiscUtil.close(loRS);
        
        //return OpenRecord(lsSQL, true);
        return OpenRecord(lsSQL, true);
    }
    
    public boolean OpenRecord(String fsValue,boolean fbByCode){        
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sClientID = " + SQLUtil.toSQL(fsValue));
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                psMessage = "No record found.";
                MiscUtil.close(loRS);        
                return false;
            }
            
            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.READY;
        return true;
    }
    
    public boolean UpdateRecord(){
        
        pnEditMode = EditMode.UPDATE;
        return true;
    }
    
    public boolean SaveRecord(){
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)){
            psMessage = "Invalid update mode detected.";
            return false;
        }
        
        try {
            if (!isEntryOK()) return false;
            
            String lsSQL = "";
            
            if (pnEditMode == EditMode.ADDNEW){ //add
                poMaster.updateString("sClientID", MiscUtil.getNextCode(MASTER_TABLE, "sClientID", true, poGRider.getConnection(), psBranchCd));
                poMaster.updateString("sCompnyNm", getFullName( (String)getMaster("sLastName"), 
                                                                (String)getMaster("sFrstName"), 
                                                                (String)getMaster("sSuffixNm"),
                                                                (String)getMaster("sMiddName")));// concat for sCompnyNm                                              
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sCntryNme»sTownName»sCustName»sSpouseNm");
            } else { //update
                poMaster.updateString("sCompnyNm", getFullName( (String)getMaster("sLastName"), 
                                                                (String)getMaster("sFrstName"), 
                                                                (String)getMaster("sSuffixNm"),
                                                                (String)getMaster("sMiddName")));// concat for sCompnyNm
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();
                
                lsSQL = MiscUtil.rowset2SQL(poMaster, 
                                            MASTER_TABLE, 
                                            "sCntryNme»sTownName»sCustName»sSpouseNm", 
                                            "sClientID = " + SQLUtil.toSQL((String) getMaster("sClientID")));
            }
            
            if (lsSQL.isEmpty()){
                psMessage = "No record to update.";
                return false;
            }
            
            if (!pbWithParent) poGRider.beginTrans();
           
            if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0){
                psMessage = poGRider.getErrMsg();
                if (!pbWithParent) poGRider.rollbackTrans();
                return false;
            }
            
            //save mobile
//            poMobile.setClientID((String) getMaster("sClientID"));
//            if (!poMobile.SaveRecord()) {
//                psMessage = poGRider.getErrMsg();
//                return false;
//            }
////            //save email
//            poEmail.setClientID((String) getMaster("sClientID"));
//            if (!poEmail.SaveRecord()){
//                psMessage = poGRider.getErrMsg();
//                return false;
//            }
            //save address
//             poAddress.setClientID((String) getMaster("sClientID"));
//            if (!poAddress.SaveRecord()){
//                psMessage = poGRider.getErrMsg();
//                return false;
//            }
//            //save social media
//            poSocMed.setClientID((String) getMaster("sClientID"));
//            if (!poSocMed.SaveRecord()){
//                psMessage = poGRider.getErrMsg();
//                return false;
//            }
            
            if (!pbWithParent) poGRider.commitTrans();
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean ActivateRecord(){
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean DeactivateRecord(){
        
        pnEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    private String getSQ_Citizen(){
        return "SELECT" +
                    "  sCntryCde" +
                    ", sCntryNme" +
                " FROM Country";
    }
    
    private String getSQ_Birthplace(){
        return "SELECT" +
                    "  sTownIDxx" +
                    ", sTownName" +
                " FROM towncity";
    }
    
    private String getSQ_Spouse(){
        return "SELECT" +
                    " sClientID" +
                    ", sLastName" +
                    ", sFrstName" +
                    ", TRIM(CONCAT(sLastName, ', ', sFrstName)) sSpouseNm"+
                " FROM client_master";
    }
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sClientID" + //1
                    ", a.sLastName" + //2
                    ", a.sFrstName" + //3
                    ", a.sMiddName" + //4
                    ", a.sMaidenNm" + //5
                    ", a.sSuffixNm" + //6
                    ", a.sTitlexxx" + //7
                    ", a.cGenderCd" + //8
                    ", a.cCvilStat" + //9
                    ", a.sCitizenx" + //10
                    ", a.dBirthDte" + //11
                    ", a.sBirthPlc" + //12
                    ", a.sTaxIDNox" + //13
                    ", a.sLTOIDxxx" + //14
                    ", a.sAddlInfo" + //15
                    ", a.sCompnyNm" + //16
                    ", a.sClientNo" + //17
                    ", a.cClientTp" + //18
                    ", a.cRecdStat" + //19
                    ", a.sEntryByx" + //20
                    ", a.dEntryDte" + //21
                    ", a.sModified" + //22
                    ", a.dModified" + //23
                    ", IFNULL(b.sCntryNme, '') sCntryNme" +  //24
                    ", TRIM(CONCAT(c.sTownName, ', ', d.sProvName, ' ', c.sZippCode)) sTownName" +  //25
                    ", TRIM(CONCAT(a.sLastName, ', ', a.sFrstName, ' ', a.sSuffixNm, ' ', a.sMiddName)) sCustName" +  //26
                    ", a.sSpouseID" +  //27
                    ", TRIM(CONCAT(e.sLastName, ', ', e.sFrstName)) sSpouseNm"+  //28
                " FROM " + MASTER_TABLE + " a" +
                    " LEFT JOIN Country b ON a.sCitizenx = b.sCntryCde" +
                    " LEFT JOIN TownCity c ON a.sBirthPlc = c.sTownIDxx" +
                    " LEFT JOIN Province d ON c.sProvIDxx = d.sProvIDxx" +
                    " LEFT JOIN Client_Master e ON e.sClientID = a.sSpouseID";
    }
    
    public void displayMasFields() throws SQLException{
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) return;
        
        int lnRow = poMaster.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poMaster.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poMaster.getMetaData().getColumnType(lnCtr));
            if (poMaster.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                poMaster.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poMaster.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    } 
    
    private boolean isEntryOK() throws SQLException{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Format the dBirthDte value into the desired format
        String formattedDate = sdf.format(poMaster.getDate("dBirthDte"));
        poMaster.first();
        
        //validate first name and last name if client type is customer
        if(poMaster.getString("cClientTp").equals(0)){
            if (poMaster.getString("sLastName").isEmpty()){
                psMessage = "Customer last name is not set.";
                return false;
            }

            if (poMaster.getString("sFrstName").isEmpty()){
                psMessage = "Customer first name is not set.";
                return false;
            }
            
            if (poMaster.getString("cGenderCd").isEmpty()){
                psMessage = "Gender is not set.";
                return false;
            }
            
            if (poMaster.getString("cCvilStat").isEmpty()){
                psMessage = "Civil Status is not set.";
                return false;
            }
            
            String lsSQL = getSQ_Master();
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sFrstName = " + SQLUtil.toSQL(poMaster.getString("sFrstName")) +
                                                    " AND a.sLastName = " + SQLUtil.toSQL(poMaster.getString("sLastName")) + 
                                                    " AND a.sBirthPlc = " + SQLUtil.toSQL(poMaster.getString("sBirthPlc"))); 
                                                    //" AND a.dBirthDte = " + SQLUtil.toSQL(formattedDate));

            ResultSet loRS = poGRider.executeQuery(lsSQL);

            if (MiscUtil.RecordCount(loRS) > 0){
                psMessage = "Existing Customer Record.";
                MiscUtil.close(loRS);        
                return false;
            }
        }
        
        //validate max size of string variables
        
        return true;
    }
    
    public boolean searchCitizenship(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Citizen();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sCntryCde = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sCntryNme LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sCitizenx", loRS.getString("sCntryCde"));
                setMaster("sCntryNme", loRS.getString("sCntryNme"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Country", "sCntryCde»sCntryNme");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sCitizenx", (String) loJSON.get("sCntryCde"));
                setMaster("sCntryNme", (String) loJSON.get("sCntryNme"));
            }
        }
        
        return true;
    }
    
    public boolean searchBirthplace(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Birthplace();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownIDxx = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sBirthPlc", loRS.getString("sTownIDxx"));
                setMaster("sTownName", loRS.getString("sTownName"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Town", "sTownIDxx»sTownName");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sBirthPlc", (String) loJSON.get("sTownIDxx"));
                setMaster("sTownName", (String) loJSON.get("sTownName"));
            }
        }
        
        return true;
    }
    
    
    public boolean searchSpouse(String fsValue, boolean fbByCode) throws SQLException{
        String lsSQL = getSQ_Spouse();
        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "sClientID = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }
        
        ResultSet loRS;
        if (!pbWithUI) {   
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);
            
            if (loRS.next()){
                setMaster("sSpouseID", loRS.getString("sClientID"));
                setMaster("sSpouseNm", loRS.getString("sSpouseNm"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);
            
            JSONObject loJSON = showFXDialog.jsonBrowse(poGRider, loRS, "Code»Customer Name", "sClientID»sSpouseNm");
            
            if (loJSON == null){
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sSpouseID", (String) loJSON.get("sClientID"));
                setMaster("sSpouseNm", (String) loJSON.get("sSpouseNm"));
            }
        }
        
        return true;
    }
}
