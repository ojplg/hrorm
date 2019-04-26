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
public class NoBackReferenceParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER, PARENTPK> implements ParentColumn<ENTITY, PARENT, BUILDER, PARENTBUILDER, PARENTPK> {

    private final String name;
    private final String prefix;

    private String sqlTypeName = "integer";

    private PrimaryKey<PARENTPK, PARENT, PARENTBUILDER> parentPrimaryKey;

    public NoBackReferenceParentColumn(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    @Override
    public void setParentPrimaryKey(PrimaryKey<PARENTPK,PARENT, PARENTBUILDER> primaryKey) {
        this.parentPrimaryKey = primaryKey;
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
    public ResultSetReader<PARENTPK> getReader(){
        return parentPrimaryKey.getReader();
    }

    @Override
    public PreparedStatementSetter<PARENTPK> getStatementSetter() {
        return parentPrimaryKey.getStatementSetter();
    }


    @Override
    public Column<PARENTPK, PARENTPK, ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
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
    public PARENTPK getParentId(ENTITY entity) {
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
    public PARENTPK toClassType(PARENTPK dbType) {
        return dbType;
    }
}
