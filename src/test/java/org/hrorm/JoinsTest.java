package org.hrorm;

import org.hrorm.examples.Cousin;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.examples.SecondCousin;
import org.hrorm.examples.Sibling;
import org.hrorm.examples.Thing;
import org.hrorm.h2.H2Helper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.time.LocalDateTime;

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

    private static DaoBuilder<SecondCousin> SecondCousinDaoBuilder =
            new DaoBuilder<>("second_cousins", SecondCousin::new)
                    .withPrimaryKey("id", "second_cousin_seq", SecondCousin::getId, SecondCousin::setId)
                    .withLocalDateTimeColumn("datetime", SecondCousin::getDateTime, SecondCousin::setDateTime);

    private static DaoBuilder<Cousin> CousinDaoBuilder =
            new DaoBuilder<>("cousins", Cousin::new)
                    .withPrimaryKey("id", "cousin_seq", Cousin::getId, Cousin::setId)
                    .withConvertingStringColumn("color", Cousin::getColor, Cousin::setColor, new EnumeratedColorConverter())
                    .withJoinColumn("second_cousin_id", Cousin::getSecondCousin, Cousin::setSecondCousin, SecondCousinDaoBuilder);

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

    /*
    select a.id as aid, a.name as aname,
    d.id as did, d.datetime as ddatetime,
    c.id as cid, c.color as ccolor,
    b.id as bid, b.number as bnumber
    from
    things a
    LEFT JOIN second_cousins d ON c.second_cousin_id=d.id
    LEFT JOIN cousins c ON b.cousin_id=c.id
    LEFT JOIN siblings b ON a.sibling_id=b.id and a.id
    */


    @Test
    public void testSelectLoadsSiblingAndCousin(){
        Connection connection = helper.connect();

        Dao<SecondCousin> secondCousinDao = SecondCousinDaoBuilder.buildDao(connection);
        Dao<Cousin> cousinDao = CousinDaoBuilder.buildDao(connection);
        Dao<Sibling> siblingDao = SiblingDaoBuilder.buildDao(connection);
        Dao<Thing> thingDao = ThingDaoBuilder.buildDao(connection);

        LocalDateTime now = LocalDateTime.now();

        SecondCousin secondCousin = new SecondCousin();
        secondCousin.setDateTime(now);
        secondCousinDao.insert(secondCousin);

        Cousin cousin = new Cousin();
        cousin.setColor(EnumeratedColor.Green);
        cousin.setSecondCousin(secondCousin);
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
        Assert.assertEquals(EnumeratedColor.Green, readFromDb.getSibling().getCousin().getColor());
        Assert.assertEquals(now, readFromDb.getSibling().getCousin().getSecondCousin().getDateTime());
    }

    @Test
    public void testUpdateCorrectlyChangesReference(){

        Connection connection = helper.connect();

        Dao<SecondCousin> secondCousinDao = SecondCousinDaoBuilder.buildDao(connection);
        Dao<Cousin> cousinDao = CousinDaoBuilder.buildDao(connection);
        Dao<Sibling> siblingDao = SiblingDaoBuilder.buildDao(connection);
        Dao<Thing> thingDao = ThingDaoBuilder.buildDao(connection);

        LocalDateTime now = LocalDateTime.now();

        SecondCousin secondCousin = new SecondCousin();
        secondCousin.setDateTime(now);
        secondCousinDao.insert(secondCousin);

        Cousin cousin = new Cousin();
        cousin.setColor(EnumeratedColor.Green);
        cousin.setSecondCousin(secondCousin);
        cousinDao.insert(cousin);

        Sibling sibling = new Sibling();
        sibling.setNumber(44L);
        sibling.setCousin(cousin);
        siblingDao.insert(sibling);

        Thing thing = new Thing();
        thing.setName("sibling load test");
        thing.setSibling(sibling);
        long thingId = thingDao.insert(thing);

        Thing readFromDb = thingDao.select(thingId);

        Sibling newSibling = new Sibling();
        newSibling.setNumber(58L);
        newSibling.setCousin(cousin);
        siblingDao.insert(newSibling);

        readFromDb.setSibling(newSibling);

        thingDao.update(readFromDb);

        Thing secondReadFromDb = thingDao.select(thingId);

        Assert.assertEquals(58L, (long) secondReadFromDb.getSibling().getNumber());
        Assert.assertEquals(now, secondReadFromDb.getSibling().getCousin().getSecondCousin().getDateTime());
    }

    @Test
    public void testDeletingEntityLeavesSiblingsAlone(){
        Connection connection = helper.connect();

        Dao<SecondCousin> secondCousinDao = SecondCousinDaoBuilder.buildDao(connection);
        Dao<Cousin> cousinDao = CousinDaoBuilder.buildDao(connection);
        Dao<Sibling> siblingDao = SiblingDaoBuilder.buildDao(connection);
        Dao<Thing> thingDao = ThingDaoBuilder.buildDao(connection);

        LocalDateTime now = LocalDateTime.now();

        SecondCousin secondCousin = new SecondCousin();
        secondCousin.setDateTime(now);
        secondCousinDao.insert(secondCousin);

        Cousin cousin = new Cousin();
        cousin.setColor(EnumeratedColor.Green);
        cousin.setSecondCousin(secondCousin);
        cousinDao.insert(cousin);

        Sibling sibling = new Sibling();
        sibling.setNumber(44L);
        sibling.setCousin(cousin);
        siblingDao.insert(sibling);

        Thing thing = new Thing();
        thing.setName("sibling load test");
        thing.setSibling(sibling);
        long thingId = thingDao.insert(thing);

        Thing readFromDb = thingDao.select(thingId);

        Assert.assertNotNull(readFromDb);

        thingDao.delete(readFromDb);

        Thing secondFromDb = thingDao.select(thingId);

        Assert.assertNull(secondFromDb);

        Sibling readSibling = siblingDao.select(sibling.getId());
        Assert.assertNotNull(readSibling);
        Assert.assertEquals(44L, (long) sibling.getNumber());

    }

    @Test
    public void testNullSiblingsAllowed(){
        Connection connection = helper.connect();

        Dao<Thing> thingDao = ThingDaoBuilder.buildDao(connection);

        Thing thing = new Thing();
        thing.setName("only child");

        long id = thingDao.insert(thing);

        System.out.println("id " + id);

        Thing readThing = thingDao.select(id);

        Assert.assertEquals("only child", readThing.getName());
        Assert.assertNull(readThing.getSibling());
    }

}
