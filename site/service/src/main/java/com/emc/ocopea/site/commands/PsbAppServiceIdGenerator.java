// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Generate unique, legal and human readable app service names
 */
public class PsbAppServiceIdGenerator {

    private PsbAppServiceIdGenerator() {
    }

    /***
     * Generate legal readable psbAppServiceIds for all app services of an application deployment
     * @param appInstanceName app instance name
     * @param appServiceNames app services names that belongs to this app
     * @param psbMaxAppIdLength psb specific appId max length definition
     * @return map of psbAppServiceIds by app service names
     */
    public static Map<String, String> generatePsbAppServiceIdsByAppSvcName(
            final String appInstanceName,
            final Set<String> appServiceNames,
            int psbMaxAppIdLength) {
        Map<String, String> psbAppServiceIds =
                new HashMap<>(appServiceNames.size());

        String timeSuffix = String.valueOf(System.currentTimeMillis());
        if (timeSuffix.length() > 11) {
            timeSuffix = timeSuffix.substring(timeSuffix.length() - 11);
        }

        // We leave characters to time-in-millis at the end of the name + 1 character for separator
        int charactersLeftToUse = psbMaxAppIdLength - timeSuffix.length() - 1;
        final String encodedAppInstanceName = encodeName(appInstanceName);
        final int appInstanceNameLength = encodedAppInstanceName.length();

        // The name will consist of the appInstance name - app service name escaped and chopped if needed
        for (String currAppServiceName : appServiceNames) {
            final String encodedAppServiceName = encodeName(currAppServiceName);
            if (appInstanceNameLength + encodedAppServiceName.length() > charactersLeftToUse) {

                // In case we have only one app service, then who cares - we'll simply chop the app service name suffix
                if (appServiceNames.size() == 1) {
                    final String singleServicePsbAppId = encodedAppInstanceName + "-" + encodedAppServiceName;

                    String appNameAndSvcName =
                            singleServicePsbAppId
                                    .substring(
                                            0,
                                            Math.min(
                                                    singleServicePsbAppId.length(),
                                                    psbMaxAppIdLength - timeSuffix.length()));
                    psbAppServiceIds.put(
                            currAppServiceName,
                            appNameAndSvcName + timeSuffix);
                } else {
                    // Now, we have more than one app service, so we need to chop names in a way it will still be unique
                    // We will use half the characters for the appInstanceName and half to the appSvcName
                    StringBuilder appInstanceNamePrefix = new StringBuilder(
                            encodedAppInstanceName
                                    .substring(
                                            0,
                                            Math.min(
                                                    encodedAppInstanceName.length(),
                                                    charactersLeftToUse / 2 + charactersLeftToUse % 2)
                                    ));

                    // Avoid double dash...
                    if (appInstanceNamePrefix.charAt(appInstanceNamePrefix.length() - 1) != '-') {
                        appInstanceNamePrefix.append('-');
                    }

                    // Now we'll append the appSvcName and make sure it is still unique
                    String psbAppServiceIdAttempt = appInstanceNamePrefix.append(
                            encodedAppServiceName
                                    .substring(
                                            0,
                                            Math.min(
                                                    charactersLeftToUse / 2,
                                                    encodedAppServiceName.length()))).toString();

                    // In case we have duplications, simply append a running index
                    int duplicateIdx = 1;
                    while (psbAppServiceIds.containsValue(psbAppServiceIdAttempt + timeSuffix)) {
                        psbAppServiceIdAttempt = psbAppServiceIdAttempt
                                .substring(0, psbAppServiceIdAttempt.length() - String.valueOf(duplicateIdx).length()) +
                                String.valueOf(duplicateIdx);
                        duplicateIdx++;
                    }

                    psbAppServiceIds.put(currAppServiceName, psbAppServiceIdAttempt + timeSuffix);
                }
            } else {
                psbAppServiceIds.put(
                        currAppServiceName,
                        encodedAppInstanceName + "-" +
                                encodedAppServiceName + timeSuffix);
            }
        }

        return psbAppServiceIds;
    }

    private static String encodeName(String name) {
        // todo: better encoding? no validation of cf name validations!
        return Normalizer.normalize(name.replaceAll(" ", "-"), Normalizer.Form.NFD);
    }
}
