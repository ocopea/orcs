// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error.serialization;

import com.emc.microservice.messaging.error.ErrorMessageHeader;
import com.emc.microservice.messaging.error.ErrorsMessageHeader;
import com.emc.ocopea.util.io.ReadFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martiv6 on 26/02/2016.
 */
public class ErrorsMessageHeaderReaderTest {

    @Test
    public void testReadErrorsMessageHeaderFromStream() {
        InputStream inputJsonStream =
                getClass().getClassLoader().getResourceAsStream("errors/serialization/errorsmessageheader.json");

        ErrorsMessageHeader actualErrorsMessageHeader = new ErrorsMessageHeaderReader().readObject(inputJsonStream);

        ArrayList<ErrorMessageHeader> expectedErrorMessageHeaders = new ArrayList<>();
        expectedErrorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-1-uri",
                1456485443905L,
                404,
                "Service 1 unavailable"));
        expectedErrorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-2-uri",
                1456485443906L,
                405,
                "Service 2 unavailable"));
        expectedErrorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-3-uri",
                1456485443907L,
                406,
                "Service 3 unavailable"));
        expectedErrorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-4-uri",
                1456485443908L,
                407,
                "Service 4 unavailable"));
        ErrorsMessageHeader expectedErrorsMessageHeader = new ErrorsMessageHeader(expectedErrorMessageHeaders);

        Assert.assertTrue(
                "Unexpected error message header",
                expectedErrorsMessageHeader
                        .getErrorMessageHeaders()
                        .containsAll(actualErrorsMessageHeader.getErrorMessageHeaders()));
    }

    @Test
    public void testReadErrorsMessageHeaderFromString() {
        InputStream inputJsonStream =
                getClass().getClassLoader().getResourceAsStream("errors/serialization/errorsmessageheader.json");
        String inputJson = null;
        try {
            inputJson = ReadFile.getFileContents(inputJsonStream).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ErrorsMessageHeader actualErrorsMessageHeader = new ErrorsMessageHeaderReader().readObject(inputJson);

        List<ErrorMessageHeader> expectedErrorMessageHeaders = new ArrayList<>();
        expectedErrorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-1-uri",
                1456485443905L,
                404,
                "Service 1 unavailable"));
        expectedErrorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-2-uri",
                1456485443906L,
                405,
                "Service 2 unavailable"));
        expectedErrorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-3-uri",
                1456485443907L,
                406,
                "Service 3 unavailable"));
        expectedErrorMessageHeaders.add(new ErrorMessageHeader(
                "microservice-4-uri",
                1456485443908L,
                407,
                "Service 4 unavailable"));
        ErrorsMessageHeader expectedErrorsMessageHeader = new ErrorsMessageHeader(expectedErrorMessageHeaders);

        Assert.assertTrue(
                "Unexpected error message header",
                expectedErrorsMessageHeader
                        .getErrorMessageHeaders()
                        .containsAll(actualErrorsMessageHeader.getErrorMessageHeaders()));
    }

    @Test
    public void testExceptionWithNullInputStream() {
        InputStream inputStream = null;
        ErrorsMessageHeaderReader reader = new ErrorsMessageHeaderReader();
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
        ErrorsMessageHeaderReader reader = new ErrorsMessageHeaderReader();
        try {
            reader.readObject(content);
        } catch (ErrorMessageHeaderSerializationException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.startsWith("Could not deserialize "));
        }
    }
}
