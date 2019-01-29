package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
public class DaoImpl<ENTITY, PARENT, BUILDER, PARENTBUILDER> extends KeylessDaoImpl<ENTITY, PARENT, BUILDER, PARENTBUILDER> implements Dao<ENTITY>, DaoDescriptor<ENTITY, BUILDER> {

    private static final Logger logger = Logger.getLogger("org.hrorm");
    protected final PrimaryKey<ENTITY, BUILDER> primaryKey;

    public DaoImpl(Connection connection,
                   DaoDescriptor<ENTITY, BUILDER> daoDescriptor){
        super(
                connection,
                daoDescriptor.tableName(),
                daoDescriptor.dataColumns(),
                daoDescriptor.supplier(),
                daoDescriptor.joinColumns(),
                daoDescriptor.childrenDescriptors(),
                daoDescriptor.parentColumn(),
                daoDescriptor.buildFunction(),
                new SqlBuilder<>(
                        daoDescriptor.tableName(),
                        KeylessDaoDescriptor.dataColumnsWithParent(
                                Collections.unmodifiableList(new ArrayList<>(daoDescriptor.dataColumns())),
                                daoDescriptor.parentColumn(),
                                daoDescriptor.parentColumn() != null
                        ),
                        Collections.unmodifiableList(new ArrayList<>(daoDescriptor.joinColumns())),
                        daoDescriptor.primaryKey())
        );
        this.primaryKey = daoDescriptor.primaryKey();
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
        String sql = sqlBuilder.selectByColumns(primaryKeyName);
        BUILDER builder = supplier().get();
        primaryKey.setKey(builder, id);
        ENTITY item = buildFunction.apply(builder);
        logger.info("Searching by " + id + " for " + item);
        List<BUILDER> items = sqlRunner.selectByColumns(sql, supplier,
                Collections.singletonList(primaryKeyName), columnMap(primaryKeyName),
                childrenDescriptors, item);
        return fromSingletonList(mapBuilders(items));
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

}
