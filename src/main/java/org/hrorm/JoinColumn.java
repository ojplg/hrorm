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
public class JoinColumn<ENTITY, JOINED, ENTITYBUILDER, JOINEDBUILDER, JOINEDPK> implements Column<JOINEDPK, JOINEDPK, ENTITY, ENTITYBUILDER> {

    private final String name;
    private final String prefix;
    private final String joiningTablePrefix;
    private final BiConsumer<ENTITYBUILDER, JOINED> setter;
    private final Function<ENTITY, JOINED> getter;
    private final DaoDescriptor<JOINEDPK, JOINED, JOINEDBUILDER> joinedDaoDescriptor;
    private final String joinedTablePrimaryKeyName;
    private final ResultSetReader<JOINEDPK> joinedPkResultSetReader;
    private final PreparedStatementSetter<JOINEDPK> joinedPkStatementSetter;
    private boolean nullable;

    private Function<JOINEDBUILDER, JOINED> joinBuilder;

    private String sqlTypeName;

    public JoinColumn(String name,
                      String joiningTablePrefix,
                      Prefixer prefixer,
                      Function<ENTITY, JOINED> getter,
                      BiConsumer<ENTITYBUILDER, JOINED> setter,
                      DaoDescriptor<JOINEDPK, JOINED, JOINEDBUILDER> daoDescriptor,
                      boolean nullable){
        this.name = name;
        this.joiningTablePrefix = joiningTablePrefix;
        this.prefix = prefixer.nextPrefix();
        this.joinedDaoDescriptor = new RelativeDaoDescriptor<>(daoDescriptor, prefix, prefixer);
        this.getter = getter;
        this.setter = setter;
        this.joinedPkResultSetReader = joinedDaoDescriptor.primaryKey().getReader();
        this.joinedPkStatementSetter = joinedDaoDescriptor.primaryKey().getStatementSetter();
        this.nullable = nullable;
        this.joinedTablePrimaryKeyName = joinedDaoDescriptor.primaryKey().getName();
        this.joinBuilder = joinedDaoDescriptor.buildFunction();

        this.sqlTypeName = joinedDaoDescriptor.primaryKey().getSqlTypeName();
    }

    public List<JoinColumn<JOINED,?, JOINEDBUILDER,?,?>> getTransitiveJoins(){
        return this.joinedDaoDescriptor.joinColumns();
    }

    public String getTable(){
        return this.joinedDaoDescriptor.tableName();
    }

    @Override
    public String getName() {
        return name;
    }

    public String getJoiningTablePrefix(){
        return joiningTablePrefix;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public PopulateResult populate(ENTITYBUILDER builder, ResultSet resultSet) throws SQLException {
        JOINEDBUILDER joinedBuilder = joinedDaoDescriptor.supplier().get();
        for (Column<?, ?, JOINED, JOINEDBUILDER> column: joinedDaoDescriptor.nonJoinColumns()) {
            PopulateResult result = column.populate(joinedBuilder, resultSet);
            if ( result == PopulateResult.NoPrimaryKey ){
                return PopulateResult.Ignore;
            }
        }
        for(JoinColumn<JOINED,?, JOINEDBUILDER,?,?> joinColumn : joinedDaoDescriptor.joinColumns()){
            joinColumn.populate(joinedBuilder, resultSet);
        }

        JOINED joinedItem = joinBuilder.apply(joinedBuilder);
        setter.accept(builder, joinedItem);
        return PopulateResult.fromJoinColumn(
                connection -> {
                    for(ChildrenDescriptor<JOINED,?, JOINEDBUILDER,?,?,?> childrenDescriptor : joinedDaoDescriptor.childrenDescriptors()){
                        childrenDescriptor.populateChildren(connection, joinedBuilder);
                    }
                }
        );
    }

    public ResultSetReader<JOINEDPK> getReader(){
        return joinedPkResultSetReader;
    }

    @Override
    public PreparedStatementSetter<JOINEDPK> getStatementSetter() {
        return joinedPkStatementSetter;
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
            JOINEDPK id = joinedDaoDescriptor.primaryKey().getKey(value);
            joinedPkStatementSetter.apply(preparedStatement, index, id);
        }
    }

    @Override
    public JoinColumn<ENTITY, JOINED, ENTITYBUILDER, JOINEDBUILDER, JOINEDPK> withPrefix(String newPrefix, Prefixer prefixer) {
        return new JoinColumn(name, newPrefix, prefixer, getter, setter, joinedDaoDescriptor, nullable);
    }

    public List<Column<?, ?, JOINED, JOINEDBUILDER>> getNonJoinColumns(){
        return this.joinedDaoDescriptor.nonJoinColumns();
    }

    @Override
    public void notNull() {
        nullable = false;
    }

    public String getJoinedTablePrimaryKeyName() {
        return joinedTablePrimaryKeyName;
    }

    @Override
    public Set<Integer> supportedTypes() {
        // FIXME: incorrect types here
        return ColumnTypes.IntegerTypes;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public String getSqlTypeName() { return sqlTypeName; }

    @Override
    public void setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
    }

    @Override
    public JOINEDPK toClassType(JOINEDPK dbType) {
        return dbType;
    }

    @Override
    public GenericColumn<JOINEDPK> asGenericColumn() {
        return new GenericColumn<>(joinedPkStatementSetter, joinedPkResultSetReader, supportedTypes().iterator().next(), sqlTypeName, supportedTypes());
    }
}
