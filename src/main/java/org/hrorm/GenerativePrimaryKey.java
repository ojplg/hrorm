package org.hrorm;

import java.util.function.Supplier;

public interface GenerativePrimaryKey<PK, ENTITY, BUILDER> extends PrimaryKey<PK, ENTITY, BUILDER> {
    Supplier<PK> generator();
}
