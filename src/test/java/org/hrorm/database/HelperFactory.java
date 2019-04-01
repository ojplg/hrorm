package org.hrorm.database;

import static org.hrorm.database.HelperFactory.DatabasePlatform.H2;
import static org.hrorm.database.HelperFactory.DatabasePlatform.Postgres;

public class HelperFactory {

    enum DatabasePlatform { H2, Postgres }

    public static DatabasePlatform CURRENT_PLATFORM = H2;

    public static Helper forSchema(String schemaName){
        if( CURRENT_PLATFORM == H2 ) {
            return h2Helper(schemaName);
        } else if (CURRENT_PLATFORM == Postgres) {
            return postgresHelper(schemaName);
        }
        throw new RuntimeException("What plaform is this: " + CURRENT_PLATFORM);
    }

    public static Helper h2Helper(String schemaName){
        return new H2Helper(schemaName);
    }

    public static Helper postgresHelper(String schemaName){
        return new PostgresHelper(schemaName);
    }
}

