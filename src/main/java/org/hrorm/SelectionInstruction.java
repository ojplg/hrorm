package org.hrorm;


// TODO: Rename?

/**
 * A description of how a selection is supposed to be done.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class SelectionInstruction {

    private final String selectSql;
    private final String primaryKeySql;
    private final ChildSelectStrategy childSelectStrategy;
    private final String parentColumnName;
    private final boolean selectAll;

    // TODO: Create a public static factory methods to make this cleaner

    public static SelectionInstruction forSelectAll(String sql, ChildSelectStrategy childSelectStrategy){
        // TODO: This should be a null select strategy
        return new SelectionInstruction(sql, null, childSelectStrategy, null, true);
    }


    public SelectionInstruction(String selectSql, String primaryKeySql, ChildSelectStrategy childSelectStrategy, String parentColumnName, boolean selectAll) {
        this.selectSql = selectSql;
        this.primaryKeySql = primaryKeySql;
        this.childSelectStrategy = childSelectStrategy;
        this.parentColumnName = parentColumnName;
        this.selectAll = selectAll;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public String getPrimaryKeySql() {
        return primaryKeySql;
    }

    public ChildSelectStrategy getChildSelectStrategy() {
        return childSelectStrategy;
    }

    public String getParentColumnName() {
        return parentColumnName;
    }

    public boolean isBulkChildSelectStrategy(){
        return childSelectStrategy.equals(ChildSelectStrategy.SubSelectInClause) ||
                childSelectStrategy.equals(ChildSelectStrategy.ByKeysInClause);
    }

    public boolean isSelectAll() {
        return selectAll;
    }
}


