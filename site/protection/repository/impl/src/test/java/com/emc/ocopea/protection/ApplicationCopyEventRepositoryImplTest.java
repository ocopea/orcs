// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.dpa.dbjunit.UnitTestNativeQueryServiceImpl;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.microservice.testing.MockTestingResourceProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApplicationCopyEventRepositoryImplTest {

    private ApplicationCopyEventRepository appCopyEventRepo;
    private static long eventCounter = 0;

    private void assertEventsAreEqual(ApplicationCopyEvent a, ApplicationCopyEvent b) {
        Assert.assertEquals(a.getAppCopyId(), b.getAppCopyId());
        Assert.assertEquals(a.getAppInstanceId(), b.getAppInstanceId());
        Assert.assertEquals(a.getTimeStamp(), b.getTimeStamp());
        Assert.assertEquals(a.getMessage(), b.getMessage());
        Assert.assertEquals(a.getVersion(), b.getVersion());
    }

    @Before
    public void before() throws IOException, SQLException {
        ProtectionRepositorySchema schemaBootstrap = new ProtectionRepositorySchema();
        MicroServiceDataSource h2InMemoryTestDataSource = MockTestingResourceProvider.wrapDataSource(
                UnitTestNativeQueryServiceImpl.createH2InMemoryTestDataSource("protection_db", true));

        SchemaBootstrapRunner.runBootstrap(h2InMemoryTestDataSource, schemaBootstrap, schemaBootstrap.getSchemaName(),
                null);
        appCopyEventRepo = new ApplicationCopyEventRepositoryImpl(
                h2InMemoryTestDataSource);
    }

    @Test
    public void testStoreLoad() {
        UUID copyId = UUID.randomUUID();
        UUID instId = UUID.randomUUID();
        Date date = Date.from(Instant.now());
        String name = "my-app-service";
        String message = "a meassage";
        long version = 1L;
        ApplicationCopyEvent event =
                new ApplicationCopyAppServiceQueuedEvent(copyId, instId, version, date, message, name);

        appCopyEventRepo.store(event);
        ApplicationCopyEvent loadedEvent =
                appCopyEventRepo.listOrderedEvents(copyId).iterator().next();

        assertEventsAreEqual(event, loadedEvent);
        Assert.assertEquals(((ApplicationCopyAppServiceQueuedEvent) event).getAppServiceName(),
                ((ApplicationCopyAppServiceQueuedEvent) event).getAppServiceName());
    }

    @Test
    public void testTwoEventsForTheSameInstance() {
        UUID copyId1 = UUID.randomUUID();
        UUID copyId2 = UUID.randomUUID();
        UUID instId = UUID.randomUUID();
        ApplicationCopyEvent event1 = generateEvent(instId, copyId1);
        ApplicationCopyEvent event2 = generateEvent(instId, copyId2);

        appCopyEventRepo.store(Arrays.asList(event1, event2));
        Map<UUID, List<ApplicationCopyEvent>> copiesMap = appCopyEventRepo.listByAppInstanceId(instId);
        Assert.assertEquals(2, copiesMap.keySet().size());
    }

    @Test
    public void testNonExistentCopyId() {
        UUID copyId = UUID.randomUUID();
        Collection<ApplicationCopyEvent> events = appCopyEventRepo.listOrderedEvents(copyId);
        Assert.assertEquals(0, events.size());
    }

    @Test
    public void testNonExistentInstanceId() {
        UUID instId = UUID.randomUUID();
        Map<UUID, List<ApplicationCopyEvent>> events = appCopyEventRepo.listByAppInstanceId(instId);
        Assert.assertEquals(0, events.keySet().size());
    }

    @Test
    public void testSubscribeUnsubscribe() {
        final ApplicationCopyEvent[] consumedEvents = {null};
        UUID subscriberId = appCopyEventRepo.subscribe(event -> consumedEvents[0] = event);

        UUID copyId = UUID.randomUUID();
        UUID appInstanceId = UUID.randomUUID();
        ApplicationCopyEvent event1 = generateEvent(appInstanceId, copyId);

        appCopyEventRepo.store(event1); // will cause subscriber to handle the event and store it in consumedEvents[0]
        assertEventsAreEqual(event1, consumedEvents[0]);

        appCopyEventRepo.unSubscribe(subscriberId);
        ApplicationCopyEvent event2 = generateEvent(appInstanceId, copyId);
        appCopyEventRepo.store(event2);

        // consumedEvents[0] should not change, since we unsubscribed
        assertEventsAreEqual(event1, consumedEvents[0]);
    }

    @Test
    public void testListSortedEvents() {
        List<ApplicationCopyEvent> events = new ArrayList<>();
        UUID copyId = UUID.randomUUID();
        UUID appInstanceId = UUID.randomUUID();
        for (int i = 0; i < 10; i++) {
            events.add(generateEvent(appInstanceId, copyId));
        }

        Collections.shuffle(events);
        appCopyEventRepo.store(events);

        List<ApplicationCopyEvent> orderedEvents = appCopyEventRepo.listOrderedEvents(copyId);
        Assert.assertEquals(10, orderedEvents.size());

        // verify events are sorted by time, ascending
        Iterator<ApplicationCopyEvent> sortedEventsIterator = orderedEvents.iterator();
        ApplicationCopyEvent cur = sortedEventsIterator.next();
        while (sortedEventsIterator.hasNext()) {
            ApplicationCopyEvent next = sortedEventsIterator.next();
            Assert.assertTrue(cur.getVersion() <= next.getVersion());
            cur = next;
        }
    }

    private ApplicationCopyEvent generateEvent(UUID appInstanceId, UUID copyId) {
        //Date date = Date.from(Instant.ofEpochMilli(random.nextLong()));
        Date date = Date.from(Instant.now());
        String name = UUID.randomUUID().toString();
        String message = UUID.randomUUID().toString();
        long version = eventCounter;
        eventCounter += 1;
        return new ApplicationCopyAppServiceQueuedEvent(copyId, appInstanceId, version, date, message, name);
    }
}
