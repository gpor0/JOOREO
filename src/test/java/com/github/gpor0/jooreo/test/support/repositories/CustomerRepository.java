package com.github.gpor0.jooreo.test.support.repositories;

import com.github.gpor0.jooreo.dao.JooreoDao;
import com.github.gpor0.jooreo.test.support.records.CustomerRecord;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

import static com.github.gpor0.jooreo.dao.records.Tables.CUSTOMER;

/**
 * Author: gpor0
 */
@ApplicationScoped
public class CustomerRepository extends JooreoDao<CustomerRecord> {

    public CustomerRecord getById(UUID id) {
        return dsl.select().from(CUSTOMER).where(CUSTOMER.ID.eq(id)).fetchOne(toRecord());
    }
}
