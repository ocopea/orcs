// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.query;

/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 * <p>
 * Extracts an object from a row of javax.persistence.Query
 *
 * @param <T> expected type
 */
public interface QueryResultSetExtractor<T> {

    T extract(Object[] dataSet);
}
