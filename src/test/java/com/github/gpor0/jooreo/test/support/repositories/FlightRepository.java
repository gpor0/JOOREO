package com.github.gpor0.jooreo.test.support.repositories;

import com.github.gpor0.jooreo.dao.JooreoDao;
import com.github.gpor0.jooreo.test.support.records.FlightRecord;

import javax.enterprise.context.ApplicationScoped;

import static com.github.gpor0.jooreo.dao.records.Tables.FLIGHT;

/**
 * Author: gpor0
 */
@ApplicationScoped
public class FlightRepository extends JooreoDao<FlightRecord> {

    public FlightRecord getByCode(String code) {
        return dsl.select().from(table).where(FLIGHT.CODE.eq(code)).fetchOne(toRecord());
    }
}
