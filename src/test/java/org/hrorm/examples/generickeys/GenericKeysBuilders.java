package org.hrorm.examples.generickeys;

import org.hrorm.GenericKeyDaoBuilder;
import org.hrorm.GenericColumn;
import org.hrorm.util.RandomUtils;

import java.util.function.Supplier;

public class GenericKeysBuilders {

    public static Supplier<String> newStringKey = () -> RandomUtils.randomAlphabeticString(10,12);

    public static final GenericKeyDaoBuilder<StringKeyed, StringKeyed, String> STRING_KEYED_DAO_BUILDER =
            new GenericKeyDaoBuilder<>("string_keyed_table", StringKeyed::new, t -> t, newStringKey)
                    .withPrimaryKey("id", GenericColumn.STRING, StringKeyed::getId, StringKeyed::setId);

}
