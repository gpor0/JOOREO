package com.github.gpor0.jooreo.operations;

import com.github.gpor0.jooreo.exceptions.ParameterSyntaxException;

/**
 * Author: gpor0
 */
public class FilterOperation implements DataOperation {

    private String field;
    private String operation;
    private Object value;

    public FilterOperation(String field, String operation, Object value) {
        this.field = field;
        this.operation = operation;
        this.value = value;
    }

    public static FilterOperation parse(String filter) {
        String[] filterStr = filter.split(":");

        if (filterStr.length < 2 || filterStr.length > 3) {
            throw new ParameterSyntaxException("filter", filter);
        }

        String field = filterStr[0];
        String op = filterStr[1];
        String val = null;

        if (filterStr.length > 2) {
            val = filterStr[2];
        }

        return new FilterOperation(field, op, val);
    }

    public String getField() {
        return field;
    }

    public String getOperation() {
        return operation;
    }

    public Object getValue() {
        return value;
    }
}
