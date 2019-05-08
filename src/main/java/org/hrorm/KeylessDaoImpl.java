package org.hrorm;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * The {@link KeylessDao} implementation.
 *
 * <p>
 *
 * There is no good reason to directly construct this class yourself.
 * Use a {@link IndirectKeylessDaoBuilder}.
 *
 * @param <ENTITY> The type whose persistence is managed by this <code>Dao</code>.
 * @param <BUILDER> The type of object that can build an <code>ENTITY</code> instance.
 */
public class KeylessDaoImpl<ENTITY, PARENT, BUILDER, PARENTBUILDER> extends AbstractKeylessDao<ENTITY, BUILDER> implements KeylessDao<ENTITY> {
    public KeylessDaoImpl(Connection connection,
                          KeylessDaoDescriptor<ENTITY, BUILDER> daoDescriptor){
        super(connection, daoDescriptor);
    }

    @Override
    public void atomicInsert(ENTITY item) {
        Transactor transactor = new Transactor(connection);
        Consumer<Connection> consumer = con -> insert(item);
        transactor.runAndCommit(consumer);
    }

    @Override
    public void insert(ENTITY item) {
        String sql = sqlBuilder.insert();
        Envelope<ENTITY, Object, ?> envelope = new Envelope(item);
        sqlRunner.insert(sql, envelope);
    }

    @Override
    protected List<ChildrenDescriptor<ENTITY, ?, BUILDER, ?, ?, ?>> childrenDescriptors() {
        return Collections.emptyList();
    }
}
