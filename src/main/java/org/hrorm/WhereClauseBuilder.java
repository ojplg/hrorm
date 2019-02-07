package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class WhereClauseBuilder<ENTITY> {

    private interface WherePhrase {
        void setValue(int index, PreparedStatement statement) throws SQLException;
        String asSqlSnippet(String prefix);
    }

    private static class LongPhrase implements WherePhrase {
        private final String columnName;
        private final Long value;
        private final Operator operator;

        LongPhrase(String columnName, Operator operator, Long value){
            this.columnName = columnName;
            this.value = value;
            this.operator = operator;
        }

        @Override
        public void setValue(int index, PreparedStatement statement) throws SQLException {
            statement.setLong(index, value);
        }

        @Override
        public String asSqlSnippet(String prefix) {
            return "a." + columnName + operator.getSqlString("") + "?";
        }
    }

    private final List<WherePhrase> phrases = new ArrayList<>();
    private final String baseSelectStatement;
    private final Function<String, List<ENTITY>> selector;

    public WhereClauseBuilder(String baseSelectStatement, Function<String, List<ENTITY>> selector, String columnName, Operator operator, Long value){
        this.baseSelectStatement = baseSelectStatement;
        this.selector = selector;
        phrases.add(new LongPhrase(columnName, operator, value));
    }

    public WhereClauseBuilder and(String columnName, Operator operator, Long value){
        phrases.add(new LongPhrase(columnName, operator, value));
        return this;
    }

    public List<ENTITY> execute(){
        StringBuilder buf = new StringBuilder();
        buf.append(baseSelectStatement);
        for(int idx=0; idx<phrases.size(); idx++){
            WherePhrase phrase = phrases.get(idx);
            buf.append(phrase.asSqlSnippet("a."));
            if( idx<phrases.size() - 1){
                buf.append("and");
            }
        }
        String sql = buf.toString();

        return selector.apply(sql);
    }

    public <T> T fold(T identity, BiFunction<T,ENTITY,T> accumulator){
        return identity;
    }
}
