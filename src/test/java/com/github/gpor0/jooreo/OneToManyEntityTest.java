package com.github.gpor0.jooreo;

import com.github.gpor0.jooreo.operations.DataOperation;
import com.github.gpor0.jooreo.operations.FilterOperation;
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

import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Author: gpor0
 */
@EnableWeld
public class OneToManyEntityTest extends JooreoTest {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    ReservationFlightRepository reservationFlightRepository;

    @Inject
    FlightRepository flightRepository;

    @Test
    public void shouldReturnFilteredResults() {

        populate("Lauren", "May", "R192", "EK318", "EK319", "76A", "76B");
        populate("Marsha", "May", "R193", "EK318", "EK312", "75C", "75D");

        //todo support oneToOne
        DataOperation filter = new FilterOperation("code", "EQ", "R192");
        //get reservation
        Queried<ReservationRecord> reservations = reservationRepository.getPaginatedByOperations(null, null, filter);

        assertNotNull(reservations, "Returned query result should not be null");
        assertEquals(1L, reservations.getTotalCount(), "Total count of filtered result should match");
        List<ReservationRecord> reservationRecords = reservations.stream().collect(Collectors.toList());
        assertEquals(2, reservationRecords.get(0).getFlights().size(), "Returned count of children does not match persisted state");
    }

    @Test
    public void shouldReturnAllChildren() {

        UUID reservationId = populate("Lauren", "May", "R192", "EK318", "EK319", "76A", "76B");
        populate("Marsha", "May", "R193", "EK318", "EK312", "75C", "75D");

        //get reservation
        ReservationRecord reservation = reservationRepository.getById(reservationId);

        assertNotNull(reservation, "Returned reservation result should not be null");
        assertNotNull(reservation.getCustomer(), "Returned customer result should not be null");
        assertEquals("Lauren", reservation.getCustomer().getFirstName(), "Returned customer firstName result should match");
        assertNotNull(reservation.getFlights(), "Returned flights result should not be null");
        assertEquals(2, reservation.getFlights().size(), "Returned flights count should match");

        FlightRecord laureensFlight =
                reservation.getFlights().stream().map(ReservationFlightRecord::getFlight).filter(f -> "EK319".equals(f.getCode())).findFirst().orElse(null);
        assertNotNull(laureensFlight, "Laureens flight not returned");

        FlightRecord marshasFlight =
                reservation.getFlights().stream().map(ReservationFlightRecord::getFlight).filter(f -> "EK319".equals(f.getCode())).findFirst().orElse(null);
        assertNotNull(marshasFlight, "Marshas flight not returned");
    }

    @Test
    public void shouldReturnRowsWithChildrenFilter() {

        populate("Lauren", "May", "R192", "EK318", "EK319", "76A", "76B");

        DataOperation childFilter = new FilterOperation("flights.seat", "EQ", "76A");
        //get reservation
        Queried<ReservationRecord> reservations = reservationRepository.getPaginatedByOperations(null, null, childFilter);

        assertNotNull(reservations, "Returned query result should not be null");
        assertEquals(1L, reservations.getTotalCount(), "Total count of filtered result should match");
        List<ReservationRecord> reservationRecords = reservations.stream().collect(Collectors.toList());
        assertEquals(2, reservationRecords.get(0).getFlights().size(), "Returned count of children does not match persisted state");
    }

    @Test
    public void shouldReturnRowsWithRecursiveFilter() {

        //todo support
        DataOperation childFilter = new FilterOperation("flights.flight.code", "EQ", "EK319");
    }

    private UUID populate(String firstName, String lastName, String reservationCode, String firstCode, String secondCode, String firstSeat,
                          String secondSeat) {
        CustomerRecord customer = new CustomerRecord();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customerRepository.create(customer);

        ReservationRecord reservationRecord = new ReservationRecord();
        reservationRecord.setCustomerId(customer.getId());
        reservationRecord.setCode(reservationCode);
        reservationRepository.create(reservationRecord);

        FlightRecord flightRecord = flightRepository.getByCode(secondCode);
        if (flightRecord == null) {
            flightRecord = new FlightRecord();
            flightRecord.setCode(firstCode);
            flightRepository.create(flightRecord);
        }

        FlightRecord flightRecord2 = flightRepository.getByCode(secondCode);
        if (flightRecord2 == null) {
            flightRecord2 = new FlightRecord();
            flightRecord2.setCode(secondCode);
            flightRepository.create(flightRecord2);
        }

        ReservationFlightRecord reservationFlightRecord = new ReservationFlightRecord();
        reservationFlightRecord.setReservationId(reservationRecord.getId());
        reservationFlightRecord.setSeat(firstSeat);
        reservationFlightRecord.setFlightId(flightRecord.getId());
        reservationFlightRepository.create(reservationFlightRecord);

        ReservationFlightRecord reservationFlightRecord2 = new ReservationFlightRecord();
        reservationFlightRecord2.setReservationId(reservationRecord.getId());
        reservationFlightRecord2.setSeat(secondSeat);
        reservationFlightRecord2.setFlightId(flightRecord2.getId());
        reservationFlightRepository.create(reservationFlightRecord2);

        return reservationRecord.getId();
    }

}
