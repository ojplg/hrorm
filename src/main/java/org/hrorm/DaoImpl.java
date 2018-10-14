package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The {@link Dao} implementation.
 *
 * <p>
 *
 * There is no good reason to directly construct this class yourself.
 * Use a {@link DaoBuilder}.
 *
 * @param <T> The item whose persistence is managed by this <code>Dao</code>
 */
public class DaoImpl<T,P> implements Dao<T>, DaoDescriptor<T> {

    private final Connection connection;
    private final String tableName;
    private final List<TypedColumn<T>> dataColumns;
    private final PrimaryKey<T> primaryKey;
    private final Supplier<T> supplier;
    private final List<JoinColumn<T,?>> joinColumns;
    private final List<ChildrenDescriptor<T,?>> childrenDescriptors;
    private final SqlBuilder<T> sqlBuilder;
    private final SqlRunner<T> sqlRunner;
    private final ParentColumn<T,P> parentColumn;

    public DaoImpl(Connection connection,
                   String tableName,
                   Supplier<T> supplier,
                   PrimaryKey<T> primaryKey,
                   List<TypedColumn<T>> dataColumns,
                   List<JoinColumn<T,?>> joinColumns,
                   List<ChildrenDescriptor<T,?>> childrenDescriptors,
                   ParentColumn<T,P> parentColumn){
        this.connection = connection;
        this.tableName = tableName;
        this.dataColumns = Collections.unmodifiableList(new ArrayList<>(dataColumns));
        this.primaryKey = primaryKey;
        this.supplier = supplier;
        this.joinColumns = Collections.unmodifiableList(new ArrayList<>(joinColumns));
        this.childrenDescriptors = Collections.unmodifiableList(new ArrayList<>(childrenDescriptors));
        this.sqlBuilder = new SqlBuilder<>(tableName, this.dataColumnsWithParent(), this.joinColumns, primaryKey);
        this.sqlRunner = new SqlRunner<>(connection, this);
        this.parentColumn = parentColumn;
    }

    @Override
    public String tableName(){
        return tableName;
    }

    @Override
    public List<TypedColumn<T>> dataColumns(){
        return dataColumns;
    }

    @Override
    public List<JoinColumn<T, ?>> joinColumns(){
        return joinColumns;
    }

    @Override
    public Supplier<T> supplier() { return supplier; }

    @Override
    public PrimaryKey<T> primaryKey() { return primaryKey; }

    @Override
    public List<ChildrenDescriptor<T, ?>> childrenDescriptors() {
        return null;
    }

    @Override
    public ParentColumn<T, P> parentColumn() {
        return parentColumn;
    }

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
        primaryKey.setKey(item, id);
        sqlRunner.insert(sql, item);
        for(ChildrenDescriptor<T,?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, item);
        }
        return id;
    }

    @Override
    public void update(T item) {
        String sql = sqlBuilder.update();
        sqlRunner.update(sql, item);
        for(ChildrenDescriptor<T,?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, item);
        }
    }

    @Override
    public void delete(T item) {
        String sql = sqlBuilder.delete();
        DaoHelper.runPreparedDelete(connection, sql, primaryKey.getKey(item));
    }

    @Override
    public T select(long id) {
        String sql = sqlBuilder.select();
        sql = sql + " and a." + primaryKey.getName() + " = " + id;
        List<T> items = sqlRunner.select(sql, supplier, childrenDescriptors);
        return fromSingletonList(items);
    }

    @Override
    public List<T> selectMany(List<Long> ids) {
        String sql = sqlBuilder.select();
        List<String> idStrings = ids.stream().map(Object::toString).collect(Collectors.toList());
        String idsString = String.join(",", idStrings);
        sql = sql + " and a." + primaryKey.getName() + " in (" + idsString + ")";
        return sqlRunner.select(sql, supplier, childrenDescriptors);
    }

    @Override
    public List<T> selectAll() {
        String sql = sqlBuilder.select();
        return sqlRunner.select(sql, supplier, childrenDescriptors);
    }

    @Override
    public T selectByColumns(T item, String ... columnNames){
        List<T> items = selectManyByColumns(item, columnNames);
        return fromSingletonList(items);
    }

    @Override
    public List<T> selectManyByColumns(T item, String ... columnNames) {
        String sql = sqlBuilder.selectByColumns(columnNames);
        return sqlRunner.selectByColumns(sql, supplier, Arrays.asList(columnNames), columnMap(columnNames), childrenDescriptors, item);
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
