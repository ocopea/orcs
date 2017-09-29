// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.auth;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Created by liebea on 8/4/16.
 * Drink responsibly
 */
public class AuthUser implements Principal, Serializable {
    private final UUID id;
    private final String userName;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String email;

    public AuthUser(UUID id, String userName, String password, String firstName, String lastName, String email) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }

    public Set<String> getRoles() {
        return Collections.singleton("admin");
    }

    public UUID getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }
}


