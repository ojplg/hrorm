package org.hrorm.examples.immutables;

import org.hrorm.IndirectDaoBuilder;

public class DaoBuilders {

    public static IndirectDaoBuilder<ImmutableSibling, ImmutableSibling.ImmutableSiblingBuilder> immutableSiblingDaoBuilder(){
        return new IndirectDaoBuilder<>("immutable_sibling", ImmutableSibling::builder, ImmutableSibling.ImmutableSiblingBuilder::build )
                .withPrimaryKey("id", "immutable_sibling_seq", ImmutableSibling::getId, ImmutableSibling.ImmutableSiblingBuilder::id)
                .withStringColumn("data", ImmutableSibling::getData, ImmutableSibling.ImmutableSiblingBuilder::data)
                .withUniqueConstraint("id","data");
    }

    public static  IndirectDaoBuilder<ImmutableChild, ImmutableChild.ImmutableChildBuilder> immutableChildDaoBuilder() {
        return new IndirectDaoBuilder<>("immutable_child", ImmutableChild::builder, ImmutableChild.ImmutableChildBuilder::build)
                .withPrimaryKey("id", "immutable_child_seq", ImmutableChild::getId, ImmutableChild.ImmutableChildBuilder::id)
                .withBooleanColumn("flag", ImmutableChild::getFlag, ImmutableChild.ImmutableChildBuilder::flag)
                .withInstantColumn("birthday", ImmutableChild::getBirthday, ImmutableChild.ImmutableChildBuilder::birthday)
                .withJoinColumn("sibling_id", ImmutableChild::getImmutableSibling, ImmutableChild.ImmutableChildBuilder::immutableSibling, immutableSiblingDaoBuilder())
                .withParentColumn("thing_id");
    }

    public static final IndirectDaoBuilder<ImmutableThing, ImmutableThing.ImmutableThingBuilder> immutableObjectDaoBuilder(){
        return new IndirectDaoBuilder<>("immutable_thing", ImmutableThing::builder, ImmutableThing.ImmutableThingBuilder::build)
                .withPrimaryKey("id", "immutable_thing_seq", ImmutableThing::getId, ImmutableThing.ImmutableThingBuilder::id)
                .withBigDecimalColumn("amount", ImmutableThing::getAmount, ImmutableThing.ImmutableThingBuilder::amount )
                .withStringColumn("word", ImmutableThing::getWord, ImmutableThing.ImmutableThingBuilder::word)
                .withChildren(ImmutableThing::getChildren, ImmutableThing.ImmutableThingBuilder::children, immutableChildDaoBuilder());
    }

    public static final IndirectDaoBuilder<ImmutableSibling, ImmutableSibling.ImmutableSiblingBuilder> IMMUTABLE_SIBLING_DAO_BUILDER =
            immutableSiblingDaoBuilder();

    public static final IndirectDaoBuilder<ImmutableChild, ImmutableChild.ImmutableChildBuilder> IMMUTABLE_CHILD_DAO_BUILDER =
            immutableChildDaoBuilder();


    public static final IndirectDaoBuilder<ImmutableThing, ImmutableThing.ImmutableThingBuilder> IMMUTABLE_OBJECT_DAO_BUILDER =
            immutableObjectDaoBuilder();
}
