package org.hrorm;

import org.hrorm.util.SimpleSqlFormatter;
import org.junit.Test;

import static org.hrorm.Where.where;

public class WhereTest {

    @Test
    public void testRenderSimplePredicate(){

        Where where = where("COLUMN", Operator.EQUALS, 52L);

        String generatedSql = where.render();
        String expectedSql = "A.COLUMN = ?";

        SimpleSqlFormatter.assertEqualSql(expectedSql, generatedSql);
    }

    @Test
    public void testWithSubClause(){
        Where where = where("NUMBER", Operator.GREATER_THAN, 3L)
                        .and("NUMBER", Operator.LESS_THAN, 82L)
                        .and( where("OTHER", Operator.EQUALS, 14L )
                                .or("THIRD", Operator.GREATER_THAN, 23L));

        String generatedSql = where.render();
        String expectedSql = "A.NUMBER > ? AND A.NUMBER < ? "
                                + "AND ( A.OTHER = ? OR A.THIRD > ? )";

        SimpleSqlFormatter.assertEqualSql(expectedSql, generatedSql);
    }

    @Test
    public void testSubClauseFirst(){
        Where where = where(where("FOO", Operator.EQUALS, 1L)
                                .and("BAR", Operator.LESS_THAN_OR_EQUALS, 2L))
                        .or("BAZ", Operator.LIKE, "baz");


        String generatedSql = where.render();
        String expectedSql = "( A.FOO = ? AND A.BAR <= ? )"
                + "OR A.BAZ LIKE ? ";

        SimpleSqlFormatter.assertEqualSql(expectedSql, generatedSql);
    }
}
