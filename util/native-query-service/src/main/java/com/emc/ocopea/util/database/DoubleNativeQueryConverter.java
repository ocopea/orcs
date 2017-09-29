// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class DoubleNativeQueryConverter implements NativeQueryRowBatchConverter<Double> {

    public static Double getDouble(ResultSet rset, int pos) throws SQLException {
        double dbl = rset.getLong(pos);
        if (rset.wasNull()) {
            return null;
        }
        return dbl;
    }

    public static Double getDouble(ResultSet rset, String fieldName) throws SQLException {
        double dbl = rset.getDouble(fieldName);
        if (rset.wasNull()) {
            return null;
        }
        return dbl;
    }

    @Override
    public Double convertRow(ResultSet rset, int pos) throws SQLException {
        if (pos < 1) {
            pos = 1;
        }
        return getDouble(rset, pos);
    }

    @Override
    public void finalizeBatch(Collection<Double> loadedObjects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
