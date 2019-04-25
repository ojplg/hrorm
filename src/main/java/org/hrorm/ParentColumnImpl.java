package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a reference from a child entity to its parent where
 * the child class has a pointer back to the parent.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <CHILD> The child entity type
 * @param <PARENT> The parent entity type
 * @param <CHILDBUILDER> The class used to construct new <code>CHILD</code> instances
 * @param <PARENTBUILDER> The class used to construct new <code>PARENT</code> instances
 */
public class ParentColumnImpl<CHILD, PARENT, CHILDBUILDER, PARENTBUILDER> implements ParentColumn<CHILD, PARENT, CHILDBUILDER, PARENTBUILDER> {

    private final String name;
    private final String prefix;
    private final BiConsumer<CHILDBUILDER, PARENT> setter;
    private final Function<CHILD, PARENT> getter;
    private PrimaryKey<Long,PARENT, PARENTBUILDER> parentPrimaryKey;
    private boolean nullable;
    private String sqlTypeName;

    public ParentColumnImpl(String name, String prefix, Function<CHILD, PARENT> getter, BiConsumer<CHILDBUILDER, PARENT> setter) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.nullable = false;
    }

    public ParentColumnImpl(String name, String prefix, Function<CHILD, PARENT> getter, BiConsumer<CHILDBUILDER, PARENT> setter,
                            PrimaryKey<Long,PARENT, PARENTBUILDER> parentPrimaryKey, boolean nullable) {
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
    public PopulateResult populate(CHILDBUILDER item, ResultSet resultSet) {
        return PopulateResult.ParentColumn;
    }

    @Override
    public void setValue(CHILD item, int index, PreparedStatement preparedStatement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetReader<Long> getReader(){
        return ResultSet::getLong;
    }

    @Override
    public Column<Long, Long, CHILD, CHILDBUILDER> withPrefix(String prefix, Prefixer prefixer) {
        return new ParentColumnImpl<>(name, prefix, getter, setter, parentPrimaryKey, nullable);
    }

    @Override
    public void notNull() {
        this.nullable = false;
    }

    public BiConsumer<CHILDBUILDER, PARENT> setter(){
        return setter;
    }

    public void setParentPrimaryKey(PrimaryKey<Long, PARENT, PARENTBUILDER> parentPrimaryKey) {
        this.parentPrimaryKey = parentPrimaryKey;
    }

    @Override
    public Set<Integer> supportedTypes() { return ColumnTypes.IntegerTypes; }

    @Override
    public Long getParentId(CHILD child) {
        PARENT parent = getter.apply(child);
        return parentPrimaryKey.getKey(parent);
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public String getSqlTypeName() { return sqlTypeName; }

    @Override
    public void setSqlTypeName(String sqlTypeName) { this.sqlTypeName = sqlTypeName; }

    @Override
    public Long toClassType(Long value){
        return value;
    }
}
