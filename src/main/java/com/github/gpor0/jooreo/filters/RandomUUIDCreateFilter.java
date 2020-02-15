package com.github.gpor0.jooreo.filters;

import com.github.gpor0.jooreo.JooreoInsertFilter;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableRecord;

import java.util.UUID;

import static com.github.gpor0.jooreo.Jooreo.getField;

/**
 * Author: gpor0
 */
public class RandomUUIDCreateFilter implements JooreoInsertFilter {

    @Override
    public <T extends TableRecord> int filter(DSLContext dsl, T r) {

        Field idField = getField(r, "id");
        if (idField != null) {
            r.setValue(idField, UUID.randomUUID());
        }

        return dsl.executeInsert(r);
    }
}
