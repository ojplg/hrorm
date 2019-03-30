package org.hrorm.database;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Helper {
    String readSchema();

    Connection connect();

    void clearTables();

    void clearTable(String tableName);

    void dropSchema();

    void initializeSchema();

    void initializeSchemaFromSql(String sql);

    void advanceSequences();

    void useConnection(Consumer<Connection> consumer);

    <T> T useConnection(Function<Connection, T> function);
}
