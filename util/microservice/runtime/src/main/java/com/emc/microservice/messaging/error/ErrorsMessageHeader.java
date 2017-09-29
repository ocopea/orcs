// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Created by patele on 23/02/2016.
 */
public class ErrorsMessageHeader {

    private List<ErrorMessageHeader> errorMessageHeaders;

    public ErrorsMessageHeader() {
    }

    public ErrorsMessageHeader(List<ErrorMessageHeader> errorMessageHeaders) {
        this.errorMessageHeaders = errorMessageHeaders;
    }

    public List<ErrorMessageHeader> getErrorMessageHeaders() {
        return errorMessageHeaders;
    }

    @NoJavadoc
    @JsonIgnore
    public ErrorMessageHeader getLatestError() {
        ErrorMessageHeader latestErrorMessageHeader = null;
        for (ErrorMessageHeader errorMessageHeader : errorMessageHeaders) {
            if (latestErrorMessageHeader == null) {
                latestErrorMessageHeader = errorMessageHeader;
                continue;
            } else if (errorMessageHeader.getTimestamp() > latestErrorMessageHeader.getTimestamp()) {
                latestErrorMessageHeader = errorMessageHeader;
            }
        }
        return latestErrorMessageHeader;
    }
}
