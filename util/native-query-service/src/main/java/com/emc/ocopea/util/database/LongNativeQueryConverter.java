// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: poznia
 * Date: 6/2/13
 * Time: 3:44 PM
 */
public class LongNativeQueryConverter implements NativeQueryRowBatchConverter<Long> {
    protected final String fieldName;
    protected final Integer pos;

    public LongNativeQueryConverter(String fieldName) {
        this.fieldName = fieldName;
        this.pos = null;
    }

    public LongNativeQueryConverter(int pos) {
        this.fieldName = null;
        this.pos = pos;
    }

    public LongNativeQueryConverter() {
        this.fieldName = null;
        this.pos = null;
    }

    public static Long getLong(ResultSet rset, int pos) throws SQLException {
        long l = rset.getLong(pos);
        if (rset.wasNull()) {
            return null;
        }
        return l;
    }

    public static Long getLong(ResultSet rset, String fieldName) throws SQLException {
        long l = rset.getLong(fieldName);
        if (rset.wasNull()) {
            return null;
        }
        return l;
    }

    @Override
    public Long convertRow(ResultSet rset, int pos) throws SQLException {
        if (fieldName != null) {
            return getLong(rset, fieldName);
        }

        int posToUse = this.pos != null ? this.pos : pos;
        if (posToUse <= 0) {
            posToUse = 1;
        }

        return getLong(rset, posToUse);
    }

    @Override
    public void finalizeBatch(Collection<Long> loadedObjects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
