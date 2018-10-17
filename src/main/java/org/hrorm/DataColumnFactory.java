package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
        };
    }


}
