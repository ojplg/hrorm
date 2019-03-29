package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenericColumn<TYPE> {

    private final int sqlType;
    private final PreparedStatementSetter<TYPE> preparedStatementSetter;
    private final ResultSetReader<TYPE> resultReader;

    public GenericColumn(PreparedStatementSetter<TYPE> preparedStatementSetter, ResultSetReader<TYPE> resultReader, int sqlType){
        this.sqlType = sqlType;
        this.preparedStatementSetter = preparedStatementSetter;
        this.resultReader = resultReader;
    }

    TYPE fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
        return resultReader.read(resultSet, columnName);
    }

    void setPreparedStatement(PreparedStatement preparedStatement, int index, TYPE value) throws SQLException {
        preparedStatementSetter.apply(preparedStatement, index, value);
    }

    int sqlType() {
        return sqlType;
    }

}
