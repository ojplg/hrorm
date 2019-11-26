package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.Columns;
import org.hrorm.examples.ColumnsDaoBuilder;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrderTest {

    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("columns_two");

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
    public void testOrder() throws SQLException {

        {
            List<Long> numbers = new ArrayList<>();
            for (long idx = 0; idx < 100; idx++) {
                numbers.add(idx);
            }
            Collections.shuffle(numbers);

            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for (Long number : numbers) {
                Columns columns = new Columns();
                columns.setIntegerThing(number);
                dao.insert(columns);
            }
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> ordered = dao.select(new Where(), Order.ascending("integer_column"));
            Assert.assertTrue(ordered.size() > 10 );
            for(long idx=0 ; idx< 100; idx++ ){
                Columns columns = ordered.get((int) idx);
                Assert.assertEquals(idx, (long) columns.getIntegerThing());
            }
            connection.close();
        }
    }

    @Test
    public void testOrderDescending() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<String> strings = Arrays.asList( "a", "b" , "c" , "d", "e" , "f" , "g", "h", "i", "a" , "a" , "b" , "c", "d");
            Collections.shuffle(strings);
            for(String s : strings){
                Columns columns = new Columns();
                columns.setStringThing(s);
                dao.insert(columns);
            }
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            String lastString = "z";
            List<Columns> ordered = dao.select(new Where(), Order.descending("string_column"));

            Assert.assertTrue(ordered.size() > 10 );

            for(Columns columns : ordered){
                String nextString = columns.getStringThing();
                Assert.assertTrue(nextString.compareTo(lastString) <= 0);
                lastString = nextString;
            }
            connection.close();
        }
    }

    @Test
    public void testOrderingWorksOnTwoColumns() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<String> strings = Arrays.asList( "a", "b" , "c" , "d", "e" , "f" , "g", "h", "i", "a" , "a" , "b" , "c", "d", "e");
            List<Long> numbers = new ArrayList<>();
            for (long idx = 0; idx < 20; idx++) {
                numbers.add(idx);
            }
            Collections.shuffle(strings);
            Collections.shuffle(numbers);

            for(String s : strings){
                for(Long n : numbers) {
                    Columns columns = new Columns();
                    columns.setStringThing(s);
                    columns.setIntegerThing(n);
                    dao.insert(columns);
                }
            }
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> ordered = dao.select(Where.where("integer_column", Operator.GREATER_THAN, 5L)
                                                .and("integer_column", Operator.LESS_THAN, 15L),
                                                Order.ascending("string_column", "integer_column"));

            connection.close();

            Assert.assertTrue(ordered.size() > 10 );

            String lastString = "";
            long lastNumber = 0L;
            for( Columns columns : ordered ){
                Assert.assertTrue(lastString.compareTo(columns.getStringThing()) <= 0);
                if( columns.getStringThing().equals(lastString)){
                    Assert.assertTrue(columns.getIntegerThing() >= lastNumber);
                }
                lastNumber = columns.getIntegerThing();
                lastString = columns.getStringThing();
            }
        }
    }

    @Test
    public void testOrderForSelectAll() throws SQLException {

        {
            List<Long> numbers = new ArrayList<>();
            for (long idx = 0; idx < 100; idx++) {
                numbers.add(idx);
            }
            Collections.shuffle(numbers);

            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            for (Long number : numbers) {
                Columns columns = new Columns();
                columns.setIntegerThing(number);
                dao.insert(columns);
            }
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);
            List<Columns> ordered = dao.select(Order.descending("integer_column"));
            Assert.assertEquals(100, ordered.size() );
            long last = 100;
            for(int idx=0 ; idx<100; idx++ ){
                Columns columns = ordered.get(idx);
                Assert.assertEquals(last - 1, (long) columns.getIntegerThing());
                last = columns.getIntegerThing();
            }
            connection.close();
        }
    }

    @Test
    public void testOrderingSelectByColumns() throws SQLException {
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            List<String> strings = Arrays.asList( "a", "b" , "c" , "d", "e" , "f" , "g", "h", "i", "a" , "a" , "b" , "c", "d", "e");
            List<Long> numbers = new ArrayList<>();
            for (long idx = 0; idx < 20; idx++) {
                numbers.add(idx);
            }
            Collections.shuffle(strings);
            Collections.shuffle(numbers);

            for(String s : strings){
                for(Long n : numbers) {
                    Columns columns = new Columns();
                    columns.setStringThing(s);
                    columns.setIntegerThing(n);
                    dao.insert(columns);
                }
            }
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<Columns> dao = daoBuilder().buildDao(connection);

            Columns template = new Columns();
            template.setStringThing("a");

            List<Columns> ordered = dao.select(template,
                    Order.ascending( "integer_column"),
                    "string_column");

            Assert.assertEquals(60,ordered.size());

            long lastNumber = 0L;
            for( Columns columns : ordered ){
                Assert.assertEquals("a", columns.getStringThing());
                Assert.assertTrue(lastNumber <= columns.getIntegerThing());
                lastNumber = columns.getIntegerThing();
            }
            connection.close();
        }
    }

}
