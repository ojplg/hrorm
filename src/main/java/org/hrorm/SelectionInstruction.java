package org.hrorm;

public class SelectionInstruction {

    private final String selectSql;
    private final String primaryKeySql;
    private final ChildSelectStrategy childSelectStrategy;
    private final String parentColumnName;

    public SelectionInstruction(String selectSql, String primaryKeySql, ChildSelectStrategy childSelectStrategy, String parentColumnName) {
        this.selectSql = selectSql;
        this.primaryKeySql = primaryKeySql;
        this.childSelectStrategy = childSelectStrategy;
        this.parentColumnName = parentColumnName;
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
}
