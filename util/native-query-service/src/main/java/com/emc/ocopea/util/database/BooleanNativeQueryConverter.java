// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.database;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * This query converter may return null values. If you desire false to be returned for nulls, please use
 * BooleanPrimitiveNativeQueryConverter
 * <p>
 * Created with IntelliJ IDEA. User: liebea Date: 8/30/12 Time: 3:33 PM
 */
public class BooleanNativeQueryConverter implements NativeQueryRowBatchConverter<Boolean> {

    @NoJavadoc
    // TODO add Javadoc
    public static Boolean getBoolean(ResultSet rset, int pos) throws SQLException {
        String asStr = rset.getString(pos);

        if (asStr == null) {
            return null;
        }

        // special behaviour for postgres which returns 't' and 'f' which Boolean.parseBoolean doesn't know about...
        if (asStr.equals("t")) {
            return true;
        }

        return Boolean.parseBoolean(asStr);
    }

    @NoJavadoc
    // TODO add Javadoc
    public static Boolean getBoolean(ResultSet rset, String name) throws SQLException {
        String asStr = rset.getString(name);

        if (asStr == null) {
            return null;
        }

        // special behaviour for postgres which returns 't' and 'f' which Boolean.parseBoolean doesn't know about...
        if (asStr.equals("t")) {
            return true;
        }

        return Boolean.parseBoolean(asStr);
    }

    @Override
    public Boolean convertRow(ResultSet rset, int pos) throws SQLException {
        return getBoolean(rset, pos);
    }

    @Override
    public void finalizeBatch(Collection<Boolean> loadedObjects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
