// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.util.function.Supplier;

/**
 * Created by liebea on 3/26/17.
 * Drink responsibly
 */
public abstract class HubWebAppUtil {
    private static final Logger log = LoggerFactory.getLogger(HubWebAppUtil.class);

    private HubWebAppUtil() {
        // Static util class
    }

    /**
     * Wraps a function call with web friendly exceptions
     */
    public static void wrap(String callDescription, Runnable funcCall) {
        wrap(callDescription, () -> {
                    funcCall.run();
                    return null;
                }
        );
    }

    /**
     * Wraps a function call with web friendly exceptions
     */
    public static <T> T wrap(String callDescription, Supplier<T> funcCall) {
        try {
            log.debug(callDescription);
            return funcCall.get();
        } catch (ClientErrorException ce) {
            throw ce;
        } catch (Exception ex) {
            throw new InternalServerErrorException("Failed " + callDescription + " - " + ex.getMessage(), ex);
        }
    }

    /**
     * Wraps a function call with web friendly exceptions. if the call returns null throwing a "NotFound" exception
     * which translates to 404 http return code
     */
    public static <T> T wrapMandatory(String callDescription, Supplier<T> funcCall) {
        final T retVal = wrap(callDescription, funcCall);
        if (retVal == null) {
            throw new NotFoundException(callDescription + " not found");
        }
        return retVal;
    }
}
