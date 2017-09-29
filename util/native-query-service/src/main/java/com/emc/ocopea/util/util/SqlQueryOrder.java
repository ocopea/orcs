// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: SqlQueryOrder 11/26/2015 2:24 PM englee
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.util;

/**
 * @author englee
 */
public class SqlQueryOrder {

    private String orderBy;
    private SortOrder sortOrder;

    public SqlQueryOrder() {
    }

    public SqlQueryOrder(String orderBy, SortOrder sortOrder) {
        this.orderBy = orderBy;
        this.sortOrder = sortOrder;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public enum SortOrder {
        ASC,
        DESC
    }
}
