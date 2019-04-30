package org.hrorm.examples.generickeys;

import org.hrorm.GenericKeyDaoBuilder;
import org.hrorm.GenericColumn;
import org.hrorm.util.RandomUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Supplier;

public class GenericKeysBuilders {

    public static Supplier<String> newStringKey = () -> RandomUtils.randomAlphabeticString(10,12);

    public static Supplier<Timestamp> newTimestampKey = () -> Timestamp.from(Instant.now());

    public static final GenericKeyDaoBuilder<StringKeyed, StringKeyed, String> STRING_KEYED_DAO_BUILDER =
            new GenericKeyDaoBuilder<>("string_keyed_table", StringKeyed::new, t -> t, newStringKey)
                    .withPrimaryKey("id", GenericColumn.STRING, StringKeyed::getId, StringKeyed::setId).setSqlTypeName("varchar")
                    .withLongColumn("data", StringKeyed::getData, StringKeyed::setData);

    public static final GenericKeyDaoBuilder<Frosting, Frosting, Timestamp> FROSTING_DAO_BUILDER =
            new GenericKeyDaoBuilder<>("frosting_table", Frosting::new, f -> f, newTimestampKey)
                    .withPrimaryKey("f_id", GenericColumn.TIMESTAMP, Frosting::getId, Frosting::setId)
                    .withBigDecimalColumn("amount", Frosting::getAmount, Frosting::setAmount);

    public static final GenericKeyDaoBuilder<Cake, Cake, String> CAKE_DAO_BUILDER =
            new GenericKeyDaoBuilder<>("cake_table", Cake::new, c -> c, newStringKey)
                    .withPrimaryKey("id", GenericColumn.STRING, Cake::getName, Cake::setName).setSqlTypeName("varchar")
                    .withStringColumn("flavor", Cake::getFlavor, Cake::setFlavor)
                    .withJoinColumn("frosting_id", Cake::getFrosting, Cake::setFrosting, FROSTING_DAO_BUILDER);
}
