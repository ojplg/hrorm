package org.hrorm;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A <code>KeylessDaoBuilder</code> provides mechanisms for defining the relationship between
 * a Java type and the table(s) that will persist the data held in the class, where the
 * specified table does not have a unique Primary Key. This allows for operations on a table that
 * do not require a unique key.
 *
 * <p>
 *     Also see {@link DaoBuilder} for tables with Primary Keys, and {@link IndirectDaoBuilder}.
 * </p>
 *
 * @param <ENTITY> The class that the Dao will support.
 */
public class KeylessDaoBuilder<ENTITY> extends IndirectKeylessDaoBuilder<ENTITY, ENTITY> implements KeylessDaoDescriptor<ENTITY, ENTITY> {

    public KeylessDaoBuilder(String table, Supplier<ENTITY> supplier, Function<ENTITY, ENTITY> buildFunction) {
        super(table, supplier, buildFunction);
    }

    public KeylessDaoBuilder(IndirectDaoBuilder.BuilderHolder<ENTITY, ENTITY> builderHolder) {
        super(builderHolder);
    }
}
