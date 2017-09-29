// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.Context;
import com.emc.ocopea.psb.PSBLogMessageDTO;
import com.emc.ocopea.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/***
 * This class implements a WebSocket that emits logs for an entire application instance. It receives log messages via
 * multiple websockets (one per app service) and merges them to be emitted on a single websocket. Each log message is
 * enriched with metadata (appInstanceId and tags) that can be used for filtering.
 *
 * There was a question of weather it is safe to hold state on an instance of a ServerEndpoint (like we do here with
 * session, appInstanceID and serviceTags). From JSR 356 (WebSocket API):
 * Unless otherwise overridden by a developer provided configurator (see 3.1.7), the websocket implementation
 * must use one instance per application per VM of the Endpoint class to represent the logical endpoint
 * per connected peer. [WSC 2.1.1-1] Each instance of the Endpoint class in this typical case only handles
 * connections to the endpoint from one and only one peer.
 *
 * Based on this, we can safely hold state on the instance of a ServerEndpoint.
 */

@ServerEndpoint(value = "/site/app-instance/{appInstanceId}/logs")
public class SiteLogsWebSocket extends Endpoint implements Consumer<SiteLogMessageDTO> {
    private static final Logger log = LoggerFactory.getLogger(SiteLogsWebSocket.class);
    private LoggingWebSocketsManager loggingWebSocketsManager;

    private UUID appInstanceId;
    private Session session;
    private List<LogsWebSocketClient> webSocketClients;
    private Map<String, Set<String>> serviceTags = new HashMap<>(); // serviceId --> List<serviceTag>

    /***
     * Called when a client opens the logging websocket. It connects to PSB to get log messages from each service
     * of the application, enriches each message with more metadata (which app it belongs to, filtering tags) and
     * emits all messages on a single websocket.
     */
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        try {
            this.appInstanceId = UUID.fromString(session.getPathParameters().get("appInstanceId"));
            Context context = (Context) session.getUserProperties().get("ctx");
            log.info("log websocket opened for appInstanceId={}. session={}", appInstanceId, session.getId());

            loggingWebSocketsManager = context.getSingletonManager()
                    .getManagedResourceByName(LoggingWebSocketsManager.class.getSimpleName()).getInstance();
            loggingWebSocketsManager.subscribe(appInstanceId, this);
            try {
                webSocketClients = loggingWebSocketsManager
                        .getAppInstanceLogsWebSockets(appInstanceId)
                        .entrySet() // each entry is (serviceId, webSocketAddress)
                        .stream()
                        .map(websocketEntry ->
                                new LogsWebSocketClient(
                                        URI.create(websocketEntry.getValue()),
                                        websocketEntry.getKey(),
                                        this::emit
                                )
                        )
                        .collect(Collectors.toList());

                webSocketClients.forEach(client -> {
                    try {
                        client.connect();
                    } catch (Exception ex) {
                        log.warn("failed connecting to logging websocket psbAppServiceId={} endpointUri={}",
                                client.getServiceId(), client.getEndpointUri(), ex);
                        emit(new PSBLogMessageDTO(
                                "Failed attaching to application log stream of service " + client.getServiceId(),
                                System.currentTimeMillis(),
                                PSBLogMessageDTO.MessageType.err,
                                client.getServiceId()
                        ));
                    }
                });
            } catch (Exception ex) {
                log.warn("Failed opening remote sockets", ex);
            }

        } catch (Exception ex) {
            log.warn("Failed opening logs socket", ex);
            throw ex;
        }
    }

    /**
     * Enriches a PSB log message with metadata and emits it on the websocket
     */
    private void emit(PSBLogMessageDTO logMessage) {
        Set<String> tags = getAppServiceTags(logMessage.getServiceId());
        SiteLogMessageDTO message = convert(logMessage, tags);
        doSend(message);
    }

    /**
     * Synchronized to avoid collisions
     */
    private synchronized void doSend(SiteLogMessageDTO message) {
        try {
            if (session.isOpen()) {
                this.session.getBasicRemote().sendText(JsonUtil.toJson(message));
            }
        } catch (IOException e) {
            log.warn("failed sending forward message: " + message, e);
        }
    }

    private Set<String> getAppServiceTags(String serviceId) {
        return serviceTags.computeIfAbsent(
                serviceId,
                s -> loggingWebSocketsManager.getAppServiceTags(appInstanceId, s)
        );
    }

    private SiteLogMessageDTO convert(PSBLogMessageDTO psbLogMessage, Set<String> tags) {

        return new SiteLogMessageDTO(
                psbLogMessage.getMessage(),
                psbLogMessage.getTimestamp(),
                psbLogMessage.getMessageType() == PSBLogMessageDTO.MessageType.out ?
                        SiteLogMessageDTO.MessageType.out :
                        SiteLogMessageDTO.MessageType.err,
                tags
        );
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        log.info("disconnecting all websocket. clients for appInstanceId={} for reason \"{}\" and closing session={}",
                appInstanceId, closeReason == null ? "" : closeReason.getReasonPhrase(), session);

        // Removing subscription for event messages
        loggingWebSocketsManager.unSubscribe(appInstanceId);

        // Disconnecting all related web sockets
        webSocketClients.forEach(LogsWebSocketClient::disconnect);
    }

    @Override
    public void accept(SiteLogMessageDTO siteLogMessageDTO) {
        // Sending message to client as accepted via singleton
        doSend(siteLogMessageDTO);
    }
}
