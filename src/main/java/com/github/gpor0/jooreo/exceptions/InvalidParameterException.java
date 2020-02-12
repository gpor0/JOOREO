package com.github.gpor0.jooreo.exceptions;

public class InvalidParameterException extends IllegalArgumentException {

    private String field;
    private Object value;

    private Object[] fieldValues;

    public InvalidParameterException(String field, Object value, Object... fieldVal) {
        super("Invalid value \"" + value + "\" for field " + field + " " + (fieldVal != null ? fieldVal : ""));
        this.field = field;
        this.value = value;
        this.fieldValues = fieldVal;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public Object[] getFieldValues() {
        return fieldValues;
    }
}
