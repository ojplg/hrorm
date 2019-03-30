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
                .withIntegerBooleanColumn("integer_column", Booleans::isIntFlag, Booleans::setIntFlag)
                .withIntegerBooleanColumn("integer_object_column", Booleans::getIntegerObjectFlag, Booleans::setIntegerObjectFlag)
                .withBooleanColumn("object_column", Booleans::getObjectFlag, Booleans::setObjectFlag)
                .withStringBooleanColumn("string_object_column", Booleans::getStringObjectFlag, Booleans::setStringObjectFlag);
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
            bools.setIntegerObjectFlag(true);
            bools.setObjectFlag(null);
            bools.setStringObjectFlag(false);

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
            Assert.assertTrue(booleans.getIntegerObjectFlag());
            Assert.assertNull(booleans.getObjectFlag());
            Assert.assertFalse(booleans.getStringObjectFlag());

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
            bools.setIntegerObjectFlag(null);
            bools.setStringObjectFlag(true);
            bools.setObjectFlag(false);

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
            Assert.assertNull(booleans.getIntegerObjectFlag());
            Assert.assertTrue(booleans.getStringObjectFlag());
            Assert.assertFalse(booleans.getObjectFlag());

            booleans.setStringFlag(true);
            booleans.setIntFlag(true);
            booleans.setBooleanFlag(true);
            booleans.setIntegerObjectFlag(false);
            booleans.setStringObjectFlag(null);
            booleans.setObjectFlag(true);

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
            Assert.assertFalse(booleans.getIntegerObjectFlag());
            Assert.assertNull(booleans.getStringObjectFlag());
            Assert.assertTrue(booleans.getObjectFlag());

            connection.close();
        }
    }

}
