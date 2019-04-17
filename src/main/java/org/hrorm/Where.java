package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Representation of a SQL where clause: a possibly nested list of
 * predicates that describes which records in the database to match.
 */
public class Where implements StatementPopulator {

    private static final PreparedStatementSetter<Instant> INSTANT_SETTER =
            (preparedStatement, index, value) ->
            {
                Timestamp timestamp = Converters.INSTANT_TIMESTAMP_CONVERTER.from(value);
                preparedStatement.setTimestamp(index, timestamp);
            };

    /**
     * Factory method equivalent to <code>new Where()</code>
     *
     * @return the new object
     */
    public static Where where(){ return new Where(); }

    /**
     * Factory method equivalent to <code>new Where(subWhere)</code>.
     *
     * @param subWhere an existing predicate that will be grouped parenthetically
     *                 within the new object
     * @return the new object
     */
    public static Where where(Where subWhere){
        return new Where(subWhere);
    }

    /**
     * Factory method equivalent to <code>new Where(columnName, operator, value)</code>.
     * Creates a new <code>Where</code> object containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return the new object
     */
    public static Where where(String columnName, Operator operator, Boolean value){
        return new Where(columnName, operator, value);
    }

    /**
     * Factory method equivalent to <code>new Where(columnName, operator, value)</code>.
     * Creates a new <code>Where</code> object containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return the new object
     */
    public static Where where(String columnName, Operator operator, Long value) {
        return new Where(columnName, operator, value);
    }

    /**
     * Factory method equivalent to <code>new Where(columnName, operator, value)</code>.
     * Creates a new <code>Where</code> object containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return the new object
     */
    public static Where where(String columnName, Operator operator, String value){
        return new Where(columnName, operator, value);
    }

    /**
     * Factory method equivalent to <code>new Where(columnName, operator, value)</code>.
     * Creates a new <code>Where</code> object containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return the new object
     */
    public static Where where(String columnName, Operator operator, BigDecimal value){
        return new Where(columnName, operator, value);
    }

    /**
     * Factory method equivalent to <code>new Where(columnName, operator, value)</code>.
     * Creates a new <code>Where</code> object containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return the new object
     */
    public static Where where(String columnName, Operator operator, Instant value){
        return new Where(columnName, operator, value);
    }

    /**
     * Factory method equivalent to <code>new Where(columnName, operator, value, column)</code>.
     * Creates a new <code>Where</code> object containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @param column The descriptor of the column type
     * @param <T> The type supported by the column
     * @return the new object
     */
    public static <T> Where where(String columnName, Operator operator, T value, GenericColumn<T> column){
        return new Where(columnName, operator, value, column);
    }

    /**
     * Creates a new object with a single predicate testing whether
     * a column is null.
     *
     * @param columnName The column to test for a null value.
     * @return the new object
     */
    public static Where isNull(String columnName){
        WherePredicate atom = new WherePredicate(columnName, true);
        return new Where(atom);
    }

    /**
     * Creates a new object with a single predicate testing whether
     * a column is not null.
     *
     * @param columnName The column to test for a not null value.
     * @return the new object
     */
    public static Where isNotNull(String columnName){
        WherePredicate atom = new WherePredicate(columnName, false);
        return new Where(atom);
    }

    private final WherePredicateTree tree;

    /**
     * Creates an object with no filters.
     */
    public Where(){
        this.tree = WherePredicateTree.EMPTY;
    }

    /**
     * Creates a new <code>Where</code> instance that is grouped,
     * that is, when the SQL where clause is generated, whatever is
     * inside the passed where instance will be wrapped inside
     * parentheses.
     *
     * @param subWhere The predicates to be wrapped
     */
    public Where(Where subWhere){
        this.tree = new WherePredicateTree(subWhere.tree);
    }

    /**
     * Create a new instance of a <code>Where</code> object
     * containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     */
    public Where(String columnName, Operator operator, Boolean value) {
        this(WherePredicate.forBoolean(columnName, operator, value));
    }

    /**
     * Create a new instance of a <code>Where</code> object
     * containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     */
    public Where(String columnName, Operator operator, Long value) {
        this(WherePredicate.forLong(columnName, operator, value));
    }

    /**
     * Create a new instance of a <code>Where</code> object
     * containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     */
    public Where(String columnName, Operator operator, String value) {
        this(WherePredicate.forString(columnName, operator, value));
    }

