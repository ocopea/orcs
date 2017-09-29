// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: chen lin
 * Date: 16/02/2015
 * Time: 14:23
 */
public class DateNativeQueryConverter implements NativeQueryRowBatchConverter<Date> {
    private final String fieldName;
    private final Integer pos;

    public DateNativeQueryConverter() {
        this.fieldName = null;
        this.pos = null;
    }

    public DateNativeQueryConverter(String fieldName) {
        this.fieldName = fieldName;
        this.pos = null;
    }

    public DateNativeQueryConverter(Integer pos) {
        this.fieldName = null;
        this.pos = pos;
    }

    @Override
    public Date convertRow(ResultSet rset, int pos) throws SQLException {
        if (fieldName != null) {
            Long timestamp = rset.getLong(fieldName);
            if (timestamp <= 0) {
                return null;
            }
            return new Date(timestamp);
        }

        int posToUse = this.pos != null ? this.pos : pos;
        if (posToUse <= 0) {
            posToUse = 1;
        }

        return getDate(rset, posToUse);

    }

    @Override
    public void finalizeBatch(Collection<Date> loadedObjects) throws NativeQueryException {
    }

    private Date getDate(ResultSet rset, int posToUse) throws SQLException {
        Long timestamp = rset.getLong(posToUse);
        if (timestamp <= 0) {
            return null;
        }
        return new Date(timestamp);
    }
}
