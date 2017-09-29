// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.webclient;

import javax.ws.rs.client.WebTarget;

/**
 * Created by liebea on 5/3/16.
 * Drink responsibly
 */
public interface WebAPIResolver {

    WebAPIResolver buildResolver(WebApiResolverBuilder builder);

    <T> T getWebAPI(String url, Class<T> resourceWebAPI);

    WebTarget getWebTarget(String url);
}
