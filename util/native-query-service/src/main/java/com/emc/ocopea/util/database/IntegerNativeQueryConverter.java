// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: IntegerNativeQueryConverter.java 78679 2013-09-09 09:29:24Z liebea $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @author nivenb
 */
public class IntegerNativeQueryConverter implements NativeQueryRowBatchConverter<Integer> {
    protected final String fieldName;
    protected final Integer pos;

    public IntegerNativeQueryConverter() {
        this.fieldName = null;
        this.pos = null;
    }

    public IntegerNativeQueryConverter(String fieldName) {
        this.fieldName = fieldName;
        this.pos = null;
    }

    public IntegerNativeQueryConverter(Integer pos) {
        this.fieldName = null;
        this.pos = pos;
    }

    public static Integer getInteger(ResultSet rset, int pos) throws SQLException {
        int retVal = rset.getInt(pos);
        return rset.wasNull() ? null : retVal;
    }

    public static Integer getInteger(ResultSet rset, String fieldName) throws SQLException {
        int retVal = rset.getInt(fieldName);
        return rset.wasNull() ? null : retVal;
    }

    @Override
    public Integer convertRow(ResultSet rset, int pos) throws SQLException {
        if (fieldName != null) {
            return getInteger(rset, fieldName);
        }

        int posToUse = this.pos != null ? this.pos : pos;
        if (posToUse <= 0) {
            posToUse = 1;
        }

        return getInteger(rset, posToUse);
    }

    @Override
    public void finalizeBatch(Collection<Integer> loadedObjects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
