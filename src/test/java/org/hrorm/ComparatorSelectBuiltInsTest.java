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

import java.util.Arrays;
import java.util.List;

import static org.hrorm.Where.inColumn;
import static org.hrorm.Where.notInColumn;

public class ComparatorSelectBuiltInsTest {

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
    public void testGenericColumnInAndNotIn(){

        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = Builders.forPlatform(helper.getPlatform()).buildDao(connection);

            for(int idx=1; idx<=10; idx++){
                BuiltIns item = new BuiltIns();
                item.setIntValue(idx * 2);
                item.setFloatValue((float) idx);
                dao.insert(item);
            }
        });

        helper.useConnection(connection -> {
            Dao<BuiltIns> dao = Builders.forPlatform(helper.getPlatform()).buildDao(connection);

            List<Integer> excluded = Arrays.asList(2,4,6,10,14,16);
            List<Float> included = Arrays.asList(1F,3F,9F,2F);

            List<BuiltIns> items = dao.select(
                    notInColumn("int_value", GenericColumn.INTEGER, excluded )
                    .and(inColumn("float_value", GenericColumn.FLOAT, included)));

            Assert.assertEquals(1, items.size());
            BuiltIns item = items.get(0);
            Assert.assertEquals(18, item.getIntValue());
            Assert.assertEquals(9F, item.getFloatValue(), 0.001);
        });
    }

}
