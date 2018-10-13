package org.hrorm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Some small utilities used internally by hrorm.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class DaoHelper {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    public static void runDelete(Connection connection, String sql) {
        logger.info(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql) ){
            preparedStatement.execute();
        } catch (SQLException ex){
            throw new HrormException(ex, sql);
        }
    }

    public static void runPreparedDelete(Connection connection, String sql, Long id){
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql) ){
            preparedStatement.setLong(1, id);
            logger.info(sql);
            preparedStatement.execute();
        } catch (SQLException ex){
            throw new HrormException(ex, sql);
        }
    }

    public static long getNextSequenceValue(Connection connection, String sequenceName) {
        Statement statement = null;
        ResultSet resultSet = null;
        String sql = "select nextval('" + sequenceName + "')";
        try {
            statement = connection.createStatement();
            logger.info(sql);
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException ex){
            throw new HrormException(ex, sql);
        } finally {
            try {
                if ( resultSet != null ){
                    resultSet.close();
                }
                if ( statement != null){
                    statement.close();
                }
            } catch (SQLException ex){
                throw new HrormException(ex);
            }
        }
    }

    public static List<Long> readLongs(Connection connection, String sql, Long id){
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            List<Long> longs = new ArrayList<>();
            logger.info(sql);
            statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                longs.add(resultSet.getLong(1));
            }
            return longs;
        } catch (SQLException ex){
            throw new HrormException(ex, sql);
        } finally {
            try {
                if ( resultSet != null ){
                    resultSet.close();
                }
                if ( statement != null){
                    statement.close();
                }
            } catch (SQLException ex){
                throw new HrormException(ex);
            }
        }
    }
}