    /**
     * Create a new instance of a <code>Where</code> object
     * containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     */
    public Where(String columnName, Operator operator, BigDecimal value) {
        this(WherePredicate.forBigDecimal(columnName, operator, value));
    }

    /**
     * Create a new instance of a <code>Where</code> object
     * containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     */
    public Where(String columnName, Operator operator, Instant value) {
        this(WherePredicate.forInstant(columnName, operator, value));
    }

    /**
     * Create a new instance of a <code>Where</code> object
     * containing one predicate.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @param <T> The type supported by the column
     * @param column The descriptor of the column type
     */
    public <T> Where(String columnName, Operator operator, T value, GenericColumn<T> column){
        this(WherePredicate.forGeneric(columnName, operator, value, column));
    }

    /**
     * Generates an <code>IN</code> clause applying to the specified column and
     * including the passed list of numbers.
     *
     * @param columnName The column name the <code>IN</code> clause applies to
     * @param elements The numbers that should be matched
     * @return the new object
     */
    public static Where inLong(String columnName, List<Long> elements){
        return new Where(columnName, GenericColumn.LONG::setPreparedStatement, elements, true);
    }

    /**
     * Generates an <code>IN</code> clause applying to the specified column and
     * including the passed list of strings.
     *
     * @param columnName The column name the <code>IN</code> clause applies to
     * @param elements The strings that should be matched
     * @return the new object
     */
    public static Where inString(String columnName, List<String> elements){
        return new Where(columnName, GenericColumn.STRING::setPreparedStatement, elements, true);
    }

    /**
     * Generates an <code>IN</code> clause applying to the specified column and
     * including the passed list of numbers.
     *
     * @param columnName The column name the <code>IN</code> clause applies to
     * @param elements The numbers that should be matched
     * @return the new object
     */
    public static Where inBigDecimal(String columnName, List<BigDecimal> elements){
        return new Where(columnName, GenericColumn.BIG_DECIMAL::setPreparedStatement, elements, true);
    }

    /**
     * Generates an <code>IN</code> clause applying to the specified column and
     * including the passed list of dates.
     *
     * @param columnName The column name the <code>IN</code> clause applies to
     * @param elements The dates that should be matched
     * @return the new object
     */
    public static Where inInstant(String columnName, List<Instant> elements){
        return new Where(columnName, INSTANT_SETTER, elements, true);
    }

    /**
     * Generates an <code>IN</code> clause applying to the specified column and
     * including the passed list of elements.
     *
     * @param columnName The column name the <code>IN</code> clause applies to
     * @param column A column supporting the <code>T</code> type
     * @param elements The elements that should be matched
     * @param <T> The type of element
     * @return the new object
     */
    public static <T> Where inColumn(String columnName, GenericColumn<T> column, List<T> elements){
        return new Where(columnName, column::setPreparedStatement, elements, true);
    }

    /**
     * Generates a <code>NOT IN</code> clause applying to the specified column and
     * excluding the passed list of numbers.
     *
     * @param columnName The column name the <code>NOT IN</code> clause applies to
     * @param elements The numbers that should be excluded
     * @return the new object
     */
    public static Where notInLong(String columnName, List<Long> elements){
        return new Where(columnName, GenericColumn.LONG::setPreparedStatement, elements, false);
    }

    /**
     * Generates a <code>NOT IN</code> clause applying to the specified column and
     * excluding the passed list of strings.
     *
     * @param columnName The column name the <code>NOT IN</code> clause applies to
     * @param elements The strings that should be excluded
     * @return the new object
     */
    public static Where notInString(String columnName, List<String> elements){
        return new Where(columnName, GenericColumn.STRING::setPreparedStatement, elements, false);
    }

    /**
     * Generates a <code>NOT IN</code> clause applying to the specified column and
     * excluding the passed list of numbers.
     *
     * @param columnName The column name the <code>NOT IN</code> clause applies to
     * @param elements The numbers that should be excluded
     * @return the new object
     */
    public static Where notInBigDecimal(String columnName, List<BigDecimal> elements){
        return new Where(columnName, GenericColumn.BIG_DECIMAL::setPreparedStatement, elements, false);
    }

