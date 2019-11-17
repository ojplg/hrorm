package org.hrorm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }
}
