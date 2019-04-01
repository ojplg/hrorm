package org.hrorm;

import java.util.function.Function;
import java.util.function.Supplier;

public class DaoBuilderHelper<ENTITY,BUILDER> {

    private final Prefixer prefixer;

    private final String tableName;
    private final String prefix;
    private final Function<BUILDER, ENTITY> buildFunction;
    private final Supplier<BUILDER> supplier;

    public DaoBuilderHelper(String tableName, Supplier<BUILDER> supplier, Function<BUILDER,ENTITY> buildFunction){
        this.prefixer = new Prefixer();
        this.prefix = prefixer.nextPrefix();
        this.tableName = tableName;
        this.supplier = supplier;
        this.buildFunction = buildFunction;
    }

    public String getTableName() {
        return tableName;
    }

    public String getPrefix() {
        return prefix;
    }

    public Function<BUILDER, ENTITY> getBuildFunction() {
        return buildFunction;
    }

    public Supplier<BUILDER> getSupplier() {
        return supplier;
    }

    public Prefixer getPrefixer(){
        return prefixer;
    }
}
