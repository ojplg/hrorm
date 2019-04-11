package org.hrorm.examples.builtins;

import org.hrorm.DaoBuilder;
import org.hrorm.GenericColumn;
import org.hrorm.database.DatabasePlatform;

public class Builders {

    public static final DaoBuilder<BuiltIns> H2_BUILT_INS_DAO_BUILDER =
            new DaoBuilder<>("built_ins", BuiltIns::new)
                    .withPrimaryKey("id", "built_ins_sequence", BuiltIns::getId, BuiltIns::setId)
                    .withGenericColumn("byte_value", BuiltIns::getByteValue, BuiltIns::setByteValue, GenericColumn.BYTE)
                    .withGenericColumn("int_value", BuiltIns::getIntValue, BuiltIns::setIntValue, GenericColumn.INTEGER)
                    .withGenericColumn("float_value", BuiltIns::getFloatValue, BuiltIns::setFloatValue, GenericColumn.FLOAT)
                    .withGenericColumn("double_value", BuiltIns::getDoubleValue, BuiltIns::setDoubleValue, GenericColumn.DOUBLE)
                    .withGenericColumn("timestamp_value", BuiltIns::getTimestampValue, BuiltIns::setTimestampValue, GenericColumn.TIMESTAMP);

    public static final DaoBuilder<BuiltIns> POSTGRES_BUILT_INS_DAO_BUILDER =
            new DaoBuilder<>("built_ins", BuiltIns::new)
                    .withPrimaryKey("id", "built_ins_sequence", BuiltIns::getId, BuiltIns::setId)
                    .withGenericColumn("byte_value", BuiltIns::getByteValue, BuiltIns::setByteValue, GenericColumn.BYTE).setSqlTypeName("smallint")
                    .withGenericColumn("int_value", BuiltIns::getIntValue, BuiltIns::setIntValue, GenericColumn.INTEGER)
                    .withGenericColumn("float_value", BuiltIns::getFloatValue, BuiltIns::setFloatValue, GenericColumn.FLOAT).setSqlTypeName("real")
                    .withGenericColumn("double_value", BuiltIns::getDoubleValue, BuiltIns::setDoubleValue, GenericColumn.DOUBLE).setSqlTypeName("double precision")
                    .withGenericColumn("timestamp_value", BuiltIns::getTimestampValue, BuiltIns::setTimestampValue, GenericColumn.TIMESTAMP);


    public static final DaoBuilder<BuiltIns> forPlatform(DatabasePlatform platform){
        switch (platform){
            case H2: return H2_BUILT_INS_DAO_BUILDER;
            case Postgres: return POSTGRES_BUILT_INS_DAO_BUILDER;
        }
        throw new IllegalArgumentException("What is this: " + platform);
    }
}
