package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Representation of a SQL where clause: a possibly nested list of
 * predicates that describes which records in the database to match.
 */
public class Where implements Iterable<WherePredicate>, StatementPopulator {

    public static final Where EMPTY = new Where();

    public static Where where(String columnName, Operator operator, Boolean value){
        return new Where(columnName, operator, value);
    }

    public static Where where(String columnName, Operator operator, Long value) {
        return new Where(columnName, operator, value);
    }

    public static Where where(String columnName, Operator operator, String value){
        return new Where(columnName, operator, value);
    }

    public static Where where(String columnName, Operator operator, BigDecimal value){
        return new Where(columnName, operator, value);
    }

    public static Where where(String columnName, Operator operator, LocalDateTime value){
        return new Where(columnName, operator, value);
    }

    public static Where where(Where subWhere){
        return new Where(subWhere);
    }

    private final WherePredicateTree tree;

    private Where(){
        this.tree = WherePredicateTree.EMPTY;
    }

    public Where(Where subWhere){
        WherePredicateTree.WherePredicateGroup group = new WherePredicateTree.WherePredicateGroup(subWhere.getRootNode());
        this.tree = new WherePredicateTree(group);
    }

    public Where(WherePredicate atom){
        tree = new WherePredicateTree(atom);
    }

    public Where(String columnName, Operator operator, Boolean value) {
        this(WherePredicate.forBoolean(columnName, operator, value));
    }

    public Where(String columnName, Operator operator, Long value) {
        this(WherePredicate.forLong(columnName, operator, value));
    }

    public Where(String columnName, Operator operator, String value) {
        this(WherePredicate.forString(columnName, operator, value));
    }

    public Where(String columnName, Operator operator, BigDecimal value) {
        this(WherePredicate.forBigDecimal(columnName, operator, value));
    }

    public Where(String columnName, Operator operator, LocalDateTime value) {
        this(WherePredicate.forLocalDateTime(columnName, operator, value));
    }

    public Where and(Where where){
        tree.addNode(WherePredicateTree.Conjunction.AND, where.getRootNode());
        return this;
    }

    public Where and(String columnName, Operator operator, Long value){
        WherePredicate<Long> atom = WherePredicate.forLong(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    public Where and(String columnName, Operator operator, BigDecimal value){
        WherePredicate<BigDecimal> atom = WherePredicate.forBigDecimal(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    public Where and(String columnName, Operator operator, String value){
        WherePredicate<String> atom = WherePredicate.forString(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    public Where and(String columnName, Operator operator, LocalDateTime value){
        WherePredicate<LocalDateTime> atom = WherePredicate.forLocalDateTime(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    public Where and(String columnName, Operator operator, Boolean value){
        WherePredicate<Boolean> atom = WherePredicate.forBoolean(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    public Where or(String columnName, Operator operator, Long value){
        WherePredicate<Long> atom = WherePredicate.forLong(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    public Where or(String columnName, Operator operator, BigDecimal value){
        WherePredicate<BigDecimal> atom = WherePredicate.forBigDecimal(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    public Where or(String columnName, Operator operator, String value){
        WherePredicate<String> atom = WherePredicate.forString(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    public Where or(String columnName, Operator operator, Boolean value){
        WherePredicate<Boolean> atom = WherePredicate.forBoolean(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    public Where or(String columnName, Operator operator, LocalDateTime value){
        WherePredicate<LocalDateTime> atom = WherePredicate.forLocalDateTime(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    public Where isNull(String columnName){
        return this;
    }

    public Where isNotNull(String columnName){
        return this;
    }

    public String render(){
        return tree.render("a.");
    }

    public WherePredicateTree.WherePredicateNode getRootNode(){
        return tree.getRootNode();
    }

    @Override
    public Iterator<WherePredicate> iterator() {
        return tree.asList().iterator();
    }

    @Override
    public void populate(PreparedStatement preparedStatement) throws SQLException {
        int idx = 1;
        for(WherePredicate atom : this){
            atom.setValue(idx, preparedStatement);
            idx++;
        }
    }
}
