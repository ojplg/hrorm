package org.hrorm;

import org.hrorm.examples.ImmutableChild;
import org.hrorm.examples.ImmutableSibling;
import org.hrorm.examples.ImmutableThing;
import org.hrorm.h2.H2Helper;
import org.hrorm.util.TestLogConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImmutableThingTest {

    static { TestLogConfig.load(); }

    private static H2Helper helper = new H2Helper("immutable_thing");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    static final IndirectDaoBuilder<ImmutableSibling, ImmutableSibling.ImmutableSiblingBuilder> IMMUTABLE_SIBLING_DAO_BUILDER =
            new IndirectDaoBuilder<>("immutable_sibling", ImmutableSibling::builder, ImmutableSibling.ImmutableSiblingBuilder::build )
                    .withPrimaryKey("id", "immutable_sibling_seq", ImmutableSibling::getId, ImmutableSibling.ImmutableSiblingBuilder::id)
                    .withStringColumn("data", ImmutableSibling::getData, ImmutableSibling.ImmutableSiblingBuilder::data);

    static final IndirectDaoBuilder<ImmutableChild, ImmutableChild.ImmutableChildBuilder> IMMUTABLE_CHILD_DAO_BUILDER =
            new IndirectDaoBuilder<>("immutable_child", ImmutableChild::builder, ImmutableChild.ImmutableChildBuilder::build)
                    .withPrimaryKey("id", "immutable_child_seq", ImmutableChild::getId, ImmutableChild.ImmutableChildBuilder::id)
                    .withBooleanColumn("flag", ImmutableChild::getFlag, ImmutableChild.ImmutableChildBuilder::flag)
                    .withLocalDateTimeColumn("birthday", ImmutableChild::getBirthday, ImmutableChild.ImmutableChildBuilder::birthday)
                    .withJoinColumn("sibling_id", ImmutableChild::getImmutableSibling, ImmutableChild.ImmutableChildBuilder::immutableSibling, IMMUTABLE_SIBLING_DAO_BUILDER)
                    .withParentColumn("thing_id");

    static final IndirectDaoBuilder<ImmutableThing, ImmutableThing.ImmutableThingBuilder> IMMUTABLE_OBJECT_DAO_BUILDER =
            new IndirectDaoBuilder<>("immutable_thing", ImmutableThing::builder, ImmutableThing.ImmutableThingBuilder::build)
                    .withPrimaryKey("id", "immutable_thing_seq", ImmutableThing::getId, ImmutableThing.ImmutableThingBuilder::id)
                    .withBigDecimalColumn("amount", ImmutableThing::getAmount, ImmutableThing.ImmutableThingBuilder::amount )
                    .withStringColumn("word", ImmutableThing::getWord, ImmutableThing.ImmutableThingBuilder::word)
                    .withChildren(ImmutableThing::getChildren, ImmutableThing.ImmutableThingBuilder::children, IMMUTABLE_CHILD_DAO_BUILDER);

    @Test
    public void insertAndSelectImmutableThing(){

        long id;
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableThing it = ImmutableThing.builder()
                    .word("test one")
                    .amount(new BigDecimal("1.3"))
                    .children(Collections.emptyList())
                    .build();

            id = dao.insert(it);
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableThing it = dao.select(id);

            Assert.assertNotNull(it.getId());
            Assert.assertEquals("test one", it.getWord());
            Assert.assertEquals(new BigDecimal("1.3"), it.getAmount());
        }
    }

    @Test
    public void insertAndSelectImmutableThingWithAChild(){

        long id;
        LocalDateTime bday = LocalDateTime.now();
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

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
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableThing it = dao.select(id);

            Assert.assertNotNull(it.getId());
            Assert.assertEquals("test one", it.getWord());
            Assert.assertEquals(new BigDecimal("1.3"), it.getAmount());

            Assert.assertEquals(1, it.getChildren().size());
            ImmutableChild child = it.getChildren().get(0);
            Assert.assertNotNull(it.getId());
            Assert.assertEquals(bday, child.getBirthday());
            Assert.assertTrue(child.getFlag());
        }
    }

    @Test
    public void insertAndSelectImmutableThingWithAChildAndSibling(){

        long id;
        long siblingId;
        LocalDateTime bday = LocalDateTime.now();
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);

            ImmutableSibling sibling = ImmutableSibling.builder()
                    .data("Foo the bar!")
                    .build();

            siblingId = siblingDao.insert(sibling);
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);
            ImmutableSibling sibling = siblingDao.select(siblingId);

            Dao<ImmutableThing> dao = IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

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
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

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
        }
    }

    @Test
    public void testCascadingUpdate(){

        long id;
        long firstSiblingId;
        long secondSiblingId;
        long thirdSiblingId;
        LocalDateTime bday = LocalDateTime.now();
        LocalDateTime otherDay = LocalDateTime.of(2018, 10, 7, 3, 34);
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);

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
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);
            ImmutableSibling secondSibling = siblingDao.select(secondSiblingId);
            ImmutableSibling thirdSibling = siblingDao.select(thirdSiblingId);

            Dao<ImmutableThing> dao = IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

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
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableSibling> siblingDao = IMMUTABLE_SIBLING_DAO_BUILDER.buildDao(connection);

            ImmutableSibling firstSibling = siblingDao.select(firstSiblingId);

            Dao<ImmutableThing> dao = IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);

            ImmutableThing it = dao.select(id);

            Assert.assertNotNull(it.getId());
            Assert.assertEquals("chirp", it.getWord());
            Assert.assertEquals(new BigDecimal("-1"), it.getAmount());

            Assert.assertEquals(2, it.getChildren().size());

            ImmutableChild childToChange = it.getChildren().stream()
                    .filter(c -> c.getFlag()).findFirst().get();

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
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = IMMUTABLE_OBJECT_DAO_BUILDER.buildDao(connection);
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

        }
    }

    @Test
    public void testDaoValidation(){
        Connection connection = helper.connect();
        Validator.validate(connection, IMMUTABLE_CHILD_DAO_BUILDER);
        Validator.validate(connection, IMMUTABLE_SIBLING_DAO_BUILDER);
        Validator.validate(connection, IMMUTABLE_OBJECT_DAO_BUILDER);
    }

}
