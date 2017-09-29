// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error.serialization;

import com.emc.microservice.messaging.error.ErrorMessageHeader;
import com.emc.microservice.serialization.SerializationReader;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.Reader;

/**
 * Created by martiv6 on 26/02/2016.
 */
public class ErrorMessageHeaderReader implements SerializationReader<ErrorMessageHeader> {

    @NoJavadoc
    @Override
    public ErrorMessageHeader readObject(InputStream inputStream) throws ErrorMessageHeaderSerializationException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(inputStream, ErrorMessageHeader.class);

        } catch (Exception e) {
            String message =
                    "Could not deserialize input into a " + ErrorMessageHeader.class.getName() + " object. Cause: " +
                            e.getMessage();
            throw new ErrorMessageHeaderSerializationException(message, e);
        }
    }

    @NoJavadoc
    @Override
    public ErrorMessageHeader readObject(Reader reader) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(reader, ErrorMessageHeader.class);

        } catch (Exception e) {
            String message =
                    "Could not deserialize input into a " + ErrorMessageHeader.class.getName() + " object. Cause: " +
                            e.getMessage();
            throw new ErrorMessageHeaderSerializationException(message, e);
        }

    }

    @NoJavadoc
    public ErrorMessageHeader readObject(String content) throws ErrorMessageHeaderSerializationException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(content, ErrorMessageHeader.class);
        } catch (Exception e) {
            String message =
                    "Could not deserialize input into a " + ErrorMessageHeader.class.getName() + " object. Cause: " +
                            e.getMessage();
            throw new ErrorMessageHeaderSerializationException(message, e);
        }
    }

}