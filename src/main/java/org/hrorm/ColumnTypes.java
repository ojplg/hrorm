package org.hrorm;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Container for some static data regarding what <code>Column</code> implementations are
 * suitable for what SQL types.
 *
 * <p>
 *     The {@link java.sql.Types} class enumerates all the SQL types supported by the
 *     JDBC. These types are exposed on the {@link java.sql.ResultSetMetaData} class.
 *     By inspecting these values, hrorm can attempt to determine if the types of the
 *     database are correct for the mappings it intends to do.
 * </p>
 */
public class ColumnTypes {

    public static final Set<Integer> BooleanTypes =
            Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(
                            Types.INTEGER,
                            Types.BIGINT,
                            Types.SMALLINT,
                            Types.BIT,
                            Types.BOOLEAN,
                            Types.NUMERIC
                    )));

    public static final Set<Integer> IntegerTypes =
            Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(
                          Types.INTEGER,
                          Types.BIGINT,
                          Types.SMALLINT,
                          Types.TINYINT
                    )));


    public static final Set<Integer> DecimalTypes =
            Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(
                            Types.DECIMAL,
                            Types.DOUBLE,
                            Types.FLOAT,
                            Types.REAL,
                            Types.NUMERIC
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

    public static final Set<Integer> InstantTypes =
            Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(
                            Types.DATE,
                            Types.TIME,
                            Types.TIMESTAMP
                    )));

}
