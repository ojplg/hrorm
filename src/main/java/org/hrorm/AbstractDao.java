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
 * An abstract class that aids in creating full <code>Dao</code>
 * implementations.
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

    private final ChildSelectStrategy childSelectStrategy;

    public AbstractDao(Connection connection,
                       KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor){
        this.connection = connection;
        this.tableName = keylessDaoDescriptor.tableName();
        this.columnCollection = keylessDaoDescriptor.getColumnCollection();
        this.supplier = keylessDaoDescriptor.supplier();
        this.buildFunction = keylessDaoDescriptor.buildFunction();

        this.childSelectStrategy = ChildSelectStrategy.Standard;
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

        this.childSelectStrategy = daoDescriptor.childSelectStrategy();
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
    public List<ENTITY> select() {
        String sql = sqlBuilder.select();
        return doSelectAll(sql);
    }

    @Override
    public List<ENTITY> select(Order order) {
        String sql = sqlBuilder.select(order);
        return doSelectAll(sql);
    }

    private List<ENTITY> doSelectAll(String sql){
        switch (childSelectStrategy) {
            case Standard:
                List<BUILDER> bs = sqlRunner.selectStandard(sql, supplier, childrenDescriptors());
                return mapBuilders(bs);
            case ByKeysInClause:
            case SubSelectInClause:
                SelectionInstruction selectionInstruction = SelectionInstruction.forSelectAll(sql, childSelectStrategy);
                List<Envelope<BUILDER>> ebs = sqlRunner.doSelection(selectionInstruction,  supplier, childrenDescriptors(), new StatementPopulator.Empty());
                return mapEnvelopedBuilders(ebs);
            default:
                throw new HrormException("Unsupported child select strategy " + childSelectStrategy);
        }
    }

    @Override
    public ENTITY selectOne(ENTITY item, String ... columnNames){
        List<ENTITY> items = select(item, columnNames);
        return fromSingletonList(items);
    }

    @Override
    public ENTITY selectOne(Where where){
        List<ENTITY> items = select(where);
        return fromSingletonList(items);
    }

    @Override
    public List<ENTITY> select(ENTITY item, String ... columnNames) {
        ColumnSelection columnSelection = select(columnNames);
        String sql = sqlBuilder.selectByColumns(columnSelection);
        return doSelectByColumns(item, sql, columnNames);
    }

    @Override
    public List<ENTITY> select(ENTITY item, Order order, String... columnNames) {
        ColumnSelection columnSelection = select(columnNames);
        String sql = sqlBuilder.selectByColumns(columnSelection, order);
        return doSelectByColumns(item, sql, columnNames);
    }

    private List<ENTITY> doSelectByColumns(ENTITY item, String sql, String ... columnNames){
        switch (childSelectStrategy) {
            case Standard:
                List<BUILDER> bs = sqlRunner.selectByColumnsStandard(sql, supplier, select(columnNames), childrenDescriptors(), item);
                return mapBuilders(bs);
            case ByKeysInClause:
                ColumnSelection columnSelection = select(columnNames);
                StatementPopulator populator = columnSelection.buildPopulator(item);
                SelectionInstruction selectionInstruction = new SelectionInstruction(
                        sql, null, ChildSelectStrategy.ByKeysInClause, null, false
                );
                List<Envelope<BUILDER>> ebs = sqlRunner.doSelection(selectionInstruction, supplier, childrenDescriptors(), populator);
                return mapEnvelopedBuilders(ebs);
            default:
                throw new HrormException("Unsupported child select strategy " + childSelectStrategy);
        }
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
        return doSelect(sql, where);
    }

    @Override
    public List<ENTITY> select(Where where, Order order) {
        String sql = sqlBuilder.select(where, order);
        return doSelect(sql, where);
    }

    private List<ENTITY> doSelect(String sql, Where where){
        switch (childSelectStrategy) {
            case Standard:
                List<BUILDER> bs = sqlRunner.selectWhereStandard(sql, supplier, childrenDescriptors(), where);
                return mapBuilders(bs);
            case ByKeysInClause:
                SelectionInstruction selectionInstruction = new SelectionInstruction(
                        sql, null, childSelectStrategy, null, false);
                List<Envelope<BUILDER>> ebs = sqlRunner.doSelection(selectionInstruction, supplier, childrenDescriptors(), where);
                return mapEnvelopedBuilders(ebs);
            case SubSelectInClause:
                String primaryKeySelector = sqlBuilder.selectPrimaryKey(where);
                SelectionInstruction selectionInstructionSub = new SelectionInstruction(
                        sql, primaryKeySelector, childSelectStrategy, null, false);
                List<Envelope<BUILDER>> ebss = sqlRunner.doSelection(selectionInstructionSub, supplier, childrenDescriptors(), where);
                return mapEnvelopedBuilders(ebss);
            default:
                throw new HrormException("Unsupported child select strategy " + childSelectStrategy);
        }
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

    private List<ENTITY> mapEnvelopedBuilders(List<Envelope<BUILDER>> bs){
        return bs.stream()
                .map(envelope -> {
                    BUILDER builder = envelope.getItem();
                    return buildFunction.apply(builder);
                })
                .collect(Collectors.toList());
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
