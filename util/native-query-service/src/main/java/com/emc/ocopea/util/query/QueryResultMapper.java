// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.query;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Provides mapping from javax.persistence.Query result to user defined type.
 */
public final class QueryResultMapper {

    private QueryResultMapper() {
    }

    public static <T> List<T> mapRows(List<?> data, QueryResultExtractor<T> extractor) {
        List<T> result = new ArrayList<>(data.size());
        for (Object o : data) {
            result.add(extractor.extract(o));
        }
        return result;
    }

    public static <T> List<T> mapRows(List<?> data, QueryResultSetExtractor<T> extractor) {
        List<T> result = new ArrayList<>(data.size());
        for (Object o : data) {
            result.add(extractor.extract((Object[]) o));
        }
        return result;
    }

    public static <T> T map(Object data, QueryResultExtractor<T> extractor) {
        return extractor.extract(data);
    }

    public static <T> T map(Object data, QueryResultSetExtractor<T> extractor) {
        return extractor.extract((Object[]) data);
    }

}
