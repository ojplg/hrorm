package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.Columns;
import org.hrorm.examples.ColumnsDaoBuilder;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.util.TestLogConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SqlFunctionTest {

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

    @Before
    public void insertRecords() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
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
        connection.commit();
        connection.close();
    }

    @Test
    public void testCount() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        long count = dao.runLongFunction(
                SqlFunction.COUNT, "id", Where.where("integer_column", Operator.GREATER_THAN_OR_EQUALS, 50L));

        Assert.assertEquals(50L, count);
        connection.close();
    }


    @Test
    public void testMin() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        long value = dao.runLongFunction(
                SqlFunction.MIN, "integer_column", Where.where());

        Assert.assertEquals(0, value);
        connection.close();
    }

    @Test
    public void testMax() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        long result = dao.runLongFunction(
                SqlFunction.MAX, "integer_column",
                Where.where("string_column", Operator.LIKE, "Function%"));
        Assert.assertEquals(99, result);
        connection.close();
    }

    @Test
    public void testSum() throws SQLException {

        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        long sum = dao.runLongFunction(
                SqlFunction.SUM,
                "integer_column",
                Where.where("boolean_column", Operator.EQUALS, true)
                        .and("decimal_column", Operator.LESS_THAN, new BigDecimal("50.7")) );

        Assert.assertEquals(650, sum);
        connection.close();
    }

    @Test
    public void testBigDecimalSum() throws SQLException {
        Columns template = new Columns();
        template.setBooleanThing(true);
        template.setDecimalThing(new BigDecimal("50.7"));

        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        Map<String, Operator> whereMap = new HashMap<>();
        whereMap.put("boolean_column", Operator.EQUALS);
        whereMap.put("decimal_column", Operator.LESS_THAN);

        BigDecimal sum = dao.runBigDecimalFunction(
                SqlFunction.SUM, "decimal_column",
                Where.where("boolean_column", Operator.EQUALS, true)
                        .and("decimal_column", Operator.LESS_THAN, new BigDecimal("50.7")));

        Assert.assertEquals(new BigDecimal("658.30"), sum);
        connection.close();
    }

    @Test
    public void testAverage() throws SQLException {
        Connection connection = helper.connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);
        BigDecimal avg = dao.runBigDecimalFunction(
                SqlFunction.AVG, "decimal_column", Where.where());
        connection.close();

        Assert.assertEquals(0, new BigDecimal("50.0355").compareTo(avg));
    }
}
