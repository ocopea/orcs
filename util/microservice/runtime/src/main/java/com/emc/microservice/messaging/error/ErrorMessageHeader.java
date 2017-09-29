// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging.error;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by martiv6 on 23/02/2016.
 */
public class ErrorMessageHeader {

    @JsonProperty("microservice_uri")
    private String microserviceURI;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("error_code")
    private int errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    public ErrorMessageHeader() {
    }

    public ErrorMessageHeader(String microserviceURI, long timestamp, int errorCode, String errorMessage) {
        this.microserviceURI = microserviceURI;
        this.timestamp = timestamp;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getMicroserviceURI() {
        return microserviceURI;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ErrorMessageHeader that = (ErrorMessageHeader) o;

        if (errorCode != that.errorCode) {
            return false;
        }
        if (timestamp != that.timestamp) {
            return false;
        }
        if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null) {
            return false;
        }
        if (microserviceURI != null ? !microserviceURI.equals(that.microserviceURI) : that.microserviceURI != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = microserviceURI != null ? microserviceURI.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + errorCode;
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        return result;
    }
}
