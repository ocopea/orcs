// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.commands;

import com.emc.ocopea.hub.HubWebAppUtil;

import javax.ws.rs.BadRequestException;

/**
 * Created by liebea on 10/5/16.
 * Drink responsibly
 */
public abstract class HubCommand<ArgsT, RetT> {
    private final String commandName;

    public HubCommand() {
        final String simpleName = getClass().getSimpleName();
        if (!simpleName.endsWith("Command")) {
            throw new IllegalStateException("Command class name must end with Command suffix");
        }
        this.commandName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1, simpleName.length() - 7);
    }

    public String getName() {
        return commandName;
    }

    // todo: change command to return entity instead of response, let the actual jax-rs elements handle jax-rs.

    /**
     * Executes the command, calling {@link #run(Object) run(ArgsT)}. Return value is always an actual response.
     *
     * @return the same as {@link #run(Object) run(ArgsT)}, unless that returns null and then an empty 'ok' response
     */
    public RetT execute(ArgsT args) {
        return HubWebAppUtil.wrap(getName(),() -> run(args));
    }

    // todo: add javadoc, detailing expected behaviour
    protected abstract RetT run(ArgsT args);

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
