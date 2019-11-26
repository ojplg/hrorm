package org.hrorm.database;

import org.hrorm.Transactor;
import org.hrorm.util.RandomUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractHelper implements Helper {

    protected final String schemaName;
    private boolean initialized = false;

    private static final Pattern createSequencePattern = Pattern.compile(
            "create sequence ([a-zA-Z_]+);", Pattern.CASE_INSENSITIVE);

    private static final Pattern createTablePattern = Pattern.compile(
            "create table ([a-zA-Z_]+)\\s*\\(", Pattern.CASE_INSENSITIVE);

    public static final Pattern constraintPattern = Pattern.compile(
            "alter table ([a-zA-Z_]+)\\s*add foreign key\\s*\\(([a-zA-Z_]+)\\).*", Pattern.CASE_INSENSITIVE);

    protected final List<String> sequenceNames = new ArrayList<>();
    protected final List<String> tableNames = new ArrayList<>();
    protected final List<Constraint> constraintNames = new ArrayList<>();

    protected AbstractHelper(String schemaName){
        this.schemaName = schemaName;
    }

    private final Transactor transactor = new Transactor(this::connect);
//    private String temporaryFileCopy(String fileName){
//        try {
//            Path pathIn = Paths.get("./schemas/" + fileName + ".sql");
//            String newFileName = fileName + "_" + RandomUtils.randomAlphabeticString(5,6);
//            Path pathOut = Paths.get("./target/db/" + fileName + ".sql");
//            Files.copy(pathIn,pathOut);
//            return newFileName;
//        } catch (IOException ex){
//            throw new RuntimeException(ex);
//        }
//    }

    @Override
    public String readSchema() {
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

        matcher = constraintPattern.matcher(line);
        if ( matcher.matches() ){
            String tableName = matcher.group(1);
            String constraintName = matcher.group(2);
            constraintNames.add(new Constraint(tableName, constraintName));
        }

    }

    @Override
    public void clearTables() {
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            for( String tableName : tableNames ) {
                statement.execute("delete from " + tableName);
            }
            connection.commit();
            connection.close();
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
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
                sql = filterSql(sql);
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
        Connection connection = null;
        try {
            connection = connect();
            for(String sequenceName : sequenceNames){
                int count = random.nextInt(100) + 1;
                for( int idx=0; idx<count; idx++) {
                    Statement statement = connection.createStatement();
                    String sql = "select nextval('" + sequenceName + "')";
                    statement.execute(sql);
                }
            }
            connection.commit();
        } catch (Exception ex){
            throw new RuntimeException(ex);
        } finally {
            try {
                if( connection != null ) {
                    connection.close();
                }
            } catch (SQLException ex){
                throw new RuntimeException(ex);
            }
        }
    }

    public String filterSql(String sql) {
        return sql;
    }

    public void useConnection(Consumer<Connection> consumer){
        transactor.runAndCommit(consumer);
    }

    public <T> T useConnection(Function<Connection, T> function){
        return transactor.runAndCommit(function);
    }

    @Override
    public List<String> tableNames() {
        return tableNames;
    }

    @Override
    public List<String> sequenceNames() {
        return sequenceNames;
    }

    @Override
    public List<Constraint> constraints() {
        return constraintNames;
    }
}
