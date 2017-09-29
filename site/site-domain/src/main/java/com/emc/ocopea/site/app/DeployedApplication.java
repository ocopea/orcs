// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.site.DeployApplicationOnSiteCommandArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Deployed Application object represents the Application Instance Aggregate current state.
 * The object can be loaded from the database in order to query the application instance current state and/or to update
 * the state by calling one of it's public methods and persisting it.
 * Since the deployed application state machine is implemented using event sourcing technique - each action is
 * interpreted as an event in the system. Each state update on the object requires persistence before invoking any
 * further state changing methods or will result in an exception.
 * The usage for example:
 * <p>
 * // Load from a persistent store
 * DeployedApplication da = [someService].load(appInstanceId);
 * <p>
 * // query and invoke 1 state changing method
 * if (da.this || da.that){
 * ...
 * }
 * ...
 * <p>
 * // State changing method
 * da.markDataServiceAsCreated("svc1", "created successfully");
 * <p>
 * // Store the changes
 * [someService].persist(da);
 */
public class DeployedApplication {
    private static final Logger log = LoggerFactory.getLogger(DeployedApplication.class);
    private final Map<Class<? extends DeployedApplicationEvent>, BiConsumer<DeployedApplicationEvent, Boolean>>
            handlers = new HashMap<>();
    private Long version = null;
    private UUID id;
    private String name;
    private String appTemplateName;
    private String appTemplateVersion;
    private Map<String, DeployedDataService> deployedDataServices;
    private Map<String, DeployedAppService> deployedAppServices;

    // appServiceName -> dsbURN -> dataSvcNames
    private Map<String, Map<String, Set<String>>> appServiceToDataServiceMappings;
    private String entryPointService;
    private DeployedApplicationState state = DeployedApplicationState.pending;
    private String stateMessage = null;
    private Date stateDate = new Date();
    private Map<String, DeployedDataServicesPolicy> dataServicesPoliciesByNameAndType;
    private String entryPointURL = null;
    private Date deployedOn = null;

    // This events list represents downstream events created as a result of rolling an event onto the state machine
    private final List<DeployedApplicationEvent> eventsSinceLoad = new ArrayList<>();

    // This event is the translation of user operation onto an event needs to be persisted to represent the user action
    private DeployedApplicationEvent executionEvent = null;

    public DeployedApplication(Collection<DeployedApplicationEvent> event) {
        this();
        applyEvents(event);
    }

