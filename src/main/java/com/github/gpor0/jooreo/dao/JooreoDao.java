package com.github.gpor0.jooreo.dao;

import com.github.gpor0.jooreo.*;
import com.github.gpor0.jooreo.annotations.OnDeleteFilter;
import com.github.gpor0.jooreo.annotations.OnInsertFilter;
import com.github.gpor0.jooreo.annotations.OnUpdateFilter;
import com.github.gpor0.jooreo.dao.record.JooreoRecord;
import com.github.gpor0.jooreo.exceptions.UnsupportedParameterException;
import com.github.gpor0.jooreo.filters.defaults.OnDeleteDefaultFilter;
import com.github.gpor0.jooreo.filters.defaults.OnInsertDefaultFilter;
import com.github.gpor0.jooreo.filters.defaults.OnUpdateDefaultFilter;
import com.github.gpor0.jooreo.operations.DataOperation;
import com.github.gpor0.jooreo.operations.FilterOperation;
import com.github.gpor0.jooreo.operations.OrderByOperation;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.jooq.*;
import org.jooq.impl.DSL;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: gpor0
 */
public abstract class JooreoDao<R extends TableRecord> {

    @Inject
    protected RequestContextProxy user;

    @Inject
    protected DSLContext dsl;

    protected Class<R> clazz;
    protected JooreoInsertFilter onInsert = new OnInsertDefaultFilter();
    protected JooreoRecordFilter onUpdate = new OnUpdateDefaultFilter();
    protected JooreoRecordFilter onDelete = new OnDeleteDefaultFilter();
    private Table<? extends org.jooq.Record> table;

    private static final JooreoRecordFilter createFilterInstance(Class<?> f) {
        try {
            Constructor<?> constructor = f.getConstructor();
            return (JooreoRecordFilter) constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Cannot create instance of filter", e);
        }
    }

    private static final JooreoInsertFilter createInsertFilterInstance(Class<?> f) {
        try {
            Constructor<?> constructor = f.getConstructor();
            return (JooreoInsertFilter) constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Cannot create instance of filter", e);
        }
    }

    @PostConstruct
    public void init() {
        try {
            if (this.getClass().getGenericSuperclass() instanceof ParameterizedType) {
                this.clazz = (Class<R>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            } else {
                this.clazz = (Class<R>) ((ParameterizedType) this.getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[0];
            }
            this.table = clazz.getConstructor().newInstance().getTable();

            Optional.ofNullable(this.clazz.getAnnotation(OnInsertFilter.class)).ifPresent(a -> onInsert = createInsertFilterInstance(a.value()));
            Optional.ofNullable(this.clazz.getAnnotation(OnUpdateFilter.class)).ifPresent(a -> onUpdate = createFilterInstance(a.value()));
            Optional.ofNullable(this.clazz.getAnnotation(OnDeleteFilter.class)).ifPresent(a -> onDelete = createFilterInstance(a.value()));
        } catch (Exception e) {
            throw new RuntimeException("Unable to make instance of " + this.getClass(), e);
        }
    }

    public Table<? extends org.jooq.Record> table() {
        return table;
    }

    public SelectWhereStep<? extends org.jooq.Record> customSelect() {
        return null;
    }

    public Queried<R> getAll() {
        SelectWhereStep<? extends org.jooq.Record> customSelect = customSelect();
        int count = customSelect == null ? dsl.fetchCount(table()) : dsl.fetchCount(customSelect);

        Select<? extends org.jooq.Record> select = customSelect == null ? dsl.selectFrom(table()) : customSelect;
        Stream<R> ts = select.fetch(toRecord()).stream();

        return Queried.result(Long.valueOf(count), ts);
    }

    public Queried<R> getPaginatedByOperations(Integer offset, Integer limit, DataOperation... operations) {
        return getPaginatedByOperations(offset, limit, operations, null);
    }

    public Queried<R> getPaginatedByOperations(Integer offset, Integer limit, DataOperation[] operations, DataOperation... additionalOperations) {

        final Table<? extends org.jooq.Record> table = table();
        final SelectWhereStep<? extends org.jooq.Record> customSelect = customSelect();
        final SelectWhereStep<? extends org.jooq.Record> selectStep = customSelect == null ? dsl.selectFrom(table) : customSelect;

        DataOperation[] ops = operations == null ? new DataOperation[]{} : operations;

        if (additionalOperations != null && additionalOperations.length > 0) {
            ops = Arrays.copyOf(ops, ops.length + additionalOperations.length);
            for (int i = 0; i < additionalOperations.length; i++) {
                ops[ops.length - additionalOperations.length + i] = additionalOperations[i];
            }
        }

        List<Condition> filterBy = getFilterFields(ops);
        List<SelectConditionStep<Record1<Integer>>> existsConditions = Jooreo.getExistConditions(clazz, dsl, table, ops);
        List<SortField<?>> orderBy = getSortFields(ops);

        Field<Integer> deletedField =
                (Field<Integer>) table.fieldStream().filter(column -> column.getName().equals("deleted")).findFirst().orElse(null);

        if (deletedField != null) {
            filterBy.add(deletedField.eq(0));
        } else {
            Field<Boolean> isDeletedField =
                    (Field<Boolean>) table.fieldStream().filter(column -> column.getName().equals("is_deleted")).findFirst().orElse(null);
            if (isDeletedField != null) {
                filterBy.add(isDeletedField.eq(false));
            }
        }

        final List<Condition> conditions = new ArrayList<>();
        if (!filterBy.isEmpty()) {
            conditions.addAll(filterBy);
        }

        for (SelectConditionStep<Record1<Integer>> existsCondition : existsConditions) {
            conditions.add(DSL.exists(existsCondition));
        }

        long count = dsl.fetchCount(table, conditions);

        selectStep.where(conditions);

        if (orderBy != null && !orderBy.isEmpty()) {
            selectStep.orderBy(orderBy);
        }

        final Stream<R> ts = selectStep
                .limit(offset == null ? 0 : offset, limit == null ? 20 : limit).fetch(toRecord())
                .stream();

        return Queried.result(count, ts);
    }

    protected List<Condition> getFilterFields(DataOperation... operations) {

        return Stream.of(operations)
                .filter(operation -> operation != null && operation.getClass() == FilterOperation.class)
                .filter(op -> {
                    String fieldName = ((FilterOperation) op).getField();
                    String[] childField = fieldName.split("\\.");
                    return childField.length == 1;
                })
                .map(op -> Jooreo.buildCondition(table(), op)).collect(Collectors.toList());
    }

    protected List<SortField<?>> getSortFields(DataOperation... operations) {
        return Stream.of(operations)
                .filter(operation -> operation != null && operation.getClass() == OrderByOperation.class)
                .map(op -> {
                    String fieldName = ((OrderByOperation) op).getField();
                    Field<?> field =
                            table().fieldStream().filter(column -> column.getName().equalsIgnoreCase(fieldName)).findFirst().orElseThrow(() -> new UnsupportedParameterException(fieldName, ((OrderByOperation) op).isAsc() ? "asc" : "desc"));
                    return ((OrderByOperation) op).isAsc() ? field.asc() : field.desc();
                }).collect(Collectors.toList());
    }

    public int create(R r) {
        if (r instanceof JooreoRecord) {
            ((JooreoRecord) r).dsl(dsl);
        }
        return onInsert.filter(dsl, r);
    }

    public R update(R r) {
        return onUpdate.filter(dsl, r);
    }

    public R delete(R r) {
        return onDelete.filter(dsl, r);
    }

    public RecordMapper<org.jooq.Record, R> toRecord() {
        return Jooreo.to(clazz, dsl);
    }
}
