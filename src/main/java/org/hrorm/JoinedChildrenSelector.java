package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final Map<String,JoinColumn<ENTITY,?,BUILDER,?>> joinColumnMap;
    private final SelectionInstruction selectionInstruction;
    private final Map<String, List<Envelope<?>>> recordMap = new HashMap<>();
    private final KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor;
    // MAYBE: Oh god, this is hideous
    private final Map<String, JoinedChildrenSelector> subResultsMap = new HashMap<>();

    public JoinedChildrenSelector(KeylessDaoDescriptor<ENTITY, BUILDER> keylessDaoDescriptor, SelectionInstruction selectionInstruction){
        this.selectionInstruction = selectionInstruction;
        this.keylessDaoDescriptor = keylessDaoDescriptor;
        Map<String, JoinColumn<ENTITY,?,BUILDER,?>> tmp = new HashMap<>();
        for(JoinColumn<ENTITY,?,BUILDER,?> jc : keylessDaoDescriptor.joinColumns()){
            String columnName = jc.getName();
            tmp.put(columnName, jc);
            recordMap.put(columnName, new ArrayList<>());

            KeylessDaoDescriptor joinedDaoDescriptor = jc.getJoinedDaoDescriptor();
            JoinedChildrenSelector joinedChildrenSelector = new JoinedChildrenSelector(joinedDaoDescriptor, selectionInstruction);
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

        ChildSelectStrategy childSelectStrategy = selectionInstruction.getChildSelectStrategy();

        for (String columnName : recordMap.keySet()) {
            List<Envelope<?>> envelopes = recordMap.get(columnName);

            DaoDescriptor joinedDaoDescriptor = matchingDaoDescriptor(columnName);

            ChildrenBuilderSelectCommand childrenBuilderSelectCommand;

            if ( selectionInstruction.isSelectAll() ){
                childrenBuilderSelectCommand = ChildrenBuilderSelectCommand.forSelectAll();
            } else if ( ChildSelectStrategy.ByKeysInClause.equals(childSelectStrategy )) {
                List<Long> parentIds = asParentIds(envelopes);
                childrenBuilderSelectCommand = ChildrenBuilderSelectCommand.forSelectByIds(parentIds);
            } else if ( ChildSelectStrategy.SubSelectInClause.equals(childSelectStrategy)){
                SqlBuilder sqlBuilder = new SqlBuilder(keylessDaoDescriptor);
                String primaryKeySql = sqlBuilder.selectPrimaryKeyOfJoinedColumn(statementPopulator, columnName);

                childrenBuilderSelectCommand = ChildrenBuilderSelectCommand.forSubSelect(
                        primaryKeySql, statementPopulator);
            } else {
                throw new HrormException("Unsupported strategy " + childSelectStrategy);
            }

            List<ChildrenDescriptor> childrenDescriptors = joinedDaoDescriptor.childrenDescriptors();
            for( ChildrenDescriptor childrenDescriptor : childrenDescriptors ) {
                childrenDescriptor.populateChildren(connection, envelopes, childrenBuilderSelectCommand);
            }

            // TODO: Do something with the joined results: subResultsMap

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
