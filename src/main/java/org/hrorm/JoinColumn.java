package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a column that links to a foreign key of some
 * other entity.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> the entity this column belongs to
 * @param <J> the entity being joined
 */
public class JoinColumn<T, J, B, JB> implements IndirectTypedColumn<T, B> {

    private final String name;
    private final String prefix;
    private final String joinedTablePrefix;
    private final BiConsumer<B, J> setter;
    private final Function<T, J> getter;
    private final DaoDescriptor<J,JB> daoDescriptor;
    private final String joinedTablePrimaryKeyName;
    private boolean nullable;

    private Function<JB,J> joinBuilder;

    public JoinColumn(String name, String joinedTablePrefix, Prefixer prefixer, Function<T, J> getter, BiConsumer<B,J> setter, DaoDescriptor<J,JB> daoDescriptor, boolean nullable){
        this.name = name;
        this.prefix = prefixer.nextPrefix();
        this.joinedTablePrefix = joinedTablePrefix;
        this.getter = getter;
        this.setter = setter;
        this.daoDescriptor = new RelativeDaoDescriptor<>(daoDescriptor, prefix, prefixer);
        this.nullable = nullable;
        this.joinedTablePrimaryKeyName = daoDescriptor.primaryKey().getName();
    }

    public List<JoinColumn<J,?,JB,?>> getTransitiveJoins(){
        return this.daoDescriptor.joinColumns();
    }

    public String getTable(){
        return this.daoDescriptor.tableName();
    }

    @Override
    public String getName() {
        return name;
    }

    public String getJoinedTablePrefix(){
        return joinedTablePrefix;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public PopulateResult populate(B builder, ResultSet resultSet) throws SQLException {
        JB joinedBuilder = daoDescriptor.supplier().get();
        for (IndirectTypedColumn<J,JB> column: daoDescriptor.dataColumns()) {
            PopulateResult result = column.populate(joinedBuilder, resultSet);
            if ( result == PopulateResult.NoPrimaryKey ){
                return PopulateResult.Ignore;
            }
        }
        for(JoinColumn<J,?,JB,?> joinColumn : daoDescriptor.joinColumns()){
            joinColumn.populate(joinedBuilder, resultSet);
        }

        J joinedItem = joinBuilder.apply(joinedBuilder);
        setter.accept(builder, joinedItem);
        return PopulateResult.fromJoinColumn(
                connection -> {
                    for(ChildrenDescriptor<J,?,JB,?> childrenDescriptor : daoDescriptor.childrenDescriptors()){
                        childrenDescriptor.populateChildren(connection, joinedBuilder);
                    }
                }
        );
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        J value = getter.apply(item);
        if( value == null ){
            if ( nullable ) {
                preparedStatement.setNull(index, Types.INTEGER);
            } else {
                throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
            }
        } else {
            Long id = daoDescriptor.primaryKey().getKey(value);
            preparedStatement.setLong(index, id);
        }
    }

    @Override
    public JoinColumn<T,J,B,JB> withPrefix(String newPrefix, Prefixer prefixer) {
        return new JoinColumn(name, newPrefix, prefixer, getter, setter, daoDescriptor, nullable);
    }

    public List<IndirectTypedColumn<J,JB>> getDataColumns(){
        return this.daoDescriptor.dataColumns();
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public void notNull() {
        nullable = false;
    }

    public String getJoinedTablePrimaryKeyName() {
        return joinedTablePrimaryKeyName;
    }
}
