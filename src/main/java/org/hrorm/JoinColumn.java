package org.hrorm;

import java.sql.Connection;
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
public class JoinColumn<T, J> implements TypedColumn<T> {

    private final String name;
    private final String prefix;
    private final String joinedTablePrefix;
    private final BiConsumer<T, J> setter;
    private final Function<T, J> getter;
    private final DaoDescriptor<J> daoDescriptor;
    private boolean nullable;
    private J lastItem;

    public JoinColumn(String name, String joinedTablePrefix, Prefixer prefixer, Function<T, J> getter, BiConsumer<T,J> setter, DaoDescriptor<J> daoDescriptor, boolean nullable){
        this.name = name;
        this.prefix = prefixer.nextPrefix();
        this.joinedTablePrefix = joinedTablePrefix;
        this.getter = getter;
        this.setter = setter;
        this.daoDescriptor = new RelativeDaoDescriptor<>(daoDescriptor, prefix, prefixer);
        this.nullable = nullable;
    }

    public List<JoinColumn<J,?>> getTransitiveJoins(){
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
    public PopulateResult populate(T item, ResultSet resultSet) throws SQLException {
        J joined = daoDescriptor.supplier().get();
        for (TypedColumn<J> column: daoDescriptor.dataColumns()) {
            PopulateResult result = column.populate(joined, resultSet);
            if ( result == PopulateResult.NoPrimaryKey ){
                return PopulateResult.Ignore;
            }
        }
        for(JoinColumn<J,?> joinColumn : daoDescriptor.joinColumns()){
            joinColumn.populate(joined, resultSet);
        }
        setter.accept(item, joined);
        lastItem = joined;
        return PopulateResult.Ignore;
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

    public JoinColumn<T,J> withPrefixes(Prefixer prefixer, String joinedTablePrefix) {
        return new JoinColumn(name, joinedTablePrefix, prefixer, getter, setter, daoDescriptor, nullable);
    }

    @Override
    public TypedColumn<T> withPrefix(String prefix) {
        throw new UnsupportedOperationException();
    }

    public List<TypedColumn<J>> getDataColumns(){
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

    public void populateChildren(Connection connection){
        for(ChildrenDescriptor<J,?> childrenDescriptor : daoDescriptor.childrenDescriptors()){
            childrenDescriptor.populateChildren(connection, lastItem);
        }
    }
}
