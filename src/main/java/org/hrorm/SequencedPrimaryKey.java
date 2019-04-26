package org.hrorm;

public interface SequencedPrimaryKey<ENTITY, BUILDER> extends PrimaryKey<Long, ENTITY, BUILDER> {
    /**
     * The name of the database sequence that is used to populate this key
     *
     * @return the sequence name
     */
    String getSequenceName();
}
