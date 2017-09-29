// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.standalone.web;

import com.emc.microservice.Context;
import com.emc.microservice.restapi.ManagedResourceDescriptor;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.ocopea.services.rest.APIMetaResource;
import com.emc.ocopea.services.rest.APIRootResource;
import com.emc.ocopea.services.rest.MicroServiceConfigurationResource;
import com.emc.ocopea.services.rest.MicroServiceExceptionMapper;
import com.emc.ocopea.services.rest.MicroServiceExecResource;
import com.emc.ocopea.services.rest.MicroServiceMetricsResource;
import com.emc.ocopea.services.rest.MicroServicePostRequestFilter;
import com.emc.ocopea.services.rest.MicroServicePreMatchingFilter;
import com.emc.ocopea.services.rest.MicroServiceRootApplication;
import com.emc.ocopea.services.rest.MicroServiceSerializationProvider;
import com.emc.ocopea.services.rest.MicroServiceStateResource;
import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.websockets.jsr.ServerEndpointConfigImpl;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServerBetter;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/5/2014. Enjoy it
 */
public class UndertowRestEasyWebServer implements MicroServiceWebServer {
    private final Logger log = LoggerFactory.getLogger(UndertowRestEasyWebServer.class);
    private final UndertowWebServerConfiguration webServerConfiguration;
    private final UndertowJaxrsServerBetter undertowRestServer;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private int port;

    public UndertowRestEasyWebServer(UndertowWebServerConfiguration webServerConfiguration) {
        this.webServerConfiguration = webServerConfiguration;
        //todo: set undertow threading configuration accordingly
        undertowRestServer = new UndertowJaxrsServerBetter();
        this.port = getWebServerPort(log);

    }

    @Override
    public void deployServiceApplication(Context context) {
        Logger logger = context.createSubLogger(UndertowRestEasyWebServer.class);

        if (System.getProperty("org.jboss.logging.provider") == null) {
            System.setProperty("org.jboss.logging.provider", "slf4j");
        }

        // Starting the micro web server
        startServer();

        String webRootPath = "/" + getWebRootPath(context);
        try {
            logger.debug("Starting micro web server for service " + context.getMicroServiceName() +
                    " on port " + port + " with root resource path " + webRootPath);
            ResteasyDeployment deployment = new ResteasyDeployment();
            Map<String, String> mediaTypeMappings = new HashMap<>();
            mediaTypeMappings.put("xml", "application/xml");
            mediaTypeMappings.put("json", "application/json");
            deployment.setMediaTypeMappings(mediaTypeMappings);
            deployment.setRegisterBuiltin(true);

            UndertowRestApplication application = new UndertowRestApplication(context);
            deployment.setApplication(application);

            List<Object> providers = getProviders(context, application);

            providers.addAll(
                    context.getRestResourceManager().getProviderDescriptorMap()
                            .values()
                            .stream()
                            .map(managedResourceDescriptor -> {
                                logger.info("Injecting rest provider " +
                                        managedResourceDescriptor.getResourceClass().getSimpleName());

                                try {
                                    return managedResourceDescriptor.getResourceClass().newInstance();
                                } catch (InstantiationException | IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }).collect(Collectors.toList())
            );

            deployment.setProviders(providers);

            DeploymentInfo deploymentInfo = undertowRestServer.undertowDeployment(deployment)
                    .setClassLoader(ClassLoader.getSystemClassLoader())
                    .setDeploymentName(context.getMicroServiceBaseURI())
                    .setContextPath(webRootPath);

            final Set<Class> webSocketClasses = context.getRestResourceManager().getWebSocketClasses();
            if (!webSocketClasses.isEmpty()) {
                final WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
                webSocketClasses.forEach(e -> {

                    // Supporting both annotated and non-annotated websocket implementations
                    if (!Endpoint.class.isAssignableFrom(e)) {
                        webSocketDeploymentInfo.addEndpoint(e);
                    } else {
                        final ServerEndpoint se = (ServerEndpoint) e.getAnnotation(ServerEndpoint.class);

                        // Adding ctx user property to store the ms context for each endpoint - enabling injections
                        final ServerEndpointConfigImpl endpoint = new ServerEndpointConfigImpl(
                                e,
                                se.value());
                        endpoint.getUserProperties().put("ctx", context);
                        webSocketDeploymentInfo.addEndpoint(
                                endpoint);
                    }
                });

                deploymentInfo
                        .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSocketDeploymentInfo);
            }

            // Ha ha
            deployment.setSecurityEnabled(false);

            undertowRestServer.deploy(deploymentInfo);

            // Registering default services
            deployment.getRegistry().addPerRequestResource(MicroServiceConfigurationResource.class);
            deployment.getRegistry().addPerRequestResource(MicroServiceStateResource.class);
            deployment.getRegistry().addPerRequestResource(APIMetaResource.class);
            deployment.getRegistry().addPerRequestResource(MicroServiceMetricsResource.class);
            deployment.getRegistry().addPerRequestResource(MicroServiceExecResource.class);

            for (ManagedResourceDescriptor currRestResource :
                    context.getRestResourceManager().getResourceDescriptorMap().values()) {
                logger.info("Starting rest resource " + currRestResource.getResourceClass().getSimpleName());
                deployment.getRegistry().addPerRequestResource(currRestResource.getResourceClass());
            }

            logger.info("micro web server for service " + context.getMicroServiceName() + " on port " + port +
                    " with root resource path " + webRootPath + " started successfully");
        } catch (Exception ex) {
            logger.error("Failed starting micro web server for service " + context.getMicroServiceName() +
                    " on port " + port + " with root resource path " + webRootPath + " started successfully", ex);
            throw ex;
        }
    }

