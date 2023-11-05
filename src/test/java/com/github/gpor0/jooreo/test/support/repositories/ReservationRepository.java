package com.github.gpor0.jooreo.test.support.repositories;

import com.github.gpor0.jooreo.dao.JooreoDao;
import com.github.gpor0.jooreo.test.support.records.ReservationRecord;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

import static com.github.gpor0.jooreo.dao.records.Tables.RESERVATION;

/**
 * Author: gpor0
 */
@ApplicationScoped
public class ReservationRepository extends JooreoDao<ReservationRecord> {

    public ReservationRecord getById(UUID id) {
        return dsl.select().from(table()).where(RESERVATION.ID.eq(id)).fetchOne(toRecord());
    }
}
