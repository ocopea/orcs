// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.Context;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.psb.PSBLogsWebSocketDTO;
import com.emc.ocopea.psb.PSBWebAPI;
import com.emc.ocopea.site.app.DeployedAppService;
import com.emc.ocopea.site.app.DeployedApplication;
import com.emc.ocopea.site.app.DeployedApplicationEventRepository;
import com.emc.ocopea.site.app.DeployedApplicationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class is responsible for managing logging WebSockets for Site.
 */
public class LoggingWebSocketsManager implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(LoggingWebSocketsManager.class);
    private WebAPIResolver webAPIResolver;
    private DeployedApplicationLoader deployedApplicationLoader;
    private ConcurrentMap<UUID, DeployedApplicationLogMessageSender> messageConsumersByAppInstanceId;

    @Override
    public void init(Context context) {
        webAPIResolver = context.getWebAPIResolver();
        deployedApplicationLoader = new DeployedApplicationLoader(
                context.getDynamicJavaServicesManager().getManagedResourceByName(
                        DeployedApplicationEventRepository.class.getSimpleName()).getInstance());
        messageConsumersByAppInstanceId = new ConcurrentHashMap<>();
    }

    void subscribe(UUID appInstanceId, Consumer<SiteLogMessageDTO> consumer) {
        messageConsumersByAppInstanceId.put(appInstanceId, new DeployedApplicationLogMessageSender(
                appInstanceId,
                consumer
        ));
    }

    /**
     * Stop receiving updates for an app instance
     */
    void unSubscribe(UUID appInstanceId) {

        final DeployedApplicationLogMessageSender messageTranslator =
                messageConsumersByAppInstanceId.get(appInstanceId);

        if (messageTranslator != null) {
            messageTranslator.stop();
        }
        messageConsumersByAppInstanceId.remove(appInstanceId);
    }

    /**
     * Publishes a message to the web socket. this method will not blocked and is thread safe
     */
    public void publish(UUID appInstanceId, SiteLogMessageDTO logMessage) {
        if (logMessage.getMessageType() == SiteLogMessageDTO.MessageType.err) {
            log.error("ws message: {}", logMessage.getMessage());
        } else {
            log.info("ws message: {}", logMessage.getMessage());
        }

        final DeployedApplicationLogMessageSender sender = messageConsumersByAppInstanceId.get(appInstanceId);

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
     * @return Map psbAppServiceId to WebSocket address
     */
    public Map<String, String> getAppInstanceLogsWebSockets(UUID appInstanceId) {
        DeployedApplication deployedApp = deployedApplicationLoader.load(appInstanceId);
        if (deployedApp == null) {
            throw new NotFoundException("failed to load application. appInstanceId=" + appInstanceId);
        }

        return deployedApp
                .getDeployedAppServices()
                .values()
                .stream()
                .collect(Collectors.toMap(
                        DeployedAppService::getPsbAppServiceId,
                        svc -> {
                            final PSBLogsWebSocketDTO appServiceLogsWebSocket = getPsbWebApi(svc.getPsbUrl())
                                    .getAppServiceLogsWebSocket(svc.getSpace(), svc.getPsbAppServiceId());
                            if (appServiceLogsWebSocket != null) {
                                return appServiceLogsWebSocket.getAddress();
                            } else {
                                return "";
                            }
                        })
                ).entrySet()
                .stream()
                .filter(stringStringEntry -> stringStringEntry.getValue().length() > 0)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    /***
     * Given an application instance id and application service id, retreives all the service's "tags" which can be
     * used to filter log messages.
     */
    public Set<String> getAppServiceTags(UUID appInstanceId, String appServiceId) {
        DeployedApplication deployedApp = deployedApplicationLoader.load(appInstanceId);
        if (deployedApp == null) {
            throw new NotFoundException("failed to load application. appInstanceId=" + appInstanceId);
        }

        return deployedApp
                .getDeployedAppServices()
                .values()
                .stream()
                .filter(Objects::nonNull)
                .filter(appService -> appService.getPsbAppServiceId().equals(appServiceId))
                .map(DeployedAppService::getAppServiceName)
                .collect(Collectors.toSet());
    }

    private PSBWebAPI getPsbWebApi(String psbUrl) {
        final PSBWebAPI psbConnection = webAPIResolver.getWebAPI(psbUrl, PSBWebAPI.class);
        if (psbConnection == null) {
            throw new InternalServerErrorException("Failed getting psb connection for psbUrn=" + psbUrl);
        }
        return psbConnection;
    }
}
