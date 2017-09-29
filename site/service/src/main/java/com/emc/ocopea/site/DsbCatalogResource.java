// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.site.dsb.Dsb;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class DsbCatalogResource implements DsbCatalogWebApi {

    private SiteRepository siteRepository;
    private WebAPIResolver webAPIResolver;

    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        siteRepository = context.getSingletonManager().getManagedResourceByName("site-singleton").getInstance();
        webAPIResolver = context.getWebAPIResolver();
    }

    @Override
    public Collection<SupportedServiceDto> getCatalog() {
        return siteRepository.load().getDsbList()
                .stream()
                .map(DsbCatalogResource::convertSupportedService)
                .collect(Collectors.toList());
    }

    static SupportedServiceDto convertSupportedService(Dsb currDsb) {
        return new SupportedServiceDto(
                currDsb.getUrn(),
                currDsb.getName(),
                currDsb.getType(),
                currDsb.getDescription(),
                currDsb.getPlans()
                        .stream()
                        .map(plan -> new SupportedServiceDto.SupportedServicePlanDto(
                                plan.getId(),
                                plan.getName(),
                                plan.getDescription(),
                                plan.getPrice(),
                                plan.getProtocols()
                                        .stream()
                                        .map(p -> new SupportedServiceDto.SupportedServiceProtocolDto(
                                                p.getProtocol(),
                                                p.getVersion(),
                                                p.getProperties()))
                                        .collect(Collectors.toList()),
                                plan.getDsbSettings()
                        ))
                        .collect(Collectors.toList()));
    }

    @Override
    public Collection<ServiceInstanceInfo> getInstancesByDsb() {
        return siteRepository.load().getDsbList()
                .stream()
                .flatMap(this::getInstancesStream)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ServiceInstanceInfo> getInstancesByDsb(@PathParam("dsbUrn") String dsbUrn) {
        final Dsb dsb = siteRepository.load().getDsb(dsbUrn);
        if (dsb == null) {
            throw new NotFoundException("DSB with urn " + dsbUrn + " not found");
        }
        return getInstancesStream(dsb).collect(Collectors.toList());

    }

    private Stream<? extends ServiceInstanceInfo> getInstancesStream(Dsb currDsb) {
        final DsbWebApi dsbConnection = webAPIResolver.getWebAPI(currDsb.getUrl(), DsbWebApi.class);
        if (dsbConnection == null) {
            throw new InternalServerErrorException("Failed connecting to DSB " + currDsb.getUrn());
        }
        return dsbConnection
                .getServiceInstances()
                .stream()
                .map(dsbInstanceDTO -> new ServiceInstanceInfo(dsbInstanceDTO.getInstanceId(), currDsb.getUrn(),
                        currDsb.getType(), null));
    }
}
