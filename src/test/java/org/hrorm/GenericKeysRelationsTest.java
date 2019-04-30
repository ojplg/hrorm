package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.generickeys.Cake;
import org.hrorm.examples.generickeys.Frosting;
import org.hrorm.examples.generickeys.GenericKeysBuilders;
import org.hrorm.examples.generickeys.Layer;
import org.hrorm.examples.generickeys.StringKeyed;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;

public class GenericKeysRelationsTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("generic_keys_relations_test");

    @BeforeClass
    public static void setUpDb(){
        Schema schema = new Schema(GenericKeysBuilders.LAYER_DAO_BUILDER,
                                   GenericKeysBuilders.CAKE_DAO_BUILDER,
                                   GenericKeysBuilders.FROSTING_DAO_BUILDER);
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
    public void testInsertAndSelectWithJoin() throws SQLException {
        String key;
        {
            Connection connection = helper.connect();
            GenericKeyDao<Frosting, Timestamp> frostingDao = GenericKeysBuilders.FROSTING_DAO_BUILDER.buildDao(connection);
            GenericKeyDao<Cake, String> cakeDao = GenericKeysBuilders.CAKE_DAO_BUILDER.buildDao(connection);

            Frosting frosting = new Frosting();
            frosting.setAmount(new BigDecimal("11.345"));

            Timestamp frostingKey = frostingDao.insert(frosting);

            Assert.assertNotNull(frostingKey);
            Assert.assertEquals(frostingKey, frosting.getId());

            Cake cake = new Cake();

            cake.setFlavor("Chocolate");
            cake.setFrosting(frosting);

            key = cakeDao.insert(cake);

            Assert.assertNotNull(key);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();

            GenericKeyDao<Cake, String> cakeDao = GenericKeysBuilders.CAKE_DAO_BUILDER.buildDao(connection);

            Cake cake = cakeDao.select(key);

            Assert.assertEquals(new BigDecimal("11.345"), cake.getFrosting().getAmount());

            connection.close();
        }
    }

    @Test
    public void testInsertAndSelectWithJoinAndChild() throws SQLException {
        String key;
        {
            Connection connection = helper.connect();
            GenericKeyDao<Frosting, Timestamp> frostingDao = GenericKeysBuilders.FROSTING_DAO_BUILDER.buildDao(connection);
            GenericKeyDao<Cake, String> cakeDao = GenericKeysBuilders.CAKE_DAO_BUILDER.buildDao(connection);

            Frosting frosting = new Frosting();
            frosting.setAmount(new BigDecimal("11.345"));

            Timestamp frostingKey = frostingDao.insert(frosting);

            Assert.assertNotNull(frostingKey);
            Assert.assertEquals(frostingKey, frosting.getId());

            Cake cake = new Cake();

            cake.setFlavor("Chocolate");
            cake.setFrosting(frosting);

            Layer layer = new Layer();
            layer.setColor("blue");

            cake.setLayers(Arrays.asList(layer));

            key = cakeDao.insert(cake);

            Assert.assertNotNull(key);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();

            GenericKeyDao<Cake, String> cakeDao = GenericKeysBuilders.CAKE_DAO_BUILDER.buildDao(connection);

            Cake cake = cakeDao.select(key);

            Assert.assertEquals(new BigDecimal("11.345"), cake.getFrosting().getAmount());

            Assert.assertEquals(1, cake.getLayers().size());

            connection.close();
        }
    }

}
