package com.github.gpor0.jooreo.test.support.records;

import com.github.gpor0.jooreo.Jooreo;
import com.github.gpor0.jooreo.annotations.OnInsertFilter;
import com.github.gpor0.jooreo.dao.record.JooreoRecord;
import com.github.gpor0.jooreo.dao.records.tables.records.BaseReservationFlightRecord;
import com.github.gpor0.jooreo.filters.AuditCreateFilter;

import static com.github.gpor0.jooreo.dao.records.Tables.FLIGHT;

@OnInsertFilter(AuditCreateFilter.class)
public class ReservationFlightRecord extends BaseReservationFlightRecord implements JooreoRecord {

    private FlightRecord flight;

    public FlightRecord getFlight() {
        if (flight == null) {
            flight = getPersistedFlight();
        }

        return flight;
    }

    public void setFlight(FlightRecord flight) {
        this.flight = flight;
    }

    //todo this should be generated
    protected FlightRecord getPersistedFlight() {

        if (this.getFlightId() == null) {
            return null;
        }

        return dsl.select()
                .from(FLIGHT)
                .where(FLIGHT.ID.eq(this.getFlightId()))
                .fetchOne(Jooreo.to(FlightRecord.class, dsl));

    }

}
