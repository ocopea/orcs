// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.ocopea.site.app.AppServiceDeployedEvent;
import com.emc.ocopea.site.app.AppServiceStateChangeEvent;
import com.emc.ocopea.site.app.ApplicationDeployedSuccessfullyEvent;
import com.emc.ocopea.site.app.ApplicationFailedDeployingEvent;
import com.emc.ocopea.site.app.DataServiceBoundEvent;
import com.emc.ocopea.site.app.DataServiceStateChangeEvent;
import com.emc.ocopea.site.app.DeployedApplicationEvent;
import com.emc.ocopea.site.app.DeployedApplicationFailedStoppingEvent;
import com.emc.ocopea.site.app.DeployedApplicationStoppedEvent;
import com.emc.ocopea.site.app.DeployedApplicationStoppingEvent;
import com.emc.ocopea.site.app.PolicyStateChangeEvent;

import java.util.Collections;
import java.util.function.Function;

/**
 * This class translates between deployed application events to log messages sent to clients
 * In case no messages are required, returning null
 */
class DeployedApplicationEventLogMessageTranslator implements
                                                          Function<DeployedApplicationEvent, SiteLogMessageDTO> {

    @Override
    public SiteLogMessageDTO apply(DeployedApplicationEvent event) {
        if (event instanceof DataServiceStateChangeEvent) {
            return format((DataServiceStateChangeEvent) event);
        } else if (event instanceof AppServiceStateChangeEvent) {
            return format((AppServiceStateChangeEvent) event);
        } else if (event instanceof ApplicationDeployedSuccessfullyEvent) {
            return format((ApplicationDeployedSuccessfullyEvent) event);
        } else if (event instanceof ApplicationFailedDeployingEvent) {
            return format((ApplicationFailedDeployingEvent) event);
        } else if (event instanceof AppServiceDeployedEvent) {
            return format((AppServiceDeployedEvent) event);
        } else if (event instanceof DataServiceBoundEvent) {
            return format((DataServiceBoundEvent) event);
        } else if (event instanceof DeployedApplicationFailedStoppingEvent) {
            return format((DeployedApplicationFailedStoppingEvent) event);
        } else if (event instanceof DeployedApplicationStoppedEvent) {
            return format((DeployedApplicationStoppedEvent) event);
        } else if (event instanceof DeployedApplicationStoppingEvent) {
            return format((DeployedApplicationStoppingEvent) event);
        } else if (event instanceof PolicyStateChangeEvent) {
            return format((PolicyStateChangeEvent) event);
        } else {
            return null;
        }
    }

    private SiteLogMessageDTO format(ApplicationFailedDeployingEvent event) {
        String message = "Application failed deploying";
        if (event.getMessage() != null && !event.getMessage().isEmpty()) {
            message += " - " + event.getMessage();
        }
        return new SiteLogMessageDTO(
                message,
                event.getTimestamp().getTime(),
                SiteLogMessageDTO.MessageType.err,
                Collections.emptySet());
    }

    private SiteLogMessageDTO format(ApplicationDeployedSuccessfullyEvent event) {
        return new SiteLogMessageDTO(
                "Application deployed successfully!",
                event.getTimestamp().getTime(),
                SiteLogMessageDTO.MessageType.out,
                Collections.emptySet());
    }

    private SiteLogMessageDTO format(PolicyStateChangeEvent event) {
        SiteLogMessageDTO.MessageType messageType;

        switch (event.getState()) {
            case error:
                messageType = SiteLogMessageDTO.MessageType.err;
                break;
            default:
                messageType = SiteLogMessageDTO.MessageType.out;
        }

        return new SiteLogMessageDTO(
                "Policy " + event.getName() + " of type " + event.getType() + " is in state " + event.getState(),
                event.getTimestamp().getTime(),
                messageType,
                Collections.emptySet());
    }

    private SiteLogMessageDTO format(DeployedApplicationStoppingEvent event) {
        return new SiteLogMessageDTO(
                "App is stopping",
                event.getTimestamp().getTime(),
                SiteLogMessageDTO.MessageType.out,
                Collections.emptySet()
        );
    }

    private SiteLogMessageDTO format(DeployedApplicationStoppedEvent event) {
        return new SiteLogMessageDTO(
                "App has has been stopped",
                event.getTimestamp().getTime(),
                SiteLogMessageDTO.MessageType.out,
                Collections.emptySet()
        );
    }

    private SiteLogMessageDTO format(DeployedApplicationFailedStoppingEvent event) {
        return new SiteLogMessageDTO(
                "App has failed stopping",
                event.getTimestamp().getTime(),
                SiteLogMessageDTO.MessageType.err,
                Collections.emptySet()
        );
    }

    private SiteLogMessageDTO format(DataServiceBoundEvent event) {
        return new SiteLogMessageDTO(
                "Data service " + event.getBindName() + " has been bound successfully",
                event.getTimestamp().getTime(),
                SiteLogMessageDTO.MessageType.out,
                Collections.singleton(event.getBindName())
        );
    }

    private SiteLogMessageDTO format(AppServiceDeployedEvent event) {
        return new SiteLogMessageDTO(
                "App service " + event.getName() + " has been deployed successfully",
                event.getTimestamp().getTime(),
                SiteLogMessageDTO.MessageType.out,
                Collections.singleton(event.getName())
        );
    }

    private SiteLogMessageDTO format(DataServiceStateChangeEvent event) {
        SiteLogMessageDTO.MessageType messageType;

        switch (event.getState()) {
            case errorbinding:
            case errorcreating:
            case errorremoving:
            case errorunbinding:
                messageType = SiteLogMessageDTO.MessageType.err;
                break;
            default:
                messageType = SiteLogMessageDTO.MessageType.out;
        }

        return new SiteLogMessageDTO(
                "Data Service " + event.getBindName() + " is in state " + event.getState(),
                event.getTimestamp().getTime(),
                messageType,
                Collections.singleton(event.getBindName()));
    }

    private SiteLogMessageDTO format(AppServiceStateChangeEvent event) {
        SiteLogMessageDTO.MessageType messageType;

        switch (event.getState()) {
            case error:
            case errorstopping:
                messageType = SiteLogMessageDTO.MessageType.err;
                break;
            default:
                messageType = SiteLogMessageDTO.MessageType.out;
        }

        return new SiteLogMessageDTO(
                "App " + event.getName() + " is in state " + event.getState(),
                event.getTimestamp().getTime(),
                messageType,
                Collections.singleton(event.getName()));
    }
}
