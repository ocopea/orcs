// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error.serialization;

import com.emc.microservice.messaging.error.ErrorMessageHeader;
import com.emc.ocopea.util.io.ReadFile;
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by martiv6 on 26/02/2016.
 */
public class ErrorMessageHeaderWriterTest {

    @Test
    public void testWriteErrorMessageHeader() {
        ErrorMessageHeader errorMessageHeader =
                new ErrorMessageHeader("microservice-1-uri", 1456485443905L, 404, "Service unavailable");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new ErrorMessageHeaderWriter().writeObject(errorMessageHeader, outputStream);
        String jsonResult = outputStream.toString();
        JsonFluentAssert actualJson = JsonFluentAssert.assertThatJson(jsonResult);

        InputStream expectedJson =
                getClass().getClassLoader().getResourceAsStream("errors/serialization/errormessageheader.json");
        String expectedJSON = null;
        try {
            expectedJSON = ReadFile.getFileContents(expectedJson).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        actualJson.hasSameStructureAs(expectedJSON);
        actualJson.isEqualTo(expectedJSON);
    }

    @Test
    public void testException() {
        ErrorMessageHeaderWriter writer = new ErrorMessageHeaderWriter();
        try {
            writer.writeObject(null, null);
        } catch (ErrorMessageHeaderSerializationException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.startsWith("Could not serialize "));
        }
    }

}
