// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error.serialization;

import com.emc.microservice.messaging.error.ErrorMessageHeader;
import com.emc.microservice.messaging.error.ErrorsMessageHeader;
import com.emc.microservice.serialization.SerializationReader;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.Reader;

/**
 * Created by martiv6 on 26/02/2016.
 */
public class ErrorsMessageHeaderReader implements SerializationReader<ErrorsMessageHeader> {

    @Override
    public ErrorsMessageHeader readObject(InputStream inputStream) throws ErrorMessageHeaderSerializationException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(inputStream, ErrorsMessageHeader.class);
        } catch (Exception e) {
            String message = "Could not deserialize input into a List of " + ErrorMessageHeader.class.getName() +
                    " object. Cause: " + e.getMessage();
            throw new ErrorMessageHeaderSerializationException(message, e);
        }
    }

    @Override
    public ErrorsMessageHeader readObject(Reader reader) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(reader, ErrorsMessageHeader.class);
        } catch (Exception e) {
            String message = "Could not deserialize input into a List of " + ErrorMessageHeader.class.getName() +
                    " object. Cause: " + e.getMessage();
            throw new ErrorMessageHeaderSerializationException(message, e);
        }

    }

    @NoJavadoc
    public ErrorsMessageHeader readObject(String content) throws ErrorMessageHeaderSerializationException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(content, ErrorsMessageHeader.class);
        } catch (Exception e) {
            String message = "Could not deserialize input into a List of " + ErrorMessageHeader.class.getName() +
                    " object. Cause: " + e.getMessage();
            throw new ErrorMessageHeaderSerializationException(message, e);
        }
    }
}