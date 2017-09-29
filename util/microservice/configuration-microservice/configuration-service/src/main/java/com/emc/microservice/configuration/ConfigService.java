// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.IntegerNativeQueryConverter;
import com.emc.ocopea.util.database.NativeQueryService;
import com.emc.ocopea.util.database.StringNativeQueryConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by liebea on 8/12/15.
 * Drink responsibly
 */
public class ConfigService {
    private final NativeQueryService nqs;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

    public ConfigService(DataSource dataSource) {
        this.nqs = new BasicNativeQueryService(dataSource);
    }

    @NoJavadoc
    public String read(String path) {
        LOGGER.info("Getting configuration for {}", path);
        String cleanPath = validatePath(path);
        String content = fetchContent(cleanPath);
        if (content == null) {
            return listFolder(cleanPath);
        }
        return content;
    }

    private String fetchContent(final String path) {
        List<String> list = nqs.getList("select config_content from config_data where config_path = ?",
                new StringNativeQueryConverter(),
                Arrays.<Object>asList(path));
        return list.isEmpty() ? null : list.get(0);
    }

    @NoJavadoc
    public String listFolder(String path) {
        final String folder = path.endsWith("/") ? path : path + '/';
        String query = "select config_path from config_data where config_path like ?";
        List<String> list = nqs.getList(query, new StringNativeQueryConverter(), Arrays.<Object>asList(folder + '%'));

        Set<String> childrenSet = new HashSet<>();

        for (String next : list) {
            String child = extractChildren(folder, next);
            childrenSet.add(child);
        }
        List<String> children = new ArrayList<>(childrenSet);
        Collections.sort(children);

        return toJson(children.iterator());
    }

    private String extractChildren(String parent, String path) {
        int end = path.indexOf('/', parent.length());
        if (end == -1) {
            return path;
        }

        return path.substring(0, end);
    }

    private String toJson(Iterator<String> children) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        if (children.hasNext()) {
            builder.append('\"').append(children.next()).append('\"');
        }
        while (children.hasNext()) {
            builder.append(',');
            builder.append('\"').append(children.next()).append('\"');
        }
        builder.append(']');
        return builder.toString();
    }

    @NoJavadoc
    public void write(String path, String data) {
        LOGGER.info("Creating configuration for {}", path);
        if (data == null || "".equals(data.trim())) {
            throw new WebApplicationException(
                    new IllegalArgumentException("Data can't be empty"),
                    Response.Status.BAD_REQUEST);
        }
        final String cleanData = data.trim();
        final String cleanedPath = validatePath(path);
        if (exists(cleanedPath)) {
            throw new WebApplicationException(
                    new IllegalArgumentException("The path already exists - " + path),
                    Response.Status.CONFLICT);
        }

        nqs.executeUpdate(
                "insert into config_data (config_path, config_content) values (?, ?)",
                Arrays.<Object>asList(cleanedPath, cleanData));
    }

    @NoJavadoc
    public void overwrite(String path, String data) {
        LOGGER.info("Updating configuration for {}", path);
        final String cleanPath = validatePath(path);
        if (isFolder(cleanPath)) {
            throw new WebApplicationException(
                    new IllegalArgumentException("Cannot overwrite a folder"),
                    Response.Status.BAD_REQUEST);
        }
        if (!exists(cleanPath)) {
            // insert
            write(path, data);
        } else {
            if (data == null || "".equals(data.trim())) {
                throw new WebApplicationException(
                        new IllegalArgumentException("Data can't be empty"),
                        Response.Status.BAD_REQUEST);
            }
            final String cleanData = data.trim();
            String query = "update config_data set config_content = ? where config_path = ? ";
            nqs.executeUpdate(query, Arrays.<Object>asList(cleanData, cleanPath));
        }
    }

    @NoJavadoc
    public void delete(String path) {
        LOGGER.info("Deleting configuration for {}", path);
        final String cleanPath = validatePath(path);
        if (isFolder(cleanPath)) {
            throw new WebApplicationException(
                    new IllegalStateException("Path is a non empty folder. Can't delete"),
                    Response.Status.BAD_REQUEST);
        }
        String query = "delete from config_data where config_path = ?";
        nqs.executeUpdate(query, Arrays.<Object>asList(cleanPath));
    }

    private boolean isFolder(String path) {
        final String folder = path.endsWith("/") ? path : path + '/';
        String query = "select count(*) from config_data where config_path like ?";
        return nqs.getSingleValue(query, new IntegerNativeQueryConverter(), Arrays.<Object>asList(folder + '%')) > 0;
    }

    private static String validatePath(String path) {
        path = path.toLowerCase().trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!PathUtilities.isValid(path)) {
            throw new WebApplicationException(
                    new IllegalArgumentException("Invalid path: " + path),
                    Response.Status.BAD_REQUEST);
        }
        return path;
    }

    private boolean exists(final String path) {
        return nqs.getSingleValue(
                "select count(*) from config_data where config_path = ?",
                new IntegerNativeQueryConverter(),
                Arrays.<Object>asList(path)) > 0;
    }

}
