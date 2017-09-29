// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: liebea
 * Date: 8/30/12
 * Time: 3:33 PM
 */
public class UuidNativeQueryConverter implements NativeQueryRowBatchConverter<UUID> {
    protected final String fieldName;
    protected final Integer pos;

    public UuidNativeQueryConverter() {
        this.fieldName = null;
        this.pos = null;

    }

    public UuidNativeQueryConverter(String fieldName) {
        this.fieldName = fieldName;
        this.pos = null;
    }

    public UuidNativeQueryConverter(int pos) {
        this.fieldName = null;
        this.pos = pos;
    }

    public static UUID getUuid(ResultSet rset, int posToUse) throws SQLException {
        String strValue = rset.getString(posToUse);
        if (strValue == null) {
            return null;
        }
        return UUID.fromString(strValue);
    }

    public static UUID getUuid(ResultSet rset, String fieldName) throws SQLException {
        String strValue = rset.getString(fieldName);
        if (strValue == null) {
            return null;
        }
        return UUID.fromString(strValue);
    }

    @Override
    public UUID convertRow(ResultSet rset, int pos) throws SQLException {

        if (fieldName != null) {
            String strValue = rset.getString(fieldName);
            if (strValue == null) {
                return null;
            }
            return UUID.fromString(strValue);
        }

        int posToUse = this.pos != null ? this.pos : pos;
        if (posToUse <= 0) {
            posToUse = 1;
        }

        return getUuid(rset, posToUse);
    }

    @Override
    public void finalizeBatch(Collection<UUID> loadedObjects) {
    }
}
