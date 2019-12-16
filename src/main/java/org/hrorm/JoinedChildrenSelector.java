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
 * This class holds cached joined objects temporarily until their children
 * are queried. It is needed in the case of a joined entity having been
 * selected using a non-standard ChildSelectStrategy.
 * There are further layers of complication because an entity can
 * have multiple children and its own joins.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class JoinedChildrenSelector<ENTITY, BUILDER> {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    private static class JoinedRecordsHolder<ENTITY, BUILDER, JOINED, JOINEDBUILDER> {
        private final JoinColumn<ENTITY, JOINED, BUILDER, JOINEDBUILDER> joinColumn;
        private final JoinedChildrenSelector<ENTITY, BUILDER> selector;
        private final List<ReadResult<JOINEDBUILDER>> joinedRecords = new ArrayList<>();

        JoinedRecordsHolder(JoinColumn<ENTITY, JOINED, BUILDER, JOINEDBUILDER> joinColumn, JoinedChildrenSelector<ENTITY, BUILDER> selector){
            this.joinColumn = joinColumn;
            this.selector = selector;
        }

        /**
         * Adds a record to the cache, and records of any successive joins.
         */
        void addRecord(ReadResult<JOINEDBUILDER> readResult){
            this.joinedRecords.add(readResult);
            for( Map.Entry<String, PopulateResult> entry : readResult.getSubResults().entrySet()){
                PopulateResult populateResult = entry.getValue();
                if( populateResult.isJoinedItemResult()) {
                    selector.addJoinedInstanceAndItsJoins(entry.getKey(), populateResult.getReadResult());
                }
            }
        }

        void populateChildren(Connection connection, StatementPopulator statementPopulator){
            this.selector.populateChildren(connection, statementPopulator);
        }

        void populateChildrenDescriptors(Connection connection, ChildrenSelector childrenSelector){
            logger.warning("doing the population");
            List<ChildrenDescriptor> childrenDescriptors = joinColumn.getJoinedDaoDescriptor().childrenDescriptors();
            for( ChildrenDescriptor childrenDescriptor : childrenDescriptors ) {
                List<Envelope<JOINEDBUILDER>> builders =
                        joinedRecords.stream().map(ReadResult::getJoinedBuilder).collect(Collectors.toList());
                childrenDescriptor.populateChildren(connection, builders, childrenSelector);
            }
            joinedRecords.forEach(ReadResult::complete);
        }

        List<Long> getParentIds(){
            return joinedRecords.stream().map(ReadResult::getId).collect(Collectors.toList());
        }

        List<ChildrenDescriptor> getChildrenDescriptors(){
            return joinColumn.getJoinedDaoDescriptor().childrenDescriptors();
        }
    }

    private final ChildSelectStrategy childSelectStrategy;
    private final boolean selectAll;
    private final SqlBuilder<ENTITY> sqlBuilder;
    private final Map<String, JoinedRecordsHolder<ENTITY, BUILDER, ?,?>> joinedRecordsMap = new HashMap<>();

    public JoinedChildrenSelector(KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor, ChildSelectStrategy childSelectStrategy, boolean selectAll){
        this.childSelectStrategy = childSelectStrategy;
        this.selectAll = selectAll;
        this.sqlBuilder = new SqlBuilder<>(keylessDaoDescriptor);
        for(JoinColumn<ENTITY,?,BUILDER,?> jc : keylessDaoDescriptor.joinColumns()){
            String columnName = jc.getName();
            KeylessDaoDescriptor joinedDaoDescriptor = jc.getJoinedDaoDescriptor();
            JoinedChildrenSelector joinedChildrenSelector = new JoinedChildrenSelector(joinedDaoDescriptor, childSelectStrategy, selectAll);
            JoinedRecordsHolder<ENTITY, BUILDER, ?, ?> holder = new JoinedRecordsHolder<>(
                    jc, joinedChildrenSelector
            );
            this.joinedRecordsMap.put(columnName, holder);
        }
    }

    public <JOINED, JOINEDBUILDER> void addJoinedInstanceAndItsJoins(String columnName, ReadResult<JOINEDBUILDER> readResult){
        if( ! joinedRecordsMap.containsKey(columnName)){
            throw new HrormException("Problem. This column name is unrecognized: "  + columnName);
        }

        JoinedRecordsHolder<ENTITY, BUILDER, JOINED, JOINEDBUILDER> joinedRecordsHolder =
                (JoinedRecordsHolder<ENTITY, BUILDER, JOINED, JOINEDBUILDER>) joinedRecordsMap.get(columnName);
        joinedRecordsHolder.addRecord(readResult);
    }

    public void populateChildren(Connection connection, StatementPopulator statementPopulator){
        for ( Map.Entry<String, JoinedRecordsHolder<ENTITY, BUILDER, ?, ?>> holderEntry : joinedRecordsMap.entrySet()){
            JoinedRecordsHolder holder = holderEntry.getValue();
            holder.populateChildren(connection, statementPopulator);
            Supplier<List<Long>> parentIdsSupplier = holder::getParentIds;
            Supplier<String> primaryKeySqlSupplier = () -> sqlBuilder.selectPrimaryKeyOfJoinedColumn(statementPopulator, holderEntry.getKey());

            ChildrenSelector<?,?> childrenSelector = ChildrenSelector.Factory.create(
                    childSelectStrategy,
                    selectAll,
                    parentIdsSupplier,
                    primaryKeySqlSupplier,
                    statementPopulator);

            holder.populateChildrenDescriptors(connection, childrenSelector);
        }
    }
}
