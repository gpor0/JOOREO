package com.github.gpor0.jooreo.filters.defaults;

import com.github.gpor0.jooreo.JooreoInsertFilter;
import org.jooq.DSLContext;
import org.jooq.TableRecord;

public class OnInsertDefaultFilter implements JooreoInsertFilter {

    @Override
    public <T extends TableRecord> int filter(DSLContext dsl, T r) {

        if (r == null) {
            return 0;
        }

        if (r.configuration() == null) {
            r.attach(dsl.configuration());
        }

        return r.insert();
    }
}
