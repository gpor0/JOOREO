package com.github.gpor0.jooreo;

import com.github.gpor0.jooreo.test.support.JooreoTest;
import com.github.gpor0.jooreo.test.support.records.CustomerRecord;
import com.github.gpor0.jooreo.test.support.records.FlightRecord;
import com.github.gpor0.jooreo.test.support.records.ReservationFlightRecord;
import com.github.gpor0.jooreo.test.support.records.ReservationRecord;
import com.github.gpor0.jooreo.test.support.repositories.CustomerRepository;
import com.github.gpor0.jooreo.test.support.repositories.FlightRepository;
import com.github.gpor0.jooreo.test.support.repositories.ReservationFlightRepository;
import com.github.gpor0.jooreo.test.support.repositories.ReservationRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

/**
 * Author: gpor0
 */
@EnableWeld
public class RelationEntityTest extends JooreoTest {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    ReservationFlightRepository reservationFlightRepository;

    @Inject
    FlightRepository flightRepository;


    @Test
    public void shouldReturnChildren() {

        CustomerRecord customer = new CustomerRecord();
        customer.setFirstName("Lauren");
        customer.setLastName("May");
        customerRepository.create(customer);

        ReservationRecord reservationRecord = new ReservationRecord();
        reservationRecord.setCustomerId(customer.getId());
        reservationRecord.setCode("R192");
        reservationRepository.create(reservationRecord);

        FlightRecord flightRecord = new FlightRecord();
        flightRecord.setCode("EK130");
        flightRepository.create(flightRecord);

        ReservationFlightRecord reservationFlightRecord = new ReservationFlightRecord();
        reservationFlightRecord.setReservationId(reservationRecord.getId());
        reservationFlightRecord.setFlightId(flightRecord.getId());
        reservationFlightRepository.create(reservationFlightRecord);

        //get reservation
        ReservationRecord reservation = reservationRepository.getById(reservationRecord.getId());

        assert reservation != null;
        assert reservation.getCustomer() != null;
        assert reservation.getFlights() != null;
        assert reservation.getFlights().size() == 1;
    }

}
