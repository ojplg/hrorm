package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JoinedChildrenInfo {

    private final Map<String, EntityRecord> records = new HashMap<>();

    public void addChildEntityInfo(Envelope<Object> joinedObject, JoinColumn joinColumn){
        String columnName = joinColumn.getName();
        if ( ! records.containsKey(columnName) ){
            EntityRecord record = new EntityRecord(joinColumn);
            records.put(record.getColumnName(), record);
        }
        EntityRecord record = records.get(columnName);
        record.addRecord(joinedObject);
    }

    public void populateChildren(Connection connection, List builders){

        for (EntityRecord entityRecord: records.values()) {
            List<Long> parentIds = entityRecord.entityIds();
            ChildrenBuilderSelectCommand childrenBuilderSelectCommand =
                    ChildrenBuilderSelectCommand.forSelectByIds(parentIds);

            DaoDescriptor daoDescriptor = entityRecord.getDaoDescriptor();
            List<ChildrenDescriptor> childrenDescriptors = daoDescriptor.childrenDescriptors();
            for( ChildrenDescriptor childrenDescriptor : childrenDescriptors ) {
                childrenDescriptor.populateChildren(connection, builders, childrenBuilderSelectCommand);
            }
        }
    }



    private static class EntityRecord {

        private final List<Envelope<Object>> records = new ArrayList<>();

        private final JoinColumn joinColumn;

        public EntityRecord(JoinColumn joinColumn){
            this.joinColumn = joinColumn;
        }

        public void addRecord(Envelope<Object> envelope){
            records.add(envelope);
        }

        public String getColumnName(){
            return joinColumn.getName();
        }

        public List<Long> entityIds(){
            return records.stream().map(Envelope::getId).collect(Collectors.toList());
        }

        public DaoDescriptor getDaoDescriptor(){
            return joinColumn.getJoinedDaoDescriptor();
        }
    }
}
