// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.messaging.error.management.parser;

import java.io.InputStream;

/**
 * @author nivenb
 */
public interface RabbitMQManagementClientResponseParser<T> {

    T readObject(InputStream inputStream);

}
