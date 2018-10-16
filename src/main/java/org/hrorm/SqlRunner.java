package org.hrorm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * This class does the heavy lifting of creating <code>Statement</code>s,
 * executing SQL, and parsing <code>ResultSet</code>s.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> the type of object this runner supports
 */
public class SqlRunner<T,B> {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    private final Connection connection;
    private final List<? extends TypedColumn<T>> allColumns;
    private final PrimaryKey<T> primaryKey;

    private final Function<B,T> builderFunction;

    public SqlRunner(Connection connection, List<? extends TypedColumn<T>> allColumns, PrimaryKey<T> primaryKey, Function<B,T> builderFunction){
        this.connection = connection;
        this.allColumns = allColumns;
        this.primaryKey = primaryKey;
        this.builderFunction = builderFunction;
    }

    public SqlRunner(Connection connection, DaoDescriptor<T,B> daoDescriptor) {
        this.connection = connection;
        List<TypedColumn<T>> columns = new ArrayList<>();
        columns.addAll(daoDescriptor.dataColumnsWithParent());
        columns.addAll(daoDescriptor.joinColumns());
        this.primaryKey = daoDescriptor.primaryKey();
        this.allColumns = Collections.unmodifiableList(columns);
        this.builderFunction = daoDescriptor.buildFunction();
    }

    public List<T> select(String sql, Supplier<T> supplier, List<ChildrenDescriptor<T,?>> childrenDescriptors){
        return selectByColumns(sql, supplier, Collections.emptyList(), Collections.emptyMap(), childrenDescriptors, null);
    }

    public List<T> selectByColumns(String sql, Supplier<T> supplier, List<String> columnNames, Map<String, TypedColumn<T>> columnNameMap,  List<ChildrenDescriptor<T,?>> childrenDescriptors, T item){
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            int idx = 1;
            for(String columnName : columnNames){
                TypedColumn<T> column = columnNameMap.get(columnName.toUpperCase());
                column.setValue(item, idx, statement);
                idx++;
            }

            logger.info(sql);
            resultSet = statement.executeQuery();

            List<T> results = new ArrayList<>();

            while (resultSet.next()) {
                T result = populate(resultSet, supplier);
                for(ChildrenDescriptor<T,?> descriptor : childrenDescriptors){
                    descriptor.populateChildren(connection, result);
                }
                results.add(result);
            }

            return results;

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

    public void insert(String sql, T item) {
        runInsertOrUpdate(sql, item, false);
    }

    public void update(String sql, T item) {
        runInsertOrUpdate(sql, item, true);
    }

    private void runInsertOrUpdate(String sql, T item, boolean isUpdate){
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            int idx = 1;
            for(TypedColumn<T> column : allColumns){
                if( ! ( isUpdate && column.isPrimaryKey() ) ) {
                    column.setValue(item, idx, preparedStatement);
                    idx++;
                }
            }
            if( isUpdate ){
                Long id = primaryKey.getKey(item);
                preparedStatement.setLong(idx, id);
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

    private T populate(ResultSet resultSet, Supplier<T> supplier)
            throws SQLException {
        T item = supplier.get();

        for (TypedColumn<T> column: allColumns) {
            PopulateResult populateResult = ((DirectTypedColumn<T>)column).populate(item, resultSet);
            populateResult.populateChildren(connection);
        }

        return item;
    }
}
