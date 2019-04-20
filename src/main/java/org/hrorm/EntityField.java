package org.hrorm;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityField<TYPE, ENTITY, BUILDER> {

    private final String name;
    private final Function<ENTITY, TYPE> getter;
    private final BiConsumer<BUILDER, TYPE> setter;

    public EntityField(String name, Function<ENTITY, TYPE> getter, BiConsumer<BUILDER, TYPE> setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    public Supplier<Consumer<TYPE>> valueSetter(){
        throw new UnsupportedOperationException();
    }

}
