package org.hrorm;

import java.util.Arrays;
import java.util.List;

/**
 * Describes how to order entities during select.
 */
public class Order {

    private enum Direction {
        ASC, DESC
    }

    private final Direction direction;
    private final List<String> columnNames;

    /**
     * To order things in an increasing direction by the passed columns.
     *
     * @param columnNames The column names to use in the ordering
     * @return The new object
     */
    public static Order ascending(String ... columnNames){
        return new Order(Direction.ASC, columnNames);
    }

    /**
     * To order things in a decreasing direction by the passed columns.
     *
     * @param columnNames The column names to use in the ordering
     * @return The new object
     */
    public static Order descending(String ... columnNames){
        return new Order(Direction.DESC, columnNames);
    }

    private Order(Direction direction, String ... columnNames){
        if ( columnNames.length == 0 ){
            throw new HrormException("Must provide at least one column to sort by");
        }
        this.direction = direction;
        this.columnNames = Arrays.asList(columnNames);
    }

    /**
     * Generates a SQL snippet for appending to a query.
     *
     * @return the SQL fragment
     */
    public String render(){
        StringBuilder buf = new StringBuilder();
        buf.append(" ORDER BY ");
        for(int idx=0; idx<columnNames.size() ; idx++ ){
            String name = columnNames.get(idx);
            buf.append(name);
            if( idx < columnNames.size() - 1){
                buf.append(", ");
            }
        }
        buf.append(" ");
        buf.append(direction);
        return buf.toString();
    }

}
