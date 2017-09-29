// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */
package com.emc.microservice.metrics.reporter.listener;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author shresa
 */
public class Listener {

    private final URI reportUri;
    //private final HttpClient httpClient;

    public Listener(URL dpaUrl) throws IOException {
        try {
            if (dpaUrl.getPath().endsWith("/report")) {
                this.reportUri = dpaUrl.toURI();
            } else {
                // till we have a proper registration process
                // if the dpa url isn't complete url we will default to using
                // local host name; alternatively server needs to be updated
                // to work with hard coded URL
                this.reportUri = new URL(dpaUrl, "/dpa-api/agent/"
                        + getHostName() + "/report").toURI();
            }

            //  httpClient = getHttpClient(reportUri, "emc.dpa.metrics.logon", "4BtByG4rTnNcQpZr!");
        } catch (URISyntaxException err) {
            throw new IOException("Unable to setup listener reporter due using URL " + dpaUrl, err);
        }
    }

    public void send(String hostname, byte[] data) throws IOException {
    }

    @Override
    public String toString() {
        return "Listener at " + reportUri;
    }

    private String getHostName() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName().toLowerCase();
    }

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(final java.security.cert.X509Certificate[] arg0, final String arg1)
                throws java.security.cert.CertificateException {
        }

        @Override
        public void checkServerTrusted(final java.security.cert.X509Certificate[] arg0, final String arg1)
                throws java.security.cert.CertificateException {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
