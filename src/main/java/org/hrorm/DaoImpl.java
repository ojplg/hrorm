package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The {@link Dao} implementation.
 *
 * <p>
 *
 * There is no good reason to directly construct this class yourself.
 * Use a {@link DaoBuilder}.
 *
 * @param <T> The type whose persistence is managed by this <code>Dao</code>
 * @param <P> The type of the parent (if any) of type <code>T</code>.
 * @param <B> The type of the builder of type <code>B</code>
 */
public class DaoImpl<T,P,B> implements Dao<T>, DaoDescriptor<T,B> {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    private final Connection connection;
    private final String tableName;
    private final List<IndirectTypedColumn<T,B>> dataColumns;
    private final IndirectPrimaryKey<T,B> primaryKey;
    private final Supplier<B> supplier;
    private final List<JoinColumn<T,?,B,?>> joinColumns;
    private final List<ChildrenDescriptor<T,?,B,?>> childrenDescriptors;
    private final SqlBuilder<T> sqlBuilder;
    private final SqlRunner<T,B> sqlRunner;
    private final ParentColumn<T,P,B,?> parentColumn;
    private final Function<B, T> buildFunction;

    public DaoImpl(Connection connection,
                   String tableName,
                   Supplier<B> supplier,
                   IndirectPrimaryKey<T,B> primaryKey,
                   List<IndirectTypedColumn<T,B>> dataColumns,
                   List<JoinColumn<T,?,B,?>> joinColumns,
                   List<ChildrenDescriptor<T,?,B,?>> childrenDescriptors,
                   ParentColumn<T,P,B,?> parentColumn,
                   Function<B,T> buildFunction){
        this.connection = connection;
        this.tableName = tableName;
        this.dataColumns = Collections.unmodifiableList(new ArrayList<>(dataColumns));
        this.primaryKey = primaryKey;
        this.supplier = supplier;
        this.joinColumns = Collections.unmodifiableList(new ArrayList<>(joinColumns));
        this.childrenDescriptors = Collections.unmodifiableList(new ArrayList<>(childrenDescriptors));
        this.sqlBuilder = new SqlBuilder<T>(tableName, this.dataColumnsWithParent(), this.joinColumns, primaryKey);
        this.sqlRunner = new SqlRunner<>(connection, this);
        this.parentColumn = parentColumn;
        this.buildFunction = buildFunction;
    }

    @Override
    public String tableName(){
        return tableName;
    }

    @Override
    public List<IndirectTypedColumn<T,B>> dataColumns(){
        return dataColumns;
    }

    @Override
    public List<JoinColumn<T, ?, B, ?>> joinColumns(){
        return joinColumns;
    }

    @Override
    public Supplier<B> supplier() { return supplier; }

    @Override
    public IndirectPrimaryKey<T,B> primaryKey() { return primaryKey; }

    @Override
    public List<ChildrenDescriptor<T, ?, B, ?>> childrenDescriptors() {
        return null;
    }

    @Override
    public ParentColumn<T, P, B, ?> parentColumn() {
        return parentColumn;
    }

    @Override
    public Function<B,T> buildFunction() { return buildFunction; }

    @Override
    public long atomicInsert(T item) {
        Transactor transactor = new Transactor(connection);
        return transactor.runAndCommit(
               con -> { return insert(item); }
        );
    }

    @Override
    public void atomicUpdate(T item) {
        Transactor transactor = new Transactor(connection);
        transactor.runAndCommit(
                con -> { update(item); }
        );
    }

    @Override
    public void atomicDelete(T item) {
        Transactor transactor = new Transactor(connection);
        transactor.runAndCommit(
                con -> { delete(item); }
        );
    }

    @Override
    public long insert(T item) {
        String sql = sqlBuilder.insert();
        long id = DaoHelper.getNextSequenceValue(connection, primaryKey.getSequenceName());
        primaryKey.optimisticSetKey(item, id);
        sqlRunner.insert(sql, item, id, -1);
        for(ChildrenDescriptor<T,?,B,?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, item);
        }
        return id;
    }

    @Override
    public void update(T item) {
        String sql = sqlBuilder.update();
        sqlRunner.update(sql, item, -1);
        for(ChildrenDescriptor<T,?,B,?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, item);
        }
    }

    @Override
    public void delete(T item) {
        String sql = sqlBuilder.delete();
        DaoHelper.runPreparedDelete(connection, sql, primaryKey.getKey(item));
    }

    private List<T> mapBuilders(List<B> bs){
        return bs.stream().map(b -> buildFunction.apply(b)).collect(Collectors.toList());
    }

    @Override
    public T select(long id) {
        String primaryKeyName = primaryKey.getName();
        String sql = sqlBuilder.selectByColumns(primaryKeyName);
        B builder = supplier().get();
        primaryKey.setKey(builder, id);
        T item = buildFunction.apply(builder);
        logger.info("Searching by " + id + " for " + item);
        List<B> items = sqlRunner.selectByColumns(sql, supplier,
                Collections.singletonList(primaryKeyName), columnMap(primaryKeyName),
                childrenDescriptors, item);
        return fromSingletonList(mapBuilders(items));
    }

    @Override
    public List<T> selectMany(List<Long> ids) {
        String sql = sqlBuilder.select();
        List<String> idStrings = ids.stream().map(Object::toString).collect(Collectors.toList());
        String idsString = String.join(",", idStrings);
        sql = sql + " and a." + primaryKey.getName() + " in (" + idsString + ")";
        List<B> bs = sqlRunner.select(sql, supplier, childrenDescriptors);
        return mapBuilders(bs);
    }

    @Override
    public List<T> selectAll() {
        String sql = sqlBuilder.select();
        List<B> bs = sqlRunner.select(sql, supplier, childrenDescriptors);
        return mapBuilders(bs);
    }

    @Override
    public T selectByColumns(T item, String ... columnNames){
        List<T> items = selectManyByColumns(item, columnNames);
        return fromSingletonList(items);
    }

    @Override
    public List<T> selectManyByColumns(T item, String ... columnNames) {
        String sql = sqlBuilder.selectByColumns(columnNames);
        List<B> bs = sqlRunner.selectByColumns(sql, supplier, Arrays.asList(columnNames), columnMap(columnNames), childrenDescriptors, item);
        return mapBuilders(bs);
    }

    private <A> A fromSingletonList(List<A> items) {
        if (items.isEmpty()) {
            return null;
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        throw new HrormException("Found " + items.size() + " items.");
    }
}
