// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error.serialization;

import com.emc.microservice.messaging.error.ErrorMessageHeader;
import com.emc.ocopea.util.io.ReadFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by martiv6 on 26/02/2016.
 */
public class ErrorMessageHeaderReaderTest {

    @Test
    public void testReadMessageHeaderFromStream() {
        InputStream inputJsonStream =
                getClass().getClassLoader().getResourceAsStream("errors/serialization/errormessageheader.json");

        ErrorMessageHeader actualErrorMessageHeader = new ErrorMessageHeaderReader().readObject(inputJsonStream);
        ErrorMessageHeader expectedErrorMessageHeader =
                new ErrorMessageHeader("microservice-1-uri", 1456485443905L, 404, "Service unavailable");

        Assert.assertEquals("Unexpected error message header", expectedErrorMessageHeader, actualErrorMessageHeader);
    }

    @Test
    public void testReadErrorMessageHeaderFromString() {
        InputStream inputJsonStream =
                getClass().getClassLoader().getResourceAsStream("errors/serialization/errormessageheader.json");
        String inputJson = null;
        try {
            inputJson = ReadFile.getFileContents(inputJsonStream).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ErrorMessageHeader actualErrorMessageHeader = new ErrorMessageHeaderReader().readObject(inputJson);
        ErrorMessageHeader expectedErrorMessageHeader =
                new ErrorMessageHeader("microservice-1-uri", 1456485443905L, 404, "Service unavailable");

        Assert.assertEquals("Unexpected error message header", expectedErrorMessageHeader, actualErrorMessageHeader);
    }

    @Test
    public void testException() {
        InputStream inputStream = null;
        ErrorMessageHeaderReader reader = new ErrorMessageHeaderReader();
        try {
            reader.readObject(inputStream);
        } catch (ErrorMessageHeaderSerializationException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.startsWith("Could not deserialize "));
        }
    }

    @Test
    public void testExceptionWithNullContent() {
        String content = null;
        ErrorMessageHeaderReader reader = new ErrorMessageHeaderReader();
        try {
            reader.readObject(content);
        } catch (ErrorMessageHeaderSerializationException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.startsWith("Could not deserialize "));
        }
    }

}
