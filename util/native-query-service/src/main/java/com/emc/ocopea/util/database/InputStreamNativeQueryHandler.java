// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by martiv6 on 18/06/2014.
 */
public class InputStreamNativeQueryHandler implements NativeQueryHandler<Void> {
    private final AbstractNativeQueryService.NativeQueryReader reader;

    public InputStreamNativeQueryHandler(AbstractNativeQueryService.NativeQueryReader reader) {
        this.reader = reader;
    }

    @Override
    public Void handleNativeQueryResult(ResultSet resultSet) throws SQLException, NativeQueryException {
        try (InputStream inputStream = new ResultSetInputStream(resultSet)) {
            reader.read(inputStream);
            return null;
        } catch (IOException e) {
            throw new NativeQueryException("Failed Reading stream", e);
        }
    }
}
