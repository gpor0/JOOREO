package com.github.gpor0.jooreo.filters;

import com.github.gpor0.jooreo.JooreoRecordFilter;
import com.github.gpor0.jooreo.RequestContextProxy;
import com.github.gpor0.jooreo.filters.defaults.OnUpdateDefaultFilter;
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
public class ShallowDeleteFilter extends OnUpdateDefaultFilter implements JooreoRecordFilter {

    @Override
    public TableRecord filter(DSLContext dsl, TableRecord r) {
        Timestamp now = new Timestamp(new Date().getTime());
        UUID userId = CDI.current().select(RequestContextProxy.class).get().getId();


        Field dmField = getField(r, "dm");
        if (dmField != null) {
            r.setValue(dmField, Convert.convert(now, Timestamp.class));
        }

        Field umField = getField(r, "um");
        if (umField != null) {
            r.setValue(umField, userId);
        }
        Field deletedField = getField(r, "deleted");
        if (deletedField != null) {
            //deleted should be integer field
            r.setValue(deletedField, 1);
        }

        //update
        return super.filter(dsl, r);
    }
}
