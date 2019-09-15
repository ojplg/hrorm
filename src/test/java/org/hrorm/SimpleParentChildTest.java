package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.SimpleChild;
import org.hrorm.examples.SimpleParent;
import org.hrorm.examples.SimpleParentChildDaos;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hrorm.Operator.EQUALS;
import static org.hrorm.Where.where;

public class SimpleParentChildTest {

    private static Helper helper = HelperFactory.forSchema("simple_parents");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @Test
    public void runOnceTest() throws SQLException {
        testInsertUpdateSelect();
    }

    private void runManyTimes() throws SQLException {
        int numberTimes = RandomUtils.range(10,20);
        for(int idx=0; idx<numberTimes; idx++){
            testInsertUpdateSelect();
        }
    }

    private void runManyTimesWithNqueries() throws SQLException {
        int numberTimes = RandomUtils.range(10,20);
        for(int idx=0; idx<numberTimes; idx++){
            testInsertUpdateSelectWithNqueries();
        }
    }


    @Test
    public void runMultipleThreads() throws SQLException {
        int numberThreads = RandomUtils.range(2,10);
        final CountDownLatch latch = new CountDownLatch(numberThreads);
        List<AssertionError> errors = new ArrayList<>();
        List<SQLException> exceptions = new ArrayList<>();
        for(int idx=0; idx<numberThreads; idx++){
            Thread thread = new Thread(
                    () -> {
                        try {
                            runManyTimes();
                            latch.countDown();
                        } catch (AssertionError failure){
                            errors.add(failure);
                        } catch (SQLException ex){
                            exceptions.add(ex);
                        }
                    });
            thread.start();
        }
        try {
            boolean completed = latch.await(10, TimeUnit.SECONDS);

            if( errors.size() > 0){
                throw errors.get(0);
            }

            if( exceptions.size() > 0 ){
                throw exceptions.get(0);
            }

            Assert.assertTrue(completed);
        } catch (InterruptedException ex){
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void runMultipleThreadsWithNqueries() throws SQLException {
        int numberThreads = RandomUtils.range(2,10);
        final CountDownLatch latch = new CountDownLatch(numberThreads);
        List<AssertionError> errors = new ArrayList<>();
        List<SQLException> exceptions = new ArrayList<>();
        for(int idx=0; idx<numberThreads; idx++){
            Thread thread = new Thread(
                    () -> {
                        try {
                            runManyTimesWithNqueries();
                            latch.countDown();
                        } catch (AssertionError failure){
                            errors.add(failure);
                        } catch (SQLException ex){
                            exceptions.add(ex);
                        }
                    });
            thread.start();
        }
        try {
            boolean completed = latch.await(10, TimeUnit.SECONDS);

            if( errors.size() > 0){
                throw errors.get(0);
            }

            if( exceptions.size() > 0 ){
                throw exceptions.get(0);
            }

            Assert.assertTrue(completed);
        } catch (InterruptedException ex){
            Assert.fail(ex.getMessage());
        }
    }


    private void testInsertUpdateSelect() throws SQLException {
        long parentId;
        String parentName;
        List<String> childNames;
        {
            SimpleParent parent = new SimpleParent();
            parentName = randomName();
            parent.setName(parentName);

            childNames = RandomUtils.randomNumberOf(0, 20, SimpleParentChildTest::randomName);

            List<SimpleChild> children = newChildren(childNames);

            parent.setChildren(children);

            Connection connection = helper.connect();
            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);

            parentId = dao.insert(parent);
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);

            // Check that the selectOne worked
            SimpleParent parent = dao.selectOne(parentId);
            Assert.assertEquals(parentName, parent.getName());

            List<SimpleChild> children = parent.getChildren();
            List<String> names = extractNames(children);

            AssertHelp.sameContents(names, childNames);

            List<SimpleChild> filteredChildren = RandomUtils.randomFiltering(children);
            for(SimpleChild child : filteredChildren){
                if( RandomUtils.bool() ){
                    child.setName(randomName());
                }
            }
            List<SimpleChild> newChildren = newChildren(0, 10);
            newChildren.addAll(filteredChildren);

            childNames = extractNames(newChildren);

            parent.setChildren(newChildren);

            dao.update(parent);
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);

            // Check that the selectOne worked
            SimpleParent parent = dao.selectOne(parentId);
            Assert.assertEquals(parentName, parent.getName());

            List<SimpleChild> children = parent.getChildren();
            List<String> names = extractNames(children);

            AssertHelp.sameContents(names, childNames);
            connection.close();
        }
    }


    private void testInsertUpdateSelectWithNqueries() throws SQLException {
        long parentId;
        String parentName;
        List<String> childNames;
        {
            SimpleParent parent = new SimpleParent();
            parentName = randomName();
            parent.setName(parentName);

            childNames = RandomUtils.randomNumberOf(0, 20, SimpleParentChildTest::randomName);

            List<SimpleChild> children = newChildren(childNames);

            parent.setChildren(children);

            Connection connection = helper.connect();
            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);

            parentId = dao.insert(parent);
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);

            List<SimpleParent> parents = dao.selectNqueries(where("id", EQUALS, parentId));
            SimpleParent parent = parents.get(0);
            Assert.assertEquals(parentName, parent.getName());

            List<SimpleChild> children = parent.getChildren();
            List<String> names = extractNames(children);

            AssertHelp.sameContents(names, childNames);

            List<SimpleChild> filteredChildren = RandomUtils.randomFiltering(children);
            for(SimpleChild child : filteredChildren){
                if( RandomUtils.bool() ){
                    child.setName(randomName());
                }
            }
            List<SimpleChild> newChildren = newChildren(0, 10);
            newChildren.addAll(filteredChildren);

            childNames = extractNames(newChildren);

            parent.setChildren(newChildren);

            dao.update(parent);
            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);

            List<SimpleParent> parents = dao.selectNqueries(where("id", EQUALS, parentId));
            SimpleParent parent = parents.get(0);
            Assert.assertEquals(parentName, parent.getName());

            List<SimpleChild> children = parent.getChildren();
            List<String> names = extractNames(children);

            AssertHelp.sameContents(names, childNames);
            connection.close();
        }
    }


    @Test
    public void testSelectWhereLoadsChildren(){
        String parentName = "testSelectWhereLoadsChildren";
        List<String> childNames = helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);

            SimpleParent simpleParent = new SimpleParent();
            simpleParent.setName(parentName);

            List<String> names = RandomUtils.randomNumberOf(5, 15, SimpleParentChildTest::randomName);
            List<SimpleChild> children = newChildren(names);

            simpleParent.setChildren(children);
            parentDao.insert(simpleParent);

            return names;
        });
        helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);
            List<SimpleParent> parentList = parentDao.select(where("name", EQUALS, parentName));
            Assert.assertEquals(1, parentList.size());
            SimpleParent parent = parentList.get(0);
            List<SimpleChild> children = parent.getChildren();

            AssertHelp.sameContents(childNames, children, SimpleChild::getName);
        });
    }

    @Test
    public void testSelectByColumnLoadsChildren(){
        String parentName = "testSelectByColumnLoadsChildren";
        List<String> childNames = helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);

            SimpleParent simpleParent = new SimpleParent();
            simpleParent.setName(parentName);

            List<String> names = RandomUtils.randomNumberOf(5, 15, SimpleParentChildTest::randomName);
            List<SimpleChild> children = newChildren(names);

            simpleParent.setChildren(children);
            parentDao.insert(simpleParent);

            return names;
        });
        helper.useConnection(connection -> {
            Dao<SimpleParent> parentDao = SimpleParentChildDaos.PARENT.buildDao(connection);
            SimpleParent template = new SimpleParent();
            template.setName(parentName);
            List<SimpleParent> parentList = parentDao.select(template, "name");
            Assert.assertEquals(1, parentList.size());
            SimpleParent parent = parentList.get(0);
            List<SimpleChild> children = parent.getChildren();

            AssertHelp.sameContents(childNames, children, SimpleChild::getName);
        });
    }


    @Test
    public void testNqueryProblem(){
        int LIMIT = 10;
        int CHILD_COUNT = 15;
        Map<String, List<String>> childNamesMap = new HashMap<>();

        helper.useConnection(connection -> {
            for(int i=0; i<LIMIT; i++){
                SimpleParent parent = new SimpleParent();
                parent.setName("Nquery_Problem_" + i);
                List<SimpleChild> children = newChildren(CHILD_COUNT, CHILD_COUNT + 1);
                parent.setChildren(children);
                List<String> childNames = extractNames(children);
                childNamesMap.put(parent.getName(), childNames);
                Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);
                dao.insert(parent);
            }
        });

