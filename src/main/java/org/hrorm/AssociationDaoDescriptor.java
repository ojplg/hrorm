package org.hrorm;

public interface AssociationDaoDescriptor {
    String getTableName();

    String getPrimaryKeyName();

    String getSequenceName();

    String getLeftColumnName();

    String getRightColumnName();
}
