package org.hrorm;

public enum ChildSelectStrategy {
    /**
     * The default strategy. A select will be issued for each child table of the entity
     * for each record found. This is the so-called N+1 Query problem.
     */
    Standard,
    /**
     * When reading a collection of entity records, for each child table,
     * issue a select statement with an in-clause containing the IDs of the
     * parent records. E.g. generate SQL that looks like
     * <code>
     *     SELECT * FROM CHILD WHERE PARENT_ID IN ( ... )
     * </code>
     */
    InClause,
    Subselect
}
