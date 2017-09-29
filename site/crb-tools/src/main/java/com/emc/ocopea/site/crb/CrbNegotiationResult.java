// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.crb;

import java.util.Map;

/**
 * Created by liebea on 2/5/17.
 * Drink responsibly
 */
public class CrbNegotiationResult {
    private final String protocolName;
    private final String protocolVersion;
    private final String crbUrn;
    private final String crbUrl;
    private final String repoId;
    private final Map<String, String> crbCredentials;

    public CrbNegotiationResult(
            String protocolName,
            String protocolVersion,
            String crbUrn,
            String crbUrl,
            String repoId,
            Map<String, String> crbCredentials) {
        this.protocolName = protocolName;
        this.protocolVersion = protocolVersion;
        this.crbUrn = crbUrn;
        this.crbUrl = crbUrl;
        this.repoId = repoId;
        this.crbCredentials = crbCredentials;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getCrbUrn() {
        return crbUrn;
    }

    public String getCrbUrl() {
        return crbUrl;
    }

    public String getRepoId() {
        return repoId;
    }

    public Map<String, String> getCrbCredentials() {
        return crbCredentials;
    }
}
