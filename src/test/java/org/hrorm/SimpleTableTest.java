package org.hrorm;

import org.hrorm.examples.Simple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public class SimpleTableTest {

    public static final String H2ConnectionUrlPrefix = "jdbc:h2:./db/";
    public static final String TestDbName = "simple";

    public static final String TestSchema =
            "create sequence simple_seq;"
            + "create table simple ("
            + " id integer PRIMARY KEY,"
            + " field text );";

    private static boolean initialized;

    private Connection connect() {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(H2ConnectionUrlPrefix + TestDbName);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @After
    public void cleanUpDb(){
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            statement.execute("delete from simple");

            Path path = Paths.get("./db/" + TestDbName + ".mv.db");
            Files.deleteIfExists(path);
            path = Paths.get("./db/" + TestDbName + ".trace.db");
            Files.deleteIfExists(path);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Before
    public void setUpDb(){
        if ( ! initialized ) {
            try {
                Connection connection = connect();
                Statement statement = connection.createStatement();
                statement.execute(TestSchema);
                initialized = true;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private DaoBuilder<Simple> daoBuilder(){
        return new DaoBuilder<>("simple", Simple::new)
                .withPrimaryKey("id", "simple_seq", Simple::getId, Simple::setId)
                .withStringColumn("field", Simple::getField, Simple::setField);
    }

    @Test
    public void testInsertSetsPrimaryKey(){
        Connection connection = connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("PrimaryKeyTest");

        dao.insert(simple);

        Assert.assertTrue(simple.getId() > 0);
    }

    @Test
    public void testInsertAndSelect(){
        Connection connection = connect();
        Dao<Simple> dao = daoBuilder().buildDao(connection);

        Simple simple = new Simple();
        simple.setField("InsertSelectTest");

        dao.insert(simple);

        Simple dbInstance = dao.select(simple.getId());

        Assert.assertEquals("InsertSelectTest", dbInstance.getField());
    }

    @Test
    public void testUpdates(){
        Connection connection = connect();
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
        Connection connection = connect();
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
        Connection connection = connect();
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
        Connection connection = connect();
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
