package org.hrorm.examples.parentage;

import org.hrorm.DaoBuilder;
import org.hrorm.examples.EnumeratedColorConverter;

public class ParentChildBuilders {

    public static DaoBuilder<Grandchild> GrandchildDaoBuilder =
            new DaoBuilder<>("grandchild_table", Grandchild::new)
                    .withPrimaryKey("id", "grandchild_seq", Grandchild::getId, Grandchild::setId)
                    .withParentColumn("child_table_id", Grandchild::getChild, Grandchild::setChild)
                    .withConvertingStringColumn("color", Grandchild::getColor, Grandchild::setColor, new EnumeratedColorConverter());

    public static DaoBuilder<Child> ChildDaoBuilder =
            new DaoBuilder<>("child_table", Child::new)
                    .withPrimaryKey("id", "child_seq", Child::getId, Child::setId)
                    .withLongColumn("number", Child::getNumber, Child::setNumber)
                    .withParentColumn("parent_table_id", Child::getParent, Child::setParent)
                    .withChildren(Child::getGrandchildList, Child::setGrandchildList, GrandchildDaoBuilder);

    public static DaoBuilder<Parent> ParentDaoBuilder =
            new DaoBuilder<>("parent_table", Parent::new)
                    .withPrimaryKey("id", "parent_seq", Parent::getId, Parent::setId)
                    .withStringColumn("name", Parent::getName, Parent::setName)
                    .withChildren(Parent::getChildList, Parent::setChildList, ChildDaoBuilder);

}
