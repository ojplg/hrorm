package org.hrorm;

import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Indication of the result, and work remaining, after an item has been populated.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public interface PopulateResult {

    enum Type {
        PrimaryKey,NullPrimaryKey,Ignore,ParentColumn,JoinColumn,JoinColumnDeferred
    }

    class Regular implements PopulateResult {
        private final Type type;

        public Regular(Type type){
            this.type = type;
        }

        public Type getType(){
            return type;
        }
    }

    PopulateResult PrimaryKey = new Regular(Type.PrimaryKey);
    PopulateResult NullPrimaryKey = new Regular(Type.NullPrimaryKey);
    PopulateResult Ignore = new Regular(Type.Ignore);
    PopulateResult ParentColumn = new Regular(Type.ParentColumn);

    public class ImmediateJoin implements PopulateResult {

        private final Consumer<Connection> connectionUser;

        public ImmediateJoin(Consumer<Connection> connectionUser){
            this.connectionUser = connectionUser;
        }

        public Type getType() { return Type.JoinColumn; }

        public void populateChildren(Connection connection){
            connectionUser.accept(connection);
        }

    }

    class JoinedResult<BUILDER> implements PopulateResult {

        private final Envelope<BUILDER> joinedBuilder;
        private final Map<String,PopulateResult> subResults;

        public JoinedResult(Envelope<BUILDER> joinedBuilder, Map<String,PopulateResult> subResults){
            this.joinedBuilder = joinedBuilder;
            this.subResults = subResults;
        }

        public Type getType() { return Type.JoinColumnDeferred; }

        public Envelope<BUILDER> getJoinedBuilder(){
            return joinedBuilder;
        }

        public Map<String,PopulateResult> getSubResults(){
            return subResults;
        }
    }

    Type getType();

    default void populateChildren(Connection connection){};

    default boolean isJoinedItemResult(){
        return /* getType().equals(Type.JoinColumn)
                || */ getType().equals(Type.JoinColumnDeferred);
    }

    default <BUILDER> Envelope<BUILDER> getJoinedBuilder(){
        return Envelope.EMPTY;
    }

    default Map<String,PopulateResult> getSubResults() {
        return Collections.emptyMap();
    }
}
