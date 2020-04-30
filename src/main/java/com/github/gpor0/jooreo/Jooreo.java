package com.github.gpor0.jooreo;

import com.github.gpor0.jooreo.annotations.OneToMany;
import com.github.gpor0.jooreo.dao.record.JooreoRecord;
import com.github.gpor0.jooreo.exceptions.ParameterSyntaxException;
import com.github.gpor0.jooreo.exceptions.UnsupportedParameterException;
import com.github.gpor0.jooreo.operations.DataOperation;
import com.github.gpor0.jooreo.operations.FilterOperation;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: gpor0
 */
public class Jooreo {

    private static final Map<Class, Table> CLASS_TABLE_MAP = new ConcurrentHashMap<>();

    public static final <R extends TableRecord> Condition buildCondition(Table<R> table, DataOperation op) {
        {
            String fieldNameStr = ((FilterOperation) op).getField();

            if (fieldNameStr.contains(".")) {
                String[] split = fieldNameStr.split("\\.");
                fieldNameStr = split[split.length - 1];
            }

            String fieldName = fieldNameStr;

            Field field = table.fieldStream().filter(column -> column.getName().equalsIgnoreCase(fieldName)).findFirst().orElseThrow(() -> new UnsupportedParameterException(fieldName));

            Object val = ((FilterOperation) op).getValue();
            if (val != null && val instanceof String && field.getDataType().isDateTime()) {
                //todo support converters
                val = new Timestamp(Long.valueOf((String) val)).toLocalDateTime();
            }

            String operation = ((FilterOperation) op).getOperation().toUpperCase();

            if (val instanceof String) {
                String strValue = Objects.toString(val, null);
                switch (operation) {
                    case "LIKE":
                        return field.like(strValue);
                    case "LIKEIC":
                        return field.likeIgnoreCase(strValue);
                    case "NEQIC":
                        return field.notEqualIgnoreCase(strValue);
                    case "EQIC":
                        return field.equalIgnoreCase(strValue);
                    case "IN":
                        return field.in(parseCollectionValue(field, strValue));
                    case "INIC":
                        return DSL.lower((Field<String>) field).in(parseCollectionValue(field, strValue));
                    case "NIN":
                        return field.notIn(parseCollectionValue(field, strValue));
                    case "NINIC":
                        return DSL.lower((Field<String>) field).notIn(parseCollectionValue(field, strValue));
                }
            }

            switch (operation) {
                case "EQ":
                    return field.equal(val);
                case "NEQ":
                    return field.notEqual(val);
                case "GT":
                    return field.greaterThan(val);
                case "GTE":
                    return field.greaterOrEqual(val);
                case "LT":
                    return field.lessThan(val);
                case "LTE":
                    return field.lessOrEqual(val);
                case "ISNULL":
                    return field.isNull();
                case "ISNOTNULL":
                    return field.isNotNull();
                case "IN":
                    return field.in(val);
                case "NIN":
                    return field.notIn(val);

            }

            throw new ParameterSyntaxException(fieldName, val.toString());
        }
    }

    private static Object parseFieldValue(Field<?> field, String strValue) {

        if (strValue == null) {
            return null;
        }

        Class<?> type = field.getType();

        if (type == Integer.class) {
            return Integer.valueOf(strValue);
        } else if (type == java.sql.Date.class) {
            return new java.sql.Date(Integer.valueOf(strValue));
        }

        return strValue;
    }

    private static List<?> parseCollectionValue(Field<?> field, String strValue) {

        if (strValue == null) {
            return null;
        }

        if (!strValue.contains("[") || !strValue.contains("]")) {
            throw new ParameterSyntaxException(field.getName(), strValue);
        }

        return Stream.of(strValue.replace("]", "").replace("[", "").split(",")).map(v -> parseFieldValue(field, v)).collect(Collectors.toList());
    }

    /**
     * This class generates list of sub-selects from list of parent operations.
     * <p>
     * Example roles.role:EQ:employee filter object translates to: select 1 from user_role where role='employee' and user_role.id = user.id
     *
     * @param clazz       parent object class
     * @param dsl         reference to DSL
     * @param parentTable parent table
     * @param operations  array of parsed operations (filters)
     * @return list of queries for exists sub selects
     */
    public static <R extends TableRecord> List<SelectConditionStep<Record1<Integer>>> getExistConditions(Class<?> clazz,
                                                                                                         DSLContext dsl,
                                                                                                         Table<R> parentTable,
                                                                                                         DataOperation[] operations) {
        return Stream.of(operations)
                .filter(operation -> operation != null && operation.getClass() == FilterOperation.class)
                .filter(op -> {
                    String fieldName = ((FilterOperation) op).getField();
                    return fieldName.split("\\.").length == 2;
                })
                .map(op -> {
                    String fieldName = ((FilterOperation) op).getField();
                    String[] childFieldSplit = fieldName.split("\\.");
                    String childObjName = childFieldSplit[0];

                    for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                        if (f.getAnnotation(OneToMany.class) != null) {
                            if (f.getName().toLowerCase().equals(childObjName)) {
                                Type genericReturnType = f.getAnnotatedType().getType();
                                if (genericReturnType instanceof ParameterizedType) {
                                    Class<TableRecord> childClass =
                                            ((Class<TableRecord>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0]);

                                    //cache
                                    if (!CLASS_TABLE_MAP.containsKey(childClass)) {
                                        try {
                                            Constructor<TableRecord> constructor = childClass.getConstructor();
                                            TableRecord tableRecord = constructor.newInstance();
                                            CLASS_TABLE_MAP.putIfAbsent(childClass, tableRecord.getTable());
                                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                            throw new UnsupportedParameterException(fieldName, ((FilterOperation) op).getValue());
                                        }
                                    }

                                    Table childTable = CLASS_TABLE_MAP.get(childClass);

                                    return new AbstractMap.SimpleEntry<>(childTable, Jooreo.buildCondition(childTable, op));
                                }
                            }
                        }
                    }

                    return null;
                }).filter(Objects::nonNull).collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey)).entrySet().stream().map(e -> {
                    Table childTable = e.getKey();
                    List<Condition> childTableConditions = e.getValue().stream().map(AbstractMap.SimpleEntry::getValue).collect(Collectors.toList());
                    List<ForeignKey<Record, R>> references = parentTable.getReferencesFrom(childTable);
                    ForeignKey<Record, R> recordFk = references.get(0);
                    Field fkTableField = recordFk.getFields().get(0);
                    Field primaryKey = parentTable.getPrimaryKey().getFields().get(0);

                    return dsl.selectOne().from(childTable).where(childTableConditions).and(fkTableField.eq(primaryKey));
                }).collect(Collectors.toList());
    }

    public static Field getField(Record r, String fieldName) {
        return r.field(fieldName.toLowerCase()) != null ? r.field(fieldName.toLowerCase()) : r.field(fieldName.toUpperCase());
    }

    public static <R extends Record> RecordMapper<Record, R> to(Class<R> recordClass, DSLContext dsl) {
        return (RecordMapper) record -> {
            R result = record.into(recordClass);
            if (JooreoRecord.class.isAssignableFrom(recordClass)) {
                ((JooreoRecord) result).dsl(dsl);
            }
            return result;
        };
    }
}
