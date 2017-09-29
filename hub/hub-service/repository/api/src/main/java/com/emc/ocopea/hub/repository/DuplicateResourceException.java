// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

/**
 * Created by liebea on 7/24/16.
 * Drink responsibly
 */
public class DuplicateResourceException extends Exception {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateResourceException(Throwable cause) {
        super(cause);
    }
}
