package com.github.gpor0.jooreo.test.support.records;

import com.github.gpor0.jooreo.annotations.OnInsertFilter;
import com.github.gpor0.jooreo.dao.record.JooreoRecord;
import com.github.gpor0.jooreo.dao.records.tables.records.BaseReservationFlightRecord;
import com.github.gpor0.jooreo.filters.AuditCreateFilter;
import org.jooq.DSLContext;

@OnInsertFilter(AuditCreateFilter.class)
public class ReservationFlightRecord extends BaseReservationFlightRecord implements JooreoRecord {

    DSLContext dsl;

    @Override
    public void dsl(DSLContext dsl) {
        this.dsl = dsl;
    }
}
