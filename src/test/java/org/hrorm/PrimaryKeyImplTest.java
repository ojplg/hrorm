package org.hrorm;

import org.hrorm.examples.Columns;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrimaryKeyImplTest {

    @Test
    public void testNotNullable() throws SQLException {
        PrimaryKeyImpl<Columns> primaryKey = new PrimaryKeyImpl<>(
                "ID", "A", Columns::getIntegerThing, Columns::setIntegerThing, "SEQUENCE");

        Columns columns = new Columns();

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        try {
            primaryKey.setValue(columns, 1, preparedStatement);
            Assert.fail("Should not allow null value");
        } catch (HrormException expected){
        }
        Mockito.verifyZeroInteractions(preparedStatement);
    }

}
