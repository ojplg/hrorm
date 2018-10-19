package org.hrorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Validator {

    public static void validate(Connection connection, DaoDescriptor daoDescriptor) {
        try {
            checkSequenceExists(connection, daoDescriptor);
            checkPrimaryKeyExists(connection, daoDescriptor);
            checkTableExists(connection, daoDescriptor);
            checkColumnNamesExist(connection, daoDescriptor);
            checkColumnTypesCorrect(connection, daoDescriptor);
        } catch (SQLException ex){
            throw new HrormException(ex);
        }
    }

    private static void checkPrimaryKeyExists(Connection connection, DaoDescriptor daoDescriptor){
        if ( daoDescriptor.primaryKey() == null ){
            throw new HrormException("No primary key set");
        }
    }

    private static void checkSequenceExists(Connection connection, DaoDescriptor daoDescriptor)
    throws SQLException {
        PrimaryKey primaryKey = daoDescriptor.primaryKey();
        if ( primaryKey != null) {
            String sequenceName = primaryKey.getSequenceName();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select currval('" + sequenceName + "')");
            while (resultSet.next()) {
                resultSet.getLong(1);
            }
        }
    }

    private static void checkTableExists(Connection connection, DaoDescriptor daoDescriptor)
    throws SQLException {
        String tableName = daoDescriptor.tableName();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select top 1 * from " + tableName);
        resultSet.next();
    }

    private static void checkColumnNamesExist(Connection connection, DaoDescriptor daoDescriptor)
    throws SQLException {
        String tableName = daoDescriptor.tableName();
        List<Column> columns = daoDescriptor.allColumns();
        for(Column column : columns) {
            Statement statement = connection.createStatement();
            String columnName = column.getName();
            ResultSet resultSet = statement.executeQuery("select top 1 " + columnName + " from " + tableName);
            resultSet.next();
        }
    }

    private static void checkColumnTypesCorrect(Connection connection, DaoDescriptor daoDescriptor)
            throws SQLException {
        String tableName = daoDescriptor.tableName();
        List<Column> columns = daoDescriptor.allColumns();
        for(Column column : columns) {
            Statement statement = connection.createStatement();
            String columnName = column.getName();
            ResultSet resultSet = statement.executeQuery("select top 1 " + columnName + " from " + tableName);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnType = resultSetMetaData.getColumnType(1);
            if ( ! column.supportedTypes().contains(columnType) ){
                throw new HrormException("Column " + columnName + " does not support type " + columnType);
            }
        }
    }

}
