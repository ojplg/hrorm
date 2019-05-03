package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.Columns;
import org.hrorm.examples.ColumnsDaoBuilder;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.hrorm.Where.where;
import static org.hrorm.Operator.GREATER_THAN;

public class SelectDistinctTest {

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
    public void clearTable() { helper.clearTables(); }

    private DaoBuilder<Columns> daoBuilder(){
        return ColumnsDaoBuilder.DAO_BUILDER;
    }

    @Test
    public void testDistinctLongs(){
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(long idx=0; idx<20; idx++){
                Columns columns = new Columns();

                columns.setIntegerThing(idx % 4);

                dao.insert(columns);
            }
        });
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            long count = dao.runLongFunction(SqlFunction.COUNT, "integer_column", new Where());
            Assert.assertEquals(20, count);
            List<Long> values = dao.selectDistinct("integer_column", new Where());
            AssertHelp.sameContents(Arrays.asList(0L, 1L, 2L , 3L), values);
        });
    }

    @Test
    public void testDistinctStrings(){
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(long idx=0; idx<20; idx++){
                Columns columns = new Columns();

                columns.setStringThing(String.valueOf(idx % 4));
                columns.setIntegerThing(idx);

                dao.insert(columns);
            }
        });
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<String> values = dao.selectDistinct( "string_column",new Where());
            AssertHelp.sameContents(Arrays.asList("0", "1", "2", "3"), values);
            long count = dao.runLongFunction(SqlFunction.COUNT, "integer_column", new Where());
            Assert.assertEquals(20, count);
        });
    }

    @Test
    public void testDistinctStringsWithWhere(){
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(long idx=0; idx<20; idx++){
                Columns columns = new Columns();

                columns.setStringThing(String.valueOf(idx));
                columns.setIntegerThing(idx);

                dao.insert(columns);
            }
        });
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<String> values = dao.selectDistinct( "string_column", where("integer_column", GREATER_THAN, 12L));
            AssertHelp.sameContents(Arrays.asList("13", "14", "15", "16", "17", "18", "19"), values);
        });
    }


    @Test
    public void testDistinctInstants(){

        final long insertCount = 10;

        LocalDateTime baseTestTime = LocalDateTime.of(2019, 4, 17, 18, 0, 0);
        Instant[] distinctDates = {
                baseTestTime.toInstant(ZoneOffset.UTC),
                baseTestTime.plusDays(1).toInstant(ZoneOffset.UTC),
                baseTestTime.plusDays(2).toInstant(ZoneOffset.UTC)};

        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(long idx=0; idx<insertCount; idx++){
                Columns columns = new Columns();

                columns.setStringThing(String.valueOf(idx));
                columns.setIntegerThing(idx);

                Instant instant = distinctDates[(int)idx%3];
                columns.setTimeStampThing(instant);

                dao.insert(columns);
            }
        });
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Instant> dbDates = dao.selectDistinct("timestamp_column", new Where());
            AssertHelp.sameContents(distinctDates, dbDates);
        });
    }

    @Test
    public void testDistinctPairs(){
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(long idx=0; idx<100; idx++){
                Columns columns = new Columns();

                columns.setStringThing(String.valueOf(idx % 10));
                columns.setIntegerThing(idx % 5);

                dao.insert(columns);
            }
        });
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Pair<String,Long>> values = dao.selectDistinctPairs(
                    "string_column",
                    "integer_column",
                    where("integer_column", GREATER_THAN, 3L));

            List<Pair<String, Long>> expected = Arrays.asList(
                    new Pair("4", 4L),
                    new Pair("9", 4L)
            );

            AssertHelp.sameContents(expected, values);
        });
    }


    @Test
    public void testDistinctTriplets(){
        LocalDateTime baseTestTime = LocalDateTime.of(2019, 4, 17, 18, 0, 0);
        Instant[] distinctDates = {
                baseTestTime.toInstant(ZoneOffset.UTC),
                baseTestTime.plusDays(1).toInstant(ZoneOffset.UTC)};

        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(long idx=0; idx<10; idx++){
                boolean even = idx % 2 == 0;

                Columns columns = new Columns();

                columns.setStringThing(even ? "A" : "B");
                columns.setIntegerThing(even ? 8L : 17L);

                Instant instant = distinctDates[(int)idx%2];
                columns.setTimeStampThing(instant);

                dao.insert(columns);
            }
        });
        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Triplet<String, Long, Instant>> values = dao.selectDistinctTriplets(
                    "string_column",
                    "integer_column",
                    "timestamp_column",
                    new Where());

            List<Triplet<String, Long, Instant>> expected = Arrays.asList(
                    new Triplet("A", 8L, distinctDates[0]),
                    new Triplet("B", 17L, distinctDates[1])
            );

            AssertHelp.sameContents(expected, values);
        });
    }
}
