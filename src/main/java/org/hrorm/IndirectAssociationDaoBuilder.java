package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> {

    private  String tableName;
    private  String primaryKeyName;
    private  String sequenceName;

    private String leftColumnName;
    private DaoDescriptor<LEFT, LEFTBUILDER> leftDaoDescriptor;

    private String rightColumnName;
    private DaoDescriptor<RIGHT, RIGHTBUILDER> rightDaoDescriptor;

    public IndirectAssociationDaoBuilder(DaoDescriptor<LEFT, LEFTBUILDER> leftDaoDescriptor,
                                         DaoDescriptor<RIGHT, RIGHTBUILDER> rightDaoDescriptor){
        this.leftDaoDescriptor = leftDaoDescriptor;
        this.rightDaoDescriptor = rightDaoDescriptor;
    }

    public AssociationDao<LEFT, RIGHT> buildDao(Connection connection){

        if( ! ready() ){
            List<String> missingFields = findMissingFields();
            String messageDetail = String.join(", ", missingFields);
            throw new HrormException("Need to set all fields before building the association dao. Missing: " + messageDetail);
        }

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

    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> withTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> withPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
        return this;
    }

    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> withSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
        return this;
    }

    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> withLeftColumnName(String leftColumnName) {
        this.leftColumnName = leftColumnName;
        return this;
    }

    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> withRightColumnName(String rightColumnName) {
        this.rightColumnName = rightColumnName;
        return this;
    }

    private List<String> findMissingFields(){
        List<String> missing = new ArrayList<>();

        if( emptyString(tableName) ){
            missing.add("TableName");
        }
        if( emptyString(primaryKeyName) ){
            missing.add("PrimaryKeyName");
        }
        if( emptyString(sequenceName) ){
            missing.add("SequenceName");
        }
        if( emptyString(leftColumnName) ){
            missing.add("LeftColumnName");
        }
        if( emptyString(rightColumnName) ){
            missing.add("RightColumnName");
        }

        return missing;
    }

    private boolean emptyString(String s){
        return s == null || s.isEmpty();
    }

    public boolean ready(){
        return findMissingFields().size() == 0;
    }
}
