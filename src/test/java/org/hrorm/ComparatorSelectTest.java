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
            template.setStringThing("LIKE");

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
}
