package org.hrorm;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final Map<String,PopulateResult> subResults;

    private PopulateResult(String code){
        this.code = code;
        this.connectionUser = con -> {};
        this.joinedItem = null;
        this.subResults = Collections.emptyMap();
    }

    private PopulateResult(Consumer<Connection> connectionUser){
        this.connectionUser = connectionUser;
        this.code = "Join Column";
        this.joinedItem = null;
        this.subResults = Collections.emptyMap();
    }

    private PopulateResult(Envelope<?> object, Map<String,PopulateResult> subResults){
        this.code = JoinedItemResult;
        this.connectionUser = con -> {};
        this.joinedItem = object;
        this.subResults = subResults;
    }

    public static PopulateResult fromJoinColumn(Consumer<Connection> connectionUser){
        return new PopulateResult(connectionUser);
    }

    public static PopulateResult fromJoinColumn(Envelope<?> item, Map<String, PopulateResult> subResults){
        return new PopulateResult(item, subResults);
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

    public Map<String,PopulateResult> getSubResults() { return subResults; }

    @Override
    public String toString() {
        return "PopulateResult{" +
                "code='" + code + '\'' +
                '}';
    }
}
