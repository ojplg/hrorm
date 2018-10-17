package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes a column with a decimal value that can be mapped
 * to a <code>BigDecimal</code>
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The entity type this column belongs to
 */
public class BigDecimalColumn<ENTITY, BUILDER> extends  AbstractColumn<BigDecimal, ENTITY, BUILDER> {

    public BigDecimalColumn(String name, String prefix, Function<ENTITY, BigDecimal> getter, BiConsumer<BUILDER, BigDecimal> setter, boolean nullable) {
        super(name, prefix, getter, setter, nullable);
    }

    @Override
    public Column<ENTITY, BUILDER> withPrefix(String prefix, Prefixer prefixer) {
        return new BigDecimalColumn<>(getName(), prefix, getter, setter, nullable);
    }

    @Override
    public BigDecimal fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBigDecimal(columnName);
    }

    @Override
    public void setPreparedStatement(PreparedStatement preparedStatement, int index, BigDecimal value) throws SQLException {
        preparedStatement.setBigDecimal(index, value);
    }
}
