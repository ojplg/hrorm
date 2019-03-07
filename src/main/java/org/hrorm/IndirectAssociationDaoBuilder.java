package org.hrorm;

import java.sql.Connection;

public class IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> {

    private final String tableName;
    private final String primaryKeyName;
    private final String sequenceName;

    private String leftColumnName;
    private DaoDescriptor<LEFT, LEFTBUILDER> leftDaoDescriptor;

    private String rightColumnName;
    private DaoDescriptor<RIGHT, RIGHTBUILDER> rightDaoDescriptor;

    public IndirectAssociationDaoBuilder(String tableName,
                                         String primaryKeyName,
                                         String sequenceName) {
        this.tableName = tableName;
        this.primaryKeyName = primaryKeyName;
        this.sequenceName = sequenceName;
    }

    public AssociationDao<LEFT, RIGHT> buildDao(Connection connection){
        DaoBuilder<Association<LEFT, RIGHT>> internalDaoBuilder =
                new DaoBuilder<Association<LEFT, RIGHT>>(tableName, Association::new)
                        .withPrimaryKey(primaryKeyName, sequenceName, Association::getId, Association::setId)
                        .withJoinColumn(leftColumnName, Association::getLeft, Association::setLeft, leftDaoDescriptor)
                        .withJoinColumn(rightColumnName, Association::getRight, Association::setRight, rightDaoDescriptor);

        Dao<Association<LEFT, RIGHT>> internalDao = internalDaoBuilder.buildDao(connection);

        return new AssociationDaoImpl<>(
                internalDao,
                leftColumnName,
                rightColumnName,
                leftDaoDescriptor.primaryKey(),
                rightDaoDescriptor.primaryKey()
        );
    }

    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER>
    withLeft(String leftColumnName, DaoDescriptor<LEFT, LEFTBUILDER> leftDaoDescriptor){
        this.leftColumnName = leftColumnName;
        this.leftDaoDescriptor = leftDaoDescriptor;
        return this;
    }

    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER>
    withRight(String rightColumnName, DaoDescriptor<RIGHT, RIGHTBUILDER> rightDaoDescriptor){
        this.rightColumnName = rightColumnName;
        this.rightDaoDescriptor = rightDaoDescriptor;
        return this;
    }

}
