package org.hrorm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class does the heavy lifting of creating <code>Statement</code>s,
 * executing SQL, and parsing <code>ResultSet</code>s.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> the type of object this runner supports
 * @param <BUILDER> the type of object that can construct new <code>ENTITY</code> instances
 */
public class SqlRunner<ENTITY, BUILDER> {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    private final Connection connection;
    private final List<Column<ENTITY, BUILDER>> allColumns;

    protected SqlRunner(Connection connection, List<Column<ENTITY, BUILDER>> allColumns) {
        this.connection = connection;
        this.allColumns = allColumns;
    }

    public SqlRunner(Connection connection, KeylessDaoDescriptor<ENTITY, BUILDER> daoDescriptor) {
        this(
                connection,
                Collections.unmodifiableList(
                        Stream.concat(
                                daoDescriptor.dataColumnsWithParent().stream(),
                                daoDescriptor.joinColumns().stream()
                        ).collect(Collectors.toList())
                ));
    }

    public List<BUILDER> select(String sql, Supplier<BUILDER> supplier, List<ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors){
        return selectByColumns(sql, supplier, SelectColumnList.EMPTY, Collections.emptyMap(), childrenDescriptors, null);
    }

    public List<BUILDER> selectByColumns(String sql,
                                         Supplier<BUILDER> supplier,
                                         SelectColumnList selectColumnList,
                                         Map<String, ? extends Column<ENTITY,?>> columnNameMap,
                                         List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors,
                                         ENTITY item){
        BiFunction<List<BUILDER>, BUILDER, List<BUILDER>> accumulator =
                (list, b) -> { list.add(b); return list; };
        return foldingSelect(
                sql,
                supplier,
                selectColumnList,
                columnNameMap,
                childrenDescriptors,
                item,
                b -> b,
                new ArrayList<>(),
                accumulator
        );
    }

    public <T,X> T foldingSelect(String sql,
                               Supplier<BUILDER> supplier,
                               SelectColumnList selectColumnList,
                               Map<String, ? extends Column<ENTITY,?>> columnNameMap,
                               List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors,
                               ENTITY template,
                               Function<BUILDER, X> buildFunction,
                               T identity,
                               BiFunction<T,X,T> accumulator){

        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            int idx = 1;
            for(SelectColumnList.ColumnOperatorEntry columnEntry : selectColumnList){
                String columnName = columnEntry.rawName;
                Column<ENTITY,?> column = columnNameMap.get(columnName.toUpperCase());
                column.setValue(template, idx, statement);
                idx++;
                Operator operator = columnEntry.operator;
                if( operator.hasSecondParameter() ) {
                    operator.setSecondParameter(idx, statement);
                    idx++;
                }
            }

            logger.info(sql);
            resultSet = statement.executeQuery();

            T result = identity;

            while (resultSet.next()) {
                BUILDER bldr = populate(resultSet, supplier);
                for(ChildrenDescriptor<ENTITY,?, BUILDER,?> descriptor : childrenDescriptors){
                    descriptor.populateChildren(connection, bldr);
                }
                X item = buildFunction.apply(bldr);
                result = accumulator.apply(result, item);
            }

            return result;

        } catch (SQLException ex){
            throw new HrormException(ex, sql);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException se){
                throw new HrormException(se);
            }
        }
    }

    public void insert(String sql, Envelope<ENTITY> envelope) {
        runInsertOrUpdate(sql, envelope, false);
    }

    public void update(String sql, Envelope<ENTITY> envelope) {
        runInsertOrUpdate(sql, envelope, true);
    }

    private void runInsertOrUpdate(String sql, Envelope<ENTITY> envelope, boolean isUpdate){

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            int idx = 1;
            for(Column<ENTITY, BUILDER> column : allColumns){
                if( column.isPrimaryKey() ) {
                    if ( ! isUpdate ) {
                        preparedStatement.setLong(idx, envelope.getId());
                        idx++;
                    }
                } else if ( column.isParentColumn() ){
                    preparedStatement.setLong(idx, envelope.getParentId());
                    idx++;
                } else if ( ! column.isPrimaryKey()  ){
                    column.setValue(envelope.getItem(), idx, preparedStatement);
                    idx++;
                }
            }
            if( isUpdate ){
                preparedStatement.setLong(idx, envelope.getId());
            }

            logger.info(sql);
            preparedStatement.execute();

        } catch (SQLException se){
            throw new HrormException(se, sql);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se){
                throw new HrormException(se);
            }
        }

    }

    private BUILDER populate(ResultSet resultSet, Supplier<BUILDER> supplier)
            throws SQLException {
        BUILDER item = supplier.get();

        for (Column<ENTITY, BUILDER> column: allColumns) {
            PopulateResult populateResult = column.populate(item, resultSet);
            populateResult.populateChildren(connection);
        }

        return item;
    }
}
