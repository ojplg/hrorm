package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.examples.Columns;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.database.H2Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrderTest {

    static { TestLogConfig.load(); }

    private static Helper helper = new H2Helper("columns");

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
    public void testOrder(){

        {
            List<Long> numbers = new ArrayList<>();
            for (long idx = 0; idx < 100; idx++) {
                numbers.add(idx);
            }
            Collections.shuffle(numbers);

            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

            for (Long number : numbers) {
                Columns columns = new Columns();
                columns.setIntegerThing(number);
                dao.insert(columns);
            }
        }
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());
            List<Columns> ordered = dao.select(new Where(), Order.ascending("integer_column"));
            Assert.assertTrue(ordered.size() > 10 );
            for(long idx=0 ; idx< 100; idx++ ){
                Columns columns = ordered.get((int) idx);
                Assert.assertEquals(idx, (long) columns.getIntegerThing());
            }
        }
    }

    @Test
    public void testOrderDescending(){
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

            List<String> strings = Arrays.asList( "a", "b" , "c" , "d", "e" , "f" , "g", "h", "i", "a" , "a" , "b" , "c", "d");
            Collections.shuffle(strings);
            for(String s : strings){
                Columns columns = new Columns();
                columns.setStringThing(s);
                dao.insert(columns);
            }
        }
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

            String lastString = "z";
            List<Columns> ordered = dao.select(new Where(), Order.descending("string_column"));

            Assert.assertTrue(ordered.size() > 10 );

            for(Columns columns : ordered){
                String nextString = columns.getStringThing();
                Assert.assertTrue(nextString.compareTo(lastString) <= 0);
                lastString = nextString;
            }
        }
    }

    @Test
    public void testOrderingWorksOnTwoColumns(){
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

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
        }
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());
            List<Columns> ordered = dao.select(Where.where("integer_column", Operator.GREATER_THAN, 5L)
                                                .and("integer_column", Operator.LESS_THAN, 15L),
                                                Order.ascending("string_column", "integer_column"));

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
    public void testOrderForSelectAll(){

        {
            List<Long> numbers = new ArrayList<>();
            for (long idx = 0; idx < 100; idx++) {
                numbers.add(idx);
            }
            Collections.shuffle(numbers);

            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

            for (Long number : numbers) {
                Columns columns = new Columns();
                columns.setIntegerThing(number);
                dao.insert(columns);
            }
        }
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());
            List<Columns> ordered = dao.selectAll(Order.descending("integer_column"));
            Assert.assertEquals(100, ordered.size() );
            long last = 100;
            for(int idx=0 ; idx<100; idx++ ){
                Columns columns = ordered.get(idx);
                Assert.assertEquals(last - 1, (long) columns.getIntegerThing());
                last = columns.getIntegerThing();
            }
        }
    }

    @Test
    public void testOrderingSelectByColumns(){
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

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
        }
        {
            Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

            Columns template = new Columns();
            template.setStringThing("a");

            List<Columns> ordered = dao.selectManyByColumns(template,
                    Order.ascending( "integer_column"),
                    "string_column");

            Assert.assertEquals(60,ordered.size());

            long lastNumber = 0L;
            for( Columns columns : ordered ){
                Assert.assertEquals("a", columns.getStringThing());
                Assert.assertTrue(lastNumber <= columns.getIntegerThing());
                lastNumber = columns.getIntegerThing();
            }
        }
    }

}
