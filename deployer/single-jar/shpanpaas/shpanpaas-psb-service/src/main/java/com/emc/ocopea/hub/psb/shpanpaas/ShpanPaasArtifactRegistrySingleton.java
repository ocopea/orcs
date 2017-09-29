// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.psb.shpanpaas;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.hackathon.HackathonCommitteeMicroService;
import com.emc.ocopea.hackathon.HackathonSubmissionMicroService;
import com.emc.ocopea.util.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class ShpanPaasArtifactRegistrySingleton implements ServiceLifecycle {
    private final Map<String, Pair<MicroService, Collection<String>>> appVersions = new HashMap<>();

    @Override
    public void init(Context context) {
        add(new HackathonSubmissionMicroService(), Arrays.asList("1.0", "1.1", "1.2"));
        add(new HackathonCommitteeMicroService(), Arrays.asList("2.1", "2.2"));
    }

    @Override
    public void shutDown() {
    }

    public void add(MicroService service, Collection<String> versions) {
        appVersions.put(service.getIdentifier().getShortName(), new Pair<>(service, versions));
    }

    public Pair<MicroService, Collection<String>> getServiceVersions(String imageName) {
        return appVersions.get(imageName);
    }

}
