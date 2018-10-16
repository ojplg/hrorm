package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
 * @param <T> The child entity type
 * @param <P> The type of the parent
 */
public class NoBackReferenceParentColumn<T,P,TBUILDER,PBUILDER> implements ParentColumn<T,P,TBUILDER,PBUILDER> {

    private final String name;
    private final String prefix;
    private IndirectPrimaryKey<P,PBUILDER> parentPrimaryKey;
    private boolean nullable;

    private final Semaphore parentSemaphore = new Semaphore(1);
    private P parent;

    public NoBackReferenceParentColumn(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
        this.nullable = false;
    }

    @Override
    public void setParentPrimaryKey(IndirectPrimaryKey<P,PBUILDER> primaryKey) {
        this.parentPrimaryKey = primaryKey;
    }

    @Override
    public BiConsumer<TBUILDER, P> setter() {
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
    public PopulateResult populate(TBUILDER item, ResultSet resultSet) throws SQLException {
        return PopulateResult.ParentColumn;
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
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
    public IndirectTypedColumn<T,TBUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        return new NoBackReferenceParentColumn(name, newPrefix);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public void notNull() {
    }
}
