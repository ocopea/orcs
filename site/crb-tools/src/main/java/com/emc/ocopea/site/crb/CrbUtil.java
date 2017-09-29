// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.crb;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CrbWebApi;
import com.emc.ocopea.crb.RepositoryInstance;
import com.emc.ocopea.util.MapBuilder;

import java.util.List;
import java.util.Map;

/**
 * CRB utilities for reuse accross the site repository.
 */
public class CrbUtil {

    /***
     * Negotiates which CRB protocol and connectivity details to use.
     * @param webAPIResolver service discovery manager
     * @param crbUrn the selected crb urn
     * @param crbUrl the selected crb url
     * @return CFB protocol and credentials
     */
    public static CrbNegotiationResult getCrbNegotiationResult(
            WebAPIResolver webAPIResolver,
            String crbUrn,
            String crbUrl) {

        // todo: properly get crb credentials from crb

        List<RepositoryInstance> repositories = webAPIResolver
                .getWebAPI(crbUrl, CrbWebApi.class)
                .listRepositoryInstances();
        if (repositories.isEmpty()) {
            throw new IllegalStateException("no repositories defined for CRB " + crbUrn);
        } else {
            // todo: figure out which repo we should actually use
            final String repoId = repositories.get(0).getRepoId();
            Map<String, String> crCredentials =
                    MapBuilder.<String, String>newHashMap()
                            .with("url", crbUrl)
                            .with("repoId", repoId)
                            .build();

            // todo: once we add CRB Protocol, get the actual protocol from the CRB's info
            return new CrbNegotiationResult("shpanRest", "1.0", crbUrn, crbUrl, repoId, crCredentials);
        }
    }

}
