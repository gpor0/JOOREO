package com.github.gpor0.jooreo.exceptions;

import java.util.UUID;

/**
 * Author: gpor0
 */
public class ResourceNotFoundException extends HandledException {

    private String id;
    private String resourceName;

    public ResourceNotFoundException(String resourceName, String id) {
        super("Resource " + resourceName + " with id " + id + " not found");
        this.resourceName = resourceName;
        this.id = id;
    }

    public ResourceNotFoundException(String resourceName, UUID id) {
        this(resourceName, String.valueOf(id));
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        this(resourceName, String.valueOf(id));
    }

    public String getId() {
        return id;
    }

    public String getResourceName() {
        return resourceName;
    }
}
