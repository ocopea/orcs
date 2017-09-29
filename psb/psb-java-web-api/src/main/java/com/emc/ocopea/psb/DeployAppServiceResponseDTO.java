// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.psb;

/**
 * Created by liebea on 1/11/16.
 * Drink responsibly
 */
public class DeployAppServiceResponseDTO {
    private final int status;
    private final String message;

    private DeployAppServiceResponseDTO() {
        this(0, null);
    }

    public DeployAppServiceResponseDTO(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
