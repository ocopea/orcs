// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.ocopea.psb.PSBLogMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class LogsWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(LogsWebSocketClient.class);
    private final URI endpointUri;
    private final String serviceId;
    private Consumer<PSBLogMessageDTO> consumer;
    private Session session;
    private static final ObjectMapper objMapper = new ObjectMapper();

    public LogsWebSocketClient(URI endpointUri, String serviceId, Consumer<PSBLogMessageDTO> consumer)  {
        this.endpointUri = endpointUri;
        this.serviceId = serviceId;
        this.consumer = consumer;
    }

    public String getServiceId() {
        return serviceId;
    }

    public URI getEndpointUri() {
        return endpointUri;
    }

    /***
     * Connect to a WebSocket endpoint and listen for messages
     */
    public void connect() throws IOException, DeploymentException {
        log.info("connecting to websocket endpointUri=" + endpointUri);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, endpointUri);
    }

    /***
     * Disconnect from a WebSocket endpoint
     */
    public void disconnect() {
        if (session != null) {
            log.info("disconnecting from websocket endpointUri={} session={}", endpointUri, session.getId());
            try {
                session.close();
            } catch (IOException e) {
                log.error("failed disconnecting from websocket endpointUri=" + endpointUri + " " +
                        "session=" + session.getId(), e);
            }
        }
    }

    /***
     * Deserialize incoming messages from PSB and consume them using consumer
     * @param message a JSON string representing a PSBLogMessageDTO
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("received message: " + message);
        try {
            PSBLogMessageDTO psbMessage = objMapper.readValue(message, PSBLogMessageDTO.class);
            consumer.accept(psbMessage);
        } catch (IOException e) {
            log.warn("failed handling PSB log message=" + message + " endpointUri=" + endpointUri, e);
        }
    }

}
