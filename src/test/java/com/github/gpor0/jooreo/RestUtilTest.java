package com.github.gpor0.jooreo;

import com.github.gpor0.jooreo.test.support.JooreoTest;
import com.github.gpor0.jooreo.test.support.records.CustomerRecord;
import com.github.gpor0.jooreo.test.support.repositories.CustomerRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

/**
 * Author: gpor0
 */
public class RestUtilTest {

    @Test
    public void camelToSnake() {
        String dateCreated = RestUtil.camelToSnake("dateCreated");

        Assertions.assertEquals("date_created", dateCreated);
    }

    @Test
    public void camelToSnakeComplex() {
        String dateCreated = RestUtil.camelToSnake("lastDateCreated");

        Assertions.assertEquals("last_date_created", dateCreated);
    }


}
