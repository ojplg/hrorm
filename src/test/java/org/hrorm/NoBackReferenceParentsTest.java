package org.hrorm;

import lombok.Data;
import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoBackReferenceParentsTest {

    private static H2Helper helper = new H2Helper("no_back_reference_parents");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    @Data
    static class SimpleChild {
        Long id;
        Long number;
    }

    @Data
    static class SimpleParent {
        Long id;
        String name;
        List<SimpleChild> simpleChildList;
    }

    static final DaoBuilder<SimpleChild> simpleChildDaoBuilder =
            new DaoBuilder<>("simple_child", SimpleChild::new)
                    .withPrimaryKey("id","no_back_sequence", SimpleChild::getId, SimpleChild::setId)
                    .withIntegerColumn("number", SimpleChild::getNumber, SimpleChild::setNumber)
                    .withParentColumn("simple_parent_id");

    static final DaoBuilder<SimpleParent> simpleParentDaoBuilder =
            new DaoBuilder<>("simple_parent", SimpleParent::new)
                    .withPrimaryKey("id", "no_back_sequence", SimpleParent::getId, SimpleParent::setId)
                    .withStringColumn("name", SimpleParent::getName, SimpleParent::setName)
                    .withChildren(SimpleParent::getSimpleChildList, SimpleParent::setSimpleChildList, simpleChildDaoBuilder);

    @Test
    public void testPersistAndLoad() throws SQLException {
        long parentId;
        {
            SimpleParent parent = new SimpleParent();
            parent.setName("testPersistAndLoad");

            List<SimpleChild> children = new ArrayList<>();
            for(long idx=1; idx<=100; idx++){
                SimpleChild child = new SimpleChild();
                child.setNumber(idx);
                children.add(child);
            }

            parent.setSimpleChildList(children);

            Connection connection = helper.connect();
            Dao<SimpleParent> dao = simpleParentDaoBuilder.buildDao(connection);
            parentId = dao.insert(parent);
            connection.commit();
        }
        {
            Connection connection = helper.connect();
            Dao<SimpleParent> dao = simpleParentDaoBuilder.buildDao(connection);
            SimpleParent parent = dao.select(parentId);

            Assert.assertEquals(100, parent.getSimpleChildList().size());

            List<Long> numbers = parent.getSimpleChildList().stream()
                    .map(c -> c.getNumber()).collect(Collectors.toList());

            for(long idx=1; idx<=100; idx++){
                Assert.assertTrue(numbers.contains(idx));
            }
        }
    }

    @Test
    public void testUpdateChildren(){
        long parentId;
        {
            SimpleParent parent = new SimpleParent();
            parent.setName("testUpdateChildren");

            List<SimpleChild> children = new ArrayList<>();
            for(long idx=1; idx<=10; idx++){
                SimpleChild child = new SimpleChild();
                child.setNumber(idx);
                children.add(child);
            }

            parent.setSimpleChildList(children);

            Connection connection = helper.connect();
            Dao<SimpleParent> dao = simpleParentDaoBuilder.buildDao(connection);
            parentId = dao.insert(parent);
        }
        {
            Connection connection = helper.connect();
            Dao<SimpleParent> dao = simpleParentDaoBuilder.buildDao(connection);
            SimpleParent parent = dao.select(parentId);

            Assert.assertEquals(10, parent.getSimpleChildList().size());

            for( SimpleChild child : parent.getSimpleChildList() ){
                Long number = child.getNumber();
                child.setNumber(number * 3);
            }

            dao.update(parent);
        }
        {
            Connection connection = helper.connect();
            Dao<SimpleParent> dao = simpleParentDaoBuilder.buildDao(connection);
            SimpleParent parent = dao.select(parentId);

            Assert.assertEquals(10, parent.getSimpleChildList().size());

            List<Long> numbers = parent.getSimpleChildList().stream()
                    .map(c -> c.getNumber()).collect(Collectors.toList());

            for(long idx=3; idx<=30; idx+=3){
                Assert.assertTrue(numbers.contains(idx));
            }
        }

    }
}
