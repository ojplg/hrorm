package org.hrorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public class FoldingSelect<ENTITY, BUILDER> implements Iterable<BUILDER> {
    private static final Logger LOGGER = Logger.getLogger(FoldingSelect.class.getName());

    private final Connection connection;
    private final ResultSet resultSet;

    private final Function<ResultSet, BUILDER> populationFunction;
    private final Map<String, ? extends Column<ENTITY,?>> columnNameMap;
    private final List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors;

    public FoldingSelect(Connection connection, ResultSet resultSet, Function<ResultSet, BUILDER> populationFunction, Map<String, ? extends Column<ENTITY, ?>> columnNameMap, List<? extends ChildrenDescriptor<ENTITY, ?, BUILDER, ?>> childrenDescriptors) {
        this.connection = connection;
        this.resultSet = resultSet;
        this.populationFunction = populationFunction;
        this.columnNameMap = columnNameMap;
        this.childrenDescriptors = childrenDescriptors;
    }

    @Override
    public Iterator<BUILDER> iterator() {
        return new FoldingSelectIterator<>(connection, resultSet, populationFunction, childrenDescriptors);
    }

    private static final class FoldingSelectIterator<BUILDER, ENTITY> implements Iterator<BUILDER>, AutoCloseable {
        private final Connection connection;
        private final ResultSet resultSet;
        private final Function<ResultSet, BUILDER> populationFunction;
        private final List<? extends ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors;
        //private BUILDER current;

        private FoldingSelectIterator(Connection connection, ResultSet resultSet, Function<ResultSet, BUILDER> populationFunction, List<? extends ChildrenDescriptor<ENTITY, ?, BUILDER, ?>> childrenDescriptors) {
            this.connection = connection;
            this.resultSet = resultSet;
            this.populationFunction = populationFunction;
            this.childrenDescriptors = childrenDescriptors;

        }

        public BUILDER populate() {
            BUILDER entity = populationFunction.apply(resultSet);
            for (ChildrenDescriptor<ENTITY, ?, BUILDER, ?> descriptor : childrenDescriptors) {
                descriptor.populateChildren(connection, entity);
            }
            return entity;
        }

        @Override
        public boolean hasNext() {
            try {
                return resultSet != null && !resultSet.isClosed() && resultSet.next();
            } catch (SQLException e) {
                throw new HrormException(e);
            }
        }

        @Override
        public BUILDER next() {
            return populate();
        }

        @Override
        public void close() throws Exception {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }
}
