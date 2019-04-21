package org.hrorm;

public class EntityField<CLASSTYPE, DBTYPE, ENTITY, BUILDER> {

    private final String name;
    private final ClassField<CLASSTYPE, ENTITY, BUILDER> classField;
    private final Converter<CLASSTYPE, DBTYPE> converter;
    private final GenericColumn<DBTYPE> genericColumn;

    public EntityField(String name, ClassField<CLASSTYPE, ENTITY, BUILDER> classField, GenericColumn<DBTYPE> genericColumn, Converter<CLASSTYPE, DBTYPE> converter) {
        this.name = name;
        this.classField = classField;
        this.genericColumn = genericColumn;
        this.converter = converter;
    }
}
