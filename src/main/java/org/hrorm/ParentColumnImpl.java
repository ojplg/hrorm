package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Represents a reference from a child entity to its parent where
 * the child class has a pointer back to the parent.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The child entity type
 * @param <P> The type of the parent
 */
public class ParentColumnImpl<T, P,TBUILDER,PBUILDER> implements ParentColumn<T,P,TBUILDER,PBUILDER> {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    private final String name;
    private final String prefix;
    private final BiConsumer<TBUILDER, P> setter;
    private final Function<T, P> getter;
    private PrimaryKey<P, PBUILDER> parentPrimaryKey;
    private boolean nullable;

    public ParentColumnImpl(String name, String prefix, Function<T, P> getter, BiConsumer<TBUILDER, P> setter) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.nullable = false;
    }

    public ParentColumnImpl(String name, String prefix, Function<T, P> getter, BiConsumer<TBUILDER, P> setter,
                            PrimaryKey<P, PBUILDER> parentPrimaryKey, boolean nullable) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.nullable = nullable;
        this.parentPrimaryKey = parentPrimaryKey;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public PopulateResult populate(TBUILDER item, ResultSet resultSet) throws SQLException {
        return PopulateResult.ParentColumn;
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        P parent = getter.apply(item);
        Long parentId = parentPrimaryKey.getKey(parent);
        if ( parentId == null ){
            if ( nullable ){
                preparedStatement.setNull(index, Types.INTEGER);
            } else {
                throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
            }
        } else {
            preparedStatement.setLong(index, parentId);
        }
    }

    @Override
    public Column<T,TBUILDER> withPrefix(String prefix, Prefixer prefixer) {
        return new ParentColumnImpl<>(name, prefix, getter, setter, parentPrimaryKey, nullable);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public void notNull() {
        this.nullable = false;
    }

    public BiConsumer<TBUILDER, P> setter(){
        return setter;
    }

    public void setParentPrimaryKey(PrimaryKey<P,PBUILDER> parentPrimaryKey) {
        this.parentPrimaryKey = parentPrimaryKey;
    }
}
