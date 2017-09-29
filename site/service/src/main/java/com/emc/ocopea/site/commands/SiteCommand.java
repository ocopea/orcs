// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.commands;

import com.emc.ocopea.site.SiteCommandArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;

/**
 * Created by liebea on 1/12/16.
 * Drink responsibly
 */
public abstract class SiteCommand<ArgsT extends SiteCommandArgs, RetT> {
    private static final Logger log = LoggerFactory.getLogger(SiteCommand.class);
    private final String commandName;

    protected SiteCommand() {
        this.commandName = getClass().getSimpleName();
    }

    public String getCommandName() {
        return commandName;
    }

    protected abstract RetT run(ArgsT args);

    /***
     * Execute the command logic.
     * @param args command specific args
     * @return entity to be passed as response.
     */
    public RetT execute(ArgsT args) {
        try {
            return run(args);
        } catch (WebApplicationException ex) {
            String msg = getCommandName() + " failed " + ex.getMessage();
            log.error(msg, ex);
            throw ex;
        } catch (Exception ex) {
            String msg = getCommandName() + " failed " + ex.getMessage();
            log.error(msg, ex);
            throw new InternalServerErrorException(msg, ex);
        }
    }

    protected <T> T validateEmptyField(String fieldName, T value) {
        if (value == null) {
            throw new BadRequestException("Missing " + fieldName);
        }
        if (String.class.isAssignableFrom(value.getClass()) &&
                value.toString().trim().isEmpty()) {
            throw new BadRequestException(fieldName + " must not be empty");
        }

        return value;
    }

}
