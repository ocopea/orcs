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
 * Time: 3:12 PM
 */
public interface NativeQueryRowBatchConverter<T> {
    int ALL_FIELDS = -1;

    T convertRow(ResultSet rset, int pos) throws SQLException;

    default void finalizeBatch(Collection<T> loadedObjects) throws NativeQueryException {
        // by default do nothing
    }
}
