// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.MicroServiceApplication;
import com.emc.ocopea.util.MapBuilder;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProtectionWebResource implements ProtectionWebAPI {

    private AppCopyManager appCopyManager;
    private ApplicationCopyLoader applicationCopyLoader;

    @Context
    public void setApplication(Application application) {
        com.emc.microservice.Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        appCopyManager = context.getSingletonManager().getManagedResourceByName("app-copy-manager").getInstance();
        applicationCopyLoader = new ApplicationCopyLoader(context.getDynamicJavaServicesManager()
                .getManagedResourceByName(ApplicationCopyEventRepository.class.getSimpleName()).getInstance());
    }

    @Override
    public void protectApplication(ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo) {
        appCopyManager.schedule(protectApplicationInstanceInfo);
    }

    @Override
    public UUID createAppCopy(ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo) {
        return appCopyManager.createSingleCopy(protectApplicationInstanceInfo);
    }

    @Override
    public Collection<ProtectionAppCopyDTO> listAppInstanceCopies(@PathParam("appInstanceId") UUID appInstanceId) {
        return applicationCopyLoader
                .listByAppInstanceId(appInstanceId)
                .stream()
                .map(this::convertCopy)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProtectionAppCopyDTO> listAppInstanceCopies(
            @PathParam("appInstanceId") UUID appInstanceId,
            @QueryParam("intervalStart") @DefaultValue("-1") Long intervalStart,
            @QueryParam("intervalEnd") @DefaultValue("-1") Long intervalEnd) {
        try {
            return appCopyManager
                    .getAppInstanceCopies(appInstanceId, intervalStart, intervalEnd)
                    .stream()
                    .map(this::convertCopy)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage(), ex);
        }
    }

    private ProtectionAppCopyDTO convertCopy(ApplicationCopy applicationCopy) {
        final Map<String, Map<String, ProtectionAppCopyDTO.DataProtectionDataServiceCopyInfoDTO>> copies =
                new HashMap<>();
        for (ApplicationDataServiceCopy curr : applicationCopy.getDataServiceCopies()) {
            copies.computeIfAbsent(curr.getDsbUrn(),
                    k -> MapBuilder.<String, ProtectionAppCopyDTO.DataProtectionDataServiceCopyInfoDTO>newHashMap()
                            .with(curr.getBindName(),
                                    new ProtectionAppCopyDTO.DataProtectionDataServiceCopyInfoDTO(curr.getCopyRepoURN(),
                                            curr.getCopyId(), curr.getState()))
                            .build());
        }

        return new ProtectionAppCopyDTO(
                applicationCopy.getId(),
                applicationCopy.getAppInstanceId().toString(),
                applicationCopy.getTimeStamp(),
                applicationCopy.getState(),
                copies,
                applicationCopy.getAppServiceCopies().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        o -> new ProtectionAppCopyDTO.DataProtectionAppServiceCopyInfoDTO(
                                o.getValue().getAppImageName(),
                                o.getValue().getAppImageType(),
                                o.getValue().getAppImageVersion(),
                                o.getValue().getAppConfiguration(),
                                o.getValue().getStateTimestamp(),
                                o.getValue().getState()))));
    }

    @Override
    public ProtectionAppCopyDTO getCopy(@PathParam("copyId") UUID copyId) {

        final ApplicationCopy copy = applicationCopyLoader.load(copyId);
        if (copy == null) {
            throw new NotFoundException("Copy with id " + copyId.toString() + " not found");
        }
        return convertCopy(copy);
    }

}
