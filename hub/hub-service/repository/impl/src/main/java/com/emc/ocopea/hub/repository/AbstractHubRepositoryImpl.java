// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.ocopea.util.PostgresUtil;
import com.emc.ocopea.util.database.NativeQueryException;
import com.emc.ocopea.util.database.NativeQueryService;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

class AbstractHubRepositoryImpl {
    protected String getJsonBParam() {
        return "?";
    }

    protected <T> Object writeJsonBParameter(T o, NativeQueryService nqs) {
        return PostgresUtil.objectToJsonBParameter(o, nqs);
    }

    interface SQLExecutor {
        int execute() throws NativeQueryException;
    }

    protected int execWithRefinedDuplicateResourceException(SQLExecutor sqlExecutor) throws DuplicateResourceException {
        try {
            return sqlExecutor.execute();
        } catch (NativeQueryException nqe) {
            final SQLException sqlException = nqe.getSQLException();
            if (sqlException != null && sqlException.getSQLState() != null &&
                    sqlException.getSQLState().startsWith("23505")) {
                throw new DuplicateResourceException(nqe);
            } else {
                throw nqe;
            }
        }
    }

    //todo:move to some sort of dbutil...
    static UUID getUuid(ResultSet r, String fieldName) throws SQLException {
        String value = r.getString(fieldName);
        return r.wasNull() ? null : UUID.fromString(value);
    }

    static URI getUri(ResultSet r, String fieldName) throws SQLException {
        String value = r.getString(fieldName);
        return r.wasNull() ? null : URI.create(value);
    }

    static Date getDate(ResultSet r, String fieldName) throws SQLException {
        Long value = r.getLong(fieldName);
        return r.wasNull() ? null : new Date(value);
    }
}
