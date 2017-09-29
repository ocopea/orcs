// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.util;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SqlBuilder {

    public static final int MAX_ITEMS_FOR_IN_CLAUSE = 1000;

    public static final String SELECT = "select ";
    public static final String DELETE = "delete";

    private static final String FROM = " from ";

    protected String command;
    protected String selection = "*";
    protected String tableName;
    protected StringBuilder whereClause;
    protected String querySuffix;
    protected List<Object> queryParams;

    public SqlBuilder(String tableName) {
        this.tableName = tableName;
        this.command = SELECT;
    }

    public SqlBuilder(String tableName, String command) {
        this.tableName = tableName;
        this.command = command;
    }

    public static String getSortOrderString(SqlQueryOrder.SortOrder sortOrder) {
        return sortOrder == SqlQueryOrder.SortOrder.ASC ? "ASC NULLS FIRST" : "DESC NULLS LAST";
    }

    /***
     * add a case insensitive LIKE condition to the where clause and supply a corresponding param
     */
    public SqlBuilder addCaseInsensitiveLikeCondition(String fieldName, String fieldValue) {
        if (!fieldValue.isEmpty()) {
            addAndToWhereClause("lower(" + fieldName + ") like lower(?)");
            addQueryParam("%" + fieldValue + "%");
        }

        return this;
    }

    /***
     * add an IN condition to the where clause
     */
    public SqlBuilder addInCondition(String fieldName, Collection<String> values) {
        if (values != null && !values.isEmpty()) {
            if (values.size() <= MAX_ITEMS_FOR_IN_CLAUSE) {
                addSimpleInCondition(fieldName, values);
            } else {
                addComplexInCondition(fieldName, values);
            }
        }

        return this;
    }

    private void addSimpleInCondition(String fieldName, Collection<String> values) {
        String inList = createInListString(values.size());
        addAndToWhereClause(fieldName + " in " + inList);
        addStringQueryParams(values);
    }

    private void addComplexInCondition(String fieldName, Collection<String> values) {
        String complexInList = createComplexInListString(fieldName, values.size());
        addCondition(complexInList);
        addStringQueryParams(values);
    }

    // creates an "in" sql group string (e.g. (?,?,?)) for the given collection
    private String createInListString(int itemsCount) {
        StringBuilder inListSB = new StringBuilder("(");
        for (int i = 0; i < itemsCount; i++) {
            inListSB.append("?,");
        }
        // delete last ','
        inListSB.deleteCharAt(inListSB.length() - 1);
        inListSB.append(")");
        return inListSB.toString();
    }

    private String createComplexInListString(String fieldName, int itemsCount) {
        StringBuilder complexInClause = new StringBuilder("(");
        for (int i = 0; i < itemsCount; i += SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE) {
            complexInClause.append(fieldName).append(" in ");
            int itemsLeft = itemsCount - i;
            int currentItemsCount =
                    itemsLeft > SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE ? SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE : itemsLeft;
            String currentInClause = createInListString(currentItemsCount);
            complexInClause.append(currentInClause);
            complexInClause.append(" or ");
        }
        // delete last " or "
        complexInClause.delete(complexInClause.length() - 4, complexInClause.length());
        complexInClause.append(")");
        return complexInClause.toString();
    }

    /***
     * adds a multiple OR conditions of key-value pairs
     * e.g. ((keyColumnName = ? and valueColumnName = ?) or (keyColumnName = ? and valueColumnName = ?) or ...)
     */
    public SqlBuilder addMultipleOrConditionOfKeyValuePairs(
            String keyColumnName,
            String valueColumnName,
            Map<String, String> keyValuePairs) {
        if (keyValuePairs == null) {
            return this;
        }
        StringBuilder multiplePairsOrConditionSB = new StringBuilder("(");
        String keyValueCondition = "(" + keyColumnName + " = ? and " + valueColumnName + " = ?)";
        for (Map.Entry<String, String> keyValueEntry : keyValuePairs.entrySet()) {
            multiplePairsOrConditionSB.append(keyValueCondition).append(" or ");
            String key = keyValueEntry.getKey();
            addQueryParam(key);
            String value = keyValueEntry.getValue();
            addQueryParam(value);
        }
        // delete last " or "
        multiplePairsOrConditionSB.delete(multiplePairsOrConditionSB.length() - 4, multiplePairsOrConditionSB.length());
        multiplePairsOrConditionSB.append(")");
        addAndToWhereClause(multiplePairsOrConditionSB.toString());

        return this;
    }

    // add a condition to the where clause
    public SqlBuilder addCondition(String condition) {
        addAndToWhereClause(condition);

        return this;
    }

    // add an AND condition to the where clause
    protected void addAndToWhereClause(String condition) {
        addToWhereClause(condition, "and");
    }

    // add an OR condition to the where clause
    protected void addOrToWhereClause(String condition) {
        addToWhereClause(condition, "or");
    }

    // add a condition to the where clause
    private void addToWhereClause(String condition, String operator) {
        if (whereClause == null) {
            whereClause = new StringBuilder();
        }
        if (whereClause.length() != 0) {
            whereClause.append(" " + operator + " ");
        }
        whereClause.append(condition);
    }

    /***
     * adds a query param
     */
    public SqlBuilder addQueryParam(Object param) {
        if (queryParams == null) {
            queryParams = new ArrayList<>();
        }
        queryParams.add(param);

        return this;
    }

    /***
     * adds query params
     */
    public SqlBuilder addQueryParams(Collection<Object> params) {
        if (queryParams == null) {
            queryParams = new ArrayList<>();
        }
        queryParams.addAll(params);

        return this;
    }

    private void addStringQueryParams(Collection<String> values) {
        for (String value : values) {
            addQueryParam(value);
        }
    }

    // Returns generated sql query string.
    public String getSqlString() {
        return command + (command.equals(DELETE) ? "" : selection) + FROM + tableName + getWhereClauseString() +
                getQuerySuffixString();
    }

    protected String getWhereClauseString() {
        return whereClause != null && whereClause.length() > 0 ? " where " + whereClause.toString() : "";
    }

    private String getQuerySuffixString() {
        return querySuffix != null && !querySuffix.isEmpty() ? " " + querySuffix : "";
    }

    public SqlBuilder setSelection(String selection) {
        this.selection = selection;

        return this;
    }

    public SqlBuilder addQuerySuffix(String querySuffix) {
        this.querySuffix = querySuffix;

        return this;
    }

    public SqlBuilder addQuerySuffixWithParam(String suffix, Object param) {
        addQuerySuffix(suffix);
        addQueryParam(param);

        return this;
    }

    @NoJavadoc
    // TODO add JavaDoc
    public SqlBuilder addOrderByClause(SqlQueryOrder sqlQueryOrder) {
        if (sqlQueryOrder == null || sqlQueryOrder.getSortOrder() == null ||
                sqlQueryOrder.getOrderBy() == null || sqlQueryOrder.getOrderBy().isEmpty()) {
            return this;
        }
        String sortOrder = getSortOrderString(sqlQueryOrder.getSortOrder());
        String orderByClause = "order by " + sqlQueryOrder.getOrderBy() + " " + sortOrder;
        addQuerySuffix(orderByClause);

        return this;
    }

    public List<Object> getQueryParams() {
        return queryParams;
    }

    @Override
    public String toString() {
        return getSqlString();
    }

    public String build() {
        return getSqlString();
    }
}
