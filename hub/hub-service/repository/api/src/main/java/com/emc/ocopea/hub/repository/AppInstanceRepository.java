// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import com.emc.ocopea.util.Pair;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by liebea on 8/14/16.
 * Drink responsibly
 */
public interface AppInstanceRepository {

    Collection<DBAppInstanceConfig> listConfig();

    Collection<Pair<DBAppInstanceConfig, DBAppInstanceState>> listConfigWithState();

    Pair<DBAppInstanceConfig, DBAppInstanceState> getConfigWithState(UUID appInstanceId);

    Collection<DBAppInstanceConfig> listDownstreamConfig(UUID appInstanceId);

    DBAppInstanceConfig getConfig(UUID appInstanceId);

    DBAppInstanceConfig findConfig(String appInstanceName);

    DBAppInstanceState getState(UUID appInstanceId);

    void updateStateAndUrl(UUID appInstanceId, String state, URI url);

    void updateState(UUID appInstanceId, String state);

    void add(DBAppInstanceConfig config, DBAppInstanceState state) throws DuplicateResourceException;
}
