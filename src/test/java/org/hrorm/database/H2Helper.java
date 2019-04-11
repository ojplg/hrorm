package org.hrorm.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.regex.Pattern;


public class H2Helper extends AbstractHelper {

    public static final Pattern unqiueConstraintMatcher = Pattern.compile("alter table \\w+ add constraint \\w+ unique.*");

    public static final String H2ConnectionUrlPrefix = "jdbc:h2:./target/db/";

    public H2Helper(String schemaName){
        super(schemaName);
    }

    @Override
    public Connection connect() {
        try {
            Class.forName("org.h2.Driver");
            String url = H2ConnectionUrlPrefix + schemaName;
            return DriverManager.getConnection(url);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void dropSchema(){
        Exception deferred = null;
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            for (String sequence : sequenceNames) {
                statement.execute("drop sequence " + sequence);
            }
            for (String table : tableNames) {
                statement.execute("drop table " + table );
            }
            connection.commit();
            connection.close();
        } catch (Exception ex) {
            deferred = ex;
        }
        try {
            Path path = Paths.get("./target/db/" + schemaName + ".mv.db");
            Files.deleteIfExists(path);
            path = Paths.get("./target/db/" + schemaName + ".trace.db");
            Files.deleteIfExists(path);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
        if ( deferred != null ){
            throw new RuntimeException(deferred);
        }
    }

    @Override
    public String filterSql(String sql) {
        StringBuffer buf = new StringBuffer();
        for(String line : sql.split("\n")){
            // H2 does not support uniqueness constraints
            if( ! unqiueConstraintMatcher.matcher(line).matches() ) {
                buf.append(line);
                buf.append("\n");
            }
        }
        return buf.toString();
    }

    @Override
    public DatabasePlatform getPlatform() {
        return DatabasePlatform.H2;
    }

}
