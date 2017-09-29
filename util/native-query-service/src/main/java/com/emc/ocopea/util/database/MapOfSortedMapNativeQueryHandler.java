// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * User: chen lin
 * Date: 2/16/15
 * Time: 2:05 PM
 */
public class MapOfSortedMapNativeQueryHandler<OuterKeyT, InnerKeyT, ValueT>
        implements NativeQueryHandler<Map<OuterKeyT, SortedMap<InnerKeyT, ValueT>>> {
    private NativeQueryRowBatchConverter<OuterKeyT> outerKeyConverter;
    private NativeQueryRowBatchConverter<InnerKeyT> innerKeyConverter;
    private NativeQueryRowBatchConverter<ValueT> valueConverter;
    private int outerKeyFieldPosition = 1;
    private int innerKeyFieldPosition = 2;
    private int valueKeyPosition = 3;

    public MapOfSortedMapNativeQueryHandler(
            NativeQueryRowBatchConverter<OuterKeyT> outerKeyConverter,
            NativeQueryRowBatchConverter<InnerKeyT> innerKeyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter) {
        this.outerKeyConverter = outerKeyConverter;
        this.innerKeyConverter = innerKeyConverter;
        this.valueConverter = valueConverter;
    }

    public MapOfSortedMapNativeQueryHandler(
            NativeQueryRowBatchConverter<OuterKeyT> outerKeyConverter,
            NativeQueryRowBatchConverter<InnerKeyT> innerKeyConverter,
            NativeQueryRowBatchConverter<ValueT> valueConverter,
            int outerKeyFieldPosition,
            int innerKeyFieldPosition,
            int valueKeyPosition) {
        this.outerKeyConverter = outerKeyConverter;
        this.innerKeyConverter = innerKeyConverter;
        this.valueConverter = valueConverter;
        this.outerKeyFieldPosition = outerKeyFieldPosition;
        this.innerKeyFieldPosition = innerKeyFieldPosition;
        this.valueKeyPosition = valueKeyPosition;
    }

    @Override
    public Map<OuterKeyT, SortedMap<InnerKeyT, ValueT>> handleNativeQueryResult(ResultSet rset)
            throws SQLException, NativeQueryException {
        Map<OuterKeyT, SortedMap<InnerKeyT, ValueT>> map = new TreeMap<>();

        List<ValueT> allValuesInOneList = new ArrayList<>();
        List<InnerKeyT> allInnerKeysInOneList = new ArrayList<>();

        while (rset.next()) {
            OuterKeyT outerKey = outerKeyConverter.convertRow(rset, outerKeyFieldPosition);
            InnerKeyT innerKey = innerKeyConverter.convertRow(rset, innerKeyFieldPosition);
            ValueT value = valueConverter.convertRow(rset, valueKeyPosition);

            SortedMap<InnerKeyT, ValueT> existingInnerMap = map.get(outerKey);
            if (existingInnerMap == null) {
                existingInnerMap = new TreeMap<>();
                map.put(outerKey, existingInnerMap);
            }
            existingInnerMap.put(innerKey, value);

            allValuesInOneList.add(value);
            allInnerKeysInOneList.add(innerKey);

        }

        outerKeyConverter.finalizeBatch(map.keySet());
        innerKeyConverter.finalizeBatch(allInnerKeysInOneList);
        valueConverter.finalizeBatch(allValuesInOneList);

        return map;
    }
}
