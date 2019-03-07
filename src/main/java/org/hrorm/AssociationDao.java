package org.hrorm;

import java.util.List;

public interface AssociationDao<LEFT,RIGHT> {

    List<LEFT> findLeftAssociates(RIGHT right);
    List<RIGHT> findRightAssociates(LEFT left);

    Long insertAssociation(LEFT left, RIGHT right);
    Long deleteAssociation(LEFT left, RIGHT right);

}
