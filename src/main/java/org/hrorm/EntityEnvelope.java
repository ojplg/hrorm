package org.hrorm;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityEnvelope<ENTITY, BUILDER> {

    private final Supplier<BUILDER> builderSupplier;
    private final Function<BUILDER, ENTITY> buildFunction;

    private final Map<String, EntityField<?,ENTITY, BUILDER>> fields = new HashMap<>();

    public EntityEnvelope(Supplier<BUILDER> builderSupplier, Function<BUILDER, ENTITY> buildFunction){
        this.builderSupplier = builderSupplier;
        this.buildFunction = buildFunction;
    }

    public <T> Supplier<Supplier<Consumer<T>>> addField(String name, Function<ENTITY, T> getter, BiConsumer<BUILDER, T> setter){
        EntityField<T,ENTITY, BUILDER> field = new EntityField<>(name, getter, setter);
        fields.put(name, field);
        return field::valueSetter;
    }

    public ENTITY hydrate(){
        BUILDER builder = builderSupplier.get();
        for(EntityField<?, ENTITY, BUILDER> field : fields.values()){
            //field.populate(builder);
        }
        ENTITY entity = buildFunction.apply(builder);
        return entity;
    }
}
