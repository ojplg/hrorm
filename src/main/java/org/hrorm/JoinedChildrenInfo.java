package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
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
public class JoinedChildrenInfo {

    /*
        TODO:

        * Subselect clause should work
        * Write javadocs
        * Write documentation
        * Figure out compatibility problems with ChildSelectStrategy differences between Dao Builders
        * Rename this class
        * Clean up mutliplexing de-multiplexing through DaoImpl and SqlRunner

     */


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

    public void populateChildren(Connection connection){

        for (EntityRecord entityRecord: records.values()) {
            List<Long> parentIds = entityRecord.entityIds();
            ChildrenBuilderSelectCommand childrenBuilderSelectCommand =
                    ChildrenBuilderSelectCommand.forSelectByIds(parentIds);

            DaoDescriptor daoDescriptor = entityRecord.getDaoDescriptor();
            List<ChildrenDescriptor> childrenDescriptors = daoDescriptor.childrenDescriptors();
            for( ChildrenDescriptor childrenDescriptor : childrenDescriptors ) {
                childrenDescriptor.populateChildren(connection, entityRecord.records, childrenBuilderSelectCommand);
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

        @Override
        public String toString() {
            return "EntityRecord{" +
                    "records=" + records +
                    ", joinColumn=" + joinColumn +
                    '}';
        }
    }

    @Override
    public String toString(){
        return records.toString();
    }
}
