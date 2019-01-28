package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
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
 * Use a {@link DaoBuilder} or {@link IndirectDaoBuilder}.
 *
 * @param <ENTITY> The type whose persistence is managed by this <code>Dao</code>.
 * @param <PARENT> The type of the parent (if any) of type <code>ENTITY</code>.
 * @param <BUILDER> The type of object that can build an <code>ENTITY</code> instance.
 * @param <PARENTBUILDER> The type of the object that can build a <code>PARENT</code> instance.
 */
public class KeylessDaoImpl<ENTITY, PARENT, BUILDER, PARENTBUILDER> implements KeylessDao<ENTITY>, DaoDescriptor<ENTITY, BUILDER> {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    protected final Connection connection;
    protected final String tableName;
    private final List<Column<ENTITY, BUILDER>> dataColumns;
    protected final PrimaryKey<ENTITY, BUILDER> primaryKey;
    protected final Supplier<BUILDER> supplier;
    private final List<JoinColumn<ENTITY,?, BUILDER,?>> joinColumns;
    protected final List<ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors;
    protected final SqlBuilder<ENTITY> sqlBuilder;
    protected final SqlRunner<ENTITY, BUILDER> sqlRunner;
    protected  final ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER> parentColumn;
    protected final Function<BUILDER, ENTITY> buildFunction;

    public KeylessDaoImpl(Connection connection,
                          DaoDescriptor<ENTITY, BUILDER> daoDescriptor){
        this.connection = connection;
        this.tableName = daoDescriptor.tableName();
        this.dataColumns = Collections.unmodifiableList(new ArrayList<>(daoDescriptor.dataColumns()));
        this.primaryKey = daoDescriptor.primaryKey().orElse(null);
        this.supplier = daoDescriptor.supplier();
        this.joinColumns = Collections.unmodifiableList(new ArrayList<>(daoDescriptor.joinColumns()));
        this.childrenDescriptors = Collections.unmodifiableList(new ArrayList<>(daoDescriptor.childrenDescriptors()));
        this.parentColumn = daoDescriptor.parentColumn();
        this.buildFunction = daoDescriptor.buildFunction();

        this.sqlBuilder = new SqlBuilder<>(tableName, this.dataColumnsWithParent(), this.joinColumns, this.primaryKey);
        this.sqlRunner = new SqlRunner<>(connection, this);
    }

    @Override
    public String tableName(){
        return tableName;
    }

    @Override
    public List<Column<ENTITY, BUILDER>> dataColumns(){
        return dataColumns;
    }

    @Override
    public List<JoinColumn<ENTITY, ?, BUILDER, ?>> joinColumns(){
        return joinColumns;
    }

    @Override
    public Supplier<BUILDER> supplier() { return supplier; }

    @Override
    public Optional<PrimaryKey<ENTITY, BUILDER>> primaryKey() { return Optional.ofNullable(primaryKey); }

    @Override
    public List<ChildrenDescriptor<ENTITY, ?, BUILDER, ?>> childrenDescriptors() {
        return null;
    }

    @Override
    public ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER> parentColumn() {
        return parentColumn;
    }

    @Override
    public Function<BUILDER, ENTITY> buildFunction() { return buildFunction; }

    @Override
    public Optional<Long> atomicInsert(ENTITY item) {
        Transactor transactor = new Transactor(connection);
        return transactor.runAndCommit(
               con -> { return insert(item); }
        );
    }


    @Override
    public Optional<Long> insert(ENTITY item) {
        String sql = sqlBuilder.insert();
        long id = DaoHelper.getNextSequenceValue(connection, primaryKey.getSequenceName());
        primaryKey.optimisticSetKey(item, id);
        Envelope<ENTITY> envelope = newEnvelope(item, id);
        sqlRunner.insert(sql, envelope);
        for(ChildrenDescriptor<ENTITY,?, BUILDER,?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, new Envelope<>(item, id));
        }
        return Optional.of(id);
    }

    protected Envelope<ENTITY> newEnvelope(ENTITY item, long id){
        if( parentColumn != null ){
            Long parentId = parentColumn.getParentId(item);
            if ( parentId != null ){
                return new Envelope<>(item, id, parentId);
            }
        }
        return new Envelope<>(item, id);
    }


    protected List<ENTITY> mapBuilders(List<BUILDER> bs){
        return bs.stream().map(buildFunction).collect(Collectors.toList());
    }


    @Override
    public List<ENTITY> selectAll() {
        String sql = sqlBuilder.select();
        List<BUILDER> bs = sqlRunner.select(sql, supplier, childrenDescriptors);
        return mapBuilders(bs);
    }

    @Override
    public List<ENTITY> selectManyByColumns(ENTITY item, String ... columnNames) {
        String sql = sqlBuilder.selectByColumns(columnNames);
        List<BUILDER> bs = sqlRunner.selectByColumns(sql, supplier, Arrays.asList(columnNames), columnMap(columnNames), childrenDescriptors, item);
        return mapBuilders(bs);
    }

    // TODO
    @Override
    public List<ENTITY> deleteManyByColumns(ENTITY item, String... columnNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ENTITY> updateManyByColumns(ENTITY selectionItem, String[] selectionColumnNames, ENTITY updateItem, String[] updateColumnNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T foldingSelect(ENTITY item, T identity, BiFunction<T,ENTITY,T> accumulator, String ... columnNames){
        String sql = sqlBuilder.selectByColumns(columnNames);
        return sqlRunner.foldingSelect(sql, supplier, Arrays.asList(columnNames), columnMap(columnNames), childrenDescriptors, item, buildFunction, identity, accumulator);
    }

    @Override
    public Queries queries() {
        return this.sqlBuilder;
    }

    protected <A> A fromSingletonList(List<A> items) {
        if (items.isEmpty()) {
            return null;
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        throw new HrormException("Found " + items.size() + " items.");
    }
}
