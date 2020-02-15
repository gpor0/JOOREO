package com.github.gpor0.jooreo.test.support.repositories;

import com.github.gpor0.jooreo.dao.JooreoDao;
import com.github.gpor0.jooreo.test.support.records.ReservationFlightRecord;

import javax.enterprise.context.ApplicationScoped;

/**
 * Author: gpor0
 */
@ApplicationScoped
public class ReservationFlightRepository extends JooreoDao<ReservationFlightRecord> {

}
