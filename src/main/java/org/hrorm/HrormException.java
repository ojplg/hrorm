package org.hrorm;

import java.sql.SQLException;
import java.util.Optional;

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

    public Optional<String> getSql(){
        return sql;
    }


}
