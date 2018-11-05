package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;
import java.util.concurrent.Semaphore;
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
    private PrimaryKey<PARENT, PARENTBUILDER> parentPrimaryKey;
    private boolean nullable;

    private final Semaphore parentSemaphore = new Semaphore(1);
    private PARENT parent;

    public NoBackReferenceParentColumn(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
        this.nullable = false;
    }

    @Override
    public void setParentPrimaryKey(PrimaryKey<PARENT, PARENTBUILDER> primaryKey) {
        this.parentPrimaryKey = primaryKey;
    }

    @Override
    public BiConsumer<BUILDER, PARENT> setter() {
        return (t,p) -> {
            try {
                this.parentSemaphore.acquire();
                this.parent = p;
            } catch (InterruptedException ex){
                throw new HrormException("Semaphore interrupted");
            }
        };
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
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException {
        Long parentId = getParentId();
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

    private Long getParentId(){
        Long parentId = parentPrimaryKey.getKey(parent);
        this.parentSemaphore.release();
        return parentId;
    }

    @Override
    public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        return new NoBackReferenceParentColumn(name, newPrefix);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public void notNull() {
    }

    @Override
    public Set<Integer> supportedTypes() { return ColumnTypes.IntegerTypes; }

    @Override
    public Long getParentId(ENTITY entity) {
        return null;
    }
}
