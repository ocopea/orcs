// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.h2;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.dsb.CopyServiceInstance;
import com.emc.ocopea.dsb.CopyServiceInstanceResponse;
import com.emc.ocopea.dsb.CreateServiceInstance;
import com.emc.ocopea.dsb.DsbInfo;
import com.emc.ocopea.dsb.DsbPlan;
import com.emc.ocopea.dsb.DsbSupportedCopyProtocol;
import com.emc.ocopea.dsb.DsbSupportedProtocol;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.dsb.ServiceInstance;
import com.emc.ocopea.dsb.ServiceInstanceDetails;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
public class H2DSBResource implements DsbWebApi {

    private static final String SLOW_PLAN_NAME = "slow";
    private static final String FAIL_CREATE_PLAN_NAME = "fail-create";
    private static final String FAIL_BIND_PLAN_NAME = "fail-bind";
    private DsbInfo dsbInfo;
    private H2DSBSingleton h2DSBSingleton;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        MicroService serviceDescriptor = context.getServiceDescriptor();
        final List<DsbSupportedProtocol> protocols = Arrays.asList(
                new DsbSupportedProtocol("postgres", null, null),
                new DsbSupportedProtocol("h2", null, null));
        final List<DsbSupportedCopyProtocol> copyProtocols = Collections.singletonList(
                new DsbSupportedCopyProtocol("shpanRest", null));

        dsbInfo = new DsbInfo(
                context.getServiceDescriptor().getIdentifier().getShortName(),
                "datasource",
                serviceDescriptor.getDescription(),
                Arrays.asList(
                        new DsbPlan(
                                "default",
                                "default",
                                "default",
                                "1$",
                                protocols,
                                copyProtocols,
                                Collections.emptyMap()),
                        new DsbPlan(
                                SLOW_PLAN_NAME,
                                SLOW_PLAN_NAME,
                                "Slow deployment",
                                null,
                                protocols,
                                copyProtocols,
                                Collections.emptyMap()),
                        new DsbPlan(
                                FAIL_CREATE_PLAN_NAME,
                                FAIL_CREATE_PLAN_NAME,
                                "Plan fails at create stage",
                                "10$",
                                protocols,
                                copyProtocols,
                                Collections.emptyMap()),
                        new DsbPlan(
                                FAIL_BIND_PLAN_NAME,
                                FAIL_BIND_PLAN_NAME,
                                "Plan fails at binding stage",
                                "20$",
                                protocols,
                                copyProtocols,
                                Collections.emptyMap())
                ));
        h2DSBSingleton = context.getSingletonManager().getManagedResourceByName("h2-dsb-singleton").getInstance();
    }

    private void sleepNoEx(int sec) {
        try {
            Thread.sleep(sec * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getPlanNameFromSettings(Map<String, String> dsbSettings) {
        String planName = "";
        if (dsbSettings != null) {
            final String s = dsbSettings.get("plan");
            if (s != null) {
                planName = s;
            }
        }
        return planName;

    }

    @Override
    public CopyServiceInstanceResponse copyServiceInstance(
            @PathParam("instanceId") String instanceId,
            CopyServiceInstance copyDetails) {
        h2DSBSingleton.copy(instanceId, copyDetails);

        return new CopyServiceInstanceResponse(0, "yey", copyDetails.getCopyId());
    }

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstance serviceSettings) {
        String planName = getPlanNameFromSettings(serviceSettings.getInstanceSettings());
        switch (planName) {
            case SLOW_PLAN_NAME:
                sleepNoEx(15);
                break;
            case FAIL_CREATE_PLAN_NAME:
                sleepNoEx(10);
                throw new InternalServerErrorException("Ha, I Fail creating on purpose! what you gonna do about it?");
            default:
                // default plan
        }

        if (serviceSettings.getRestoreInfo() == null) {
            h2DSBSingleton.create(serviceSettings.getInstanceId(), serviceSettings.getInstanceSettings());
        } else {
            h2DSBSingleton.createFromCopy(
                    serviceSettings.getInstanceId(),
                    serviceSettings.getRestoreInfo(),
                    serviceSettings.getInstanceSettings());
        }
        return new ServiceInstance(serviceSettings.getInstanceId());
    }

    @Override
    public ServiceInstance deleteServiceInstance(@PathParam("instanceId") String instanceId) {
        h2DSBSingleton.deleteDB(instanceId);
        return new ServiceInstance(instanceId);
    }

    @Override
    public Response getDSBIcon() {
        return Response.noContent().build();
    }

    @Override
    public DsbInfo getDSBInfo() {
        return dsbInfo;
    }

    @Override
    public ServiceInstanceDetails getServiceInstance(@PathParam("instanceId") String instanceId) {
        H2Instance h2Instance = h2DSBSingleton.get(instanceId);
        if (h2Instance == null) {
            throw new NotFoundException("instanceId " + instanceId + " does not exist");
        }

        String planName = getPlanNameFromSettings(h2Instance.getDsbSettings());
        switch (planName) {
            case SLOW_PLAN_NAME:
                sleepNoEx(15);
                break;
            case FAIL_BIND_PLAN_NAME:
                sleepNoEx(10);
                throw new InternalServerErrorException("Ha, I Fail binding on purpose! what you gonna do about it?");
            default:
                // default plan
        }

        Map<String, String> bind = new HashMap<>();
        bind.put("serviceType", ServiceRegistryApi.SERVICE_TYPE_DATASOURCE);
        bind.put("serviceName", instanceId);
        return new ServiceInstanceDetails(
                instanceId,
                bind,
                Collections.emptyList(),
                "EBS",
                h2Instance.getSize(),
                ServiceInstanceDetails.StateEnum.RUNNING
        );

    }

    @Override
    public List<ServiceInstance> getServiceInstances() {
        return h2DSBSingleton.list()
                .stream()
                .map(curr -> new ServiceInstance(curr.getName()))
                .collect(Collectors.toList());
    }

}
