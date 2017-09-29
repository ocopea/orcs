// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 12/22/14.
 * Drink responsibly
 */
public class ServiceState {
    private final String name;
    private final ServiceStateEnum state;
    private final String logger;
    private final List<InputQueueState> inputQueues;
    private final List<RestRequestState> restRequests;

    public enum ServiceStateEnum {
        STOPPED,
        STARTING,
        RUNNING,
        PAUSED
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InputQueueReceiver {
        private final String state;
        private final Date initTime;
        private final Date lastExecutionTime;
        private final Date currExecutionStartTime;
        private final Map<String, String> properties;

        public InputQueueReceiver() {
            this(null, null, null, null, null);
        }

        public InputQueueReceiver(
                String state,
                Date initTime,
                Date lastExecutionTime,
                Date currExecutionStartTime,
                Map<String, String> properties) {
            this.state = state;
            this.initTime = initTime;
            this.lastExecutionTime = lastExecutionTime;
            this.currExecutionStartTime = currExecutionStartTime;
            this.properties = properties;
        }

        public String getState() {
            return state;
        }

        public Date getInitTime() {
            return initTime;
        }

        public Date getLastExecutionTime() {
            return lastExecutionTime;
        }

        public Date getCurrExecutionStartTime() {
            return currExecutionStartTime;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }

    public static class InputQueueState {
        private final String queueName;
        private final List<InputQueueReceiver> receivers;

        private InputQueueState() {
            this(null, null);
        }

        public InputQueueState(String queueName, List<InputQueueReceiver> receivers) {
            this.queueName = queueName;
            this.receivers = receivers;
        }

        public String getQueueName() {
            return queueName;
        }

        public List<InputQueueReceiver> getReceivers() {
            return receivers;
        }
    }

    public static class RestRequestState {
        private final String method;
        private final String url;
        private final long timeRunning;

        private RestRequestState() {
            this(null, null, 0L);
        }

        public RestRequestState(String method, String url, long timeRunning) {
            this.method = method;
            this.url = url;
            this.timeRunning = timeRunning;
        }

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public long getTimeRunning() {
            return timeRunning;
        }

        @Override
        public String toString() {
            return "RestRequestState{" +
                    "method='" + method + '\'' +
                    ", url='" + url + '\'' +
                    ", timeRunning=" + timeRunning +
                    '}';
        }
    }

    public ServiceState(
            String name,
            ServiceStateEnum state,
            String logger,
            List<InputQueueState> inputQueues,
            List<RestRequestState> restRequests) {
        this.name = name;
        this.state = state;
        this.logger = logger;
        this.inputQueues = inputQueues;
        this.restRequests = restRequests;
    }

    private ServiceState() {
        this(null, null, null, null, null);
    }

    public String getName() {
        return name;
    }

    public ServiceStateEnum getState() {
        return state;
    }

    public String getLogger() {
        return logger;
    }

    public List<InputQueueState> getInputQueues() {
        return inputQueues;
    }

    public List<RestRequestState> getRestRequests() {
        return restRequests;
    }
}
