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
 * User: liebea
 * Date: 8/30/12
 * Time: 3:33 PM
 */
public class StringNativeQueryConverter implements NativeQueryRowBatchConverter<String> {
    protected String fieldName = null;
    protected int pos = 1;

    public StringNativeQueryConverter() {
    }

    public StringNativeQueryConverter(int pos) {
        this.pos = pos;
    }

    public StringNativeQueryConverter(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String convertRow(ResultSet rset, int pos) throws SQLException {
        if (fieldName != null) {
            return rset.getString(fieldName);
        }

        int posToUse = pos > 0 ? pos : this.pos;

        return rset.getString(posToUse);
    }

    @Override
    public void finalizeBatch(Collection<String> loadedObjects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
