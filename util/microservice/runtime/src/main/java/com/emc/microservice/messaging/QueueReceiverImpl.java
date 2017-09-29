// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreAPI;

import java.util.Map;

/**
 * Created with true love.
 * User: liebea
 * Date: 12/1/13
 * Time: 7:33 PM
 */
public abstract class QueueReceiverImpl<DlqConfT extends QueueConfiguration> implements QueueReceiver {
    private final InputQueueConfiguration inputQueueConfiguration;
    private final DlqConfT queueConfiguration;
    private final Map<String, DlqConfT> deadLetterQueueConfigurations;
    private final ManagedMessageListener messageListener;
    private final Context context;
    private final String consumerName;
    protected final BlobStoreAPI blobStoreAPI;

    protected QueueReceiverImpl(
            InputQueueConfiguration inputQueueConfiguration,
            DlqConfT queueConfiguration,
            Map<String, DlqConfT> deadLetterQueueConfigurations,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName) {
        this.inputQueueConfiguration = inputQueueConfiguration;
        this.queueConfiguration = queueConfiguration;
        this.deadLetterQueueConfigurations = deadLetterQueueConfigurations;
        this.messageListener = messageListener;
        this.context = context;
        this.consumerName = consumerName;
        this.blobStoreAPI = initBlobstoreAPI();
    }

    protected QueueReceiverImpl(
            InputQueueConfiguration inputQueueConfiguration,
            DlqConfT queueConfiguration,
            Map<String, DlqConfT> deadLetterQueueConfigurations,
            ManagedMessageListener messageListener,
            Context context,
            String consumerName,
            BlobStoreAPI blobStoreAPI) {
        this.inputQueueConfiguration = inputQueueConfiguration;
        this.queueConfiguration = queueConfiguration;
        this.deadLetterQueueConfigurations = deadLetterQueueConfigurations;
        this.messageListener = messageListener;
        this.context = context;
        this.consumerName = consumerName;
        this.blobStoreAPI = blobStoreAPI;
    }

    protected BlobStoreAPI initBlobstoreAPI() {
        String blobstoreName = getQueueConfiguration().getBlobstoreName();
        if (blobstoreName == null || blobstoreName.isEmpty()) {
            return null;
        } else {
            try {
                return getContext().getBlobStoreManager()
                        .getManagedResourceByName(blobstoreName).getBlobStoreAPI();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed loading blobStore " + blobstoreName + " which is required "
                        + "for messaging configuration " + getInputQueueConfiguration().toString());
            }
        }
    }

    public InputQueueConfiguration getInputQueueConfiguration() {
        return inputQueueConfiguration;
    }

    public DlqConfT getQueueConfiguration() {
        return queueConfiguration;
    }

    public Map<String, DlqConfT> getDeadLetterQueueConfigurations() {
        return deadLetterQueueConfigurations;
    }

    public ManagedMessageListener getMessageListener() {
        return messageListener;
    }

    public Context getContext() {
        return context;
    }

    public String getConsumerName() {
        return consumerName;
    }
}