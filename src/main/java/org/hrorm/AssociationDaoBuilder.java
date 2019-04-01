package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A mechanism for building an {@link AssociationDao}.
 *
 * @param <LEFT> The type of one of the entities being associated
 * @param <RIGHT> The type of the other of the entities being associated
 */
public class AssociationDaoBuilder<LEFT, RIGHT>
        implements SchemaDescriptor<Association<LEFT, RIGHT>, Association<LEFT, RIGHT>> {

    private final DaoDescriptor<LEFT, ?> leftDaoDescriptor;
    private final DaoDescriptor<RIGHT, ?> rightDaoDescriptor;

    private String tableName;
    private String primaryKeyName;
    private String sequenceName;
    private String leftColumnName;
    private String rightColumnName;

    private DaoBuilder<Association<LEFT, RIGHT>> internalDaoBuilder;

    /**
     * Construct a new builder instance.
     *
     * @param leftDaoDescriptor the <code>DaoBuilder</code> or other descriptor
     *                          of one of the entities being associated
     * @param rightDaoDescriptor the <code>DaoBuilder</code> or other descriptor
     *                          of one of other of the entities being associated
     */
    public AssociationDaoBuilder(DaoDescriptor<LEFT, ?> leftDaoDescriptor,
                                         DaoDescriptor<RIGHT, ?> rightDaoDescriptor){
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

        prepareDaoBuilder();

        Dao<Association<LEFT, RIGHT>> internalDao = internalDaoBuilder.buildDao(connection);

        return new AssociationDaoImpl<>(
                internalDao,
                leftColumnName,
                rightColumnName,
                leftDaoDescriptor.primaryKey(),
                rightDaoDescriptor.primaryKey()
        );
    }

    private void prepareDaoBuilder(){
        if( internalDaoBuilder != null ){
            return;
        }

        if( ! ready() ){
            List<String> missingFields = findMissingFields();
            String messageDetail = String.join(", ", missingFields);
            throw new HrormException("Need to set all fields before building the association dao. Missing: " + messageDetail);
        }

        internalDaoBuilder =
                new DaoBuilder<Association<LEFT, RIGHT>>(tableName, Association::new)
                        .withPrimaryKey(primaryKeyName, sequenceName, Association::getId, Association::setId)
                        .withJoinColumn(leftColumnName, Association::getLeft, Association::setLeft, leftDaoDescriptor).notNull()
                        .withJoinColumn(rightColumnName, Association::getRight, Association::setRight, rightDaoDescriptor).notNull();
    }

    /**
     * Setter for the name of the association table in the database.
     *
     * @param tableName the database table name
     * @return this
     */
    public AssociationDaoBuilder<LEFT, RIGHT> withTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * Setter for the primary key column name of the association table in the database.
     *
     * @param primaryKeyName the database column representing the primary key in the association table
     * @return this
     */
    public AssociationDaoBuilder<LEFT, RIGHT> withPrimaryKeyName(String primaryKeyName) {
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
    public AssociationDaoBuilder<LEFT, RIGHT> withSequenceName(String sequenceName) {
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
    public AssociationDaoBuilder<LEFT, RIGHT> withLeftColumnName(String leftColumnName) {
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
    public AssociationDaoBuilder<LEFT, RIGHT> withRightColumnName(String rightColumnName) {
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

    /**
     * Flag indicating whether or not all the necessary fields have been set.
     *
     * @return true if all fields have been set, false otherwise
     */
    public boolean ready(){
        return findMissingFields().size() == 0;
    }

    @Override
    public PrimaryKey<Association<LEFT, RIGHT>, Association<LEFT, RIGHT>> primaryKey() {
        prepareDaoBuilder();
        return internalDaoBuilder.primaryKey();
    }

    @Override
    public String tableName() {
        prepareDaoBuilder();
        return internalDaoBuilder.tableName();
    }

    @Override
    public Supplier<Association<LEFT, RIGHT>> supplier() {
        prepareDaoBuilder();
        return internalDaoBuilder.supplier();
    }

    @Override
    public List<ChildrenDescriptor<Association<LEFT, RIGHT>, ?, Association<LEFT, RIGHT>, ?>> childrenDescriptors() {
        prepareDaoBuilder();
        return internalDaoBuilder.childrenDescriptors();
    }

    @Override
    public ColumnCollection<Association<LEFT, RIGHT>, Association<LEFT, RIGHT>> getColumnCollection() {
        return internalDaoBuilder.getColumnCollection();
    }

    @Override
    public ParentColumn<Association<LEFT, RIGHT>, ?, Association<LEFT, RIGHT>, ?> parentColumn() {
        prepareDaoBuilder();
        return internalDaoBuilder.parentColumn();
    }

    @Override
    public Function<Association<LEFT, RIGHT>, Association<LEFT, RIGHT>> buildFunction() {
        return internalDaoBuilder.buildFunction();
    }

    @Override
    public List<List<String>> uniquenessConstraints() {
        return Collections.emptyList();
    }
}
