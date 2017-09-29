// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by martiv6 on 18/06/2014.
 */
public class ByteArrayNativeQueryConverter implements NativeQueryRowBatchConverter<byte[]> {

    @Override
    public byte[] convertRow(ResultSet rset, int columnIndex) throws SQLException {
        return rset.getBytes(columnIndex);
    }

    @Override
    public void finalizeBatch(Collection<byte[]> loadedObjects) throws NativeQueryException {

    }

}
