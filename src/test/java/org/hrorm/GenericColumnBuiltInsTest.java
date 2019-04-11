package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.builtins.Builders;
import org.hrorm.examples.builtins.BuiltIns;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;

public class GenericColumnBuiltInsTest {


    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("generic_columns_built_ins_test");

    @BeforeClass
    public static void setUpDb(){
        Schema schema = new Schema(Builders.BUILT_INS_DAO_BUILDER);
        String sql = schema.sql();
        helper.initializeSchemaFromSql(sql);
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @Test
    public void testInsertAndSelect(){
        long id = helper.useConnection(connection -> {
            BuiltIns builtIns = new BuiltIns();

            builtIns.setIntValue(32);
            builtIns.setByteValue((byte)2);
            builtIns.setDoubleValue(123.123);
            builtIns.setFloatValue(456.456F);

            builtIns.setTimestampValue(new Timestamp(987654321));

            Dao<BuiltIns> dao = Builders.BUILT_INS_DAO_BUILDER.buildDao(connection);
            return dao.insert(builtIns);
        });
        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = Builders.BUILT_INS_DAO_BUILDER.buildDao(connection);

            BuiltIns builtIns = dao.select(id);

            Assert.assertEquals(32, builtIns.getIntValue());
            Assert.assertEquals(2, builtIns.getByteValue());
            Assert.assertEquals(123.123, builtIns.getDoubleValue(), 0.0001);
            Assert.assertEquals(456.456, builtIns.getFloatValue(), 0.0001);

            Assert.assertEquals(new Timestamp(987654321), builtIns.getTimestampValue());
        });
    }

    @Test
    public void testValidation(){
        helper.useConnection(connection -> {
            Validator.validate(connection, Builders.BUILT_INS_DAO_BUILDER);
        });
    }

}
