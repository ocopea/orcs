// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.ocopea.site.SiteLogMessageDTO;
import com.emc.ocopea.util.JsonUtil;
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
import java.util.UUID;
import java.util.function.Consumer;

@ClientEndpoint
public class HubLogsWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(HubLogsWebSocketClient.class);
    private final URI endpointUri;
    private final UUID siteId;
    private Consumer<SiteLogMessageDTO> consumer;
    private Session session;

    public HubLogsWebSocketClient(URI endpointUri, UUID siteId, Consumer<SiteLogMessageDTO> consumer)  {
        this.endpointUri = endpointUri;
        this.siteId = siteId;
        this.consumer = consumer;
    }

    public UUID getSiteId() {
        return siteId;
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
            SiteLogMessageDTO psbMessage = JsonUtil.fromJson(SiteLogMessageDTO.class, message);
            consumer.accept(psbMessage);
        } catch (Exception e) {
            log.warn("failed handling Site log message=" + message + " endpointUri=" + endpointUri, e);
        }
    }

}
