package org.hrorm;

import org.hrorm.examples.Columns;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LongColumnTest {

    @Test
    public void testNotNullable() throws SQLException {
        LongColumn<Columns> column = new LongColumn<>(
                "NUMBER", "A", Columns::getIntegerThing, Columns::setIntegerThing);
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
