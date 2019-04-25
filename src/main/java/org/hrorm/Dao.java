package org.hrorm;

import java.util.List;

/**
 * A <code>Dao</code> is an interface that allows basic CRUD operations to be performed.
 * Using a <code>Dao</code>, you can insert, select, update, and delete records.
 *
 * @param <ENTITY> The type of the data to be persisted.
 */
public interface Dao<ENTITY> extends GenericPrimaryKeyDao<Long,ENTITY> {}
