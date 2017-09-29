// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: liebea
 * Date: 8/30/12
 * Time: 2:45 PM
 */
public class SetNativeQueryHandler<T> implements NativeQueryHandler<Set<T>> {

    protected NativeQueryRowBatchConverter<T> converter;

    public SetNativeQueryHandler(NativeQueryRowBatchConverter<T> converter) {
        this.converter = converter;
    }

    @Override
    public Set<T> handleNativeQueryResult(ResultSet rset) throws SQLException, NativeQueryException {
        Set<T> resultList = new HashSet<>();
        while (rset.next()) {
            T convertedRow = converter.convertRow(rset, NativeQueryRowBatchConverter.ALL_FIELDS);
            if (convertedRow != null) {
                resultList.add(convertedRow);
            }
        }
        converter.finalizeBatch(resultList);
        return resultList;
    }
}
