// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.Context;
import com.emc.microservice.dependency.ManagedDependency;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.hub.HubWebAppUtil;
import com.emc.ocopea.hub.application.HubAppInstanceConfigurationDTO;
import com.emc.ocopea.hub.application.HubWebApi;
import com.emc.ocopea.hub.site.SiteDto;
import com.emc.ocopea.site.ServiceLogsWebSocketDTO;
import com.emc.ocopea.site.SiteWebApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class is responsible for managing logging WebSockets for Site.
 */
public class HubLoggingWebSocketsManager implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(HubLoggingWebSocketsManager.class);
    private WebAPIResolver webAPIResolver;
    private ManagedDependency hubServiceDependency;
    private ConcurrentMap<UUID, HubWebLogMessageSender> messageConsumersByAppInstanceId;

    @Override
    public void init(Context context) {
        webAPIResolver = context.getWebAPIResolver();
        hubServiceDependency = context.getDependencyManager().getManagedResourceByName("hub");
        messageConsumersByAppInstanceId = new ConcurrentHashMap<>();
    }

    void subscribe(UUID appInstanceId, Consumer<UILogMessage> consumer) {
        messageConsumersByAppInstanceId.put(appInstanceId, new HubWebLogMessageSender(
                appInstanceId,
                consumer
        ));
    }

    /**
     * Stop receiving updates for an app instance
     */
    void unSubscribe(UUID appInstanceId) {

        final HubWebLogMessageSender messageTranslator =
                messageConsumersByAppInstanceId.get(appInstanceId);

        if (messageTranslator != null) {
            messageTranslator.stop();
        }
        messageConsumersByAppInstanceId.remove(appInstanceId);
    }

    /**
     * Publishes a message to the web socket. this method will not blocked and is thread safe
     */
    public void publish(UUID appInstanceId, UILogMessage logMessage) {
        if (logMessage.getMessageType() == UILogMessage.MessageType.err) {
            log.error("ws message: {}", logMessage.getMessage());
        } else {
            log.info("ws message: {}", logMessage.getMessage());
        }

        final HubWebLogMessageSender sender = messageConsumersByAppInstanceId.get(appInstanceId);

        // If there is a sender for this app instance, sending it
        if (sender != null) {
            try {
                sender.sendMessage(logMessage);
            } catch (Exception ex) {
                log.warn("Failed publishing log message for appInstanceId " + appInstanceId + "; message\n " +
                        logMessage.toString(), ex);
            }
        }
    }

    @Override
    public void shutDown() {
    }

    /**
     * Given an application instance id, retrieves all websocket addresses for the application's services
     *
     * @return Map siteId to WebSocket address
     */
    public Map<UUID, String> getAppInstanceLogsWebSockets(UUID appInstanceId) {

        // Fetching app instance from hub
        HubAppInstanceConfigurationDTO appInstance = getAppInstanceFromHub(appInstanceId);

        // Loading site info
        SiteDto siteDto = getSite(appInstance.getSiteId());

        return getWebSocketsFromSite(appInstanceId, siteDto.getUrl())
                .stream()
                .map(socket -> {
                    UILogsWebSocketInfo.SerializationEnum serialization =
                            UILogsWebSocketInfo.SerializationEnum.fromValue(socket.getSerialization());
                    if (serialization == null) {
                        throw new IllegalArgumentException("illegal serialization=" + socket.getSerialization());
                    }
                    return new UILogsWebSocketInfo(socket.getAddress(), serialization, socket.getTags());
                })
                .collect(Collectors.toMap(
                        o -> appInstance.getSiteId(),
                        UILogsWebSocketInfo::getAddress,
                        (s, s2) -> s
                ));
    }

    private HubAppInstanceConfigurationDTO getAppInstanceFromHub(@PathParam("appInstanceId") UUID appInstanceId) {
        return HubWebAppUtil.wrapMandatory(
                "loading app with id " + appInstanceId + " from hub",
                () -> getHubApi().getAppInstance(appInstanceId));
    }

    private HubWebApi getHubApi() {
        return hubServiceDependency.getWebAPI(HubWebApi.class);
    }

    private SiteDto getSite(UUID siteId) {
        return HubWebAppUtil.wrapMandatory(
                "loading site info with id " + siteId + " from the hub",
                () -> getHubApi().getSite(siteId));
    }

    private List<ServiceLogsWebSocketDTO> getWebSocketsFromSite(UUID appInstanceId, String siteUrl) {
        return HubWebAppUtil.wrap(
                "fetching logging web sockets from site",
                () -> getSiteApi(siteUrl).getAppInstanceLogsWebSockets(appInstanceId));
    }

    private SiteWebApi getSiteApi(String siteUrl) {
        return webAPIResolver.getWebAPI(siteUrl, SiteWebApi.class);
    }

}
