package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents a column that links to a foreign key of some
 * other entity.
 *
 * <br/><br/>
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
    private final String table;
    private final BiConsumer<T, J> setter;
    private final Function<T, J> getter;
    private final Supplier<J> supplier;
    private final List<TypedColumn<J>> dataColumns;
    private final PrimaryKey<J> primaryKey;
    private final List<JoinColumn<J,?>> transitiveJoins;

    public JoinColumn(String name, String joinedTablePrefix, Prefixer prefixer, Function<T, J> getter, BiConsumer<T,J> setter, DaoDescriptor<J> daoDescriptor){
        this.name = name;
        this.prefix = prefixer.nextPrefix();
        this.joinedTablePrefix = joinedTablePrefix;
        this.getter = getter;
        this.setter = setter;
        this.table = daoDescriptor.tableName();
        this.supplier = daoDescriptor.supplier();
        this.dataColumns = daoDescriptor.dataColumns().stream().map(c -> c.withPrefix(prefix)).collect(Collectors.toList());
        this.primaryKey = daoDescriptor.primaryKey();
        this.transitiveJoins = resetColumnPrefixes(prefixer,prefix, daoDescriptor.joinColumns());
    }

    private JoinColumn(String name, Prefixer prefixer, String joinedTablePrefix, String table, Function<T, J> getter,
                       BiConsumer<T, J> setter, Supplier<J> supplier, PrimaryKey<J> primaryKey,
                       List<TypedColumn<J>> dataColumns, List<JoinColumn<J,?>> transitiveJoins) {
        this.name = name;
        this.table = table;
        this.joinedTablePrefix = joinedTablePrefix;
        this.prefix = prefixer.nextPrefix();
        this.getter = getter;
        this.setter = setter;
        this.supplier = supplier;
        this.primaryKey = primaryKey;
        this.dataColumns = dataColumns.stream().map(c -> c.withPrefix(prefix) ).collect(Collectors.toList());
        this.transitiveJoins = resetColumnPrefixes(prefixer, prefix, transitiveJoins);
    }

    private List<JoinColumn<J,?>> resetColumnPrefixes(Prefixer prefixer, String joinedTablePrefix, List<JoinColumn<J,?>> joinColumns){
        List<JoinColumn<J,?>> tmp = new ArrayList<>();
        for(JoinColumn<J,?> column : joinColumns){
            JoinColumn<J,?> resetColumn = column.withPrefixes(prefixer, joinedTablePrefix);
            tmp.add(resetColumn);
        }
        return tmp;
    }

    public List<JoinColumn<J,?>> getTransitiveJoins(){
        return transitiveJoins;
    }

    public String getTable(){
        return table;
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
    public void populate(T item, ResultSet resultSet) throws SQLException {
        J joined  = supplier.get();
        for (TypedColumn<J> column: dataColumns) {
            column.populate(joined, resultSet);
        }
        for(JoinColumn<J,?> joinColumn : transitiveJoins){
            joinColumn.populate(joined, resultSet);
        }
        setter.accept(item, joined);
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        J value = getter.apply(item);
        if( value == null){
            throw new RuntimeException("Cannot find join column value for " + item + " for " + name);
        }
        Long id = primaryKey.getKey(value);
        preparedStatement.setLong(index, id);
    }

    public JoinColumn<T,J> withPrefixes(Prefixer prefixer, String joinedTablePrefix) {
        return new JoinColumn<>(name, prefixer, joinedTablePrefix, table, getter, setter, supplier, primaryKey, dataColumns, transitiveJoins);
    }

    @Override
    public TypedColumn<T> withPrefix(String prefix) {
        throw new UnsupportedOperationException();
    }

    public List<TypedColumn<J>> getDataColumns(){
        return dataColumns;
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }
}
