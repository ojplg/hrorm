package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractKeylessDao<ENTITY, BUILDER> implements KeylessDaoDescriptor<ENTITY, BUILDER>, UnkeyedDao<ENTITY> {

    protected final Connection connection;

    protected final SqlBuilder<ENTITY> sqlBuilder;
    protected final SqlRunner<Object, ENTITY, BUILDER> sqlRunner;

    private final String tableName;
    private final Supplier<BUILDER> supplier;
    private final Function<BUILDER, ENTITY> buildFunction;
    private final ColumnCollection<Object, ENTITY, BUILDER> columnCollection;

    public AbstractKeylessDao(Connection connection,
                              KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor){
        this.connection = connection;
        this.tableName = keylessDaoDescriptor.tableName();
        this.columnCollection = (ColumnCollection<Object, ENTITY, BUILDER>) keylessDaoDescriptor.getColumnCollection();
        this.supplier = keylessDaoDescriptor.supplier();
        this.buildFunction = keylessDaoDescriptor.buildFunction();

        // TODO: Can this hideousness be fixed. Perhaps with static factory methods?
        if( keylessDaoDescriptor instanceof DaoDescriptor ) {
            this.sqlBuilder = new SqlBuilder<>((DaoDescriptor) keylessDaoDescriptor);
        } else {
            this.sqlBuilder = new SqlBuilder<>(keylessDaoDescriptor);
        }
        this.sqlRunner = new SqlRunner<>(connection, keylessDaoDescriptor);
    }

    protected abstract List<ChildrenDescriptor<ENTITY,?, BUILDER,?, ?>> childrenDescriptors();

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
    public ColumnCollection<Object, ENTITY, BUILDER> getColumnCollection() {
        return columnCollection;
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
    public <T,DBTYPE> List<T> selectDistinct(String columnName, Where where) {
        String sql = sqlBuilder.selectDistinct(columnName, where);
        // This cast is unfortunate. But I do not see how to avoid it, except
        // by requiring the user to specify the column type in the Dao interface.
        Column<DBTYPE,T,ENTITY, BUILDER> column = (Column<DBTYPE,T,ENTITY,BUILDER>) columnCollection.columnByName(columnName);
        ResultSetReader<T> reader = (res, col) -> {
            DBTYPE dbType = column.getReader().read(res, col);
            return column.toClassType(dbType);
        };
        List<T> values = sqlRunner.selectDistinct(sql, where, columnName, reader);
        return values;
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
