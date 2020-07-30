package com.github.gpor0.jooreo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Author: gpor0
 */
public class JooreoTest {

    @Test
    public void camelToSnake() {
        String dateCreated = Jooreo.camelToSnake("dateCreated");

        Assertions.assertEquals("date_created", dateCreated);
    }

    @Test
    public void camelToSnakeComplex() {
        String dateCreated = Jooreo.camelToSnake("lastDateCreated");

        Assertions.assertEquals("last_date_created", dateCreated);
    }


}
