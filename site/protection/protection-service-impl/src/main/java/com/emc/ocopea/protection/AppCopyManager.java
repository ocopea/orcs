// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.schedule.ManagedScheduler;
import com.emc.microservice.schedule.ScheduleListener;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AppCopyManager implements ServiceLifecycle, Consumer<ApplicationCopyEvent> {
    private static final Logger log = LoggerFactory.getLogger(AppCopyManager.class);
    private ApplicationCopyLoader applicationCopyLoader;
    private AppCopyCreatorService appCopyCreatorService;

    private MessageSender workQueueMessageSender;
    private ManagedScheduler scheduler;
    private static final long DAY = 24L * 60 * 60 * 1000;
    private static final long WEEK = 7 * DAY;
    private static final long MONTH = 4 * WEEK;

    private static UUID createAppCopy(
            ApplicationCopyBuilder copyBuilder,
            AppCopyCreatorService appCopyCreatorService) {

        return appCopyCreatorService.create(copyBuilder).getId();

    }

    /**
     * Returns a list of app instance copies within the half open dates interval [intervalStart, intervalEnd).
     *
     * @param appInstanceId app instance id
     * @param intervalStart filter copy time start (use -1 for no limit)
     * @param intervalEnd filter copy time end (use -1 for no limit)
     *
     * @throws IllegalArgumentException if intervalEnd smaller than intervalStart
     */
    public List<ApplicationCopy> getAppInstanceCopies(UUID appInstanceId, long intervalStart, long intervalEnd) {

        if (intervalEnd < intervalStart) {
            throw new IllegalArgumentException(
                    "intervalStart=" + intervalStart + " is bigger than intervalEnd=" + intervalEnd);
        }
        //todo:push filter to db
        final Collection<ApplicationCopy> applicationCopies =
                applicationCopyLoader.listByAppInstanceId(appInstanceId);

        Date fromDate = new Date(intervalStart == -1 ? 0 : intervalStart);
        Date toDate = new Date(intervalEnd == -1 ? Long.MAX_VALUE : intervalEnd);

        return applicationCopies
                .stream()
                .filter(applicationCopy -> {
                    Date copyDate = applicationCopy.getTimeStamp();
                    return !fromDate.after(copyDate) && toDate.after(copyDate); // fromDate <= copyDate < toDate
                })
                .collect(Collectors.toList());
    }

    @Override
    public void init(Context context) {
        scheduler = context.getSchedulerManager().getManagedResourceByName("default");
        workQueueMessageSender = context.getDestinationManager().getManagedResourceByName("application-copy-events")
                .getMessageSender();
        ApplicationCopyEventRepository applicationCopyEventRepository = context.getDynamicJavaServicesManager()
                .getManagedResourceByName(ApplicationCopyEventRepository.class.getSimpleName()).getInstance();
        applicationCopyEventRepository.subscribe(this);
        applicationCopyLoader = new ApplicationCopyLoader(applicationCopyEventRepository);

        appCopyCreatorService = new AppCopyCreatorService(
                applicationCopyEventRepository,
                new AppCopyPersisterService(context.getDestinationManager()
                        .getManagedResourceByName("pending-application-copy-events").getMessageSender()));

    }

    @Override
    public void shutDown() {
        //todo?
        //scheduler.removeAll();
    }

    /**
     * Schedule app copies according to schedule.
     *
     * @param protectApplicationInstanceInfo protection info including app data and schedule frequency
     */
    public void schedule(final ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo) {
        log.info(
                "Applying schedule for appInstanceId {}, with period {}",
                protectApplicationInstanceInfo.getAppInstanceId(),
                protectApplicationInstanceInfo.getIntervalSeconds());

        //todo:nah, take copies only as part of the schedule...
        new Thread(() -> createAppCopy(createBuilder(protectApplicationInstanceInfo), appCopyCreatorService)).start();

        // Schedule protection
        scheduler.create(
                "protect " + protectApplicationInstanceInfo.getAppInstanceId(),
                protectApplicationInstanceInfo.getIntervalSeconds(),
                ProtectScheduleListener.SCHEDULE_LISTENER_IDENTIFIER,
                MapBuilder.<String, String>newHashMap()
                        .with("appInstanceId", protectApplicationInstanceInfo.getAppInstanceId())
                        .build(),
                ProtectApplicationInstanceInfoDTO.class,
                protectApplicationInstanceInfo);
    }

    private static ApplicationCopyBuilder createBuilder(
            ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo) {
        return createBuilder(protectApplicationInstanceInfo, new Date());
    }

    private static ApplicationCopyBuilder createBuilder(
            ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo, Date timestamp) {
        final ApplicationCopyBuilder copyBuilder =
                new ApplicationCopyBuilder(
                        UUID.fromString(protectApplicationInstanceInfo.getAppInstanceId()), timestamp);

        protectApplicationInstanceInfo.getBindings().forEach(b ->
                copyBuilder.withDataService(
                        b.getDsbUrn(),
                        b.getDsbUrl(),
                        b.getDataServiceId(),
                        b.getServiceBindName(),
                        b.getFacility(),
                        b.getDsbSettings()));

        protectApplicationInstanceInfo.getAppConfigurations().forEach(
                b -> copyBuilder.withAppService(b.getPsbURN(), b.getAppServiceName(),
                        b.getAppImageName(), b.getAppImageType(), b.getAppImageVersion()));
        return copyBuilder;
    }

    public UUID createSingleCopy(final ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo) {
        //todo:nah, take copies only as part of the schedule...
        return createAppCopy(createBuilder(protectApplicationInstanceInfo), appCopyCreatorService);

    }

    /***
     * Todo: this is a temp ugly solution for getting historical copies.
     * need to make this   work nicer with tempering persistence rather than this kind of  
     * @param protectApplicationInstanceInfo copy data to use
     */
    public void fakeHistoricalCopies(ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo) {
        Date now = new Date();
        Date thisWeek = new Date(now.getTime() - WEEK);
        Date lastQuarter = new Date(now.getTime() - 3 * MONTH);
        Date yesterday = new Date(now.getTime() - DAY + 1000 * 60 * 5);

        // First copy - 1 year from now
        Date currCopyTimeStamp = new Date(now.getTime() - 12 * MONTH);

        // Last year quarterly copy
        while (currCopyTimeStamp.before(lastQuarter)) {
            createAppCopy(createBuilder(protectApplicationInstanceInfo, currCopyTimeStamp), appCopyCreatorService);
            currCopyTimeStamp = new Date(currCopyTimeStamp.getTime() + 3 * MONTH);
        }
        // Last quarter, monthly copy
        while (currCopyTimeStamp.before(thisWeek)) {
            createAppCopy(createBuilder(protectApplicationInstanceInfo, currCopyTimeStamp), appCopyCreatorService);
            currCopyTimeStamp = new Date(currCopyTimeStamp.getTime() + WEEK);
        }

        // Last week daily copy
        while (currCopyTimeStamp.before(yesterday)) {
            createAppCopy(createBuilder(protectApplicationInstanceInfo, currCopyTimeStamp), appCopyCreatorService);
            currCopyTimeStamp = new Date(currCopyTimeStamp.getTime() + DAY);
        }
    }

    @Override
    public void accept(ApplicationCopyEvent applicationCopyEvent) {
        workQueueMessageSender.sendMessage(
                ApplicationCopyEvent.class,
                applicationCopyEvent,
                MapBuilder.<String, String>newHashMap()
                        .with("appInstanceId", applicationCopyEvent.getAppInstanceId().toString())
                        .with("appCopyId", applicationCopyEvent.getAppCopyId().toString())
                        .with("eventType", applicationCopyEvent.getClass().getSimpleName())
                        .build());

    }

    public static class ProtectScheduleListener implements ScheduleListener, ServiceLifecycle {
        private AppCopyCreatorService appCopyCreatorService;

        // Do not change this identifier as it may be persisted in deployed systems
        public static final String SCHEDULE_LISTENER_IDENTIFIER = "protect-schedule-listener";

        @Override
        public boolean onTick(Message message) {
            final ProtectApplicationInstanceInfoDTO protectApplicationInstanceInfo =
                    message.readObject(ProtectApplicationInstanceInfoDTO.class);
            createAppCopy(createBuilder(protectApplicationInstanceInfo), appCopyCreatorService);
            return true;
        }

        @Override
        public void init(Context context) {
            ApplicationCopyEventRepository applicationCopyEventRepository = context.getDynamicJavaServicesManager()
                    .getManagedResourceByName(ApplicationCopyEventRepository.class.getSimpleName()).getInstance();
            appCopyCreatorService = new AppCopyCreatorService(
                    applicationCopyEventRepository,
                    new AppCopyPersisterService(context.getDestinationManager()
                            .getManagedResourceByName("pending-application-copy-events").getMessageSender()));
        }

        @Override
        public void shutDown() {

        }
    }
}
