package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GenerativePrimaryKey<PK, ENTITY, BUILDER> implements PrimaryKey<PK, ENTITY, BUILDER> {

    private final Supplier<PK> supplier;

    private final GenericColumn<PK> genericColumn;

    private final String name;
    private final String prefix;
    private final Function<ENTITY, PK> getter;
    private final BiConsumer<BUILDER, PK> setter;

    private String sqlTypeName;

    public GenerativePrimaryKey(Supplier<PK> supplier,
                                GenericColumn<PK> genericColumn,
                                String prefix,
                                String name,
                                Function<ENTITY, PK> getter,
                                BiConsumer<BUILDER, PK> setter) {
        this.supplier = supplier;
        this.genericColumn = genericColumn;
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;

        this.sqlTypeName = genericColumn.getSqlTypeName();
    }

    private GenerativePrimaryKey(Supplier<PK> supplier,
                                 GenericColumn<PK> genericColumn,
                                 String prefix,
                                 String name,
                                 Function<ENTITY, PK> getter,
                                 BiConsumer<BUILDER, PK> setter,
                                 String sqlTypeName) {
        this.supplier = supplier;
        this.genericColumn = genericColumn;
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;

        this.sqlTypeName = sqlTypeName;
    }

    @Override
    public void optimisticSetKey(ENTITY item, PK id) {
        try {
            BUILDER builder = (BUILDER) item;
            setter.accept(builder, id);
        } catch (ClassCastException ignore) {
        }
    }

    @Override
    public void setKey(BUILDER builder, PK id) {
        setter.accept(builder, id);
    }

    @Override
    public PK getKey(ENTITY item) {
        return getter.apply(item);
    }

    @Override
    public KeyProducer<PK> getKeyProducer() {
        return connection -> supplier.get();
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
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException {
        genericColumn.setPreparedStatement(preparedStatement, index, getter.apply(item));
    }

    @Override
    public void notNull() {
        throw new HrormException("Cannot make primary key nullable");
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public PopulateResult populate(BUILDER constructor, ResultSet resultSet) throws SQLException {
        PK value = genericColumn.fromResultSet(resultSet,prefix  + name);
        setter.accept(constructor, value);
        if (value == null){
            return PopulateResult.NoPrimaryKey;
        }
        return PopulateResult.PrimaryKey;
    }

    @Override
    public Column<PK, PK, ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        return new GenerativePrimaryKey<>(
                supplier,
                genericColumn,
                prefix,
                name,
                getter,
                setter,
                sqlTypeName
        );
    }

    @Override
    public ResultSetReader<PK> getReader() {
        return genericColumn::fromResultSet;
    }

    @Override
    public PreparedStatementSetter<PK> getStatementSetter() {
        return genericColumn::setPreparedStatement;
    }

    @Override
    public PK toClassType(PK dbType) {
        return dbType;
    }

    @Override
    public Set<Integer> supportedTypes() {
        return genericColumn.getSupportedTypes();
    }

    @Override
    public String getSqlTypeName() {
        return sqlTypeName;
    }

    @Override
    public void setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
    }

    public GenericColumn<PK> getGenericColumn(){
        return genericColumn;
    }
}
