package com.github.gpor0.jooreo.exceptions;

public class MissingParameterException extends ManagedException {

    private String fieldName;

    public MissingParameterException(String fieldName) {
        super("Missing parameter " + fieldName);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
