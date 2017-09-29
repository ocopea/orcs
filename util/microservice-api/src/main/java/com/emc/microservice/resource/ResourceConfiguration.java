// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/5/2014. Enjoy it
 * This class represent a Resource configuration as returned by the Service Registry
 * Resource configuration is a key value pair map abstraction that allows different deployment stacks to have
 * different configuration descriptors in runtime while keeping the same non-technology dependent codebase the same for
 * all deployment stacks
 */
public abstract class ResourceConfiguration {
    private Map<String, String> propertyValues;
    private final String configurationName;

    // Ordered list of properties
    private final List<ResourceConfigurationProperty> propertyList;

    // Properties map by name
    private final Map<String, ResourceConfigurationProperty> supportedProperties;

    protected ResourceConfiguration(String configurationName, List<ResourceConfigurationProperty> properties) {
        this.configurationName = configurationName;
        this.propertyList = new ArrayList<>(properties);
        this.supportedProperties = new HashMap<>(properties.size());
        for (ResourceConfigurationProperty currResourceConfigProperty : properties) {
            this.supportedProperties.put(currResourceConfigProperty.getName(), currResourceConfigProperty);
        }
    }

    /**
     * Return a set of property names that belong to this configuration resource but should not be printed to
     * public logs (e.g. passwords) Resource Configuration implementations that require this should override this method
     *
     * @return Set of private secure property names
     */
    protected Set<String> getPrivatePropertyNames() {
        return Collections.emptySet();
    }

    public static Map<String, String> propArrayToMap(String[] propertiesPairs) {
        Map<String, String> props = new HashMap<>(propertiesPairs.length / 2);
        for (int i = 0; i < propertiesPairs.length; i += 2) {
            props.put(propertiesPairs[i], propertiesPairs[i + 1]);
        }
        return props;
    }

    protected static Map<String, String> propArrayToMap(String[] propertiesPairs, String[] extraPropertiesPairs) {
        Map<String, String> props = new HashMap<>(propertiesPairs.length + extraPropertiesPairs.length / 2);
        for (int i = 0; i < propertiesPairs.length; i += 2) {
            props.put(propertiesPairs[i], propertiesPairs[i + 1]);
        }
        for (int i = 0; i < extraPropertiesPairs.length; i += 2) {
            props.put(extraPropertiesPairs[i], extraPropertiesPairs[i + 1]);
        }
        return props;
    }

    public Map<String, String> getPropertyValues() {
        return propertyValues;
    }

    /***
     * Returns map of public property values
     */
    public Map<String, String> getPublicPropertyValues() {
        Map<String, String> publicProperties = new HashMap<>(propertyValues.size());
        propertyList.stream().filter(currProp -> !currProp.isHideWhenLogging()).forEach(currProp -> {
            publicProperties.put(currProp.getName(), getProperty(currProp.getName()));
        });
        return publicProperties;
    }

    public String getProperty(String name) {
        return propertyValues.get(name);
    }

    public <T extends ResourceConfiguration> T asSpecificConfiguration(Class<T> t) {
        if (t.isAssignableFrom(this.getClass())) {
            //noinspection unchecked
            return (T) this;
        }
        return instantiateResourceConfClass(t, this.propertyValues);
    }

    public static <T extends ResourceConfiguration> T asSpecificConfiguration(
            Class<T> t,
            Map<String, String> properties) {

        return instantiateResourceConfClass(t, properties);
    }

    public static <T extends ResourceConfiguration> T asSpecificConfiguration(
            Class<T> t,
            String[] properties) {

        return instantiateResourceConfClass(t, propArrayToMap(properties));
    }

    private static <T extends ResourceConfiguration> T instantiateResourceConfClass(
            Class<T> t,
            Map<String, String> properties) {
        try {
            T newInstance = t.newInstance();
            newInstance.setPropertyValues(properties);
            return newInstance;

        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to cast configuration class", e);
        }
    }

    public String toPersistentString() {
        //todo:better :)
        return propsToPersistentFormat(propertyValues);
    }

    /***
     * Represent properties as persistent string format
     */
    public static String propsToPersistentFormat(Map<String, String> props) {
        StringBuilder builder = new StringBuilder();
        props.entrySet()
                .stream()
                .filter(currEntry -> currEntry.getValue() != null)
                .forEach(currEntry -> {
                    if (builder.length() > 0) {
                        builder.append(',');
                    }
                    builder.append(currEntry.getKey()).append(',').append(currEntry.getValue());
                });
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> currEntry : getPublicPropertyValues().entrySet()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(currEntry.getKey());
            builder.append(',');
            builder.append(currEntry.getValue());
        }
        return builder.toString();
    }

    /***
     * Override this method to provide custom validations
     * Hook to create custom checks for configuration property values
     * e.g. mandatory types dependent on specific values, ranges etc.
     * If validation fails method is expected to throw IllegalArgumentException.
     * @param propertyValues property values by propName as string values before assignation
     */
    protected void customValidate(Map<String, String> propertyValues) {
    }

    protected void setPropertyValues(Map<String, String> propertyValues) {

        //todo: validate types too...
        for (ResourceConfigurationProperty currProp : this.propertyList) {
            if (currProp.isMandatory()) {
                String mandatoryPropertyValue = propertyValues.get(currProp.getName());
                if (mandatoryPropertyValue == null ||
                        mandatoryPropertyValue.isEmpty()) {
                    throw new IllegalArgumentException("Value for property " + currProp.getName() +
                            " must not be empty in " + configurationName + " Resource Configuration");
                }
            }
        }

        // Call custom validation
        customValidate(propertyValues);

        this.propertyValues = propertyValues;
    }

    protected static List<ResourceConfigurationProperty> appendProps(
            List<ResourceConfigurationProperty> properties,
            List<ResourceConfigurationProperty> additionalProperties) {
        ArrayList<ResourceConfigurationProperty> merged =
                new ArrayList<>(properties.size() + (additionalProperties == null ? 0 : additionalProperties.size()));
        merged.addAll(properties);
        if (additionalProperties != null) {
            merged.addAll(additionalProperties);
        }
        return merged;
    }

    public Map<String, ResourceConfigurationProperty> getSupportedProperties() {
        return Collections.unmodifiableMap(supportedProperties);
    }
}
