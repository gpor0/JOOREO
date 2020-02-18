package com.github.gpor0.jooreo.test.support.records;

import com.github.gpor0.jooreo.Jooreo;
import com.github.gpor0.jooreo.annotations.OnInsertFilter;
import com.github.gpor0.jooreo.annotations.OneToMany;
import com.github.gpor0.jooreo.dao.records.tables.records.BaseReservationRecord;
import com.github.gpor0.jooreo.filters.AuditCreateFilter;

import java.util.List;

import static com.github.gpor0.jooreo.dao.records.Tables.CUSTOMER;
import static com.github.gpor0.jooreo.dao.records.Tables.RESERVATION_FLIGHT;

@OnInsertFilter(AuditCreateFilter.class)
public class ReservationRecord extends BaseReservationRecord {

    private CustomerRecord customer;

    @OneToMany
    private List<ReservationFlightRecord> flights;

    public List<ReservationFlightRecord> getFlights() {
        if (flights == null) {
            flights = getPersistedFlights();
        }

        return flights;
    }

    public void setFlights(List<ReservationFlightRecord> flights) {
        this.flights = flights;
    }

    //todo this should be generated
    protected List<ReservationFlightRecord> getPersistedFlights() {

        if (this.getId() == null) {
            return null;
        }

        return dsl.select()
                .from(RESERVATION_FLIGHT)
                .where(RESERVATION_FLIGHT.RESERVATION_ID.eq(this.getId()))
                .fetch(Jooreo.to(ReservationFlightRecord.class, dsl));

    }

    public CustomerRecord getCustomer() {
        if (customer == null) {
            customer = getPersistedCustomer();
        }

        return customer;
    }

    public void setCustomer(CustomerRecord customer) {
        this.customer = customer;
    }

    protected CustomerRecord getPersistedCustomer() {

        if (this.getId() == null) {
            return null;
        }

        //todo use mapper
        return dsl.select()
                .from(CUSTOMER)
                .where(CUSTOMER.ID.eq(this.getCustomerId()))
                .fetchOneInto(CustomerRecord.class);

    }
}
