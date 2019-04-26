package org.hrorm;

import java.sql.Connection;

public interface KeyProducer<T> {
    T produceKey(Connection connection);
}
