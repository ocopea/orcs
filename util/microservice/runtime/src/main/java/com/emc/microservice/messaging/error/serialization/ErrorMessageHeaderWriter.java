// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error.serialization;

import com.emc.microservice.messaging.error.ErrorMessageHeader;
import com.emc.microservice.serialization.SerializationWriter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;

/**
 * Created by martiv6 on 26/02/2016.
 */
public class ErrorMessageHeaderWriter implements SerializationWriter<ErrorMessageHeader> {
    @Override
    public void writeObject(ErrorMessageHeader errorMessageHeader, OutputStream outputStream)
            throws ErrorMessageHeaderSerializationException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(outputStream, errorMessageHeader);
        } catch (Exception e) {
            String message =
                    "Could not serialize a " + ErrorMessageHeader.class.getName() + " object. Cause: " + e.getMessage();
            throw new ErrorMessageHeaderSerializationException(message, e);
        }
    }
}
