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
    public static boolean isRowEqual(CachedRowSet row1, CachedRowSet row2) throws SQLException{
        row1.beforeFirst();
        row2.beforeFirst();
        while (row1.next() && row2.next()) {
            int columnCount = row1.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                Object value1 = row1.getObject(i);
                Object value2 = row2.getObject(i);
                if (!Objects.equals(value1, value2)) {
                    return false;
                }
            }
        }
    return true;          
    }
    
}
