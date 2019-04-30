package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An abstract class that is almost a <code>KeylessDao</code>.
 *
 * <p>
 *     Most users of hrorm will have no need to directly use this.
 * </p>
 *
 * @param <ENTITY> The type whose persistence is managed by this <code>Dao</code>.
 * @param <BUILDER> The type of object that can build an <code>ENTITY</code> instance.
 */
public abstract class AbstractDao<ENTITY, BUILDER, PK> extends AbstractKeylessDao<ENTITY, BUILDER> implements GenericKeyDao<ENTITY, PK> {

    public AbstractDao(Connection connection,
                       KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor){
        super(connection, keylessDaoDescriptor);
    }

    public AbstractDao(Connection connection,
                       DaoDescriptor<PK, ENTITY, BUILDER> daoDescriptor){
        super(connection, daoDescriptor);
    }

    protected abstract List<ChildrenDescriptor<ENTITY,?, BUILDER,?, ?,?>> childrenDescriptors();

    public abstract PK insert(ENTITY item);

    @Override
    public PK atomicInsert(ENTITY item) {
        Transactor transactor = new Transactor(connection);
        return transactor.runAndCommit(
                con -> { return insert(item); }
        );
    }
}
