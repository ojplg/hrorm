package org.hrorm.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractHelper implements Helper {

    protected final String schemaName;
    private boolean initialized = false;

    private static final Pattern createSequencePattern = Pattern.compile(
            "create sequence ([a-zA-Z_]+);", Pattern.CASE_INSENSITIVE);

    private static final Pattern createTablePattern = Pattern.compile(
            "create table ([a-zA-Z_]+)\\s*\\(", Pattern.CASE_INSENSITIVE);

    protected final List<String> sequenceNames = new ArrayList<>();
    protected final List<String> tableNames = new ArrayList<>();

    protected AbstractHelper(String schemaName){
        this.schemaName = schemaName;
    }

    @Override
    public String readSchema() {
        try {
            InputStream inputStream = this.getClass().getResourceAsStream("/schemas/" + schemaName + ".sql");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder wholeFileBuffer = new StringBuilder();
            bufferedReader.lines().forEach( line -> {
                //extractNameFromLine(line);

                wholeFileBuffer.append(line);
                wholeFileBuffer.append("\n");
            });
            return wholeFileBuffer.toString();
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private void extractNameFromLine(String line){
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

    }

    @Override
    public void clearTables() {
        tableNames.forEach(this::clearTable);
    }

    @Override
    public void clearTable(String tableName) {
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            statement.execute("delete from " + tableName);
            connection.commit();
            connection.close();
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void initializeSchemaFromSql(String sql) {
        if ( ! initialized ) {
            try {
                Connection connection = connect();
                Statement statement = connection.createStatement();
                Arrays.asList(sql.split("\n")).stream().forEach(l -> extractNameFromLine(l));
                statement.execute(sql);
                connection.commit();
                connection.close();
                initialized = true;
                advanceSequences();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void initializeSchema() {
        String sql = readSchema();
        initializeSchemaFromSql(sql);
    }

    @Override
    public void advanceSequences() {
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
            connection.commit();
            connection.close();
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
