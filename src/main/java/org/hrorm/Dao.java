package org.hrorm;

import java.util.List;

/**
 * A <code>Dao</code> is an interface that allows basic CRUD operations to be performed.
 * Using a <code>Dao</code>, you can insert, select, update, and delete records.
 *
 * @param <ENTITY> The type of the data to be persisted.
 */
public interface Dao<ENTITY> extends KeylessDao<ENTITY> {

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
     * Read a record from the database by its primary key.
     *
     * @param id The primary key of the record desired.
     * @return The populated instance of type ENTITY.
     */
    ENTITY select(long id);

    /**
     * Read several records from the database by their primary keys.
     *
     * @param ids The primary keys of the records desired.
     * @return A list of populated instances of type ENTITY.
     */
    List<ENTITY> selectMany(List<Long> ids);


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
