package org.hrorm;

import org.hrorm.examples.SimpleParent;
import org.hrorm.examples.SimpleParentChildDaos;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.hrorm.Operator.LIKE;
import static org.hrorm.Where.where;

public class NPlusOneQueriesTest {

    @Test
    public void testMakesOnlyTwoQueries() throws SQLException {

        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement parentStatement = Mockito.mock(PreparedStatement.class);
        ResultSet parentResultSet = Mockito.mock(ResultSet.class);

        PreparedStatement childStatement = Mockito.mock(PreparedStatement.class);
        ResultSet childResultSet = Mockito.mock(ResultSet.class);

        Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT_IN_CLAUSE_STRATEGY.buildDao(connection);
        Queries parentQueries = dao.queries();
        Where where = where("NAME", LIKE, "%silly%");

        String selectSql = parentQueries.select(where);

        Mockito.when(connection.prepareStatement(selectSql)).thenReturn(parentStatement);
        Mockito.when(parentStatement.executeQuery()).thenReturn(parentResultSet);

        Queries childQueries = SimpleParentChildDaos.CHILD.buildQueries();
        Where childWhere = Where.inLong("parent_id", Arrays.asList(1L, 2L));
        String childSelectSql = childQueries.select(childWhere);

        Mockito.when(connection.prepareStatement(childSelectSql)).thenReturn(childStatement);
        Mockito.when(childStatement.executeQuery()).thenReturn(childResultSet);

        Mockito.when(parentResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(parentResultSet.getLong("id")).thenReturn(1L).thenReturn(2L);
        Mockito.when(parentResultSet.getString("name")).thenReturn("one").thenReturn("two");

        List<SimpleParent> parents = dao.select(where);
        Assert.assertNotNull(parents);
        Assert.assertEquals(2, parents.size());

        InOrder parentResultSetOrder = Mockito.inOrder(parentResultSet);
        parentResultSetOrder.verify(parentResultSet, Mockito.calls(3)).next();

        Mockito.verify(connection).prepareStatement(selectSql);
        Mockito.verify(connection).prepareStatement(childSelectSql);
    }

    @Test
    public void testMakesUnqualifiedQueryOfChildTableWhenAppropriate() throws SQLException {

        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement parentStatement = Mockito.mock(PreparedStatement.class);
        ResultSet parentResultSet = Mockito.mock(ResultSet.class);

        PreparedStatement childStatement = Mockito.mock(PreparedStatement.class);
        ResultSet childResultSet = Mockito.mock(ResultSet.class);

        Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT_IN_CLAUSE_STRATEGY.buildDao(connection);
        Queries parentQueries = dao.queries();

        String selectSql = parentQueries.select();

        Mockito.when(connection.prepareStatement(selectSql)).thenReturn(parentStatement);
        Mockito.when(parentStatement.executeQuery()).thenReturn(parentResultSet);

        Queries childQueries = SimpleParentChildDaos.CHILD.buildQueries();
        String childSelectSql = childQueries.select();

        Mockito.when(connection.prepareStatement(childSelectSql)).thenReturn(childStatement);
        Mockito.when(childStatement.executeQuery()).thenReturn(childResultSet);

        Mockito.when(parentResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(parentResultSet.getLong("id")).thenReturn(1L).thenReturn(2L);
        Mockito.when(parentResultSet.getString("name")).thenReturn("one").thenReturn("two");

        try {
            List<SimpleParent> parents = dao.select();
            Assert.assertNotNull(parents);
            Assert.assertEquals(2, parents.size());
        } catch (Exception ex){
            ex.printStackTrace();
        }

        InOrder parentResultSetOrder = Mockito.inOrder(parentResultSet);
        parentResultSetOrder.verify(parentResultSet, Mockito.calls(3)).next();

        Mockito.verify(connection).prepareStatement(selectSql);
        Mockito.verify(connection).prepareStatement(childSelectSql);
    }

    @Test
    public void testSubselectMakesTwoQueries() throws SQLException {

        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement parentStatement = Mockito.mock(PreparedStatement.class);
        ResultSet parentResultSet = Mockito.mock(ResultSet.class);

        PreparedStatement childStatement = Mockito.mock(PreparedStatement.class);
        ResultSet childResultSet = Mockito.mock(ResultSet.class);

        Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT_SUBSELECT_STRATEGY.buildDao(connection);
        Queries parentQueries = dao.queries();
        Where where = where("NAME", LIKE, "%silly%");

        String selectSql = parentQueries.select(where);

        Mockito.when(connection.prepareStatement(selectSql)).thenReturn(parentStatement);
        Mockito.when(parentStatement.executeQuery()).thenReturn(parentResultSet);

        Queries childQueries = SimpleParentChildDaos.CHILD.buildQueries();
        String childSelectSql = childQueries.select() + " where a.parent_id in (select id from simple_parent_table where NAME LIKE ? )";

        Mockito.when(connection.prepareStatement(childSelectSql)).thenReturn(childStatement);
        Mockito.when(childStatement.executeQuery()).thenReturn(childResultSet);

        Mockito.when(parentResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(parentResultSet.getLong("id")).thenReturn(1L).thenReturn(2L);
        Mockito.when(parentResultSet.getString("name")).thenReturn("one").thenReturn("two");

        try {
            List<SimpleParent> parents = dao.select(where);
            Assert.assertNotNull(parents);
            Assert.assertEquals(2, parents.size());
        } catch (Exception ex){
            ex.printStackTrace();
        }

        InOrder parentResultSetOrder = Mockito.inOrder(parentResultSet);
        parentResultSetOrder.verify(parentResultSet, Mockito.calls(3)).next();

        Mockito.verify(connection).prepareStatement(selectSql);
        Mockito.verify(connection).prepareStatement(childSelectSql);
    }

}