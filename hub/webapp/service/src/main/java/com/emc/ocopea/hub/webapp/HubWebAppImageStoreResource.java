// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.blobstore.BlobStoreAPI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liebea on 1/16/17.
 * Drink responsibly
 */
public class HubWebAppImageStoreResource implements HubWebAppImageStoreWebApi {
    private BlobStoreAPI iconStore;

    //todo:find a collection that holds maximum number of elements or bloom filter like thingie...
    private static Map<String, Boolean> existsCache = new HashMap<>();

    @javax.ws.rs.core.Context
    private UriInfo uriInfo;
    @javax.ws.rs.core.Context
    private HttpServletRequest servletRequest;

    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        iconStore = context.getBlobStoreManager().getManagedResourceByName("image-store").getBlobStoreAPI();
    }

    @Override
    public Response getImage(@PathParam("iconType") String iconType, @PathParam("iconId") String iconId) {
        if (isExists(iconType, iconId)) {
            return Response.ok((StreamingOutput) output -> iconStore.readBlob(iconType, iconId, output)).build();
        } else {
            return Response.noContent().build();
        }
    }

    @Override
    public void uploadIcon(
            InputStream inputStream,
            @PathParam("iconType") String iconType,
            @PathParam("iconId") String iconId) {
        iconStore.create(iconType, iconId, Collections.emptyMap(), inputStream);
        servletRequest.setAttribute("NAZGUL-LOCATION-HEADER-FILTER", uriInfo.getRequestUri());
    }

    private boolean isExists(String iconType, String iconId) {
        final String key = iconType + "|" + iconId;
        Boolean e = existsCache.get(key);
        if (e == null) {
            e = iconStore.isExists(iconType, iconId);
            if (e) {
                existsCache.put(key, e);
            }
        }
        return e;
    }
}
