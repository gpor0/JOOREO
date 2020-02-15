package com.github.gpor0.jooreo;

import org.jooq.DSLContext;
import org.jooq.TableRecord;

/**
 * Author: gpor0
 */
public interface JooreoRecordFilter {

    <T extends TableRecord> T filter(DSLContext dsl, T r);

}
