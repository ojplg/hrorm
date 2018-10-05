package org.hrorm;

import org.hrorm.examples.Columns;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.EnumeratedColorConverter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public class ColumnsTest {

    public static final String H2ConnectionUrlPrefix = "jdbc:h2:./db/";
    public static final String TestDbName = "columns";

    public static final String TestSchema =
            "create sequence columns_seq;"
            + "create table columns ("
            + " id integer PRIMARY KEY,"
            + " string_column text, "
            + " integer_column integer, "
            + " boolean_column text, "
            + " timestamp_column timestamp, "
            + " color_column text "
            + ");";

    private static boolean initialized;

    private Connection connect() {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(H2ConnectionUrlPrefix + TestDbName);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @After
    public void cleanUpDb(){
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            statement.execute("delete from columns");

            Path path = Paths.get("./db/" + TestDbName + ".mv.db");
            Files.deleteIfExists(path);
            path = Paths.get("./db/" + TestDbName + ".trace.db");
            Files.deleteIfExists(path);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Before
    public void setUpDb(){
        if ( ! initialized ) {
            try {
                Connection connection = connect();
                Statement statement = connection.createStatement();
                statement.execute(TestSchema);
                initialized = true;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private DaoBuilder<Columns> daoBuilder(){
        return new DaoBuilder<>("columns", Columns::new)
                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withLocalDateTimeColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());
    }

    @Test
    public void testInsertAndSelect(){
        Connection connection = connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime time = LocalDateTime.now();

        Columns columns = new Columns();
        columns.setStringThing("InsertSelectTest");
        columns.setIntegerThing(762L);
        columns.setBooleanThing(true);
        columns.setTimeStampThing(time);
        columns.setColorThing(EnumeratedColor.Red);

        dao.insert(columns);

        Columns dbInstance = dao.select(columns.getId());

        Assert.assertEquals(columns, dbInstance);
    }

    @Test
    public void testUpdates(){
        Connection connection = connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime dec1 = LocalDateTime.of(2018, 12, 1, 3, 45);

        Columns columns = new Columns();
        columns.setStringThing("UpdateTest");
        columns.setIntegerThing(762L);
        columns.setBooleanThing(true);
        columns.setTimeStampThing(dec1);
        columns.setColorThing(EnumeratedColor.Green);

        dao.insert(columns);

        Columns dbInstance = dao.select(columns.getId());
        Assert.assertEquals(columns, dbInstance);

        LocalDateTime oct5 = LocalDateTime.of(2019, 10, 5, 4, 45);

        columns.setStringThing("UpdateTest New Value");
        columns.setIntegerThing(1234L);
        columns.setBooleanThing(false);
        columns.setTimeStampThing(oct5);
        columns.setColorThing(EnumeratedColor.Blue);

        dao.update(columns);

        Columns dbInstance2 = dao.select(columns.getId());
        Assert.assertEquals(columns, dbInstance2);
    }

    @Test
    public void testSelectByColumns(){
        Connection connection = connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime dec1 = LocalDateTime.of(2018, 12, 1, 3, 45);

        Columns columns = new Columns();
        columns.setStringThing("Select By Column Test");
        columns.setIntegerThing(762L);
        columns.setBooleanThing(true);
        columns.setTimeStampThing(dec1);
        columns.setColorThing(EnumeratedColor.Green);

        dao.insert(columns);

        Columns template = new Columns();
        template.setStringThing("Select By Column Test");
        template.setIntegerThing(762L);
        template.setBooleanThing(true);
        template.setTimeStampThing(dec1);
        template.setColorThing(EnumeratedColor.Green);

        Columns dbInstance = dao.selectByColumns(template, "string_column", "integer_column", "boolean_column", "timestamp_column", "color_column");
        Assert.assertEquals(columns.getId(), dbInstance.getId());
    }

    @Test
    public void testSelectManyByColumns(){
        Connection connection = connect();
        Dao<Columns> dao = daoBuilder().buildDao(connection);

        LocalDateTime dec1 = LocalDateTime.of(2018, 12, 1, 3, 45);

        Columns columns1 = new Columns();
        columns1.setStringThing("Select By Column Test");
        columns1.setIntegerThing(762L);
        columns1.setBooleanThing(true);
        columns1.setTimeStampThing(dec1);
        columns1.setColorThing(EnumeratedColor.Green);

        dao.insert(columns1);

        Columns columns2 = new Columns();
        columns2.setStringThing("Select By Column Test");
        columns2.setIntegerThing(-1234L);
        columns2.setBooleanThing(false);
        columns2.setTimeStampThing(dec1);
        columns2.setColorThing(EnumeratedColor.Red);

        dao.insert(columns2);

        Columns columns3 = new Columns();
        columns3.setStringThing("Select By Column Test");
        columns3.setIntegerThing(0L);
        columns3.setBooleanThing(false);
        columns3.setTimeStampThing(dec1);
        columns3.setColorThing(EnumeratedColor.Blue);

        dao.insert(columns3);

        LocalDateTime oct5 = LocalDateTime.of(2019, 10, 5, 4, 45);

        Columns columns4 = new Columns();
        columns4.setStringThing("Select By Column Test");
        columns4.setIntegerThing(0L);
        columns4.setBooleanThing(false);
        columns4.setTimeStampThing(oct5);
        columns4.setColorThing(EnumeratedColor.Blue);

        dao.insert(columns4);

        Columns template = new Columns();
        template.setStringThing("Select By Column Test");
        template.setTimeStampThing(dec1);

        List<Columns> dbInstanceList = dao.selectManyByColumns(template, "string_column", "timestamp_column");
        Assert.assertEquals(3, dbInstanceList.size());
    }


}
