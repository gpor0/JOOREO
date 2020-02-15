package com.github.gpor0.jooreo.filters.defaults;

import com.github.gpor0.jooreo.JooreoRecordFilter;
import org.jooq.DSLContext;
import org.jooq.TableRecord;
import org.jooq.UpdatableRecord;

public class OnDeleteDefaultFilter implements JooreoRecordFilter {

    @Override
    public TableRecord filter(DSLContext dsl, TableRecord r) {

        if (r == null) {
            return null;
        }

        if (r instanceof UpdatableRecord) {
            ((UpdatableRecord) r).delete();
            return null;
        } else {
            throw new RuntimeException("Unable to remove record");
        }

    }
}
