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
import java.sql.Types;

public class StringConverterTest {

    @Test
    public void testSetValueHandlesNulls() throws SQLException {

        Column<String, EnumeratedColor, Columns, Columns> column = DataColumnFactory.stringConverterColumn(
                "COLOR", "A", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter(), true);

        Columns columns = new Columns();

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        column.setValue(columns, 1, preparedStatement);

        Mockito.verify(preparedStatement).setNull(1, Types.VARCHAR);
        Mockito.verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    public void testPopulateHandlesNulls() throws SQLException {

        Column<String, EnumeratedColor, Columns, Columns> column = DataColumnFactory.stringConverterColumn(
                "COLOR", "A", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter(), true);

        Columns columns = new Columns();

        ResultSet resultSet = Mockito.mock(ResultSet.class);

        column.populate(columns, resultSet);

        Mockito.verify(resultSet).getString("ACOLOR");
        Mockito.verify(resultSet).wasNull();
        Mockito.verifyNoMoreInteractions(resultSet);
    }

    @Test
    public void testPreventsNullsWhenSet() throws SQLException {
        Column<String, EnumeratedColor, Columns, Columns> column = DataColumnFactory.stringConverterColumn(
                "COLOR", "A", Columns::getColorThing, Columns::setColorThing, new EnumeratedColorConverter(), true);
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
