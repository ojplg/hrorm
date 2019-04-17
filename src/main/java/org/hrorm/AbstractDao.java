package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
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

    protected final KeylessSqlBuilder<ENTITY> keylessSqlBuilder;
    protected final SqlRunner<ENTITY, BUILDER> sqlRunner;

    private final String tableName;
    private final Supplier<BUILDER> supplier;
    private final Function<BUILDER, ENTITY> buildFunction;
    private final ColumnCollection<ENTITY, BUILDER> columnCollection;

    public AbstractDao(Connection connection,
                       KeylessDaoDescriptor<ENTITY, BUILDER> daoDescriptor){
        this.connection = connection;
        this.tableName = daoDescriptor.tableName();
        this.columnCollection = daoDescriptor.getColumnCollection();
        this.supplier = daoDescriptor.supplier();
        this.buildFunction = daoDescriptor.buildFunction();

        this.keylessSqlBuilder = new KeylessSqlBuilder<>(daoDescriptor);
        this.sqlRunner = new SqlRunner<>(connection, daoDescriptor);
    }

    protected abstract List<ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors();

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
    public Long insert(ENTITY item) {
        String sql = keylessSqlBuilder.insert();
        Envelope<ENTITY> envelope = new Envelope(item);
        sqlRunner.insert(sql, envelope);
        for(ChildrenDescriptor<ENTITY,?, BUILDER,?> childrenDescriptor : childrenDescriptors()){
            childrenDescriptor.saveChildren(connection, envelope);
        }
        return null;
    }

    protected List<ENTITY> mapBuilders(List<BUILDER> bs){
        return bs.stream().map(buildFunction).collect(Collectors.toList());
    }

    @Override
    public List<ENTITY> selectAll() {
        String sql = keylessSqlBuilder.select();
        List<BUILDER> bs = sqlRunner.select(sql, supplier, childrenDescriptors());
        return mapBuilders(bs);
    }

    @Override
    public List<ENTITY> selectAll(Order order) {
        String sql = keylessSqlBuilder.select(order);
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
        String sql = keylessSqlBuilder.selectByColumns(columnSelection);
        List<BUILDER> bs = sqlRunner.selectByColumns(sql, supplier, select(columnNames), childrenDescriptors(), item);
        return mapBuilders(bs);
    }

    @Override
    public List<ENTITY> selectManyByColumns(ENTITY template, Order order, String... columnNames) {
        ColumnSelection columnSelection = select(columnNames);
        String sql = keylessSqlBuilder.selectByColumns(columnSelection, order);
        List<BUILDER> bs = sqlRunner.selectByColumns(sql, supplier, select(columnNames), childrenDescriptors(), template);
        return mapBuilders(bs);
    }

    @Override
    public Long runLongFunction(SqlFunction function,
                                String columnName,
                                Where where) {
        String sql = keylessSqlBuilder.selectFunction(function, columnName, where);
        return sqlRunner.runLongFunction(sql, where);
    }

    @Override
    public BigDecimal runBigDecimalFunction(SqlFunction function,
                                            String columnName,
                                            Where where) {
        String sql = keylessSqlBuilder.selectFunction(function, columnName, where);
        return sqlRunner.runBigDecimalFunction(sql, where);
    }


    @Override
    public <T> T foldingSelect(T identity, BiFunction<T,ENTITY,T> accumulator, Where where){
        String sql = keylessSqlBuilder.select(where) ;
        return sqlRunner.foldingSelect(sql, where, supplier, childrenDescriptors(), buildFunction, identity, accumulator);
    }

    @Override
    public List<ENTITY> select(Where where) {
        String sql = keylessSqlBuilder.select(where);
        List<BUILDER> bs = sqlRunner.selectWhere(sql, supplier, childrenDescriptors(), where);
        return mapBuilders(bs);
    }

    @Override
    public List<ENTITY> select(Where where, Order order) {
        String sql = keylessSqlBuilder.select(where, order);
        List<BUILDER> bs = sqlRunner.selectWhere(sql, supplier, childrenDescriptors(), where);
        return mapBuilders(bs);
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
