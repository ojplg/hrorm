package org.hrorm;

import java.util.List;

public interface Dao<T> {

    /**
     * Insert a record into the database.
     *
     * @param item The instance to be inserted.
     * @return The newly issued primary key of the record.
     */
    long insert(T item);

    /**
     * Run an update statement to change the values in the database associated
     * with an existing record. Updates are applied by primary key.
     *
     * @param item An instance of the class with a populated primary key field
     *             and updated field values.
     */
    void update(T item);

    /**
     * Run a delete statement in the database. Deletion is done by primary key.
     *
     * @param item An instance of type T with a populated primary key.
     */
    void delete(T item);

    /**
     * Read a record from the database.
     *
     * @param id The primary key of the record desired.
     * @return The populated instance of type T.
     */
    T select(long id);

    /**
     * Read several records from the database.
     *
     * @param ids The primary keys of the records desired.
     * @return A list of populated instances of type T.
     */
    List<T> selectMany(List<Long> ids);

    /**
     * Read all the records in the database of type T.
     *
     * @return A list of populated instances of type T.
     */
    List<T> selectAll();

    /**
     * Select a single record from the database by some search criteria.
     *
     * If multiple records are found that match the passed item, an exception will be thrown.
     *
     * @param item An instance of type T with populated values corresponding to the
     *             column names to select by.
     * @param columnNames The names of the database columns
     * @return The populated instance of type T with matching values with the passed item for
     *         the indicated columnNames.
     */
    T selectByColumns(T item, String... columnNames);

    /**
     * Select multiple records from the database by some search criteria.
     *
     * @param item An instance of type T with populated values corresponding to the
     *             column names to select by.
     * @param columnNames The names of the database columns
     * @return The populated instances of type T with matching values with the passed item for
     *         the indicated columnNames.
     */
    List<T> selectManyByColumns(T item, String... columnNames);

}
