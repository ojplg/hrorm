package org.hrorm;

import org.hrorm.examples.Cousin;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.examples.Sibling;
import org.hrorm.examples.Thing;
import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;

public class JoinsTest {

    private static H2Helper helper = new H2Helper("joins");

    @BeforeClass
    public static void setUpDb(){
        helper.initializeSchema();
    }

    @AfterClass
    public static void cleanUpDb(){
        helper.dropSchema();
    }

    private static DaoBuilder<Cousin> CousinDaoBuilder =
            new DaoBuilder<>("cousins", Cousin::new)
                    .withPrimaryKey("id", "cousin_seq", Cousin::getId, Cousin::setId)
                    .withConvertingStringColumn("color", Cousin::getColor, Cousin::setColor, new EnumeratedColorConverter());

    private static DaoBuilder<Sibling> SiblingDaoBuilder =
            new DaoBuilder<>("siblings", Sibling::new)
                    .withPrimaryKey("id", "sibling_seq", Sibling::getId, Sibling::setId)
                    .withIntegerColumn("number", Sibling::getNumber, Sibling::setNumber)
                    .withJoinColumn("cousin_id", Sibling::getCousin, Sibling::setCousin, CousinDaoBuilder);

    private static DaoBuilder<Thing> ThingDaoBuilder =
            new DaoBuilder<>("things", Thing::new)
                    .withPrimaryKey("id", "thing_seq", Thing::getId, Thing::setId)
                    .withStringColumn("name", Thing::getName, Thing::setName)
                    .withJoinColumn("sibling_id", Thing::getSibling, Thing::setSibling, SiblingDaoBuilder);

    @Test
    public void testSelectLoadsSiblingAndCousin(){
        Connection connection = helper.connect();

        Dao<Cousin> cousinDao = CousinDaoBuilder.buildDao(connection);
        Dao<Sibling> siblingDao = SiblingDaoBuilder.buildDao(connection);
        Dao<Thing> thingDao = ThingDaoBuilder.buildDao(connection);

        Cousin cousin = new Cousin();
        cousin.setColor(EnumeratedColor.Green);

        cousinDao.insert(cousin);

        Sibling sibling = new Sibling();
        sibling.setNumber(44L);
        sibling.setCousin(cousin);

        siblingDao.insert(sibling);

        Thing thing = new Thing();
        thing.setName("sibling load test");
        thing.setSibling(sibling);

        thingDao.insert(thing);

        Thing readFromDb = thingDao.select(thing.getId());

        Assert.assertEquals(44L, (long) readFromDb.getSibling().getNumber());
//        Assert.assertEquals(EnumeratedColor.Green, readFromDb.getSibling().getCousin().getColor());
    }

}
