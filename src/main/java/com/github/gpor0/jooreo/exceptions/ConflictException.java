package com.github.gpor0.jooreo.exceptions;

public class ConflictException extends ManagedException {

    private String faultCode;
    private String field;
    private Object value;
    private Object[] fieldValues;

    public ConflictException(String faultCode, String field, Object value, Object... fieldVal) {
        super(faultCode);
        this.faultCode = faultCode;
        this.field = field;
        this.value = value;
        this.fieldValues = fieldVal;
    }

    public String getFaultCode() {
        return faultCode;
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
