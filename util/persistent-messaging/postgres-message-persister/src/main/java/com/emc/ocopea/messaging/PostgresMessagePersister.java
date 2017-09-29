// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import com.emc.ocopea.util.PostgresUtil;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import com.emc.ocopea.util.io.StreamUtil;
import com.emc.ocopea.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PostgresMessagePersister implements MessagePersister {
    private static final Logger log = LoggerFactory.getLogger(PostgresMessagePersister.class);
    private final DataSource dataSource;
    private final NativeQueryService nqs;
    private final String messagingTableName;
    private static final String NAZ_MESSAGING_TABLE_NAME = "nazMessages";

    public PostgresMessagePersister(DataSource dataSource, String messagingSchemaName) {
        messagingSchemaName = PostgresUtil.sanitizeIdentifier(messagingSchemaName);
        this.dataSource = dataSource;
        this.nqs = new BasicNativeQueryService(dataSource);
        this.messagingTableName = messagingSchemaName + "." + NAZ_MESSAGING_TABLE_NAME;

        synchronized (PostgresMessagePersister.class) {

            if (!PostgresUtil.isSchemaExists(dataSource, messagingSchemaName)) {
                throw new IllegalStateException("schema " + messagingSchemaName + " does not exist");
            }

            if (!PostgresUtil.isTableExists(dataSource, messagingSchemaName, NAZ_MESSAGING_TABLE_NAME)) {
                log.info("creating database table " + messagingSchemaName + "." + NAZ_MESSAGING_TABLE_NAME +
                        " for persistent messaging messages");
                nqs.executeUpdate(
                        "CREATE TABLE " + messagingTableName + " (" +
                                "key UUID NOT NULL," +
                                "queueName VARCHAR(1024) NOT NULL," +
                                "time BIGINT NOT NULL," +
                                "headers JSONB NOT NULL," +
                                "data OID NOT NULL, " +
                                "CONSTRAINT pk_messages_id PRIMARY KEY (key)" +
                                ")");

                nqs.executeUpdate(
                        "CREATE INDEX idx_messages_queueName_Time on " + messagingTableName + "(queueName, time)");
            }
        }
    }

    @Override
    public Message persistMessage(
            String queueName, InputStream messageBody, Map<String, String> headers) {
        return persistMessage(queueName, out -> StreamUtil.copy(messageBody, out), headers);
    }

    @Override
    public Message persistMessage(String queueName, Consumer<OutputStream> writer, Map<String, String> headers) {
        try {

            // Storing the blob in postgres
            long oid = PostgresUtil.storeBlob(dataSource, writer);

            // Generating random UUID key
            final UUID key = UUID.randomUUID();

            // Headers must not be null
            Map<String, String> headersToStore = headers != null ? headers : Collections.emptyMap();

            // Inserting into our messages table
            nqs.executeUpdate(
                    "insert into " + messagingTableName + " (key,queueName,time,headers,data) values (?,?,?,?,?)",
                    Arrays.asList(
                            key,
                            queueName,
                            System.currentTimeMillis(),
                            PostgresUtil.getJsonB(JsonUtil.toJson(headersToStore)),
                            oid
                    ));
            return new PostgresMessage(key, oid, headers);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed persisting message on queue " + queueName, ex);
        }
    }

    @Override
    public List<Message> loadNextMessages(
            String queueName, int maxCount, Set<String> skipMessageIds) {
        return nqs.getList(
                "select * " +
                        "from " + messagingTableName + " " +
                        "where queueName=? " +
                        "order by time asc " +
                        "limit " + maxCount,
                (rset, pos) -> {
                    try (Reader data = rset.getCharacterStream("headers")) {
                        return new PostgresMessage(
                                UUID.fromString(rset.getString("key")),
                                rset.getLong("data"),
                                JsonUtil.readMap(data)
                        );
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed reading message headers for queue " + queueName, e);
                    }
                },
                Collections.singletonList(queueName)
        );
    }

    @Override
    public void deleteMessage(String queueName, String messageId) {
        nqs.executeUpdate(
                "delete from " + messagingTableName + " where key=? and queueName=?",
                Arrays.asList(
                        UUID.fromString(messageId),
                        queueName));
    }

    private class PostgresMessage implements Message {
        private final UUID key;
        private final long oid;
        private final Map<String, String> headers;

        private PostgresMessage(UUID key, long oid, Map<String, String> headers) {
            this.key = key;
            this.oid = oid;
            this.headers = headers;
        }

        @Override
        public String getId() {
            return key.toString();
        }

        @Override
        public Map<String, String> getHeaders() {
            return headers;
        }

        @Override
        public void readMessage(Consumer<InputStream> messageConsumer) {
            PostgresUtil.readBlob(PostgresMessagePersister.this.dataSource, this.oid, messageConsumer);
        }
    }
}
