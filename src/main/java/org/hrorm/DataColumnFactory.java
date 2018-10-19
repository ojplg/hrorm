package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
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

    public static <ENTITY,BUILDER> AbstractColumn<BigDecimal, ENTITY, BUILDER> bigDecimalColumn(
            String name, String prefix, Function<ENTITY, BigDecimal> getter, BiConsumer<BUILDER, BigDecimal> setter, boolean nullable){
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
            public Set<Integer> supportedTypes() { return ColumnTypes.DecimalTypes; }
        };
    }

    public static <ENTITY,BUILDER> AbstractColumn<Long, ENTITY, BUILDER> longColumn(
            String name, String prefix, Function<ENTITY, Long> getter, BiConsumer<BUILDER, Long> setter, boolean nullable){
        return new AbstractColumn<Long, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
            @Override
            public Long fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                return resultSet.getLong(columnName);
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
            public Set<Integer> supportedTypes() { return ColumnTypes.IntegerTypes; }
        };
    }

    public static <ENTITY,BUILDER> AbstractColumn<String, ENTITY, BUILDER> stringColumn(
            String name, String prefix, Function<ENTITY, String> getter, BiConsumer<BUILDER, String> setter, boolean nullable){
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
            public Set<Integer> supportedTypes() { return ColumnTypes.StringTypes; }
        };
    }

    public static <ENTITY,BUILDER> AbstractColumn<LocalDateTime, ENTITY, BUILDER> localDateTimeColumn(
            String name, String prefix, Function<ENTITY, LocalDateTime> getter, BiConsumer<BUILDER, LocalDateTime> setter, boolean nullable){
        return new AbstractColumn<LocalDateTime, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
            @Override
            public LocalDateTime fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                Timestamp sqlTime = resultSet.getTimestamp(columnName);
                LocalDateTime value = null;
                if ( sqlTime != null ) {
                    value = sqlTime.toLocalDateTime();
                }
                return value;
            }

            @Override
            public void setPreparedStatement(PreparedStatement preparedStatement, int index, LocalDateTime value) throws SQLException {
                Timestamp sqlTime = Timestamp.valueOf(value);
                preparedStatement.setTimestamp(index, sqlTime);
            }

            @Override
            public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
                return localDateTimeColumn(getName(), newPrefix, getter, setter, nullable);
            }

            @Override
            int sqlType() {
                return Types.TIMESTAMP;
            }

            @Override
            public Set<Integer> supportedTypes() { return ColumnTypes.LocalDateTimeTypes; }
        };
    }


    public static <E,ENTITY,BUILDER> AbstractColumn<E, ENTITY, BUILDER> stringConverterColumn(
            String name, String prefix, Function<ENTITY, E> getter, BiConsumer<BUILDER, E> setter,
            Converter<E,String> converter, boolean nullable){
        return new AbstractColumn<E, ENTITY, BUILDER>(name, prefix, getter, setter, nullable) {
            @Override
            public E fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
                String code = resultSet.getString(columnName);
                if ( code == null ) {
                    return null;
                }
                E value = converter.to(code);
                return value;
            }

            @Override
            public void setPreparedStatement(PreparedStatement preparedStatement, int index, E value) throws SQLException {
                String code = null;
                if ( value != null ){
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
            public Set<Integer> supportedTypes() { return ColumnTypes.StringTypes; }
        };
    }

}
