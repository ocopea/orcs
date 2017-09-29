// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.objectstore.pgsql;

import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.DuplicateObjectKeyException;
import com.emc.microservice.blobstore.IllegalStoreStateException;
import com.emc.microservice.blobstore.ManagedBlobStore;
import com.emc.microservice.blobstore.ObjectKeyFormatException;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Object Store REST API Class
 * Provides basic CRUD API for Object Store
 */
@Path("/")
public class ObjectStoreResource {

    private static final Logger log = LoggerFactory.getLogger(ObjectStoreResource.class);

    private BlobStoreAPI blobStore;

    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        MicroServiceApplication microServiceApplication = (MicroServiceApplication) application;
        ManagedBlobStore managedBlobStore =
                microServiceApplication.getMicroServiceContext().getBlobStoreManager().getManagedResources().get(0);
        blobStore = managedBlobStore.getBlobStoreAPI();
    }

    @NoJavadoc
    @POST
    @Path("{namespace}/{key}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createObjectWithHeadersFromQueryString(
            @PathParam("namespace") String namespace,
            @PathParam("key") String key,
            InputStream body,
            @Context UriInfo info) {
        Response result = null;
        try {
            blobStore.create(namespace, key, processQueryParameters(info.getQueryParameters()), body);
        } catch (ObjectKeyFormatException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.key.or.namespace.illegal.format",
                            "Illegal namespace or key format.",
                            null,
                            null))
                    .build();
            log.debug("Illegal key or namespace format.", e);
        } catch (DuplicateObjectKeyException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.with.same.key.and.namespace.exists",
                            "Object with the same namespace and key already exists.",
                            null,
                            null))
                    .build();
            log.error("Object with the namespace : '{}' and key : '{}' already exists.", namespace, key, e);
        } catch (IllegalStoreStateException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.system.error",
                            "System error whilst creating object.",
                            Arrays.toString(e.getStackTrace()),
                            null))
                    .build();
            log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
        }
        result = result != null ? result : Response.created(info.getBaseUri()).build();
        return result;

    }

    private Map<String, String> processQueryParameters(MultivaluedMap<String, String> queryParameters) {
        Map<String, String> result = null;
        if (queryParameters != null && queryParameters.size() > 0) {
            result = new HashMap<>();
            for (String key : queryParameters.keySet()) {
                result.put(key, queryParameters.getFirst(key));
            }
        }
        return result;
    }

    @NoJavadoc
    @PUT
    @Path("{namespace}/{key}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateObjectWithHeadersFromQueryString(
            @PathParam("namespace") String namespace,
            @PathParam("key") String key,
            InputStream body,
            @Context UriInfo info) {
        Response result = null;
        try {
            blobStore.update(namespace, key, processQueryParameters(info.getQueryParameters()), body);
        } catch (ObjectKeyFormatException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.key.or.namespace.illegal.format",
                            "Illegal namespace or key format.",
                            null,
                            null))
                    .build();
            log.debug("Illegal key or namespace format.", e);
        } catch (IllegalStoreStateException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.system.error",
                            "System error whilst creating object.",
                            Arrays.toString(e.getStackTrace()),
                            null))
                    .build();
            log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
        }
        result = result != null ? result : Response.ok(info.getBaseUri()).build();
        return result;

    }

    /**
     * Main issue with current implementation is rest easy - it dumps incoming stream directly to file system, then
     * let's process it possibly would be nice to experiment with: TODO http://commons.apache.org/proper/commons-fileupload/apidocs/org/apache/commons/fileupload/MultipartStream.html
     */
    //    @POST
    //    @Path("{namespace}/{key}")
    //    @Consumes(MediaType.MULTIPART_FORM_DATA)
    //    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
            @PathParam("namespace") String namespace,
            @PathParam("key") String key,
            MultipartInput input,
            @Context UriInfo info) throws Exception {
        Response result = null;
        String headers = URLDecoder.decode(input.getParts().get(0).getBodyAsString());
        InputStream blobInputStream = input.getParts().get(1).getBody(new GenericType<InputStream>() {
        });
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, String> headersMap =
                    objectMapper.readValue(headers, new TypeReference<HashMap<String, String>>() {
                    });
            blobStore.create(namespace, key, headersMap, blobInputStream);
        } catch (ObjectKeyFormatException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.key.or.namespace.illegal.format",
                            "Illegal namespace or key format.",
                            null,
                            null))
                    .build();
            log.debug("Illegal key or namespace format.", e);
        } catch (DuplicateObjectKeyException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.with.same.key.and.namespace.exists",
                            "Object with the same namespace and key already exists.",
                            null,
                            null))
                    .build();
            log.error("Object with the namespace : '{}' and key : '{}' already exists.", namespace, key, e);
        } catch (IllegalStoreStateException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.system.error",
                            "System error whilst creating object.",
                            Arrays.toString(e.getStackTrace()),
                            null))
                    .build();
            log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
        } catch (FileNotFoundException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.system.error",
                            "Error whilst receiving data from client.",
                            Arrays.toString(e.getStackTrace()),
                            null))
                    .build();
            log.error(
                    "Error whilst receiving data from client for object with namespace {} and key {}.",
                    namespace,
                    key,
                    e);
        } catch (IOException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.headers.format.error",
                            "Can't parse headers, possibly wrong JSON format.",
                            Arrays.toString(e.getStackTrace()),
                            null))
                    .build();
            log.error("Can't parse headers for object with namespace {} and key {}.", namespace, key, e);
        }
        result = result != null ? result : Response.created(info.getBaseUri()).build();
        return result;
    }

    @NoJavadoc
    @GET
    @Path("{namespace}/{key}/headers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response readHeaders(@PathParam("namespace") String namespace, @PathParam("key") String key) {

        Response result = null;
        Map<String, String> headers = null;
        try {
            headers = blobStore.readHeaders(namespace, key);
        } catch (ObjectKeyFormatException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.key.or.namespace.illegal.format",
                            "Illegal namespace or key format",
                            null,
                            null))
                    .build();
            log.error("Illegal key or namespace format.", e);
        } catch (IllegalStoreStateException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.system.error",
                            "System error whilst creating object.",
                            Arrays.toString(e.getStackTrace()),
                            null))
                    .build();
            log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
        }
        result = headers != null ? Response.ok(headers).build() : result;
        return result;
    }

    @NoJavadoc
    @GET
    @Path("{namespace}/{key}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response readBlob(@PathParam("namespace") final String namespace, @PathParam("key") final String key) {
        StreamingOutput so = output -> {
            try {
                blobStore.readBlob(namespace, key, output);
            } catch (ObjectKeyFormatException e) {
                log.error("Illegal key or namespace format.", e);
            } catch (IllegalStoreStateException e) {
                log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
            }
        };
        return Response.ok(so).build();
    }

    @NoJavadoc
    @DELETE
    @Path("{namespace}/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("namespace") String namespace, @PathParam("key") String key) {
        Response result = null;
        try {
            blobStore.delete(namespace, key);
        } catch (ObjectKeyFormatException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.key.or.namespace.illegal.format",
                            "Illegal namespace or key format",
                            null,
                            null))
                    .build();
            log.error("Illegal key or namespace format.", e);
        } catch (IllegalStoreStateException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.system.error",
                            "System error whilst deleting object.",
                            Arrays.toString(e.getStackTrace()),
                            null))
                    .build();
            log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
        }
        result = result != null ? result : Response.noContent().build();
        return result;
    }

    @HEAD
    @Path("{namespace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfo(@PathParam("namespace") String namespace) {
        Response result = Response.ok().build();
        return result;
    }

    @NoJavadoc
    @HEAD
    @Path("{namespace}/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isExists(@PathParam("namespace") String namespace, @PathParam("key") String key) {
        Response result = null;
        try {
            if (blobStore.isExists(namespace, key)) {
                result = Response.status(Response.Status.OK).build();
            } else {
                result = Response.status(Response.Status.GONE).build();
            }
        } catch (IllegalStoreStateException e) {
            result = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Message("object.store.object.system.error",
                            "System error whilst checking if object exists.",
                            Arrays.toString(e.getStackTrace()),
                            null))
                    .build();
            log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
        }
        return result;
    }

    static class Message {
        private String id;
        private String message;
        private String stackTrace;
        private Map<String, String> entries;

        public Message() {
        }

        public Message(String id, String message, String stackTrace, Map<String, String> entries) {
            this.id = id;
            this.message = message;
            this.stackTrace = stackTrace;
            this.entries = entries;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }

        public Map<String, String> getEntries() {
            return entries;
        }

        public void setEntries(Map<String, String> entries) {
            this.entries = entries;
        }
    }
}
