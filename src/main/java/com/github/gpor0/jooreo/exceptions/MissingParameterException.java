package com.github.gpor0.jooreo.exceptions;

/**
 * Author: gpor0
 */
public class MissingParameterException extends HandledException {

    private String fieldName;

    public MissingParameterException(String fieldName) {
        super("Missing parameter " + fieldName);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
