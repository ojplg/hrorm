package org.hrorm;

import org.hrorm.util.SimpleSqlFormatter;
import org.junit.Assert;
import org.junit.Test;

import static org.hrorm.Where.where;

public class WhereTest {

    @Test
    public void testRenderSimplePredicate(){

        Where where = where("COLUMN", Operator.EQUALS, 52L);

        String generatedSql = where.render();
        String expectedSql = "WHERE A.COLUMN = ?";

        SimpleSqlFormatter.assertEqualSql(expectedSql, generatedSql);
    }

    @Test
    public void testWithSubClause(){
        Where where = where("NUMBER", Operator.GREATER_THAN, 3L)
                        .and("NUMBER", Operator.LESS_THAN, 82L)
                        .and( where("OTHER", Operator.EQUALS, 14L )
                                .or("THIRD", Operator.GREATER_THAN, 23L));

        String generatedSql = where.render();
        String expectedSql = "WHERE A.NUMBER > ? AND A.NUMBER < ? "
                                + "AND ( A.OTHER = ? OR A.THIRD > ? )";

        SimpleSqlFormatter.assertEqualSql(expectedSql, generatedSql);
    }

    @Test
    public void testSubClauseFirst(){
        Where where = where(where("FOO", Operator.EQUALS, 1L)
                                .and("BAR", Operator.LESS_THAN_OR_EQUALS, 2L))
                        .or("BAZ", Operator.LIKE, "baz");


        String generatedSql = where.render();
        String expectedSql = "WHERE ( A.FOO = ? AND A.BAR <= ? )"
                + "OR A.BAZ LIKE ? ";

        SimpleSqlFormatter.assertEqualSql(expectedSql, generatedSql);
    }

    @Test
    public void testCannotExtendEmptyWhereClause_WithOr(){
        Where where = new Where();

        try {
            where.or("FOO", Operator.EQUALS, 7L);
            Assert.fail("Should not extend an empty where clause");
        } catch (HrormException expected){
        }
    }

    @Test
    public void testCannotExtendEmptyWhereClause_WithAnd(){
        Where where = new Where();

        try {
            where.and("FOO", Operator.LIKE, "FOO");
            Assert.fail("Should not extend an empty where clause");
        } catch (HrormException expected){
        }
    }

}
