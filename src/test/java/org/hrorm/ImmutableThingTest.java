package org.hrorm;

import org.hrorm.examples.ImmutableChild;
import org.hrorm.examples.ImmutableSibling;
import org.hrorm.examples.ImmutableThing;
import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Collections;

public class ImmutableThingTest {

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
}
