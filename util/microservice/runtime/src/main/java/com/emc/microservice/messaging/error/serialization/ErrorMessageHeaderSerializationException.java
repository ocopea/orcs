// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error.serialization;

/**
 * Created by martiv6 on 26/02/2016.
 */
public class ErrorMessageHeaderSerializationException extends RuntimeException {
    public ErrorMessageHeaderSerializationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}