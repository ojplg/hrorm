package org.hrorm;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the <code>AssociationDao</code>.
 *
 * <p>
 *     There is no need to construct this yourself.
 *     Use the {@link AssociationDaoBuilder}.
 * </p>
 *
 * @param <LEFT> The type of one of the entities being associated.
 * @param <LEFTBUILDER> The type of the class that can construct new <code>LEFT</code> instances.
 * @param <RIGHT> The type of the other of the entities being associated.
 * @param <RIGHTBUILDER> The type of the class that can construct new <code>RIGHT</code> instances.
 */
public class AssociationDaoImpl<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> implements AssociationDao<LEFT, RIGHT> {

    private final Dao<Association<LEFT, RIGHT>> internalDao;
    private final String leftColumnName;
    private final String rightColumnName;
    private final PrimaryKey<Long,LEFT, LEFTBUILDER> leftPrimaryKey;
    private final PrimaryKey<Long,RIGHT, RIGHTBUILDER> rightPrimaryKey;

    public AssociationDaoImpl(Dao<Association<LEFT, RIGHT>> internalDao,
                              String leftColumnName,
                              String rightColumnName,
                              PrimaryKey<Long,LEFT, LEFTBUILDER> leftPrimaryKey,
                              PrimaryKey<Long,RIGHT, RIGHTBUILDER> rightPrimaryKey){
        this.internalDao = internalDao;
        this.leftColumnName = leftColumnName;
        this.rightColumnName = rightColumnName;
        this.leftPrimaryKey = leftPrimaryKey;
        this.rightPrimaryKey = rightPrimaryKey;
    }

    @Override
    public List<LEFT> selectLeftAssociates(RIGHT right) {
        Long rightId = rightPrimaryKey.getKey(right);
        Where where = new Where(rightColumnName, Operator.EQUALS, rightId);
        List<Association<LEFT, RIGHT>> associations = internalDao.select(where);
        return associations.stream().map(Association::getLeft).collect(Collectors.toList());
    }

    @Override
    public List<RIGHT> selectRightAssociates(LEFT left) {
        Long leftId = leftPrimaryKey.getKey(left);
        Where where = new Where(leftColumnName, Operator.EQUALS, leftId);
        List<Association<LEFT, RIGHT>> associations = internalDao.select(where);
        return associations.stream().map(Association::getRight).collect(Collectors.toList());
    }

    @Override
    public Long insertAssociation(LEFT left, RIGHT right) {
        Association<LEFT, RIGHT> association = new Association<>();
        association.setLeft(left);
        association.setRight(right);
        return internalDao.insert(association);
    }

    @Override
    public void deleteAssociation(LEFT left, RIGHT right) {
        Long rightId = rightPrimaryKey.getKey(right);
        Long leftId = leftPrimaryKey.getKey(left);
        Where where = new Where(leftColumnName, Operator.EQUALS, leftId)
                            .and(rightColumnName, Operator.EQUALS, rightId);
        List<Association<LEFT, RIGHT>> associations = internalDao.select(where);
        associations.forEach(internalDao::delete);
    }
}
