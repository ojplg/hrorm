package org.hrorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility for checking whether or not the configuration of a <code>KeylessDao</code>
 * matches the database schema.
 * See also: {@link Validator}
 */
public class KeylessValidator {

    /**
     * Calling this method will attempt to check to make sure that certain basics of the
     * <code>Dao</code> definition correctly match the database schema. The following will
     * be checked.
     *
     * <ul>
     *     <li>That the <code>Dao</code> has a <code>PrimaryKey</code> defined</li>
     *     <li>That the sequence exists</li>
     *     <li>That the table exists</li>
     *     <li>That the columns are correctly named</li>
     *     <li>That the columns are of correct type</li>
     * </ul>
     *
     * <p>If a problem is found, an <code>HrormException</code> will be thrown
     * describing the issue.</p>
     *
     * <p>This is no substitute for testing.</p>
     *
     * @param connection A connection to the database with the schema for the passed <code>DaoDescriptor</code>
     * @param daoDescriptor The definition of the <code>Dao</code> to be checked
     * @throws HrormException if a problem is discovered
     */
    public static void validate(Connection connection, KeylessDaoDescriptor daoDescriptor) {
        List<String> errors = findErrors(connection, daoDescriptor);
        if ( errors.size() > 0 ){
            List<String> oneLineErrors = errors.stream().map(s -> s.replaceAll("\n", " ")).collect(Collectors.toList());
            String completeMessage = String.join("\n", oneLineErrors);
            throw new HrormException(completeMessage);
        }
    }

    public static List<String> findErrors(Connection connection, KeylessDaoDescriptor daoDescriptor) {
        List<String> errors = new ArrayList<>();
        errors.addAll(checkTableExists(connection, daoDescriptor));
        errors.addAll(checkColumnTypesCorrect(connection, daoDescriptor));
        return errors;
    }

    private static List<String> checkTableExists(Connection connection, KeylessDaoDescriptor daoDescriptor) {
        try {
            String tableName = daoDescriptor.tableName();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select top 1 * from " + tableName);
            resultSet.next();
        } catch (SQLException ex){
            return Collections.singletonList(ex.getMessage());
        }
        return Collections.emptyList();
    }

    private static List<String> checkColumnTypesCorrect(Connection connection, KeylessDaoDescriptor daoDescriptor) {
        String tableName = daoDescriptor.tableName();
        List<Column> columns = daoDescriptor.allColumns();
        List<String> errors = new ArrayList<>();
        for(Column column : columns) {
            try {
                Statement statement = connection.createStatement();
                String columnName = column.getName();
                ResultSet resultSet = statement.executeQuery("select top 1 " + columnName + " from " + tableName);
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int columnType = resultSetMetaData.getColumnType(1);
                if ( ! column.supportedTypes().contains(columnType) ){
                    errors.add("Column " + columnName + " does not support type " + columnType);
                }
            } catch (SQLException ex){
                errors.add(ex.getMessage());
            }
        }
        return errors;
    }

}
