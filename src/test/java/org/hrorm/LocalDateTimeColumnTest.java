package org.hrorm;

import org.hrorm.examples.Columns;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocalDateTimeColumnTest {

    @Test
    public void testSetValueHandlesNulls() throws SQLException  {

        LocalDateTimeColumn<Columns> column = new LocalDateTimeColumn<>(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing);

        Columns columns = new Columns();

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        column.setValue(columns, 1, preparedStatement);

        Mockito.verify(preparedStatement).setTimestamp(1, null);
        Mockito.verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    public void testPopulateHandlesNulls() throws SQLException {

        LocalDateTimeColumn<Columns> column = new LocalDateTimeColumn<>(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing);

        Columns columns = new Columns();

        ResultSet resultSet = Mockito.mock(ResultSet.class);

        column.populate(columns, resultSet);

        Mockito.verify(resultSet).getTimestamp("ATIME COLUMN");
        Mockito.verifyNoMoreInteractions(resultSet);
    }
}
