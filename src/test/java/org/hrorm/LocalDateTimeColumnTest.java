package org.hrorm;

import org.hrorm.examples.Columns;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;

public class LocalDateTimeColumnTest {

    @Test
    public void testSetValueHandlesNulls() throws SQLException  {

        AbstractColumn<LocalDateTime,Columns,Columns> column = DataColumnFactory.localDateTimeColumn(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing, true);

        Columns columns = new Columns();

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        column.setValue(columns, 1, preparedStatement);

        Mockito.verify(preparedStatement).setNull(1, Types.TIMESTAMP);
        Mockito.verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    public void testPopulateHandlesNulls() throws SQLException {

        AbstractColumn<LocalDateTime,Columns,Columns> column = DataColumnFactory.localDateTimeColumn(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing, true);

        Columns columns = new Columns();

        ResultSet resultSet = Mockito.mock(ResultSet.class);

        column.populate(columns, resultSet);

        Mockito.verify(resultSet).getTimestamp("ATIME COLUMN");
        Mockito.verifyNoMoreInteractions(resultSet);
    }

    @Test
    public void testPreventsNullsWhenSet() throws SQLException {
        AbstractColumn<LocalDateTime,Columns,Columns> column = DataColumnFactory.localDateTimeColumn(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing, true);
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
