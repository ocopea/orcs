// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.messaging.error.management.command;

/**
 * @author nivenb
 */
public class RabbitMQManagementClientCommandException extends RuntimeException {

    public RabbitMQManagementClientCommandException(String message) {
        super(message);
    }

    public RabbitMQManagementClientCommandException(String message, Throwable cause) {
        super(message, cause);
    }

}
