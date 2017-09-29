// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: liebea
 * Date: 8/30/12
 * Time: 2:45 PM
 */
public class ListNativeQueryHandler<T> implements NativeQueryHandler<List<T>> {

    protected NativeQueryRowBatchConverter<T> converter;

    public ListNativeQueryHandler(NativeQueryRowBatchConverter<T> converter) {
        this.converter = converter;
    }

    @Override
    public List<T> handleNativeQueryResult(ResultSet rset) throws SQLException, NativeQueryException {
        List<T> resultList = new ArrayList<>();
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
