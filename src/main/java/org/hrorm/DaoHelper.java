package org.hrorm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DaoHelper {

    public static void runDelete(Connection connection, String sql) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql) ){
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
}
