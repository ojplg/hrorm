package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.Booleans;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class BooleansTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("booleans");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @After
    public void clearTable() { helper.clearTable("booleans_table");}


    private DaoBuilder<Booleans> daoBuilder(){
        return new DaoBuilder<>("booleans_table", Booleans::new )
                .withPrimaryKey("id", "booleans_sequence", Booleans::getId, Booleans::setId)
                .withBooleanColumn("boolean_column", Booleans::isBooleanFlag, Booleans::setBooleanFlag)
                .withStringBooleanColumn("string_column", Booleans::isStringFlag, Booleans::setStringFlag)
                .withIntegerBooleanColumn("integer_column", Booleans::isIntFlag, Booleans::setIntFlag);
    }


    @Test
    public void testInsertAndSelect() throws SQLException {
        long id;
        {
            Connection connection = helper.connect();
            Dao<Booleans> dao = daoBuilder().buildDao(connection);

            Booleans bools = new Booleans();
            bools.setBooleanFlag(true);
            bools.setIntFlag(true);
            bools.setStringFlag(true);

            id = dao.insert(bools);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Booleans> dao = daoBuilder().buildDao(connection);

            Booleans booleans = dao.select(id);

            Assert.assertTrue(booleans.isBooleanFlag());
            Assert.assertTrue(booleans.isStringFlag());
            Assert.assertTrue(booleans.isIntFlag());

            connection.close();
        }
    }

    @Test
    public void testInsertAndUpdate() throws SQLException {
        long id;
        {
            Connection connection = helper.connect();
            Dao<Booleans> dao = daoBuilder().buildDao(connection);

            Booleans bools = new Booleans();
            bools.setBooleanFlag(false);
            bools.setIntFlag(false);
            bools.setStringFlag(false);

            id = dao.insert(bools);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Booleans> dao = daoBuilder().buildDao(connection);

            Booleans booleans = dao.select(id);

            Assert.assertFalse(booleans.isBooleanFlag());
            Assert.assertFalse(booleans.isStringFlag());
            Assert.assertFalse(booleans.isIntFlag());

            booleans.setStringFlag(true);
            booleans.setIntFlag(true);
            booleans.setBooleanFlag(true);

            dao.update(booleans);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Booleans> dao = daoBuilder().buildDao(connection);

            Booleans booleans = dao.select(id);

            Assert.assertTrue(booleans.isBooleanFlag());
            Assert.assertTrue(booleans.isStringFlag());
            Assert.assertTrue(booleans.isIntFlag());

            connection.close();
        }
    }

}
