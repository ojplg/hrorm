package org.hrorm.examples.back_reference_parents;

import org.hrorm.DaoBuilder;

public class SimpleParentChildDaos {

    public static final DaoBuilder<SimpleChild> CHILD =
            new DaoBuilder<>("simple_child_table", SimpleChild::new)
                    .withPrimaryKey("id", "simple_child_seq", SimpleChild::getId, SimpleChild::setId)
                    .withStringColumn("name", SimpleChild::getName, SimpleChild::setName)
                    .withParentColumn("parent_id", SimpleChild::getParent, SimpleChild::setParent);

    public static final DaoBuilder<SimpleParent> PARENT =
            new DaoBuilder<>("simple_parent_table", SimpleParent::new)
                    .withPrimaryKey("id", "simple_child_seq", SimpleParent::getId, SimpleParent::setId)
                    .withStringColumn("name", SimpleParent::getName, SimpleParent::setName)
                    .withChildren(SimpleParent::getChildren, SimpleParent::setChildren, CHILD);

}
