package com.github.gpor0.jooreo.dao.record;

import org.jooq.DSLContext;

public interface ManagedRecord {

    void dsl(DSLContext dslContext);

}
