package org.hrorm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ChildrenBuilderSelectCommand<CHILD,CHILDBUILDER> {

    private enum SelectionType {
        SelectOne,
        SelectAll,
        SelectByIds,
        SubSelect
    }

    private final String primaryKeySelect;
    private final StatementPopulator statementPopulator;
    private final Collection<Long> parentIds;
    private final SelectionType selectionType;

    public static ChildrenBuilderSelectCommand forSubSelect(String primaryKeySelect, StatementPopulator statementPopulator){
        return new ChildrenBuilderSelectCommand(primaryKeySelect,
                statementPopulator,
                Collections.emptyList(),
                SelectionType.SubSelect);
    }

    public static ChildrenBuilderSelectCommand forSelectByIds(Collection<Long> parentIds){
        return new ChildrenBuilderSelectCommand(
                null,
                null,
                parentIds,
                SelectionType.SelectByIds);
    }

    public static ChildrenBuilderSelectCommand forSelectAll(){
        return new ChildrenBuilderSelectCommand(
                null,
                null,
                Collections.emptyList(),
                SelectionType.SelectAll);
    }

    private ChildrenBuilderSelectCommand(String primaryKeySelect,
                                         StatementPopulator statementPopulator,
                                         Collection<Long> parentIds,
                                         SelectionType selectionType) {
        this.primaryKeySelect = primaryKeySelect;
        this.statementPopulator = statementPopulator;
        this.parentIds = parentIds;
        this.selectionType = selectionType;
    }

    public List<Envelope<CHILDBUILDER>> select(
            SqlBuilder<CHILD> sqlBuilder,
            Supplier<CHILDBUILDER> supplier,
            SqlRunner<CHILD, CHILDBUILDER> sqlRunner,
            String parentChildColumnName,
            List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList) {
        switch (selectionType){
            case SelectAll:
                return doSelectAll(sqlBuilder, supplier, sqlRunner, parentChildColumnName, childrenDescriptorsList);
            case SelectByIds:
                return doSelectByIds(sqlBuilder, supplier, sqlRunner, parentChildColumnName, childrenDescriptorsList);
            case SubSelect:
                return doSelectWithSubSelect(sqlBuilder, supplier, sqlRunner, parentChildColumnName, childrenDescriptorsList);
            default:
                throw new HrormException("Unrecognized selection type " + selectionType);
        }
    }

    private List<Envelope<CHILDBUILDER>> doSelectWithSubSelect(
            SqlBuilder<CHILD> sqlBuilder,
            Supplier<CHILDBUILDER> supplier,
            SqlRunner<CHILD, CHILDBUILDER> sqlRunner,
            String parentChildColumnName,
            List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList){
        String primaryKeySql = sqlBuilder.selectPrimaryKey(primaryKeySelect);
        String sql = sqlBuilder.selectByParentSubSelect(primaryKeySelect);

        SelectionInstruction selectionInstruction = new SelectionInstruction(
                sql, primaryKeySql, ChildSelectStrategy.ByKeysInClause, parentChildColumnName, false
        );

        return sqlRunner.doSelection(
                selectionInstruction,
                supplier,
                childrenDescriptorsList,
                statementPopulator);
    }

    private List<Envelope<CHILDBUILDER>> doSelectAll(
            SqlBuilder<CHILD> sqlBuilder,
            Supplier<CHILDBUILDER> supplier,
            SqlRunner<CHILD, CHILDBUILDER> sqlRunner,
            String parentChildColumnName,
            List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList){
        String sql = sqlBuilder.select();
        return sqlRunner.selectAllAndSelectAllChildren(
                sql,
                supplier,
                childrenDescriptorsList,
                parentChildColumnName);
    }

    private List<Envelope<CHILDBUILDER>> doSelectByIds(
            SqlBuilder<CHILD> sqlBuilder,
            Supplier<CHILDBUILDER> supplier,
            SqlRunner<CHILD, CHILDBUILDER> sqlRunner,
            String parentChildColumnName,
            List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList){
        Where where = Where.inLong(parentChildColumnName, new ArrayList<>(parentIds));
        String sql = sqlBuilder.select(where);
        SelectionInstruction selectionInstruction = new SelectionInstruction(
                sql, null, ChildSelectStrategy.ByKeysInClause, parentChildColumnName, false);
        return sqlRunner.doSelection(selectionInstruction, supplier, childrenDescriptorsList, where);
    }
}
