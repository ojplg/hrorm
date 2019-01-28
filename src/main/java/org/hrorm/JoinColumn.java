package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Set;
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
 * @param <ENTITY> the entity this column belongs to
 * @param <JOINED> the entity being joined
 * @param <ENTITYBUILDER> the class that can construct new entity instances
 * @param <JOINEDBUILDER> the class that can construct instances of the joined entity
 */
public class JoinColumn<ENTITY, JOINED, ENTITYBUILDER, JOINEDBUILDER> implements Column<ENTITY, ENTITYBUILDER> {

    private final String name;
    private final String prefix;
    private final String joinedTablePrefix;
    private final BiConsumer<ENTITYBUILDER, JOINED> setter;
    private final Function<ENTITY, JOINED> getter;
    private final DaoDescriptor<JOINED, JOINEDBUILDER> daoDescriptor;
    private final String joinedTablePrimaryKeyName;
    private boolean nullable;

    private Function<JOINEDBUILDER, JOINED> joinBuilder;

    public JoinColumn(String name, String joinedTablePrefix, Prefixer prefixer, Function<ENTITY, JOINED> getter, BiConsumer<ENTITYBUILDER, JOINED> setter, DaoDescriptor<JOINED, JOINEDBUILDER> daoDescriptor, boolean nullable){
        this.name = name;
        this.prefix = prefixer.nextPrefix();
        this.joinedTablePrefix = joinedTablePrefix;
        this.getter = getter;
        this.setter = setter;
        this.daoDescriptor = new RelativeDaoDescriptor<>(daoDescriptor, prefix, prefixer);
        this.nullable = nullable;
        this.joinedTablePrimaryKeyName = daoDescriptor.primaryKey().orElseThrow(() -> new IllegalArgumentException("JoinColumns must have a PrimaryKey to reference")).getName();
        this.joinBuilder = daoDescriptor.buildFunction();
    }

    public List<JoinColumn<JOINED,?, JOINEDBUILDER,?>> getTransitiveJoins(){
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
    public PopulateResult populate(ENTITYBUILDER builder, ResultSet resultSet) throws SQLException {
        JOINEDBUILDER joinedBuilder = daoDescriptor.supplier().get();
        for (Column<JOINED, JOINEDBUILDER> column: daoDescriptor.dataColumns()) {
            PopulateResult result = column.populate(joinedBuilder, resultSet);
            if ( result == PopulateResult.NoPrimaryKey ){
                return PopulateResult.Ignore;
            }
        }
        for(JoinColumn<JOINED,?, JOINEDBUILDER,?> joinColumn : daoDescriptor.joinColumns()){
            joinColumn.populate(joinedBuilder, resultSet);
        }

        JOINED joinedItem = joinBuilder.apply(joinedBuilder);
        setter.accept(builder, joinedItem);
        return PopulateResult.fromJoinColumn(
                connection -> {
                    for(ChildrenDescriptor<JOINED,?, JOINEDBUILDER,?> childrenDescriptor : daoDescriptor.childrenDescriptors()){
                        childrenDescriptor.populateChildren(connection, joinedBuilder);
                    }
                }
        );
    }

    @Override
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException {
        JOINED value = getter.apply(item);
        if( value == null ){
            if ( nullable ) {
                preparedStatement.setNull(index, Types.INTEGER);
            } else {
                throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
            }
        } else {
            Long id = daoDescriptor.primaryKey().orElseThrow(() -> new IllegalArgumentException("JoinColumns must have PrimaryKey to reference")).getKey(value);
            preparedStatement.setLong(index, id);
        }
    }

    @Override
    public JoinColumn<ENTITY, JOINED, ENTITYBUILDER, JOINEDBUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        return new JoinColumn(name, newPrefix, prefixer, getter, setter, daoDescriptor, nullable);
    }

    public List<Column<JOINED, JOINEDBUILDER>> getDataColumns(){
        return this.daoDescriptor.dataColumns();
    }

    @Override
    public void notNull() {
        nullable = false;
    }

    public String getJoinedTablePrimaryKeyName() {
        return joinedTablePrimaryKeyName;
    }

    @Override
    public Set<Integer> supportedTypes() { return ColumnTypes.IntegerTypes; }

}
