package org.hrorm;

import org.hrorm.examples.Child;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.examples.Grandchild;
import org.hrorm.examples.Parent;
import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
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


    private static DaoBuilder<Grandchild> GrandchildDaoBuilder =
            new DaoBuilder<>("grandchild_table", Grandchild::new)
                    .withPrimaryKey("id", "grandchild_seq", Grandchild::getId, Grandchild::setId)
                    .withParentColumn("child_table_id", Grandchild::getChild, Grandchild::setChild)
                    .withConvertingStringColumn("color", Grandchild::getColor, Grandchild::setColor, new EnumeratedColorConverter());

    private static DaoBuilder<Child> ChildDaoBuilder =
            new DaoBuilder<>("child_table", Child::new)
                    .withPrimaryKey("id", "child_seq", Child::getId, Child::setId)
                    .withIntegerColumn("number", Child::getNumber, Child::setNumber)
                    .withParentColumn("parent_table_id", Child::getParent, Child::setParent)
                    .withChildren(Child::getGrandchildList, Child::setGrandchildList, GrandchildDaoBuilder);

    private static DaoBuilder<Parent> ParentDaoBuilder =
            new DaoBuilder<>("parent_table", Parent::new)
                    .withPrimaryKey("id", "parent_seq", Parent::getId, Parent::setId)
                    .withStringColumn("name", Parent::getName, Parent::setName)
                    .withChildren(Parent::getChildList, Parent::setChildList, ChildDaoBuilder);

    @Test
    public void testSavePropagatesToChildren(){
        Child child = new Child();
        child.setNumber(123L);

        Parent parent = new Parent();
        parent.setName("save propagation test");
        parent.setChildList(Arrays.asList(child));

        Connection connection = helper.connect();

        Dao<Parent> parentDao = ParentDaoBuilder.buildDao(connection);

        parentDao.insert(parent);

        long parentId = parent.getId();

        Parent readItem = parentDao.select(parentId);

        Assert.assertEquals("save propagation test", readItem.getName());
        Assert.assertEquals(1, readItem.getChildList().size());
        Assert.assertEquals(123L, (long) readItem.getChildList().get(0).getNumber());
    }

    @Test
    public void testDeletesHappenOnUpdate(){
        Connection connection = helper.connect();
        Dao<Parent> parentDao = ParentDaoBuilder.buildDao(connection);

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
        Dao<Parent> parentDao = ParentDaoBuilder.buildDao(connection);

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

        Dao<Parent> parentDao = ParentDaoBuilder.buildDao(connection);

        parentDao.insert(parent);

        long parentId = parent.getId();

        Parent readItem = parentDao.select(parentId);

        Assert.assertEquals(1, readItem.getChildList().get(0).getGrandchildList().size());
        Assert.assertEquals(EnumeratedColor.Green,  readItem.getChildList().get(0).getGrandchildList().get(0).getColor());
    }

    @Test
    public void testUpdatesPropagatesToGrandChildren(){
        Grandchild grandchild = new Grandchild();
        grandchild.setColor(EnumeratedColor.Green);

        Child child = new Child();
        child.setNumber(123L);
        child.setGrandchildList(Arrays.asList(grandchild));

        Parent parent = new Parent();
        parent.setName("update multigeneration test");
        parent.setChildList(Arrays.asList(child));

        Connection connection = helper.connect();

        Dao<Parent> parentDao = ParentDaoBuilder.buildDao(connection);

        parentDao.insert(parent);

        long parentId = parent.getId();

        Parent readItem = parentDao.select(parentId);

        Assert.assertEquals(1, readItem.getChildList().get(0).getGrandchildList().size());
        Assert.assertEquals(EnumeratedColor.Green,  readItem.getChildList().get(0).getGrandchildList().get(0).getColor());

        readItem.getChildList().get(0).getGrandchildList().get(0).setColor(EnumeratedColor.Blue);

        parentDao.update(readItem);

        Parent secondReadItem = parentDao.select(parentId);

        Assert.assertEquals(1, secondReadItem.getChildList().get(0).getGrandchildList().size());
        Assert.assertEquals(EnumeratedColor.Blue,  secondReadItem.getChildList().get(0).getGrandchildList().get(0).getColor());

    }

    @Test
    public void deletionOfChildrenDoesNotOrphanGrandchildRecords(){
        deleteAll();

        Grandchild grandchild = new Grandchild();
        grandchild.setColor(EnumeratedColor.Green);

        Child child = new Child();
        child.setNumber(123L);
        child.setGrandchildList(Arrays.asList(grandchild));

        Parent parent = new Parent();
        parent.setName("delete orphan grandchildren test");
        parent.setChildList(Arrays.asList(child));

        Connection connection = helper.connect();

        Dao<Parent> parentDao = ParentDaoBuilder.buildDao(connection);

        parentDao.insert(parent);

        long parentId = parent.getId();

        Parent readItem = parentDao.select(parentId);

        Assert.assertEquals(1, readItem.getChildList().get(0).getGrandchildList().size());
        Assert.assertEquals(EnumeratedColor.Green,  readItem.getChildList().get(0).getGrandchildList().get(0).getColor());

        Dao<Grandchild> grandchildDao = GrandchildDaoBuilder.buildDao(connection);
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

        Child childA = new Child();
        childA.setNumber(23L);
        Child childB = new Child();
        childB.setNumber(46L);
        Child childC= new Child();
        childC.setNumber(72L);

        Parent parent = new Parent();
        parent.setName("Multi Child Parent");
        parent.setChildList(Arrays.asList(childA, childB, childC));

        Connection connection = helper.connect();
        Dao<Parent> parentDao = ParentDaoBuilder.buildDao(connection);
        parentDao.insert(parent);
        long parentId = parent.getId();

        Parent readItem = parentDao.select(parentId);

        Assert.assertEquals(3, readItem.getChildList().size());

        List<Long> numbers = readItem.getChildList().stream()
                .map(c -> c.getNumber()).collect(Collectors.toList());

        Assert.assertTrue(numbers.contains(23L));
        Assert.assertTrue(numbers.contains(46L));
        Assert.assertTrue(numbers.contains(72L));
    }

    @Test
    public void testUpdateMultipleChildren(){

        long parentId;
        {
            Child childA = new Child();
            childA.setNumber(23L);
            Child childB = new Child();
            childB.setNumber(46L);
            Child childC = new Child();
            childC.setNumber(72L);

            Parent parent = new Parent();
            parent.setName("Multi Child Parent");
            parent.setChildList(Arrays.asList(childA, childB, childC));

            Dao<Parent> parentDao = ParentDaoBuilder.buildDao(helper.connect());
            parentDao.insert(parent);
            parentId = parent.getId();
        }

        {
            Dao<Parent> parentDao = ParentDaoBuilder.buildDao(helper.connect());

            Parent readItem = parentDao.select(parentId);

            Assert.assertEquals(3, readItem.getChildList().size());

            Child child46 = readItem.getChildByNumber(46L);
            child46.setNumber(146L);

            Child child23 = readItem.getChildByNumber(23L);
            readItem.getChildList().remove(child23);

            Child child98 = new Child();
            child98.setNumber(98L);

            Child child54 = new Child();
            child54.setNumber(54L);

            readItem.getChildList().add(child54);
            readItem.getChildList().add(child98);

            parentDao.update(readItem);
        }

        {
            Dao<Parent> parentDao = ParentDaoBuilder.buildDao(helper.connect());

            Parent readItem = parentDao.select(parentId);

            Assert.assertEquals(4, readItem.getChildList().size());
            List<Long> numbers = readItem.getChildList().stream()
                    .map(c -> c.getNumber()).collect(Collectors.toList());

            Assert.assertTrue(numbers.contains(146L));
            Assert.assertTrue(numbers.contains(72L));
            Assert.assertTrue(numbers.contains(54L));
            Assert.assertTrue(numbers.contains(98L));
        }

    }


}
