package org.hrorm.database;

import lombok.Data;

@Data
public class Constraint {

    private final String tableName;
    private final String constraintName;

}
