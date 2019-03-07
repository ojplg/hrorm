package org.hrorm;

import java.sql.Connection;

public class AssociationDaoBuilder<LEFT, RIGHT> {

    private final IndirectAssociationDaoBuilder<LEFT, LEFT, RIGHT, RIGHT> indirectDaoBuilder;

    public AssociationDaoBuilder(DaoDescriptor<LEFT, LEFT> leftDaoDescriptor, DaoDescriptor<RIGHT, RIGHT> rightDaoDescriptor){
        this.indirectDaoBuilder = new IndirectAssociationDaoBuilder<>(leftDaoDescriptor, rightDaoDescriptor);
    }

    public AssociationDao<LEFT, RIGHT> buildDao(Connection connection){
        return indirectDaoBuilder.buildDao(connection);
    }

    public AssociationDaoBuilder<LEFT, RIGHT> withTableName(String tableName) {
        indirectDaoBuilder.withTableName(tableName);
        return this;
    }

    public AssociationDaoBuilder<LEFT, RIGHT> withPrimaryKeyName(String primaryKeyName) {
        this.indirectDaoBuilder.withPrimaryKeyName(primaryKeyName);
        return this;
    }

    public AssociationDaoBuilder<LEFT, RIGHT> withSequenceName(String sequenceName) {
        this.indirectDaoBuilder.withSequenceName(sequenceName);
        return this;
    }

    public AssociationDaoBuilder<LEFT, RIGHT> withLeftColumnName(String leftColumnName) {
        this.indirectDaoBuilder.withLeftColumnName(leftColumnName);
        return this;
    }

    public AssociationDaoBuilder<LEFT, RIGHT> withRightColumnName(String rightColumnName) {
        this.indirectDaoBuilder.withRightColumnName(rightColumnName);
        return this;
    }
}
