package org.hrorm.h2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class H2Helper {

    public static final String H2ConnectionUrlPrefix = "jdbc:h2:./db/";

    private final String schemaName;
    private boolean initialized = false;

    public H2Helper(String schemaName){
        this.schemaName = schemaName;
    }

    public String readSchema(){
        try {
            InputStream inputStream = this.getClass().getResourceAsStream("/schemas/" + schemaName + ".sql");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder wholeFileBuffer = new StringBuilder();
            bufferedReader.lines().forEach( line -> {
                wholeFileBuffer.append(line);
                wholeFileBuffer.append("\n");
            });
            return wholeFileBuffer.toString();
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public Connection connect() {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(H2ConnectionUrlPrefix + schemaName);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void dropSchema(){
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            statement.execute("delete from simple");

            Path path = Paths.get("./db/" + schemaName + ".mv.db");
            Files.deleteIfExists(path);
            path = Paths.get("./db/" + schemaName + ".trace.db");
            Files.deleteIfExists(path);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void initializeSchema(){
        if ( ! initialized ) {
            try {
                Connection connection = connect();
                Statement statement = connection.createStatement();
                String sql = readSchema();
                statement.execute(sql);
                initialized = true;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
