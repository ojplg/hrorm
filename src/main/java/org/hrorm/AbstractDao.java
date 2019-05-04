package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An abstract class that is almost a <code>KeylessDao</code>.
 *
 * <p>
 *     Most users of hrorm will have no need to directly use this.
 * </p>
 *
 * @param <ENTITY> The type whose persistence is managed by this <code>Dao</code>.
 * @param <BUILDER> The type of object that can build an <code>ENTITY</code> instance.
 */
public abstract class AbstractDao<ENTITY, BUILDER> implements KeylessDaoDescriptor<ENTITY, BUILDER>, KeylessDao<ENTITY> {

    protected final Connection connection;

    protected final SqlBuilder<ENTITY> sqlBuilder;
    protected final SqlRunner<ENTITY, BUILDER> sqlRunner;

    private final String tableName;
    private final Supplier<BUILDER> supplier;
    private final Function<BUILDER, ENTITY> buildFunction;
    private final ColumnCollection<ENTITY, BUILDER> columnCollection;

    public AbstractDao(Connection connection,
                       KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor){
        this.connection = connection;
        this.tableName = keylessDaoDescriptor.tableName();
        this.columnCollection = keylessDaoDescriptor.getColumnCollection();
        this.supplier = keylessDaoDescriptor.supplier();
        this.buildFunction = keylessDaoDescriptor.buildFunction();

        this.sqlBuilder = new SqlBuilder<>(keylessDaoDescriptor);
        this.sqlRunner = new SqlRunner<>(connection, keylessDaoDescriptor);
    }

    public AbstractDao(Connection connection,
                       DaoDescriptor<ENTITY, BUILDER> daoDescriptor){
        this.connection = connection;
        this.tableName = daoDescriptor.tableName();
        this.columnCollection = daoDescriptor.getColumnCollection();
        this.supplier = daoDescriptor.supplier();
        this.buildFunction = daoDescriptor.buildFunction();

        this.sqlBuilder = new SqlBuilder<>(daoDescriptor);
        this.sqlRunner = new SqlRunner<>(connection, daoDescriptor);
    }

    protected abstract List<ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors();

    public abstract Long insert(ENTITY item);

    @Override
    public String tableName() {
        return tableName;
    }

    @Override
    public Supplier<BUILDER> supplier() {
        return supplier;
    }

    @Override
    public Function<BUILDER, ENTITY> buildFunction() {
        return buildFunction;
    }

    @Override
    public ColumnCollection<ENTITY, BUILDER> getColumnCollection() {
        return columnCollection;
    }

    @Override
    public Long atomicInsert(ENTITY item) {
        Transactor transactor = new Transactor(connection);
        return transactor.runAndCommit(
                con -> { return insert(item); }
        );
    }

    @Override
    public List<ENTITY> selectAll() {
        String sql = sqlBuilder.select();
        List<BUILDER> bs = sqlRunner.select(sql, supplier, childrenDescriptors());
        return mapBuilders(bs);
    }

    @Override
    public List<ENTITY> selectAll(Order order) {
        String sql = sqlBuilder.select(order);
        List<BUILDER> bs = sqlRunner.select(sql, supplier, childrenDescriptors());
        return mapBuilders(bs);
    }


    @Override
    public ENTITY selectByColumns(ENTITY item, String ... columnNames){
        List<ENTITY> items = selectManyByColumns(item, columnNames);
        return fromSingletonList(items);
    }

    @Override
    public List<ENTITY> selectManyByColumns(ENTITY item, String ... columnNames) {
        ColumnSelection columnSelection = select(columnNames);
        String sql = sqlBuilder.selectByColumns(columnSelection);
        List<BUILDER> bs = sqlRunner.selectByColumns(sql, supplier, select(columnNames), childrenDescriptors(), item);
        return mapBuilders(bs);
    }

    @Override
    public List<ENTITY> selectManyByColumns(ENTITY template, Order order, String... columnNames) {
        ColumnSelection columnSelection = select(columnNames);
        String sql = sqlBuilder.selectByColumns(columnSelection, order);
        List<BUILDER> bs = sqlRunner.selectByColumns(sql, supplier, select(columnNames), childrenDescriptors(), template);
        return mapBuilders(bs);
    }