    protected List<Object> getProviders(Context context, UndertowRestApplication application) {
        return new ArrayList<>(Arrays.asList(
                new MicroServicePostRequestFilter(application),
                new MicroServicePreMatchingFilter(application),
                new MicroServiceExceptionMapper(),
                new ResteasyJackson2Provider(),
                new MicroServiceSerializationProvider(context.getSerializationManager()),
                new LocationHeaderFilter()
        ));
    }

    protected String getWebRootPath(Context context) {
        String rootPath = "";
        String basePath = webServerConfiguration.getBasePath();
        if (basePath != null && !basePath.isEmpty()) {
            rootPath = basePath + "/";
        }
        return rootPath + context.getServiceDescriptor().getIdentifier().getRestURI();
    }

    //TODO: fix synchronization safety
    private void startServer() {
        log.info("Starting server");
        if (!started.get()) {
            undertowRestServer.start(
                    Undertow.builder()
                            .addHttpListener(port, "0.0.0.0")
            );

            deployUndertowRootDeployment();

            started.set(true);
        }
    }

    /**
     * Getting next available port after base port supplied by configuration
     *
     * @param logger logger to user for messages
     *
     * @return port use
     */
    private int getWebServerPort(Logger logger) {
        int port = webServerConfiguration.getPort();
        while (!available(port)) {
            logger.debug("Port {} is unavailable, trying next", port);
            port++;
        }

        logger.info("Found available port to use {}", port);
        return port;
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static boolean available(int port) {

        // Let's check if we we can create a server socket
        ServerSocket ss = null;
        DatagramSocket ds = null;
        boolean canOpenServerSocket = false;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            canOpenServerSocket = true;
        } catch (IOException ignored) {
            // Ignore, this is  
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                /* should not be thrown */
                }
            }
        }

        if (canOpenServerSocket) {
            // so far so good, but we need a further check as the above by itself does not appear to correctly identify
            // all scenarios (see DPA-40376) So, let's see if we can open a client socket on this port. If we can,
            // then it is clearly already in use and is hence unavailable
            boolean canOpenClientSocket = canOpenClientSocket(port);
            return !canOpenClientSocket;
        }

        return false;
    }

    /**
     * @param port port to use
     *
     * @return true if successful
     */
    private static boolean canOpenClientSocket(int port) {

        try (Socket ignored = new Socket("localhost", port)) {
            // we could successfully open a socket to an existing live connection on this port,
            // which means that this port is currently in use and is unavailable
            return true;
        } catch (Exception e) {
            // Exception caught, meaning that we couldn't open a connection,
            // inferring that the port is likely not in use
        }

        return false;
    }

    @Override
    public void unDeployServiceApplication(Context context) {
        ServletContainer servletContainer = undertowRestServer.getServletContainer();
        for (String deployment : servletContainer.listDeployments()) {
            servletContainer.getDeployment(deployment).undeploy();
        }
        undertowRestServer.stop();
    }

    @Override
    public Set<String> listDeploymentURNs() {
        return undertowRestServer.getServletContainer()
                .listDeployments()
                .stream()
                .map(n -> undertowRestServer
                        .getServletContainer()
                        .getDeployment(n)
                        .getDeployment()
                        .getDeploymentInfo()
                        .getContextPath())
                .collect(Collectors.toSet());
    }

    @Override
    public int getPort() {
        return port;
    }

    private void deployUndertowRootDeployment() {
        ResteasyDeployment deployment = new ResteasyDeployment();
        Map<String, String> mediaTypeMappings = new HashMap<>();
        mediaTypeMappings.put("xml", "application/xml");
        mediaTypeMappings.put("json", "application/json");
        deployment.setMediaTypeMappings(mediaTypeMappings);
        deployment.setRegisterBuiltin(true);

        Application application = new MicroServiceRootApplication(this);
        deployment.setApplication(application);

        List<Object> providers = new ArrayList<>(Arrays.asList(
                new MicroServiceExceptionMapper(),
                new ResteasyJackson2Provider()
        ));

        deployment.setProviders(providers);

        DeploymentInfo deploymentInfo = undertowRestServer.undertowDeployment(deployment)
                .setClassLoader(ClassLoader.getSystemClassLoader())
                .setDeploymentName("root")
                .setContextPath("/");

        deployment.setSecurityEnabled(false);
        undertowRestServer.deploy(deploymentInfo);
        deployment.getRegistry().addPerRequestResource(APIRootResource.class);

    }
}
