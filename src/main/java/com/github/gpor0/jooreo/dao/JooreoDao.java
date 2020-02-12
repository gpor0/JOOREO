package com.github.gpor0.jooreo.dao;

import com.github.gpor0.jooreo.Jooq;
import com.github.gpor0.jooreo.LoggedUser;
import com.github.gpor0.jooreo.Queried;
import com.github.gpor0.jooreo.exceptions.InvalidParameterException;
import com.github.gpor0.jooreo.operations.DataOperation;
import com.github.gpor0.jooreo.operations.FilterOperation;
import com.github.gpor0.jooreo.operations.OrderByOperation;
import org.jooq.*;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.tools.Convert;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;


public abstract class JooreoDao<R extends UpdatableRecordImpl> {

    @Inject
    protected LoggedUser user;

    @Inject
    protected DSLContext dsl;

    protected Class<R> clazz;

    @PostConstruct
    public void init() {
        this.clazz = (Class<R>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected Consumer<Record> onUpdate() {
        Timestamp now = new Timestamp(new Date().getTime());
        UUID userId = getUserId();

        return (r) -> {

            if (r.field("dm") != null) {
                r.setValue(field(name("dm"), Timestamp.class), Convert.convert(now, Timestamp.class));
            }
            if (r.field("um") != null) {
                r.setValue(field(name("um"), UUID.class), userId);
            }
        };
    }

    protected Consumer<Record> onCreate() {
        Timestamp now = new Timestamp(new Date().getTime());
        UUID userId = getUserId();

        //todo externalize
        return (r) -> {
            if (r.field("dc") != null) {
                r.setValue(field(name("dc"), Timestamp.class), Convert.convert(now, Timestamp.class));
            }
            if (r.field("uc") != null) {
                r.setValue(field(name("uc"), UUID.class), userId);
            }
            if (r.field("dm") != null) {
                r.setValue(field(name("dm"), Timestamp.class), Convert.convert(now, Timestamp.class));
            }
            if (r.field("um") != null) {
                r.setValue(field(name("um"), UUID.class), userId);
            }
            if (r.field("id") != null) {
                r.setValue(field(name("id"), UUID.class), UUID.randomUUID());
            }
            if (r.field("deleted") != null) {
                r.setValue(field(name("deleted"), Integer.class), 0);
            }
        };
    }

    public Queried<R> getAll() {
        int count = dsl.fetchCount(table());

        Stream<R> ts = dsl.selectFrom(table()).fetch(Jooq.to(clazz, dsl)).stream();

        return Queried.result(Long.valueOf(count), ts);
    }

    public Queried<R> getSortedAndPaginated(Integer offset, Integer limit, DataOperation... operations) {
        return getSortedAndPaginated(offset, limit, operations, null);
    }

    public Queried<R> getSortedAndPaginated(Integer offset, Integer limit, DataOperation[] operations, DataOperation... additionalOperations) {

        Table<R> tmp = table();
        SelectWhereStep<R> selectStep = dsl.selectFrom(tmp);

        List<Condition> filterBy = new LinkedList<>();
        List<SelectConditionStep<Record1<Integer>>> existsConditions = new LinkedList<>();
        List<SortField<?>> orderBy = null;
        if (operations != null && operations.length > 0) {

            if (additionalOperations != null && additionalOperations.length > 0) {
                operations = Arrays.copyOf(operations, operations.length + additionalOperations.length);
                for (int i = 0; i < additionalOperations.length; i++) {
                    operations[operations.length - additionalOperations.length + i] = additionalOperations[i];
                }
            }

            filterBy = getFilterFields(operations);
            existsConditions = Jooq.getExistConditions(clazz, dsl, tmp, operations);
            orderBy = getSortFields(operations);
        }

        Field<Integer> deletedField =
                (Field<Integer>) tmp.fieldStream().filter(column -> column.getName().equals("deleted")).findFirst().orElse(null);

        if (deletedField != null) {
            filterBy.add(deletedField.eq(0));
        }

        if (!filterBy.isEmpty()) {
            selectStep.where(filterBy);
        }

        for (SelectConditionStep<Record1<Integer>> existsCondition : existsConditions) {
            selectStep.whereExists(existsCondition);
        }

        Long count = dsl.selectCount().from(selectStep).fetchOneInto(Long.class);

        if (orderBy != null && !orderBy.isEmpty()) {
            selectStep.orderBy(orderBy);
        }

        Stream<R> ts = selectStep
                .limit(offset == null ? 0 : offset, limit == null ? 20 : limit).fetch(Jooq.to(clazz, dsl))
                .stream();

        return Queried.result(count, ts);
    }

    abstract Table<R> table();

    protected List<Condition> getFilterFields(DataOperation... operations) {

        return Stream.of(operations)
                .filter(operation -> operation != null && operation.getClass() == FilterOperation.class)
                .filter(op -> {
                    String fieldName = ((FilterOperation) op).getField();
                    String[] childField = fieldName.split("\\.");
                    return childField.length == 1;
                })
                .map(op -> Jooq.buildCondition(table(), op)).collect(Collectors.toList());
    }

    protected List<SortField<?>> getSortFields(DataOperation... operations) {
        return Stream.of(operations)
                .filter(operation -> operation != null && operation.getClass() == OrderByOperation.class)
                .map(op -> {
                    String fieldName = ((OrderByOperation) op).getField();
                    Field<?> field =
                            table().fieldStream().filter(column -> column.getName().equals(fieldName)).findFirst().orElseThrow(() -> new InvalidParameterException(fieldName, ((OrderByOperation) op).isAsc() ? "asc" : "desc"));
                    return ((OrderByOperation) op).isAsc() ? field.asc() : field.desc();
                }).collect(Collectors.toList());
    }

    public void create(R r) {
        onCreate().accept(r);
        dsl.executeInsert(r);
    }

    public void update(R r) {
        onUpdate().accept(r);
        r.update();
    }

    public void delete(R r) {
        r.delete();
    }

    private <T> T getUserId() {
        try {
            return user.getId();
        } catch (Exception e) {
            //warn
        }

        return null;
    }

}
