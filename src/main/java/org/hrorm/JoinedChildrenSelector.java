package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class holds cached joined objects temporarily while their children
 * are queried.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class JoinedChildrenSelector<ENTITY, BUILDER> {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    private static class ChildRecordsHolder<ENTITY, BUILDER> {
        private final JoinColumn<ENTITY, ?, BUILDER, ?> joinColumn;
        private final JoinedChildrenSelector<ENTITY, BUILDER> selector;
        private final List<Envelope<?>> joinedRecords = new ArrayList<>();

        ChildRecordsHolder(JoinColumn<ENTITY, ?, BUILDER, ?> joinColumn, JoinedChildrenSelector<ENTITY, BUILDER> selector){
            this.joinColumn = joinColumn;
            this.selector = selector;
        }

        void addChildRecord(Envelope<?> joinedObject, Map<String,PopulateResult> subResults){
            this.joinedRecords.add(joinedObject);
            for( String name : subResults.keySet()){
                PopulateResult populateResult = subResults.get(name);
                Envelope envelope = populateResult.getJoinedItem();
                selector.addChildEntityInfo(name, envelope, populateResult.getSubResults());
            }
        }

        void populateChildren(Connection connection, StatementPopulator statementPopulator){
            this.selector.populateChildren(connection, statementPopulator);
        }

        void populateChildrenDescriptors(Connection connection, ChildrenSelector childrenSelector){
            List<ChildrenDescriptor> childrenDescriptors = joinColumn.getJoinedDaoDescriptor().childrenDescriptors();
            for( ChildrenDescriptor childrenDescriptor : childrenDescriptors ) {
                childrenDescriptor.populateChildren(connection, joinedRecords, childrenSelector);
            }
        }

        List<Long> getParentIds(){
            return joinedRecords.stream().map(Envelope::getId).collect(Collectors.toList());
        }

        List<ChildrenDescriptor> getChildrenDescriptors(){
            return joinColumn.getJoinedDaoDescriptor().childrenDescriptors();
        }
    }

    private final ChildSelectStrategy childSelectStrategy;
    private final boolean selectAll;
    private final KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor;
    private final Map<String, ChildRecordsHolder<ENTITY, BUILDER>> joinedRecordsMap = new HashMap<>();

    public JoinedChildrenSelector(KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor, ChildSelectStrategy childSelectStrategy, boolean selectAll){
        this.childSelectStrategy = childSelectStrategy;
        this.selectAll = selectAll;
        this.keylessDaoDescriptor = keylessDaoDescriptor;
        for(JoinColumn<ENTITY,?,BUILDER,?> jc : keylessDaoDescriptor.joinColumns()){
            String columnName = jc.getName();
            KeylessDaoDescriptor joinedDaoDescriptor = jc.getJoinedDaoDescriptor();
            JoinedChildrenSelector joinedChildrenSelector = new JoinedChildrenSelector(joinedDaoDescriptor, childSelectStrategy, selectAll);
            ChildRecordsHolder<ENTITY, BUILDER> holder = new ChildRecordsHolder<>(
                    jc, joinedChildrenSelector
            );
            this.joinedRecordsMap.put(columnName, holder);
        }
    }

    public void addChildEntityInfo(String columnName, Envelope<?> joinedObject, Map<String,PopulateResult> subResults){
        if( ! joinedRecordsMap.containsKey(columnName)){
            throw new HrormException("Problem. This column name is unrecognized: "  + columnName);
        }

        // FIXME: case issue?
        ChildRecordsHolder<ENTITY, BUILDER> childRecordsHolder = joinedRecordsMap.get(columnName);
        childRecordsHolder.addChildRecord(joinedObject, subResults);
    }

    public void populateChildren(Connection connection, StatementPopulator statementPopulator){
        for ( Map.Entry<String, ChildRecordsHolder<ENTITY, BUILDER>> holderEntry : joinedRecordsMap.entrySet()){
            ChildRecordsHolder holder = holderEntry.getValue();
            holder.populateChildren(connection, statementPopulator);
            Supplier<List<Long>> parentIdsSupplier = holder::getParentIds;
            Supplier<String> primaryKeySqlSupplier = () -> createPrimaryKeySql(statementPopulator, holderEntry.getKey());

            ChildrenSelector<?,?> childrenSelector = ChildrenSelector.Factory.create(
                    childSelectStrategy,
                    selectAll,
                    parentIdsSupplier,
                    primaryKeySqlSupplier,
                    statementPopulator);

            holder.populateChildrenDescriptors(connection, childrenSelector);
        }
    }

    private String createPrimaryKeySql(StatementPopulator statementPopulator, String columnName){
        SqlBuilder sqlBuilder = new SqlBuilder(keylessDaoDescriptor);
        String primaryKeySql = sqlBuilder.selectPrimaryKeyOfJoinedColumn(statementPopulator, columnName);
        return primaryKeySql;
    }
}
