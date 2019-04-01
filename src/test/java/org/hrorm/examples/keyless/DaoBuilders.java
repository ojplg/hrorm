package org.hrorm.examples.keyless;

import org.hrorm.DaoBuilder;
import org.hrorm.IndirectKeylessDaoBuilder;
import org.hrorm.Schema;

public class DaoBuilders {

    public static final DaoBuilder<Sibling> SIBLING_DAO_BUILDER =
            new DaoBuilder<>("sibling", Sibling::new)
                    .withPrimaryKey("id", "sibling_seq", Sibling::getId, Sibling::setId)
                    .withStringColumn("name", Sibling::getName, Sibling::setName);

    public static final IndirectKeylessDaoBuilder<UnkeyedThing, UnkeyedThing> UNKEYED_THING_DAO_BUILDER =
            new IndirectKeylessDaoBuilder<>("unkeyed_thing", UnkeyedThing::new, t -> t)
                    .withStringColumn("name", UnkeyedThing::getName, UnkeyedThing::setName)
                    .withJoinColumn("sibling_id", UnkeyedThing::getSibling, UnkeyedThing::setSibling, SIBLING_DAO_BUILDER);

    public static final Schema SCHEMA = new Schema(SIBLING_DAO_BUILDER, UNKEYED_THING_DAO_BUILDER);

}