    public DeployedApplication() {
        handlers.put(DeployedApplicationCreatedEvent.class, this::onCreate);
        handlers.put(DataServiceStateChangeEvent.class, this::onDataServiceStateChange);
        handlers.put(AppServiceStateChangeEvent.class, this::onAppServiceStateChange);
        handlers.put(AppServiceDeployedEvent.class, this::onAppServiceDeployed);
        handlers.put(ApplicationDeployedSuccessfullyEvent.class, this::onAppDeployedSuccessfully);
        handlers.put(DataServiceBoundEvent.class, this::onDataServiceBound);
        handlers.put(PolicyStateChangeEvent.class, this::onPolicyStateChange);
        handlers.put(ApplicationFailedDeployingEvent.class, this::onApplicationFailedDeployingEvent);
        handlers.put(DeployedApplicationStoppingEvent.class, this::onDeployedApplicationStoppingEvent);
        handlers.put(DeployedApplicationStoppedEvent.class, this::onDeployedApplicationStoppedEvent);
        handlers.put(DeployedApplicationFailedStoppingEvent.class, this::onDeployedApplicationFailedStoppingEvent);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAppTemplateName() {
        return appTemplateName;
    }

    public String getAppTemplateVersion() {
        return appTemplateVersion;
    }

    public Map<String, DeployedDataService> getDeployedDataServices() {
        return deployedDataServices;
    }

    public Map<String, DeployedAppService> getDeployedAppServices() {
        return deployedAppServices;
    }

    public Map<String, Map<String, Set<String>>> getAppServiceToDataServiceMappings() {
        return appServiceToDataServiceMappings;
    }

    /***
     * Given an application service name returns a list of deployed data services that are bound to that app.
     * If no data services are bound to the app returns an empty list.
     */
    public Collection<DeployedDataService> getBoundServices(String appServiceName) {
        // boundServices maps dsbURN -> dataSvcNames
        final Map<String, Set<String>> boundServices = getAppServiceToDataServiceMappings().get(appServiceName);
        if (boundServices == null) {
            return Collections.emptyList();
        } else {
            return boundServices
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .distinct()
                    .map(dsName -> deployedDataServices.get(dsName))
                    .collect(Collectors.toList());
        }
    }

    /***
     * Given a data service name, returning app services that are bound to it.
     */
    Collection<DeployedAppService> getDependentAppServices(String dataServiceName) {
        return getAppServiceToDataServiceMappings().entrySet()
                .stream()
                .filter(appNameToDSBEntry ->
                        appNameToDSBEntry.getValue().values()
                                .stream()
                                .flatMap(Collection::stream)
                                .anyMatch(s -> s.contains(dataServiceName)))
                .map(appSvcEntry -> deployedAppServices.get(appSvcEntry.getKey()))
                .collect(Collectors.toList());
    }

    public String getEntryPointService() {
        return entryPointService;
    }

    public DeployedApplicationState getState() {
        return state;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public Date getDeployedOn() {
        return deployedOn;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public String getEntryPointURL() {
        return entryPointURL;
    }

    public DeployedDataServicesPolicy getDataServicesPolicy(String name, String type) {
        return dataServicesPoliciesByNameAndType.get(type + "-" + name);
    }

    public Collection<DeployedDataServicesPolicy> getDataServicesPolicies() {
        return dataServicesPoliciesByNameAndType.values();
    }

    public DeployedApplication stop() {
        return stop(null);
    }

    /***
     * Stops an application by adding and applying an ApplicationStopping event to the application's events list,
     * and changing the status of all application services to "stopping".
     * If the application is not in the "running" state behaviour is not defined.
     * @param message - added to the ApplicationStopping event
     * @return this
     */
    public DeployedApplication stop(String message) {

        // todo: validate current app state! if it is not "running" we shouldn't be able to stop it!
        // Applying the "App Stopping event"
        final Date timestamp = new Date();
        setExecutionEvent(
                new DeployedApplicationStoppingEvent(this.id, version + 1, timestamp, message));

        return this;
    }

    private Set<String> findAppServiceNamesUsingDataService(String dataServiceName) {
        return appServiceToDataServiceMappings
                .entrySet()
                .stream()
                .filter(e ->
                        e.getValue()
                                .values()
                                .stream()
                                .flatMap(Collection::stream)
                                .anyMatch(Predicate.isEqual(dataServiceName)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private boolean isAllAppServicesBoundToServiceStopped(String dataServiceName) {
        return findAppServiceNamesUsingDataService(dataServiceName)
                .stream()
                .allMatch(
                        appServiceName ->
                                deployedAppServices.get(appServiceName).getState() == DeployedAppServiceState.stopped
                );
    }

    private void stopAllDataServicesThatAreNotBoundByRunningApps() {
        Date timestamp = new Date();
        long[] currVersion = {version};
        eventsSinceLoad.addAll(applyEvents(
                this.deployedDataServices.values()
                        .stream()
                        .filter(ds -> isAllAppServicesBoundToServiceStopped(ds.getBindName()))
                        .map(ds -> new DataServiceStateChangeEvent(this.id, ++currVersion[0],
                                timestamp, ds.getBindName(), DeployedDataServiceState.unbinding, null))
                        .collect(Collectors.toList())
        ));
    }

    /***
     * Creates a deployed application by add a ApplicationCreated event to the application's event list.
     * In addition, it queues:
     * 1. All deployed data services to be handled by deployers
     * 2. Policy changes events
     * 3. Application services that do not depend on data services and can be deployed
     */
    public DeployedApplication create(DeployedApplicationBuilder builder) {

        // Applying the "App Created event"
        setExecutionEvent(builder.build());

        return this;
    }

    private List<DeployedApplicationEvent> findAppsReadyForQueueing() {

        // If there are data service level policies that were not applied, apps should still wait
        final long[] currVersion = {this.version};
        if (dataServicesPoliciesByNameAndType.values().stream().allMatch(
                deployedDataServicesPolicy -> deployedDataServicesPolicy.getState() ==
                        DeployedDataServicesPolicyState.active)) {

            // Finding app services that either have no dependencies or all of it's DSBs are in "bound" state
            return this.deployedAppServices.entrySet()
                    .stream()
                    .filter(appSvcEntry ->
                                    appSvcEntry.getValue().getState() == DeployedAppServiceState.pending &&
                                            this
                                                    .getAppServiceToDataServiceMappings()
                                                    .getOrDefault(appSvcEntry.getKey(), Collections.emptyMap())
                                                    .values()
                                                    .stream()
                                                    .flatMap(Collection::stream)
                                                    .allMatch(dsb -> this.deployedDataServices.get(dsb).getState() ==
                                                            DeployedDataServiceState.bound)
                    // Mapping each such app service to a "Queued event"
                    ).map(appSvcEntry ->
                            new AppServiceStateChangeEvent(
                                    this.id,
                                    ++currVersion[0],
                                    new Date(),
                                    appSvcEntry.getKey(),
                                    DeployedAppServiceState.queued,
                                    null)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @NoJavadoc
    public DeployedApplication markDataServiceAsCreating(String bindName) {
        setExecutionEvent(
                new DataServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        bindName,
                        DeployedDataServiceState.creating,
                        null));
        return this;
    }

    public DeployedApplication markDataServiceAsCreated(String bindName) {
        return markDataServiceAsCreated(bindName, null);
    }

    @NoJavadoc
    public DeployedApplication markDataServiceAsCreated(String bindName, String message) {
        setExecutionEvent(
                new DataServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        bindName,
                        DeployedDataServiceState.created,
                        message));
        return this;
    }

    @NoJavadoc
    public DeployedApplication markDataServiceAsRemoving(String bindName) {
        setExecutionEvent(
                new DataServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        bindName,
                        DeployedDataServiceState.removing,
                        null));
        return this;
    }

    /***
     * Changes the state of a data service to "removed" by adding a StateChange event.
     */
    public DeployedApplication markDataServiceAsRemoved(String bindName) {
        setExecutionEvent(
                new DataServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        bindName,
                        DeployedDataServiceState.removed,
                        null));

        return this;
    }

    /***
     * Adds and applies a state change event to change the data service state to "errorremoving".
     * In addition, marks the entire application as "ErrorStopping".
     */
    public DeployedApplication markDataServiceAsErrorRemoving(String bindName, String message) {
        final Date timestamp = new Date();
        setExecutionEvent(
                new DataServiceStateChangeEvent(id, this.version + 1, timestamp, bindName,
                        DeployedDataServiceState.errorremoving, message));

        return this;
    }

    /***
     * Adds and applies a state change event to change the data service state to "errorunbinding".
     * In addition, marks the entire application as "ErrorStopping".
     */
    public DeployedApplication markDataServiceAsErrorUnBinding(String bindName, String message) {
        final Date timestamp = new Date();
        setExecutionEvent(
                new DataServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        timestamp,
                        bindName,
                        DeployedDataServiceState.errorunbinding,
                        message));

        return this;
    }

    @NoJavadoc
    public DeployedApplication markDataServiceAsUnbound(String bindName) {
        setExecutionEvent(
                new DataServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        bindName,
                        DeployedDataServiceState.unbound,
                        null));
        return this;
    }

    @NoJavadoc
    public DeployedApplication markDataServiceAsBinding(String bindName) {
        setExecutionEvent(
                new DataServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        bindName, DeployedDataServiceState.binding,
                        null));
        return this;
    }

    /***
     * Mark Data service as failed in the binding process.
     * This will result in failing the entire app deployment producing an ApplicationFailedDeployingEvent.
     * @param bindName data service name that failed
     * @param message error message describing the error leading to bind failure
     */
    public DeployedApplication markDataServiceAsErrorBinding(String bindName, String message) {

        setExecutionEvent(
                new DataServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        bindName,
                        DeployedDataServiceState.errorbinding,
                        message));
        return this;
    }

    /***
     * Mark Data service as failed in the creation process.
     * This will result in failing the entire app deployment producing an ApplicationFailedDeployingEvent.
     * @param bindName data service name that failed
     * @param message error message describing the error leading to creation failure
     */
    public DeployedApplication markDataServiceAsErrorCreating(String bindName, String message) {

        // mark the application as failed deploying too..
        setExecutionEvent(
                new DataServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        bindName,
                        DeployedDataServiceState.errorcreating,
                        message));
        return this;
    }

