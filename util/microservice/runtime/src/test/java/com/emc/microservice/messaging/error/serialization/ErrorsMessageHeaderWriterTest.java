// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error.serialization;

import com.emc.microservice.messaging.error.ErrorMessageHeader;
import com.emc.microservice.messaging.error.ErrorsMessageHeader;
import com.emc.ocopea.util.io.ReadFile;
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martiv6 on 26/02/2016.
 */
public class ErrorsMessageHeaderWriterTest {

    @Test
    public void testWriteListErrorMessageHeader() {
        List<ErrorMessageHeader> errorMessageHeaders = new ArrayList<>();
        errorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-1-uri",
                1456485443905L,
                404,
                "Service 1 unavailable"));
        errorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-2-uri",
                1456485443906L,
                405,
                "Service 2 unavailable"));
        errorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-3-uri",
                1456485443907L,
                406,
                "Service 3 unavailable"));
        errorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-4-uri",
                1456485443908L,
                407,
                "Service 4 unavailable"));
        ErrorsMessageHeader errorsMessageHeader = new ErrorsMessageHeader(errorMessageHeaders);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new ErrorsMessageHeaderWriter().writeObject(errorsMessageHeader, outputStream);
        String jsonResult = outputStream.toString();
        JsonFluentAssert actualJson = JsonFluentAssert.assertThatJson(jsonResult);

        InputStream expectedJson =
                getClass().getClassLoader().getResourceAsStream("errors/serialization/errorsmessageheader.json");
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
        ErrorsMessageHeaderWriter writer = new ErrorsMessageHeaderWriter();
        try {
            writer.writeObject(null, null);
        } catch (ErrorMessageHeaderSerializationException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.startsWith("Could not serialize "));
        }
    }

}
