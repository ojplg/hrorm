package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.immutables.DaoBuilders;
import org.hrorm.examples.immutables.ImmutableChild;
import org.hrorm.examples.immutables.ImmutableSibling;
import org.hrorm.examples.immutables.ImmutableThing;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImmutableThingTest {

//    static { TestLogConfig.load(); }

    private static Helper HELPER = HelperFactory.forSchema("immutable_thing");

    @BeforeClass
    public static void setUpDb(){
        HELPER.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        HELPER.dropSchema();
    }

    @Test
    public void insertAndSelectImmutableThing() throws SQLException {
        doInsertAndSelectImmutableThing(HELPER);
    }

    @Test
    public void insertAndSelectImmutableThingWithAChildAndSibling() throws SQLException {
        doInsertAndSelectImmutableThingWithAChildAndSibling(HELPER);
    }

    @Test
    public void testCascadingUpdate() throws SQLException {
        doTestCascadingUpdate(HELPER);
    }

    public static void doInsertAndSelectImmutableThing(Helper helper) throws SQLException {
        long id;
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableThing it = ImmutableThing.builder()
                    .word("test one")
                    .amount(new BigDecimal("1.3"))
                    .children(Collections.emptyList())
                    .build();

            id = dao.insert(it);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableThing it = dao.select(id);

            Assert.assertNotNull(it.getId());
            Assert.assertEquals("test one", it.getWord());
            Assert.assertEquals(new BigDecimal("1.3"), it.getAmount());

            connection.close();
        }
    }

    @Test
    public void insertAndSelectImmutableThingWithAChild() throws SQLException {
        doInsertAndSelectImmutableThingWithAChild(HELPER);
    }

    public static void doInsertAndSelectImmutableThingWithAChild(Helper helper) throws SQLException {
        long id;
        Instant bday = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableChild ic = ImmutableChild.builder()
                    .birthday(bday)
                    .flag(true)
                    .build();

            ImmutableThing it = ImmutableThing.builder()
                    .word("test one")
                    .amount(new BigDecimal("1.3"))
                    .children(Collections.singletonList(ic))
                    .build();

            id = dao.insert(it);

            connection.commit();
            connection.close();
        }
        {

            System.out.println("READING !!!!!!!!!!!!!!!");
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableThing it = dao.select(id);

            Assert.assertNotNull(it.getId());
            Assert.assertEquals("test one", it.getWord());
            Assert.assertEquals(new BigDecimal("1.3"), it.getAmount());

            Assert.assertEquals(1, it.getChildren().size());
            ImmutableChild child = it.getChildren().get(0);
            Assert.assertNotNull(it.getId());
            Assert.assertEquals(bday, child.getBirthday());
            Assert.assertTrue(child.getFlag());

            connection.close();
        }
    }

    public static void doInsertAndSelectImmutableThingWithAChildAndSibling(Helper helper) throws SQLException {

        long id;
        long siblingId;
        Instant bday = Instant.now();
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = DaoBuilders.IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);

            ImmutableSibling sibling = ImmutableSibling.builder()
                    .data("Foo the bar!")
                    .build();

            siblingId = siblingDao.insert(sibling);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = DaoBuilders.IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);
            ImmutableSibling sibling = siblingDao.select(siblingId);

            Dao<ImmutableThing> dao = DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableChild ic = ImmutableChild.builder()
                    .birthday(bday)
                    .flag(true)
                    .immutableSibling(sibling)
                    .build();

            ImmutableThing it = ImmutableThing.builder()
                    .word("something or other")
                    .amount(new BigDecimal("98765.4321"))
                    .children(Collections.singletonList(ic))
                    .build();

            id = dao.insert(it);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableThing it = dao.select(id);

            Assert.assertNotNull(it.getId());
            Assert.assertEquals("something or other", it.getWord());
            Assert.assertEquals(new BigDecimal("98765.4321"), it.getAmount());

            Assert.assertEquals(1, it.getChildren().size());

            ImmutableChild child = it.getChildren().get(0);
            Assert.assertNotNull(it.getId());
            Assert.assertEquals(bday, child.getBirthday());
            Assert.assertTrue(child.getFlag());

            ImmutableSibling sibling = child.getImmutableSibling();
            Assert.assertNotNull(sibling);
            Assert.assertNotNull(sibling.getId());
            Assert.assertEquals("Foo the bar!", sibling.getData());

            connection.close();
        }
    }


    public static void doTestCascadingUpdate(Helper helper) throws SQLException {

        long id;
        long firstSiblingId;
        long secondSiblingId;
        long thirdSiblingId;
        Instant bday = Instant.now();
        Instant otherDay = LocalDateTime.of(2018, 10, 7, 3, 34).toInstant(ZoneOffset.UTC);
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = DaoBuilders.IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);

            ImmutableSibling sibling1 = ImmutableSibling.builder()
                    .data("First!")
                    .build();
            ImmutableSibling sibling2 = ImmutableSibling.builder()
                    .data("Second!")
                    .build();
            ImmutableSibling sibling3 = ImmutableSibling.builder()
                    .data("Third!")
                    .build();

            firstSiblingId = siblingDao.insert(sibling1);
            secondSiblingId = siblingDao.insert(sibling2);
            thirdSiblingId = siblingDao.insert(sibling3);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = DaoBuilders.IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);
            ImmutableSibling secondSibling = siblingDao.select(secondSiblingId);
            ImmutableSibling thirdSibling = siblingDao.select(thirdSiblingId);

            Dao<ImmutableThing> dao = DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableChild child1 = ImmutableChild.builder()
                    .birthday(otherDay)
                    .flag(true)
                    .immutableSibling(secondSibling)
                    .build();

            ImmutableChild child2 = ImmutableChild.builder()
                    .birthday(bday)
                    .flag(false)
                    .immutableSibling(thirdSibling)
                    .build();

            ImmutableThing it = ImmutableThing.builder()
                    .word("chirp")
                    .amount(new BigDecimal("-1"))
                    .children(Arrays.asList(child1, child2))
                    .build();

            id = dao.insert(it);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = DaoBuilders.IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);

            ImmutableSibling firstSibling = siblingDao.select(firstSiblingId);

            Dao<ImmutableThing> dao = DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableThing it = dao.select(id);

            Assert.assertNotNull(it.getId());
            Assert.assertEquals("chirp", it.getWord());
            Assert.assertEquals(new BigDecimal("-1"), it.getAmount());

            Assert.assertEquals(2, it.getChildren().size());

            ImmutableChild childToChange = it.getChildren().stream()
                    .filter(ImmutableChild::getFlag).findFirst().get();

            ImmutableChild changedChild = ImmutableChild.builder()
                    .id(childToChange.getId())
                    .birthday(childToChange.getBirthday())
                    .flag(false)
                    .immutableSibling(firstSibling)
                    .build();

            ImmutableChild differentChild = ImmutableChild.builder()
                    .flag(true)
                    .build();

            ImmutableThing updatedThing = ImmutableThing.builder()
                    .id(it.getId())
                    .word("cheap")
                    .amount(it.getAmount())
                    .children(Arrays.asList(changedChild, differentChild))
                    .build();

            dao.update(updatedThing);

            connection.commit();
            connection.close();
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);
            ImmutableThing it = dao.select(id);

            Assert.assertEquals("cheap", it.getWord());
            List<ImmutableChild> children = it.getChildren();
            Assert.assertEquals(2, children.size());

            ImmutableChild trueChild = it.getChildren().stream()
                    .filter(c -> c.getFlag()).findFirst().get();

            Assert.assertNotNull(trueChild);
            Assert.assertNull(trueChild.getImmutableSibling());
            Assert.assertNull(trueChild.getBirthday());

            ImmutableChild falseChild = it.getChildren().stream()
                    .filter(c -> ! c.getFlag()).findFirst().get();

            Assert.assertNotNull(falseChild);
            Assert.assertEquals("First!", falseChild.getImmutableSibling().getData());

            connection.close();
        }
    }

    @Test
    public void testDaoValidation() throws SQLException {
        Connection connection = HELPER.connect();
        Validator.validate(connection, DaoBuilders.IMMUTABLE_CHILD_DAO_BUILDER);
        Validator.validate(connection, DaoBuilders.IMMUTABLE_SIBLING_DAO_BUILDER);
        Validator.validate(connection, DaoBuilders.IMMUTABLE_OBJECT_DAO_BUILDER);
        connection.close();
    }

}
