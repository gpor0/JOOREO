package com.github.gpor0.jooreo.test.support;

import com.github.gpor0.jooreo.LoggedUser;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Author: gpor0
 */
@ApplicationScoped
public class CurrentUser implements LoggedUser {

    @Override
    public <T> T getId() {
        return (T) UUID.randomUUID();
    }
}
