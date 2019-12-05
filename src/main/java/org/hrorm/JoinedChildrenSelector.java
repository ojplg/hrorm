package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
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

    // MAYBE: All these maps .... ugly
    private final Map<String,JoinColumn<ENTITY,?,BUILDER,?>> joinColumnMap;
    private final ChildSelectStrategy childSelectStrategy;
    private final boolean selectAll;
    private final Map<String, List<Envelope<?>>> recordMap = new HashMap<>();
    private final KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor;
    private final Map<String, JoinedChildrenSelector> subResultsMap = new HashMap<>();

    public JoinedChildrenSelector(KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor, ChildSelectStrategy childSelectStrategy, boolean selectAll){
        this.childSelectStrategy = childSelectStrategy;
        this.selectAll = selectAll;
        this.keylessDaoDescriptor = keylessDaoDescriptor;
        Map<String, JoinColumn<ENTITY,?,BUILDER,?>> tmp = new HashMap<>();
        for(JoinColumn<ENTITY,?,BUILDER,?> jc : keylessDaoDescriptor.joinColumns()){
            String columnName = jc.getName();
            tmp.put(columnName, jc);
            recordMap.put(columnName, new ArrayList<>());

            KeylessDaoDescriptor joinedDaoDescriptor = jc.getJoinedDaoDescriptor();
            JoinedChildrenSelector joinedChildrenSelector = new JoinedChildrenSelector(joinedDaoDescriptor, childSelectStrategy, selectAll);
            subResultsMap.put(columnName, joinedChildrenSelector);
        }
        this.joinColumnMap = Collections.unmodifiableMap(tmp);
    }

    public void addChildEntityInfo(String columnName, Envelope<?> joinedObject, Map<String,PopulateResult> subResults){
        if( ! joinColumnMap.containsKey(columnName)){
            throw new HrormException("Problem. This column name is unrecognized: "  + columnName);
        }

        List<Envelope<?>> records = recordMap.get(columnName);
        records.add(joinedObject);

        JoinedChildrenSelector subSelector = subResultsMap.get(columnName);

        for( String name : subResults.keySet()){
            PopulateResult populateResult = subResults.get(name);
            Envelope envelope = populateResult.getJoinedItem();
            subSelector.addChildEntityInfo(name, envelope, populateResult.getSubResults());
        }
    }

    public void populateChildren(Connection connection, StatementPopulator statementPopulator){

        for (String columnName : recordMap.keySet()) {

            JoinedChildrenSelector subSelector = subResultsMap.get(columnName);
            subSelector.populateChildren(connection, statementPopulator);

            List<Envelope<?>> envelopes = recordMap.get(columnName);

            DaoDescriptor joinedDaoDescriptor = matchingDaoDescriptor(columnName);

            Supplier<List<Long>> parentIdsSupplier = () ->  asParentIds(envelopes);
            Supplier<String> primaryKeySqlSupplier = () -> {
                SqlBuilder sqlBuilder = new SqlBuilder(keylessDaoDescriptor);
                String primaryKeySql = sqlBuilder.selectPrimaryKeyOfJoinedColumn(statementPopulator, columnName);
                return primaryKeySql;
            };

            ChildrenSelector<?,?> childrenSelector = ChildrenSelector.Factory.create(
                    childSelectStrategy,
                    selectAll,
                    parentIdsSupplier,
                    primaryKeySqlSupplier,
                    statementPopulator);

            List<ChildrenDescriptor> childrenDescriptors = joinedDaoDescriptor.childrenDescriptors();
            for( ChildrenDescriptor childrenDescriptor : childrenDescriptors ) {
                childrenDescriptor.populateChildren(connection, envelopes, childrenSelector);
            }
        }
    }

    private List<Long> asParentIds(List<Envelope<?>> envelopes){
        return envelopes.stream().map(Envelope::getId).collect(Collectors.toList());
    }

    private DaoDescriptor<?,?> matchingDaoDescriptor(String columnName){
        JoinColumn<ENTITY, ?, BUILDER, ?> column = joinColumnMap.get(columnName);
        return column.getJoinedDaoDescriptor();
    }

    @Override
    public String toString(){
        return recordMap.toString();
    }
}
