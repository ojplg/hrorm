package org.hrorm;

import com.sun.jndi.cosnaming.CNCtx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class IndirectDaoImpl<T, CONSTRUCTOR> implements Dao<T> {

    private static final Logger logger = Logger.getLogger("org.hrorm");


    private final Connection connection;
    private final ImmutableObjectPrimaryKey<T, CONSTRUCTOR> primaryKey;
    private final List<IndirectTypedColumn<T, CONSTRUCTOR>> dataColumns;
    private final SqlBuilder<T> sqlBuilder;
    private final SqlRunner<T,CONSTRUCTOR> sqlRunner;
    private final Supplier<CONSTRUCTOR> constructorSupplier;
    private final Function<CONSTRUCTOR, T> construct;


    public IndirectDaoImpl(Connection connection,
                           String tableName,
                           ImmutableObjectPrimaryKey<T, CONSTRUCTOR> primaryKey,
                           List<IndirectTypedColumn<T, CONSTRUCTOR>> dataColumns,
                           Supplier<CONSTRUCTOR> constructorSupplier,
                           Function<CONSTRUCTOR, T> construct){
        this.connection = connection;
        this.primaryKey = primaryKey;
        this.dataColumns = dataColumns;
        this.construct = construct;
        this.constructorSupplier = constructorSupplier;

        List<IndirectTypedColumn<T,CONSTRUCTOR>> sqlBuilderColumns = new ArrayList<>();
        sqlBuilderColumns.add(primaryKey);
        sqlBuilderColumns.addAll(dataColumns);

        this.sqlBuilder = new SqlBuilder<T>(
                tableName,
                sqlBuilderColumns,
                Collections.emptyList(),
                primaryKey
        );

        this.sqlRunner = new SqlRunner<>(
                connection,
                dataColumns,
                primaryKey,
                construct
        );
    }

    @Override
    public long insert(T item) {
        long id = DaoHelper.getNextSequenceValue(connection, primaryKey.getSequenceName());
        // TODO: need to return a whole new instance here? or what?
//        primaryKey.setKey(item, id);
        String sql = sqlBuilder.insert();
        logger.info(sql);

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, id);
            int idx = 2;
            for(TypedColumn<T> column : dataColumns){
                column.setValue(item, idx, preparedStatement);
                idx++;
            }

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



        return id;
    }

    @Override
    public void update(T item) {

    }

    @Override
    public void delete(T item) {

    }

    @Override
    public T select(long id) {

        String primaryKeyName = primaryKey.getName();
        String sql = sqlBuilder.selectByColumns(primaryKeyName);

        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setLong(1, id);

            logger.info(sql);
            resultSet = statement.executeQuery();

            List<CONSTRUCTOR> results = new ArrayList<>();

            while (resultSet.next()) {
                CONSTRUCTOR result = constructorSupplier.get();

                primaryKey.populate(result,resultSet );

                for( IndirectTypedColumn<T, CONSTRUCTOR> dataColumn : dataColumns){
                    dataColumn.populate(result, resultSet);
                }
                results.add(result);
            }

            CONSTRUCTOR constructor = results.get(0);

            return construct.apply(constructor);

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

    @Override
    public List<T> selectMany(List<Long> ids) {
        return null;
    }

    @Override
    public List<T> selectAll() {
        return null;
    }

    @Override
    public T selectByColumns(T item, String... columnNames) {
        return null;
    }

    @Override
    public List<T> selectManyByColumns(T item, String... columnNames) {
        return null;
    }

    @Override
    public long atomicInsert(T item) {
        return 0;
    }

    @Override
    public void atomicUpdate(T item) {

    }

    @Override
    public void atomicDelete(T item) {

    }
}
