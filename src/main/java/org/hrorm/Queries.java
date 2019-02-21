package org.hrorm;

/**
 * Represents the generated SQL for doing CRUD operations on some entity.
 *
 * <p>
 *     All the SQL is for populating <code>PreparedStatement</code> objects,
 *     i.e. the necessary variables are represented by question-marks, for
 *     late binding in the database.
 * </p>
 */
public interface Queries {

    String insert();
    String update();
    String delete();
    String select();

}



