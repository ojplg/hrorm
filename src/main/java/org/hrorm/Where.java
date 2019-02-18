package org.hrorm;

import java.math.BigDecimal;

public class Where {

    private WherePredicateTree tree;

    public static Where where(String columnName, Operator operator, Long value) {
        WherePredicateAtom<Long> atom = new WherePredicateAtom<>(columnName, operator, value);
        Where where = new Where(atom);
        return where;
    }

    public static Where where(String columnName, Operator operator, String value){
        throw new UnsupportedOperationException();
    }

    public Where(WherePredicateAtom atom){
        tree = new WherePredicateTree(atom);
    }

    public Where and(String columnName, Operator operator, Long value){
        WherePredicateAtom<Long> atom = new WherePredicateAtom<>(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atom);
        return this;
    }

    public Where or(String columnName, Operator operator, Long value){
        WherePredicateAtom<Long> atom = new WherePredicateAtom<>(columnName, operator, value);
        tree.addAtom(WherePredicateTree.Conjunction.OR, atom);
        return this;
    }

    public Where or(String columnName, Operator operator, BigDecimal value){
        return this;
    }

    public Where and(Where where){
        tree.addNode(WherePredicateTree.Conjunction.AND, where.getRootNode());
        return this;
    }

    public String render(){
        return tree.render("A.");
    }

    public WherePredicateTree.WherePredicateNode getRootNode(){
        return tree.getRootNode();
    }
}
