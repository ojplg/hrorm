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
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hrorm.Where.where;

import static org.hrorm.Operator.LESS_THAN;
import static org.hrorm.Operator.GREATER_THAN;
import static org.hrorm.Operator.LIKE;

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
            List<Columns> found = dao.select(
                    where("string_column", Operator.LIKE, "LIKE%")
            );
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
            List<Columns> found = dao.select(
                    where("string_column", LIKE, "%FOO%")
            );
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
            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 1234L)
            );
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
            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 123L)
            );
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
            List<Columns> found = dao.select(
                    where("decimal_column", LESS_THAN, new BigDecimal("5.03"))
            );
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
            List<Columns> found = dao.select(
                    where("decimal_column", LESS_THAN, new BigDecimal("3.12"))
            );
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
            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 1234L)
                      .and("decimal_column", GREATER_THAN, new BigDecimal("3.12"))
                      .and("string_column", LIKE, "%And%")
            );
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
            // change string value
            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 1234L)
                            .and("decimal_column", GREATER_THAN, new BigDecimal("3.12"))
                            .and("string_column", LIKE, "%BADSTRING%")
            );
            Assert.assertEquals(0, found.size());
        }
        {
            // change decimal value
            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 1234L)
                            .and("decimal_column", GREATER_THAN, new BigDecimal("13.12"))
                            .and("string_column", LIKE, "%And%")
            );
            Assert.assertEquals(0, found.size());
        }
        {
            // change long value
            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 4L)
                            .and("decimal_column", GREATER_THAN, new BigDecimal("3.12"))
                            .and("string_column", LIKE, "%And%")
            );
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
            List<Columns> filtered = dao.select(
                    where("integer_column", GREATER_THAN, 10L)
                    .and("integer_column", LESS_THAN, 20L)
            );
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
            List<Columns> filtered = dao.select(
                    where("integer_column", Operator.GREATER_THAN_OR_EQUALS, 10L)
                            .and("integer_column", Operator.LESS_THAN_OR_EQUALS, 20L)
            );
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
            List<Columns> filtered = dao.select(where("decimal_column", Operator.GREATER_THAN, new BigDecimal("5"))
                                                    .and("decimal_column", Operator.LESS_THAN, new BigDecimal("18")));
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
            List<Columns> filtered = dao.select(where("decimal_column", Operator.GREATER_THAN_OR_EQUALS, new BigDecimal("5.123"))
                    .and("decimal_column", Operator.LESS_THAN_OR_EQUALS, new BigDecimal("7.123")));
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
            List<Columns> filtered = dao.select(where("timestamp_column", Operator.GREATER_THAN, LocalDateTime.of(2018, 3, 7, 9, 30))
                    .and("timestamp_column", Operator.LESS_THAN, LocalDateTime.of(2018, 3, 22, 5, 5)));
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
            List<Columns> filtered = dao.select(
                    where("timestamp_column", Operator.GREATER_THAN_OR_EQUALS, LocalDateTime.of(2018, 3, 7, 10, 30))
                    .and("timestamp_column", Operator.LESS_THAN_OR_EQUALS, LocalDateTime.of(2018, 3, 22, 10, 30)));
            Assert.assertEquals(16, filtered.size());
        }
    }

    @Test
    public void testFluentSelectNesting(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();
        {
            for(long idx = 1; idx<100; idx++) {
                Columns columns = new Columns();
                columns.setStringThing("FluentSelectTest2_" + idx + (idx%3==0 ? "THREE" : "NOPE"));
                columns.setIntegerThing(idx);
                columns.setBooleanThing(true);
                columns.setDecimalThing(idx%5==0 ? new BigDecimal("5.0") : new BigDecimal("4.321"));
                columns.setTimeStampThing(time);
                columns.setColorThing( EnumeratedColor.Green);

                dao.insert(columns);
            }
        }
        {
            List<Columns> found =
                    dao.select(where("integer_column", Operator.GREATER_THAN, 67L)
                            .and("integer_column", Operator.LESS_THAN, 84L)
                            .and(
                                    where("string_column", Operator.LIKE, "%THREE%")
                                    .or("decimal_column", Operator.EQUALS, new BigDecimal("5.0"))
                            ));

            List<Long> expectedIntegers = Arrays.asList(69L, 70L, 72L, 75L, 78L, 80L, 81L);

            Set<Long> foundIntegerSet = found.stream().map(Columns::getIntegerThing).collect(Collectors.toSet());

            Assert.assertTrue(foundIntegerSet.containsAll(expectedIntegers));
        }

    }

    @Test
    public void testSelectNotEquals(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();
        {
            for(long idx = 1; idx<=100; idx++) {
                Columns columns = new Columns();
                columns.setStringThing("FluentSelectTest2_" + idx + (idx%3==0 ? "THREE" : "NOPE"));
                columns.setIntegerThing(idx);
                columns.setBooleanThing(true);
                columns.setDecimalThing(idx%5==0 ? new BigDecimal("5.0") : new BigDecimal("4.321"));
                columns.setTimeStampThing(time);
                columns.setColorThing( EnumeratedColor.Green);

                dao.insert(columns);
            }
        }
        {
            List<Columns> found =
                    dao.select(where("integer_column", Operator.NOT_EQUALS, 67L));

            Assert.assertEquals(99, found.size());

            Set<Long> foundIntegerSet = found.stream().map(Columns::getIntegerThing).collect(Collectors.toSet());
            Assert.assertFalse(foundIntegerSet.contains(67L));
        }

    }

    @Test
    public void testSelectNotLike(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();
        {
            for(long idx = 1; idx<=100; idx++) {
                Columns columns = new Columns();
                columns.setStringThing("SelectNotLike" + idx);
                columns.setIntegerThing(idx);
                columns.setBooleanThing(true);
                columns.setDecimalThing(idx%5==0 ? new BigDecimal("5.0") : new BigDecimal("4.321"));
                columns.setTimeStampThing(time);
                columns.setColorThing( EnumeratedColor.Green);

                dao.insert(columns);
            }
        }
        {
            List<Columns> found =
                    dao.select(where("string_column", Operator.NOT_LIKE, "%67%"));

            Assert.assertEquals(99, found.size());

            Set<Long> foundIntegerSet = found.stream().map(Columns::getIntegerThing).collect(Collectors.toSet());
            Assert.assertFalse(foundIntegerSet.contains(67L));
        }
    }

    @Test
    public void testSelectIsNull(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();
        {
            for(long idx = 1; idx<=100; idx++) {
                Columns columns = new Columns();
                columns.setStringThing(idx%3 == 0 ? "SelectIsNull" + idx : null);
                columns.setIntegerThing(idx);
                columns.setBooleanThing(true);
                columns.setDecimalThing(idx%5==0 ? new BigDecimal("5.0") : new BigDecimal("4.321"));
                columns.setTimeStampThing(time);
                columns.setColorThing( EnumeratedColor.Green);

                dao.insert(columns);
            }
        }
        {
            List<Columns> found =
                    dao.select(Where.isNull("string_column"));

            Assert.assertEquals(67, found.size());
        }
    }

    @Test
    public void testSelectIsNotNull(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();
        {
            for(long idx = 1; idx<=100; idx++) {
                Columns columns = new Columns();
                columns.setStringThing(idx%3 == 0 ? "SelectIsNotNull" + idx : null);
                columns.setIntegerThing(idx);
                columns.setBooleanThing(true);
                columns.setDecimalThing(idx%5==0 ? new BigDecimal("5.0") : new BigDecimal("4.321"));
                columns.setTimeStampThing(time);
                columns.setColorThing( EnumeratedColor.Green);

                dao.insert(columns);
            }
        }
        {
            List<Columns> found =
                    dao.select(Where.isNotNull("string_column"));

            Assert.assertEquals(33, found.size());
        }
    }

    @Test
    public void testSelectWithOrSubclause(){
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime runTime = LocalDateTime.now();
        LocalDateTime iteratedTime = runTime;
        {
            for(long idx = 1; idx<=100; idx++) {
                Columns columns = new Columns();
                columns.setStringThing("SelectOrSubClause" + idx);
                columns.setIntegerThing(idx);
                columns.setBooleanThing(true);
                columns.setDecimalThing(idx%5==0 ? new BigDecimal("5.0") : new BigDecimal("4.321"));
                iteratedTime = iteratedTime.plus(1, ChronoUnit.DAYS);
                columns.setTimeStampThing(iteratedTime);
                columns.setColorThing(EnumeratedColor.Green);

                dao.insert(columns);
            }
        }
        {
            LocalDateTime startTime = runTime.plus(72, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS);
            LocalDateTime endTime = runTime.plus(83, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
            List<Columns> found = dao.select(where("string_column",LIKE,"%7")
                                                .or(where("timestamp_column", GREATER_THAN, startTime)
                                                    .and("timestamp_column", LESS_THAN, endTime)));

            List<Long> expectedIntegers = Arrays.asList(7L, 17L, 27L, 37L, 47L, 57L, 67L, 72L, 73L,
                                                        74L, 75L, 76L, 77L, 78L, 79L, 80L, 81L, 82L, 83L,
                                                        87L, 97L);

            Set<Long> foundIntegerSet = found.stream().map(Columns::getIntegerThing).collect(Collectors.toSet());

            Assert.assertTrue(foundIntegerSet.containsAll(expectedIntegers));
            Assert.assertEquals(expectedIntegers.size(), foundIntegerSet.size());
        }
    }
}
