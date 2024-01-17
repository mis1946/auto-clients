/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.json;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author Arsiela
 * Date Created : 01-06-2024
 */
public class TabsStateManager {
    private static final String FILE_PATH = "D://GGC_Java_Systems/config/Autapp_json/tabs_state.json";

    /**
     * Save Currently opened tabs based on sFormName list. Otherwise delete json file
     * @param sFormName list of currently opened tabs
     */
    public static void saveCurrentTab( List<String> sFormName) { //List<String> tabIds,
        JSONArray jsonArray = new JSONArray();
        JSONObject json = new JSONObject();
        
        if(sFormName.isEmpty()) {
            if(!deleteJsonFile(FILE_PATH)){
                json.put("form", new JSONArray());
            }else{
                return;
            }
        } else {
            for (String FormName : sFormName) {
                if(!getJsonFileName(FormName).isEmpty()){
                    String sFilePath = "D://GGC_Java_Systems/config/Autapp_json/" + getJsonFileName(FormName) ;
                    File Delfile = new File(sFilePath);
                    if (Delfile.exists() && Delfile.isFile()) {
                    } else {
                        createJsonFile(sFilePath);
                        System.out.println("Created Json File successfully.");
                    }
                    jsonArray.add(FormName);
                } 
            }
            json.put("form", jsonArray);
        }
        
        try (FileWriter file = new FileWriter(FILE_PATH)) {
            file.write(json.toString()); // Use toString(4) for pretty-printing
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
    }
    
    private static boolean createJsonFile(String sFilePath){
        // Create a JSONObject with the desired structure
        JSONObject jsonObject = new JSONObject();
        // Write the JSONObject to a file
        try (FileWriter file = new FileWriter(sFilePath)) {
            file.write(jsonObject.toJSONString());
            System.out.println("JSON file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    private static boolean deleteJsonFile(String sFilePath){
        // Create a File object representing the JSON file
        File Delfile = new File(sFilePath);
        // Check if the file exists and is a file (not a directory)
        if (Delfile.exists() && Delfile.isFile()) {
            // Attempt to delete the file
            if (Delfile.delete()) {
                System.out.println(sFilePath + " File deleted successfully.");
            } else {
                System.out.println(sFilePath + " Failed to delete the file.");
                return false;
            }
        } else {
            System.out.println(sFilePath + " File does not exist or is not a regular file.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Load previously opened tab based on json file
     * @return 
     */
    public static List loadCurrentTab() {
        List<String> tabIds = new ArrayList<>();
        File Delfile = new File(FILE_PATH);
        if (Delfile.exists() && Delfile.isFile()) {
        } else {
            return tabIds;
        }
        
        try {
            // Parse the JSON file
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(FILE_PATH));

            // Extract the JSON array
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray currentTabIds = (JSONArray) jsonObject.get("form");

            // Iterate over the array and print each value
            for (Object tabId : currentTabIds) {
                System.out.println(tabId);
                tabIds.add(tabId.toString());
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return tabIds;
    }
    
    public static String getJsonFileName(String sFormName){
        switch(sFormName){
            /*DIRECTORY*/
            case "Activity":
                return "Activity_state.json";
//            case "Activity Approval":
//                return "ActivityApproval_state.json";
            case "Customer":
                return "CustomerInfoMaster_state.json";
            case "Customer Vehicle Information":
                return "CustomerVehicleInfo_state.json";
            case "Vehicle Sales Information":
                return "VehicleSalesInfo_state.json";
//            case "Supplier":
//                return "SupplierMaster_state.json";
            /*SALES*/
//            case "Sales Agent":
//                return "SalesAgent_state.json";
            case "Vehicle Description":
                return "VehicleDescription_state.json";
//            case "Unit Receiving":
//                return "UnitReceivingMaster_state.json";
            case "Inquiry":
                return "InquiryMaster_state.json";
//            case "Vehicle Reservation Approval":
//                return "";
            case "Unit Delivery Receipt":
                return "UnitDeliverReceipt_state.json";
            case "Vehicle Sales Proposal":
                return "VehicleSalesProposalMaster_state.json";
            case "Sales Job Order Information":
                return "SalesJobOrderMaster_state.json";
            /*ACCOUNTING*/
            case "Bank":
                return "BankInformation_state.json";
            /*CASHIERING*/
//            case "Acknowledgement Receipt":
//                return "";
//            case "Billing Statement":
//                return "";
//            case "Collection Receipt":
//                return "";
//            case "Official Receipt":
//                return "";
//            case "Parts Sales Invoice":
//                return "";    
            case "Vehicle Sales Invoice":
                return "VehicleSalesInvoice_state.json";
            /*PARTS*/
            case "Item Information":
                return "ItemEntry_state.json";
//            case "Sales Parts Request":
//                return "";
                //return "SalesPartsRequest_state.json";
            /*SERVICE*/
            case "Service Job Order Information":
                return "ServiceJobOrderMaster_state.json";
            default:
                return "";
        }
    }
    
    /**
     * Deleted json file per form. Otherwise clear json file
     * @param sFormName 
     */
    public static void closeTab(String sFormName){
        if(!getJsonFileName(sFormName).isEmpty()){
        } else {
            return;
        }
        String sFilePath = "D://GGC_Java_Systems/config/Autapp_json/" + getJsonFileName(sFormName) ;
        
        if(!deleteJsonFile(sFilePath)){
            try {
                //Parse the JSON file
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(sFilePath));
                JSONObject json = new JSONObject();
                // Iterate over the keys of the JSONObject
                for (Object key : jsonObject.keySet()) {
                    Object value = jsonObject.get(key);
                    if (value instanceof JSONArray) {
                        json.put(key, new JSONArray());
                    } else if (value instanceof JSONObject) {
                        json.put(key, new JSONObject());
                    }
                }
                json = new JSONObject();
                try (FileWriter file = new FileWriter(sFilePath)) {
                    file.write(json.toString()); // Use toString(4) for pretty-printing
                    System.out.println("JSON file cleared successfully.");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
