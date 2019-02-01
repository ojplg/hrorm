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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class H2Helper {

    public static final String H2ConnectionUrlPrefix = "jdbc:h2:./target/db/";

    private final String schemaName;
    private boolean initialized = false;

    private static final Pattern createSequencePattern = Pattern.compile(
            "create sequence ([a-zA-Z_]+);", Pattern.CASE_INSENSITIVE);

    private static final Pattern createTablePattern = Pattern.compile(
            "create table ([a-zA-Z_]+)\\s*\\(", Pattern.CASE_INSENSITIVE);

    private final List<String> sequenceNames = new ArrayList<>();
    private final List<String> tableNames = new ArrayList<>();

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

                Matcher matcher = createSequencePattern.matcher(line);
                if ( matcher.matches() ){
                    String seqName = matcher.group(1);
                    sequenceNames.add(seqName);
                }

                matcher = createTablePattern.matcher(line);
                if ( matcher.matches()){
                    String tableName = matcher.group(1);
                    tableNames.add(tableName);
                }

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

    public void clearTable(String tableName){
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            statement.execute("delete from " + tableName);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void dropSchema(){
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            for(String sequence : sequenceNames){
                statement.execute("drop sequence " + sequence);
            }
            for(String table : tableNames){
                statement.execute("drop table " + table);
            }

            Path path = Paths.get("./target/db/" + schemaName + ".mv.db");
            Files.deleteIfExists(path);
            path = Paths.get("./target/db/" + schemaName + ".trace.db");
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
                advanceSequences();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void advanceSequences(){
        Random random = new Random();
        try {
            Connection connection = connect();
            for(String sequenceName : sequenceNames){
                int count = random.nextInt(100) + 1;
                for( int idx=0; idx<count; idx++) {
                    Statement statement = connection.createStatement();
                    String sql = "select nextval('" + sequenceName + "')";
                    statement.execute(sql);
                }
            }
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
