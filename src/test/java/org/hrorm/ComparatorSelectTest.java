package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.Columns;
import org.hrorm.examples.ColumnsDaoBuilder;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hrorm.Operator.EQUALS;
import static org.hrorm.Where.where;

import static org.hrorm.Operator.LESS_THAN;
import static org.hrorm.Operator.GREATER_THAN;
import static org.hrorm.Operator.LIKE;

public class ComparatorSelectTest {

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

    @After
    public void clearTable() { helper.clearTable("columns_table");}

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

        connection.commit();
        connection.close();
    }

    @Test
    public void testSelectStringLike() throws SQLException {
        Long id;

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns columns = new Columns();
            columns.setStringThing("LIKESELECTTEST");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);
            id = dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> found = dao.select(
                    where("string_column", Operator.LIKE, "LIKE%")
            );
            Assert.assertEquals(1, found.size());
            Assert.assertEquals(id, found.get(0).getId());

            connection.close();
        }
    }

    @Test
    public void testSelectStringUnlike() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns columns = new Columns();
            columns.setStringThing("UNLIKESELECTTEST");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);
            dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {

            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> found = dao.select(
                    where("string_column", LIKE, "%FOO%")
            );
            Assert.assertEquals(0, found.size());

            connection.commit();
            connection.close();
        }
    }

    @Test
    public void testLessThanInteger() throws SQLException {
        Long id;

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns columns = new Columns();
            columns.setStringThing("Less than test");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            id = dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 1234L)
            );
            Assert.assertEquals(1, found.size());
            Assert.assertEquals(id, found.get(0).getId());

            connection.commit();
            connection.close();
        }
    }

    @Test
    public void testLessThanInteger_Fails() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns columns = new Columns();
            columns.setStringThing("Less than test fails");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 123L)
            );
            Assert.assertEquals(0, found.size());

            connection.close();
        }
    }


    @Test
    public void testLessThanDecimal() throws SQLException {
        Long id;

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns columns = new Columns();
            columns.setStringThing("Less than decimal test");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);
            id = dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> found = dao.select(
                    where("decimal_column", LESS_THAN, new BigDecimal("5.03"))
            );
            Assert.assertEquals(1, found.size());
            Assert.assertEquals(id, found.get(0).getId());

            connection.close();
        }

    }

    @Test
    public void testLessThanDecimal_Fails() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns columns = new Columns();
            columns.setStringThing("Less than decimal test fails");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> found = dao.select(
                    where("decimal_column", LESS_THAN, new BigDecimal("3.12"))
            );
            Assert.assertEquals(0, found.size());

            connection.close();
        }
    }

    @Test
    public void testWorksWithAnds() throws SQLException {
        Long id;

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns columns = new Columns();
            columns.setStringThing("BigAndTest");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            id = dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 1234L)
                      .and("decimal_column", GREATER_THAN, new BigDecimal("3.12"))
                      .and("string_column", LIKE, "%And%")
            );
            Assert.assertEquals(1, found.size());
            Assert.assertEquals(id, found.get(0).getId());

            connection.close();
        }
    }

    @Test
    public void testAnyFailedComparisonFails() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns columns = new Columns();
            columns.setStringThing("BigAndTestFails");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            // change string value
            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 1234L)
                            .and("decimal_column", GREATER_THAN, new BigDecimal("3.12"))
                            .and("string_column", LIKE, "%BADSTRING%")
            );
            Assert.assertEquals(0, found.size());

            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            // change decimal value
            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 1234L)
                            .and("decimal_column", GREATER_THAN, new BigDecimal("13.12"))
                            .and("string_column", LIKE, "%And%")
            );
            Assert.assertEquals(0, found.size());

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            // change long value
            List<Columns> found = dao.select(
                    where("integer_column", LESS_THAN, 4L)
                            .and("decimal_column", GREATER_THAN, new BigDecimal("3.12"))
                            .and("string_column", LIKE, "%And%")
            );
            Assert.assertEquals(0, found.size());

            connection.close();
        }
    }

    @Test
    public void testIntegerOpenRange() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            for(long number = 1; number<=25; number++){
                Columns columns = new Columns();
                columns.setStringThing("IntegerOpenRangeTest");
                columns.setIntegerThing(number);
                dao.insert(columns);
            }
            connection.commit();
            connection.close();

        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());

            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> filtered = dao.select(
                    where("integer_column", GREATER_THAN, 10L)
                    .and("integer_column", LESS_THAN, 20L)
            );
            Assert.assertEquals(9, filtered.size());
            connection.close();
        }
    }

    @Test
    public void testIntegerClosedRange() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            for(long number = 1; number<=25; number++){
                Columns columns = new Columns();
                columns.setStringThing("IntegerClosedRange");
                columns.setIntegerThing(number);
                dao.insert(columns);
            }

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());

            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> filtered = dao.select(
                    where("integer_column", Operator.GREATER_THAN_OR_EQUALS, 10L)
                            .and("integer_column", Operator.LESS_THAN_OR_EQUALS, 20L)
            );
            Assert.assertEquals(11, filtered.size());

            connection.close();
        }
    }

    @Test
    public void testDecimalOpenRange() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            for(long number = 1; number<=25; number++){
                Columns columns = new Columns();
                columns.setStringThing("DecimalOpenRangeTest");
                columns.setDecimalThing(new BigDecimal(String.valueOf(number)));
                dao.insert(columns);
            }

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());

            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> filtered = dao.select(where("decimal_column", Operator.GREATER_THAN, new BigDecimal("5"))
                                                    .and("decimal_column", Operator.LESS_THAN, new BigDecimal("18")));
            Assert.assertEquals(12, filtered.size());

            connection.close();
        }

    }

    @Test
    public void testDecimalClosedRange() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            for(long number = 1; number<=25; number++){
                Columns columns = new Columns();
                columns.setStringThing("DecimalClosed");
                columns.setDecimalThing(new BigDecimal(String.valueOf(number) + ".123"));
                dao.insert(columns);
            }

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());

            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> filtered = dao.select(where("decimal_column", Operator.GREATER_THAN_OR_EQUALS, new BigDecimal("5.123"))
                    .and("decimal_column", Operator.LESS_THAN_OR_EQUALS, new BigDecimal("7.123")));
            Assert.assertEquals(3, filtered.size());

            connection.close();
        }
    }

    @Test
    public void testDateOpenRange() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(int number = 1; number<=25; number++){
                Instant dateTime = LocalDateTime.of(2018, 3, number, 10, 30).toInstant(ZoneOffset.UTC);
                Columns columns = new Columns();
                columns.setStringThing("DateOpenRangeTest");
                columns.setTimeStampThing(dateTime);
                dao.insert(columns);
            }
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Instant start = (LocalDateTime.of(2018, 3, 7, 9, 30)).toInstant(ZoneOffset.UTC);
            Instant end = (LocalDateTime.of(2018, 3, 22, 5, 5)).toInstant(ZoneOffset.UTC);

            List<Columns> filtered = dao.select(where("timestamp_column", Operator.GREATER_THAN, start)
                    .and("timestamp_column", Operator.LESS_THAN, end));
            Assert.assertEquals(15, filtered.size());
            connection.close();
        }
    }

    @Test
    public void testDateClosedRange() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            for(int number = 1; number<=25; number++){
                Instant dateTime = LocalDateTime.of(2018, 3, number, 10, 30).toInstant(ZoneOffset.UTC);
                Columns columns = new Columns();
                columns.setStringThing("DateOpenRangeTest");
                columns.setTimeStampThing(dateTime);
                dao.insert(columns);
            }
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> all = dao.selectAll();
            Assert.assertEquals(25, all.size());
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Instant start = (LocalDateTime.of(2018, 3, 7, 10, 30)).toInstant(ZoneOffset.UTC);
            Instant end = (LocalDateTime.of(2018, 3, 22, 10, 30)).toInstant(ZoneOffset.UTC);

            List<Columns> filtered = dao.select(
                    where("timestamp_column", Operator.GREATER_THAN_OR_EQUALS, start)
                     .and("timestamp_column", Operator.LESS_THAN_OR_EQUALS, end));
            Assert.assertEquals(16, filtered.size());
            connection.close();
        }
    }

    @Test
    public void testFluentSelectNesting() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
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
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
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

            connection.close();

        }

    }

    @Test
    public void testSelectNotEquals() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
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
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> found =
                    dao.select(where("integer_column", Operator.NOT_EQUALS, 67L));

            Assert.assertEquals(99, found.size());

            Set<Long> foundIntegerSet = found.stream().map(Columns::getIntegerThing).collect(Collectors.toSet());
            Assert.assertFalse(foundIntegerSet.contains(67L));

            connection.close();
        }

    }

    @Test
    public void testSelectNotLike() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
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

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> found =
                    dao.select(where("string_column", Operator.NOT_LIKE, "%67%"));

            Assert.assertEquals(99, found.size());

            Set<Long> foundIntegerSet = found.stream().map(Columns::getIntegerThing).collect(Collectors.toSet());
            Assert.assertFalse(foundIntegerSet.contains(67L));

            connection.close();
        }
    }

    @Test
    public void testSelectIsNull() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
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
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> found =
                    dao.select(Where.isNull("string_column"));

            Assert.assertEquals(67, found.size());

            connection.close();

        }
    }

    @Test
    public void testSelectIsNullWithAndClause() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            for(long idx = 1; idx<=100; idx++) {
                Columns columns = new Columns();
                columns.setStringThing(idx%3 == 0 ? "SelectIsNullWithAndClause" + idx : null);
                columns.setIntegerThing(idx);
                columns.setBooleanThing(true);
                columns.setDecimalThing(idx%5==0 ? new BigDecimal("5.0") : new BigDecimal("4.321"));
                columns.setTimeStampThing(time);
                columns.setColorThing( EnumeratedColor.Green);

                dao.insert(columns);
            }
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> found =
                    dao.select(where("integer_column", GREATER_THAN, 90L)
                                    .and(Where.isNull("string_column")));

            List<Long> expectedIntegers = Arrays.asList(91L, 92L, 94L, 95L, 97L, 98L, 100L);

            Set<Long> foundIntegerSet = found.stream().map(Columns::getIntegerThing).collect(Collectors.toSet());

            Assert.assertEquals(expectedIntegers.size(), foundIntegerSet.size());
            Assert.assertTrue(foundIntegerSet.containsAll(expectedIntegers));
            connection.close();

        }
    }


    @Test
    public void testSelectIsNotNull() throws SQLException {

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
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
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> found =
                    dao.select(Where.isNotNull("string_column"));

            Assert.assertEquals(33, found.size());

            connection.close();

        }
    }

    @Test
    public void testSelectWithOrSubclause() throws SQLException {

        Instant runTime = Instant.now();
        Instant iteratedTime = runTime;
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
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
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Instant startTime = runTime.plus(72, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS);
            Instant endTime = runTime.plus(83, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
            List<Columns> found = dao.select(where("string_column",LIKE,"%7")
                                                .or(where("timestamp_column", GREATER_THAN, startTime)
                                                    .and("timestamp_column", LESS_THAN, endTime)));

            List<Long> expectedIntegers = Arrays.asList(7L, 17L, 27L, 37L, 47L, 57L, 67L, 72L, 73L,
                                                        74L, 75L, 76L, 77L, 78L, 79L, 80L, 81L, 82L, 83L,
                                                        87L, 97L);

            Set<Long> foundIntegerSet = found.stream().map(Columns::getIntegerThing).collect(Collectors.toSet());

            Assert.assertTrue(foundIntegerSet.containsAll(expectedIntegers));
            Assert.assertEquals(expectedIntegers.size(), foundIntegerSet.size());

            connection.close();

        }
    }

    @Test
    public void testCapitalizationNotAProblem() throws SQLException {
        Long id;

        Instant time = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            Columns columns = new Columns();
            columns.setStringThing("Less than test");
            columns.setIntegerThing(762L);
            columns.setBooleanThing(true);
            columns.setDecimalThing(new BigDecimal("4.567"));
            columns.setTimeStampThing(time);
            columns.setColorThing(EnumeratedColor.Red);

            id = dao.insert(columns);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> found = dao.select(
                    where("INTEGer_cOLUmn", LESS_THAN, 1234L)
            );
            Assert.assertEquals(1, found.size());
            Assert.assertEquals(id, found.get(0).getId());

            connection.close();

        }

    }

    @Test
    public void testStaticOrConstruction() {
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(long idx=1; idx<=10; idx++){
                Columns columns = new Columns();
                columns.setIntegerThing(idx);
                dao.insert(columns);
            }
        });
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Where> wheres = new ArrayList<>();
            for(long idx=2; idx<=10; idx+=2){
                wheres.add(new Where("integer_column", EQUALS, idx));
            }
            List<Columns> columns = dao.select(Where.or(wheres));
            AssertHelp.containsAllItems(new Long[]{ 2L, 4L, 6L, 8L, 10L }, columns, c -> c.getIntegerThing());
        });
    }

    @Test
    public void testStaticAndConstruction() {
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(long idx=1; idx<=100; idx++){
                Columns columns = new Columns();
                columns.setIntegerThing(idx);
                columns.setStringThing(Long.toString(idx));
                columns.setBooleanThing(idx % 3 == 0);
                dao.insert(columns);
            }
        });
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<Where> wheres = new ArrayList<>();
            wheres.add(where("integer_column", GREATER_THAN, 33L));
            wheres.add(where("boolean_column", EQUALS, true));
            wheres.add(where("string_column", LIKE, "%7%"));
            List<Columns> columns = dao.select(Where.and(wheres));
            Long[] expectedInts = new Long[]{57L, 72L, 75L, 78L, 87L};
            AssertHelp.containsAllItems(expectedInts, columns, Columns::getIntegerThing);
        });
    }
}
