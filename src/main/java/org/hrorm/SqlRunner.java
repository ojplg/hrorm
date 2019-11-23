package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    private final List<Column<?, ?,ENTITY, BUILDER>> allColumns;
    private final List<JoinColumn<ENTITY,?,BUILDER,?>> joinColumns;
    private final KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor;

    public SqlRunner(Connection connection){
        this.connection = connection;
        this.allColumns = Collections.emptyList();
        this.joinColumns = Collections.emptyList();
        this.keylessDaoDescriptor = null;
    }

    public SqlRunner(Connection connection, KeylessDaoDescriptor<ENTITY, BUILDER> daoDescriptor) {
        this.connection = connection;
        this.allColumns = daoDescriptor.allColumns();
        this.joinColumns = daoDescriptor.joinColumns();
        this.keylessDaoDescriptor = daoDescriptor;
    }

    public List<BUILDER> selectStandard(String sql, Supplier<BUILDER> supplier, List<ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors){
        return selectByColumnsStandard(sql, supplier, ColumnSelection.empty(), childrenDescriptors, null);
    }

    public List<BUILDER> selectByColumnsStandard(String sql,
                                                 Supplier<BUILDER> supplier,
                                                 ColumnSelection<ENTITY,BUILDER> columnSelection,
                                                 List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors,
                                                 ENTITY item){
        BiFunction<List<BUILDER>, BUILDER, List<BUILDER>> accumulator =
                (list, b) -> { list.add(b); return list; };
        StatementPopulator populator = columnSelection.buildPopulator(item);
        return foldingSelect(
                sql,
                populator,
                supplier,
                childrenDescriptors,
                b -> b,
                new ArrayList<>(),
                accumulator
        );
    }

    public List<Envelope<BUILDER>> doSelection(SelectionInstruction selectionInstruction,
                                               Supplier<BUILDER> supplier,
                                               List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors,
                                               StatementPopulator statementPopulator) {

        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(selectionInstruction.getSelectSql());
            statementPopulator.populate(statement);

            logger.info(selectionInstruction.getSelectSql());
            resultSet = statement.executeQuery();

            List<Envelope<BUILDER>> builders = new ArrayList<>();

            JoinedChildrenSelector joinedChildrenSelector = new JoinedChildrenSelector(keylessDaoDescriptor, selectionInstruction);

            while (resultSet.next()) {
                Envelope<BUILDER> builder = populate(resultSet, supplier, selectionInstruction.getParentColumnName(), joinedChildrenSelector);
                builders.add(builder);
            }

            if ( selectionInstruction.isBulkChildSelectStrategy()) {
                for (ChildrenDescriptor<ENTITY, ?, BUILDER, ?> descriptor : childrenDescriptors) {
                    ChildrenBuilderSelectCommand<?, ?> childrenBuilderSelectCommand;
                    if( selectionInstruction.isSelectAll()) {
                        childrenBuilderSelectCommand = ChildrenBuilderSelectCommand.forSelectAll();
                    } else if (selectionInstruction.getChildSelectStrategy().equals(ChildSelectStrategy.SubSelectInClause)) {
                        childrenBuilderSelectCommand =
                                ChildrenBuilderSelectCommand.forSubSelect(selectionInstruction.getPrimaryKeySql(), statementPopulator);
                    } else {
                        List<Long> parentIds = builders.stream().map(Envelope::getId).collect(Collectors.toList());
                        childrenBuilderSelectCommand =
                                ChildrenBuilderSelectCommand.forSelectByIds(parentIds);
                    }
                    descriptor.populateChildren(connection, builders, childrenBuilderSelectCommand);
                }
            }

            joinedChildrenSelector.populateChildren(connection, statementPopulator);

            return builders;

        } catch (SQLException ex){
            throw new HrormException(ex, selectionInstruction.getSelectSql());
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

    public List<BUILDER> selectWhereStandard(String sql,
                                             Supplier<BUILDER> supplier,
                                             List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors,
                                             Where where){
        BiFunction<List<BUILDER>, BUILDER, List<BUILDER>> accumulator =
                (list, b) -> { list.add(b); return list; };
        return foldingSelect(
                sql,
                where,
                supplier,
                childrenDescriptors,
                b -> b,
                new ArrayList<>(),
                accumulator
        );
    }

    public <T,X> T foldingSelect(String sql,
                               StatementPopulator statementPopulator,
                               Supplier<BUILDER> supplier,
                               List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors,
                               Function<BUILDER, X> buildFunction,
                               T identity,
                               BiFunction<T,X,T> accumulator){

        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statementPopulator.populate(statement);

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

    private <T> T runFunction(String sql,
                              Where where,
                              Function<ResultSet, T> reader) {
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            where.populate(statement);

            logger.info(sql);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return reader.apply(resultSet);
            } else {
                return null;
            }
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

    public BigDecimal runBigDecimalFunction(String sql,
                                            Where where) {
        Function<ResultSet, BigDecimal> reader = resultSet -> {
            try {
                return resultSet.getBigDecimal(1);
            } catch (SQLException ex){
                throw new HrormException(ex, sql);
            }
        };
        return runFunction(sql, where, reader);
    }

    public Long runLongFunction(String sql,
                                Where where) {
        Function<ResultSet, Long> reader = resultSet -> {
            try {
                return resultSet.getLong(1);
            } catch (SQLException ex){
                throw new HrormException(ex, sql);
            }
        };
        return runFunction(sql, where, reader);
    }

    public <T> List<T> selectDistinct(String sql, Where where, Function<ResultSet, T> resultParser){
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        List<T> values = new ArrayList<>();
        try {
            statement = connection.prepareStatement(sql);
            where.populate(statement);

            logger.info(sql);
            resultSet = statement.executeQuery();

            while(resultSet.next()){
                T value = resultParser.apply(resultSet);
                values.add(value);
            }
            return values;
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
            for(Column<?, ?, ENTITY, BUILDER> column : allColumns){
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

    public void runPreparedDelete(String sql, Long id){
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setLong(1, id);
            logger.info(sql);
            preparedStatement.execute();
        } catch (SQLException ex){
            throw new HrormException(ex, sql);
        }
    }


    public long runSequenceNextValue(String sql) {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            logger.info(sql);
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException ex){
            throw new HrormException(ex, sql);
        } finally {
            try {
                if ( resultSet != null ){
                    resultSet.close();
                }
                if ( statement != null){
                    statement.close();
                }
            } catch (SQLException ex){
                throw new HrormException(ex);
            }
        }
    }

    public Set<Long> runSelectChildIds(String sql, Long id){
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            Set<Long> longs = new HashSet<>();
            logger.info(sql);
            statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                longs.add(resultSet.getLong(1));
            }
            return longs;
        } catch (SQLException ex){
            throw new HrormException(ex, sql);
        } finally {
            try {
                if ( resultSet != null ){
                    resultSet.close();
                }
                if ( statement != null){
                    statement.close();
                }
            } catch (SQLException ex){
                throw new HrormException(ex);
            }
        }
    }


    private BUILDER populate(ResultSet resultSet, Supplier<BUILDER> supplier)
            throws SQLException {
        BUILDER item = supplier.get();

        for (Column<?, ?, ENTITY, BUILDER> column: allColumns) {
            PopulateResult populateResult = column.populate(item, resultSet);
            populateResult.populateChildren(connection);
        }

        return item;
    }

    private Envelope<BUILDER> populate(ResultSet resultSet, Supplier<BUILDER> supplier, String parentColumName, JoinedChildrenSelector joinedChildrenSelector)
            throws SQLException {
        BUILDER item = supplier.get();
        Long parentId = null;
        Long itemId = null;

        for (Column<?, ?, ENTITY, BUILDER> column: allColumns) {
            PopulateResult populateResult = column.populate(item, resultSet);

            if ( populateResult.isJoinedItemResult() ){
                Envelope<Object> envelope = populateResult.getJoinedItem();
                joinedChildrenSelector.addChildEntityInfo(column.getName(),envelope);
            } else {
                populateResult.populateChildren(connection);
            }
            if( column.getName().equalsIgnoreCase(parentColumName)){
                parentId  = resultSet.getLong(column.getPrefix() + column.getName());
            }
            if( column.isPrimaryKey() ){
                itemId = resultSet.getLong(column.getPrefix() + column.getName());
            }
        }

        return new Envelope<>(item, itemId, parentId);
    }

}
