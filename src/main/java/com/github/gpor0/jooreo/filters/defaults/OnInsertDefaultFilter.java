package com.github.gpor0.jooreo.filters.defaults;

import com.github.gpor0.jooreo.JooreoInsertFilter;
import org.jooq.DSLContext;
import org.jooq.TableRecord;

public class OnInsertDefaultFilter implements JooreoInsertFilter {

    @Override
    public int filter(DSLContext dsl, TableRecord r) {

        if (r == null) {
            return 0;
        }

        return dsl.executeInsert(r);
    }
}
