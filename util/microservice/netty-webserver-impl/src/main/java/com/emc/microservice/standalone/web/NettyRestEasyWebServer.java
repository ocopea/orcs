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
import com.emc.ocopea.services.rest.MicroServiceSerializationProvider;
import com.emc.ocopea.services.rest.MicroServiceStateResource;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/5/2014. Enjoy it
 */
public class NettyRestEasyWebServer implements MicroServiceWebServer {
    protected final RestEasyWebServerConfiguration webServerConfiguration;
    ResteasyDeployment deployment = null;
    private int port;
    private boolean doScanning = false; // by default - use provided port without scanning
    private String webRootPath = null;

    public NettyRestEasyWebServer(RestEasyWebServerConfiguration webServerConfiguration) {
        this.webServerConfiguration = webServerConfiguration;
    }

    @Override
    public void deployServiceApplication(Context context) {
        Logger logger = context.createSubLogger(NettyRestEasyWebServer.class);
        this.port = getWebServerPort(logger);
        webRootPath = context.getServiceDescriptor().getIdentifier().getRestURI();
        try {
            logger.debug("Starting micro web server for service " + context.getMicroServiceName() +
                    " on port " + port + " with root resource path " + webRootPath);
            deployment = new ResteasyDeployment();
            Map<String, String> mediaTypeMappings = new HashMap<>();
            mediaTypeMappings.put("xml", "application/xml");
            mediaTypeMappings.put("json", "application/json");
            deployment.setMediaTypeMappings(mediaTypeMappings);
            deployment.setRegisterBuiltin(true);

            //todo: set netty threading configuration accordingly
            NettyJaxrsServer netty = new NettyJaxrsServer();
            netty.setDeployment(deployment);
            netty.setPort(port);
            netty.setRootResourcePath("/");
            netty.setSecurityDomain(null);
            netty.setMaxRequestSize(1024 * 1024 * 1024);

            StandaloneRestApplication application = new StandaloneRestApplication(context);
            deployment.setApplication(application);

            List<Object> providers = new ArrayList<>(Arrays.asList(
                    new RestEasyRequestFilter(application),
                    new MicroServiceExceptionMapper(),
                    new ResteasyJacksonProvider(),
                    new MicroServiceSerializationProvider(context.getSerializationManager())

            ));

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

            //deployment.setActualProviderClasses(Arrays.<Class>asList(ResteasyJacksonProvider.class));

            // Starting the micro web server
            netty.start();

            // Registering default services
            deployment.getRegistry().addPerRequestResource(MicroServiceConfigurationResource.class, webRootPath);
            deployment.getRegistry().addPerRequestResource(MicroServiceStateResource.class, webRootPath);
            deployment.getRegistry().addPerRequestResource(APIMetaResource.class, webRootPath);
            deployment.getRegistry().addPerRequestResource(MicroServiceMetricsResource.class, webRootPath);
            deployment.getRegistry().addPerRequestResource(MicroServiceExecResource.class, webRootPath);
            deployment.getRegistry().addPerRequestResource(APIRootResource.class, webRootPath);

            //todo: register metrics (make it a jax-rs resource?)

            for (ManagedResourceDescriptor currRestResource :
                    context.getRestResourceManager().getResourceDescriptorMap().values()) {
                logger.info("Starting rest resource " + currRestResource.getResourceClass().getSimpleName());
                deployment.getRegistry().addPerRequestResource(currRestResource.getResourceClass(), webRootPath);
            }

            logger.info("micro web server for service " + context.getMicroServiceName() + " on port " + port +
                    " with root resource path " + webRootPath + " started successfully");
        } catch (Exception ex) {
            logger.error("Failed starting micro web server for service " + context.getMicroServiceName() +
                    " on port " + port + " with root resource path " + webRootPath + " started successfully", ex);
            throw ex;
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
        if (this.doScanning) {
            while (!available(port)) {
                logger.debug("Port {} is unavailable, trying next", port);
                port++;
            }
        } else {
            if (!available(port)) {
                throw new IllegalStateException(String.format("Requested port %d is not available", port));
            }
        }

        logger.info("Using available port: " + port);
        return port;
    }

    /**
     * Specifying if the given port should be used, or allow to scan next ports until available one
     *
     * @param doScanning if true - search for available port
     */
    public void setScanPortMode(boolean doScanning) {
        this.doScanning = doScanning;
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
            // Ignore
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
        if (deployment != null) {
            deployment.stop();
        }

    }

    @Override
    public Set<String> listDeploymentURNs() {
        if (webRootPath == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(webRootPath);
        }
    }

    @Override
    public int getPort() {
        return port;
    }
}
