// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import java.sql.SQLException;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA. User: liebea Date: 8/30/12 Time: 6:27 PM
 */
public class NativeQueryException extends RuntimeException {

    private String sql;
    private Collection params;

    public NativeQueryException(String message, String sql, Collection<Object> params, Throwable cause) {
        this(message + "sql=" + sql + "; params=" + params, cause);
        this.sql = sql;
        this.params = params;
    }

    public NativeQueryException(String message, String sql, Throwable cause) {
        this(message + "; sql = " + sql, cause);
        this.sql = sql;
    }

    public NativeQueryException(String message, Throwable cause) {
        super(stripSQLMessageMessage(message, cause), cause);
    }

    private static String stripSQLMessageMessage(String message, Throwable cause) {
        String finalMessage = message == null ? "" : message;
        if (cause != null && cause instanceof SQLException) {
            SQLException nextException = ((SQLException) cause).getNextException();
            if (nextException != null && nextException.getMessage() != null) {
                finalMessage += "; SQL Next Exception: " + nextException.getMessage();
            }
        }

        return finalMessage;
    }

    @Override
    public String toString() {
        String message = getLocalizedMessage();
        message = message != null ? message + "; " : "";
        message = message + "sql=" + sql;
        message = message + "; parameters=" + params;
        String s = getClass().getName();
        return s + ": " + message;
    }

    public SQLException getSQLException() {
        final Throwable cause = getCause();
        return cause instanceof SQLException ? (SQLException) cause : null;
    }
}
