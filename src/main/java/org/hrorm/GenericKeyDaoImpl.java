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
public class GenericKeyDaoImpl<ENTITY, PARENT, BUILDER, PARENTBUILDER,PK>
        extends AbstractDao<ENTITY, BUILDER, PK>
        implements GenericKeyDao<ENTITY, PK>, DaoDescriptor<PK,ENTITY, BUILDER> {

    private final GenerativePrimaryKey<PK,ENTITY, BUILDER> primaryKey;
    private final ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER, ?> parentColumn;
    private final List<ChildrenDescriptor<ENTITY,?, BUILDER,?,?>> childrenDescriptors;

    // TODO: This should not be necessary, split the sql runner into parts again maybe?
    private final SqlRunner<PK, ENTITY, BUILDER> keyedSqlRunner;

    public GenericKeyDaoImpl(Connection connection,
                             DaoDescriptor<PK,ENTITY, BUILDER> daoDescriptor){
        super(connection, daoDescriptor);
        this.childrenDescriptors = daoDescriptor.childrenDescriptors();
        if (daoDescriptor.primaryKey() == null) {
            throw new IllegalArgumentException("Must have a Primary Key");
        }
        this.primaryKey = (GenerativePrimaryKey<PK,ENTITY, BUILDER>) daoDescriptor.primaryKey();
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
    public PK insert(ENTITY item) {
        String sql = sqlBuilder.insert();
        KeyProducer<PK> keyProducer = primaryKey.getKeyProducer();
        PK id = keyProducer.produceKey(connection);
        primaryKey.optimisticSetKey(item, id);
        Envelope<ENTITY, PK> envelope = newEnvelope(item, id);
        keyedSqlRunner.insert(sql, envelope);
//        for(ChildrenDescriptor<ENTITY,?, BUILDER,?, ?> childrenDescriptor : childrenDescriptors){
//            childrenDescriptor.saveChildren(connection, envelope);
//        }
        return id;
    }

    @Override
    public void update(ENTITY item) {
        String sql = sqlBuilder.update();
        Envelope<ENTITY, PK> envelope = newEnvelope(item, primaryKey.getKey(item));
        keyedSqlRunner.update(sql, envelope);
//        for(ChildrenDescriptor<ENTITY,?, BUILDER,?, ?> childrenDescriptor : childrenDescriptors){
//            childrenDescriptor.saveChildren(connection, envelope);
//        }
    }

    @Override
    public void delete(ENTITY item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ENTITY select(PK id) {
        Where where = new Where(primaryKey.getName(), Operator.EQUALS, id, primaryKey.getGenericColumn());
        List<ENTITY> items = select(where);
        return KeylessDaoImpl.fromSingletonList(items);
    }

    @Override
    public List<ENTITY> selectMany(List<PK> ids) {
        throw new UnsupportedOperationException();
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
    public PrimaryKey<PK,ENTITY, BUILDER> primaryKey() { return primaryKey; }

    @Override
    public Queries queries() {
        return this.sqlBuilder;
    }

    private Envelope<ENTITY, PK> newEnvelope(ENTITY item, PK id){
//        if( parentColumn != null ){
//            PK parentId = (PK) parentColumn.getParentId(item);
//            if ( parentId != null ){
//                return new Envelope<>(item, id, parentId);
//            }
//        }
        return new Envelope<>(item, id);
    }
}
