// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.discovery.ServiceDiscoveryManager;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.site.SiteCopyRepoInfoDTO;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.crb.CrbNegotiationResult;
import com.emc.ocopea.site.crb.CrbUtil;

import java.util.Optional;

/**
 * Created by liebea on 2/5/17.
 * Drink responsibly
 */
public class CopyRepositoryNegotiatorImpl implements CopyRepositoryNegotiator {
    private final ServiceDiscoveryManager serviceDiscoveryManager;
    private final WebAPIResolver webAPIResolver;

    public CopyRepositoryNegotiatorImpl(
            ServiceDiscoveryManager serviceDiscoveryManager,
            WebAPIResolver webAPIResolver) {
        this.serviceDiscoveryManager = serviceDiscoveryManager;
        this.webAPIResolver = webAPIResolver;
    }

    @Override
    public CrbNegotiationResult findCrb() {

        //todo: just taking the first now
        final Optional<SiteCopyRepoInfoDTO> firstCrb = serviceDiscoveryManager.discoverServiceConnection("site")
                .resolve(SiteWebApi.class)
                .listCopyRepositories()
                .stream()
                .findFirst();

        if (!firstCrb.isPresent()) {
            throw new IllegalStateException("Failed locating CRB. none available in site");
        } else {
            return CrbUtil.getCrbNegotiationResult(webAPIResolver, firstCrb.get().getUrn(), firstCrb.get().getUrl());
        }
    }

}
