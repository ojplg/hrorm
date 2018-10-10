package org.hrorm;

import java.sql.SQLException;
import java.util.Optional;

/**
 * A {@link RuntimeException} that wraps {@link SQLException}.
 *
 * <p>
 *
 * Hrorm does not declare checked exceptions in any methods
 * or interfaces that are intended to be used by clients.
 * Nevertheless, it may be a good idea to catch this exception
 * at strategic points in your application.
 */
public class HrormException extends RuntimeException {

    private final Optional<SQLException> sqlException;
    private final Optional<String> sql;

    public HrormException(String message){
        super(message);
        this.sqlException = Optional.empty();
        this.sql = Optional.empty();
    }

    public HrormException(SQLException sqlException){
        super(sqlException);
        this.sqlException = Optional.of(sqlException);
        this.sql = Optional.empty();
    }

    public HrormException(SQLException sqlException, String sql){
        super(sqlException);
        this.sqlException = Optional.of(sqlException);
        this.sql = Optional.of(sql);
    }

    public Optional<SQLException> getSqlException(){
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

    @Override
    public String getMessage() {
        if ( sql.isPresent() ) {
            return super.getMessage() + " on sql '" + sql.get() + "'";
        } else {
            return super.getMessage();
        }
    }
}
