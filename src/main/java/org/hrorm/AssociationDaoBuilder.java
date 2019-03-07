package org.hrorm;

import java.sql.Connection;

public class AssociationDaoBuilder<LEFT, RIGHT> {

    private final String tableName;
    private final String primaryKeyName;
    private final String sequenceName;

    private String leftColumnName;
    private DaoDescriptor<LEFT, LEFT> leftDaoDescriptor;

    private String rightColumnName;
    private DaoDescriptor<RIGHT, RIGHT> rightDaoDescriptor;

    public AssociationDaoBuilder(String tableName,
                                 String primaryKeyName,
                                 String sequenceName) {
        this.tableName = tableName;
        this.primaryKeyName = primaryKeyName;
        this.sequenceName = sequenceName;
    }

    public AssociationDao<LEFT, RIGHT> buildDao(Connection connection){
        throw new UnsupportedOperationException();
    }

    public AssociationDaoBuilder<LEFT, RIGHT> withLeft(String leftColumnName,
                                                       DaoDescriptor<LEFT, LEFT> leftDaoDescriptor){
        this.leftColumnName = leftColumnName;
        this.leftDaoDescriptor = leftDaoDescriptor;
        return this;
    }

    public AssociationDaoBuilder<LEFT, RIGHT> withRight(String rightColumnName,
                                                        DaoDescriptor<RIGHT, RIGHT> rightDaoDescriptor){
        this.rightColumnName = rightColumnName;
        this.rightDaoDescriptor = rightDaoDescriptor;
        return this;
    }

}
