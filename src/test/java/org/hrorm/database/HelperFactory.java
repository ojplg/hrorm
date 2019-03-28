package org.hrorm.database;

public class HelperFactory {

    public static Helper forSchema(String schemaName){
//        return h2Helper(schemaName);
        return postgresHelper(schemaName);
    }

    public static Helper h2Helper(String schemaName){
        return new H2Helper(schemaName);
    }

    public static Helper postgresHelper(String schemaName){
        return new PostgresHelper(schemaName);
    }
}

