package org.hrorm.examples.builtins;

import org.hrorm.DaoBuilder;
import org.hrorm.GenericColumn;

public class Builders {

    public static final DaoBuilder<BuiltIns> BUILT_INS_DAO_BUILDER =
            new DaoBuilder<>("built_ins", BuiltIns::new)
                    .withPrimaryKey("id", "built_ins_sequence", BuiltIns::getId, BuiltIns::setId)
                    .withGenericColumn("byte_value", BuiltIns::getByteValue, BuiltIns::setByteValue, GenericColumn.BYTE)
                    .withGenericColumn("int_value", BuiltIns::getIntValue, BuiltIns::setIntValue, GenericColumn.INTEGER)
                    .withGenericColumn("float_value", BuiltIns::getFloatValue, BuiltIns::setFloatValue, GenericColumn.FLOAT)
                    .withGenericColumn("double_value", BuiltIns::getDoubleValue, BuiltIns::setDoubleValue, GenericColumn.DOUBLE)
                    .withGenericColumn("timestamp_value", BuiltIns::getTimestampValue, BuiltIns::setTimestampValue, GenericColumn.TIMESTAMP);

}
