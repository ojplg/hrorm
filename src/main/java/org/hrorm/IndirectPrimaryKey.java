package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Primary key for an entity whose construction is indirect, i.e. the
 * entity is immutable and without setters.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type of the class being persisted.
 * @param <BUILDER> The type of the class that can construct new <code>ENTITY</code> instances.
 */
public class IndirectPrimaryKey<ENTITY, BUILDER> implements PrimaryKey<ENTITY, BUILDER> {

    private final String prefix;
    private final String name;
    private final String sequenceName;
    private final BiConsumer<BUILDER, Long> setter;
    private final Function<ENTITY, Long> getter;
    private String sqlTypeName = "integer";

    public IndirectPrimaryKey(String prefix,
                              String name,
                              String sequenceName,
                              Function<ENTITY, Long> getter,
                              BiConsumer<BUILDER, Long> setter) {
        this.prefix = prefix;
        this.name = name;
        this.sequenceName = sequenceName;
        this.setter = setter;
        this.getter = getter;
    }

    @Override
    public Long getKey(ENTITY item) {
        if ( item == null ){
            throw new HrormException("Cannot get a key from a null item ");
        }
        return getter.apply(item);
    }

    @Override
    public String getSequenceName() {
        return sequenceName;
    }

    @Override
    public void optimisticSetKey(ENTITY item, Long id) {
        // nothing to do in this case
        // in the indirect case the primary key has no
        // access to the entity itself
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
    public PopulateResult populate(BUILDER constructor, ResultSet resultSet) throws SQLException {
        long value = resultSet.getLong(prefix  + name);
        setter.accept(constructor, value);
        if ( value == 0 ){
            return PopulateResult.NoPrimaryKey;
        }
        return PopulateResult.PrimaryKey;

    }

    @Override
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException {
        Long value = getter.apply(item);
        if ( value == null ){
            throw new HrormException("Tried to set a null value for the primary key named " + name);
        } else {
            preparedStatement.setLong(index, value);
        }
    }

    @Override
    public Column<Long, Long, ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        return new IndirectPrimaryKey<>(newPrefix, name, sequenceName, getter, setter);
    }

    @Override
    public boolean isPrimaryKey() {
        return true;
    }

    @Override
    public void notNull() {
        throw new HrormException("Cannot set a primary key to be nullable");
    }

    @Override
    public Set<Integer> supportedTypes() { return ColumnTypes.IntegerTypes; }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public String getSqlTypeName() { return sqlTypeName; }

    @Override
    public void setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
    }

    @Override
    public Long toClassType(Long dbType) {
        return dbType;
    }

    @Override
    public GenericColumn<Long> asGenericColumn() {
        return new GenericColumn<>(PreparedStatement::setLong, ResultSet::getLong, Types.INTEGER, sqlTypeName, ColumnTypes.IntegerTypes);
    }

}
