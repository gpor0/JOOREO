package com.github.gpor0.jooreo;

/**
 * Author: gpor0
 * <p>
 * Interface to CDI managed bean. Must be provided by implementor
 */
public interface LoggedUser {

    <T> T getId();

}
