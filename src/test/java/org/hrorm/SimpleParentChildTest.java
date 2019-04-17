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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

            // Check that the select worked
            SimpleParent parent = dao.select(parentId);
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

            // Check that the select worked
            SimpleParent parent = dao.select(parentId);
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
            List<SimpleParent> parentList = parentDao.select(where("name", Operator.EQUALS, parentName));
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
            List<SimpleParent> parentList = parentDao.selectManyByColumns(template, "name");
            Assert.assertEquals(1, parentList.size());
            SimpleParent parent = parentList.get(0);
            List<SimpleChild> children = parent.getChildren();

            AssertHelp.sameContents(childNames, children, SimpleChild::getName);
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
