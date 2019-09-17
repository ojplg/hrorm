package org.hrorm;

import org.hrorm.examples.parentage.Child;
import org.hrorm.examples.parentage.Parent;
import org.hrorm.examples.parentage.ParentChildBuilders;
import org.hrorm.util.SimpleSqlFormatter;
import org.junit.Test;

import static org.hrorm.Operator.LIKE;
import static org.hrorm.Where.where;

public class SqlBuilderTest {

    @Test
    public void selectParentIds(){

        String expected = "select id from child_table where parent_table_id = ?";

        SqlBuilder<Child> sqlBuilder = new SqlBuilder<>(ParentChildBuilders.ChildDaoBuilder);

        String sql = sqlBuilder.selectChildIds();

        SimpleSqlFormatter.assertEqualSql(expected, sql);

    }

    @Test
    public void testDeleteSql(){

        String expected = "delete from child_table where id = ?";

        SqlBuilder<Child> sqlBuilder = new SqlBuilder<>(ParentChildBuilders.ChildDaoBuilder);

        String sql = sqlBuilder.delete();

        SimpleSqlFormatter.assertEqualSql(expected, sql);

    }

    @Test
    public void testSelectByParentSubselect(){

        String expected = "select a.id as aid, a.parent_table_id as aparent_table_id, a.number as anumber " +
                " from child_table a " +
                " where a.parent_table_id in (select id from parent_table where name like ?)";

        SqlBuilder<Parent> parentSqlBuilder = new SqlBuilder<>(ParentChildBuilders.ParentDaoBuilder);

        String parentSelect = parentSqlBuilder.selectPrimaryKey(where("name", LIKE, "%foo%"));

        SqlBuilder<Child> childSqlBuilder = new SqlBuilder<>(ParentChildBuilders.ChildDaoBuilder);

        String sql = childSqlBuilder.selectByParentSubselect(parentSelect);

        SimpleSqlFormatter.assertEqualSql(expected, sql);
    }
}
