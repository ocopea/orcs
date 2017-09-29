// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: TestSqlBuilder 10/25/2015 4:10 PM englee
*
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlBuilderTest {

    @Test
    public void testSimpleQuery() {
        SqlBuilder builder = new SqlBuilder("table");
        Assert.assertEquals("wrong query", "select * from table", builder.getSqlString());
    }

    @Test
    public void testDelete() {
        SqlBuilder builder = new SqlBuilder("table", SqlBuilder.DELETE)
                .setSelection("nevermind")
                .addCondition("id=?")
                .addQueryParam("123");
        Assert.assertEquals("wrong query", "delete from table where id=?", builder.build());
    }

    @Test
    public void testQueryWithSelection() {
        SqlBuilder builder = new SqlBuilder("table");
        builder.setSelection("id");
        Assert.assertEquals("wrong query", "select id from table", builder.getSqlString());
    }

    @Test
    public void testLikeClausesQuery() {
        SqlBuilder builder = new SqlBuilder("table");
        builder.addCaseInsensitiveLikeCondition("f1", "v1");
        builder.addCaseInsensitiveLikeCondition("f2", "v2");
        builder.addCaseInsensitiveLikeCondition("f3", "v3");
        String expected =
                "select * from table where lower(f1) like lower(?) and lower(f2) like lower(?) and lower(f3) like lower(?)";
        Assert.assertEquals("wrong query", expected, builder.getSqlString());
        List<Object> queryParams = builder.getQueryParams();
        Assert.assertEquals("should be 3 params", 3, queryParams.size());
        Assert.assertEquals("should be v1", "%v1%", queryParams.get(0));
        Assert.assertEquals("should be v2", "%v2%", queryParams.get(1));
        Assert.assertEquals("should be v3", "%v3%", queryParams.get(2));
    }

    @Test
    public void testInClausesQuery() {
        SqlBuilder builder = new SqlBuilder("table");
        builder.addInCondition("f1", new ArrayList<String>() {{
            add("v1");
            add("v2");
            add("v3");
        }});
        builder.addInCondition("f2", new ArrayList<String>() {{
            add("v4");
            add("v5");
        }});
        String expected = "select * from table where f1 in (?,?,?) and f2 in (?,?)";
        Assert.assertEquals("wrong query", expected, builder.getSqlString());
        List<Object> queryParams = builder.getQueryParams();
        Assert.assertEquals("should be 5 params", 5, queryParams.size());
        Assert.assertEquals("should be v1", "v1", queryParams.get(0));
        Assert.assertEquals("should be v2", "v2", queryParams.get(1));
        Assert.assertEquals("should be v3", "v3", queryParams.get(2));
        Assert.assertEquals("should be v4", "v4", queryParams.get(3));
        Assert.assertEquals("should be v5", "v5", queryParams.get(4));
    }

    @Test
    public void testLargeAmountInClausesQuery() {
        runSimpleInClause(SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE);
        runComplexInClause(SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE + 1);
        runComplexInClause(2 * SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE);
        runComplexInClause(3 * SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE - 1);
    }

    private void runSimpleInClause(int itemsCount) {
        SqlBuilder builder = createSqlBuilderWithInClause(itemsCount);
        StringBuilder simpleInClause = createSimpleInClauseString(itemsCount);

        String expected = "select * from table where field in " + simpleInClause;
        Assert.assertEquals("wrong query", expected, builder.getSqlString());
        verifySqlBuilderParamsForInClause(itemsCount, builder);
    }

    private void runComplexInClause(int itemsCount) {
        SqlBuilder builder = createSqlBuilderWithInClause(itemsCount);

        StringBuilder complexInClause = createComplexInClauseString(itemsCount);

        String expected = "select * from table where " + complexInClause;
        Assert.assertEquals("wrong query", expected, builder.getSqlString());
        verifySqlBuilderParamsForInClause(itemsCount, builder);
    }

    private SqlBuilder createSqlBuilderWithInClause(int itemsCount) {
        SqlBuilder builder = new SqlBuilder("table");
        List<String> items = new ArrayList<>(itemsCount);
        String baseItemName = "item";
        for (int i = 0; i < itemsCount; i++) {
            items.add(baseItemName + i);
        }
        builder.addInCondition("field", items);
        return builder;
    }

    private StringBuilder createSimpleInClauseString(int itemsCount) {
        StringBuilder simpleInClause = new StringBuilder("(");
        for (int i = 0; i < itemsCount; i++) {
            simpleInClause.append("?,");
        }
        // delete last ','
        simpleInClause.deleteCharAt(simpleInClause.length() - 1);
        simpleInClause.append(")");
        return simpleInClause;
    }

    private StringBuilder createComplexInClauseString(int itemsCount) {
        StringBuilder complexInClause = new StringBuilder("(");
        for (int i = 0; i < itemsCount; i += SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE) {
            complexInClause.append("field in ");
            int itemsLeft = itemsCount - i;
            int currentItemsCount =
                    itemsLeft > SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE ? SqlBuilder.MAX_ITEMS_FOR_IN_CLAUSE : itemsLeft;
            StringBuilder currentInClause = createSimpleInClauseString(currentItemsCount);
            complexInClause.append(currentInClause);
            complexInClause.append(" or ");
        }
        // delete last " or "
        complexInClause.delete(complexInClause.length() - 4, complexInClause.length());
        complexInClause.append(")");
        return complexInClause;
    }

    private void verifySqlBuilderParamsForInClause(int itemsCount, SqlBuilder builder) {
        List<Object> queryParams = builder.getQueryParams();
        Assert.assertEquals("should be " + itemsCount + " params", itemsCount, queryParams.size());
        for (int i = 0; i < itemsCount; i++) {
            Assert.assertEquals("should be item" + i, "item" + i, queryParams.get(i));
        }
    }

    @Test
    public void testConditionWithParams() {
        SqlBuilder builder = new SqlBuilder("table");
        builder.addCondition("id in (?,?)");
        ArrayList<Object> params = new ArrayList<>();
        params.add("id1");
        params.add("id2");
        builder.addQueryParams(params);
        String expected = "select * from table where id in (?,?)";
        Assert.assertEquals("wrong query", expected, builder.getSqlString());
        List<Object> queryParams = builder.getQueryParams();
        Assert.assertEquals("should be 2 params", 2, queryParams.size());
        Assert.assertEquals("should be id1", "id1", queryParams.get(0));
        Assert.assertEquals("should be id2", "id2", queryParams.get(1));
    }

    @Test
    public void testSuffixWithParam() {
        SqlBuilder builder = new SqlBuilder("table");
        builder.setSelection("id");
        builder.addQuerySuffixWithParam("group by id having count(*) = ?", 5);
        String expected = "select id from table group by id having count(*) = ?";
        Assert.assertEquals("wrong query", expected, builder.getSqlString());
        List<Object> queryParams = builder.getQueryParams();
        Assert.assertEquals("should be 1 param", 1, queryParams.size());
        Assert.assertEquals("should be 5", 5, queryParams.get(0));
    }

    @Test
    public void testComplexQuery() {
        SqlBuilder builder = new SqlBuilder("table");
        builder.addInCondition("f1", new ArrayList<String>() {{
            add("v1");
            add("v2");
            add("v3");
        }});
        builder.addCaseInsensitiveLikeCondition("f2", "v4");
        builder.addCaseInsensitiveLikeCondition("f3", "v5");
        builder.addInCondition("f4", new ArrayList<String>() {{
            add("v6");
            add("v7");
        }});
        String expected =
                "select * from table where f1 in (?,?,?) and lower(f2) like lower(?) and lower(f3) like lower(?) and f4 in (?,?)";
        Assert.assertEquals("wrong query", expected, builder.getSqlString());
        List<Object> queryParams = builder.getQueryParams();
        Assert.assertEquals("should be 7 params", 7, queryParams.size());
        Assert.assertEquals("should be v1", "v1", queryParams.get(0));
        Assert.assertEquals("should be v2", "v2", queryParams.get(1));
        Assert.assertEquals("should be v3", "v3", queryParams.get(2));
        Assert.assertEquals("should be v4", "%v4%", queryParams.get(3));
        Assert.assertEquals("should be v5", "%v5%", queryParams.get(4));
        Assert.assertEquals("should be v6", "v6", queryParams.get(5));
        Assert.assertEquals("should be v7", "v7", queryParams.get(6));
    }

    @Test
    public void testMultipleOrConditionOfKeyValuePairs() {
        SqlBuilder builder = new SqlBuilder("table");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("f1", "v1");
        attributes.put("f2", "v2");
        builder.addMultipleOrConditionOfKeyValuePairs("key", "value", attributes);
        String expected = "select * from table where ((key = ? and value = ?) or (key = ? and value = ?))";
        Assert.assertEquals("wrong query", expected, builder.getSqlString());
        List<Object> queryParams = builder.getQueryParams();
        Assert.assertEquals("should be 4 params", 4, queryParams.size());
        Assert.assertEquals("should be f1", "f1", queryParams.get(0));
        Assert.assertEquals("should be v1", "v1", queryParams.get(1));
        Assert.assertEquals("should be f2", "f2", queryParams.get(2));
        Assert.assertEquals("should be v2", "v2", queryParams.get(3));
    }

    @Test
    public void testOrderByClause() {
        SqlBuilder builder = new SqlBuilder("table");
        SqlQueryOrder sqlQueryOrder = new SqlQueryOrder("column", SqlQueryOrder.SortOrder.ASC);
        builder.addOrderByClause(sqlQueryOrder);
        String expected = "select * from table order by column ASC NULLS FIRST";
        Assert.assertEquals("wrong query", expected, builder.getSqlString());

        builder = new SqlBuilder("table");
        sqlQueryOrder.setSortOrder(SqlQueryOrder.SortOrder.DESC);
        builder.addOrderByClause(sqlQueryOrder);
        expected = "select * from table order by column DESC NULLS LAST";
        Assert.assertEquals("wrong query", expected, builder.getSqlString());
    }
}
