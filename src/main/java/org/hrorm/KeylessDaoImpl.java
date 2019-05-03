package org.hrorm;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

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
public class KeylessDaoImpl<ENTITY, BUILDER> extends AbstractDao<ENTITY, BUILDER> {

    public KeylessDaoImpl(Connection connection,
                          KeylessDaoDescriptor<ENTITY, BUILDER> daoDescriptor){
        super(connection, daoDescriptor);
    }

    @Override
    public Long insert(ENTITY item) {
        String sql = sqlBuilder.insert();
        Envelope<ENTITY> envelope = new Envelope(item);
        sqlRunner.insert(sql, envelope);
        return null;
    }

    @Override
    protected List<ChildrenDescriptor<ENTITY, ?, BUILDER, ?>> childrenDescriptors() {
        return Collections.emptyList();
    }
}
