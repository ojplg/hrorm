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
    private final List<IndirectTypedColumn<T,B>> allColumns;
    private final IndirectPrimaryKey<T,B> primaryKey;

    private final Function<B,T> builderFunction;

    public SqlRunner(Connection connection, List<IndirectTypedColumn<T,B>> allColumns, IndirectPrimaryKey<T,B> primaryKey, Function<B,T> builderFunction){
        this.connection = connection;
        this.allColumns = allColumns;
        this.primaryKey = primaryKey;
        this.builderFunction = builderFunction;
    }

    public SqlRunner(Connection connection, DaoDescriptor<T,B> daoDescriptor) {
        this.connection = connection;
        List<IndirectTypedColumn<T,B>> columns = new ArrayList<>();
        columns.addAll(daoDescriptor.dataColumnsWithParent());
        columns.addAll(daoDescriptor.joinColumns());
        this.primaryKey = daoDescriptor.primaryKey();
        this.allColumns = Collections.unmodifiableList(columns);
        this.builderFunction = daoDescriptor.buildFunction();
    }

    public List<B> select(String sql, Supplier<B> supplier, List<ChildrenDescriptor<T,?,B,?>> childrenDescriptors){
        return selectByColumns(sql, supplier, Collections.emptyList(), Collections.emptyMap(), childrenDescriptors, null);
    }

    public List<B> selectByColumns(String sql, Supplier<B> supplier, List<String> columnNames, Map<String, TypedColumn<T>> columnNameMap,  List<? extends ChildrenDescriptor<T,?,B,?>> childrenDescriptors, T item){
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            int idx = 1;
            for(String columnName : columnNames){

                logger.info("Setting " + columnName);

                TypedColumn<T> column = columnNameMap.get(columnName.toUpperCase());
                column.setValue(item, idx, statement);
                idx++;
            }

            logger.info(sql);
            resultSet = statement.executeQuery();

            List<B> results = new ArrayList<>();

            while (resultSet.next()) {
                logger.info("Working on result set!");

                B result = populate(resultSet, supplier);
                for(ChildrenDescriptor<T,?,B,?> descriptor : childrenDescriptors){
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

    public void insert(String sql, T item, long id, long parentId) {
        runInsertOrUpdate(sql, item, id, parentId,false);
    }

    public void update(String sql, T item) {
        Long id = primaryKey.getKey(item);
        runInsertOrUpdate(sql, item, id, -34575, true);
    }

    private void runInsertOrUpdate(String sql, T item, long id, long parentId, boolean isUpdate){
        if( item == null ){
            throw new HrormException("Cannot insert or update a null item");
        }

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            int idx = 1;
            for(TypedColumn<T> column : allColumns){
                if( column.isPrimaryKey() ) {
                    if ( ! isUpdate ) {
                        preparedStatement.setLong(idx, id);
                        idx++;
                    }
                } else if ( column.isParentColumn() ){
                    preparedStatement.setLong(idx, parentId);
                    idx++;
                } else if ( ! column.isPrimaryKey()  ){
                    column.setValue(item, idx, preparedStatement);
                    idx++;
                }
            }
            if( isUpdate ){
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

    private B populate(ResultSet resultSet, Supplier<B> supplier)
            throws SQLException {
        B item = supplier.get();

        for (IndirectTypedColumn<T,B> column: allColumns) {
            PopulateResult populateResult = column.populate(item, resultSet);
            populateResult.populateChildren(connection);
        }

        return item;
    }
}
