package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Primary key for an entity whose construction is direct, i.e. the
 * entity has setters.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type of the class being persisted.
 */
public class DirectPrimaryKey<ENTITY> implements SequencedPrimaryKey<ENTITY, ENTITY> {

    private final String prefix;
    private final String name;
    private final String sequenceName;
    private final BiConsumer<ENTITY, Long> setter;
    private final Function<ENTITY, Long> getter;

    private String sqlTypeName = "integer";

    public DirectPrimaryKey(String prefix,
                            String name,
                            String sequenceName,
                            Function<ENTITY, Long> getter,
                            BiConsumer<ENTITY, Long> setter) {
        this.prefix = prefix;
        this.name = name;
        this.sequenceName = sequenceName;
        this.setter = setter;
        this.getter = getter;
    }

    @Override
    public Long getKey(ENTITY item) {
        if ( item == null ){
            throw new HrormException("Cannot get a key from a null item ");
        }
        return getter.apply(item);
    }

    @Override
    public String getSequenceName() {
        return sequenceName;
    }

    @Override
    public void optimisticSetKey(ENTITY item, Long id) {
        setter.accept(item, id);
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
    public PopulateResult populate(ENTITY constructor, ResultSet resultSet) throws SQLException {
        long value = resultSet.getLong(prefix  + name);
        setter.accept(constructor, value);
        if (value == 0){
            return PopulateResult.NoPrimaryKey;
        }
        return PopulateResult.PrimaryKey;

    }

    @Override
    public ResultSetReader<Long> getReader(){
        return ResultSet::getLong;
    }

    @Override
    public PreparedStatementSetter<Long> getStatementSetter() {
        return PreparedStatement::setLong;
    }

    @Override
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException {
        Long value = getter.apply(item);
        if ( value == null ){
            throw new HrormException("Tried to set a null value for the primary key named " + name);
        } else {
            preparedStatement.setLong(index, value);
        }
    }

    @Override
    public Column<Long, Long, ENTITY, ENTITY> withPrefix(String newPrefix, Prefixer prefixer) {
        return new DirectPrimaryKey<>(newPrefix, name, sequenceName, getter, setter);
    }

    @Override
    public boolean isPrimaryKey() {
        return true;
    }

    @Override
    public void notNull() {
        throw new HrormException("Cannot set a primary key to be nullable");
    }

    @Override
    public Set<Integer> supportedTypes() { return ColumnTypes.IntegerTypes; }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public String getSqlTypeName() { return sqlTypeName; }

    @Override
    public void setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
    }

    @Override
    public Long toClassType(Long value){
        return value;
    }

    @Override
    public KeyProducer<Long> getKeyProducer() {
        return new SequenceKeyProducer(this);
    }

    @Override
    public GenericColumn<Long> asGenericColumn() {
        return new GenericColumn<>(PreparedStatement::setLong, ResultSet::getLong, Types.INTEGER, sqlTypeName, ColumnTypes.IntegerTypes);

    }
}
