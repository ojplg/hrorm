package org.hrorm;

import java.math.BigDecimal;
import java.util.Iterator;

/**
 * Representation of a SQL where clause: a possibly nested list of
 * predicates that describes which records in the database to match.
 */
public class Where implements Iterable<WherePredicateAtom> {

    public static Where where(String columnName, Operator operator, Long value) {
        WherePredicateAtom<Long> atom = WherePredicateAtom.forLong(columnName, operator, value);
        return new Where(atom);
    }

    public static Where where(String columnName, Operator operator, String value){
        WherePredicateAtom<String> atom = WherePredicateAtom.forString(columnName, operator, value);
        return new Where(atom);
    }

    public static Where where(String columnName, Operator operator, BigDecimal value){
        WherePredicateAtom<BigDecimal> atom = WherePredicateAtom.forBigDecimal(columnName, operator, value);
        return new Where(atom);
    }

    private WherePredicateTree tree;

    public Where(WherePredicateAtom atom){
        tree = new WherePredicateTree(atom);
    }

    public Where and(String columnName, Operator operator, Long value){
        WherePredicateAtom<Long> atom = WherePredicateAtom.forLong(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    public Where and(String columnName, Operator operator, BigDecimal value){
        WherePredicateAtom<BigDecimal> atom = WherePredicateAtom.forBigDecimal(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    public Where and(String columnName, Operator operator, String value){
        WherePredicateAtom<String> atom = WherePredicateAtom.forString(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }


    public Where or(String columnName, Operator operator, Long value){
        WherePredicateAtom<Long> atom = WherePredicateAtom.forLong(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    public Where or(String columnName, Operator operator, BigDecimal value){
        WherePredicateAtom<BigDecimal> atom = WherePredicateAtom.forBigDecimal(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    public Where and(Where where){
        tree.addNode(WherePredicateTree.Conjunction.AND, where.getRootNode());
        return this;
    }

    public String render(){
        return tree.render("a.");
    }

    public WherePredicateTree.WherePredicateNode getRootNode(){
        return tree.getRootNode();
    }

    @Override
    public Iterator<WherePredicateAtom> iterator() {
        return tree.asAtomList().iterator();
    }
}
