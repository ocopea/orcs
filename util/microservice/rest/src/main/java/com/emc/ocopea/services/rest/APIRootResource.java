// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.ocopea.services.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author shresa
 */
@Path("/")
public class APIRootResource {

    private MicroServiceRootApplication rootApplication;

    @Context
    public void setApplication(Application application) {
        this.rootApplication = (MicroServiceRootApplication) application;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> listDeployedServices() {
        return rootApplication.getWebServer().listDeploymentURNs();
    }

    /**
     * Returns a thread dump of the entire JVM.
     */
    @GET
    @Path("threads")
    @Produces(MediaType.TEXT_PLAIN)
    public String threadDump() {
        return Arrays.stream(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true))
                .map(APIRootResource::threadInfoToString)
                .collect(
                        StringBuilder::new,
                        (builder, string) -> builder.append("\n").append(string),
                        (builder1, builder2) -> builder1.append("\n").append(builder2))
                .toString();
    }

    /**
     * This is a copy-paste of {@link ThreadInfo#toString()}, with the limitation on stack trace size dropped.
     */
    private static String threadInfoToString(ThreadInfo threadInfo) {
        StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" +
                " Id=" + threadInfo.getThreadId() + " " +
                threadInfo.getThreadState());
        if (threadInfo.getLockName() != null) {
            sb.append(" on " + threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"" + threadInfo.getLockOwnerName() +
                    "\" Id=" + threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        for (; i < stackTrace.length; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : lockedMonitors) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
        }
        if (i < stackTrace.length) {
            sb.append("\t...");
            sb.append('\n');
        }

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = " + locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- " + li);
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();

    }
}
