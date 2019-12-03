package org.hrorm;

import java.util.List;
import java.util.function.Supplier;

public interface ChildrenSelector<CHILD, CHILDBUILDER> {

    List<Envelope<CHILDBUILDER>> select(
            SqlBuilder<CHILD> sqlBuilder,
            Supplier<CHILDBUILDER> supplier,
            SqlRunner<CHILD, CHILDBUILDER> sqlRunner,
            String parentChildColumnName,
            List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList);

    class SelectAllChildren<CHILD, CHILDBUILDER> implements ChildrenSelector<CHILD, CHILDBUILDER> {

        @Override
        public List<Envelope<CHILDBUILDER>> select(SqlBuilder<CHILD> sqlBuilder,
                                                   Supplier<CHILDBUILDER> supplier,
                                                   SqlRunner<CHILD, CHILDBUILDER> sqlRunner,
                                                   String parentChildColumnName,
                                                   List<ChildrenDescriptor<CHILD, ?, CHILDBUILDER, ?>> childrenDescriptorsList) {
            String sql = sqlBuilder.select();
            SelectionInstruction selectionInstruction = SelectionInstruction.forSelectAll(sql, parentChildColumnName);
            return sqlRunner.doSelection(selectionInstruction,  supplier, childrenDescriptorsList, new StatementPopulator.Empty());
        }
    }

    class SubSelectChildren<CHILD,CHILDBUILDER> implements  ChildrenSelector<CHILD, CHILDBUILDER> {

        private final Supplier<String> primaryKeySelectSource;
        private final StatementPopulator statementPopulator;

        public SubSelectChildren(Supplier<String> primaryKeySelectSource, StatementPopulator statementPopulator) {
            this.primaryKeySelectSource = primaryKeySelectSource;
            this.statementPopulator = statementPopulator;
        }

        @Override
        public List<Envelope<CHILDBUILDER>> select(SqlBuilder<CHILD> sqlBuilder,
                                                   Supplier<CHILDBUILDER> supplier,
                                                   SqlRunner<CHILD, CHILDBUILDER> sqlRunner,
                                                   String parentChildColumnName,
                                                   List<ChildrenDescriptor<CHILD, ?, CHILDBUILDER, ?>> childrenDescriptorsList) {
            String primaryKeySelect = primaryKeySelectSource.get();
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
    }

    class SelectByIds<CHILD,CHILDBUILDER> implements  ChildrenSelector<CHILD, CHILDBUILDER> {

        private final Supplier<List<Long>> parentIdsSource;

        public SelectByIds(Supplier<List<Long>> parentIdsSource) {
            this.parentIdsSource = parentIdsSource;
        }

        public List<Envelope<CHILDBUILDER>> select(
                SqlBuilder<CHILD> sqlBuilder,
                Supplier<CHILDBUILDER> supplier,
                SqlRunner<CHILD, CHILDBUILDER> sqlRunner,
                String parentChildColumnName,
                List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList) {
            Where where = Where.inLong(parentChildColumnName, parentIdsSource.get());
            String sql = sqlBuilder.select(where);
            SelectionInstruction selectionInstruction = SelectionInstruction.withParentColumnName(
                    sql, parentChildColumnName, ChildSelectStrategy.ByKeysInClause);
            return sqlRunner.doSelection(selectionInstruction, supplier, childrenDescriptorsList, where);
        }
    }

    class Factory {
        public static ChildrenSelector<?, ?> create(ChildSelectStrategy childSelectStrategy,
                                                    boolean selectAll,
                                                    Supplier<List<Long>> parentIdsSource,
                                                    Supplier<String> primaryKeySqlSource,
                                                    StatementPopulator statementPopulator){

            if( selectAll ){
                return new SelectAllChildren<>();
            } else {
                if (ChildSelectStrategy.ByKeysInClause.equals(childSelectStrategy)) {
                    return new SelectByIds<>(parentIdsSource);
                } else if (ChildSelectStrategy.SubSelectInClause.equals(childSelectStrategy)) {
                    return new SubSelectChildren<>(primaryKeySqlSource, statementPopulator);
                }
            }

            throw new HrormException("BUG. This should be unreachable. Select all=" + selectAll + " + " +
                    ", childSelectStrategy=" + childSelectStrategy);
        }

    }
}
