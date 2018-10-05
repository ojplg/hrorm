package org.hrorm;

import org.hrorm.examples.Simple;
import org.hrorm.h2.H2Helper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;

public class SimpleTableTest {

    private static H2Helper helper = new H2Helper("simple");

    @Before
    public void setUpDb(){
        helper.initializeSchema();
    }

    @After
    public void cleanUpDb(){
        helper.dropSchema();
    }

    private DaoBuilder<Simple> daoBuilder(){
        return new DaoBuilder<>("simple", Simple::new)
                .withPrimaryKey("id", "simple_seq", Simple::getId, Simple::setId)
                .withStringColumn("field", Simple::getField, Simple::setField);
    }

    @Test
    public void testInsertSetsPrimaryKey(){
        Connection connection = helper.connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("PrimaryKeyTest");

        dao.insert(simple);

        Assert.assertTrue(simple.getId() > 0);
    }

    @Test
    public void testInsertAndSelect(){
        Connection connection = helper.connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("InsertSelectTest");

        dao.insert(simple);

        Simple dbInstance = dao.select(simple.getId());

        Assert.assertEquals("InsertSelectTest", dbInstance.getField());
    }

    @Test
    public void testUpdates(){
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
    }

    @Test
    public void testSelectByColumn(){
        Connection connection = helper.connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("Select By Column Test");

        dao.insert(simple);

        Simple template = new Simple();
        template.setField("Select By Column Test");

        Simple dbInstance = dao.selectByColumns(template, "field");
        Assert.assertEquals(simple.getId(), dbInstance.getId());
    }

    @Test
    public void testDelete(){
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
    }

    @Test
    public void testSelectAll(){
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
    }
}
