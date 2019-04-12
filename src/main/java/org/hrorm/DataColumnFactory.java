package org.hrorm;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Mechanisms for creating columns that can handle persistence of various Java types.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class DataColumnFactory {

    public static <ENTITY, BUILDER> Column<ENTITY, BUILDER> bigDecimalColumn(
            String name, String prefix, Function<ENTITY, BigDecimal> getter, BiConsumer<BUILDER, BigDecimal> setter, boolean nullable) {
        return new ColumnImpl<>(GenericColumn.BIG_DECIMAL, prefix, name, getter, setter, GenericColumn.BIG_DECIMAL.getSqlTypeName(), nullable);
    }

    public static <ENTITY, BUILDER> Column<ENTITY, BUILDER> longColumn(
            String name, String prefix, Function<ENTITY, Long> getter, BiConsumer<BUILDER, Long> setter, boolean nullable) {
        return new ColumnImpl<>(GenericColumn.LONG, prefix, name, getter, setter, GenericColumn.LONG.getSqlTypeName(), nullable);
    }

    public static <ENTITY, BUILDER> Column<ENTITY, BUILDER> booleanColumn(
            String name, String prefix, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter, boolean nullable) {
        return new ColumnImpl<>(GenericColumn.BOOLEAN, prefix, name, getter, setter, GenericColumn.BOOLEAN.getSqlTypeName(), nullable);
    }

    public static <ENTITY, BUILDER> Column<ENTITY, BUILDER> textBackedBooleanColumn(
            String name, String prefix, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter, boolean nullable) {
        return new ColumnImpl<>(GenericColumn.STRING, prefix, name, getter, setter, GenericColumn.STRING.getSqlTypeName(),nullable, Converters.T_F_BOOLEAN_STRING_CONVERTER);
    }

    public static <ENTITY, BUILDER> Column<ENTITY, BUILDER> longBackedBooleanColumn(
            String name, String prefix, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter, boolean nullable) {
        return new ColumnImpl<>(GenericColumn.LONG, prefix, name, getter, setter, GenericColumn.LONG.getSqlTypeName(),nullable, Converters.ONE_ZERO_BOOLEAN_LONG_CONVERTER);
    }

    public static <ENTITY, BUILDER> Column<ENTITY, BUILDER> stringColumn(
            String name, String prefix, Function<ENTITY, String> getter, BiConsumer<BUILDER, String> setter, boolean nullable) {
        return new ColumnImpl<>(GenericColumn.STRING, prefix, name, getter, setter, GenericColumn.STRING.getSqlTypeName(), nullable);
    }

    public static <ENTITY, BUILDER> Column<ENTITY, BUILDER> instantColumn(
            String name, String prefix, Function<ENTITY, Instant> getter, BiConsumer<BUILDER, Instant> setter, boolean nullable) {
        return new ColumnImpl<>(GenericColumn.TIMESTAMP, prefix, name, getter, setter, GenericColumn.TIMESTAMP.getSqlTypeName(), nullable, Converters.INSTANT_TIMESTAMP_CONVERTER);
    }

    public static <TYPE, ENTITY, BUILDER> Column<ENTITY, BUILDER> stringConverterColumn(
            String name, String prefix, Function<ENTITY, TYPE> getter, BiConsumer<BUILDER, TYPE> setter,
            Converter<TYPE, String> converter, boolean nullable) {
        return new ColumnImpl<>(GenericColumn.STRING, prefix, name, getter, setter, GenericColumn.STRING.getSqlTypeName(), nullable, converter);
    }

    public static <TYPE, ENTITY, BUILDER> Column<ENTITY, BUILDER> genericColumn(
            String name, String prefix, Function<ENTITY, TYPE> getter, BiConsumer<BUILDER, TYPE> setter,
            GenericColumn<TYPE> genericColumn, boolean nullable) {
        return new ColumnImpl<>(genericColumn, prefix, name, getter, setter, genericColumn.getSqlTypeName(), nullable);
    }

    public static <TYPE, DBTYPE, ENTITY, BUILDER> Column<ENTITY, BUILDER> convertedGenericColumn(
            String name, String prefix, Function<ENTITY, TYPE> getter, BiConsumer<BUILDER, TYPE> setter,
            GenericColumn<DBTYPE> genericColumn, Converter<TYPE, DBTYPE> converter, boolean nullable) {
        return new ColumnImpl<>(genericColumn, prefix, name, getter, setter, genericColumn.getSqlTypeName(), nullable, converter);
    }

}
