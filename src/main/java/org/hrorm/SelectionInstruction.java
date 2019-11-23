package org.hrorm;

/**
 * A container for some of the details of how a deferred selection
 * is supposed to work. This is used for certain {@link }ChildSelectStrategy}
 * variants.
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

    // MAYBE: These names could be better

    public static SelectionInstruction forSelectAll(String sql, String parentColumnName){
        return new SelectionInstruction(sql, null, null, parentColumnName, true);
    }

    public static SelectionInstruction simpleInstruction(String sql, ChildSelectStrategy childSelectStrategy){
        return new SelectionInstruction(sql, null, childSelectStrategy, null, false);
    }

    public static SelectionInstruction withPrimaryKeySql(String sql, String primaryKeySql, ChildSelectStrategy childSelectStrategy){
        return new SelectionInstruction(sql, primaryKeySql, childSelectStrategy, null, false);
    }

    public static SelectionInstruction withParentColumnName(String sql, String parentColumnName, ChildSelectStrategy childSelectStrategy){
        return new SelectionInstruction(sql, null, childSelectStrategy, parentColumnName, false);
    }

    public static SelectionInstruction withPrimaryKeySqlAndParentColumnName(String sql, String primaryKeySql, String parentColumnName, ChildSelectStrategy childSelectStrategy){
        return new SelectionInstruction(sql, primaryKeySql, childSelectStrategy, parentColumnName, false);
    }

    private SelectionInstruction(String selectSql, String primaryKeySql, ChildSelectStrategy childSelectStrategy, String parentColumnName, boolean selectAll) {
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
        return selectAll ||
                childSelectStrategy.equals(ChildSelectStrategy.SubSelectInClause) ||
                childSelectStrategy.equals(ChildSelectStrategy.ByKeysInClause);
    }

    public boolean isSelectAll() {
        return selectAll;
    }
}


