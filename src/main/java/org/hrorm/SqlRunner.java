package org.hrorm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public Stream<BUILDER> select(String sql, Supplier<BUILDER> supplier, List<ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors){
        return selectByColumns(sql, supplier, Collections.emptyList(), Collections.emptyMap(), childrenDescriptors, null);
    }

    public Stream<BUILDER> selectByColumns(String sql, Supplier<BUILDER> supplier, List<String> columnNames, Map<String, ? extends Column<ENTITY,?>> columnNameMap, List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors, ENTITY item){
        return foldingSelect(
                sql,
                supplier,
                columnNames,
                columnNameMap,
                childrenDescriptors,
                item
        );
    }

    public Stream<BUILDER> foldingSelect(String sql,
                               Supplier<BUILDER> supplier,
                               List<String> columnNames,
                               Map<String, ? extends Column<ENTITY,?>> columnNameMap,
                               List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors,
                               ENTITY template) {

        UncheckedCloseable close = UncheckedCloseable.wrap(connection);
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setFetchSize(5000);
            close = close.nest(statement);
            int idx = 1;
            for (String columnName : columnNames) {
                Column<ENTITY, ?> column = columnNameMap.get(columnName.toUpperCase());
                column.setValue(template, idx, statement);
                idx++;
            }

            logger.info(sql);
            ResultSet resultSet = statement.executeQuery();
            close = close.nest(resultSet);

            return StreamSupport.stream(new Spliterators.AbstractSpliterator<BUILDER>(
                    Long.MAX_VALUE,Spliterator.ORDERED) {
                @Override
                public boolean tryAdvance(Consumer<? super BUILDER> action) {
                    try {
                        if (resultSet.isClosed() || !resultSet.next()) return false;
                        BUILDER bldr = populate(resultSet, supplier);
                        for (ChildrenDescriptor<ENTITY, ?, BUILDER, ?> descriptor : childrenDescriptors) {
                            descriptor.populateChildren(connection, bldr);
                        }
                        if (bldr == null) return false;
                        action.accept(bldr);
                        return true;
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, false).onClose(close);

        } catch (SQLException e) {
            if(close!=null)
                try { close.close(); } catch(Exception ex) { e.addSuppressed(ex); }
            throw new HrormException(e, sql);
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
