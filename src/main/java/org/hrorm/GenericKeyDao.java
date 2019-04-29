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

    /**
     * Read a record from the database by its primary key.
     *
     * @param id The primary key of the record desired.
     * @return The populated instance of type ENTITY.
     */
    ENTITY select(PK id);

    /**
     * Read several records from the database by their primary keys.
     *
     * @param ids The primary keys of the records desired.
     * @return A list of populated instances of type ENTITY.
     */
    List<ENTITY> selectMany(List<PK> ids);

    /**
     * Run an update statement to change the values in the database associated
     * with an existing record. Updates are applied by primary key.
     *
     * @param item An instance of the class with a populated primary key field
     *             and updated field values.
     */
    void update(ENTITY item);

    /**
     * Run a delete statement in the database. Deletion is done by primary key.
     *
     * @param item An instance of type ENTITY with a populated primary key.
     */
    void delete(ENTITY item);

    /**
     * Run an update statement to change the values in the database associated
     * with an existing record. Updates are applied by primary key.
     * The update will occur within a transaction that is
     * managed within the Dao. The Dao will either commit or rollback
     * the transaction and <b>close the underlying <code>Connection</code></b>
     * when complete.
     *
     * @param item An instance of the class with a populated primary key field
     *             and updated field values.
     */
    void atomicUpdate(ENTITY item);

    /**
     * Run a delete statement in the database within a transaction.
     * Deletion is done by primary key.
     * The delete will occur within a transaction that is
     * managed within the Dao. The Dao will either commit or rollback
     * the transaction and <b>close the underlying <code>Connection</code></b>
     * when complete.
     *
     * @param item An instance of type ENTITY with a populated primary key.
     */
    void atomicDelete(ENTITY item);

    /**
     * Access the <code>SQL</code> this <code>Dao</code> is using.
     *
     * <p>The <code>SQL</code> provided is suitable for being passed to a
     * <code>PreparedStatement</code>.</p>
     *
     * @return An object containing the literal <code>SQL</code>.
     */
    Queries queries();

}
