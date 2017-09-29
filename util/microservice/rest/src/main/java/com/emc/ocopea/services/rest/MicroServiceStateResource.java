// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.ocopea.services.rest;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.messaging.ManagedInputQueue;
import com.emc.microservice.messaging.QueueExecutionState;
import com.emc.microservice.messaging.QueueReceiver;
import com.emc.microservice.messaging.QueueReceiverImpl;
import com.emc.microservice.metrics.StaticObjectMetric;
import com.emc.microservice.resource.ResourceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MicroServiceStateResource extends MicroServiceResource implements MicroServiceStateAPI {

    @Override
    public ServiceState getServiceState() {
        Context context = getMicroServiceApplication().getMicroServiceContext();

        List<ServiceState.InputQueueState> inputQueues = new ArrayList<>();

        ResourceManager<InputQueueDescriptor, InputQueueConfiguration, ManagedInputQueue> queuesManager =
                context.getQueuesManager();
        List<ManagedInputQueue> managedQueuesStats = queuesManager.getManagedResources();
        if (!managedQueuesStats.isEmpty()) {
            for (ManagedInputQueue currManagedQueue : managedQueuesStats) {
                List<ServiceState.InputQueueReceiver> receivers = new ArrayList<>();

                if (!currManagedQueue.getReceivers().isEmpty()) {
                    for (QueueReceiver currReceiver : currManagedQueue.getReceivers()) {
                        QueueExecutionState currState =
                                ((QueueReceiverImpl) currReceiver).getMessageListener().getQueueExecutionState();
                        QueueExecutionState.ReceiverRunningState state = currState.getState();
                        Date lastExecutionTime = null;
                        Date currExecutionStartTime = null;
                        Map<String, String> properties = null;

                        switch (state) {
                            case RUNNING:
                                currExecutionStartTime = new Date(currState.getExecutionStartTime());
                                Map<String, String> headers = currState.getHeaders();
                                if (!headers.isEmpty()) {
                                    properties = new HashMap<>(headers);
                                }
                                break;
                            case IDLE:
                                Long executionEndTime = currState.getExecutionEndTime();
                                if (executionEndTime != null) {
                                    lastExecutionTime = new Date(executionEndTime);
                                }
                                break;
                            default:
                                break;
                        }
                        receivers.add(new ServiceState.InputQueueReceiver(
                                currState.getState().name(),
                                currState.getInitializedDate(),
                                lastExecutionTime,
                                currExecutionStartTime,
                                properties));

                    }
                }
                inputQueues.add(new ServiceState.InputQueueState(currManagedQueue.getQueueName(), receivers));

            }

        }

        final StaticObjectMetric<RestCallInfo> restCallInfoStaticObjectMetric =
                context.getMetricsRegistry().getStaticObjectMetric(RestCallInfo.class);
        final long currentTimeMillis = System.currentTimeMillis();
        return new ServiceState(
                context.getMicroServiceName(),
                ServiceState.ServiceStateEnum.valueOf(context.getServiceState().name()),
                context.getLogger().getName(),
                inputQueues,
                restCallInfoStaticObjectMetric.list()
                        .stream()
                        .map((i) -> new ServiceState.RestRequestState(
                                i.getMethod(),
                                i.getUrl(),
                                currentTimeMillis - i.getStartTime().getTime()))
                        .collect(Collectors.toList())
        );

    }
}
