package org.hrorm;

import org.hrorm.database.Helper;
import org.hrorm.database.HelperFactory;
import org.hrorm.examples.immutables.zoo.Cage;
import org.hrorm.examples.immutables.zoo.Parrot;
import org.hrorm.examples.immutables.zoo.Room;
import org.hrorm.examples.immutables.zoo.ZooBuilders;
import org.hrorm.util.AssertHelp;
import org.hrorm.util.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class ImmutableJoinChildrenTest {

    private static Helper HELPER = HelperFactory.forSchema("zoo");

    @BeforeClass
    public static void setUpDb(){

        IndirectDaoBuilder<Parrot, Parrot.ParrotBuilder> parrotDaoBuilder =
                ZooBuilders.newParrotBuilder(ChildSelectStrategy.Standard);
        IndirectDaoBuilder<Cage, Cage.CageBuilder> cageDaoBuilder =
                ZooBuilders.newCageBuilder(ChildSelectStrategy.Standard, parrotDaoBuilder);
        IndirectDaoBuilder<Room, Room.RoomBuilder> roomDaoBuilder =
                ZooBuilders.newRoomBuilder(ChildSelectStrategy.Standard, cageDaoBuilder);

        Schema schema = new Schema(
                parrotDaoBuilder,
                cageDaoBuilder,
                roomDaoBuilder
        );

        HELPER.initializeSchemaFromSql(schema.sql());
    }

    @AfterClass
    public static void cleanUpDb(){
        HELPER.dropSchema();
    }


    private void insertAndSelectOne(ChildSelectStrategy childSelectStrategy){

        final List<String> parrotNames = RandomUtils.aFewRandomStrings();
        final BigDecimal area = RandomUtils.bigDecimal();
        final String roomName = RandomUtils.randomAlphabeticString(5,10);

        final Long cageId = HELPER.useConnection(connection -> {

            IndirectDaoBuilder<Parrot, Parrot.ParrotBuilder> parrotDaoBuilder =
                    ZooBuilders.newParrotBuilder(childSelectStrategy);
            IndirectDaoBuilder<Cage, Cage.CageBuilder> cageDaoBuilder =
                    ZooBuilders.newCageBuilder(childSelectStrategy, parrotDaoBuilder);

            Dao<Cage> cageDao = cageDaoBuilder.buildDao(connection);
            Cage newCage = Cage.newCage(area, parrotNames);

            return cageDao.insert(newCage);
        });
        final Long roomId = HELPER.useConnection(connection -> {

            IndirectDaoBuilder<Parrot, Parrot.ParrotBuilder> parrotDaoBuilder =
                    ZooBuilders.newParrotBuilder(childSelectStrategy);
            IndirectDaoBuilder<Cage, Cage.CageBuilder> cageDaoBuilder =
                    ZooBuilders.newCageBuilder(childSelectStrategy, parrotDaoBuilder);
            IndirectDaoBuilder<Room, Room.RoomBuilder> roomDaoBuilder =
                    ZooBuilders.newRoomBuilder(childSelectStrategy, cageDaoBuilder);

            Dao<Cage> cageDao = cageDaoBuilder.buildDao(connection);
            Dao<Room> roomDao = roomDaoBuilder.buildDao(connection);

            Cage cage = cageDao.selectOne(cageId);

            Assert.assertEquals(parrotNames, cage.getParrotNames());

            Room room = Room.newRoom(roomName, cage);

            return roomDao.insert(room);
        });

        HELPER.useConnection(connection -> {
            IndirectDaoBuilder<Parrot, Parrot.ParrotBuilder> parrotDaoBuilder =
                    ZooBuilders.newParrotBuilder(childSelectStrategy);
            IndirectDaoBuilder<Cage, Cage.CageBuilder> cageDaoBuilder =
                    ZooBuilders.newCageBuilder(childSelectStrategy, parrotDaoBuilder);
            IndirectDaoBuilder<Room, Room.RoomBuilder> roomDaoBuilder =
                    ZooBuilders.newRoomBuilder(childSelectStrategy, cageDaoBuilder);

            Dao<Room> roomDao = roomDaoBuilder.buildDao(connection);

            Room room = roomDao.selectOne(roomId);

            Assert.assertEquals(roomName, room.getName());
            Assert.assertEquals(area, room.getCageArea());
            //AssertHelp.sameContents(parrotNames, room.getParrotNames());
        });

    }

    @Test
    public void testStandardStrategyOneInstance(){
        insertAndSelectOne(ChildSelectStrategy.Standard);
    }

}
