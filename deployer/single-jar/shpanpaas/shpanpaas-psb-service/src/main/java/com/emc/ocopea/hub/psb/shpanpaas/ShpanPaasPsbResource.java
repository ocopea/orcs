// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.psb.shpanpaas;

import com.emc.microservice.Context;
import com.emc.microservice.ContextThreadLocal;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceApplication;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.psb.DeployAppServiceManifestDTO;
import com.emc.ocopea.psb.DeployAppServiceResponseDTO;
import com.emc.ocopea.psb.PSBAppServiceInstanceDTO;
import com.emc.ocopea.psb.PSBAppServiceStatusEnumDTO;
import com.emc.ocopea.psb.PSBInfoDTO;
import com.emc.ocopea.psb.PSBLogsWebSocketDTO;
import com.emc.ocopea.psb.PSBServiceBindingInfoDTO;
import com.emc.ocopea.psb.PSBSpaceDTO;
import com.emc.ocopea.psb.PSBWebAPI;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
public class ShpanPaasPsbResource implements PSBWebAPI {

    private PSBInfoDTO psbInfo;
    private ApplicationServiceManager applicationServiceManager;
    private String publicURL;
    private ShpanPaasArtifactRegistrySingleton artifactRegistry;
    @javax.ws.rs.core.Context
    private UriInfo uriInfo;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        MicroService serviceDescriptor = context.getServiceDescriptor();
        psbInfo = new PSBInfoDTO(
                serviceDescriptor.getName(),
                Integer.toString(serviceDescriptor.getVersion()),
                "ShpanPaaS",
                serviceDescriptor.getDescription(),
                55);

        applicationServiceManager = context.getSingletonManager().getManagedResourceByName(
                ApplicationServiceManager.class.getSimpleName()).getInstance();

        String publicLB = context.getParametersBag().getString("public-load-balancer");
        if (publicLB != null && !publicLB.isEmpty()) {
            publicURL = publicLB;
        } else {
            //todo:not like this...
            publicURL = context.getServiceDiscoveryManager()
                    .discoverService(ShpanPaasPsbMicroService.SERVICE_BASE_URI).getServiceURL();
            publicURL = publicURL.substring(0, publicURL.lastIndexOf("/"));
        }
        artifactRegistry = context.getSingletonManager().getManagedResourceByName(
                ShpanPaasArtifactRegistrySingleton.class.getSimpleName()).getInstance();

    }

    @Override
    public PSBInfoDTO getPSBInfo() {
        return psbInfo;
    }

    @Override
    public PSBAppServiceInstanceDTO getAppService(
            @PathParam("space") String space,
            @PathParam("appServiceId") String appServiceId) {

        ApplicationInstance instance = applicationServiceManager.getInstance(appServiceId);
        if (instance == null) {
            throw new NotFoundException();
        }
        return convertAppInstanceInfo(instance);
    }

    @Override
    public PSBLogsWebSocketDTO getAppServiceLogsWebSocket(
            @PathParam("space") String space,
            @PathParam("appServiceId") String appServiceId) {
        return new PSBLogsWebSocketDTO(
                uriInfo.getBaseUriBuilder()
                        .scheme("ws")
                        .segment("ws", appServiceId)
                        .build()
                        .toString(),
                "json");

    }

    @Override
    public DeployAppServiceResponseDTO deployApplicationService(final DeployAppServiceManifestDTO appServiceManifest) {
        final Context c = ContextThreadLocal.getContext();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ContextThreadLocal.setContext(c);
                //todo:amit:the ugliest thing ever. deploying app destroys web
                // server context not allowing nice return so sleeping :(
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<ServiceMapping> serviceMapping = Collections.emptyList();
                Map<String, Collection<PSBServiceBindingInfoDTO>> serviceBindings =
                        appServiceManifest.getServiceBindings();
                if (serviceBindings != null && !serviceBindings.isEmpty()) {
                    serviceMapping = new ArrayList<>(serviceBindings.size());

                    for (Map.Entry<String, Collection<PSBServiceBindingInfoDTO>> currDSBBind :
                            serviceBindings.entrySet()) {

                        for (PSBServiceBindingInfoDTO currBind : currDSBBind.getValue()) {
                            String serviceType = Objects.requireNonNull(
                                    currBind.getBindInfo().get("serviceType"),
                                    "serviceType missing in bind info");

                            String serviceName = Objects.requireNonNull(
                                    currBind.getBindInfo().get("serviceName"),
                                    "serviceName missing in bind info");

                            serviceMapping.add(new ServiceMapping(serviceType, currBind.getServiceName(), serviceName));
                        }
                    }

                }

                // Getting the image from artifact registry:
                final MicroService svc =
                        artifactRegistry.getServiceVersions(appServiceManifest.getImageName()).getObject1();
                if (svc == null) {
                    throw new NotFoundException("could not locate image " +
                            appServiceManifest.getImageName() + " in artifact registry");
                }

                // Loading service into jvm
                applicationServiceManager.createTemplate(svc);

                applicationServiceManager.runInstance(
                        appServiceManifest.getImageName(),
                        appServiceManifest.getAppServiceId(),
                        appServiceManifest.getAppServiceId(),
                        serviceMapping,
                        appServiceManifest.getImageVersion(),
                        appServiceManifest.getEnvironmentVariables()
                );
            }
        }, 4000L);

        //todo:error handling
        return new DeployAppServiceResponseDTO(0, "Yey!");
    }

    @Override
    public DeployAppServiceResponseDTO stopApp(
            @PathParam("space") String space,
            @PathParam("appServiceId") String appServiceId) {

        ApplicationInstance instance = applicationServiceManager.getInstance(appServiceId);
        if (instance == null) {
            throw new NotFoundException();
        }
        applicationServiceManager.stopApp(appServiceId);
        return new DeployAppServiceResponseDTO(0, "died, bye");
    }

    @Override
    public List<PSBSpaceDTO> listSpaces() {
        return Collections.singletonList(new PSBSpaceDTO("Shpan Space", Collections.emptyMap()));
    }

    private PSBAppServiceInstanceDTO convertAppInstanceInfo(ApplicationInstance instance) {
        return new PSBAppServiceInstanceDTO(
                instance.getBaseURI(),
                PSBAppServiceStatusEnumDTO.running,
                "Yey",
                1,
                Collections.emptyMap(),
                publicURL + "/" + instance.getBaseURI());
    }
}
