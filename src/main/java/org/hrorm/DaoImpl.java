package org.hrorm;

import java.sql.Connection;
import java.util.List;

/**
 * The {@link Dao} implementation.
 *
 * <p>
 *
 * There is no good reason to directly construct this class yourself.
 * Use a {@link DaoBuilder} or {@link IndirectDaoBuilder}.n
 *
 * @param <ENTITY> The type whose persistence is managed by this <code>Dao</code>.
 * @param <PARENT> The type of the parent (if any) of type <code>ENTITY</code>.
 * @param <BUILDER> The type of object that can build an <code>ENTITY</code> instance.
 * @param <PARENTBUILDER> The type of the object that can build a <code>PARENT</code> instance.
 */
public class DaoImpl<ENTITY, PARENT, BUILDER, PARENTBUILDER> extends AbstractDao<ENTITY, BUILDER, Long> implements Dao<ENTITY>, DaoDescriptor<Long,ENTITY, BUILDER> {

    private final PrimaryKey<Long,ENTITY, BUILDER> primaryKey;
    private final ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER, ?> parentColumn;
    private final List<ChildrenDescriptor<ENTITY,?, BUILDER,?,?>> childrenDescriptors;

    // TODO: This should not be necessary, split the sql runner into parts again maybe?
    private final SqlRunner<Long, ENTITY, BUILDER> keyedSqlRunner;

    public DaoImpl(Connection connection,
                   DaoDescriptor<Long,ENTITY, BUILDER> daoDescriptor){
        super(connection, daoDescriptor);
        this.childrenDescriptors = daoDescriptor.childrenDescriptors();
        if (daoDescriptor.primaryKey() == null) {
            throw new IllegalArgumentException("Must have a Primary Key");
        }
        this.primaryKey = daoDescriptor.primaryKey();
        this.parentColumn = daoDescriptor.parentColumn();
        this.keyedSqlRunner = new SqlRunner<>(connection, daoDescriptor);
    }

    @Override
    public boolean hasParent() { return parentColumn != null; }

    @Override
    public ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER, ?> parentColumn() {
        return parentColumn;
    }

    @Override
    public List<ChildrenDescriptor<ENTITY, ?, BUILDER, ?, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    @Override
    public Long insert(ENTITY item) {
        String sql = sqlBuilder.insert();
        KeyProducer<Long> keyProducer = primaryKey.getKeyProducer();
        Long id = keyProducer.produceKey(connection);
        primaryKey.optimisticSetKey(item, id);
        Envelope<ENTITY, Long> envelope = newEnvelope(item, id);
        keyedSqlRunner.insert(sql, envelope);
        for(ChildrenDescriptor<ENTITY,?, BUILDER,?, ?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, envelope);
        }
        return id;
    }

    @Override
    public void update(ENTITY item) {
        String sql = sqlBuilder.update();
        Envelope<ENTITY, Long> envelope = newEnvelope(item, primaryKey.getKey(item));
        keyedSqlRunner.update(sql, envelope);
        for(ChildrenDescriptor<ENTITY,?, BUILDER,?, ?> childrenDescriptor : childrenDescriptors){
            childrenDescriptor.saveChildren(connection, envelope);
        }
    }

    @Override
    public void delete(ENTITY item) {
        String sql = sqlBuilder.delete();
        keyedSqlRunner.runPreparedDelete(sql, primaryKey.getKey(item));
    }

    @Override
    public ENTITY select(Long id) {
        Where where = new Where(primaryKey.getName(), Operator.EQUALS, id);
        List<ENTITY> items = select(where);
        return KeylessDaoImpl.fromSingletonList(items);
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
    public PrimaryKey<Long,ENTITY, BUILDER> primaryKey() { return primaryKey; }

    @Override
    public Queries queries() {
        return this.sqlBuilder;
    }

    private Envelope<ENTITY, Long> newEnvelope(ENTITY item, long id){
        if( parentColumn != null ){
            Long parentId = (Long) parentColumn.getParentId(item);
            if ( parentId != null ){
                return new Envelope<>(item, id, parentId);
            }
        }
        return new Envelope<>(item, id);
    }
}
