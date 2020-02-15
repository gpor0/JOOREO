package com.github.gpor0.jooreo;

import org.jooq.DSLContext;
import org.jooq.TableRecord;

/**
 * Author: gpor0
 */
public interface JooreoInsertFilter {

    <T extends TableRecord> int filter(DSLContext dsl, T r);

}
