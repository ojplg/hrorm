package org.hrorm;

import org.hrorm.examples.Columns;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class ColumnsTest {

    static { TestLogConfig.load(); }

    private static H2Helper helper = new H2Helper("columns");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    private DaoBuilder<Columns> daoBuilder(){
        return new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withLocalDateTimeColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());
    }

    @Test
    public void testInsertAndSelect(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();

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
    }

    @Test
    public void testUpdates(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime dec1 = LocalDateTime.of(2018, 12, 1, 3, 45);

        Columns columns = new Columns();
        columns.setStringThing("UpdateTest");
        columns.setIntegerThing(762L);
        columns.setBooleanThing(true);
        columns.setTimeStampThing(dec1);
        columns.setColorThing(EnumeratedColor.Green);

        dao.insert(columns);

        Columns dbInstance = dao.select(columns.getId());
        Assert.assertEquals(columns, dbInstance);

        LocalDateTime oct5 = LocalDateTime.of(2019, 10, 5, 4, 45);

        columns.setStringThing("UpdateTest New Value");
        columns.setIntegerThing(1234L);
        columns.setBooleanThing(false);
        columns.setTimeStampThing(oct5);
        columns.setColorThing(EnumeratedColor.Blue);

        dao.update(columns);

        Columns dbInstance2 = dao.select(columns.getId());
        Assert.assertEquals(columns, dbInstance2);
    }

    @Test
    public void testSelectByColumns(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime dec1 = LocalDateTime.of(2018, 12, 1, 3, 45);

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
    }

    @Test
    public void testSelectManyByColumns(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime dec1 = LocalDateTime.of(2018, 12, 1, 3, 45);

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

        LocalDateTime oct5 = LocalDateTime.of(2019, 10, 5, 4, 45);

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
    }

    @Test
    public void handlesNullElements(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        Columns columns = new Columns();

        dao.insert(columns);

        Assert.assertNotNull(columns.getId());
        Assert.assertTrue(columns.getId()>1);
    }

    @Test
    public void testDelete(){
        long itemId;
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            itemId = dao.insert(columns);
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = dao.select(itemId);
            Assert.assertNotNull(columns);
            dao.delete(columns);
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = dao.select(itemId);
            Assert.assertNull(columns);
        }
    }

    @Test
    public void testSelectByColumnsIsCaseInsensitive(){
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
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            columns.setDecimalThing(new BigDecimal(123.4));
            columns.setIntegerThing(1234L);

            Columns readFromDb = dao.selectByColumns(columns, "decimal_column", "integer_column");
            Assert.assertEquals(itemId, (long) readFromDb.getId());
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            columns.setDecimalThing(new BigDecimal(123.4));
            columns.setIntegerThing(1234L);

            Columns readFromDb = dao.selectByColumns(columns, "DECIMAL_COLUMN", "INTEGER_COLUMN");
            Assert.assertEquals(itemId, (long) readFromDb.getId());
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            columns.setDecimalThing(new BigDecimal(123.4));
            columns.setIntegerThing(1234L);

            Columns readFromDb = dao.selectByColumns(columns, "DECIMal_colUMN", "InTEGeR_COlUmn");
            Assert.assertEquals(itemId, (long) readFromDb.getId());
        }
    }

    @Test
    public void testFoldingSelect(){
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
        }
        {
            Columns template = new Columns();
            template.setStringThing("Include");

            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Long result = dao.foldingSelect(template, 0L,
                    (accumulator, columns) -> accumulator+columns.getIntegerThing(),
                    "STRING_COLUMN");

            Assert.assertEquals(2550L, (long) result);
        }
    }
}
