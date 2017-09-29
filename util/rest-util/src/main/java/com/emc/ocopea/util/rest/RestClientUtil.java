// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by liebea on 3/27/17.
 * Drink responsibly
 */
public abstract class RestClientUtil {

    private RestClientUtil() {
        // static util class
    }

    public static ResteasyWebTarget getWebTarget(String url) {
        return getWebTarget(url, true, null);
    }

    public static ResteasyWebTarget getWebTarget(String url, boolean sslVerify) {
        return getWebTarget(url, sslVerify,null);
    }

    public static ResteasyWebTarget getWebTarget(String url, BasicAuthentication authentication) {
        return getWebTarget(url, true, authentication);
    }

    /**
     * Return Insecure webtarget connection ignoring SSL  
     */
    public static ResteasyWebTarget getWebTarget(String url, boolean sslVerify, BasicAuthentication authentication) {

        HttpClientBuilder b = HttpClientBuilder.create();
        PoolingHttpClientConnectionManager cm;

        if (sslVerify) {
            cm = new PoolingHttpClientConnectionManager();
        } else {
            // setup a Trust Strategy that allows all certificates.
            //
            SSLContext sslContext;
            try {
                sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                throw new IllegalStateException(e);
            }
            b.setSslcontext(sslContext);

            // don't check Hostnames, either.
            // use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
            X509HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

            // here's the special part:
            // need to create an SSL Socket Factory, to use our weakened "trust strategy";
            // and create a Registry, to register it.
            //
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();
            cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        }

        b.setConnectionManager(cm);
        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(b.build(), true);
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(engine).build();
        final ResteasyWebTarget target = client.target(url);
        if (authentication != null) {
            target.register(authentication);
        }
        return target;
    }
}
