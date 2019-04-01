package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
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
public class DaoImpl<ENTITY, PARENT, BUILDER, PARENTBUILDER> extends KeylessDaoImpl<ENTITY, PARENT, BUILDER, PARENTBUILDER> implements Dao<ENTITY>, DaoDescriptor<ENTITY, BUILDER> {

    private final PrimaryKey<ENTITY, BUILDER> primaryKey;
    private final SqlBuilder<ENTITY> sqlBuilder;
    private final ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER> parentColumn;
    private final List<ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors;

    private final Connection connection;

    public DaoImpl(Connection connection,
                   DaoDescriptor<ENTITY, BUILDER> daoDescriptor){
        super(connection, daoDescriptor);
        this.childrenDescriptors = daoDescriptor.childrenDescriptors();
        if (daoDescriptor.primaryKey() == null) {
            throw new IllegalArgumentException("Must have a Primary Key");
        }
        this.connection = connection;
        this.primaryKey = daoDescriptor.primaryKey();
        this.sqlBuilder = new SqlBuilder<>(daoDescriptor);
        this.parentColumn = daoDescriptor.parentColumn();
    }

    @Override
    public boolean hasParent() { return parentColumn != null; }

    @Override
    public ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER> parentColumn() {
        return parentColumn;
    }

    @Override
    public List<ChildrenDescriptor<ENTITY, ?, BUILDER, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    @Override
    public Long insert(ENTITY item) {
        String sql = sqlBuilder.insert();
        long id = DaoHelper.getNextSequenceValue(connection, primaryKey.getSequenceName());
        primaryKey.optimisticSetKey(item, id);
        Envelope<ENTITY> envelope = newEnvelope(item, id);
        sqlRunner.insert(sql, envelope);
        for(ChildrenDescriptor<ENTITY,?, BUILDER,?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, new Envelope<>(item, id));
        }
        return id;
    }

    @Override
    public void update(ENTITY item) {
        String sql = sqlBuilder.update();
        Envelope<ENTITY> envelope = newEnvelope(item, primaryKey.getKey(item));
        sqlRunner.update(sql, envelope);
        for(ChildrenDescriptor<ENTITY,?, BUILDER,?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, new Envelope<>(item, primaryKey.getKey(item)));
        }
    }

    @Override
    public void delete(ENTITY item) {
        String sql = sqlBuilder.delete();
        DaoHelper.runPreparedDelete(connection, sql, primaryKey.getKey(item));
    }

    @Override
    public ENTITY select(long id) {
        String primaryKeyName = primaryKey.getName();
        ColumnSelection<ENTITY, BUILDER> columnSelection = select(primaryKeyName);
        String sql = sqlBuilder.selectByColumns(columnSelection);
        BUILDER builder = supplier().get();
        primaryKey.setKey(builder, id);
        ENTITY item = buildFunction.apply(builder);
        List<BUILDER> items = sqlRunner.selectByColumns(sql, supplier(),
                select(primaryKeyName),
                childrenDescriptors, item);
        return KeylessDaoImpl.fromSingletonList(mapBuilders(items));
    }

    @Override
    public List<ENTITY> selectMany(List<Long> ids) {
        String sql = sqlBuilder.select();
        List<String> idStrings = ids.stream().map(Object::toString).collect(Collectors.toList());
        String idsString = String.join(",", idStrings);
        sql = sql + " and a." + primaryKey.getName() + " in (" + idsString + ")";
        List<BUILDER> bs = sqlRunner.select(sql, supplier, childrenDescriptors);
        return mapBuilders(bs);
    }

    @Override
    public void atomicUpdate(ENTITY item) {
        Transactor transactor = new Transactor(connection);
        transactor.runAndCommit(
                con -> { update(item); }
        );
    }

    @Override
    public void atomicDelete(ENTITY item) {
        Transactor transactor = new Transactor(connection);
        transactor.runAndCommit(
                con -> { delete(item); }
        );
    }

    @Override
    public PrimaryKey<ENTITY, BUILDER> primaryKey() { return primaryKey; }

    @Override
    public Queries queries() {
        return this.sqlBuilder;
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
}
