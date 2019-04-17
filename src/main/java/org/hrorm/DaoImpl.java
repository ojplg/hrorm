package org.hrorm;

import java.sql.Connection;
import java.util.List;

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
public class DaoImpl<ENTITY, PARENT, BUILDER, PARENTBUILDER> extends AbstractDao<ENTITY, BUILDER> implements Dao<ENTITY>, DaoDescriptor<ENTITY, BUILDER> {

    private final PrimaryKey<ENTITY, BUILDER> primaryKey;
    private final ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER> parentColumn;
    private final List<ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors;

    public DaoImpl(Connection connection,
                   DaoDescriptor<ENTITY, BUILDER> daoDescriptor){
        super(connection, daoDescriptor);
        this.childrenDescriptors = daoDescriptor.childrenDescriptors();
        if (daoDescriptor.primaryKey() == null) {
            throw new IllegalArgumentException("Must have a Primary Key");
        }
        this.primaryKey = daoDescriptor.primaryKey();
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
            childrenDescriptor.saveChildren(connection, envelope);
        }
        return id;
    }

    @Override
    public void update(ENTITY item) {
        String sql = sqlBuilder.update();
        Envelope<ENTITY> envelope = newEnvelope(item, primaryKey.getKey(item));
        sqlRunner.update(sql, envelope);
        for(ChildrenDescriptor<ENTITY,?, BUILDER,?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, envelope);
        }
    }

    @Override
    public void delete(ENTITY item) {
        String sql = sqlBuilder.delete();
        DaoHelper.runPreparedDelete(connection, sql, primaryKey.getKey(item));
    }

    @Override
    public ENTITY select(long id) {
        Where where = new Where(primaryKey.getName(), Operator.EQUALS, id);
        String sql = sqlBuilder.select(where);
        List<BUILDER> items = sqlRunner.selectWhere(sql, supplier(), childrenDescriptors(), where);
        return KeylessDaoImpl.fromSingletonList(mapBuilders(items));
    }

    @Override
    public List<ENTITY> selectMany(List<Long> ids) {
        Where where = Where.inLong(primaryKey.getName(), ids);
        return select(where);
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

    private Envelope<ENTITY> newEnvelope(ENTITY item, long id){
        if( parentColumn != null ){
            Long parentId = parentColumn.getParentId(item);
            if ( parentId != null ){
                return new Envelope<>(item, id, parentId);
            }
        }
        return new Envelope<>(item, id);
    }
}
