// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: BooleanPrimitiveNativeQueryConverter.java 78679 2013-09-09 09:29:24Z liebea $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * This query converter will return null values as 'false'. If you want the ability to return null values, please use
 * BooleanNativeQueryConverter
 *
 * @author nivenb
 */
public class BooleanPrimitiveNativeQueryConverter implements NativeQueryRowBatchConverter<Boolean> {

    @Override
    public Boolean convertRow(ResultSet rset, int pos) throws SQLException {
        return rset.getBoolean(pos);
    }

    @Override
    public void finalizeBatch(Collection<Boolean> loadedObjects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
