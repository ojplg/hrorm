package org.hrorm;

import org.hrorm.examples.SimpleChild;
import org.hrorm.examples.SimpleParent;
import org.hrorm.examples.SimpleParentChildDaos;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SimpleParentChildTest {

    private static H2Helper helper = new H2Helper("simple_parents");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @Test
    public void runOnceTest(){
        testInsertUpdateSelect();
    }

    private void runManyTimes(){
        int numberTimes = RandomUtils.range(10,20);
        for(int idx=0; idx<numberTimes; idx++){
            testInsertUpdateSelect();
        }
    }

    @Test
    public void runMultipleThreads(){
        int numberThreads = RandomUtils.range(2,10);
        final CountDownLatch latch = new CountDownLatch(numberThreads);
        List<AssertionError> errors = new ArrayList<>();
        for(int idx=0; idx<numberThreads; idx++){
            Thread thread = new Thread(
                    () -> {
                        try {
                            runManyTimes();
                            latch.countDown();
                        } catch (AssertionError failure){
                            errors.add(failure);
                        }
                    });
            thread.start();
        }
        try {
            boolean completed = latch.await(10, TimeUnit.SECONDS);

            if( errors.size() > 0){
                throw errors.get(0);
            }

            Assert.assertTrue(completed);
        } catch (InterruptedException ex){
            Assert.fail(ex.getMessage());
        }
    }

    private void testInsertUpdateSelect(){
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
        }
        {
            Connection connection = helper.connect();
            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);

            // Check that the select worked
            SimpleParent parent = dao.select(parentId);
            Assert.assertEquals(parentName, parent.getName());

            List<SimpleChild> children = parent.getChildren();
            List<String> names = extractNames(children);

            assertSameContent(names, childNames);

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
        }
        {
            Connection connection = helper.connect();
            Dao<SimpleParent> dao = SimpleParentChildDaos.PARENT.buildDao(connection);

            // Check that the select worked
            SimpleParent parent = dao.select(parentId);
            Assert.assertEquals(parentName, parent.getName());

            List<SimpleChild> children = parent.getChildren();
            List<String> names = extractNames(children);

            assertSameContent(names, childNames);
        }
    }

    private static  <T> void assertSameContent(List<T> as, List<T> bs){
        Assert.assertEquals(as.size(), bs.size());
        Assert.assertTrue(as.containsAll(bs));
        Assert.assertTrue(bs.containsAll(as));
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
