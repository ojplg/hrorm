package org.hrorm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresHelper extends AbstractHelper {

    private final String url;
    private final String user;
    private final String password;

    public PostgresHelper(String schemaName){
        this(schemaName, "jdbc:postgresql://localhost:5432/hrorm", "hrorm_user", "hrorm_password");
    }

    public PostgresHelper(String schemaName, String url, String user, String password){
        super(schemaName);
        this.url = url;
        this.user = user;
        this.password = password;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex){
            throw new RuntimeException(ex);
        }
    }

    public Connection connect(){
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void dropSchema() {
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();

            for(Constraint constraint : constraintNames){
                statement.execute("alter table " + constraint.getTableName() + " drop " + constraint.getConstraintName());
            }

            for (String tableName : tableNames) {
                statement.execute("drop table " + tableName);
            }

            for (String sequenceName : sequenceNames) {
                statement.execute("drop sequence " + sequenceName);
            }

            statement.close();
            connection.commit();
            connection.close();
        } catch (SQLException ex){
            throw new RuntimeException(ex);
        }

    }

    @Override
    public DatabasePlatform getPlatform() {
        return DatabasePlatform.Postgres;
    }
}
