package com.github.gpor0.jooreo;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class I18nUtil {

    private static final Set<String> commonProps = Set.of("id", "dc", "dm", "uc", "um", "language", "version");

    public static final Table buildSelectFields(final String language, final Table baseTable, final Table i18nTable) {

        if (language == null || i18nTable == null) {
            return baseTable;
        }

        Table b = baseTable.as("b");
        Table t = i18nTable.as("t");

        Field<?>[] bFields = b.fields();

        Field<?>[] tFields = t.fields();
        Map<String, Field<?>> fieldSet =
                Arrays.stream(tFields).filter(f -> !commonProps.contains(f.getName())).collect(Collectors.toMap(f -> f.getName(), Function.identity()));

        Set<Field> fields = Stream.concat(Arrays.stream(bFields).filter(f -> !fieldSet.containsKey(f.getName())),
                fieldSet.values().stream().map(tr -> DSL.nvl(tr, (Field<?>) b.field(tr.getName())).as(tr.getName()))).collect(Collectors.toSet());

        return DSL.select(fields).from(b)
                .leftOuterJoin(t).on(b.field("id", UUID.class).eq(t.field("id", UUID.class)).and(t.field("language", String.class).eq(language))).asTable(baseTable.getName());
    }

}