    /***
     * Mark policy as failed applying for the application. this will result in failing the entire app deployment.
     * Producing a ApplicationFailedDeployingEvent event.
     * @param policyName policy name that failed applying
     * @param policyType policy type
     * @param message error message describing the error leading to the policy failure
     */
    public DeployedApplication markPolicyAsError(String policyName, String policyType, String message) {
        setExecutionEvent(
                new PolicyStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        message,
                        policyName,
                        policyType,
                        DeployedDataServicesPolicyState.error));
        return this;

    }

    @NoJavadoc
    public DeployedApplication markPolicyAsActivating(String policyName, String policyType) {
        setExecutionEvent(
                new PolicyStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        null,
                        policyName,
                        policyType,
                        DeployedDataServicesPolicyState.activating));
        return this;
    }

    /***
     * Mark policy as successfully activated. this will allow further deployment of the app services
     * @param policyName policy name
     * @param policyType policy type
     */
    public DeployedApplication markPolicyAsActive(String policyName, String policyType) {
        // Adding the State change event
        setExecutionEvent(
                new PolicyStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        null,
                        policyName,
                        policyType,
                        DeployedDataServicesPolicyState.active));

        return this;
    }

    /***
     * Marking a data service as bound successfully. this will result in further deploying of dependent app services.
     * @param bindName service bind name
     * @param bindingInfo binding info including the result of the bind process
     */
    public DeployedApplication markDataServiceAsBound(String bindName, DataServiceBoundEvent.BindingInfo bindingInfo) {

        // Applying the service bound event to update the deployed service with binding info
        setExecutionEvent(
                new DataServiceBoundEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        null,
                        bindName,
                        bindingInfo));
        return this;
    }

    private List<DeployedApplicationEvent> findPoliciesReadyForQueueing() {
        if (areAllDataServicesBound()) {
            final long[] currVersion = {this.version};
            return dataServicesPoliciesByNameAndType
                    .values()
                    .stream()
                    .map(
                            deployedDataServicesPolicy ->
                                    new PolicyStateChangeEvent(
                                            id,
                                            ++currVersion[0],
                                            new Date(),
                                            null,
                                            deployedDataServicesPolicy.getName(),
                                            deployedDataServicesPolicy.getType(),
                                            DeployedDataServicesPolicyState.queued))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private boolean areAllDataServicesBound() {
        return deployedDataServices
                .values()
                .stream()
                .allMatch(
                        deployedDataService ->
                                deployedDataService.getState() == DeployedDataServiceState.bound);
    }

    private boolean areAllAppServicesDeployed() {
        return deployedAppServices
                .values()
                .stream()
                .allMatch(
                        deployedAppService ->
                                deployedAppService.getState() == DeployedAppServiceState.deployed);
    }

    @NoJavadoc
    public DeployedApplication markAppServiceAsDeploying(String appServiceName) {
        setExecutionEvent(
                new AppServiceStateChangeEvent(
                        id,
                        this.version + 1,
                        new Date(),
                        appServiceName,
                        DeployedAppServiceState.deploying,
                        null));
        return this;
    }

    /***
     * Mark App Service as deployed with error - this will result in events marking the entire
     * App as failed ApplicationFailedDeployingEvent.
     * @param appServiceName app service name that failed
     * @param message error message describing the failure reason
     */
    public DeployedApplication markAppServiceAsError(String appServiceName, String message) {
        // mark the application as failed deploying too..
        setExecutionEvent(
                new AppServiceStateChangeEvent(id, this.version + 1, new Date(), appServiceName,
                        DeployedAppServiceState.error, message));
        return this;

    }

    /***
     * Adds and applies a state change event to change the application service state to "stopped".
     * In addition, marks the entire application as "ErrorStopping".
     */
    public DeployedApplication markAppServiceAsStopped(String appServiceName) {

        // Applying the stopped event
        setExecutionEvent(
                new AppServiceStateChangeEvent(id, this.version + 1, new Date(), appServiceName,
                        DeployedAppServiceState.stopped, null));
        return this;
    }

    private void markDeployedAppAsStoppedIfAllComponentsAreReallyStoppedThankYou() {
        if (areAllAppServicesStopped() && areAllDataServicesStopped()) {
            eventsSinceLoad.add(applyEvent(new DeployedApplicationStoppedEvent(id, this.version + 1, new Date(),
                    null)));
        }
    }

    private boolean areAllDataServicesStopped() {
        return deployedDataServices.values().stream().allMatch(
                deployedDataService -> deployedDataService.getState() == DeployedDataServiceState.removed);
    }

    private boolean areAllAppServicesStopped() {
        return deployedAppServices.values()
                .stream()
                .allMatch(deployedAppService -> deployedAppService.getState() == DeployedAppServiceState.stopped);
    }

    /***
     * Adds and applies a state change event to change the application service state to "errorstopping".
     * In addition, marks the entire application as "ErrorStopping".
     */
    public DeployedApplication markAppServiceAsErrorStopping(String appServiceName, String message) {
        // Applying the error stopping event
        final Date timestamp = new Date();
        setExecutionEvent(
                new AppServiceStateChangeEvent(id, this.version + 1, timestamp, appServiceName,
                        DeployedAppServiceState.errorstopping, message));

        return this;
    }

    private void addDeployedAppAsErrorStoppingEvent(Date timestamp, String message) {
        eventsSinceLoad.add(applyEvent(
                new DeployedApplicationFailedStoppingEvent(
                        id,
                        this.version + 1,
                        timestamp,
                        message)));
    }

    /***
     * Adds and applies an "AppServiceDeployed" event and marks the entire app as deployed if all app services are
     * deployed.
     */
    public DeployedApplication markAppServiceAsDeployed(String appServiceName, String publicURL) {

        // Adding the appService deployed event
        setExecutionEvent(new AppServiceDeployedEvent(
                id,
                this.version + 1,
                new Date(),
                appServiceName,
                null,
                publicURL));
        return this;

    }

    private Collection<DeployedApplicationEvent> applyEvents(Collection<DeployedApplicationEvent> events) {
        return events.stream().map(this::applyEvent).collect(Collectors.toList());
    }

    private DeployedApplicationEvent applyEvent(DeployedApplicationEvent event) {
        this.version = event.getVersion();
        final Class<? extends DeployedApplicationEvent> eventClass = event.getClass();
        log.debug("Processing event {}, {}", eventClass.getSimpleName(), event);
        try {
            final BiConsumer<DeployedApplicationEvent, Boolean> consumer = handlers.get(eventClass);
            if (consumer == null) {
                log.warn("Ignoring unsupported event {} {}", eventClass.getSimpleName(), event);
            } else {
                consumer.accept(event, false);
            }
            return event;
        } catch (Exception ex) {
            log.error("Failed Applying event " + eventClass.getSimpleName() + event, ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Rolls an event and returns the down-stream events that should be executed as a result
     */
    public Collection<DeployedApplicationEvent> rollEvent(DeployedApplicationEvent event) {
        this.version = event.getVersion();
        final Class<? extends DeployedApplicationEvent> eventClass = event.getClass();
        log.debug("Rolling event {}, {}", eventClass.getSimpleName(), event);
        try {
            final BiConsumer<DeployedApplicationEvent, Boolean> consumer = handlers.get(eventClass);
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

    private void onDataServiceStateChange(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        DataServiceStateChangeEvent dataServiceStateChangeEvent = (DataServiceStateChangeEvent) event;
        final DeployedDataService ds = deployedDataServices.get(dataServiceStateChangeEvent.getBindName());
        if (ds == null) {
            log.warn("Data Service state change received for app {} with id {}, but no bind name {} had no match. " +
                    "event detail {}", name, id, dataServiceStateChangeEvent.getBindName(), event);
        } else {
            ds.setState(dataServiceStateChangeEvent.getState(), dataServiceStateChangeEvent.getMessage(),
                    dataServiceStateChangeEvent.getTimestamp());
        }

        if (triggerDownstreamEvents) {
            switch (dataServiceStateChangeEvent.getState()) {
                case errorbinding:
                    eventsSinceLoad.add(applyEvent(
                            new ApplicationFailedDeployingEvent(id, this.version + 2, new Date(),
                                    "Error binding data service " + dataServiceStateChangeEvent.getBindName())));
                    break;
                case errorcreating:
                    eventsSinceLoad.add(applyEvent(
                            new ApplicationFailedDeployingEvent(id, this.version + 2, new Date(),
                                    "Error creating data service " + dataServiceStateChangeEvent.getBindName())));
                    break;
                case errorunbinding:
                    addDeployedAppAsErrorStoppingEvent(
                            dataServiceStateChangeEvent.getTimestamp(),
                            "Error unbinding data service Service" + dataServiceStateChangeEvent.getBindName());
                    break;
                case errorremoving:
                    // Now need to mark the entire application as error stopping
                    addDeployedAppAsErrorStoppingEvent(
                            dataServiceStateChangeEvent.getTimestamp(),
                            "Error removing data service Service" + dataServiceStateChangeEvent.getBindName());
                    break;
                case removed:
                    // Now if all stopped need to mark entire app as stopped
                    markDeployedAppAsStoppedIfAllComponentsAreReallyStoppedThankYou();
                    break;
                case binding:
                case created:
                case creating:
                case bound:
                case queued:
                case unbound:
                case unbinding:
                case pending:
                case removing:
                default:
                    break;
            }

        }
    }

    private void onDataServiceBound(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        DataServiceBoundEvent dataServiceBoundEvent = (DataServiceBoundEvent) event;
        final DeployedDataService ds = deployedDataServices.get(dataServiceBoundEvent.getBindName());
        if (ds == null) {
            log.warn("Data Service binding received for app {} with id {}, but no bind name {} had no match. event " +
                    "detail {}", name, id, dataServiceBoundEvent.getBindName(), event);
        } else {

            ds.bind(new DeployedDataService.DeployedDataServiceBindings(
                    dataServiceBoundEvent.getBindingInfo().getBindInfo(),
                    dataServiceBoundEvent.getBindingInfo().getPorts()
                            .stream()
                            .map(deployedDataServicePort ->
                                    new DeployedDataService.DeployedDataServicePort(
                                            deployedDataServicePort.getProtocol(),
                                            deployedDataServicePort.getDestination(),
                                            deployedDataServicePort.getPort()))
                            .collect(Collectors.toList())), dataServiceBoundEvent.getTimestamp());
        }

        if (triggerDownstreamEvents) {
            // When services are bound we can queue data service level policies
            eventsSinceLoad.addAll(applyEvents(findPoliciesReadyForQueueing()));

            // When services are bound we can queue app services that all of their dependencies are satisfied
            eventsSinceLoad.addAll(applyEvents(findAppsReadyForQueueing()));
        }
    }

    private void onAppDeployedSuccessfully(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        //todo: validation? test that everything is deployed?
        ApplicationDeployedSuccessfullyEvent applicationDeployedSuccessfullyEvent =
                (ApplicationDeployedSuccessfullyEvent) event;

        this.entryPointURL = applicationDeployedSuccessfullyEvent.getEntryPointURL();
        this.state = DeployedApplicationState.running;
        this.stateMessage = event.getMessage();
        this.stateDate = event.getTimestamp();

    }

    private void onDeployedApplicationStoppedEvent(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        this.state = DeployedApplicationState.stopped;
        this.stateMessage = event.getMessage();
        this.stateDate = event.getTimestamp();
    }

    private void onDeployedApplicationStoppingEvent(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        this.state = DeployedApplicationState.stopping;
        this.stateDate = event.getTimestamp();
        this.stateMessage = event.getMessage();

        if (triggerDownstreamEvents) {

            final long[] currVersion = {version};

            // Stopping all apps first
            eventsSinceLoad.addAll(applyEvents(
                    this.deployedAppServices.values()
                            .stream()
                            .map(a -> new AppServiceStateChangeEvent(this.id, ++currVersion[0], event.getTimestamp(),
                                    a.getAppServiceName(), DeployedAppServiceState.stopping, null)
                            )
                            .collect(Collectors.toList())

            ));

            // When stopping the application, first stopping the apps, then the services, however in cases there are
            // only services, and no apps, need to check..
            stopAllDataServicesThatAreNotBoundByRunningApps();

            // In the rare probably non-existent  case of app that doesn't have any components at all,
            // just verifying and marking as stopped
            markDeployedAppAsStoppedIfAllComponentsAreReallyStoppedThankYou();

        }
    }

    private void onAppServiceStateChange(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        AppServiceStateChangeEvent appServiceStateChangeEvent = (AppServiceStateChangeEvent) event;
        final DeployedAppService as = deployedAppServices.get(appServiceStateChangeEvent.getName());
        if (as == null) {
            log.warn("App Service state change received for app {} with id {}, but service name {} had no match. " +
                    "event detail {}", name, id, appServiceStateChangeEvent.getName(), event);
        } else {
            as.setState(appServiceStateChangeEvent.getState(), appServiceStateChangeEvent.getMessage(),
                    appServiceStateChangeEvent.getTimestamp());
        }

        if (triggerDownstreamEvents) {
            switch (appServiceStateChangeEvent.getState()) {
                case error:
                    eventsSinceLoad.add(applyEvent(
                            new ApplicationFailedDeployingEvent(id, this.version + 2, new Date(),
                                    "Error deploying " + appServiceStateChangeEvent.getName())));
                    break;
                case errorstopping:
                    // Now need to mark the entire application as error stopping
                    addDeployedAppAsErrorStoppingEvent(
                            appServiceStateChangeEvent.getTimestamp(),
                            "Error stopping app Service " + appServiceStateChangeEvent.getName());
                    break;
                case stopped:
                    // Not if this app Service resulted in stopped need to start stopping all data services that
                    // were bound by this app service
                    stopAllDataServicesThatAreNotBoundByRunningApps();

                    // In case there are no data services at all, simply marking entire app as stopped
                    markDeployedAppAsStoppedIfAllComponentsAreReallyStoppedThankYou();

                    break;
                case deploying:
                case pending:
                case queued:
                case stopping:
                case deployed:
                default:
                    break;
            }
        }
    }

    private void onPolicyStateChange(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        PolicyStateChangeEvent policyStateChangeEvent = (PolicyStateChangeEvent) event;
        final DeployedDataServicesPolicy policy = getDataServicesPolicy(
                policyStateChangeEvent.getName(),
                policyStateChangeEvent.getType());

        if (policy == null) {
            log.warn("Policy Service state change received for app {} with id {}, but policy type/name {}/{} had " +
                            "no match. event detail {}", name, id, policyStateChangeEvent.getType(),
                    policyStateChangeEvent.getName(), event);
            return;
        } else {
            policy.setState(policyStateChangeEvent.getState(), policyStateChangeEvent.getMessage(),
                    policyStateChangeEvent.getTimestamp());
        }

        if (triggerDownstreamEvents) {
            switch (policy.getState()) {
                case active:
                    // When policies become active app services might start running, giving them a chance..
                    eventsSinceLoad.addAll(applyEvents(findAppsReadyForQueueing()));
                    break;
                case error:
                    // mark the application as failed deploying too..
                    eventsSinceLoad.add(applyEvent(
                            new ApplicationFailedDeployingEvent(
                                    id,
                                    this.version + 1,
                                    new Date(),
                                    "Error activating policy " + policyStateChangeEvent.getType() + "/" +
                                            policyStateChangeEvent.getName())
                    ));
                    break;
                case activating:
                case pending:
                case queued:
                default:
                    break;
            }
        }
    }

    private void onAppServiceDeployed(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        AppServiceDeployedEvent appServiceDeployedEvent = (AppServiceDeployedEvent) event;
        final DeployedAppService as = deployedAppServices.get(appServiceDeployedEvent.getName());
        if (as == null) {
            log.warn("App Service state change received for app {} with id {}, but service name {} had no match. " +
                    "event detail {}", name, id, appServiceDeployedEvent.getName(), event);
        } else {
            as.setState(DeployedAppServiceState.deployed, appServiceDeployedEvent.getMessage(),
                    appServiceDeployedEvent.getTimestamp());
            as.setPublicURL(appServiceDeployedEvent.getUrlIfPublic());
        }

        if (triggerDownstreamEvents) {
            // Checking whether we can mark the entire app as deployed (yey?)
            if (areAllAppServicesDeployed()) {
                String rootURL = null;
                if (entryPointService != null) {
                    final DeployedAppService entryPointService = deployedAppServices.get(this.entryPointService);
                    if (entryPointService == null) {
                        eventsSinceLoad.add(
                                applyEvent(
                                        new ApplicationFailedDeployingEvent(
                                                id,
                                                this.version + 1,
                                                new Date(),
                                                "failed locating entry point service " + this.entryPointService)));
                        return;
                    }
                    rootURL = entryPointService.getPublicURL();
                }

                if (this.state != DeployedApplicationState.running) {
                    eventsSinceLoad.add(applyEvent(new ApplicationDeployedSuccessfullyEvent(
                            id,
                            this.version + 1,
                            new Date(),
                            "Oh yeah!",
                            rootURL)));
                }
            }
        }
    }

    private void onApplicationFailedDeployingEvent(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        // Booo
        this.state = DeployedApplicationState.error;
        this.stateMessage = event.getMessage();
        this.stateDate = event.getTimestamp();
    }

    private void onDeployedApplicationFailedStoppingEvent(
            DeployedApplicationEvent event,
            Boolean triggerDownstreamEvents) {
        // Booo
        this.state = DeployedApplicationState.errorstopping;
        this.stateMessage = event.getMessage();
        this.stateDate = event.getTimestamp();
    }

    private void onCreate(DeployedApplicationEvent event, Boolean triggerDownstreamEvents) {
        DeployedApplicationCreatedEvent deployedApplicationCreatedEvent = (DeployedApplicationCreatedEvent) event;
        this.deployedOn = deployedApplicationCreatedEvent.getTimestamp();
        this.stateDate = deployedApplicationCreatedEvent.getTimestamp();
        if (state != DeployedApplicationState.pending) {
            throw new IllegalStateException("Deployed App can't be created more than once");
        }
        this.state = DeployedApplicationState.deploying;

        this.id = deployedApplicationCreatedEvent.getAppInstanceId();
        this.name = deployedApplicationCreatedEvent.getName();
        this.appTemplateName = deployedApplicationCreatedEvent.getAppTemplateName();
        this.appTemplateVersion = deployedApplicationCreatedEvent.getAppTemplateVersion();
        this.deployedAppServices = deployedApplicationCreatedEvent.getDeployedAppServices().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        t -> new DeployedAppService(
                                t.getValue().getServiceName(),
                                t.getValue().getPsbUrn(),
                                t.getValue().getPsbAppServiceId(),
                                t.getValue().getArtifactRegistryName(),
                                t.getValue().getPsbUrl(),
                                t.getValue().getSpace(),
                                t.getValue().getImageName(),
                                t.getValue().getImageType(),
                                t.getValue().getImageVersion(),
                                t.getValue().getPsbSettings(),
                                t.getValue().getEnvironmentVariables(),
                                t.getValue().getExposedPorts(),
                                t.getValue().getHttpPort(),
                                t.getValue().getRoute())
                ));
        this.deployedDataServices = deployedApplicationCreatedEvent.getDeployedDataServices().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        t -> new DeployedDataService(
                                t.getValue().getDsbUrn(),
                                t.getValue().getDsbUrl(),
                                t.getValue().getPlan(),
                                t.getValue().getBindName(),
                                t.getValue().getServiceId(),
                                t.getValue().getDsbSettings(),
                                t.getValue().getRestoreInfo() == null ? null :
                                        new DeployApplicationOnSiteCommandArgs
                                                .DeployAppServiceOnSiteManifestDTO.DeployDataServiceRestoreInfoDTO(
                                                t.getValue().getRestoreInfo().getCopyRepoUrn(),
                                                t.getValue().getRestoreInfo().getCopyRepoProtocol(),
                                                t.getValue().getRestoreInfo().getCopyRepoProtocolVersion(),
                                                t.getValue().getRestoreInfo().getCopyId(),
                                                t.getValue().getRestoreInfo().getRestoreFacility()
                                        ))
                ));

        this.dataServicesPoliciesByNameAndType = deployedApplicationCreatedEvent.getDataServicePolicies()
                .stream()
                .collect(
                        Collectors.toMap(o -> o.getPolicyType() + "-" + o.getPolicyName(), p ->
                                new DeployedDataServicesPolicy(
                                        p.getPolicyType(),
                                        p.getPolicyName(),
                                        p.getPolicySettings())));

        this.appServiceToDataServiceMappings = deployedApplicationCreatedEvent.getAppServiceToDataServiceMappings();
        entryPointService = deployedApplicationCreatedEvent.getEntryPointService();

        if (triggerDownstreamEvents) {
            final long[] currVersion = {version};

            // Create a "queued" event for every data service so that deployers can start working
            eventsSinceLoad.addAll(applyEvents(this.deployedDataServices
                    .values()
                    .stream()
                    .map(deployedDataService ->
                            new DataServiceStateChangeEvent(
                                    this.id,
                                    ++currVersion[0],
                                    new Date(),
                                    deployedDataService.getBindName(),
                                    DeployedDataServiceState.queued,
                                    null))
                    .collect(Collectors.toList())));

            // When services are bound we can queue data service level policies
            eventsSinceLoad.addAll(applyEvents(findPoliciesReadyForQueueing()));

            // find application service that can deploy (Not dependent on any Data Service) and create a "queued" event
            eventsSinceLoad.addAll(applyEvents(findAppsReadyForQueueing()));
        }
    }

    List<DeployedApplicationEvent> getEventsSinceLoad() {
        if (eventsSinceLoad.isEmpty()) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(eventsSinceLoad);
        }
    }

    public DeployedApplicationEvent getExecutionEvent() {
        return executionEvent;
    }

    void markAsClean() {
        eventsSinceLoad.clear();
        executionEvent = null;
    }

    public void setExecutionEvent(DeployedApplicationEvent executionEvent) {
        if (this.executionEvent != null) {
            throw new IllegalStateException("Unable to perform more than one operation on the " + name +
                    " application, please persist object after each operation");
        }
        this.executionEvent = applyEvent(executionEvent);
    }
}
