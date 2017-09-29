// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with true love.
 * User: liebea
 * Date: 4/14/13
 * Time: 10:06 AM
 */
public class MapOfListNativeQueryHandler<KeyT, ValueT> implements NativeQueryHandler<Map<KeyT, List<ValueT>>> {
    private NativeQueryRowBatchConverter<KeyT> keyConverter;
    private NativeQueryRowBatchConverter<ValueT> valueConverter;
    private int keyFieldPosition = 1;
    private int valueKeyPosition = 2;

    public MapOfListNativeQueryHandler(
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter) {
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    public MapOfListNativeQueryHandler(
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            int keyFieldPosition,
            int valueKeyPosition) {
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
        this.keyFieldPosition = keyFieldPosition;
        this.valueKeyPosition = valueKeyPosition;
    }

    @Override
    public Map<KeyT, List<ValueT>> handleNativeQueryResult(ResultSet rset) throws SQLException, NativeQueryException {
        Map<KeyT, List<ValueT>> map = new HashMap<>();

        List<ValueT> allValuesInOneList = new ArrayList<>();
        while (rset.next()) {
            KeyT keyValue = keyConverter.convertRow(rset, keyFieldPosition);
            ValueT value = valueConverter.convertRow(rset, valueKeyPosition);

            List<ValueT> existingList = map.get(keyValue);
            if (existingList == null) {
                existingList = new ArrayList<>();
                map.put(keyValue, existingList);
            }
            existingList.add(value);
            allValuesInOneList.add(value);
        }

        keyConverter.finalizeBatch(map.keySet());
        valueConverter.finalizeBatch(allValuesInOneList);

        return map;
    }
}
