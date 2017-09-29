// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: chen lin
 * Date: 17/02/2015
 * Time: 12:38
 */
public class EnumNativeQueryConverter<E extends Enum> implements NativeQueryRowBatchConverter<E> {
    private final String fieldName;
    private final Integer pos;
    private E[] enumValues;

    public EnumNativeQueryConverter(E[] values) {
        this.enumValues = Arrays.copyOf(values, values.length);
        this.fieldName = null;
        this.pos = null;
    }

    public EnumNativeQueryConverter(E[] values, String fieldName) {
        this.enumValues = Arrays.copyOf(values, values.length);
        this.fieldName = fieldName;
        this.pos = null;
    }

    public EnumNativeQueryConverter(E[] values, Integer pos) {
        this.enumValues = Arrays.copyOf(values, values.length);
        this.pos = pos;
        this.fieldName = null;
    }

    @Override
    public E convertRow(ResultSet rset, int pos) throws SQLException {
        if (fieldName != null) {
            return getEnum(rset, fieldName);
        }

        int posToUse = this.pos != null ? this.pos : pos;
        if (posToUse <= 0) {
            posToUse = 1;
        }

        return getEnum(rset, posToUse);
    }

    @Override
    public void finalizeBatch(Collection<E> loadedObjects) throws NativeQueryException {

    }

    private E getEnum(ResultSet rset, String fieldName) throws SQLException {
        int enumOrdinal = rset.getInt(fieldName);
        return rset.wasNull() ? null : enumValues[enumOrdinal];
    }

    public E getEnum(ResultSet rset, int pos) throws SQLException {
        int enumOrdinal = rset.getInt(fieldName);
        return rset.wasNull() ? null : enumValues[enumOrdinal];

    }
}
