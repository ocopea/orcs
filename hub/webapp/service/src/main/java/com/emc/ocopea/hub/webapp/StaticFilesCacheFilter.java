// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import org.jboss.resteasy.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.RedirectionException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A filter that enables client side caching of static resources.
 * // TODO: change caching mechanism to use Entity Tags instead of dates
 */
public class StaticFilesCacheFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger log = LoggerFactory.getLogger(StaticFilesCacheFilter.class);

    // format and parse again to avoid errors related to formatted granularity
    private static final Date startDate = DateUtil.parseDate(DateUtil.formatDate(new Date()));

    private List<Pattern> whiteListPatterns = Collections.singletonList(Pattern.compile("/html/.*"));
    // index.html black listed because otherwise chrome downloads it instead of opening
    private List<Pattern> blackListPatterns = Collections.singletonList(Pattern.compile(".*/index.html"));

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (isStaticFile(requestContext.getUriInfo().getPath())) {
            String ifModifiedSinceHeader = requestContext.getHeaderString("If-Modified-Since");
            boolean useCache =
                    ifModifiedSinceHeader != null && !DateUtil.parseDate(ifModifiedSinceHeader).before(startDate);
            log.debug("If-Modified-Since is {}, startDate is {}, use {}",
                    ifModifiedSinceHeader, DateUtil.formatDate(startDate), useCache ? "cache" : "new-response");
            if (useCache) {
                throw new RedirectionException(
                        Response.Status.NOT_MODIFIED,
                        requestContext.getUriInfo().getRequestUri());
            }
        }
    }

    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {
        if (isStaticFile(requestContext.getUriInfo().getPath())) {
            responseContext.getHeaders().add(HttpHeaders.LAST_MODIFIED, DateUtil.formatDate(startDate));
        }
    }

    private boolean isStaticFile(String path) {
        return whiteListPatterns.stream().anyMatch(pattern -> pattern.matcher(path).find())
                && blackListPatterns.stream().noneMatch(pattern -> pattern.matcher(path).find());
    }
}
