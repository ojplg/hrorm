package org.hrorm;

import org.hrorm.examples.Columns;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparatorSelectTest {

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

    @After
    public void clearTable() { helper.clearTable("columns_table");}

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
    public void testSelectStringLike(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        Long id;

        LocalDateTime time = LocalDateTime.now();
        {
            Columns columns = new Columns();
            columns.setStringThing("LIKESELECTTEST");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);
            id = dao.insert(columns);
        }
        {
            Columns template = new Columns();
            template.setStringThing("LIKE%");

            Map<String, Operator> map = new HashMap<>();
            map.put("string_column", Operator.LIKE);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(1, found.size());
            Assert.assertEquals(id, found.get(0).getId());
        }
    }

    @Test
    public void testSelectStringUnlike(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();
        {
            Columns columns = new Columns();
            columns.setStringThing("UNLIKESELECTTEST");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);
            dao.insert(columns);
        }
        {
            Columns template = new Columns();
            template.setStringThing("FOOBAR");

            Map<String, Operator> map = new HashMap<>();
            map.put("string_column", Operator.LIKE);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(0, found.size());
        }
    }

    @Test
    public void testLessThanInteger(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        Long id;

        LocalDateTime time = LocalDateTime.now();
        {
            Columns columns = new Columns();
            columns.setStringThing("Less than test");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            id = dao.insert(columns);
        }
        {
            Columns template = new Columns();
            template.setIntegerThing(1234L);

            Map<String, Operator> map = new HashMap<>();
            map.put("integer_column", Operator.LESS_THAN);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(1, found.size());
            Assert.assertEquals(id, found.get(0).getId());
        }

    }

    @Test
    public void testLessThanInteger_Fails(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();
        {
            Columns columns = new Columns();
            columns.setStringThing("Less than test fails");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            dao.insert(columns);
        }
        {
            Columns template = new Columns();
            template.setIntegerThing(123L);

            Map<String, Operator> map = new HashMap<>();
            map.put("integer_column", Operator.LESS_THAN);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(0, found.size());
        }

    }


    @Test
    public void testLessThanDecimal(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        Long id;

        LocalDateTime time = LocalDateTime.now();
        {
            Columns columns = new Columns();
            columns.setStringThing("Less than decimal test");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);
            id = dao.insert(columns);
        }
        {
            Columns template = new Columns();
            template.setDecimalThing(new BigDecimal("5.03"));

            Map<String, Operator> map = new HashMap<>();
            map.put("decimal_column", Operator.LESS_THAN);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(1, found.size());
            Assert.assertEquals(id, found.get(0).getId());
        }

    }

    @Test
    public void testLessThanDecimal_Fails(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();
        {
            Columns columns = new Columns();
            columns.setStringThing("Less than decimal test fails");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            dao.insert(columns);
        }
        {
            Columns template = new Columns();
            template.setDecimalThing(new BigDecimal("3.12"));

            Map<String, Operator> map = new HashMap<>();
            map.put("integer_column", Operator.LESS_THAN);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(0, found.size());
        }

    }

    @Test
    public void testWorksWithAnds(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        Long id;

        LocalDateTime time = LocalDateTime.now();
        {
            Columns columns = new Columns();
            columns.setStringThing("BigAndTest");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            id = dao.insert(columns);
        }
        {
            Columns template = new Columns();
            template.setDecimalThing(new BigDecimal("3.12"));
            template.setStringThing("%And%");
            template.setIntegerThing(1234L);

            Map<String, Operator> map = new HashMap<>();
            map.put("integer_column", Operator.LESS_THAN);
            map.put("decimal_column", Operator.GREATER_THAN);
            map.put("string_column", Operator.LIKE);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(1, found.size());
            Assert.assertEquals(id, found.get(0).getId());
        }


    }

    @Test
    public void testAnyFailedComparisonFails(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();
        {
            Columns columns = new Columns();
            columns.setStringThing("BigAndTestFails");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            dao.insert(columns);
        }
        {
            Columns template = new Columns();
            template.setDecimalThing(new BigDecimal("3.12"));
            template.setStringThing("%Foo%");
            template.setIntegerThing(1234L);

            Map<String, Operator> map = new HashMap<>();
            map.put("integer_column", Operator.LESS_THAN);
            map.put("decimal_column", Operator.GREATER_THAN);
            map.put("string_column", Operator.LIKE);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(0, found.size());
        }
        {
            Columns template = new Columns();
            template.setDecimalThing(new BigDecimal("6.212"));
            template.setStringThing("%And%");
            template.setIntegerThing(1234L);

            Map<String, Operator> map = new HashMap<>();
            map.put("integer_column", Operator.LESS_THAN);
            map.put("decimal_column", Operator.GREATER_THAN);
            map.put("string_column", Operator.LIKE);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(0, found.size());
        }
        {
            Columns template = new Columns();
            template.setDecimalThing(new BigDecimal("3.12"));
            template.setStringThing("%And%");
            template.setIntegerThing(3L);

            Map<String, Operator> map = new HashMap<>();
            map.put("integer_column", Operator.LESS_THAN);
            map.put("decimal_column", Operator.GREATER_THAN);
            map.put("string_column", Operator.LIKE);

            List<Columns> found = dao.selectManyByColumns(template, map);
            Assert.assertEquals(0, found.size());
        }
    }

    @Test
    public void testIntegerOpenRange(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        {
            for(long number = 1; number<=25; number++){
                Columns columns = new Columns();
                columns.setStringThing("IntegerOpenRangeTest");
                columns.setIntegerThing(number);
                dao.insert(columns);
            }
        }
        {
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());
        }
        {
            Columns template = new Columns();
            template.setIntegerThing(10L);
            Operator rangeLimit = Operator.openRangeTo(20L);
            Map<String, Operator> columnMap = Collections.singletonMap("integer_column", rangeLimit);

            List<Columns> filtered = dao.selectManyByColumns(template, columnMap);
            Assert.assertEquals(9, filtered.size());
        }
    }

    @Test
    public void testIntegerClosedRange(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        {
            for(long number = 1; number<=25; number++){
                Columns columns = new Columns();
                columns.setStringThing("IntegerClosedRange");
                columns.setIntegerThing(number);
                dao.insert(columns);
            }
        }
        {
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());
        }
        {
            Columns template = new Columns();
            template.setIntegerThing(10L);
            Operator rangeLimit = Operator.closedRangeTo(20L);
            Map<String, Operator> columnMap = Collections.singletonMap("integer_column", rangeLimit);

            List<Columns> filtered = dao.selectManyByColumns(template, columnMap);
            Assert.assertEquals(11, filtered.size());
        }
    }

    @Test
    public void testDecimalOpenRange(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        {
            for(long number = 1; number<=25; number++){
                Columns columns = new Columns();
                columns.setStringThing("DecimalOpenRangeTest");
                columns.setDecimalThing(new BigDecimal(String.valueOf(number)));
                dao.insert(columns);
            }
        }
        {
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());
        }
        {
            Columns template = new Columns();
            template.setDecimalThing(new BigDecimal("5"));
            Operator rangeLimit = Operator.openRangeTo(new BigDecimal("18"));
            Map<String, Operator> columnMap = Collections.singletonMap("decimal_column", rangeLimit);

            List<Columns> filtered = dao.selectManyByColumns(template, columnMap);
            Assert.assertEquals(12, filtered.size());
        }
    }

    @Test
    public void testDecimalClosedRange(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        {
            for(long number = 1; number<=25; number++){
                Columns columns = new Columns();
                columns.setStringThing("DecimalClosed");
                columns.setDecimalThing(new BigDecimal(String.valueOf(number) + ".123"));
                dao.insert(columns);
            }
        }
        {
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());
        }
        {
            Columns template = new Columns();
            template.setDecimalThing(new BigDecimal("5.123"));
            Operator rangeLimit = Operator.closedRangeTo(new BigDecimal("7.123"));
            Map<String, Operator> columnMap = Collections.singletonMap("decimal_column", rangeLimit);

            List<Columns> filtered = dao.selectManyByColumns(template, columnMap);
            Assert.assertEquals(3, filtered.size());
        }
    }

    @Test
    public void testDateOpenRange(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        {
            for(int number = 1; number<=25; number++){
                LocalDateTime dateTime = LocalDateTime.of(2018, 3, number, 10, 30);
                Columns columns = new Columns();
                columns.setStringThing("DateOpenRangeTest");
                columns.setTimeStampThing(dateTime);
                dao.insert(columns);
            }
        }
        {
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());
        }
        {
            Columns template = new Columns();
            template.setTimeStampThing(LocalDateTime.of(2018, 3, 7, 9, 30));
            Operator rangeLimit = Operator.openRangeTo(LocalDateTime.of(2018, 3, 22, 5, 5));
            Map<String, Operator> columnMap = Collections.singletonMap("timestamp_column", rangeLimit);

            List<Columns> filtered = dao.selectManyByColumns(template, columnMap);
            Assert.assertEquals(15, filtered.size());
        }
    }

    @Test
    public void testDateClosedRange(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        {
            for(int number = 1; number<=25; number++){
                LocalDateTime dateTime = LocalDateTime.of(2018, 3, number, 10, 30);
                Columns columns = new Columns();
                columns.setStringThing("DateOpenRangeTest");
                columns.setTimeStampThing(dateTime);
                dao.insert(columns);
            }
        }
        {
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());
        }
        {
            Columns template = new Columns();
            template.setTimeStampThing(LocalDateTime.of(2018, 3, 7, 10, 30));
            Operator rangeLimit = Operator.closedRangeTo(LocalDateTime.of(2018, 3, 22, 10, 30));
            Map<String, Operator> columnMap = Collections.singletonMap("timestamp_column", rangeLimit);

            List<Columns> filtered = dao.selectManyByColumns(template, columnMap);
            Assert.assertEquals(16, filtered.size());
        }
    }


}
