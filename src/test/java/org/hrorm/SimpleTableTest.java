package org.hrorm;

import lombok.Data;
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
    public static final String TestDbName = "simple_table";

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

    @Data
    static class SimpleClass {
        private Long id;
        private String field;
    }

    private DaoBuilder<SimpleClass> daoBuilder(){
        return new DaoBuilder<>("simple", SimpleClass::new)
                .withPrimaryKey("id", "simple_seq", SimpleClass::getId, SimpleClass::setId)
                .withStringColumn("field", SimpleClass::getField, SimpleClass::setField);
    }

    @Test
    public void testInsertSetsPrimaryKey(){
        Connection connection = connect();
        Dao<SimpleClass> dao = daoBuilder().buildDao(connection);

        SimpleClass simple = new SimpleClass();
        simple.setField("PrimaryKeyTest");

        dao.insert(simple);

        Assert.assertTrue(simple.getId() > 0);
    }

    @Test
    public void testInsertAndSelect(){
        Connection connection = connect();
        Dao<SimpleClass> dao = daoBuilder().buildDao(connection);

        SimpleClass simple = new SimpleClass();
        simple.setField("InsertSelectTest");

        dao.insert(simple);

        SimpleClass dbInstance = dao.select(simple.getId());

        Assert.assertEquals("InsertSelectTest", dbInstance.getField());
    }

    @Test
    public void testUpdates(){
        Connection connection = connect();
        Dao<SimpleClass> dao = daoBuilder().buildDao(connection);

        SimpleClass simple = new SimpleClass();
        simple.setField("UpdateTest");

        dao.insert(simple);

        SimpleClass dbInstance = dao.select(simple.getId());
        Assert.assertEquals("UpdateTest", dbInstance.getField());

        simple.setField("UpdateTest New Value");
        dao.update(simple);

        dbInstance = dao.select(simple.getId());
        Assert.assertEquals("UpdateTest New Value", dbInstance.getField());
    }

    @Test
    public void testSelectByColumn(){
        Connection connection = connect();
        Dao<SimpleClass> dao = daoBuilder().buildDao(connection);

        SimpleClass simple = new SimpleClass();
        simple.setField("Select By Column Test");

        dao.insert(simple);

        SimpleClass template = new SimpleClass();
        template.setField("Select By Column Test");

        SimpleClass dbInstance = dao.selectByColumns(template, "field");
        Assert.assertEquals(simple.getId(), dbInstance.getId());
    }

    @Test
    public void testDelete(){
        Connection connection = connect();
        Dao<SimpleClass> dao = daoBuilder().buildDao(connection);

        SimpleClass simple = new SimpleClass();
        simple.setField("Delete Test");

        dao.insert(simple);

        SimpleClass fromDb = dao.select(simple.getId());
        Assert.assertNotNull(fromDb);

        dao.delete(simple);

        SimpleClass fromDbAfterDelete = dao.select(simple.getId());
        Assert.assertNull(fromDbAfterDelete);
    }

    @Test
    public void testSelectAll(){
        Connection connection = connect();
        Dao<SimpleClass> dao = daoBuilder().buildDao(connection);

        SimpleClass red = new SimpleClass();
        red.setField("Red");
        dao.insert(red);

        SimpleClass blue = new SimpleClass();
        blue.setField("Blue");
        dao.insert(blue);

        SimpleClass green = new SimpleClass();
        green.setField("Green");
        dao.insert(green);

        List<SimpleClass> items = dao.selectAll();

        Assert.assertEquals(3, items.size());
    }
}
