package org.hrorm.database;

import org.hrorm.util.RandomUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;


public class H2Helper extends AbstractHelper {

    public static final Pattern unqiueConstraintMatcher = Pattern.compile("alter table \\w+ add constraint \\w+ unique.*");

    public static final String H2ConnectionUrlPrefix = "jdbc:h2:./target/db/";


    /*
     * Creating and deleting the same schema many times seemed to be
     * causing h2 intermittent problems. Giving it a unique name
     * each time to prevent confusion.
     */
    private final String schemaExtension;

    public H2Helper(String schemaName){
        super(schemaName);
        schemaExtension = RandomUtils.randomAlphabeticString(5,6);
        cleanUpFiles();
    }

    @Override
    public Connection connect() {
        try {
            Class.forName("org.h2.Driver");
            String url = H2ConnectionUrlPrefix + extendedSchemaName();
            return DriverManager.getConnection(url);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void dropSchema(){
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
        } catch (SQLException ex){
            throw new RuntimeException(ex);
        } finally {
            cleanUpFiles();
        }
    }

    private void cleanUpFiles(){
        try {
            Path path = Paths.get("./target/db/" + extendedSchemaName() + ".mv.db");
            Files.deleteIfExists(path);
            path = Paths.get("./target/db/" + extendedSchemaName() + ".trace.db");
            Files.deleteIfExists(path);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private String extendedSchemaName(){
        return schemaName + "_" + schemaExtension;
    }

    public static String substituteDecFloatForDecimal(String line){
        return line.replaceAll("decimal", "decfloat");
    }

    @Override
    public String filterSql(String sql) {
        StringBuffer buf = new StringBuffer();
        for(String line : sql.split("\n")){
            // H2 does not support uniqueness constraints
            // and requires type decfloat to correctly support fractional values
            if( ! unqiueConstraintMatcher.matcher(line).matches() ) {
                line = substituteDecFloatForDecimal(line);
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
