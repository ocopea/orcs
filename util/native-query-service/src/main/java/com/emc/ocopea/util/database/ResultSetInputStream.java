// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResultSetInputStream extends InputStream implements AutoCloseable {

    private final ResultSet resultSet;
    private byte[] buffer;
    private int position;

    public ResultSetInputStream(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public int read() throws IOException {
        try {
            if (buffer == null) {
                // first call of read method
                if (!resultSet.next()) {
                    return -1; // no rows - empty input stream
                } else {
                    buffer = convertRow();
                    position = 0;
                    return buffer[position++] & (0xff);
                }
            } else {
                // not first call of read method
                if (position < buffer.length) {
                    // buffer already has some data in, which hasn't been read yet - returning it
                    return buffer[position++] & (0xff);
                } else {
                    // all data from buffer was read - checking whether there is next row and re-filling buffer
                    if (!resultSet.next()) {
                        return -1; // the buffer was read to the end and there is no rows - end of input stream
                    } else {
                        // there is next row - converting it to byte array and re-filling buffer
                        buffer = convertRow();
                        position = 0;
                        return buffer[position++] & (0xff);
                    }
                }
            }
        } catch (final Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {

            final Statement statement = resultSet == null ? null : resultSet.getStatement();
            final Connection connection = statement == null ? null : statement.getConnection();

            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IOException("Unable to close ResultSetInputStream", e);
        }
    }

    private byte[] convertRow() throws IOException, SQLException {
        StringBuilder sb = new StringBuilder();

        // columnIndex - the first column is 1, the second is 2, ...
        int columnCount = resultSet.getMetaData().getColumnCount();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            if (columnIndex > 1) {
                sb.append(",");
            }
            sb.append(resultSet.getString(columnIndex));
        }
        sb.append(System.getProperty("line.separator"));

        return sb.toString().getBytes();
    }
}
