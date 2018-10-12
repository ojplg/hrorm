package org.hrorm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RelativeDaoDescriptor<T, P> implements DaoDescriptor<T> {

    private final String tableName;
    private final Supplier<T> supplier;
    private final List<TypedColumn<T>> dataColumns;
    private final List<JoinColumn<T, ?>> joinColumns;
    private final PrimaryKey<T> primaryKey;
    private final List<ChildrenDescriptor<T,?>> childrenDescriptors;
    private final ParentColumn<T,P> parentColumn;

    public RelativeDaoDescriptor(DaoDescriptor<T> originalDaoDescriptor, String newPrefix, Prefixer prefixer){
        this.tableName = originalDaoDescriptor.tableName();
        this.supplier = originalDaoDescriptor.supplier();
        this.dataColumns = originalDaoDescriptor.dataColumns().stream().map(c -> c.withPrefix(newPrefix, prefixer)).collect(Collectors.toList());
        this.joinColumns = resetColumnPrefixes(prefixer, newPrefix, originalDaoDescriptor.joinColumns());
        this.primaryKey = originalDaoDescriptor.primaryKey();
        this.childrenDescriptors = originalDaoDescriptor.childrenDescriptors();
        this.parentColumn = originalDaoDescriptor.parentColumn();
    }

    private List<JoinColumn<T,?>> resetColumnPrefixes(Prefixer prefixer, String joinedTablePrefix, List<JoinColumn<T,?>> joinColumns){
        List<JoinColumn<T,?>> tmp = new ArrayList<>();
        for(JoinColumn<T,?> column : joinColumns){
            JoinColumn<T,?> resetColumn = column.withPrefix(joinedTablePrefix, prefixer);
            tmp.add(resetColumn);
        }
        return tmp;
    }

    @Override
    public String tableName() {
        return tableName;
    }

    @Override
    public Supplier<T> supplier() {
        return supplier;
    }

    @Override
    public List<TypedColumn<T>> dataColumns() {
        return dataColumns;
    }

    @Override
    public List<JoinColumn<T, ?>> joinColumns() {
        return joinColumns;
    }

    @Override
    public PrimaryKey<T> primaryKey() {
        return primaryKey;
    }

    @Override
    public List<ChildrenDescriptor<T, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    @Override
    public ParentColumn<T, P> parentColumn() {
        return parentColumn;
    }
}
