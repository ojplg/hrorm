package org.hrorm;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransactorTest {

    @Test
    public void testsCommitsAndClosesOnAction() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);

        Transactor transactor = new Transactor(connection);

        transactor.runAndCommit(con -> {});

        Mockito.verify(connection).commit();
        Mockito.verify(connection).close();
    }

    @Test
    public void testsCommitsAndClosesOnFunction() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);

        Transactor transactor = new Transactor(connection);

        Object object = transactor.runAndCommit(con -> { return new Object(); });

        Assert.assertNotNull(object);

        Mockito.verify(connection).commit();
        Mockito.verify(connection).close();
    }

    @Test
    public void testRollsbackAndClosesOnActionException() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);

        Transactor transactor = new Transactor(connection);

        try {
            transactor.runAndCommit((Consumer<Connection>) con -> {
                throw new HrormException("expected");
            });
        } catch (RuntimeException expected){
        }

        Mockito.verify(connection).rollback();
        Mockito.verify(connection).close();
    }

    @Test
    public void testRollsbackAndClosesOnFunctionException() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);

        Transactor transactor = new Transactor(connection);

        try {
            transactor.runAndCommit((Function<Connection, Object>) con -> {
                throw new HrormException("expected");
            });
        } catch (RuntimeException expected){
        }

        Mockito.verify(connection).rollback();
        Mockito.verify(connection).close();
    }

}
