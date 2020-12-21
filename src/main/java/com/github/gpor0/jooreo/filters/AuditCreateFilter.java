package com.github.gpor0.jooreo.filters;


import com.github.gpor0.jooreo.RequestContextProxy;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableRecord;
import org.jooq.tools.Convert;

import javax.enterprise.inject.spi.CDI;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import static com.github.gpor0.jooreo.Jooreo.getField;

/**
 * Author: gpor0
 */
public class AuditCreateFilter extends RandomUUIDCreateFilter {

    @Override
    public <T extends TableRecord> int filter(DSLContext dsl, T r) {
        Timestamp now = new Timestamp(new Date().getTime());
        UUID userId = CDI.current().select(RequestContextProxy.class).get().getId();

        Field dcField = getField(r, "dc");
        if (dcField != null) {
            r.setValue(dcField, Convert.convert(now, dcField.getType()));
        }
        Field ucField = getField(r, "uc");
        if (ucField != null) {
            r.setValue(ucField, userId);
        }
        Field dmField = getField(r, "dm");
        if (dmField != null) {
            r.setValue(dmField, Convert.convert(now, dmField.getType()));
        }

        Field umField = getField(r, "um");
        if (umField != null) {
            r.setValue(umField, userId);
        }
        Field idField = getField(r, "id");
        if (idField != null) {
            r.setValue(idField, UUID.randomUUID());
        }
        Field delField = getField(r, "deleted");
        if (delField != null) {
            r.setValue(delField, 0);
        }

        return super.filter(dsl, r);
    }
}
