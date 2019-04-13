package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.builtins.Builders;
import org.hrorm.examples.builtins.BuiltIns;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

import static org.hrorm.Operator.GREATER_THAN;
import static org.hrorm.Operator.LESS_THAN;
import static org.hrorm.Where.where;

public class GenericColumnBuiltInsTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("generic_columns_built_ins_test");

    @BeforeClass
    public static void setUpDb(){
        Schema schema = new Schema(Builders.forPlatform(helper.getPlatform()));
        String sql = schema.sql();
        helper.initializeSchemaFromSql(sql);
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @After
    public void clearTable(){
        helper.clearTables();
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

            Dao<BuiltIns> dao = Builders.forPlatform(helper.getPlatform()).buildDao(connection);
            return dao.insert(builtIns);
        });
        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = Builders.forPlatform(helper.getPlatform()).buildDao(connection);

            BuiltIns builtIns = dao.select(id);

            Assert.assertEquals(32, builtIns.getIntValue());
            Assert.assertEquals(2, builtIns.getByteValue());
            Assert.assertEquals(123.123, builtIns.getDoubleValue(), 0.0001);
            Assert.assertEquals(456.456, builtIns.getFloatValue(), 0.0001);

            Assert.assertEquals(new Timestamp(987654321), builtIns.getTimestampValue());
        });
    }

    @Test
    public void testWhereClauseBuilding(){
        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = dao(connection);
            for(int idx=1; idx<=10; idx++){
                BuiltIns obj = new BuiltIns();
                obj.setIntValue(idx);
                dao.insert(obj);
            }
        });
        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = dao(connection);
            List<BuiltIns> found = dao.select(where("int_value", GREATER_THAN, 5, GenericColumn.INTEGER));
            Assert.assertEquals(5, found.size());
        });
    }


    @Test
    public void testWhereClauseBuildingWithAnd(){
        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = dao(connection);
            for(int idx=1; idx<=10; idx++){
                float f = (float) idx;
                f *= 3.4;
                BuiltIns obj = new BuiltIns();
                obj.setIntValue(idx);
                obj.setFloatValue(f);
                dao.insert(obj);
            }
        });
        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = dao(connection);
            List<BuiltIns> found = dao.select(where("int_value", GREATER_THAN, 5, GenericColumn.INTEGER)
                                                .and("float_value", LESS_THAN, 24.5f, GenericColumn.FLOAT));
            Assert.assertEquals(2, found.size());
        });
    }

    @Test
    public void testWhereClauseBuildingWithOr(){
        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = dao(connection);
            for(int idx=1; idx<=10; idx++){
                float f = (float) idx;
                f *= 3.4;
                BuiltIns obj = new BuiltIns();
                obj.setIntValue(idx);
                obj.setFloatValue(f);
                dao.insert(obj);
            }
        });
        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = dao(connection);
            List<BuiltIns> found = dao.select(where("int_value", GREATER_THAN, 5, GenericColumn.INTEGER)
                    .or("float_value", LESS_THAN, 16.5f, GenericColumn.FLOAT));
            Assert.assertEquals(9, found.size());
        });
    }
    
    @Test
    public void testValidation(){
        helper.useConnection(connection -> {
            Validator.validate(connection, Builders.forPlatform(helper.getPlatform()));
        });
    }

    private Dao<BuiltIns> dao(Connection connection){
        return Builders.forPlatform(helper.getPlatform()).buildDao(connection);
    }

}
