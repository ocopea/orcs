// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp.sankey;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.webapp.UIHubAppInstanceStatistics;
import com.emc.ocopea.hub.webapp.UISankeyColumn;
import com.emc.ocopea.hub.webapp.UISankeyInfo;

import java.util.Arrays;

/**
 * Created by liebea on 5/23/16.
 * Drink responsibly
 */
public abstract class DistributionSankeyHelper {
    // todo: add javadoc
    @NoJavadoc
    public static UISankeyInfo buildCopyDistributionSankey(UIHubAppInstanceStatistics appStats) {
        CopyDistributionSankeyTree t = new CopyDistributionSankeyTree(appStats.getAppInstanceName());
        addStats(appStats, t);
        return new UISankeyInfo(
                Arrays.asList(
                        new UISankeyColumn("From", "string"),
                        new UISankeyColumn("To", "string"),
                        new UISankeyColumn("Weight", "number")
                ), t.toSankeyFormat());
    }

    private static void addStats(UIHubAppInstanceStatistics appStats, CopyDistributionSankeyTree t) {
        // Adding offline backup copies
        appStats.getAppCopies()
                .stream()
                .flatMap(c -> c.getDataServiceCopies().stream())
                .forEach(c ->
                        t.addLeaf(
                                c.getSize(),
                                c.getCopyRepositoryName(),
                                "Offline Backup",
                                extractServiceName(c.getBindName(), c.getDsbUrn())));

        // Adding production statistics
        appStats.getProductionCopyStatistics().forEach(
                s -> t.addLeaf(
                        s.getSize(),
                        s.getStorageType(),
                        extractUseCaseNameFromDeploymentType(appStats.getDeploymentType()),
                        extractServiceName(s.getBindName(), s.getDsbName())));

        // Adding downstream statistics

        appStats.getDownStreamAppInstanceStats().values().forEach(i ->
                i.forEach(inst -> addStats(inst, t)));
    }

    private static String extractUseCaseNameFromDeploymentType(String deploymentType) {
        switch (deploymentType.toLowerCase()) {
            case "production":
                return "Primary Storage";
            default:
                //todo: stop this lunacy and have at least constants
                return "Test/Dev";
        }
    }

    private static String extractServiceName(String bindName, String dsbName) {
        return bindName + "(" + dsbName + ")";
    }
}
