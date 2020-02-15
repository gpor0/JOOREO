package com.github.gpor0.jooreo.test.support.records;

import com.github.gpor0.jooreo.annotations.OnInsertFilter;
import com.github.gpor0.jooreo.dao.record.JooreoRecord;
import com.github.gpor0.jooreo.dao.records.tables.records.BaseFlightRecord;
import com.github.gpor0.jooreo.filters.AuditCreateFilter;
import org.jooq.DSLContext;

@OnInsertFilter(AuditCreateFilter.class)
public class FlightRecord extends BaseFlightRecord implements JooreoRecord {

    DSLContext dsl;

    @Override
    public void dsl(DSLContext dsl) {
        this.dsl = dsl;
    }
}
