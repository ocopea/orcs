// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: liebea
 * Date: 8/30/12
 * Time: 2:32 PM
 */
public class MapNativeQueryHandler<KeyT, ValueT> implements NativeQueryHandler<Map<KeyT, ValueT>> {
    private final NativeQueryRowBatchConverter<KeyT> keyConverter;
    private final NativeQueryRowBatchConverter<ValueT> valueConverter;
    private final int keyFieldPosition;
    private final int valueKeyPosition;

    public MapNativeQueryHandler(
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter) {
        this(keyConverter, valueConverter, null, null);
    }

    public MapNativeQueryHandler(
            NativeQueryRowBatchConverter<KeyT> keyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            Integer keyFieldPosition,
            Integer valueKeyPosition) {
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
        this.keyFieldPosition = keyFieldPosition == null ? 1 : keyFieldPosition;
        this.valueKeyPosition = valueKeyPosition == null ? 2 : valueKeyPosition;
    }

    @Override
    public Map<KeyT, ValueT> handleNativeQueryResult(ResultSet rset) throws SQLException, NativeQueryException {
        Map<KeyT, ValueT> map = new HashMap<KeyT, ValueT>();

        while (rset.next()) {
            map.put(keyConverter.convertRow(rset, keyFieldPosition), valueConverter.convertRow(rset, valueKeyPosition));
        }

        if (!map.isEmpty()) {
            keyConverter.finalizeBatch(map.keySet());
            valueConverter.finalizeBatch(map.values());
        }
        return map;
    }

}
