package com.github.gpor0.jooreo.test.support.repositories;

import com.github.gpor0.jooreo.dao.JooreoDao;
import com.github.gpor0.jooreo.test.support.records.FlightRecord;

import javax.enterprise.context.ApplicationScoped;

/**
 * Author: gpor0
 */
@ApplicationScoped
public class FlightRepository extends JooreoDao<FlightRecord> {

}
