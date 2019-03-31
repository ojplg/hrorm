package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.Columns;
import org.hrorm.examples.ColumnsDaoBuilder;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ColumnsTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("columns");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    private DaoBuilder<Columns> daoBuilder(){
        return ColumnsDaoBuilder.DAO_BUILDER;
    }

    @Test
    public void testInsertAndSelect() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        Instant time = Instant.now();

        Columns columns = new Columns();
        columns.setStringThing("InsertSelectTest");
        columns.setIntegerThing(762L);
        columns.setBooleanThing(true);
        columns.setDecimalThing(new BigDecimal("4.567"));
        columns.setTimeStampThing(time);
        columns.setColorThing(EnumeratedColor.Red);

        Long id = dao.insert(columns);

        Columns dbInstance = dao.select(id);

        Assert.assertEquals(columns, dbInstance);
        Assert.assertNotNull(columns.getId());
        Assert.assertEquals(id, columns.getId());

        connection.close();
    }

    @Test
    public void testNulls() throws SQLException {
        Long id;
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            id = dao.insert(columns);
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = dao.select(id);

            Assert.assertNull(columns.getIntegerThing());
            Assert.assertNull(columns.getBooleanThing());
            Assert.assertNull(columns.getTimeStampThing());
            Assert.assertNull(columns.getColorThing());
            Assert.assertNull(columns.getStringThing());
            Assert.assertNull(columns.getDecimalThing());

            connection.close();
        }
    }

    @Test
    public void testUpdates() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        Instant dec1 = LocalDateTime.of(2018, 12, 1, 3, 45).toInstant(ZoneOffset.UTC);

        Columns columns = new Columns();
        columns.setStringThing("UpdateTest");
        columns.setIntegerThing(762L);
        columns.setBooleanThing(true);
        columns.setTimeStampThing(dec1);
        columns.setColorThing(EnumeratedColor.Green);

        dao.insert(columns);

        Columns dbInstance = dao.select(columns.getId());
        Assert.assertEquals(columns, dbInstance);

        Instant oct5 = LocalDateTime.of(2019, 10, 5, 4, 45).toInstant(ZoneOffset.UTC);

        columns.setStringThing("UpdateTest New Value");
        columns.setIntegerThing(1234L);
        columns.setBooleanThing(false);
        columns.setTimeStampThing(oct5);
        columns.setColorThing(EnumeratedColor.Blue);

        dao.update(columns);

        Columns dbInstance2 = dao.select(columns.getId());
        Assert.assertEquals(columns, dbInstance2);

        connection.close();
    }

    @Test
    public void testSelectByColumns() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        Instant dec1 = LocalDateTime.of(2018, 12, 1, 3, 45).toInstant(ZoneOffset.UTC);

        Columns columns = new Columns();
        columns.setStringThing("Select By Column Test");
        columns.setIntegerThing(762L);
        columns.setBooleanThing(true);
        columns.setTimeStampThing(dec1);
        columns.setColorThing(EnumeratedColor.Green);

        dao.insert(columns);

        Columns template = new Columns();
        template.setStringThing("Select By Column Test");
        template.setIntegerThing(762L);
        template.setBooleanThing(true);
        template.setTimeStampThing(dec1);
        template.setColorThing(EnumeratedColor.Green);

        Columns dbInstance = dao.selectByColumns(template, "string_column", "integer_column", "boolean_column", "timestamp_column", "color_column");
        Assert.assertEquals(columns.getId(), dbInstance.getId());

        connection.close();
    }

    @Test
    public void testSelectManyByColumns() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        Instant dec1 = LocalDateTime.of(2018, 12, 1, 3, 45).toInstant(ZoneOffset.UTC);

        Columns columns1 = new Columns();
        columns1.setStringThing("Select Many By Column Test");
        columns1.setIntegerThing(762L);
        columns1.setBooleanThing(true);
        columns1.setTimeStampThing(dec1);
        columns1.setColorThing(EnumeratedColor.Green);

        dao.insert(columns1);

        Columns columns2 = new Columns();
        columns2.setStringThing("Select Many By Column Test");
        columns2.setIntegerThing(-1234L);
        columns2.setBooleanThing(false);
        columns2.setTimeStampThing(dec1);
        columns2.setColorThing(EnumeratedColor.Red);

        dao.insert(columns2);

        Columns columns3 = new Columns();
        columns3.setStringThing("Select Many By Column Test");
        columns3.setIntegerThing(0L);
        columns3.setBooleanThing(false);
        columns3.setTimeStampThing(dec1);
        columns3.setColorThing(EnumeratedColor.Blue);

        dao.insert(columns3);

        Instant oct5 = LocalDateTime.of(2019, 10, 5, 4, 45).toInstant(ZoneOffset.UTC);

        Columns columns4 = new Columns();
        columns4.setStringThing("Select Many By Column Test");
        columns4.setIntegerThing(0L);
        columns4.setBooleanThing(false);
        columns4.setTimeStampThing(oct5);
        columns4.setColorThing(EnumeratedColor.Blue);

        dao.insert(columns4);

        Columns template = new Columns();
        template.setStringThing("Select Many By Column Test");
        template.setTimeStampThing(dec1);

        List<Columns> dbInstanceList = dao.selectManyByColumns(template, "string_column", "timestamp_column");
        Assert.assertEquals(3, dbInstanceList.size());

        connection.close();
    }

    @Test
    public void handlesNullElements() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        Columns columns = new Columns();

        dao.insert(columns);

        Assert.assertNotNull(columns.getId());
        Assert.assertTrue(columns.getId()>1);

        connection.close();
    }

    @Test
    public void testDelete() throws SQLException {
        long itemId;
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            itemId = dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = dao.select(itemId);
            Assert.assertNotNull(columns);
            dao.delete(columns);

            connection.commit();
            connection.close();}
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = dao.select(itemId);
            Assert.assertNull(columns);

            connection.close();}
    }

    @Test
    public void testSelectByColumnsIsCaseInsensitive() throws SQLException {
        long itemId;
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            columns.setDecimalThing(new BigDecimal(123.4));
            columns.setIntegerThing(1234L);
            columns.setStringThing("CASE INSENSITIVE");
            columns.setBooleanThing(true);
            itemId = dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            columns.setDecimalThing(new BigDecimal(123.4));
            columns.setIntegerThing(1234L);

            Columns readFromDb = dao.selectByColumns(columns, "decimal_column", "integer_column");
            Assert.assertEquals(itemId, (long) readFromDb.getId());

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            columns.setDecimalThing(new BigDecimal(123.4));
            columns.setIntegerThing(1234L);

            Columns readFromDb = dao.selectByColumns(columns, "DECIMAL_COLUMN", "INTEGER_COLUMN");
            Assert.assertEquals(itemId, (long) readFromDb.getId());

            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            columns.setDecimalThing(new BigDecimal(123.4));
            columns.setIntegerThing(1234L);

            Columns readFromDb = dao.selectByColumns(columns, "DECIMal_colUMN", "InTEGeR_COlUmn");
            Assert.assertEquals(itemId, (long) readFromDb.getId());

            connection.close();
        }
    }

    @Test
    public void testFoldingSelect() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for (long idx=1; idx<=100; idx++) {
                Columns c = new Columns();
                c.setIntegerThing(idx);
                String label = idx % 2 == 0 ? "Include" : "Exclude";
                c.setStringThing(label);
                dao.insert(c);
            }

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Long result = dao.foldingSelect(0L,
                    (accumulator, columns) -> accumulator+columns.getIntegerThing(),
                    Where.where("string_column", Operator.EQUALS, "Include"));

            Assert.assertEquals(2550L, (long) result);

            connection.close();
        }
    }

    @Test
    public void testSelectMany() throws SQLException {
        List<Long> ids = new ArrayList<>();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for (long idx=1; idx<=100; idx++) {
                Columns c = new Columns();
                c.setIntegerThing(idx);
                c.setStringThing("Select Many " + idx);
                long id = dao.insert(c);
                ids.add(id);
            }

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> found = dao.selectMany(ids);
            Assert.assertEquals(100, found.size());

            connection.close();
        }
    }

    @Test
    public void testAtomicOperations() throws SQLException {
        long id;
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns c = new Columns();
            c.setIntegerThing(34589L);
            c.setStringThing("Test Atomic Operations");
            id = dao.atomicInsert(c);
        }
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());
            Columns found = dao.select(id);
            Assert.assertEquals("Test Atomic Operations", found.getStringThing());
            found.setStringThing("Updated Atomic Operations Test");
            dao.atomicUpdate(found);

        }
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());
            Columns found = dao.select(id);
            Assert.assertEquals("Updated Atomic Operations Test", found.getStringThing());
            dao.atomicDelete(found);
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns found = dao.select(id);
            Assert.assertNull(found);
            connection.close();
        }
    }

    @Test
    public void testQueries() throws SQLException{
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        Queries queries = dao.queries();

        Assert.assertTrue(queries.select().toUpperCase().contains("SELECT"));
        Assert.assertTrue(queries.update().toUpperCase().contains("UPDATE"));
        Assert.assertTrue(queries.insert().toUpperCase().contains("INSERT"));
        Assert.assertTrue(queries.delete().toUpperCase().contains("DELETE"));

        connection.close();
    }
}
