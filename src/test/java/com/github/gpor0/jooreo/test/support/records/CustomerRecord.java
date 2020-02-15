package com.github.gpor0.jooreo.test.support.records;

import com.github.gpor0.jooreo.annotations.OnInsertFilter;
import com.github.gpor0.jooreo.dao.record.JooreoRecord;
import com.github.gpor0.jooreo.dao.records.tables.records.BaseCustomerRecord;
import com.github.gpor0.jooreo.filters.AuditCreateFilter;

@OnInsertFilter(AuditCreateFilter.class)
public class CustomerRecord extends BaseCustomerRecord implements JooreoRecord {

}
