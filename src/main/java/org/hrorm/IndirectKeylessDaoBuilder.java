package org.hrorm;

import java.sql.Connection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/***
 * An <code>IndirectKeylessDaoBuilder</code> provides mechanisms for defining the relationship between
 * a Java type and the table that backs it.
 *
 * <p>
 *    Using this builder (as opposed to the {@link DaoBuilder} or {@link IndirectDaoBuilder})
 *    will allow support for entities that do not have primary keys, but that support comes at
 *    a price. A <code>KeylessDao</code> supports fewer methods than a standard <code>Dao</code>
 *    and these entities cannot be joined to other objects or have children or be children to other objects.
 *    In general, the regular variants should be preferred.
 * </p>
 *
 * @param <ENTITY> The class that the <code>KeylessDao</code> will support.
 * @param <BUILDER> The class that builds the <code>ENTITY</code> type.
 */
public class IndirectKeylessDaoBuilder<ENTITY, BUILDER>
        extends AbstractKeylessDaoBuilder<ENTITY, BUILDER, IndirectKeylessDaoBuilder<ENTITY, BUILDER>>
        implements KeylessDaoDescriptor<ENTITY, BUILDER> {


    public IndirectKeylessDaoBuilder(String table, Supplier<BUILDER> supplier, Function<BUILDER, ENTITY> buildFunction) {
        super(table, supplier, buildFunction);
    }

    /**
     * Creates a {@link Dao} for performing CRUD operations of type <code>ENTITY</code>.
     *
     * @param connection The SQL connection this <code>Dao</code> will use
     *                   for its operations.
     * @return The newly created <code>Dao</code>.
     */
    public KeylessDao<ENTITY> buildDao(Connection connection){
        return new KeylessDaoImpl(connection, this);
    }

}
