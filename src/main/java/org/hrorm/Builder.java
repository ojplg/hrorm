package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Builder<ENTITY, ENTITYBUILDER, B extends Builder<?,?,?>> implements DaoDescriptor<ENTITY,ENTITYBUILDER> {

    private final ColumnCollection<ENTITY,ENTITYBUILDER> columnCollection = new ColumnCollection<>();
    private final DaoBuilderHelper<ENTITY, ENTITYBUILDER> daoBuilderHelper;
    private final List<ChildrenDescriptor<ENTITY,?, ENTITYBUILDER,?>> childrenDescriptors = new ArrayList<>();

    public Builder(String tableName, Supplier<ENTITYBUILDER> supplier, Function<ENTITYBUILDER, ENTITY> buildFunction){
        this.daoBuilderHelper = new DaoBuilderHelper<>(tableName, supplier, buildFunction);
    }


    public Dao<ENTITY> buildDao(Connection connection){
        if( primaryKey() == null){
            throw new HrormException("Cannot create a Dao without a primary key.");
        }
        return new DaoImpl<>(connection, this);
    }

    public Builder<ENTITY, ENTITYBUILDER, B> withStringColumn(String columnName, Function<ENTITY, String> getter, BiConsumer<ENTITYBUILDER, String> setter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.stringColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return this;
    }

}
