// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.psb.shpanpaas;

import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.ocopea.hub.ShpanPaaSResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by liebea on 2/27/17.
 * Drink responsibly
 */
@ServerEndpoint(value = "/ws/{appServiceId}")
public class ShpanPaaSWebSocket extends Endpoint {
    private static final Logger log = LoggerFactory.getLogger(ShpanPaaSWebSocket.class);

    @Override
    public void onOpen(
            Session session,
            EndpointConfig config) {

        log.info("onOpen sessionId {}", session.getId());
        final String appServiceId = session.getPathParameters().get("appServiceId");
        ((ShpanPaaSResourceProvider) ResourceProviderManager.getResourceProvider()).addLogAppender(
                session,
                appServiceId);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        log.info("session {} closed, {}", session.getId(), closeReason.getReasonPhrase());
        ((ShpanPaaSResourceProvider) ResourceProviderManager.getResourceProvider()).removeSession(session);
    }
}
