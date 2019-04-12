package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Set;
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
        return new SimpleColumnImpl<>(GenericColumn.BIG_DECIMAL, prefix, name, getter, setter, GenericColumn.BIG_DECIMAL.getSqlTypeName(), nullable);
    }

    public static <ENTITY, BUILDER> Column<ENTITY, BUILDER> longColumn(
            String name, String prefix, Function<ENTITY, Long> getter, BiConsumer<BUILDER, Long> setter, boolean nullable) {
        return new SimpleColumnImpl<>(GenericColumn.LONG, prefix, name, getter, setter, GenericColumn.LONG.getSqlTypeName(), nullable);
    }

    public static <ENTITY, BUILDER> Column<ENTITY, BUILDER> booleanColumn(
            String name, String prefix, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter, boolean nullable) {
        return new SimpleColumnImpl<>(GenericColumn.BOOLEAN, prefix, name, getter, setter, GenericColumn.BOOLEAN.getSqlTypeName(), nullable);
    }

    public static <ENTITY, BUILDER> AbstractColumn<Boolean, ENTITY, BUILDER> textBackedBooleanColumn(
            String name, String prefix, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter, boolean nullable) {
        return stringConverterColumn(name, prefix, getter, setter, new BooleanStringConverter("T", "F"), nullable);
    }


    public static <ENTITY, BUILDER> AbstractColumn<String, ENTITY, BUILDER> stringColumn(
            String name, String prefix, Function<ENTITY, String> getter, BiConsumer<BUILDER, String> setter, boolean nullable) {
        return new AbstractColumn<String, ENTITY, BUILDER>(name, prefix, getter, setter, nullable, "text") {
            @Override
            public String fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                return resultSet.getString(columnName);
            }

            @Override
            public void setPreparedStatement(PreparedStatement preparedStatement, int index, String value) throws SQLException {
                preparedStatement.setString(index, value);
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return stringColumn(getName(), newPrefix, getter, setter, nullable);
            }

            @Override
            int sqlType() {
                return Types.VARCHAR;
            }

            @Override
            public Set<Integer> supportedTypes() {
                return ColumnTypes.StringTypes;
            }
        };
    }

    public static <ENTITY, BUILDER> AbstractColumn<Instant, ENTITY, BUILDER> instantColumn(
            String name, String prefix, Function<ENTITY, Instant> getter, BiConsumer<BUILDER, Instant> setter, boolean nullable) {
        return new AbstractColumn<Instant, ENTITY, BUILDER>(name, prefix, getter, setter, nullable, "timestamp") {
            @Override
            public Instant fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                Timestamp sqlTime = resultSet.getTimestamp(columnName);
                Instant value = null;
                if (sqlTime != null) {
                    value = sqlTime.toInstant();
                }
                return value;
            }

            @Override
            public void setPreparedStatement(PreparedStatement preparedStatement, int index, Instant value) throws SQLException {
                Timestamp sqlTime = Timestamp.from(value);
                preparedStatement.setTimestamp(index, sqlTime);
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return instantColumn(getName(), newPrefix, getter, setter, nullable);
            }

            @Override
            int sqlType() {
                return Types.TIMESTAMP;
            }

            @Override
            public Set<Integer> supportedTypes() {
                return ColumnTypes.InstantTypes;
            }
        };
    }


    public static <E, ENTITY, BUILDER> AbstractColumn<E, ENTITY, BUILDER> stringConverterColumn(
            String name, String prefix, Function<ENTITY, E> getter, BiConsumer<BUILDER, E> setter,
            Converter<E, String> converter, boolean nullable) {
        return new AbstractColumn<E, ENTITY, BUILDER>(name, prefix, getter, setter, nullable, "text") {
            @Override
            public E fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                String code = resultSet.getString(columnName);
                if (code == null) {
                    return null;
                }
                return converter.to(code);
            }

            @Override
            public void setPreparedStatement(PreparedStatement preparedStatement, int index, E value) throws SQLException {
                String code = null;
                if (value != null) {
                    code = converter.from(value);
                }
                preparedStatement.setString(index, code);
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return stringConverterColumn(getName(), newPrefix, getter, setter, converter, nullable);
            }

            @Override
            int sqlType() {
                return Types.VARCHAR;
            }

            @Override
            public Set<Integer> supportedTypes() {
                return ColumnTypes.StringTypes;
            }
        };
    }


    public static <E, ENTITY, BUILDER> AbstractColumn<E, ENTITY, BUILDER> integerConverterColumn(
            String name, String prefix, Function<ENTITY, E> getter, BiConsumer<BUILDER, E> setter,
            Converter<E, Long> converter, boolean nullable) {
        return new AbstractColumn<E, ENTITY, BUILDER>(name, prefix, getter, setter, nullable, "integer") {
            @Override
            public E fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                Long code = resultSet.getLong(columnName);
                if ( resultSet.wasNull() ) {
                    return null;
                }
                return converter.to(code);
            }

            @Override
            public void setPreparedStatement(PreparedStatement preparedStatement, int index, E value) throws SQLException {
                Long code = null;
                if (value != null) {
                    code = converter.from(value);
                }
                preparedStatement.setLong(index, code);
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return integerConverterColumn(getName(), newPrefix, getter, setter, converter, nullable);
            }

            @Override
            int sqlType() {
                return Types.INTEGER;
            }

            @Override
            public Set<Integer> supportedTypes() {
                return ColumnTypes.IntegerTypes;
            }
        };
    }

    public static <TYPE, ENTITY, BUILDER> Column<ENTITY, BUILDER> genericColumn(
            String name, String prefix, Function<ENTITY, TYPE> getter, BiConsumer<BUILDER, TYPE> setter,
            GenericColumn<TYPE> genericColumn, boolean nullable) {
        return new SimpleColumnImpl<>(genericColumn, prefix, name, getter, setter, genericColumn.getSqlTypeName(), nullable);
    }

    public static <TYPE, DBTYPE, ENTITY, BUILDER> Column<ENTITY, BUILDER> convertedGenericColumn(
            String name, String prefix, Function<ENTITY, TYPE> getter, BiConsumer<BUILDER, TYPE> setter,
            GenericColumn<DBTYPE> genericColumn, Converter<TYPE, DBTYPE> converter, boolean nullable) {
        return new ConvertingColumnImpl<TYPE, DBTYPE, ENTITY, BUILDER>(genericColumn, prefix, name, getter, setter, genericColumn.getSqlTypeName(), nullable, converter);
    }

}
