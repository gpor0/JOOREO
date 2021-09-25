package com.github.gpor0.jooreo;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Author: gpor0
 * <p>
 * Interface to CDI managed bean. Must be provided by implementor
 */
public interface RequestContextProxy {

    UUID getId();

    Set<String> getIamRoles();

    boolean hasIamRole(String role);

    Set<String> getScopes();

    boolean hasScope(String scope);

    Optional<String> language();

    Optional<Boolean> hasRel(String rel);

    String getBasePath();

    String getCid();

}
