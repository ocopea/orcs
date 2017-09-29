// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

package com.emc.microservice.bootstrap;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * Created with true love by liebea on 10/27/2014.
 */
public class SchemaBootstrapScript {
    private final String resourceLocation;
    private final StringBuilder scriptContent;
    private final String description;
    private final int schemaVersion;

    public SchemaBootstrapScript(String resourceLocation, String description, int schemaVersion) {
        this.resourceLocation = resourceLocation;
        this.scriptContent = null;
        this.description = description;
        this.schemaVersion = schemaVersion;
    }

    public SchemaBootstrapScript(StringBuilder scriptContent, String description, int schemaVersion) {
        this.resourceLocation = null;
        this.scriptContent = scriptContent;
        this.description = description;
        this.schemaVersion = schemaVersion;
    }

    public String getDescription() {
        return description;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    @NoJavadoc
    public Reader getScriptReader() throws IOException {
        StringReader reader;
        if (scriptContent != null) {
            reader = new StringReader(scriptContent.toString());
        } else {
            InputStream resourceAsStream = Objects.requireNonNull(
                    this.getClass().getClassLoader().getResourceAsStream(resourceLocation),
                    "Invalid resource path provided for sql file, resource could not be located: " +
                            resourceLocation);

            reader = new StringReader(IOUtils.toString(resourceAsStream));
        }
        return reader;
    }

}
