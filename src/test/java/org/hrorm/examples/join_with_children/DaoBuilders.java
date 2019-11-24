package org.hrorm.examples.join_with_children;

import org.hrorm.DaoBuilder;

public class DaoBuilders {

    public static DaoBuilder<Pea> basePeaDaoBuilder(){
        return new DaoBuilder<>("pea", Pea::new)
                .withPrimaryKey("id", "pea_seq", Pea::getId, Pea::setId)
                .withStringColumn("flag", Pea::getFlag, Pea::setFlag)
                .withParentColumn("pod_id");
    }

    public static DaoBuilder<Pod> basePodDaoBuilder(){
        return new DaoBuilder<>("pod", Pod::new)
                .withPrimaryKey("id", "pod_seq", Pod::getId, Pod::setId)
                .withStringColumn("mark", Pod::getMark, Pod::setMark)
                .withChildren(Pod::getPeas, Pod::setPeas, basePeaDaoBuilder());
    }

    public static DaoBuilder<Stem> baseStemDaoBuilder(DaoBuilder<Pod> podDaoBuilder){
        return new DaoBuilder<>("stem", Stem::new)
                .withPrimaryKey("id", "stem_seq", Stem::getId, Stem::setId)
                .withStringColumn("tag", Stem::getTag, Stem::setTag)
                .withJoinColumn("pod_id", Stem::getPod, Stem::setPod, podDaoBuilder);
    }

    public static DaoBuilder<Root> baseRootDaoBuilder(DaoBuilder<Stem> stemDaoBuilder){
        return new DaoBuilder<>("root", Root::new)
                .withPrimaryKey("id", "root_seq", Root::getId, Root::setId)
                .withLongColumn("number", Root::getNumber, Root::setNumber)
                .withJoinColumn("stem_id", Root::getStem, Root::setStem, stemDaoBuilder);
    }

}
