package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.siblings.Cousin;
import org.hrorm.examples.EnumeratedColor;
import org.hrorm.examples.EnumeratedColorConverter;
import org.hrorm.examples.siblings.SecondCousin;
import org.hrorm.examples.siblings.Sibling;
import org.hrorm.examples.siblings.Thing;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;

public class JoinsTest {

//    static { TestLogConfig.load(); }

    private static Helper helper = HelperFactory.forSchema("joins");

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
                    .withPrimaryKey("second_cousin_id", "second_cousin_seq", SecondCousin::getId, SecondCousin::setId)
                    .withInstantColumn("datetime", SecondCousin::getDateTime, SecondCousin::setDateTime);

    private static DaoBuilder<Cousin> CousinDaoBuilder =
            new DaoBuilder<>("cousins", Cousin::new)
                    .withPrimaryKey("cousin_id", "cousin_seq", Cousin::getId, Cousin::setId)
                    .withConvertingStringColumn("color", Cousin::getColor, Cousin::setColor, new EnumeratedColorConverter())
                    .withJoinColumn("second_cousin_id", Cousin::getSecondCousin, Cousin::setSecondCousin, SecondCousinDaoBuilder);

    private static DaoBuilder<Sibling> SiblingDaoBuilder =
            new DaoBuilder<>("siblings", Sibling::new)
                    .withPrimaryKey("sibling_id_id", "sibling_seq", Sibling::getId, Sibling::setId)
                    .withLongColumn("number", Sibling::getNumber, Sibling::setNumber)
                    .withJoinColumn("cousin_id", Sibling::getCousin, Sibling::setCousin, CousinDaoBuilder);

    private static DaoBuilder<Thing> ThingDaoBuilder =
            new DaoBuilder<>("things", Thing::new)
                    .withPrimaryKey("thing_id", "thing_seq", Thing::getId, Thing::setId)
                    .withStringColumn("name", Thing::getName, Thing::setName)
                    .withJoinColumn("sibling_id", Thing::getSibling, Thing::setSibling, SiblingDaoBuilder);

    private static DaoBuilder<Thing> ThingDaoBuilderNotNullSibling =
            new DaoBuilder<>("things", Thing::new)
                    .withPrimaryKey("thing_id", "thing_seq", Thing::getId, Thing::setId)
                    .withStringColumn("name", Thing::getName, Thing::setName)
                    .withJoinColumn("sibling_id", Thing::getSibling, Thing::setSibling, SiblingDaoBuilder).notNull();


    @Test
    public void testSelectLoadsSiblingAndCousin() throws SQLException {
        Connection connection = helper.connect();

        Dao<SecondCousin> secondCousinDao = SecondCousinDaoBuilder.buildDao(connection);
        Dao<Cousin> cousinDao = CousinDaoBuilder.buildDao(connection);
        Dao<Sibling> siblingDao = SiblingDaoBuilder.buildDao(connection);
        Dao<Thing> thingDao = ThingDaoBuilder.buildDao(connection);

        Instant now = Instant.now();

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

        Assert.assertEquals(44L, (long) readFromDb.getSibling().getNumber());
        Assert.assertEquals(EnumeratedColor.Green, readFromDb.getSibling().getCousin().getColor());
        Assert.assertEquals(now, readFromDb.getSibling().getCousin().getSecondCousin().getDateTime());

        Assert.assertNotNull(readFromDb.getId());
        Assert.assertNotNull(readFromDb.getSibling().getId());


        connection.commit();
        connection.close();
    }

    @Test
    public void testUpdateCorrectlyChangesReference() throws SQLException {

        Connection connection = helper.connect();

        Dao<SecondCousin> secondCousinDao = SecondCousinDaoBuilder.buildDao(connection);
        Dao<Cousin> cousinDao = CousinDaoBuilder.buildDao(connection);
        Dao<Sibling> siblingDao = SiblingDaoBuilder.buildDao(connection);
        Dao<Thing> thingDao = ThingDaoBuilder.buildDao(connection);

        Instant now = Instant.now();

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

        connection.commit();
        connection.close();
    }

    @Test
    public void testDeletingEntityLeavesSiblingsAlone() throws SQLException {
        Connection connection = helper.connect();

        Dao<SecondCousin> secondCousinDao = SecondCousinDaoBuilder.buildDao(connection);
        Dao<Cousin> cousinDao = CousinDaoBuilder.buildDao(connection);
        Dao<Sibling> siblingDao = SiblingDaoBuilder.buildDao(connection);
        Dao<Thing> thingDao = ThingDaoBuilder.buildDao(connection);

        Instant now = Instant.now();

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

        connection.commit();
        connection.close();
    }

    @Test
    public void testNullSiblingsAllowed() throws SQLException {
        Connection connection = helper.connect();

        Dao<Thing> thingDao = ThingDaoBuilder.buildDao(connection);

        Thing thing = new Thing();
        thing.setName("only child");

        long id = thingDao.insert(thing);

        Thing readThing = thingDao.select(id);

        Assert.assertEquals("only child", readThing.getName());
        Assert.assertNull(readThing.getSibling());

        connection.commit();
        connection.close();
    }

    @Test
    public void notNullSettingPreventsNullSiblings() throws SQLException {
        Connection connection = helper.connect();

        Dao<Thing> thingDao = ThingDaoBuilderNotNullSibling.buildDao(connection);

        Thing thing = new Thing();
        thing.setName("only child");

        try {
            thingDao.insert(thing);
            Assert.fail("Should not have inserted with a null sibling");
        } catch (HrormException expected){
        }
        connection.close();
    }

    @Test
    public void testDaoValidation() throws SQLException {
        Connection connection = helper.connect();
        Validator.validate(connection, ThingDaoBuilder);
        Validator.validate(connection, SecondCousinDaoBuilder);
        Validator.validate(connection, CousinDaoBuilder);
        Validator.validate(connection, SiblingDaoBuilder);
        Validator.validate(connection, ThingDaoBuilderNotNullSibling);
        connection.close();
    }

}
