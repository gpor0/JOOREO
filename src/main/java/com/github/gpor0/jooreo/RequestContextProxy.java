package com.github.gpor0.jooreo;

import java.util.Optional;
import java.util.Set;

/**
 * Author: gpor0
 * <p>
 * Interface to CDI managed bean. Must be provided by implementor
 */
public interface RequestContextProxy {

    <T> T getId();

    Set<String> getRoles();

    boolean hasRole(String role);

    Optional<Boolean> hasRel(String rel);

    String getBasePath();

    String getCid();

}
