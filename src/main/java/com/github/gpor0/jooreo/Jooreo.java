package com.github.gpor0.jooreo;

import com.github.gpor0.jooreo.annotations.ManyToOne;
import com.github.gpor0.jooreo.annotations.OneToMany;
import com.github.gpor0.jooreo.dao.record.JooreoRecord;
import com.github.gpor0.jooreo.exceptions.ParameterSyntaxException;
import com.github.gpor0.jooreo.exceptions.UnsupportedParameterException;
import com.github.gpor0.jooreo.operations.DataOperation;
import com.github.gpor0.jooreo.operations.FilterOperation;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: gpor0
 */
public class Jooreo {

    private static final Map<Class, Table> CLASS_TABLE_MAP = new ConcurrentHashMap<>();

    private static Object convertToFieldType(DataType dataType, String value) {
        if (dataType.isDateTime()) {
            return new Timestamp(Long.parseLong(value)).toLocalDateTime();
        } else if (dataType.getType() == UUID.class) {
            return UUID.fromString(value);
        } else if (dataType.isInteger() && ("false".equals(value) || "true".equals(value))) {
            return Boolean.valueOf(value) ? 1 : 0;
        }
        //todo support converters

        return value;
    }

    public static final <R extends TableRecord> Condition buildCondition(Table<? extends org.jooq.Record> table, DataOperation op) {
        {
            String fieldNameStr = ((FilterOperation) op).getField();

            if (fieldNameStr.contains(".")) {
                String[] split = fieldNameStr.split("\\.");
                fieldNameStr = split[split.length - 1];
            }

            String fieldName = camelToSnake(fieldNameStr);

            Field field = table.fieldStream().filter(column -> column.getName().equalsIgnoreCase(fieldName)).findFirst().orElseThrow(() -> new UnsupportedParameterException(fieldName));

            Object val = ((FilterOperation) op).getValue();
            if (val != null && val instanceof String) {
                final DataType dataType = field.getDataType();
                String strVal = (String) val;
                if (strVal.startsWith("[") && strVal.endsWith("]")) {
                    val = Stream.of(strVal.substring(1, strVal.length() - 1).split(",")).map(v -> convertToFieldType(dataType, v)).collect(Collectors.toList());
                } else {
                    val = convertToFieldType(dataType, strVal);
                }
            }

            String operation = ((FilterOperation) op).getOperation().toUpperCase();

            if (val instanceof String) {
                String strValue = Objects.toString(val, null);
                switch (operation) {
                    case "LIKE":
                        return field.like(strValue.replace("*", "%"));
                    case "LIKEIC":
                        return field.likeIgnoreCase(strValue.replace("*", "%"));
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
            return new java.sql.Date(Long.parseLong(strValue));
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
                                                                                                         Table<? extends org.jooq.Record> parentTable,
                                                                                                         DataOperation[] operations) {
        if (clazz != null && !CLASS_TABLE_MAP.containsKey(clazz)) {
            try {
                final Constructor<TableRecord> constructor = (Constructor<TableRecord>) clazz.getConstructor();
                final TableRecord tableRecord = constructor.newInstance();
                CLASS_TABLE_MAP.putIfAbsent(clazz, tableRecord.getTable());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
            }
        }

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
                        if (f.getAnnotation(OneToMany.class) != null || f.getAnnotation(ManyToOne.class) != null) {
                            if (f.getName().equals(childObjName)) {
                                Type genericReturnType = f.getAnnotatedType().getType();
                                Class<TableRecord> childClass;
                                if (genericReturnType instanceof ParameterizedType) {
                                    childClass = ((Class<TableRecord>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0]);
                                } else {
                                    childClass = (Class<TableRecord>) genericReturnType;
                                }
                                //cache
                                if (!CLASS_TABLE_MAP.containsKey(childClass)) {
                                    try {
                                        Constructor<TableRecord> constructor = childClass.getConstructor();
                                        TableRecord tableRecord = constructor.newInstance();
                                        CLASS_TABLE_MAP.putIfAbsent(childClass, tableRecord.getTable());
                                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                                             InvocationTargetException e) {
                                        throw new UnsupportedParameterException(fieldName, ((FilterOperation) op).getValue());
                                    }
                                }

                                final JoinField joinField = new JoinField();
                                joinField.childTable = CLASS_TABLE_MAP.get(childClass);
                                joinField.parentClassField = f;

                                return new AbstractMap.SimpleEntry<>(joinField, Jooreo.buildCondition(joinField.childTable, op));
                            }
                        }
                    }

                    return null;
                }).filter(Objects::nonNull).collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey)).entrySet().stream().map(e -> {
                    JoinField joinField = e.getKey();
                    Table childTable = joinField.childTable;
                    List<Condition> childTableConditions = e.getValue().stream().map(AbstractMap.SimpleEntry::getValue).collect(Collectors.toList());
                    List<ForeignKey<Record, R>> references = parentTable.getReferencesFrom(childTable);
                    Field fkTableField = null;
                    Field primaryKey;
                    if (references.isEmpty()) {
                        primaryKey = (Field) childTable.getPrimaryKey().getFields().get(0);
                        references = childTable.getReferencesFrom(parentTable);
                        if (!references.isEmpty()) {
                            ForeignKey<Record, R> recordFk = references.get(0);
                            fkTableField = recordFk.getFields().get(0);
                        } else {
                            java.lang.reflect.Field f = joinField.parentClassField;
                            Class parentTableClass = CLASS_TABLE_MAP.entrySet().stream().filter(en -> en.getValue().equals(parentTable)).map(en -> en.getKey()).findFirst().orElse(null);
                            if (parentTableClass == null) {
                                throw new UnsupportedParameterException("filter", f.getName(), "null");
                            }
                            ManyToOne mto = f.getAnnotation(ManyToOne.class);
                            if (mto != null && mto.field() != null && !mto.field().isEmpty()) {
                                fkTableField = parentTable.field(mto.field());
                            }
                            OneToMany otm = f.getAnnotation(OneToMany.class);
                            if (otm != null && otm.childField() != null && !otm.childField().isEmpty()) {
                                primaryKey = parentTable.getPrimaryKey().getFields().get(0);
                                fkTableField = childTable.field(otm.childField());
                            }
                            if (fkTableField == null) {
                                throw new RuntimeException("Unable to build required filter for parent " + parentTable.getName() + " and field " + f.getName());
                            }
                        }
                    } else {
                        ForeignKey<Record, R> recordFk = references.get(0);
                        fkTableField = recordFk.getFields().get(0);
                        primaryKey = parentTable.getPrimaryKey().getFields().get(0);
                    }

                    return dsl.selectOne().from(childTable).where(childTableConditions).and(fkTableField.eq(primaryKey));
                }).collect(Collectors.toList());
    }

    public static Field getField(org.jooq.Record r, String fieldName) {
        return r.field(fieldName.toLowerCase()) != null ? r.field(fieldName.toLowerCase()) : r.field(fieldName.toUpperCase());
    }

    public static <R extends org.jooq.Record> RecordMapper<org.jooq.Record, R> to(Class<R> recordClass, DSLContext dsl) {
        return (RecordMapper<org.jooq.Record, R>) record -> {
            R result = record.into(recordClass);
            if (JooreoRecord.class.isAssignableFrom(recordClass)) {
                ((JooreoRecord) result).dsl(dsl);
            }
            return result;
        };
    }

    public static String camelToSnake(String str) {
        return str == null ? null : str.replaceAll("(?<!^|_|[A-Z])([A-Z])", "_$1").toLowerCase();
    }

    private static class JoinField {
        public java.lang.reflect.Field parentClassField;
        public Table childTable;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JoinField joinField = (JoinField) o;
            return Objects.equals(parentClassField, joinField.parentClassField) && Objects.equals(childTable, joinField.childTable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parentClassField, childTable);
        }
    }

}
