package org.hrorm;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ColumnTypes {

    public static final Set<Integer> IntegerTypes =
            Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(
                          Types.INTEGER,
                          Types.BIGINT,
                          Types.SMALLINT
                    )));


    public static final Set<Integer> DecimalTypes =
            Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(
                            Types.DECIMAL,
                            Types.DOUBLE,
                            Types.FLOAT,
                            Types.REAL
                    )));


    public static final Set<Integer> StringTypes =
            Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(
                            Types.CHAR,
                            Types.NVARCHAR,
                            Types.LONGNVARCHAR,
                            Types.LONGVARCHAR,
                            Types.VARCHAR,
                            Types.CLOB,
                            Types.NCLOB,
                            Types.BLOB
                    )));

    public static final Set<Integer> LocalDateTimeTypes =
            Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(
                            Types.DATE,
                            Types.TIME,
                            Types.TIMESTAMP
                    )));


}
