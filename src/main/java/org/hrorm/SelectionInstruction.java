package org.hrorm;

public class SelectionInstruction {

    private final String selectSql;
    private final String primaryKeySql;
    private final ChildSelectStrategy childSelectStrategy;
    private final String parentColumnName;
    private final boolean selectAll;

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


