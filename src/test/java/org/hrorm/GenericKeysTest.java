package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.generickeys.GenericKeysBuilders;
import org.hrorm.examples.generickeys.StringKeyed;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class GenericKeysTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("generic_keys_test");

    @BeforeClass
    public static void setUpDb(){
        Schema schema = new Schema(GenericKeysBuilders.STRING_KEYED_DAO_BUILDER);
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
    public void testInsertAndSelect() throws SQLException {
        String key;
        {
            Connection connection = helper.connect();
            GenericKeyDao<StringKeyed, String> dao = GenericKeysBuilders.STRING_KEYED_DAO_BUILDER.buildDao(connection);

            StringKeyed item = new StringKeyed();
            item.setData(16L);

            key = dao.insert(item);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            GenericKeyDao<StringKeyed, String> dao = GenericKeysBuilders.STRING_KEYED_DAO_BUILDER.buildDao(connection);

            StringKeyed item = dao.select(key);
            Assert.assertEquals(16L, (long) item.getData());
            Assert.assertNotNull(item.getId());

            connection.close();
        }
    }


    @Test
    public void testInsertUpdateAndSelect() throws SQLException {
        String key;
        {
            Connection connection = helper.connect();
            GenericKeyDao<StringKeyed, String> dao = GenericKeysBuilders.STRING_KEYED_DAO_BUILDER.buildDao(connection);

            StringKeyed item = new StringKeyed();
            item.setData(166L);

            key = dao.insert(item);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            GenericKeyDao<StringKeyed, String> dao = GenericKeysBuilders.STRING_KEYED_DAO_BUILDER.buildDao(connection);

            StringKeyed item = dao.select(key);
            Assert.assertEquals(166L, (long) item.getData());
            Assert.assertNotNull(item.getId());

            item.setData(133L);

            dao.update(item);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            GenericKeyDao<StringKeyed, String> dao = GenericKeysBuilders.STRING_KEYED_DAO_BUILDER.buildDao(connection);

            StringKeyed item = dao.select(key);
            Assert.assertEquals(133L, (long) item.getData());
        }

    }

}