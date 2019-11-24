package org.hrorm;

import java.sql.Connection;
import java.util.function.Consumer;

/**
 * Indication of the result, and work remaining, after an item has been populated.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class PopulateResult {

    public static final String JoinedItemResult = "Joined Item Result";

    public static final PopulateResult PrimaryKey = new PopulateResult("Primary Key");
    public static final PopulateResult NoPrimaryKey = new PopulateResult("No Primary Key");
    public static final PopulateResult Ignore = new PopulateResult("Ignore");
    public static final PopulateResult ParentColumn = new PopulateResult("Parent Column");

    private final String code;
    private final Consumer<Connection> connectionUser;
    private final Envelope<?> joinedItem;

    private PopulateResult(String code){
        this.code = code;
        this.connectionUser = con -> {};
        this.joinedItem = null;
    }

    private PopulateResult(Consumer<Connection> connectionUser){
        this.connectionUser = connectionUser;
        this.code = "Join Column";
        this.joinedItem = null;
    }

    private PopulateResult(Envelope<?> object){
        this.code = JoinedItemResult;
        this.connectionUser = con -> {};
        this.joinedItem = object;
    }

    public static PopulateResult fromJoinColumn(Consumer<Connection> connectionUser){
        return new PopulateResult(connectionUser);
    }

    public static PopulateResult fromJoinColumn(Envelope<?> item){
        return new PopulateResult(item);
    }

    public void populateChildren(Connection connection){
        this.connectionUser.accept(connection);
    }

    public boolean isJoinedItemResult(){
        return code.equals(JoinedItemResult);
    }

    public Envelope<?> getJoinedItem(){
        return joinedItem;
    }

    @Override
    public String toString() {
        return "PopulateResult{" +
                "code='" + code + '\'' +
                '}';
    }
}
