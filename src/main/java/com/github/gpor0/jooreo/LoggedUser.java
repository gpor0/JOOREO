package com.github.gpor0.jooreo;

/**
 * Interface to CDI managed bean. Must be provided by implementor
 */
public interface LoggedUser {

    <T> T getId();

}
