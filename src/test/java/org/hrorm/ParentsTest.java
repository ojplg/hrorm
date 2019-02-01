package org.hrorm;

import org.hrorm.examples.Child;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.Grandchild;
import org.hrorm.examples.Parent;
import org.hrorm.examples.ParentChildBuilders;
import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ParentsTest {

    private static H2Helper helper = new H2Helper("parents");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    private void deleteAll(){
        try {
            Connection connection = helper.connect();
            Statement statement = connection.createStatement();
            statement.execute("delete from grandchild_table");
            statement.execute("delete from child_table");
            statement.execute("delete from parent_table");
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testSavePropagatesToChildren() throws SQLException {
        Child child = new Child();
        child.setNumber(123L);

        Parent parent = new Parent();
        parent.setName("save propagation test");
        parent.setChildList(Arrays.asList(child));

        Connection connection = helper.connect();

        Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);

        parentDao.insert(parent);

        long parentId = parent.getId();

        Assert.assertTrue(parentId > 0);

        Parent readItem = parentDao.select(parentId);

        Assert.assertNotNull(readItem);
        Assert.assertEquals("save propagation test", readItem.getName());
        Assert.assertEquals(1, readItem.getChildList().size());
        Assert.assertEquals(123L, (long) readItem.getChildList().get(0).getNumber());
        Assert.assertTrue(readItem.getChildList().get(0).getId() > 1);
    }

    @Test
    public void testDeletesHappenOnUpdate(){
        Connection connection = helper.connect();
        Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);

        Child child = new Child();
        child.setNumber(123L);

        Parent parent = new Parent();
        parent.setName("propagated deletes test");
        parent.setChildList(Arrays.asList(child));

        long id = parentDao.insert(parent);

        Parent readParent = parentDao.select(id);

        Assert.assertEquals(1, readParent.getChildList().size());

        readParent.setChildList(Collections.emptyList());

        parentDao.update(readParent);

        Parent readAfterUpdate = parentDao.select(id);

        Assert.assertEquals(0, readAfterUpdate.getChildList().size());
    }

    @Test
    public void testUpdatesPropagate(){
        Connection connection = helper.connect();
        Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);

        Child child = new Child();
        child.setNumber(123L);

        Parent parent = new Parent();
        parent.setName("propagated updates test");
        parent.setChildList(Arrays.asList(child));

        long id = parentDao.insert(parent);

        Parent readParent = parentDao.select(id);

        Assert.assertEquals(1, readParent.getChildList().size());
        Assert.assertEquals(123L, (long) readParent.getChildList().get(0).getNumber());

        readParent.getChildList().get(0).setNumber(55L);

        parentDao.update(readParent);

        Parent readAfterUpdate = parentDao.select(id);

        Assert.assertEquals(55L, (long) readAfterUpdate.getChildList().get(0).getNumber());
    }

    @Test
    public void testSavePropagatesToGrandChildren(){
        Grandchild grandchild = new Grandchild();
        grandchild.setColor(EnumeratedColor.Green);

        Child child = new Child();
        child.setNumber(123L);
        child.setGrandchildList(Arrays.asList(grandchild));

        Parent parent = new Parent();
        parent.setName("save multigeneration test");
        parent.setChildList(Arrays.asList(child));

        Connection connection = helper.connect();

        Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);

        parentDao.insert(parent);

        long parentId = parent.getId();

        Parent readItem = parentDao.select(parentId);

        Assert.assertEquals(1, readItem.getChildList().get(0).getGrandchildList().size());
        Assert.assertEquals(EnumeratedColor.Green,  readItem.getChildList().get(0).getGrandchildList().get(0).getColor());
        Assert.assertTrue(readItem.getChildList().get(0).getGrandchildList().get(0).getId() > 1);
    }

    @Test
    public void testUpdatesPropagatesToGrandChildren(){
        long parentId;
        {
            Grandchild grandchild = new Grandchild();
            grandchild.setColor(EnumeratedColor.Green);

            Child child = new Child();
            child.setNumber(123L);
            child.setGrandchildList(Arrays.asList(grandchild));

            Parent parent = new Parent();
            parent.setName("update multigeneration test");
            parent.setChildList(Arrays.asList(child));

            Connection connection = helper.connect();
            Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);

            parentId = parentDao.insert(parent);
        }
        {
            Connection connection = helper.connect();
            Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);
            Parent readItem = parentDao.select(parentId);

            Assert.assertEquals(1, readItem.getChildList().get(0).getGrandchildList().size());
            Assert.assertEquals(EnumeratedColor.Green, readItem.getChildList().get(0).getGrandchildList().get(0).getColor());

            readItem.getChildList().get(0).getGrandchildList().get(0).setColor(EnumeratedColor.Blue);

            parentDao.update(readItem);
        }
        {
            Connection connection = helper.connect();
            Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);
            Parent secondReadItem = parentDao.select(parentId);

            Assert.assertNotNull(secondReadItem);
            Assert.assertEquals(1, secondReadItem.getChildList().size());

            Assert.assertEquals(1, secondReadItem.getChildList().get(0).getGrandchildList().size());
            Assert.assertEquals(EnumeratedColor.Blue, secondReadItem.getChildList().get(0).getGrandchildList().get(0).getColor());
        }

    }

    @Test
    public void deletionOfChildrenDoesNotOrphanGrandchildRecords(){
        //deleteAll();

        Grandchild grandchild = new Grandchild();
        grandchild.setColor(EnumeratedColor.Green);

        Child child = new Child();
        child.setNumber(123L);
        child.setGrandchildList(Arrays.asList(grandchild));

        Parent parent = new Parent();
        parent.setName("delete orphan grandchildren test");
        parent.setChildList(Arrays.asList(child));

        Connection connection = helper.connect();

        Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);

        parentDao.insert(parent);

        long parentId = parent.getId();

        Parent readItem = parentDao.select(parentId);

        Assert.assertEquals(1, readItem.getChildList().get(0).getGrandchildList().size());
        Assert.assertEquals(EnumeratedColor.Green,  readItem.getChildList().get(0).getGrandchildList().get(0).getColor());

        Dao<Grandchild> grandchildDao = ParentChildBuilders.GrandchildDaoBuilder.buildDao(connection);
        List<Grandchild> allGrandchildren = grandchildDao.selectAll();

        Assert.assertEquals(1, allGrandchildren.size());

        readItem.setChildList(Collections.emptyList());

        parentDao.update(readItem);

        Parent secondReadItem = parentDao.select(parentId);

        Assert.assertEquals(0, secondReadItem.getChildList().size());

        allGrandchildren = grandchildDao.selectAll();
        Assert.assertEquals(0, allGrandchildren.size());

    }

    @Test
    public void testInsertMultipleChildren(){

        for( int idx = 0 ; idx< 10 ; idx++ ) {
            long parentId;
            {
                Child childA = new Child();
                childA.setNumber(23L +idx);
                Child childB = new Child();
                childB.setNumber(46L +idx);
                Child childC = new Child();
                childC.setNumber(72L +idx);

                Parent parent = new Parent();
                parent.setName("Multi Child Parent");
                parent.setChildList(Arrays.asList(childA, childB, childC));

                Connection connection = helper.connect();
                Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);
                parentDao.insert(parent);
                parentId = parent.getId();
            }
            {
                Connection connection = helper.connect();
                Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(connection);

                Parent readItem = parentDao.select(parentId);

                Assert.assertEquals(3, readItem.getChildList().size());

                List<Long> numbers = readItem.getChildList().stream()
                        .map(c -> c.getNumber()).collect(Collectors.toList());

                Assert.assertTrue(numbers.contains(23L +idx));
                Assert.assertTrue(numbers.contains(46L +idx));
                Assert.assertTrue(numbers.contains(72L +idx));
            }
        }
    }

    @Test
    public void testUpdateMultipleChildren(){

        {
            for (int idx = 0 ; idx< 10; idx++){
                Parent parent = new Parent();
                parent.setName("Multi Child Parent " + idx);

                Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(helper.connect());
                parentDao.insert(parent);
            }
        }

        for (int idx = 0 ; idx < 5 ; idx++ ) {
            long parentId;
            {
                Child childA = new Child();
                childA.setNumber(23L + idx);
                Child childB = new Child();
                childB.setNumber(46L + idx);
                Child childC = new Child();
                childC.setNumber(72L + idx);

                Parent parent = new Parent();
                parent.setName("Multi Child Parent");
                parent.setChildList(Arrays.asList(childA, childB, childC));

                Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(helper.connect());
                parentDao.insert(parent);
                parentId = parent.getId();
            }

            {
                Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(helper.connect());

                Parent readItem = parentDao.select(parentId);

                Assert.assertEquals(3, readItem.getChildList().size());

                Child child46 = readItem.getChildByNumber(46L + idx);
                child46.setNumber(146L);

                Child child23 = readItem.getChildByNumber(23L + idx);
                readItem.getChildList().remove(child23);

                Child child98 = new Child();
                child98.setNumber(98L + idx);

                Child child54 = new Child();
                child54.setNumber(54L + idx);

                readItem.getChildList().add(child54);
                readItem.getChildList().add(child98);

                parentDao.update(readItem);
            }

            {
                Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(helper.connect());

                Parent readItem = parentDao.select(parentId);

                Assert.assertEquals(4, readItem.getChildList().size());
                List<Long> numbers = readItem.getChildList().stream()
                        .map(c -> c.getNumber()).collect(Collectors.toList());

                Assert.assertTrue(numbers.contains(146L));
                Assert.assertTrue(numbers.contains(72L + idx));
                Assert.assertTrue(numbers.contains(54L + idx));
                Assert.assertTrue(numbers.contains(98L + idx));
            }

        }
    }

    @Test
    public void testDaoValidation(){
        Connection connection = helper.connect();
        Validator.validate(connection, ParentChildBuilders.ParentDaoBuilder);
        Validator.validate(connection, ParentChildBuilders.ChildDaoBuilder);
        Validator.validate(connection, ParentChildBuilders.GrandchildDaoBuilder);
    }

    @Test
    public void testCanInsertSomethingWithParentDirectly(){
        Parent parent;
        {
            parent = new Parent();
            parent.setName("DirectlySaveChildTest");

            Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(helper.connect());
            parentDao.insert(parent);

        }
        Assert.assertNotNull(parent.getId());
        long childId;
        {
            Child child = new Child();
            child.setParent(parent);
            child.setNumber(123L);

            Grandchild g1 = new Grandchild();
            g1.setColor(EnumeratedColor.Blue);

            Grandchild g2 = new Grandchild();
            g2.setColor(EnumeratedColor.Red);

            child.setGrandchildList(Arrays.asList(g1, g2));

            Dao<Child> childDao = ParentChildBuilders.ChildDaoBuilder.buildDao(helper.connect());

            childId = childDao.insert(child);
        }
        {
            Dao<Child> childDao = ParentChildBuilders.ChildDaoBuilder.buildDao(helper.connect());
            Child child = childDao.select(childId);
            Assert.assertEquals(2, child.getGrandchildList().size());
            Assert.assertEquals(123L, (long) child.getNumber());
            Assert.assertNull(child.getParent());
        }

    }


    @Test
    public void testCanUpdateSomethingWithParentDirectly(){
        long childId;
        Parent parent;
        {
            Grandchild grandchild = new Grandchild();
            grandchild.setColor(EnumeratedColor.Blue);

            Child child = new Child();
            child.setNumber(4321L);
            child.setGrandchildList(Collections.singletonList(grandchild));

            parent = new Parent();
            parent.setName("Directly Update Child Test");
            parent.setChildList(Collections.singletonList(child));

            Dao<Parent> parentDao = ParentChildBuilders.ParentDaoBuilder.buildDao(helper.connect());
            parentDao.insert(parent);

            childId = child.getId();
        }
        {
            Dao<Child> childDao = ParentChildBuilders.ChildDaoBuilder.buildDao(helper.connect());
            Child child = childDao.select(childId);

            Assert.assertNull(child.getParent());

            child.setNumber(45325L);
            child.setParent(parent);

            childDao.update(child);
        }
        {
            Dao<Child> childDao = ParentChildBuilders.ChildDaoBuilder.buildDao(helper.connect());
            Child child = childDao.select(childId);
            Assert.assertEquals(45325L, (long) child.getNumber());
        }

    }

}
