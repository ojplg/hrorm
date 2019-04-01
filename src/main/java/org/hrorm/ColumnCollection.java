package org.hrorm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColumnCollection<ENTITY,BUILDER> {

    private PrimaryKey<ENTITY, BUILDER> primaryKey;
    private ParentColumn<ENTITY, ?, BUILDER, ?> parentColumn;
    private List<Column<ENTITY, BUILDER>> dataColumns = new ArrayList<>();
    private List<JoinColumn<ENTITY, ?, BUILDER, ?>> joinColumns = new ArrayList<>();

    private Column<ENTITY, BUILDER> lastColumnAdded;

    public void addJoinColumn(JoinColumn<ENTITY, ?, BUILDER, ?> joinColumn) {
        lastColumnAdded = joinColumn;
        joinColumns.add(joinColumn);
    }

    public void addDataColumn(Column<ENTITY, BUILDER> dataColumn) {
        lastColumnAdded = dataColumn;
        dataColumns.add(dataColumn);
    }

    public PrimaryKey<ENTITY, BUILDER> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey<ENTITY, BUILDER> primaryKey) {
        if ( this.primaryKey != null ){
            throw new HrormException("Attempt to set a second primary key");
        }
        lastColumnAdded = primaryKey;
        this.primaryKey = primaryKey;
    }

    public ParentColumn<ENTITY, ?, BUILDER, ?> getParentColumn() {
        return parentColumn;
    }

    public void setParentColumn(ParentColumn<ENTITY, ?, BUILDER, ?> parentColumn) {
        if ( this.parentColumn != null ){
            throw new HrormException("Attempt to set a second parent");
        }
        lastColumnAdded = parentColumn;
        this.parentColumn = parentColumn;
    }

    public List<Column<ENTITY, BUILDER>> getDataColumns() {
        return Collections.unmodifiableList(dataColumns);
    }

    public List<JoinColumn<ENTITY, ?, BUILDER, ?>> getJoinColumns() {
        return Collections.unmodifiableList(joinColumns);
    }

    public void setLastColumnAddedNotNull() {
        if (lastColumnAdded == null) {
            throw new HrormException("No column to set as not null has been added.");
        }
        lastColumnAdded.notNull();
    }

    public List<Column<ENTITY, BUILDER>> nonJoinColumns() {
        return nonJoinColumns(primaryKey, parentColumn, dataColumns);
    }

    public List<Column<ENTITY, BUILDER>> allColumns() {
        return allColumns(nonJoinColumns(), joinColumns);
    }

    public static <E,B> List<Column<E, B>> allColumns(List<Column<E,B>> nonJoinColumns,
                                                      List<JoinColumn<E,?,B,?>> joinColumns) {
        List<Column<E, B>> columns = new ArrayList<>();
        columns.addAll(nonJoinColumns);
        columns.addAll(joinColumns);
        return Collections.unmodifiableList(columns);
    }

    public static <E, B> List<Column<E, B>> nonJoinColumns(PrimaryKey<E, B> primaryKey,
                                                           ParentColumn<E, ?, B, ?> parentColumn,
                                                           List<Column<E, B>> dataColumns) {
        List<Column<E, B>> columns = new ArrayList<>();
        if (primaryKey != null) {
            columns.add(primaryKey);
        }
        if (parentColumn != null) {
            columns.add(parentColumn);
        }
        columns.addAll(dataColumns);
        return Collections.unmodifiableList(columns);
    }


}
