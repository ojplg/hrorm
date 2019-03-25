package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.examples.Columns;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.database.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class ValidatorTest {

    // TODO: How to test this for other connections?
    // With Postgres, after the first error, it will not
    // keep working, just reporting the connection is in a bad state.
    // Perhaps the validator should ask for a connection factory?
    private static Helper helper = new H2Helper("columns");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }


    @Test
    public void testGoodDaoWorks() throws SQLException {

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withInstantColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();

        Validator.validate(connection, daoBuilder);
        connection.close();
    }

    @Test
    public void testDetectsBadSeqeunceName() throws SQLException{
        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("id", "wrong_name", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withInstantColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not detect bad sequence name");
        } catch (HrormException expected){
            String message = expected.getMessage();
            String[] messageArray = message.split("\n");
            Assert.assertEquals(1, messageArray.length);
        }
        connection.close();
    }

    @Test
    public void testDetectsBadTableName() throws SQLException {

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("bad_table_name", Columns::new)
                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withInstantColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not detect bad table name");
        } catch (HrormException expected){
            // A bad table name means lots of errors, no point in trying to count
        }
        connection.close();
    }

    @Test
    public void testDetectsMissingPrimaryKey() throws SQLException {

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("bad_table_name", Columns::new)
                .withPrimaryKey("pk", "seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withInstantColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());


        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not detect missing primary key");
        } catch (HrormException expected){
            // a bad primary key means several errors, no counting required
        }
        connection.close();
    }

    @Test
    public void testDetectsBadPrimaryKeyColumnName() throws SQLException {

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("bad_id_name", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withInstantColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not bad primary key name");
        } catch (HrormException expected){
            String message = expected.getMessage();
            String[] messageArray = message.split("\n");
            Assert.assertEquals(1, messageArray.length);
        }
        connection.close();
    }


    @Test
    public void testDetectsBadColumnName() throws SQLException {

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("bad_column_name", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withInstantColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not bad primary key name");
        } catch (HrormException expected){
            String message = expected.getMessage();
            String[] messageArray = message.split("\n");
            Assert.assertEquals(1, messageArray.length);
        }
        connection.close();
    }

    @Test
    public void testDetectsBadColumnType() throws SQLException {

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withStringColumn("integer_column", c -> "", (c,s) -> {})
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withInstantColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not bad primary key name");
        } catch (HrormException expected){
            String message = expected.getMessage();
            String[] messageArray = message.split("\n");
            Assert.assertEquals(1, messageArray.length);
        }
        connection.close();
    }

    @Test
    public void testDetectsMultipleErrors() throws SQLException {
        // four errors have been introduced
        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("id", "Wrong_Name", Columns::getId, Columns::setId)
                .withStringColumn("bad_name", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withStringColumn("decimal_column", Columns::getStringThing, Columns::setStringThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withInstantColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("Other_bad_name", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();

        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Should have found many errors");
        } catch (HrormException expected){
            String message = expected.getMessage();
            String[] messageArray = message.split("\n");
            Assert.assertEquals(4, messageArray.length);
        }
        connection.close();
    }

}
