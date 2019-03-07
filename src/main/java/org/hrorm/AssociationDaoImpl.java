package org.hrorm;

import java.util.List;
import java.util.stream.Collectors;

public class AssociationDaoImpl<LEFT, RIGHT> implements AssociationDao<LEFT, RIGHT> {

    private final Dao<Association<LEFT, RIGHT>> internalDao;
    private final String leftColumnName;
    private final String rightColumnName;
    private final PrimaryKey<LEFT, LEFT> leftPrimaryKey;
    private final PrimaryKey<RIGHT, RIGHT> rightPrimaryKey;

    public AssociationDaoImpl(Dao<Association<LEFT, RIGHT>> internalDao,
                              String leftColumnName,
                              String rightColumnName,
                              PrimaryKey<LEFT, LEFT> leftPrimaryKey,
                              PrimaryKey<RIGHT, RIGHT> rightPrimaryKey){
        this.internalDao = internalDao;
        this.leftColumnName = leftColumnName;
        this.rightColumnName = rightColumnName;
        this.leftPrimaryKey = leftPrimaryKey;
        this.rightPrimaryKey = rightPrimaryKey;
    }

    @Override
    public List<LEFT> findLeftAssociates(RIGHT right) {
        Long rightId = rightPrimaryKey.getKey(right);
        Where where = new Where(rightColumnName, Operator.EQUALS, rightId);
        List<Association<LEFT, RIGHT>> associations = internalDao.select(where);
        List<LEFT> lefts = associations.stream().map(assoc -> assoc.getLeft()).collect(Collectors.toList());
        return lefts;
    }

    @Override
    public List<RIGHT> findRightAssociates(LEFT left) {
        Long leftId = leftPrimaryKey.getKey(left);
        Where where = new Where(leftColumnName, Operator.EQUALS, leftId);
        List<Association<LEFT, RIGHT>> associations = internalDao.select(where);
        List<RIGHT> rights = associations.stream().map(assoc -> assoc.getRight()).collect(Collectors.toList());
        return rights;
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
        associations.forEach(assoc -> internalDao.delete(assoc));
    }
}