    /**
     * Generates a <code>NOT IN</code> clause applying to the specified column and
     * excluding the passed list of dates.
     *
     * @param columnName The column name the <code>NOT IN</code> clause applies to
     * @param elements The dates that should be excluded
     * @return the new object
     */
    public static Where notInInstant(String columnName, List<Instant> elements){
        return new Where(columnName, INSTANT_SETTER, elements, false);
    }

    /**
     * Generates a <code>NOT IN</code> clause applying to the specified column and
     * excluding the passed list of elements.
     *
     * @param columnName The column name the <code>NOT IN</code> clause applies to
     * @param column A column supporting the <code>T</code> type
     * @param elements The elements that should be excluded
     * @param <T> The type of element
     * @return the new object
     */
    public static <T> Where notInColumn(String columnName, GenericColumn<T> column, List<T> elements){
        return new Where(columnName, column::setPreparedStatement, elements, false);
    }

    private <T> Where(String columnName, PreparedStatementSetter<T> setter, List<T> elements, boolean in){
        this(new WherePredicate(columnName, setter, elements, in));
    }

    private Where(WherePredicate atom){
        tree = new WherePredicateTree(atom);
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the passed argument with a logical and
     * operation. The passed object will be grouped parenthetically.
     *
     * @param subWhere the new predicate to add
     * @return this object
     */
    public Where and(Where subWhere){
        tree.addSubtree(WherePredicateTree.Conjunction.AND, subWhere.tree);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical and.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where and(String columnName, Operator operator, Long value){
        WherePredicate<Long> atom = WherePredicate.forLong(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical and.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where and(String columnName, Operator operator, BigDecimal value){
        WherePredicate<BigDecimal> atom = WherePredicate.forBigDecimal(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical and.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where and(String columnName, Operator operator, String value){
        WherePredicate<String> atom = WherePredicate.forString(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical and.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where and(String columnName, Operator operator, Instant value){
        WherePredicate<Instant> atom = WherePredicate.forInstant(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical and.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where and(String columnName, Operator operator, Boolean value){
        WherePredicate<Boolean> atom = WherePredicate.forBoolean(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical and.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @param column The descriptor of this column type
     * @param <T> The type supported by the column
     * @return this object
     */
    public <T> Where and(String columnName, Operator operator, T value, GenericColumn<T> column){
        WherePredicate<T> atom = WherePredicate.forGeneric(columnName, operator, value, column);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the passed argument with a logical or
     * operation. The passed object will be grouped parenthetically.
     *
     * @param subWhere the new predicate to add
     * @return this object
     */
    public Where or(Where subWhere){
        tree.addSubtree(WherePredicateTree.Conjunction.OR, subWhere.tree);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical or.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where or(String columnName, Operator operator, Long value){
        WherePredicate<Long> atom = WherePredicate.forLong(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical or.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where or(String columnName, Operator operator, BigDecimal value){
        WherePredicate<BigDecimal> atom = WherePredicate.forBigDecimal(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical or.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where or(String columnName, Operator operator, String value){
        WherePredicate<String> atom = WherePredicate.forString(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical or.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where or(String columnName, Operator operator, Boolean value){
        WherePredicate<Boolean> atom = WherePredicate.forBoolean(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical or.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @return this object
     */
    public Where or(String columnName, Operator operator, Instant value){
        WherePredicate<Instant> atom = WherePredicate.forInstant(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    /**
     * Add a new predicate to the existing object by connecting the
     * existing predicates to the new one with a logical or.
     *
     * @param columnName The column name the predicate applies to
     * @param operator The operation used to evaluate the predicate
     * @param value The value to compare the column values against
     * @param column The descriptor of this column type
     * @param <T> The type supported by the column
     * @return this object
     */
    public <T> Where or(String columnName, Operator operator, T value, GenericColumn<T> column){
        WherePredicate<T> atom = WherePredicate.forGeneric(columnName, operator, value, column);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    /**
     * The SQL that makes up the where clause this object represents.
     *
     * @return the SQL
     */
    public String render(){
        if ( tree.isEmpty() ){
            return "";
        }
        return " where " + tree.render("a.");
    }

    @Override
    public void populate(PreparedStatement preparedStatement) throws SQLException {
        int idx = 1;
        for(WherePredicate atom : this.tree.asList()){
            int cnt = atom.setValue(idx, preparedStatement);
            idx += cnt;
        }
    }
}
