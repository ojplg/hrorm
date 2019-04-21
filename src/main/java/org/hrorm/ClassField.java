package org.hrorm;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ClassField<TYPE, ENTITY, BUILDER> {

    private final Function<ENTITY, TYPE> getter;
    private final BiConsumer<BUILDER, TYPE> setter;

    public ClassField(Function<ENTITY, TYPE> getter, BiConsumer<BUILDER, TYPE> setter) {
        this.getter = getter;
        this.setter = setter;
    }
}
