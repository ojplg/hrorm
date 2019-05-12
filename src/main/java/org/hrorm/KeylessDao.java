package org.hrorm;

/**
 * A <code>KeylessDao</code> supports some operations for inserting
 * and selecting records in a database. Because a <code>KeylessDao</code>
 * has no knowledge of a primary key, it cannot perform all the operations
 * a full {@link Dao} can. In particular, it cannot update or delete records.
 * Moreover, a keyless entity cannot be used in most relations. In general,
 * a complete <code>Dao</code> should be preferred whenever possible.
 *
 * @param <ENTITY> The type of the data to be persisted.
 */
public interface KeylessDao<ENTITY> extends UnkeyedDao<ENTITY> {

    void insert(ENTITY item);

    /**
     * Insert a record into the database within a transaction that is
     * managed within the Dao. The Dao will either commit or rollback
     * the transaction and <b>close the underlying <code>Connection</code></b>
     * when complete.
     *
     * @param item The instance to be inserted.
     */
    void atomicInsert(ENTITY item);
}
