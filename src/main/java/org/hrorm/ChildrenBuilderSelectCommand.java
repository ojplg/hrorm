package org.hrorm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Does a selection of children in the case of a
 * deferred <code>ChildSelectionStrategy</code>.
 *
 * <p>
 *     Most users of hrorm will have no need to directly use this.
 * </p>
 *
 * @param <CHILD> The child type.
 * @param <CHILDBUILDER> The builder of the child type.
 */
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

        SelectionInstruction selectionInstruction = SelectionInstruction.withPrimaryKeySqlAndParentColumnName(
                sql, primaryKeySql, parentChildColumnName, ChildSelectStrategy.SubSelectInClause
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
            String parentColumnName,
            List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList){
        String sql = sqlBuilder.select();
        SelectionInstruction selectionInstruction = SelectionInstruction.forSelectAll(sql, parentColumnName);
        return sqlRunner.doSelection(selectionInstruction,  supplier, childrenDescriptorsList, new StatementPopulator.Empty());
    }

    private List<Envelope<CHILDBUILDER>> doSelectByIds(
            SqlBuilder<CHILD> sqlBuilder,
            Supplier<CHILDBUILDER> supplier,
            SqlRunner<CHILD, CHILDBUILDER> sqlRunner,
            String parentChildColumnName,
            List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList){
        Where where = Where.inLong(parentChildColumnName, new ArrayList<>(parentIds));
        String sql = sqlBuilder.select(where);
        SelectionInstruction selectionInstruction = SelectionInstruction.withParentColumnName(
                sql, parentChildColumnName, ChildSelectStrategy.ByKeysInClause);
        return sqlRunner.doSelection(selectionInstruction, supplier, childrenDescriptorsList, where);
    }
}
