package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.Columns;
import org.hrorm.examples.ColumnsDaoBuilder;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.RandomUtils;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    public void testDistinctInstants(){

        final long insertCount = 1;

//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        System.out.println("TIMESTAMP " + timestamp);
//        Instant instant = (Instant) timestamp;


        LocalDateTime baseTestTime = LocalDateTime.of(2019, 4, 17, 18, 0, 0);
        Instant[] distinctDates = {
//                baseTestTime.toInstant(ZoneOffset.UTC),
//                baseTestTime.plusDays(1).toInstant(ZoneOffset.UTC),
//                baseTestTime.plusDays(2).toInstant(ZoneOffset.UTC)
                Instant.now()        };

        helper.useConnection(connection -> {
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for(long idx=0; idx<insertCount; idx++){
                Columns columns = new Columns();

                columns.setStringThing(String.valueOf(idx));
                columns.setIntegerThing(idx);

//                LocalDateTime localDateTime = baseTestTime.plusDays(idx % 3);
//                Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
                Instant instant = distinctDates[(int)idx%3];
                columns.setTimeStampThing(instant);

                dao.insert(columns);
            }
        });
        helper.useConnection(connection -> {
            System.out.println("DOING READ TEST");
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> found = dao.selectAll();
            List<Instant> expectedDates = Arrays.asList(distinctDates);
            found.stream().forEach(
                    item -> Assert.assertTrue(expectedDates.contains(item.getTimeStampThing()))
            );
            Assert.assertEquals(insertCount, found.size());
        });

        helper.useConnection(connection -> {
            System.out.println("DOING DISTINCT TEST");
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Instant> values = dao.selectDistinct( "timestamp_column",new Where());
            AssertHelp.sameContents(distinctDates, values);
            long count = dao.runLongFunction(SqlFunction.COUNT, "integer_column", new Where());
            Assert.assertEquals(insertCount, count);
        });
    }

}