    @Override
    public Long runLongFunction(SqlFunction function,
                                String columnName,
                                Where where) {
        String sql = sqlBuilder.selectFunction(function, columnName, where);
        return sqlRunner.runLongFunction(sql, where);
    }

    @Override
    public BigDecimal runBigDecimalFunction(SqlFunction function,
                                            String columnName,
                                            Where where) {
        String sql = sqlBuilder.selectFunction(function, columnName, where);
        return sqlRunner.runBigDecimalFunction(sql, where);
    }


    @Override
    public <T> T foldingSelect(T identity, BiFunction<T,ENTITY,T> accumulator, Where where){
        String sql = sqlBuilder.select(where) ;
        return sqlRunner.foldingSelect(sql, where, supplier, childrenDescriptors(), buildFunction, identity, accumulator);
    }

    @Override
    public List<ENTITY> select(Where where) {
        String sql = sqlBuilder.select(where);
        List<BUILDER> bs = sqlRunner.selectWhere(sql, supplier, childrenDescriptors(), where);
        return mapBuilders(bs);
    }

    @Override
    public List<ENTITY> select(Where where, Order order) {
        String sql = sqlBuilder.select(where, order);
        List<BUILDER> bs = sqlRunner.selectWhere(sql, supplier, childrenDescriptors(), where);
        return mapBuilders(bs);
    }

    @Override
    public <T> List<T> selectDistinct(String columnName, Where where) {
        String sql = sqlBuilder.selectDistinct(where, columnName);
        // This cast is unfortunate. But I do not see how to avoid it, except
        // by requiring the user to specify the column type in the Dao interface.
        Column<?,T,ENTITY, BUILDER> column = (Column<?,T,ENTITY,BUILDER>) columnCollection.columnByName(columnName);
        return sqlRunner.selectDistinct(sql, where, column::fromResultSet);
    }

    @Override
    public <T,U> List<Pair<T,U>> selectDistinct(String firstColumnName, String secondColumnName, Where where) {
        String sql = sqlBuilder.selectDistinct(where, firstColumnName, secondColumnName);
        // Casting as above
        Column<?,T,ENTITY, BUILDER> firstColumn = (Column<?,T,ENTITY,BUILDER>) columnCollection.columnByName(firstColumnName);
        Column<?,U,ENTITY, BUILDER> secondColumn = (Column<?,U,ENTITY,BUILDER>) columnCollection.columnByName(secondColumnName);
        Function<ResultSet,Pair<T,U>> reader = rs ->
        {
            T t = firstColumn.fromResultSet(rs);
            U u = secondColumn.fromResultSet(rs);
            return new Pair<>(t, u);
        };
        return sqlRunner.selectDistinct(sql, where, reader);
    }

    @Override
    public <T, U, V> List<Triplet<T, U, V>> selectDistinct(String firstColumnName, String secondColumnName, String thirdColumnName, Where where) {
        String sql = sqlBuilder.selectDistinct(where, firstColumnName, secondColumnName, thirdColumnName);
        // Casting as above
        Column<?,T,ENTITY, BUILDER> firstColumn = (Column<?,T,ENTITY,BUILDER>) columnCollection.columnByName(firstColumnName);
        Column<?,U,ENTITY, BUILDER> secondColumn = (Column<?,U,ENTITY,BUILDER>) columnCollection.columnByName(secondColumnName);
        Column<?,V,ENTITY, BUILDER> thirdColumn = (Column<?,V,ENTITY,BUILDER>) columnCollection.columnByName(thirdColumnName);
        Function<ResultSet,Triplet<T,U,V>> reader = rs ->
        {
            T t = firstColumn.fromResultSet(rs);
            U u = secondColumn.fromResultSet(rs);
            V v = thirdColumn.fromResultSet(rs);
            return new Triplet<>(t, u, v);
        };
        return sqlRunner.selectDistinct(sql, where, reader);
    }

    private List<ENTITY> mapBuilders(List<BUILDER> bs){
        return bs.stream().map(buildFunction).collect(Collectors.toList());
    }

    public static <A> A fromSingletonList(List<A> items) {
        if (items.isEmpty()) {
            return null;
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        throw new HrormException("Found " + items.size() + " items.");
    }
}
