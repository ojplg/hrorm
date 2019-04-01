package org.hrorm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An implementation of a <code>DaoDescriptor</code> that has its
 * prefixes shifted. Used when two or more tables are joined.
 *
 * <p>
 *
 *  Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type represented by this descriptor
 * @param <PARENT> The type of the parent of type <code>ENTITY</code>, if there is one
 * @param <ENTITYBUILDER> The type that can build a <code>ENTITY</code>
 */
public class RelativeDaoDescriptor<ENTITY, PARENT, ENTITYBUILDER> implements DaoDescriptor<ENTITY, ENTITYBUILDER> {

    private final String tableName;
    private final Supplier<ENTITYBUILDER> supplier;

    private final ColumnCollection<ENTITY, ENTITYBUILDER> columnCollection;
    private final List<ChildrenDescriptor<ENTITY,?, ENTITYBUILDER,?>> childrenDescriptors;
    private final Function<ENTITYBUILDER, ENTITY> buildFunction;

    public RelativeDaoDescriptor(DaoDescriptor<ENTITY, ENTITYBUILDER> originalDaoDescriptor, String newPrefix, Prefixer prefixer){
        this.tableName = originalDaoDescriptor.tableName();
        this.supplier = originalDaoDescriptor.supplier();
        this.childrenDescriptors = originalDaoDescriptor.childrenDescriptors();
        this.buildFunction = originalDaoDescriptor.buildFunction();

        List<Column<ENTITY, ENTITYBUILDER>> dataColumns = originalDaoDescriptor.dataColumns().stream().map(c -> c.withPrefix(newPrefix, prefixer)).collect(Collectors.toList());
        List<JoinColumn<ENTITY,?,ENTITYBUILDER,?>> joinColumns = resetColumnPrefixes(prefixer, newPrefix, originalDaoDescriptor.joinColumns());
        PrimaryKey<ENTITY, ENTITYBUILDER> primaryKey = (PrimaryKey<ENTITY, ENTITYBUILDER>) originalDaoDescriptor.primaryKey().withPrefix(newPrefix, prefixer);
        ParentColumn<ENTITY, PARENT, ENTITYBUILDER, ?> parentColumn = null;
        if( originalDaoDescriptor.hasParent()) {
            parentColumn = (ParentColumn<ENTITY, PARENT, ENTITYBUILDER, ?>) originalDaoDescriptor.parentColumn().withPrefix(newPrefix, prefixer);
        }
        this.columnCollection = new ColumnCollection<ENTITY, ENTITYBUILDER>(primaryKey, parentColumn, dataColumns, joinColumns);
    }

    private List<JoinColumn<ENTITY,?, ENTITYBUILDER,?>> resetColumnPrefixes(Prefixer prefixer,
                                                                            String joinedTablePrefix,
                                                                            List<JoinColumn<ENTITY,?, ENTITYBUILDER,?>> joinColumns){
        List<JoinColumn<ENTITY,?, ENTITYBUILDER,?>> tmp = new ArrayList<>();
        for(JoinColumn<ENTITY,?, ENTITYBUILDER,?> column : joinColumns){
            JoinColumn<ENTITY,?, ENTITYBUILDER,?> resetColumn = column.withPrefix(joinedTablePrefix, prefixer);
            tmp.add(resetColumn);
        }
        return tmp;
    }

    @Override
    public String tableName() {
        return tableName;
    }

    @Override
    public Supplier<ENTITYBUILDER> supplier() {
        return supplier;
    }

    @Override
    public ColumnCollection<ENTITY, ENTITYBUILDER> getColumnCollection() {
        return columnCollection;
    }

    @Override
    public List<ChildrenDescriptor<ENTITY, ?, ENTITYBUILDER, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    @Override
    public ParentColumn<ENTITY, ?, ENTITYBUILDER, ?> parentColumn() {
        return columnCollection.getParentColumn();
    }

    @Override
    public Function<ENTITYBUILDER, ENTITY> buildFunction() {
        return buildFunction;
    }
}
