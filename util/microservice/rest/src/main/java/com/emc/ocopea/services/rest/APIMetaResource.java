// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.ocopea.services.rest;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceApplication;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.io.StreamUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.ResourceInvoker;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ResourceMethodRegistry;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author shresa
 */
@Path(APIMetaResource.BASE_URI)
public class APIMetaResource {

    public static final String BASE_URI = "/meta";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoJavadoc
    public Response getServiceConfiguration(@Context Dispatcher dispatcher, @Context Application application) {
        StreamingOutput stream = null;
        if (MicroServiceApplication.class.isAssignableFrom(application.getClass())) {
            final Class<? extends MicroService> msClass =
                    ((MicroServiceApplication) application).getMicroServiceContext().getServiceDescriptor().getClass();
            if (msClass.getResource("swagger.yaml") != null) {
                stream = output -> {
                    try (final InputStream resourceAsStream = msClass.getResourceAsStream("swagger.yaml")) {
                        StreamUtil.copy(resourceAsStream, output);
                    } catch (IOException e) {
                        throw new InternalServerErrorException(e);
                    }
                };
            }
        }

        if (stream == null) {
            ResourceMethodRegistry registry = (ResourceMethodRegistry) dispatcher.getRegistry();
            Set<Map.Entry<String, List<ResourceInvoker>>> invokers = registry.getBounded().entrySet();
            final Collection<Resource> resources = fromInvokers(invokers);

            stream = outputStream -> {
                JsonFactory factory = new JsonFactory();
                try (JsonGenerator generator = factory.createGenerator(outputStream)) {
                    generator.setPrettyPrinter(new DefaultPrettyPrinter());
                    writeJson(resources, generator);
                }
            };
        }
        return Response.ok(stream, MediaType.APPLICATION_JSON_TYPE).build();
    }

    private void writeJson(Collection<Resource> resources, JsonGenerator generator) throws IOException {
        List<Resource> sorted = new ArrayList<>(resources);
        Collections.sort(sorted);
        generator.writeStartObject();
        generator.writeArrayFieldStart("resources");
        for (Resource resource : sorted) {
            resource.toJson(generator);
        }
    }

    private Collection<Resource> fromInvokers(Collection<Map.Entry<String, List<ResourceInvoker>>> invokers) {
        Map<String, Resource> resources = new HashMap<>();

        for (Map.Entry<String, List<ResourceInvoker>> entry : invokers) {
            ResourceMethodInvoker resourceMethod = (ResourceMethodInvoker) entry.getValue().get(0);
            String path = resourceMethod.getMethod().getDeclaringClass().getAnnotation(Path.class).value();

            if (!resources.containsKey(path)) {
                resources.put(path, new Resource(path));
            }

            for (ResourceInvoker invoker : entry.getValue()) {
                for (Method method : Method.fromResourceMethod(path, (ResourceMethodInvoker) invoker)) {
                    resources.get(path).addMethod(method);
                }
            }
        }
        return resources.values();
    }

    private static final class Resource implements Comparable<Resource> {
        private final String path;
        private final List<Method> methods;

        Resource(String path) {
            this.path = path;
            methods = new ArrayList<>();
        }

        void addMethod(Method method) {
            this.methods.add(method);
        }

        void toJson(JsonGenerator generator) throws IOException {
            generator.writeStartObject();

            generator.writeStringField("path", path);
            generator.writeArrayFieldStart("methods");
            for (Method method : methods) {
                method.toJson(generator);
            }
            generator.writeEndArray();

            generator.writeEndObject();
        }

        @Override
        public int compareTo(Resource other) {
            return path.compareToIgnoreCase(other.path);
        }
    }

    private static final class Method {
        private final String path;
        private final String verb;
        private final MediaType[] consumes;
        private final MediaType[] produces;

        Method(String path, String verb, MediaType[] consumes, MediaType[] produces) {
            this.path = path;
            this.verb = verb;
            this.consumes = consumes;
            this.produces = produces;
        }

        static Collection<Method> fromResourceMethod(String basePath, ResourceMethodInvoker method) {
            String path = basePath;
            Path annotation = method.getMethod().getAnnotation(Path.class);
            if (annotation != null) {
                path = basePath + '/' + annotation.value();
            }
            List<Method> methods = new ArrayList<>();
            for (String verb : method.getHttpMethods()) {
                methods.add(new Method(
                        path.replaceAll("//+", "/").replaceAll("/$", ""),
                        verb,
                        method.getConsumes(),
                        method.getProduces()));
            }
            return methods;
        }

        public void toJson(JsonGenerator generator) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("verb", verb);
            generator.writeStringField("path", path);

            if (consumes != null && consumes.length > 0) {
                generator.writeArrayFieldStart("consumes");
                for (MediaType type : consumes) {
                    generator.writeString(type.toString());
                }
                generator.writeEndArray();
            }

            if (produces != null && produces.length != 0) {
                generator.writeArrayFieldStart("produces");
                for (MediaType type : produces) {
                    generator.writeString(type.toString());
                }
                generator.writeEndArray();
            }

            generator.writeEndObject();

        }
    }
}
