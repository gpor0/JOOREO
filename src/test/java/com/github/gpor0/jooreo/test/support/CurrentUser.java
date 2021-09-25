package com.github.gpor0.jooreo.test.support;

import com.github.gpor0.jooreo.RequestContextProxy;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Author: gpor0
 */
@ApplicationScoped
public class CurrentUser implements RequestContextProxy {

    @Override
    public UUID getId() {
        return UUID.randomUUID();
    }

    @Override
    public Set<String> getIamRoles() {
        return null;
    }

    @Override
    public boolean hasIamRole(String role) {
        return false;
    }

    @Override
    public Set<String> getScopes() {
        return null;
    }

    @Override
    public boolean hasScope(String scope) {
        return false;
    }

    @Override
    public Optional<String> language() {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> hasRel(String rel) {
        return Optional.empty();
    }

    @Override
    public String getBasePath() {
        return null;
    }

    @Override
    public String getCid() {
        return null;
    }
}
