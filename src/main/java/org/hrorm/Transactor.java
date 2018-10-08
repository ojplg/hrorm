package org.hrorm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An aid for managing transactions.
 *
 * <p>
 *
 * Transactors save the trouble of writing blocks like
 *     <pre>{@code
 *     Connection connection = null;
 *     try {
 *          connection = getConnection();
 *
 *          // do something with the connection
 *
 *          connection.commit();
 *     } catch (Exception ex){
 *         connection.rollback();
 *         throw ex;
 *     } finally {
 *         if (connection != null){
 *             connection.close();
 *         }
 *     }}</pre>
 *
 * Instead, you just write the part in the middle where you do
 * something with the connection and the other parts are done for you.
 *
 * <p>
 * Transactors will close the connection when done with their business.
 */
public class Transactor {

    private final Supplier<Connection> connectionSupplier;

    /**
     * Use this constructor for a use-once Transactor.
     *
     * @param connection the connection to use
     */
    public Transactor(Connection connection){
        this.connectionSupplier = () -> connection;
    }

    /**
     * Use this constructor with a connection source, so that the Transactor
     * can create a new connection each time it needs one.
     *
     * @param connectionSupplier A source for connections.
     */
    public Transactor(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    /**
     * Runs the specified action and either commits when complete or
     * rolls back on an exception. The connection used for this action
     * <b>will be closed</b> when the action executes, whether it succeeds or fails.
     *
     * @param action the action to perform
     */
    public void runAndCommit(Consumer<Connection> action) {
        try {
            internalRunAndCommit(action);
        } catch (SQLException ex){
            throw new HrormException(ex);
        }
    }

    /**
     * Runs the specified function and either commits when complete or
     * rolls back on an exception. The connection used for this action
     * <b>will be closed</b> when the action executes, whether it succeeds or fails.
     *
     * @param function the function to run
     * @param <RESULT> the type of result the function returns
     * @return the result of the transaction
     */
    public <RESULT> RESULT runAndCommit(Function<Connection, RESULT> function){
        try {
            return internalRunAndCommit(function);
        } catch (SQLException ex){
            throw new HrormException(ex);
        }
    }

    private void internalRunAndCommit(Consumer<Connection> action) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionSupplier.get();
            action.accept(connection);
            connection.commit();
        } catch (SQLException ex){
            connection.rollback();
            throw ex;
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            if( connection != null ) {
                connection.close();
            }
        }
    }

    private <RESULT> RESULT internalRunAndCommit(Function<Connection, RESULT> function) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionSupplier.get();
            RESULT result = function.apply(connection);
            connection.commit();
            return result;
        } catch (SQLException ex){
            connection.rollback();
            throw ex;
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