//        helper.useConnection(connection -> {
//            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);
//            List<SimpleParent> parents = dao.select(where("name", Operator.LIKE, "Nquery_Problem%"));
//            Assert.assertEquals(LIMIT, parents.size());
//            for(SimpleParent parent : parents){
//                Assert.assertEquals(CHILD_COUNT, parent.getChildren().size());
//            }
//        });

        helper.useConnection( connection -> {
            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);
            List<SimpleParent> parents = dao.selectNqueries(where("name", Operator.LIKE, "Nquery_Problem%"));
            Assert.assertEquals(LIMIT, parents.size());
            for(SimpleParent parent : parents){
                Assert.assertEquals(CHILD_COUNT, parent.getChildren().size());
                List<String> childNamesFound = extractNames(parent.getChildren());
                List<String> expectedChildNames = childNamesMap.get(parent.getName());
                AssertHelp.sameContents(expectedChildNames, childNamesFound);
            }
        });
    }

    private static String randomName(){
        return RandomUtils.randomAlphabeticString(5,15);
    }

    private static List<SimpleChild> newChildren(int min, int max){
        List<String> names = RandomUtils.randomNumberOf(min, max, SimpleParentChildTest::randomName);
        return newChildren(names);
    }

    private static List<SimpleChild> newChildren(List<String> names){
        return names.stream().map(n -> newChild(n)).collect(Collectors.toList());
    }

    private static SimpleChild newChild(String name){
        SimpleChild child = new SimpleChild();
        child.setName(name);
        return child;
    }

    private static List<String> extractNames(List<SimpleChild> children){
        return children.stream().map(c -> c.getName()).collect(Collectors.toList());
    }
}
