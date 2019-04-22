package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Represents a reference from a child entity to its parent where the
 * child class has no reference to the parent.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type whose persistence is managed by this <code>Dao</code>.
 * @param <PARENT> The type of the parent (if any) of type <code>ENTITY</code>.
 * @param <BUILDER> The type of object that can build an <code>ENTITY</code> instance.
 * @param <PARENTBUILDER> The type of the object that can build a <code>PARENT</code> instance.
 */
public class NoBackReferenceParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER> implements ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER> {

    private final String name;
    private final String prefix;

    private String sqlTypeName = "integer";

    public NoBackReferenceParentColumn(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    @Override
    public void setParentPrimaryKey(PrimaryKey<PARENT, PARENTBUILDER> primaryKey) {
    }

    @Override
    public BiConsumer<BUILDER, PARENT> setter() {
        return (b,s) -> {};
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
    public PopulateResult populate(BUILDER item, ResultSet resultSet) {
        return PopulateResult.ParentColumn;
    }

    @Override
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetReader<Long> getReader(){
        return ResultSet::getLong;
    }

    @Override
    public Column<Long, Long, ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        return new NoBackReferenceParentColumn(name, newPrefix);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public void notNull() {
        throw new UnsupportedOperationException("Parent column cannot be null.");
    }

    @Override
    public Set<Integer> supportedTypes() { return ColumnTypes.IntegerTypes; }

    @Override
    public Long getParentId(ENTITY entity) {
        return null;
    }

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
}
