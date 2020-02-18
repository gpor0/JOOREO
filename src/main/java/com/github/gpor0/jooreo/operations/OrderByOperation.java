package com.github.gpor0.jooreo.operations;

import com.github.gpor0.jooreo.RestUtil;
import com.github.gpor0.jooreo.exceptions.ParameterSyntaxException;

/**
 * Author: gpor0
 */
public class OrderByOperation implements DataOperation {

    private String field;
    private boolean asc;

    public OrderByOperation(String field, boolean asc) {
        this.field = field;
        this.asc = asc;
    }

    public static OrderByOperation parse(String orderBy) {
        String[] fieldOrderStr = orderBy.split(" ");
        if (fieldOrderStr.length != 2) {
            throw new ParameterSyntaxException("order", orderBy);
        }

        //provides camelCase (API) to snake_case (db)
        return new OrderByOperation(RestUtil.camelToSnake(fieldOrderStr[0]),
                "asc".toLowerCase().equals(fieldOrderStr[1] == null ? null : fieldOrderStr[1].toLowerCase()));
    }

    public String getField() {
        return field;
    }

    public boolean isAsc() {
        return asc;
    }
}
