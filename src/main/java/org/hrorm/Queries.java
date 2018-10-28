package org.hrorm;

public interface Queries {

    String insert();
    String update();
    String delete();
    String select();
    String selectByColumns(String ... columnNames);

}



