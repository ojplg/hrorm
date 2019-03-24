package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.examples.Columns;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.database.H2Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SqlFunctionTest {

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

    @Before
    public void insertRecords(){
        Dao<Columns> dao = daoBuilder().buildDao(helper.connect());
        for(int idx=0; idx<100; idx++){
            Columns columns = new Columns();

            columns.setStringThing("FunctionTest_" + idx);
            columns.setTimeStampThing(LocalDateTime.now());
            columns.setIntegerThing((long) idx);
            columns.setDecimalThing(new BigDecimal(idx + "." + idx));
            columns.setBooleanThing(idx % 2 == 0);
            columns.setColorThing(EnumeratedColor.Green);

            dao.insert(columns);
        }
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
    public void testCount(){

        Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

        long count = dao.runLongFunction(
                SqlFunction.COUNT, "id", Where.where("integer_column", Operator.GREATER_THAN_OR_EQUALS, 50L));

        Assert.assertEquals(50L, count);
    }


    @Test
    public void testMin(){

        Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

        long value = dao.runLongFunction(
                SqlFunction.MIN, "integer_column", Where.where());

        Assert.assertEquals(0, value);
    }

    @Test
    public void testMax(){
        Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

        long result = dao.runLongFunction(
                SqlFunction.MAX, "integer_column",
                Where.where("string_column", Operator.LIKE, "Function%"));
        Assert.assertEquals(99, result);

    }

    @Test
    public void testSum(){

        Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

        long sum = dao.runLongFunction(
                SqlFunction.SUM,
                "integer_column",
                Where.where("boolean_column", Operator.EQUALS, true)
                        .and("decimal_column", Operator.LESS_THAN, new BigDecimal("50.7")) );

        Assert.assertEquals(650, sum);

    }

    @Test
    public void testBigDecimalSum(){
        Columns template = new Columns();
        template.setBooleanThing(true);
        template.setDecimalThing(new BigDecimal("50.7"));

        Dao<Columns> dao = daoBuilder().buildDao(helper.connect());

        Map<String, Operator> whereMap = new HashMap<>();
        whereMap.put("boolean_column", Operator.EQUALS);
        whereMap.put("decimal_column", Operator.LESS_THAN);

        BigDecimal sum = dao.runBigDecimalFunction(
                SqlFunction.SUM, "decimal_column",
                Where.where("boolean_column", Operator.EQUALS, true)
                        .and("decimal_column", Operator.LESS_THAN, new BigDecimal("50.7")));

        Assert.assertEquals(new BigDecimal("658.30"), sum);
    }

    @Test
    public void testAverage(){
        Dao<Columns> dao = daoBuilder().buildDao(helper.connect());
        BigDecimal avg = dao.runBigDecimalFunction(
                SqlFunction.AVG, "decimal_column", Where.where());

        Assert.assertEquals(new BigDecimal("50.0355"), avg);
    }
}
