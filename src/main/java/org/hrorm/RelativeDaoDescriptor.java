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
 * @param <T> The type represented by this descriptor
 * @param <P> The type of the parent of type <code>T</code>, if there is one
 * @param <B> The type that can build a <code>T</code>
 */
public class RelativeDaoDescriptor<T, P, B> implements DaoDescriptor<T,B> {

    private final String tableName;
    private final Supplier<B> supplier;
    private final List<IndirectTypedColumn<T,B>> dataColumns;
    private final List<JoinColumn<T, ?, B, ?>> joinColumns;
    private final IndirectPrimaryKey<T,B> primaryKey;
    private final List<ChildrenDescriptor<T,?,B,?>> childrenDescriptors;
    private final ParentColumn<T,P,B,?> parentColumn;
    private final Function<B,T> buildFunction;

    public RelativeDaoDescriptor(DaoDescriptor<T,B> originalDaoDescriptor, String newPrefix, Prefixer prefixer){
        this.tableName = originalDaoDescriptor.tableName();
        this.supplier = originalDaoDescriptor.supplier();
        this.dataColumns = originalDaoDescriptor.dataColumns().stream().map(c -> c.withPrefix(newPrefix, prefixer)).collect(Collectors.toList());
        this.joinColumns = resetColumnPrefixes(prefixer, newPrefix, originalDaoDescriptor.joinColumns());
        this.primaryKey = originalDaoDescriptor.primaryKey();
        this.childrenDescriptors = originalDaoDescriptor.childrenDescriptors();
        this.parentColumn = originalDaoDescriptor.parentColumn();
        this.buildFunction = originalDaoDescriptor.buildFunction();
    }

    private List<JoinColumn<T,?,B,?>> resetColumnPrefixes(Prefixer prefixer, String joinedTablePrefix, List<JoinColumn<T,?,B,?>> joinColumns){
        List<JoinColumn<T,?,B,?>> tmp = new ArrayList<>();
        for(JoinColumn<T,?,B,?> column : joinColumns){
            JoinColumn<T,?,B,?> resetColumn = column.withPrefix(joinedTablePrefix, prefixer);
            tmp.add(resetColumn);
        }
        return tmp;
    }

    @Override
    public String tableName() {
        return tableName;
    }

    @Override
    public Supplier<B> supplier() {
        return supplier;
    }

    @Override
    public List<IndirectTypedColumn<T,B>> dataColumns() {
        return dataColumns;
    }

    @Override
    public List<JoinColumn<T, ?, B, ?>> joinColumns() {
        return joinColumns;
    }

    @Override
    public IndirectPrimaryKey<T,B> primaryKey() {
        return primaryKey;
    }

    @Override
    public List<ChildrenDescriptor<T, ?, B, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    @Override
    public ParentColumn<T, P, B, ?> parentColumn() {
        return parentColumn;
    }

    @Override
    public Function<B, T> buildFunction() {
        return buildFunction;
    }
}
