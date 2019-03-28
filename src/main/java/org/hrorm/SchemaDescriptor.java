package org.hrorm;

import java.util.List;

public interface SchemaDescriptor<ENTITY, BUILDER> extends DaoDescriptor<ENTITY, BUILDER> {

    List<List<String>> uniquenessConstraints();

}
