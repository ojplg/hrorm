package org.hrorm;

import java.sql.SQLException;
import java.util.Optional;

/**
 * A {@link RuntimeException} that wraps {@link SQLException}.
 *
 * <br/><br/>
 *
 * Hrorm does not declare checked exceptions in any methods
 * or interfaces that are intended to be used by clients.
 * Nevertheless, it may be a good idea to catch this exception
 * at strategic points in your application.
 */
public class HrormException extends RuntimeException {

    private final SQLException sqlException;
    private final Optional<String> sql;

    public HrormException(SQLException sqlException){
        super(sqlException);
        this.sqlException = sqlException;
        this.sql = Optional.empty();
    }

    public HrormException(SQLException sqlException, String sql){
        super(sqlException);
        this.sqlException = sqlException;
        this.sql = Optional.of(sql);
    }

    public SQLException getSqlException(){
        return sqlException;
    }

    /**
     * The SQL that was executed that caused the exception.
     *
     * @return the SQL, probably with numerous "?" symbols, as is common
     * for prepared statements
     */
    public Optional<String> getSql(){
        return sql;
    }


}
