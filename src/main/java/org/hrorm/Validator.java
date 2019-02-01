package org.hrorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility for checking whether or not the configuration of a <code>Dao</code>
 * matches the database schema.
 */
public class Validator extends KeylessValidator {

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
    public static void validate(Connection connection, DaoDescriptor daoDescriptor) {
        List<String> errors = findErrors(connection, daoDescriptor);
        if ( errors.size() > 0 ){
            List<String> oneLineErrors = errors.stream().map(s -> s.replaceAll("\n", " ")).collect(Collectors.toList());
            String completeMessage = String.join("\n", oneLineErrors);
            throw new HrormException(completeMessage);
        }
    }

    public static List<String> findErrors(Connection connection, DaoDescriptor daoDescriptor) {
        List<String> errors = KeylessValidator.findErrors(connection, daoDescriptor);
        errors.addAll(checkSequenceExists(connection, daoDescriptor));
        errors.addAll(checkPrimaryKeyExists(daoDescriptor));
        return errors;
    }

    private static List<String> checkPrimaryKeyExists(DaoDescriptor daoDescriptor){
        if (daoDescriptor.primaryKey() == null ){
            return Collections.singletonList("No primary key set");
        }
        return Collections.emptyList();
    }

    private static List<String> checkSequenceExists(Connection connection, DaoDescriptor<?, ?> daoDescriptor) {
        List<String> errors = new ArrayList<>();
        try {
            String sequenceName = daoDescriptor.primaryKey().getSequenceName();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select currval('" + sequenceName + "')");
            while (resultSet.next()) {
                resultSet.getLong(1);
            }
        } catch (SQLException ex){
            errors.add(ex.getMessage());
        }
        return errors;
    }

}
