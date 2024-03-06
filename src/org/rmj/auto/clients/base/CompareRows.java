/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.auto.clients.base;

import java.sql.SQLException;
import java.util.Objects;
import javax.sql.rowset.CachedRowSet;

/**
 *
 * @author User
 */
public class CompareRows {    
    public static boolean isRowEqual(CachedRowSet row1, CachedRowSet row2, int rowNum) throws SQLException{
        if (row1 != null && row2 != null) {
//            int row1Count = row1.size();
//            int row2Count = row2.size();
            
            int row1Count = 0; //row1.size(); changed by arsiela 03-05-2024
            row1.last();
            row1Count = row1.getRow();
            
            int row2Count = 0; //row2.size(); changed by arsiela 03-05-2024
            row2.last();
            row2Count = row2.getRow();
            
            System.out.println("row1Count " + row1Count);
            System.out.println("row2Count " + row2Count);

            if (rowNum < 1 || rowNum > row1Count || rowNum > row2Count) {
                // The specified row number is greater than the number of rows in one of the cached row sets
                return false;
            }

            row1.absolute(rowNum);
            row2.absolute(rowNum);

            int columnCount = row1.getMetaData().getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                Object value1 = row1.getObject(i);
                Object value2 = row2.getObject(i);
                if (!Objects.equals(value1, value2)) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
//        if(row1 != null && row2 != null){
//            row1.beforeFirst();
//            row2.beforeFirst();
//            while (row1.next() || row2.next()) {
//                int columnCount = row1.getMetaData().getColumnCount();
//                
//                if (row1.getRow() != row2.getRow()) {
//                    // Rows don't match
//                    return false;
//                }
//                
//                for (int i = 1; i <= columnCount; i++) {
//                    Object value1 = row1.getObject(i);
//                    Object value2 = row2.getObject(i);
//                    if (!Objects.equals(value1, value2)) {
//                        return false;
//                    }
//                }
//            }
//        }else{
//            return false;
//        }
//    return true;          
//    }
    
//}
