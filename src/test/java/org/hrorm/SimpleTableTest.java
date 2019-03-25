package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.Simple;
import org.hrorm.database.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SimpleTableTest {

    private static Helper helper = HelperFactory.forSchema("simple");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    private DaoBuilder<Simple> daoBuilder(){
        return new DaoBuilder<>("simple", Simple::new)
                .withPrimaryKey("simple_id", "simple_seq", Simple::getId, Simple::setId)
                .withStringColumn("field", Simple::getField, Simple::setField);
    }

    @Test
    public void testInsertSetsPrimaryKey() throws SQLException {
        Connection connection = helper.connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("PrimaryKeyTest");

        dao.insert(simple);

        Assert.assertTrue(simple.getId() > 0);
        connection.commit();
        connection.close();
    }

    @Test
    public void testInsertAndSelect() throws SQLException {
        Connection connection = helper.connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("InsertSelectTest");

        dao.insert(simple);

        Simple dbInstance = dao.select(simple.getId());

        Assert.assertEquals("InsertSelectTest", dbInstance.getField());
        connection.commit();
        connection.close();
    }

    @Test
    public void testUpdates() throws SQLException {
        Connection connection = helper.connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("UpdateTest");

        dao.insert(simple);

        Simple dbInstance = dao.select(simple.getId());
        Assert.assertEquals("UpdateTest", dbInstance.getField());

        simple.setField("UpdateTest New Value");
        dao.update(simple);

        dbInstance = dao.select(simple.getId());
        Assert.assertEquals("UpdateTest New Value", dbInstance.getField());
        connection.commit();
        connection.close();
    }

    @Test
    public void testSelectByColumn() throws SQLException {
        Connection connection = helper.connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("Select By Column Test");

        dao.insert(simple);

        Simple template = new Simple();
        template.setField("Select By Column Test");

        Simple dbInstance = dao.selectByColumns(template, "field");
        Assert.assertEquals(simple.getId(), dbInstance.getId());
        connection.commit();
        connection.close();
    }

    @Test
    public void testDelete() throws SQLException {
        Connection connection = helper.connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("Delete Test");

        dao.insert(simple);

        Simple fromDb = dao.select(simple.getId());
        Assert.assertNotNull(fromDb);

        dao.delete(simple);

        Simple fromDbAfterDelete = dao.select(simple.getId());
        Assert.assertNull(fromDbAfterDelete);
        connection.commit();
        connection.close();

    }

    @Test
    public void testSelectAll() throws SQLException {

        helper.clearTables();

        Connection connection = helper.connect();

        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple red = new Simple();
        red.setField("Red");
        dao.insert(red);

        Simple blue = new Simple();
        blue.setField("Blue");
        dao.insert(blue);

        Simple green = new Simple();
        green.setField("Green");
        dao.insert(green);

        List<Simple> items = dao.selectAll();

        Assert.assertEquals(3, items.size());
        connection.commit();
        connection.close();
    }
}
