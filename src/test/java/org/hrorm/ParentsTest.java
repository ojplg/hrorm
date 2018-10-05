package org.hrorm;

import org.hrorm.examples.Child;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.examples.Grandchild;
import org.hrorm.examples.Parent;
import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.util.Arrays;

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

    private static DaoBuilder<Grandchild> GrandchildDaoBuilder =
            new DaoBuilder<>("grandchild_table", Grandchild::new)
                    .withPrimaryKey("id", "grandchild_seq", Grandchild::getId, Grandchild::setId)
                    .withIntegerColumn("child_table_id", Grandchild::getChildId, Grandchild::setChildId)
                    .withConvertingStringColumn("color", Grandchild::getColor, Grandchild::setColor, new EnumeratedColorConverter());

    private static DaoBuilder<Child> ChildDaoBuilder =
            new DaoBuilder<>("child_table", Child::new)
                    .withPrimaryKey("id", "child_seq", Child::getId, Child::setId)
                    .withIntegerColumn("number", Child::getNumber, Child::setNumber)
                    .withIntegerColumn("parent_table_id", Child::getParentId, Child::setParentId)
                    .withChildren("child_table_id", Grandchild::setChildId,
                            Child::getGrandchildList, Child::setGrandchildList, GrandchildDaoBuilder);

    private static DaoBuilder<Parent> ParentDaoBuilder =
            new DaoBuilder<>("parent_table", Parent::new)
                    .withPrimaryKey("id", "parent_seq", Parent::getId, Parent::setId)
                    .withStringColumn("name", Parent::getName, Parent::setName)
                    .withChildren("parent_table_id", Child::setParentId,
                            Parent::getChildList, Parent::setChildList, ChildDaoBuilder);

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

}
