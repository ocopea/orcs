// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.testing;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;
import org.jboss.resteasy.spi.ResteasyUriInfo;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;

/**
 * Created by liebea on 7/6/2014. Enjoy it
 */
public class HttpRequestTestWrapperRestEasySpecific implements HttpRequest {
    private final InputStream requestInputStream;

    public HttpRequestTestWrapperRestEasySpecific(InputStream requestInputStream) {
        this.requestInputStream = requestInputStream;
    }

    @Override
    public InputStream getInputStream() {
        return requestInputStream;
    }

    @Override
    public HttpHeaders getHttpHeaders() {

        return new ResteasyHttpHeaders(new MultivaluedMapImpl<>());

    }

    @Override
    public MultivaluedMap<String, String> getMutableHeaders() {
        return new MultivaluedMapImpl<>();
    }

    @Override
    public void setInputStream(InputStream stream) {

    }

    @Override
    public ResteasyUriInfo getUri() {
        return new ResteasyUriInfo(URI.create("/"));
    }

    @Override
    public String getHttpMethod() {
        return null;
    }

    @Override
    public void setHttpMethod(String s) {

    }

    @Override
    public void setRequestUri(URI uri) throws IllegalStateException {

    }

    @Override
    public void setRequestUri(URI uri, URI uri1) throws IllegalStateException {

    }

    @Override
    public MultivaluedMap<String, String> getFormParameters() {
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getDecodedFormParameters() {
        return null;
    }

    @Override
    public Object getAttribute(String attribute) {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public ResteasyAsynchronousContext getAsyncContext() {
        return null;
    }

    @Override
    public boolean isInitial() {
        return false;
    }

    @Override
    public void forward(String s) {

    }

    @Override
    public boolean wasForwarded() {
        return false;
    }
}
