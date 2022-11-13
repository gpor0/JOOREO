package com.github.gpor0.jooreo.converters;

import org.jooq.Converter;

public class BooleanConverter implements Converter<Byte, Boolean> {
    @Override
    public final Boolean from(Byte t) {
        return t != null && t == (byte) 1;
    }

    @Override
    public final Byte to(Boolean u) {
        return u != null && u ? (byte) 1 : (byte) 0;
    }

    @Override
    public Class<Byte> fromType() {
        return Byte.class;
    }

    @Override
    public Class<Boolean> toType() {
        return Boolean.class;
    }
}