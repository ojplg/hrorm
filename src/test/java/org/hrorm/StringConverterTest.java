package org.hrorm;

import org.hrorm.examples.Columns;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.EnumeratedColorConverter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StringConverterTest {

    @Test
    public void testSetValueHandlesNulls() throws SQLException {

        StringConverterColumn<Columns, EnumeratedColor> column = new StringConverterColumn<>(
                "COLOR", "A", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Columns columns = new Columns();

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        column.setValue(columns, 1, preparedStatement);

        Mockito.verify(preparedStatement).setString(1, null);
        Mockito.verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    public void testPopulateHandlesNulls() throws SQLException {

        StringConverterColumn<Columns, EnumeratedColor> column = new StringConverterColumn<>(
                "COLOR", "A", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());

        Columns columns = new Columns();

        ResultSet resultSet = Mockito.mock(ResultSet.class);

        column.populate(columns, resultSet);

        Mockito.verify(resultSet).getString("ACOLOR");
        Mockito.verifyNoMoreInteractions(resultSet);
    }

    @Test
    public void testPreventsNullsWhenSet() throws SQLException {
        StringConverterColumn<Columns, EnumeratedColor> column = new StringConverterColumn<>(
                "COLOR", "A", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter());
        column.notNull();

        Columns columns = new Columns();

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        try {
            column.setValue(columns, 1, preparedStatement);
            Assert.fail("Should not allow null value");
        } catch (HrormException expected){

        }
        Mockito.verifyZeroInteractions(preparedStatement);
    }

}
