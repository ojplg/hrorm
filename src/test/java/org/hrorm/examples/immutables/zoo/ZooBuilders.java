package org.hrorm.examples.immutables.zoo;

import org.hrorm.ChildSelectStrategy;
import org.hrorm.DaoDescriptor;
import org.hrorm.IndirectDaoBuilder;

public class ZooBuilders {

    public static IndirectDaoBuilder<Room, Room.RoomBuilder> newRoomBuilder(ChildSelectStrategy childSelectStrategy,
                                                                            DaoDescriptor<Cage, Cage.CageBuilder> cageDaoDescriptor){
        return new IndirectDaoBuilder<>("room", Room.RoomBuilder::new, Room.RoomBuilder::build)
                .withPrimaryKey("id", "room_seq", Room::getId, Room.RoomBuilder::id)
                .withStringColumn("name", Room::getName, Room.RoomBuilder::name)
                .withJoinColumn("cage_id", Room::getCage, Room.RoomBuilder::cage, cageDaoDescriptor)
                .withChildSelectStrategy(childSelectStrategy);
    }

    public static IndirectDaoBuilder<Cage, Cage.CageBuilder> newCageBuilder(ChildSelectStrategy childSelectStrategy,
                                                                            DaoDescriptor<Parrot,Parrot.ParrotBuilder> parrotDaoDescriptor){
        return new IndirectDaoBuilder<>("cage", Cage.CageBuilder::new, Cage.CageBuilder::build)
                .withPrimaryKey("id", "cage_seq", Cage::getId, Cage.CageBuilder::id)
                .withBigDecimalColumn("area", Cage::getArea, Cage.CageBuilder::area)
                .withChildren(Cage::getParrots, Cage.CageBuilder::parrots, parrotDaoDescriptor)
                .withChildSelectStrategy(childSelectStrategy);
    }

    public static IndirectDaoBuilder<Parrot, Parrot.ParrotBuilder> newParrotBuilder(ChildSelectStrategy strategy){
        return new IndirectDaoBuilder<>("parrot", Parrot.ParrotBuilder::new, Parrot.ParrotBuilder::build)
                .withPrimaryKey("id", "parrot_seq", Parrot::getId, Parrot.ParrotBuilder::id)
                .withStringColumn("name", Parrot::getName, Parrot.ParrotBuilder::name)
                .withParentColumn("cage_id")
                .withChildSelectStrategy(strategy);
    }
}
