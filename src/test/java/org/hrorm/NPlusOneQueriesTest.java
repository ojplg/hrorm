package org.hrorm;

import org.hrorm.examples.SimpleParent;
import org.hrorm.examples.SimpleParentChildDaos;
import org.hrorm.examples.join_with_children.DaoBuilders;
import org.hrorm.examples.join_with_children.Pod;
import org.hrorm.examples.join_with_children.Stem;
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
import static org.mockito.internal.verification.VerificationModeFactory.times;

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
        Mockito.when(parentResultSet.getLong("aid")).thenReturn(1L).thenReturn(1L).thenReturn(2L).thenReturn(2L);
        Mockito.when(parentResultSet.getString("aname")).thenReturn("one").thenReturn("two");

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

        List<SimpleParent> parents = dao.select();
        Assert.assertNotNull(parents);
        Assert.assertEquals(2, parents.size());

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

        List<SimpleParent> parents = dao.select(where);
        Assert.assertNotNull(parents);
        Assert.assertEquals(2, parents.size());

        InOrder parentResultSetOrder = Mockito.inOrder(parentResultSet);
        parentResultSetOrder.verify(parentResultSet, Mockito.calls(3)).next();

        Mockito.verify(connection).prepareStatement(selectSql);
        Mockito.verify(connection).prepareStatement(childSelectSql);
    }

    @Test
    public void testMakesTwoQueriesForChildrenOfJoinedEntity() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement stemStatement = Mockito.mock(PreparedStatement.class);
        ResultSet stemResultSet = Mockito.mock(ResultSet.class);

        PreparedStatement peaStatement = Mockito.mock(PreparedStatement.class);
        ResultSet peaResultSet = Mockito.mock(ResultSet.class);

        DaoBuilder<Pod> podDaoBuilder = DaoBuilders.basePodDaoBuilder();
        podDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.ByKeysInClause);
        DaoBuilder<Stem> stemDaoBuilder = DaoBuilders.baseStemDaoBuilder(podDaoBuilder);
        stemDaoBuilder.withChildSelectStrategy(ChildSelectStrategy.ByKeysInClause);

        Dao<Stem> stemDao = stemDaoBuilder.buildDao(connection);

        String stemSelect = "select a.id as aid, a.tag as atag, b.id as bid, b.mark as bmark from stem a LEFT JOIN pod b ON a.pod_id=b.id where a.tag LIKE ? ";
        String peaSelect = "select a.id as aid, a.pod_id as apod_id, a.flag as aflag from pea a where a.pod_id IN ( ?, ? ) ";

        Mockito.when(connection.prepareStatement(stemSelect)).thenReturn(stemStatement);
        Mockito.when(stemStatement.executeQuery()).thenReturn(stemResultSet);

        Mockito.when(stemResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(stemResultSet.getLong("aid")).thenReturn(1L).thenReturn(2L);
        Mockito.when(stemResultSet.getLong("bid")).thenReturn(11L).thenReturn(12L);
        Mockito.when(stemResultSet.getString("atag")).thenReturn("one").thenReturn("two");
        Mockito.when(stemResultSet.getString("bmark")).thenReturn("eleven").thenReturn("twelve");

        Mockito.when(connection.prepareStatement(peaSelect)).thenReturn(peaStatement);
        Mockito.when(peaStatement.executeQuery()).thenReturn(peaResultSet);

        Where where = where("tag", LIKE, "%silly%");
        List<Stem> parents = stemDao.select(where);
        Assert.assertEquals(2, parents.size());

        // MAYBE: Investigate why this is 4. Should be 2.
        Mockito.verify(stemResultSet, times(4)).getLong("aid");
        Mockito.verify(stemResultSet, times(2)).getLong("bid");
        Mockito.verify(stemResultSet, times(2)).getString("atag");
        Mockito.verify(stemResultSet, times(2)).getString("bmark");

        Mockito.verify(connection).prepareStatement(stemSelect);
        Mockito.verify(connection).prepareStatement(peaSelect);
    }

}
