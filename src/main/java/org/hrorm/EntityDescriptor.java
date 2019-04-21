package org.hrorm;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityDescriptor<ENTITY, BUILDER> {

    private final Supplier<BUILDER> builderSupplier;
    private final Function<BUILDER, ENTITY> buildFunction;

    private final Map<String, EntityField<?,?,ENTITY, BUILDER>> fields = new HashMap<>();

    public EntityDescriptor(Supplier<BUILDER> builderSupplier, Function<BUILDER, ENTITY> buildFunction){
        this.builderSupplier = builderSupplier;
        this.buildFunction = buildFunction;
    }

    public <CLASSTYPE, DBTYPE> void addField(String name,
                                             Function<ENTITY, CLASSTYPE> getter,
                                             BiConsumer<BUILDER, CLASSTYPE> setter,
                                             GenericColumn<DBTYPE> genericColumn,
                                             Converter<CLASSTYPE, DBTYPE> converter){
        String capitalizedName = name.toUpperCase();
        ClassField<CLASSTYPE, ENTITY, BUILDER> classField = new ClassField<>(getter, setter);
        EntityField<CLASSTYPE, DBTYPE, ENTITY, BUILDER> field = new EntityField<>(capitalizedName, classField, genericColumn, converter);
        fields.put(capitalizedName, field);
    }

    public <TYPE> void addField(String name,
                                Function<ENTITY, TYPE> getter,
                                BiConsumer<BUILDER, TYPE> setter,
                                GenericColumn<TYPE> genericColumn){
        addField(name, getter, setter, genericColumn, Converters.identity());
    }

    public ENTITY hydrate(){
        BUILDER builder = builderSupplier.get();
        for(EntityField<?, ?, ENTITY, BUILDER> field : fields.values()){
            //field.populate(builder);
        }
        ENTITY entity = buildFunction.apply(builder);
        return entity;
    }
}
