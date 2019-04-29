package org.hrorm;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A <code>KeylessDao</code> is an interface that allows basic, non-singular CRUD operations to be performed.
 * Using a <code>KeylessDao</code>, you can insert, records individually, but select, update, and delete all are limited
 * to multi-row operations, as there is no Primary Key defined.
 *
 * @param <ENTITY> The type of the data to be persisted.
 */
public interface GenericKeyDao<ENTITY,PK> extends UnkeyedDao<ENTITY> {

    /**
     * Insert a record into the database.
     *
     * <p>Depending on how the Dao was constucted (whether from a regular
     * <code>DaoBuilder</code> or an <code>IndirectDaoBuilder</code>)
     * a particular instance of this class may or may not attempt
     * to mutate the state of the passed item by setting its primary
     * key.</p>
     *
     * @param item The instance to be inserted.
     * @return The newly issued primary key of the record, if there is one. Else, Optional.empty()
     */
    PK insert(ENTITY item);

    /**
     * Insert a record into the database within a transaction that is
     * managed within the Dao. The Dao will either commit or rollback
     * the transaction and <b>close the underlying <code>Connection</code></b>
     * when complete.
     *
     * @param item The instance to be inserted.
     * @return The newly issued primary key of the record, if there is one. Else, Optional.empty()
     */
    PK atomicInsert(ENTITY item);

    ENTITY select(PK id);
}
