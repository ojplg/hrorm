package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * A mechanism for building an {@link AssociationDao} for immutable
 * objects.
 *
 * @param <LEFT> The type of one of the entities being associated
 * @param <LEFTBUILDER> The builder type for that entity
 * @param <RIGHT> The type of the other of the entities being associated
 * @param <RIGHTBUILDER> The builder type for the other entity
 */
public class IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> implements AssociationDaoDescriptor {

    private final DaoDescriptor<LEFT, LEFTBUILDER> leftDaoDescriptor;
    private final DaoDescriptor<RIGHT, RIGHTBUILDER> rightDaoDescriptor;

    private String tableName;
    private String primaryKeyName;
    private String sequenceName;
    private String leftColumnName;
    private String rightColumnName;

    /**
     * Construct a new builder instance.
     *
     * @param leftDaoDescriptor the <code>DaoBuilder</code> or other descriptor
     *                          of one of the entities being associated
     * @param rightDaoDescriptor the <code>DaoBuilder</code> or other descriptor
     *                          of one of other of the entities being associated
     */
    public IndirectAssociationDaoBuilder(DaoDescriptor<LEFT, LEFTBUILDER> leftDaoDescriptor,
                                         DaoDescriptor<RIGHT, RIGHTBUILDER> rightDaoDescriptor){
        this.leftDaoDescriptor = leftDaoDescriptor;
        this.rightDaoDescriptor = rightDaoDescriptor;
    }

    /**
     * Creates a new <code>AssociationDao</code> using the passed <code>Connection</code>.
     *
     * @param connection a connection to the underlying data store
     * @return a newly constructed DAO
     */
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

    /**
     * Setter for the name of the association table in the database.
     *
     * @param tableName the database table name
     * @return this
     */
    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> withTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * Setter for the primary key column name of the association table in the database.
     *
     * @param primaryKeyName the database column representing the primary key in the association table
     * @return this
     */
    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> withPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
        return this;
    }

    /**
     * Setter for the name of the database sequence used to populate
     * the primary key of the association table
     *
     * @param sequenceName the database sequence name
     * @return this
     */
    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> withSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
        return this;
    }

    /**
     * The name of the column in the association table that references the
     * primary key of the left entity.
     *
     * @param leftColumnName The column name
     * @return this
     */
    public IndirectAssociationDaoBuilder<LEFT, LEFTBUILDER, RIGHT, RIGHTBUILDER> withLeftColumnName(String leftColumnName) {
        this.leftColumnName = leftColumnName;
        return this;
    }

    /**
     * The name of the column in the association table that references the
     * primary key of the right entity.
     *
     * @param rightColumnName The column name
     * @return this
     */
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

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    @Override
    public String getSequenceName() {
        return sequenceName;
    }

    @Override
    public String getLeftColumnName() {
        return leftColumnName;
    }

    @Override
    public String getRightColumnName() {
        return rightColumnName;
    }

    @Override
    public String getLeftTableName() {
        return leftDaoDescriptor.tableName();
    }

    @Override
    public String getRightTableName() {
        return rightDaoDescriptor.tableName();
    }

    @Override
    public String getLeftPrimaryKeyName() {
        return leftDaoDescriptor.primaryKey().getName();
    }

    @Override
    public String getRightPrimaryKeyName() {
        return rightDaoDescriptor.primaryKey().getName();
    }

    private boolean emptyString(String s){
        return s == null || s.isEmpty();
    }

    /**
     * Flag indicating whether or not all the necessary fields have been set.
     *
     * @return true if all fields have been set, false otherwise
     */
    public boolean ready(){
        return findMissingFields().size() == 0;
    }
}
