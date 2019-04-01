package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Collections;
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

    public static <ENTITY, BUILDER> AbstractColumn<BigDecimal, ENTITY, BUILDER> bigDecimalColumn(
            String name, String prefix, Function<ENTITY, BigDecimal> getter, BiConsumer<BUILDER, BigDecimal> setter, boolean nullable) {
        return new AbstractColumn<BigDecimal, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
            @Override
            public BigDecimal fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                return resultSet.getBigDecimal(columnName);
            }

            @Override
            public void setPreparedStatement(PreparedStatement preparedStatement, int index, BigDecimal value) throws SQLException {
                preparedStatement.setBigDecimal(index, value);
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return bigDecimalColumn(getName(), newPrefix, getter, setter, nullable);
            }

            @Override
            int sqlType() {
                return Types.DECIMAL;
            }

            @Override
            public Set<Integer> supportedTypes() {
                return ColumnTypes.DecimalTypes;
            }
            @Override
            public String getSqlType() { return "decimal"; }
        };
    }

    public static <ENTITY, BUILDER> AbstractColumn<Long, ENTITY, BUILDER> longColumn(
            String name, String prefix, Function<ENTITY, Long> getter, BiConsumer<BUILDER, Long> setter, boolean nullable) {
        return new AbstractColumn<Long, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
            @Override
            public Long fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                Long result = resultSet.getLong(columnName);
                if( resultSet.wasNull() ){
                    return null;
                }
                return result;
            }

            @Override
            public void setPreparedStatement(PreparedStatement preparedStatement, int index, Long value) throws SQLException {
                preparedStatement.setLong(index, value);
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return longColumn(getName(), newPrefix, getter, setter, nullable);
            }

            @Override
            int sqlType() {
                return Types.INTEGER;
            }

            @Override
            public Set<Integer> supportedTypes() {
                return ColumnTypes.IntegerTypes;
            }

            @Override
            public String getSqlType() { return "integer"; }
        };
    }

    public static <ENTITY, BUILDER> AbstractColumn<Boolean, ENTITY, BUILDER> booleanColumn(
            String name, String prefix, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter, boolean nullable) {
        return new AbstractColumn<Boolean, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
            @Override
            public Boolean fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                Boolean result = resultSet.getBoolean(columnName);
                if ( resultSet.wasNull() ){
                    return null;
                }
                return result;
            }

            @Override
            public void setPreparedStatement(PreparedStatement preparedStatement, int index, Boolean value) throws SQLException {
                preparedStatement.setBoolean(index, value);
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return booleanColumn(getName(), newPrefix, getter, setter, nullable);
            }

            @Override
            int sqlType() {
                return Types.BOOLEAN;
            }

            @Override
            public Set<Integer> supportedTypes() {
                return ColumnTypes.BooleanTypes;
            }

            @Override
            public String getSqlType() { return "boolean"; }
        };
    }

    public static <ENTITY, BUILDER> AbstractColumn<Boolean, ENTITY, BUILDER> textBackedBooleanColumn(
            String name, String prefix, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter, boolean nullable) {
        return stringConverterColumn(name, prefix, getter, setter, new BooleanStringConverter("T", "F"), nullable);
    }


    public static <ENTITY, BUILDER> AbstractColumn<String, ENTITY, BUILDER> stringColumn(
            String name, String prefix, Function<ENTITY, String> getter, BiConsumer<BUILDER, String> setter, boolean nullable) {
        return new AbstractColumn<String, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
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

            @Override
            public String getSqlType() { return "text"; }
        };
    }

    public static <ENTITY, BUILDER> AbstractColumn<Instant, ENTITY, BUILDER> instantColumn(
            String name, String prefix, Function<ENTITY, Instant> getter, BiConsumer<BUILDER, Instant> setter, boolean nullable) {
        return new AbstractColumn<Instant, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
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

            @Override
            public String getSqlType() { return "timestamp"; }
        };
    }


    public static <E, ENTITY, BUILDER> AbstractColumn<E, ENTITY, BUILDER> stringConverterColumn(
            String name, String prefix, Function<ENTITY, E> getter, BiConsumer<BUILDER, E> setter,
            Converter<E, String> converter, boolean nullable) {
        return new AbstractColumn<E, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
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

            @Override
            public String getSqlType() { return "text"; }
        };
    }


    public static <E, ENTITY, BUILDER> AbstractColumn<E, ENTITY, BUILDER> integerConverterColumn(
            String name, String prefix, Function<ENTITY, E> getter, BiConsumer<BUILDER, E> setter,
            Converter<E, Long> converter, boolean nullable) {
        return new AbstractColumn<E, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
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

            @Override
            public String getSqlType() { return "integer"; }
        };
    }

    public static <E, ENTITY, BUILDER> AbstractColumn<E, ENTITY, BUILDER> genericColumn(
            String name, String prefix, Function<ENTITY, E> getter, BiConsumer<BUILDER, E> setter,
            GenericColumn<E> genericColumn, boolean nullable) {

        return new AbstractColumn<E, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
            @Override
            E fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                return genericColumn.fromResultSet(resultSet, columnName);
            }

            @Override
            void setPreparedStatement(PreparedStatement preparedStatement, int index, E value) throws SQLException {
                genericColumn.setPreparedStatement(preparedStatement, index, value);
            }

            @Override
            int sqlType() {
                return genericColumn.sqlType();
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return genericColumn(getName(), newPrefix, getter, setter, genericColumn, nullable);
            }

            @Override
            public Set<Integer> supportedTypes() {
                return Collections.singleton(genericColumn.sqlType());
            }

            @Override
            public String getSqlType() {
                return genericColumn.getSqlTypeName();
            }
        };
    }

    public static <T, U, ENTITY, BUILDER> AbstractColumn<U, ENTITY, BUILDER> convertedGenericColumn(
            String name, String prefix, Function<ENTITY, U> getter, BiConsumer<BUILDER, U> setter,
            GenericColumn<T> genericColumn, Converter<U,T> converter, boolean nullable) {

        return new AbstractColumn<U, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
            @Override
            U fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                T columnValue = genericColumn.fromResultSet(resultSet, columnName);
                return converter.to(columnValue);
            }

            @Override
            void setPreparedStatement(PreparedStatement preparedStatement, int index, U value) throws SQLException {
                T columnValue = converter.from(value);
                genericColumn.setPreparedStatement(preparedStatement, index, columnValue);
            }

            @Override
            int sqlType() {
                return genericColumn.sqlType();
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return convertedGenericColumn(getName(), newPrefix, getter, setter, genericColumn, converter, nullable);
            }

            @Override
            public Set<Integer> supportedTypes() {
                return Collections.singleton(genericColumn.sqlType());
            }

            @Override
            public String getSqlType() {
                return genericColumn.getSqlTypeName();
            }
        };
    }

}
