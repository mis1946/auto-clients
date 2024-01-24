/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.clients.base;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.callback.MasterCallback;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;
import org.rmj.auto.json.TabsStateManager;

/**
 *
 * @author Arsiela Date Created: 01-23-2024
 */
public class InsuranceInformation {

    private final String MASTER_TABLE = "insurance_company";
    private final String DEFAULT_DATE = "1900-01-01";
    private final String FILE_PATH = "D://GGC_Java_Systems/config/Autapp_json/" + TabsStateManager.getJsonFileName("Insurance");

    private GRider poGRider;
    private String psBranchCd;
    private boolean pbWithParent;
    private MasterCallback poCallback;

    private int pnEditMode;
    private boolean pbWithUI;
    private String psMessage;
    private String psReport;

    private CachedRowSet poMaster;
    private CachedRowSet poMasterOrig;
    private CachedRowSet poDetail;
    private CachedRowSet poTransactions;

    public InsuranceInformation(GRider foGRider, String fsBranchCd, boolean fbWithParent) {
        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;
    }

    public int getEditMode() {
        return pnEditMode;
    }

    public String getMessage() {
        return psMessage;
    }

    public void setWithUI(boolean fbValue) {
        pbWithUI = fbValue;
    }

    public void setCallback(MasterCallback foValue) {
        poCallback = foValue;
    }

    public int getItemCount() throws SQLException {
        poMaster.last();
        return poMaster.getRow();
    }

