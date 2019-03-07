package org.hrorm;

import java.sql.Connection;

/**
 * A mechanism for building an {@link AssociationDao}.
 *
 * @param <LEFT> The type of one of the entities being associated
 * @param <RIGHT> The type of the other of the entities being associated
 */
public class AssociationDaoBuilder<LEFT, RIGHT> {

    private final IndirectAssociationDaoBuilder<LEFT, LEFT, RIGHT, RIGHT> indirectDaoBuilder;

    /**
     * Construct a new builder instance.
     *
     * @param leftDaoDescriptor the <code>DaoBuilder</code> or other descriptor
     *                          of one of the entities being associated
     * @param rightDaoDescriptor the <code>DaoBuilder</code> or other descriptor
     *                          of one of other of the entities being associated
     */
    public AssociationDaoBuilder(DaoDescriptor<LEFT, LEFT> leftDaoDescriptor, DaoDescriptor<RIGHT, RIGHT> rightDaoDescriptor){
        this.indirectDaoBuilder = new IndirectAssociationDaoBuilder<>(leftDaoDescriptor, rightDaoDescriptor);
    }

    /**
     * Creates a new <code>AssociationDao</code> using the passed <code>Connection</code>.
     *
     * @param connection a connection to the underlying data store
     * @return a newly constructed DAO
     */
    public AssociationDao<LEFT, RIGHT> buildDao(Connection connection){
        return indirectDaoBuilder.buildDao(connection);
    }

    /**
     * Setter for the name of the association table in the database.
     *
     * @param tableName the database table name
     * @return this
     */
    public AssociationDaoBuilder<LEFT, RIGHT> withTableName(String tableName) {
        indirectDaoBuilder.withTableName(tableName);
        return this;
    }

    /**
     * Setter for the primary key column name of the association table in the database.
     *
     * @param primaryKeyName the database column representing the primary key in the association table
     * @return this
     */
    public AssociationDaoBuilder<LEFT, RIGHT> withPrimaryKeyName(String primaryKeyName) {
        this.indirectDaoBuilder.withPrimaryKeyName(primaryKeyName);
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
        this.indirectDaoBuilder.withSequenceName(sequenceName);
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
        this.indirectDaoBuilder.withLeftColumnName(leftColumnName);
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
        this.indirectDaoBuilder.withRightColumnName(rightColumnName);
        return this;
    }
}
