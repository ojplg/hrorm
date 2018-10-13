package org.hrorm;

import org.hrorm.examples.Child;
import org.hrorm.examples.ParentChildBuilders;
import org.hrorm.util.SimpleSqlFormatter;
import org.junit.Test;

public class SqlBuilderTest {

    @Test
    public void selectParentIds(){

        String expected = "select id from child_table where parent_id = ?";

        SqlBuilder<Child> sqlBuilder = new SqlBuilder<>(ParentChildBuilders.ChildDaoBuilder);

        String sql = sqlBuilder.selectChildIds("parent_id");

        SimpleSqlFormatter.assertEqualSql(expected, sql);

    }

    @Test
    public void testDeleteSql(){

        String expected = "delete from child_table where id = ?";

        SqlBuilder<Child> sqlBuilder = new SqlBuilder<>(ParentChildBuilders.ChildDaoBuilder);

        String sql = sqlBuilder.delete();

        SimpleSqlFormatter.assertEqualSql(expected, sql);

    }
}
