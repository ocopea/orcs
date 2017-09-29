// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with love by liebea on 5/18/14.
 */
public class ParametersBag {
    private final Map<String, MicroServiceParameterDescriptor> parameterDescriptorsMap;
    private final Map<String, String> parametersValues;

    public ParametersBag(Collection<MicroServiceParameterDescriptor> parameterDescriptors) {
        this.parameterDescriptorsMap = new HashMap<>(parameterDescriptors.size());
        this.parametersValues = new HashMap<>(parameterDescriptors.size());

        for (ParametersBag.MicroServiceParameterDescriptor currParamDesc : parameterDescriptors) {
            this.parameterDescriptorsMap.put(currParamDesc.getName(), currParamDesc);
        }
    }

    /***
     * Return a formatted params string separated by a delimiter
     */
    public String formatParams(String delimiter) {
        if (parameterDescriptorsMap.isEmpty()) {
            return "";
        }
        StringBuilder formattedParams = new StringBuilder();
        boolean first = true;
        for (MicroServiceParameterDescriptor currParam : parameterDescriptorsMap.values()) {
            if (first) {
                first = false;
            } else {
                formattedParams.append(delimiter);
            }
            formattedParams.append(currParam.getName())
                    .append('=')
                    .append(parametersValues.get(currParam.getName()));
        }
        return formattedParams.toString();
    }

    /***
     * Get integer parameter value
     * @param paramName parameter name
     * @return integer value of parameter
     */
    public Integer getInt(String paramName) {
        String paramValue = getString(paramName);
        if (paramValue == null) {
            return null;
        }
        return Integer.parseInt(paramValue);
    }

    private void validateParamName(String paramName) {
        if (parameterDescriptorsMap.get(paramName) == null) {
            throw new IllegalArgumentException("Unsupported parameter " + paramName);
        }
    }

    /***
     * Get long parameter value
     * @param paramName parameter name
     * @return long value of parameter
     */
    public Long getLong(String paramName) {

        String paramValue = getString(paramName);
        if (paramValue == null) {
            return null;
        }

        return Long.parseLong(paramValue);
    }

    /**
     * Get a boolean parameter value
     */
    public Boolean getBoolean(String paramName) {
        String paramValue = getString(paramName);
        if (paramValue == null) {
            return null;
        }
        return Boolean.parseBoolean(paramValue);
    }

    /***
     * Get character string parameter value
     * @param paramName parameter name
     * @return String value of parameter
     */
    public String getString(String paramName) {
        validateParamName(paramName);
        return parametersValues.get(paramName);
    }

    /**
     * Describes a single parameter
     */
    public static class MicroServiceParameterDescriptor {
        private final String name;
        private final String description;
        private final String defaultValue;
        private final boolean mandatory;

        public MicroServiceParameterDescriptor(String name,
                                               String description,
                                               String defaultValue,
                                               boolean mandatory) {
            this.name = name;
            this.description = description;
            this.defaultValue = defaultValue;
            this.mandatory = mandatory;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public boolean isMandatory() {
            return mandatory;
        }
    }

    /***
     * Set multiple parameter values
     */
    public void setParameterValues(Map<String, String> parameterValues, MicroService service) {
        // Assigning Service parameters
        for (ParametersBag.MicroServiceParameterDescriptor currParamDescriptor : parameterDescriptorsMap.values()) {
            String currParamValue = parameterValues.get(currParamDescriptor.getName());
            if (currParamValue == null) {
                currParamValue = currParamDescriptor.getDefaultValue();
            }
            if (currParamValue == null && currParamDescriptor.isMandatory()) {
                throw new IllegalArgumentException("Unable to satisfy " + service.getName() +
                        " mandatory service parameters - missing parameter: " + currParamDescriptor.getName());
            }

            this.parametersValues.put(currParamDescriptor.getName(), currParamValue);
        }

    }

    public Map<String, MicroServiceParameterDescriptor> getParameterDescriptorsMap() {
        return parameterDescriptorsMap;
    }

    public Map<String, String> getParametersValues() {
        return new HashMap<>(parametersValues);
    }
}
