package org.hrorm;

import org.hrorm.util.SimpleSqlFormatter;
import org.junit.Test;

public class WherePredicateTreeTest {

    @Test
    public void onePredicateTest(){
        WherePredicate<Long> atom = WherePredicate.forLong("COLUMN", Operator.LESS_THAN, 5L);
        WherePredicateTree tree = new WherePredicateTree(atom);

        String generatedSql = tree.render("A.");
        String expectedSql = "A.COLUMN < ?";
        SimpleSqlFormatter.assertEqualSql(expectedSql, generatedSql);
    }

    @Test
    public void twoPredicateTest(){
        WherePredicate<Long> atomLeft = WherePredicate.forLong("COLUMN", Operator.LESS_THAN, 5L);
        WherePredicateTree tree = new WherePredicateTree(atomLeft);
        WherePredicate<Long> atomRight = WherePredicate.forLong("COLUMN", Operator.GREATER_THAN, 0L);
        tree.addAtom(WherePredicateTree.Conjunction.AND, atomRight);

        String generatedSql = tree.render("A.");
        String expectedSql = "A.COLUMN < ? AND A.COLUMN > ?";
        SimpleSqlFormatter.assertEqualSql(expectedSql, generatedSql);
    }
}
