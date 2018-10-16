package org.hrorm;

import org.hrorm.examples.ImmutableThing;
import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;

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


    static final DaoBuilder<ImmutableThing.ImmutableThingBuilder> itbDaoBuilder =
            new DaoBuilder<>("immutable_thing",ImmutableThing::builder)
            .withPrimaryKey("id", "immutable_thing_seq",
                    bldr -> bldr.build().getId(), ImmutableThing.ImmutableThingBuilder::id)
            .withStringColumn("word", bldr -> bldr.build().getWord(), ImmutableThing.ImmutableThingBuilder::word)
            .withBigDecimalColumn("amount", bldr -> bldr.build().getAmount(), ImmutableThing.ImmutableThingBuilder::amount);

    static final ConstructingDaoBuilder<ImmutableThing, ImmutableThing.ImmutableThingBuilder> constructingDaoBuilder =
            new ConstructingDaoBuilder<>("immutable_thing", ImmutableThing::builder, ImmutableThing.ImmutableThingBuilder::build)
            .withPrimaryKey("id", "immutable_thing_seq", ImmutableThing::getId, ImmutableThing.ImmutableThingBuilder::id);

    @Test
    public void insertAndSelectImmutableThing(){

        long id;
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = constructingDaoBuilder.buildDao(connection);

            ImmutableThing it = ImmutableThing.builder()
                    .word("test one")
                    .amount(new BigDecimal("1.3"))
                    .build();

            id = dao.insert(it);
        }
        {
            Connection connection = helper.connect();
            Dao<ImmutableThing> dao = constructingDaoBuilder.buildDao(connection);

            ImmutableThing it = dao.select(id);
            Assert.assertNotNull(it.getId());
            Assert.assertEquals("test one", it.getWord());
            Assert.assertEquals(new BigDecimal("1.3"), it.getAmount());
        }
    }
}
