package org.hrorm;

import java.sql.Connection;

public class AssociationDaoBuilder<LEFT, RIGHT> {

    private final IndirectAssociationDaoBuilder<LEFT, LEFT, RIGHT, RIGHT> indirectDaoBuilder;

    public AssociationDaoBuilder(String tableName,
                                 String primaryKeyName,
                                 String sequenceName) {
        indirectDaoBuilder = new IndirectAssociationDaoBuilder<>(tableName, primaryKeyName, sequenceName);
    }

    public AssociationDao<LEFT, RIGHT> buildDao(Connection connection){
        return indirectDaoBuilder.buildDao(connection);
    }

    public AssociationDaoBuilder<LEFT, RIGHT> withLeft(String leftColumnName,
                                                       DaoDescriptor<LEFT, LEFT> leftDaoDescriptor){
        this.indirectDaoBuilder.withLeft(leftColumnName, leftDaoDescriptor);
        return this;
    }

    public AssociationDaoBuilder<LEFT, RIGHT> withRight(String rightColumnName,
                                                        DaoDescriptor<RIGHT, RIGHT> rightDaoDescriptor){
        this.indirectDaoBuilder.withRight(rightColumnName, rightDaoDescriptor);
        return this;
    }

}