    private String toJSONString() {
        JSONParser loParser = new JSONParser();
        JSONArray laMaster = new JSONArray();
        JSONObject loMaster;
        JSONObject loJSON;

        try {
            loJSON = new JSONObject();
            String lsValue = CommonUtils.RS2JSON(poMaster).toJSONString();
            laMaster = (JSONArray) loParser.parse(lsValue);
            loMaster = (JSONObject) laMaster.get(0);
            loJSON.put("master", loMaster);

            // Populate mode with data
            JSONArray modeArray = new JSONArray();
            JSONObject modeJson = new JSONObject();
            modeJson.put("EditMode", String.valueOf(pnEditMode));
            modeJson.put("TransCod", (String) getMaster(1));
            modeArray.add(modeJson);
            loJSON.put("mode", modeArray);

            return loJSON.toJSONString();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(InsuranceInformation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    private void saveState(String fsValue) {
        if (pnEditMode == EditMode.UNKNOWN) {
            return;
        }
        try {
            File Delfile = new File(FILE_PATH);
            if (Delfile.exists() && Delfile.isFile()) {
            } else {
                return;
            }

            // Write the JSON object to file
            try (FileWriter file = new FileWriter(FILE_PATH)) {
                file.write(fsValue);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("JSON file updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean loadState() {
        try {
            String lsTransCd = "";
            String tempValue = "";

            File Delfile = new File(FILE_PATH);
            if (Delfile.exists() && Delfile.isFile()) {
            } else {
                psMessage = "";
                pnEditMode = EditMode.UNKNOWN;
                return false;
            }

            // Parse the JSON file
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(FILE_PATH));
            JSONObject jsonObject = (JSONObject) obj;

            JSONArray modeArray = (JSONArray) jsonObject.get("mode");
            if (modeArray == null) {
                psMessage = "";
                return false;
            }

            // Extract index and value from each object in the "master" array
            for (Object item : modeArray) {
                JSONObject mode = (JSONObject) item;
                lsTransCd = (String) mode.get("TransCod");
                pnEditMode = Integer.valueOf((String) mode.get("EditMode"));
            }

            if (modeArray.size() > 0) {
                switch (pnEditMode) {
                    case EditMode.ADDNEW:
                        if (NewRecord()) {
                        } else {
                            psMessage = "Error while setting state to New Record.";
                            return false;
                        }
                        break;
                    case EditMode.UPDATE:
                        if (OpenRecord(lsTransCd)) {
                            if (UpdateRecord()) {
                            } else {
                                psMessage = "Error while setting state to Update Record.";
                                return false;
                            }
                        } else {
                            psMessage = "Error while setting state to Ready.";
                            return false;
                        }
                        break;
                    case EditMode.READY:
                        if (OpenRecord(lsTransCd)) {
                        } else {
                            psMessage = "Error while setting state to Ready.";
                            return false;
                        }
                        break;
                }

                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                    poMaster.first();
                    JSONObject masterObject = (JSONObject) jsonObject.get("master");
                    // Add a row to the CachedRowSet with the values from the masterObject
                    for (Object key : masterObject.keySet()) {
                        Object value = masterObject.get(key);
                        //System.out.println("MASTER value : " + value + " : key #" + Integer.valueOf(key.toString()) +" : "  + poVehicle.getMetaData().getColumnType(Integer.valueOf(key.toString())));
                        if (value == null) {
                            tempValue = "";
                        } else {
                            tempValue = String.valueOf(value);
                        }
                        switch (poMaster.getMetaData().getColumnType(Integer.valueOf(key.toString()))) {
                            case Types.CHAR:
                            case Types.VARCHAR:
                                poMaster.updateObject(Integer.valueOf(key.toString()), tempValue);
                                //setMaster(Integer.valueOf(key.toString()), tempValue);
                                break;
                            case Types.DATE:
                            case Types.TIMESTAMP:
                                if (String.valueOf(tempValue).isEmpty()) {
                                    tempValue = DEFAULT_DATE;
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poMaster.updateObject(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE));

                                //setMaster(Integer.valueOf(key.toString()), SQLUtil.toDate(tempValue, SQLUtil.FORMAT_SHORT_DATE));
                                break;
                            case Types.INTEGER:
                                if (String.valueOf(tempValue).isEmpty()) {
                                    tempValue = "0";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poMaster.updateObject(Integer.valueOf(key.toString()), Integer.valueOf(tempValue));
                                //setMaster(Integer.valueOf(key.toString()), Integer.valueOf(tempValue));
                                break;
                            case Types.DECIMAL:
                            case Types.DOUBLE:
                                if (String.valueOf(tempValue).isEmpty()) {
                                    tempValue = "0.00";
                                } else {
                                    tempValue = String.valueOf(value);
                                }
                                poMaster.updateObject(Integer.valueOf(key.toString()), Double.valueOf(tempValue));
                                //setMaster(Integer.valueOf(key.toString()), Double.valueOf(tempValue));
                                break;
                            default:
                                //System.out.println("MASTER value : " + tempValue + " negative key #" + Integer.valueOf(key.toString()) +" : "  + poVehicle.getMetaData().getColumnType(Integer.valueOf(key.toString())));
                                poMaster.updateObject(Integer.valueOf(key.toString()), tempValue);
                                //setMaster(Integer.valueOf(key.toString()), tempValue);
                                break;
                        }
                        tempValue = "";
                    }
                    poMaster.updateRow();
                }
            } else {
                psMessage = "";
                return false;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(InsuranceInformation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    public void setMaster(int fnIndex, Object foValue) throws SQLException {
        poMaster.first();

        switch (fnIndex) {
            case 1: //sInsurIDx
            case 2: // sCompnyTp
            case 3: // sInsurNme
            case 4: // sBranchxx
            case 5: // sInsurCde
            case 6: // sContactP
            case 7: // sAddressx
            case 8: // sTownIDxx
            case 9: // sZippCode
            case 10: // sTelNoxxx
            case 11: // sFaxNoxxx
            case 12: // cRecdStat
            case 13: // sEntryByx
            case 15: // sModified
            case 17: // sProvName
            case 18: // sTownProv
            case 19: // sTownName
            case 20: // sProvIDxx
                poMaster.updateObject(fnIndex, (String) foValue);
                poMaster.updateRow();
                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                }
                break;
            case 14: // dEntryDte
            case 16: // dModified
                if (foValue instanceof Date) {
                    poMaster.updateObject(fnIndex, foValue);
                } else {
                    poMaster.updateObject(fnIndex, SQLUtil.toDate(DEFAULT_DATE, SQLUtil.FORMAT_SHORT_DATE));
                }
                poMaster.updateRow();
                if (poCallback != null) {
                    poCallback.onSuccess(fnIndex, getMaster(fnIndex));
                }
                break;
        }
        saveState(toJSONString());
    }

    public void setMaster(String fsIndex, Object foValue) throws SQLException {
        setMaster(MiscUtil.getColumnIndex(poMaster, fsIndex), foValue);
    }

    public Object getMaster(String fsIndex) throws SQLException {
        return getMaster(MiscUtil.getColumnIndex(poMaster, fsIndex));
    }

    public Object getMaster(int fnIndex) throws SQLException {
        poMaster.first();
        return poMaster.getObject(fnIndex);
    }

    public Object getDetail(int fnRow, int fnIndex) throws SQLException {
        if (fnIndex == 0) {
            return null;
        }

        poDetail.absolute(fnRow);
        return poDetail.getObject(fnIndex);
    }

    public Object getDetail(int fnRow, String fsIndex) throws SQLException {
        return getDetail(fnRow, MiscUtil.getColumnIndex(poDetail, fsIndex));
    }

    public int getDetailCount() throws SQLException {
        if (poDetail != null) {
            poDetail.last();
            return poDetail.getRow();
        } else {
            return 0;
        }
    }

    /**
     * Initializes the master data for adding a new entry.
     */
    public boolean NewRecord() {
        if (poGRider == null) {
            psMessage = "Application driver is not set.";
            return false;
        }

        if (psBranchCd.isEmpty()) {
            psBranchCd = poGRider.getBranchCode();
        }

        try {
            String lsSQL = getSQ_Master() + " WHERE 0=1";
            System.out.println(lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);

            RowSetFactory factory = RowSetProvider.newFactory();
            poMaster = factory.createCachedRowSet();
            poMaster.populate(loRS);
            MiscUtil.close(loRS);

            poMaster.last();
            poMaster.moveToInsertRow();

            MiscUtil.initRowSet(poMaster);
            poMaster.updateString("cRecdStat", RecordStatus.ACTIVE);   //0 Cancelled, 1 Active
            poMaster.insertRow();
            poMaster.moveToCurrentRow();

        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        pnEditMode = EditMode.ADDNEW;
        return true;
    }

    /**
     * Searches for a record.
     */
    public boolean SearchRecord() throws SQLException {
        String lsSQL = getSQ_Master();

        JSONObject loJSON = null;
        if (pbWithUI) {
            loJSON = showFXDialog.jsonSearch(poGRider,
                     lsSQL,
                     "%",
                     "Insurance ID»Insurance Name»",
                     "sInsurIDx»sInsurNme",
                     "a.sInsurIDx»b.sInsurNme",
                     0);
            if (loJSON == null) {
                psMessage = "No record found/selected.";
                return false;
            } else {
                if (OpenRecord((String) loJSON.get("sInsurIDx"))) {

                } else {
                    psMessage = "No record found/selected.";
                    return false;
                }
            }
        }
        return true;
    }

    public boolean loadList() {
        try {
            if (poGRider == null) {
                psMessage = "Application driver is not set.";
                return false;
            }

            psMessage = "";

            String lsSQL = getSQ_Master() + " GROUP BY a.sInsurIDx ";
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            poDetail = factory.createCachedRowSet();
            poDetail.populate(loRS);
            MiscUtil.close(loRS);
        } catch (SQLException ex) {
            Logger.getLogger(InsuranceInformation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * Opens a record with the specified value.
     *
     * @param fsValue the value used to open the record
     *
     */
    public boolean OpenRecord(String fsValue) {
        try {
            try {
                String lsSQL = getSQ_Master() + " WHERE a.sInsurIDx = " + SQLUtil.toSQL(fsValue) + " GROUP BY a.sInsurIDx ";
                ResultSet loRS = poGRider.executeQuery(lsSQL);

                System.out.println(lsSQL);
                if (MiscUtil.RecordCount(loRS) <= 0) {
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

            if (((String) getMaster(2)).equals("0")) {
                pnEditMode = EditMode.UNKNOWN;
            } else {
                pnEditMode = EditMode.READY;
            }

        } catch (SQLException ex) {
            Logger.getLogger(InsuranceInformation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * Prepares to update a record in the data. This method creates copies of
     * the original data to be updated and sets the edit mode to UPDATE.
     *
     */
    public boolean UpdateRecord() {
        try {
            if (poMaster != null) {
                poMasterOrig = (CachedRowSet) poMaster.createCopy();
            }
        } catch (SQLException ex) {
            Logger.getLogger(InsuranceInformation.class.getName()).log(Level.SEVERE, null, ex);
        }
        pnEditMode = EditMode.UPDATE;
        return true;
    }

    /**
     * Saves a record to the database. This method is responsible for adding a
     * new record or updating an existing one based on the edit mode. It
     * performs data validation and handles database transactions.
     *
     */
    public boolean SaveRecord() {
        if (!(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            psMessage = "Invalid update mode detected.";
            return false;
        }

        try {
            String lsID = "";
            String lsSQL = "";

            if (!isEntryOK()) {
                return false;
            }

            if (!pbWithParent) {
                poGRider.beginTrans();
            }
            if (pnEditMode == EditMode.ADDNEW) { //add
                /*MASTER*/
                lsID = MiscUtil.getNextCode(MASTER_TABLE, "sInsurIDx", true, poGRider.getConnection(), psBranchCd);
                poMaster.updateString("sInsurIDx", lsID);
                poMaster.updateString("sEntryByx", poGRider.getUserID());
                poMaster.updateObject("dEntryDte", (Date) poGRider.getServerDate());
                poMaster.updateString("sModified", poGRider.getUserID());
                poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                poMaster.updateRow();

                lsSQL = MiscUtil.rowset2SQL(poMaster, MASTER_TABLE, "sProvName»sTownProv»sTownName»sProvIDxx");

                if (lsSQL.isEmpty()) {
                    psMessage = "No record to update in master.";
                    return false;
                }
                if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
                    psMessage = "ADD MASTER: " + poGRider.getErrMsg();
                    if (!pbWithParent) {
                        poGRider.rollbackTrans();
                    }
                    return false;
                }

            } else { //update
                boolean lbisModified = false;
                if (!CompareRows.isRowEqual(poMaster, poMasterOrig, 1)) {
                    lbisModified = true;
                }
                if (lbisModified) {
                    /*MASTER*/
                    poMaster.updateString("sModified", poGRider.getUserID());
                    poMaster.updateObject("dModified", (Date) poGRider.getServerDate());
                    poMaster.updateRow();
                    lsSQL = MiscUtil.rowset2SQL(poMaster,
                            MASTER_TABLE,
                            "sProvName»sTownProv»sTownName»sProvIDxx",
                            "sInsurIDx = " + SQLUtil.toSQL((String) getMaster("sInsurIDx")));
                    if (lsSQL.isEmpty()) {
                        psMessage = "No record to update.";
                        return false;
                    }
                    if (poGRider.executeQuery(lsSQL, MASTER_TABLE, psBranchCd, "") <= 0) {
                        psMessage = "UPDATE MASTER: " + poGRider.getErrMsg();
                        if (!pbWithParent) {
                            poGRider.rollbackTrans();
                        }
                        return false;
                    }
                }

                if (poMaster != null) {
                    poMasterOrig = (CachedRowSet) poMaster.createCopy();
                }
            }
            if (!pbWithParent) {
                poGRider.commitTrans();
            }
        } catch (SQLException e) {
            psMessage = e.getMessage();
            return false;
        }

        pnEditMode = EditMode.UNKNOWN;

        return true;
    }

    /**
     * Validate data before saving.
     *
     */
    private boolean isEntryOK() throws SQLException {
        poMaster.first();

        if (poMaster.getString("sInsurNme").isEmpty()) {
            psMessage = "Insurance Company cannot be empty.";
            return false;
        }
        
        if (poMaster.getString("sCompnyTp").isEmpty()) {
            psMessage = "Insurance Company Type cannot be empty.";
            return false;
        }

        if (poMaster.getString("sBranchxx").isEmpty()) {
            psMessage = "Insurance Branch cannot be empty.";
            return false;
        }

        if (poMaster.getString("sAddressx").isEmpty()) {
            psMessage = "Address cannot be empty.";
            return false;
        }

        if (poMaster.getString("sTownIDxx").isEmpty()) {
            psMessage = "Town cannot be empty.";
            return false;
        }

        if (poMaster.getString("sZippCode").isEmpty()) {
            psMessage = "Zippcode cannot be empty.";
            return false;
        }
        
        if (poMaster.getString("sContactP").isEmpty()) {
            psMessage = "Contact Person cannot be empty.";
            return false;
        }
        
        if (poMaster.getString("sTelNoxxx").isEmpty()) {
            psMessage = "Telephone Number cannot be empty.";
            return false;
        }
        
        String lsSQL = getSQ_Master();
        ResultSet loRS;
        lsSQL = lsSQL + " WHERE a.sInsurNme = " + SQLUtil.toSQL(poMaster.getString("sInsurNme"))
                + " AND a.sBranchxx = " + SQLUtil.toSQL(poMaster.getString("sBranchxx"))
                + " AND a.sZippCode = " + SQLUtil.toSQL(poMaster.getString("sZippCode"))
                + " AND a.sInsurIDx <> " + SQLUtil.toSQL(poMaster.getString("sInsurIDx"))
                + " GROUP BY a.sInsurIDx ";
        loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0) {
            psMessage = "Insurance Company already exist.";
            MiscUtil.close(loRS);
            return false;
        }

        return true;
    }

    private String getSQ_Master() {
        return " SELECT "
                + "  IFNULL(a.sInsurIDx,'') AS  sInsurIDx  " //1
                + " , IFNULL(a.sCompnyTp,'') AS  sCompnyTp " //2
                + " , IFNULL(a.sInsurNme,'') AS  sInsurNme " //3
                + " , IFNULL(a.sBranchxx,'') AS  sBranchxx " //4
                + " , IFNULL(a.sInsurCde,'') AS  sInsurCde " //5
                + " , IFNULL(a.sContactP,'') AS  sContactP " //6
                + " , IFNULL(a.sAddressx,'') AS  sAddressx " //7
                + " , IFNULL(a.sTownIDxx,'') AS  sTownIDxx " //8
                + " , IFNULL(a.sZippCode,'') AS  sZippCode " //9
                + " , IFNULL(a.sTelNoxxx,'') AS  sTelNoxxx " //10
                + " , IFNULL(a.sFaxNoxxx,'') AS  sFaxNoxxx " //11
                + " , IFNULL(a.cRecdStat,'') AS  cRecdStat " //12
                + " , IFNULL(a.sEntryByx,'') AS  sEntryByx " //13
                + " , a.dEntryDte                          " //14
                + " , IFNULL(a.sModified,'') AS  sModified " //15
                + " , a.dModified                          " //16
                + " , IFNULL(UPPER(c.sProvName), '') sProvName		 " //17
                + " , IFNULL(UPPER(TRIM(CONCAT(b.sTownName, ', ', c.sProvName))) , '') sTownProv " //18
                + " , IFNULL(UPPER(b.sTownName), '') sTownName		 " //19
                + " , IFNULL(UPPER(b.sProvIDxx), '') sProvIDxx		 " //20
                /*dTimeStmp*/
                + "FROM insurance_company  a              "
                + " LEFT JOIN TownCity b ON b.sTownIDxx = a.sTownIDxx "
                + " LEFT JOIN Province c ON c.sProvIDxx = b.sProvIDxx ";

    }

    //query for town
    private String getSQ_Town() {
        return "SELECT "
                + "  IFNULL(sTownIDxx, '') sTownIDxx "
                + ", IFNULL(sTownName, '') sTownName "
                + ", IFNULL(sZippCode, '') sZippCode "
                + " FROM TownCity ";
    }

    //search town (used when "Town" is double clicked or searched)
    /**
     * Searches for a town within a specified province based on criteria and
     * retrieves town details.
     *
     * This method is used to search for a town within a specified province,
     * either by town code or town name criteria. It allows both UI and non-UI
     * search modes and retrieves town details if found.
     *
     * @param fsValue The search criteria, which can be a town code or town
     * name.
     * @param fbByCode Set to true if searching by town code, false if searching
     * by town name.
     * @return True if a town within the specified province is successfully
     * found, and its details are retrieved, otherwise false.
     * @throws SQLException if a database error occurs.
     */
    public boolean searchTown(String fsValue, boolean fbByCode) throws SQLException {
        String lsSQL = getSQ_Town();
        String lsProvIDxx = poMaster.getString("sProvIDxx");

        if (lsProvIDxx.isEmpty()) {
            psMessage = "Please Enter Province First.";
            return false;
        }

        if (fbByCode) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownIDxx = " + SQLUtil.toSQL(fsValue)
                    + "AND sProvIDxx = " + SQLUtil.toSQL(lsProvIDxx));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sTownName LIKE " + SQLUtil.toSQL(fsValue + "%")
                    + "AND sProvIDxx = " + SQLUtil.toSQL(lsProvIDxx));
        }

        ResultSet loRS;
        if (!pbWithUI) {
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);

            if (loRS.next()) {
                setMaster("sTownIDxx", loRS.getString("sTownIDxx"));
                setMaster("sTownName", loRS.getString("sTownName"));
                setMaster("sZippCode", loRS.getString("sZippCode"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);

            JSONObject loJSON = showFXDialog.jsonSearch(poGRider,
                    lsSQL,
                    fsValue,
                    "Code»Town",
                    "sTownIDxx»sTownName",
                    "sTownIDxx»sTownName",
                    fbByCode ? 0 : 1);

            if (loJSON == null) {
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sTownIDxx", (String) loJSON.get("sTownIDxx"));
                setMaster("sTownName", (String) loJSON.get("sTownName"));
                setMaster("sZippCode", (String) loJSON.get("sZippCode"));
            }
        }

        return true;
    }

    //query for Province
    private String getSQ_Province() {
        return "SELECT "
                + " sProvName "
                + ", sProvIDxx "
                + " FROM Province  ";
    }

    //search Province (used when "Province" is double clicked or searched)
    /**
     * Searches for a province based on criteria and retrieves its details.
     *
     * This method is used to search for a province, either by province code or
     * province name criteria. It allows both UI and non-UI search modes and
     * retrieves the province's details if found.
     *
     * @param fsValue The search criteria, which can be a province code or
     * province name.
     * @param fbByCode Set to true if searching by province code, false if
     * searching by province name.
     * @return True if a province is successfully found, and its details are
     * retrieved, otherwise false.
     * @throws SQLException if a database error occurs.
     */
    public boolean searchProvince(String fsValue, boolean fbByCode) throws SQLException {
        String lsSQL = getSQ_Province();

        if (fbByCode) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sProvIDxx = " + SQLUtil.toSQL(fsValue));
        } else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sProvName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        }

        ResultSet loRS;
        if (!pbWithUI) {
            lsSQL += " LIMIT 1";
            loRS = poGRider.executeQuery(lsSQL);

            if (loRS.next()) {
                setMaster("sProvName", loRS.getString("sProvName"));
                setMaster("sProvIDxx", loRS.getString("sProvIDxx"));
            } else {
                psMessage = "No record found.";
                return false;
            }
        } else {
            loRS = poGRider.executeQuery(lsSQL);

            JSONObject loJSON = showFXDialog.jsonSearch(
                    poGRider,
                    lsSQL,
                    fsValue,
                    "Code»Province",
                    "sProvIDxx»sProvName",
                    "sProvIDxx»sProvName",
                    fbByCode ? 0 : 1);
            if (loJSON == null) {
                psMessage = "No record found/selected.";
                return false;
            } else {
                setMaster("sProvName", (String) loJSON.get("sProvName"));
                setMaster("sProvIDxx", (String) loJSON.get("sProvIDxx"));
            }
        }

        return true;
    }

    public void displayMasFields() throws SQLException {
        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE) {
            return;
        }

        int lnRow = poMaster.getMetaData().getColumnCount();

        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");

        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++) {
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + poMaster.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + poMaster.getMetaData().getColumnType(lnCtr));
            if (poMaster.getMetaData().getColumnType(lnCtr) == Types.CHAR
                    || poMaster.getMetaData().getColumnType(lnCtr) == Types.VARCHAR) {

                System.out.println("Column index: " + (lnCtr) + " --> Size: " + poMaster.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }

        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }

}
