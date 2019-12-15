package org.hrorm;

import java.sql.Connection;
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

    PopulateResult PrimaryKey = new Regular(Type.PrimaryKey);
    PopulateResult NullPrimaryKey = new Regular(Type.NullPrimaryKey);
    PopulateResult Ignore = new Regular(Type.Ignore);
    PopulateResult ParentColumn = new Regular(Type.ParentColumn);

    Type getType();

    default void populateChildren(Connection connection){}

    default boolean isJoinedItemResult(){
        return getType().equals(Type.JoinColumnDeferred);
    }

    default <BUILDER> ReadResult<BUILDER> getReadResult(){
        return ReadResult.EMPTY;
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

    class ImmediateJoin implements PopulateResult {

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

        private final ReadResult<BUILDER> readResult;

        public JoinedResult(Envelope<BUILDER> builder, Map<String, PopulateResult> subResults){
            this.readResult = new ReadResult<>(builder, subResults);
        }

//        public JoinedResult(ReadResult<BUILDER> readResult){
//            this.readResult = readResult;
//        }

        public Type getType() { return Type.JoinColumnDeferred; }

        public ReadResult<BUILDER> getReadResult(){
            return readResult;
        }
    }
}
