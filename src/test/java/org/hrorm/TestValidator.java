package org.hrorm;

import org.hrorm.examples.Columns;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;

public class TestValidator {

    private static H2Helper helper = new H2Helper("columns");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }


    @Test
    public void testGoodDaoWorks() {

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withLocalDateTimeColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();

        Validator.validate(connection, daoBuilder);
    }

    @Test
    public void testDetectsBadSeqeunceName(){
        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("id", "wrong_name", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withLocalDateTimeColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not detect bad sequence name");
        } catch (HrormException expected){}
    }

    @Test
    public void testDetectsBadTableName(){

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("bad_table_name", Columns::new)
                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withLocalDateTimeColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());


        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not detect bad table name");
        } catch (HrormException expected){}

    }

    @Test
    public void testDetectsMissingPrimaryKey(){

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("bad_table_name", Columns::new)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withLocalDateTimeColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());


        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not detect missing primary key");
        } catch (HrormException expected){}

    }

    @Test
    public void testDetectsBadPrimaryKeyColumnName(){

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("bad_id_name", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("integer_column", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withLocalDateTimeColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not bad primary key name");
        } catch (HrormException expected){}
    }


    @Test
    public void testDetectsBadColumnName(){

        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
                .withIntegerColumn("bad_column_name", Columns::getIntegerThing, Columns::setIntegerThing)
                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
                .withLocalDateTimeColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Connection connection = helper.connect();
        try {
            Validator.validate(connection, daoBuilder);
            Assert.fail("Did not bad primary key name");
        } catch (HrormException expected){}
    }

    // FIXME: need to make this work
//    @Test
//    public void testDetectsBadColumnType(){
//
//        DaoBuilder<Columns> daoBuilder = new DaoBuilder<>("columns_table", Columns::new)
//                .withPrimaryKey("id", "columns_seq", Columns::getId, Columns::setId)
//                .withStringColumn("string_column", Columns::getStringThing, Columns::setStringThing)
//                .withStringColumn("integer_column", c -> "", (c,s) -> {})
//                .withBigDecimalColumn("decimal_column", Columns::getDecimalThing, Columns::setDecimalThing)
//                .withBooleanColumn("boolean_column", Columns::getBooleanThing, Columns::setBooleanThing)
//                .withLocalDateTimeColumn("timestamp_column", Columns::getTimeStampThing, Columns::setTimeStampThing)
//                .withConvertingStringColumn("color_column", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());
//
//        Connection connection = helper.connect();
//        try {
//            Validator.validate(connection, daoBuilder);
//            Assert.fail("Did not bad primary key name");
//        } catch (HrormException expected){}
//    }


}
