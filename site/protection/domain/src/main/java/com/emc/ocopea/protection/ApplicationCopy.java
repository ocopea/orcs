// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopy {
    private static final Logger log = LoggerFactory.getLogger(ApplicationCopy.class);
    private UUID id;
    private Long version = null;
    private UUID appInstanceId;
    private ApplicationCopyState state = ApplicationCopyState.scheduled;
    private String stateMessage;
    private Date stateTimestamp;
    private Date timeStamp;

    // key = dsbURN-serviceId
    private Map<String, ApplicationDataServiceCopy> dataServiceCopies;

    // key = appServiceName..
    private Map<String, ApplicationAppServiceCopy> appServiceCopies;

    private final List<ApplicationCopyEvent> eventsSinceLoad = new ArrayList<>();

    // This events list represents downstream events created as a result of rolling an event onto the state machine
    private final Map<Class<? extends ApplicationCopyEvent>, BiConsumer<ApplicationCopyEvent, Boolean>> handlers = new
            HashMap<>();

    // This event is the translation of user operation onto an event needs to be persisted to represent the user action
    private ApplicationCopyEvent executionEvent = null;

    /***
     * Creates an application copy and starts the process of making the copy generating the required
     * Events for event listeners to consume. creating copy queue event for all data services and app services
     * @param builder copy builder containing all copy details
     */
    public ApplicationCopy create(ApplicationCopyBuilder builder) {

        // Applying a "ApplicationCopyScheduledEvent"
        setExecutionEvent(builder.build());

        return this;
    }

    @NoJavadoc
    public ApplicationCopy markDataServiceCopyAsRunning(String dsbURN, String serviceId, String message) {

        setExecutionEvent(new ApplicationCopyDataServiceRunningEvent(
                id,
                appInstanceId,
                version + 1,
                new Date(),
                message,
                dsbURN,
                serviceId));

        return this;
    }

    @NoJavadoc
    public ApplicationCopy markAppServiceCopyAsRunning(String appServiceName, String message) {

        setExecutionEvent(new ApplicationCopyAppServiceRunningEvent(
                id,
                appInstanceId,
                version + 1,
                new Date(),
                message,
                appServiceName));

        return this;
    }

    @NoJavadoc
    public ApplicationCopy markAppCopyAsInvalid(String message) {

        setExecutionEvent(new ApplicationCopyErrorEvent(
                id,
                appInstanceId,
                version + 1,
                new Date(),
                message));

        return this;
    }

    /***
     * Marks a specific app service copy as copied successfully and generate expected down-stream events
     * If applicable for the flow of successfully finishing the copy.
     * @param appServiceName app service name
     * @param appConfiguration app copy details - to be stored with the copy metadata
     */
    public ApplicationCopy markAppServiceCopyAsCreatedSuccessfully(
            String appServiceName,
            Map<String, String> appConfiguration) {
        setExecutionEvent(new ApplicationCopyAppServiceCreatedSuccessfullyEvent(
                id,
                appInstanceId,
                version + 1,
                new Date(),
                null,
                appServiceName,
                appConfiguration));

        return this;
    }

    /***
     * Marks a specific data service copy as copied successfully and generate expected down-stream events
     * If applicable for the flow of successfully finishing the copy.
     * @param dsbURN dsb urn used to make the copy
     * @param serviceId service Id
     * @param message message from dsb for the copy if applicable
     * @param dataServiceCopyId data service copy id stored on cr
     * @param copyRepoURN CRB urn used to store the data service copy
     */
    public ApplicationCopy markDataServiceCopyAsCreatedSuccessfully(
            String dsbURN,
            String serviceId,
            String message,
            UUID dataServiceCopyId,
            String copyRepoURN) {

        setExecutionEvent(new ApplicationCopyDataServiceCreatedSuccessfullyEvent(
                id,
                appInstanceId,
                version + 1,
                new Date(),
                message,
                dsbURN,
                serviceId,
                dataServiceCopyId,
                copyRepoURN));

        return this;
    }

    public boolean isAllCreated() {
        return dataServiceCopies.values()
                .stream()
                .allMatch(
                        applicationDataServiceCopy ->
                                applicationDataServiceCopy.getState() ==
                                        ApplicationDataServiceCopyState.created) &&

                appServiceCopies.values()
                        .stream()
                        .allMatch(
                                applicationAppServiceCopy ->
                                        applicationAppServiceCopy.getState() ==
                                                ApplicationDataServiceCopyState.created);
    }

    /**
     * Marks a specific app service copy as failed. this will trigger events to fail the entire app copy
     *
     * @param appServiceName app service name
     * @param message error message causing the app service copy to fail
     */
    public ApplicationCopy markAppServiceCopyAsFailed(String appServiceName, String message) {

        setExecutionEvent(new ApplicationCopyAppServiceFailedEvent(
                id,
                appInstanceId,
                version + 1,
                new Date(),
                message,
                appServiceName));

        return this;

    }

    @NoJavadoc
    public ApplicationCopy markDataServiceCopyAsFailed(String dsbURN, String serviceId, String message) {

        setExecutionEvent(new ApplicationCopyDataServiceFailedEvent(
                id,
                appInstanceId,
                version + 1,
                new Date(),
                message,
                dsbURN,
                serviceId));

        return this;
    }

    public ApplicationCopy(Collection<ApplicationCopyEvent> events) {
        this();
        applyEvents(events);
    }

    public ApplicationCopy() {
        handlers.put(ApplicationCopyScheduledEvent.class, this::onCreate);
        handlers.put(ApplicationCopyDataServiceQueuedEvent.class, this::onDataServiceQueued);
        handlers.put(ApplicationCopyDataServiceFailedEvent.class, this::onDataServiceFailed);
        handlers.put(ApplicationCopyDataServiceRunningEvent.class, this::onDataServiceRunning);
        handlers.put(ApplicationCopyDataServiceCreatedSuccessfullyEvent.class, this::onDataServiceCreatedSuccessfully);

        handlers.put(ApplicationCopyAppServiceQueuedEvent.class, this::onAppServiceQueued);
        handlers.put(ApplicationCopyAppServiceFailedEvent.class, this::onAppServiceFailed);
        handlers.put(ApplicationCopyAppServiceRunningEvent.class, this::onAppServiceRunning);
        handlers.put(ApplicationCopyAppServiceCreatedSuccessfullyEvent.class, this::onAppServiceCreatedSuccessfully);

        handlers.put(ApplicationCopyErrorEvent.class, this::onApplicationCopyError);
        handlers.put(ApplicationCopyCreatedSuccessfullyEvent.class, this::onApplicationCopyCreatedSuccessfully);
    }

    private void onApplicationCopyCreatedSuccessfully(
            ApplicationCopyEvent applicationCopyEvent,
            Boolean triggerDownstreamEvents) {

        this.state = ApplicationCopyState.created;
    }

    private void onApplicationCopyError(ApplicationCopyEvent applicationCopyEvent, Boolean triggerDownstreamEvents) {
        this.state = ApplicationCopyState.failed;
    }

    private void onDataServiceCreatedSuccessfully(
            ApplicationCopyEvent applicationCopyEvent,
            Boolean triggerDownstreamEvents) {

        ApplicationCopyDataServiceCreatedSuccessfullyEvent e =
                (ApplicationCopyDataServiceCreatedSuccessfullyEvent) applicationCopyEvent;

        final ApplicationDataServiceCopy dataServiceCopy = getDataServiceCopy(e.getDsbURN(), e.getServiceId());
        dataServiceCopy.setState(ApplicationDataServiceCopyState.created, e.getTimeStamp(), e.getMessage());
        dataServiceCopy.setCopyId(e.getCopyRepoURN(), e.getDataServiceCopyId());

        if (triggerDownstreamEvents) {
            // if all are running, apply an application copy created successfully event
            if (isAllCreated()) {
                applyAndAdd(new ApplicationCopyCreatedSuccessfullyEvent(
                        id,
                        appInstanceId,
                        version,
                        applicationCopyEvent.getTimeStamp(),
                        null));
            }

        }
    }

    private void onAppServiceCreatedSuccessfully(
            ApplicationCopyEvent applicationCopyEvent,
            Boolean triggerDownstreamEvents) {

        ApplicationCopyAppServiceCreatedSuccessfullyEvent e =
                (ApplicationCopyAppServiceCreatedSuccessfullyEvent) applicationCopyEvent;

        final ApplicationAppServiceCopy appServiceCopy = appServiceCopies.get(e.getAppServiceName());
        appServiceCopy.setState(ApplicationDataServiceCopyState.created, e.getTimeStamp(), e.getMessage());
        appServiceCopy.setAppConfiguration(e.getAppConfiguration());

        if (triggerDownstreamEvents) {
            // if all are running, apply an application copy created successfully event
            if (isAllCreated()) {
                applyAndAdd(new ApplicationCopyCreatedSuccessfullyEvent(
                        id,
                        appInstanceId,
                        version,
                        applicationCopyEvent.getTimeStamp(),
                        null));
            }

        }
    }

    private void onDataServiceRunning(ApplicationCopyEvent applicationCopyEvent, Boolean triggerDownstreamEvents) {

        ApplicationCopyDataServiceRunningEvent e = (ApplicationCopyDataServiceRunningEvent) applicationCopyEvent;
        getDataServiceCopy(e.getDsbURN(), e.getServiceId()).setState(
                ApplicationDataServiceCopyState.running,
                e.getTimeStamp(),
                e.getMessage());
    }

    private void onAppServiceRunning(ApplicationCopyEvent applicationCopyEvent, Boolean triggerDownstreamEvents) {

        ApplicationCopyAppServiceRunningEvent e = (ApplicationCopyAppServiceRunningEvent) applicationCopyEvent;

        appServiceCopies.get(e.getAppServiceName()).setState(
                ApplicationDataServiceCopyState.running,
                e.getTimeStamp(),
                e.getMessage());
    }

    private void onDataServiceFailed(ApplicationCopyEvent applicationCopyEvent, Boolean triggerDownstreamEvents) {
        ApplicationCopyDataServiceFailedEvent e = (ApplicationCopyDataServiceFailedEvent) applicationCopyEvent;
        getDataServiceCopy(e.getDsbURN(), e.getServiceId()).setState(ApplicationDataServiceCopyState.failed,
                e.getTimeStamp(), e.getMessage());

        if (triggerDownstreamEvents) {
            applyAndAdd(new ApplicationCopyErrorEvent(
                    id,
                    appInstanceId,
                    version + 1,
                    applicationCopyEvent.getTimeStamp(),
                    applicationCopyEvent.getMessage()));
        }
    }

    private void onAppServiceFailed(
            ApplicationCopyEvent applicationCopyEvent,
            Boolean triggerDownstreamEvents) {
        ApplicationCopyAppServiceFailedEvent e = (ApplicationCopyAppServiceFailedEvent) applicationCopyEvent;
        appServiceCopies.get(e.getAppServiceName()).setState(ApplicationDataServiceCopyState.failed, e.getTimeStamp(),
                e.getMessage());

        if (triggerDownstreamEvents) {
            applyAndAdd(new ApplicationCopyErrorEvent(
                    id,
                    appInstanceId,
                    version + 1,
                    applicationCopyEvent.getTimeStamp(),
                    applicationCopyEvent.getMessage()));
        }
    }

    private void onDataServiceQueued(ApplicationCopyEvent applicationCopyEvent, Boolean triggerDownstreamEvents) {
        ApplicationCopyDataServiceQueuedEvent e = (ApplicationCopyDataServiceQueuedEvent) applicationCopyEvent;

        getDataServiceCopy(e.getDsbUrn(), e.getServiceId()).setState(
                ApplicationDataServiceCopyState.queued,
                e.getTimeStamp(),
                e.getMessage());
    }

    private void onAppServiceQueued(ApplicationCopyEvent applicationCopyEvent, Boolean triggerDownstreamEvents) {

        ApplicationCopyAppServiceQueuedEvent e = (ApplicationCopyAppServiceQueuedEvent) applicationCopyEvent;

        appServiceCopies.get(e.getAppServiceName()).setState(
                ApplicationDataServiceCopyState.queued,
                e.getTimeStamp(),
                e.getMessage());
    }

    private void onCreate(ApplicationCopyEvent applicationCopyEvent, Boolean triggerDownstreamEvents) {

        ApplicationCopyScheduledEvent applicationCopyCreatedEvent =
                (ApplicationCopyScheduledEvent) applicationCopyEvent;

        if (state != ApplicationCopyState.scheduled) {
            throw new IllegalStateException("Application copy can't be created more than once");
        }

        this.id = applicationCopyCreatedEvent.getAppCopyId();
        this.appInstanceId = applicationCopyCreatedEvent.getAppInstanceId();
        this.state = ApplicationCopyState.inprogress;
        this.timeStamp = applicationCopyCreatedEvent.getCopyTimestamp();
        this.stateTimestamp = applicationCopyCreatedEvent.getTimeStamp();
        this.dataServiceCopies = applicationCopyCreatedEvent.getDataServiceCopies()
                .stream()
                .collect(Collectors.toMap(
                        o -> o.getDsbUrn() + '-' + o.getServiceId(),
                        o ->
                                new ApplicationDataServiceCopy(
                                        o.getDsbUrn(),
                                        o.getDsbUrl(),
                                        o.getServiceId(),
                                        o.getBindName(),
                                        o.getFacility(),
                                        o.getDsbSettings(),
                                        ApplicationDataServiceCopyState.pending,
                                        null,
                                        applicationCopyCreatedEvent.getTimeStamp())
                ));

        this.appServiceCopies = applicationCopyCreatedEvent.getAppServiceCopies()
                .stream()
                .collect(
                        Collectors.toMap(
                                ApplicationCopyScheduledEvent.AppServiceInfo::getAppServiceName,
                                appServiceInfo -> new ApplicationAppServiceCopy(
                                        appServiceInfo.getPsbURN(),
                                        appServiceInfo.getAppServiceName(),
                                        appServiceInfo.getAppImageName(),
                                        appServiceInfo.getAppImageType(),
                                        appServiceInfo.getAppImageVersion(),
                                        ApplicationDataServiceCopyState.pending,
                                        null,
                                        applicationCopyCreatedEvent.getTimeStamp()
                                )));

        if (triggerDownstreamEvents) {
            // Applying Data service copy queued events for each ds involved
            applyAndAdd(
                    dataServiceCopies
                            .values()
                            .stream()
                            .map(ds -> new ApplicationCopyDataServiceQueuedEvent(
                                    id,
                                    appInstanceId,
                                    version + 1,
                                    stateTimestamp,
                                    null,
                                    ds.getDsbUrn(),
                                    ds.getDsbUrl(),
                                    ds.getServiceId()
                            ))
                            .collect(Collectors.toList())
            );

            // Applying App service copy queued events for each ds involved
            applyAndAdd(
                    appServiceCopies
                            .values()
                            .stream()
                            .map(appServiceCopy -> new ApplicationCopyAppServiceQueuedEvent(
                                    id,
                                    appInstanceId,
                                    version + 1,
                                    stateTimestamp,
                                    null,
                                    appServiceCopy.getAppServiceName()
                            ))
                            .collect(Collectors.toList())
            );
        }

    }

    public UUID getId() {
        return id;
    }

    public UUID getAppInstanceId() {
        return appInstanceId;
    }

    public Long getVersion() {
        return version;
    }

    public ApplicationCopyState getState() {
        return state;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public Date getStateTimestamp() {
        return stateTimestamp;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public Collection<ApplicationDataServiceCopy> getDataServiceCopies() {
        return new ArrayList<>(dataServiceCopies.values());
    }

    public ApplicationDataServiceCopy getDataServiceCopy(String dsbURN, String serviceId) {
        return dataServiceCopies.get(dsbURN + '-' + serviceId);
    }

    public ApplicationAppServiceCopy getAppServiceCopy(String appServiceName) {
        return appServiceCopies.get(appServiceName);
    }

    public Map<String, ApplicationAppServiceCopy> getAppServiceCopies() {
        return appServiceCopies;
    }

    private Collection<ApplicationCopyEvent> applyEvents(Collection<ApplicationCopyEvent> events) {
        return events.stream().map(this::applyEvent).collect(Collectors.toList());
    }

    private ApplicationCopyEvent applyEvent(ApplicationCopyEvent event) {
        this.version = event.getVersion();
        this.stateTimestamp = event.getTimeStamp();
        this.stateMessage = event.getMessage();

        final Class<? extends ApplicationCopyEvent> eventClass = event.getClass();
        log.debug("Processing event {}, {}", eventClass.getSimpleName(), event);
        try {
            final BiConsumer<ApplicationCopyEvent, Boolean> consumer = handlers.get(eventClass);
            if (consumer == null) {
                log.warn("Ignoring unsupported event {} {}", eventClass.getSimpleName(), event);
            } else {
                consumer.accept(event, false);
            }
            return event;
        } catch (Exception ex) {
            log.error("Failed Applying event " + eventClass.getSimpleName() + event, ex);
            //todo: rethrow? swallow?
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Rolls an event and returns the down-stream events that should be executed as a result
     */
    public Collection<ApplicationCopyEvent> rollEvent(ApplicationCopyEvent event) {
        this.version = event.getVersion();
        final Class<? extends ApplicationCopyEvent> eventClass = event.getClass();
        log.debug("Rolling event {}, {}", eventClass.getSimpleName(), event);
        try {
            final BiConsumer<ApplicationCopyEvent, Boolean> consumer = handlers.get(eventClass);
            if (consumer == null) {
                log.warn("Ignoring unsupported event {} {}", eventClass.getSimpleName(), event);
            } else {
                consumer.accept(event, true);
            }
            return getEventsSinceLoad();
        } catch (Exception ex) {
            log.error("Failed Processing event " + eventClass.getSimpleName() + event, ex);
            throw new IllegalStateException(ex);
        }
    }

    List<ApplicationCopyEvent> getEventsSinceLoad() {
        return eventsSinceLoad;
    }

    private void applyAndAdd(ApplicationCopyEvent applicationCopyEvent) {
        eventsSinceLoad.add(applyEvent(applicationCopyEvent));
    }

    private void applyAndAdd(Collection<ApplicationCopyEvent> applicationCopyEvents) {
        eventsSinceLoad.addAll(applyEvents(applicationCopyEvents));
    }

    public ApplicationCopyEvent getExecutionEvent() {
        return executionEvent;
    }

    void markAsClean() {
        eventsSinceLoad.clear();
        executionEvent = null;
    }

    private void setExecutionEvent(ApplicationCopyEvent executionEvent) {
        if (this.executionEvent != null) {
            throw new IllegalStateException("Unable to perform more than one operation on copy with id " + id +
                    ", please persist object after each operation");
        }
        this.executionEvent = applyEvent(executionEvent);
    }
}
