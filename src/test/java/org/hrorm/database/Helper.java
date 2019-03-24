package org.hrorm.database;

import java.sql.Connection;

public interface Helper {
    String readSchema();

    Connection connect();

    void clearTables();

    void clearTable(String tableName);

    void dropSchema();

    void initializeSchema();

    void initializeSchemaFromSql(String sql);

    void advanceSequences();
}
