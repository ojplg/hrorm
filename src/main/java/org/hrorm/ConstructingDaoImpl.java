package org.hrorm;

import java.util.List;
import java.util.function.Function;

public class ConstructingDaoImpl<T, CONSTRUCTOR> implements Dao<T> {

    private final Dao<CONSTRUCTOR> internalDao;
    private final Function<CONSTRUCTOR, T> construct;

    public ConstructingDaoImpl(Dao<CONSTRUCTOR> dao, Function<CONSTRUCTOR, T> construct){
        this.internalDao = dao;
        this.construct = construct;
    }

    @Override
    public long insert(T item) {

    }

    @Override
    public void update(T item) {

    }

    @Override
    public void delete(T item) {

    }

    @Override
    public T select(long id) {
        CONSTRUCTOR constructor = internalDao.select(id);
        return construct.apply(constructor);
    }

    @Override
    public List<T> selectMany(List<Long> ids) {
        return null;
    }

    @Override
    public List<T> selectAll() {
        return null;
    }

    @Override
    public T selectByColumns(T item, String... columnNames) {
        return null;
    }

    @Override
    public List<T> selectManyByColumns(T item, String... columnNames) {
        return null;
    }

    @Override
    public long atomicInsert(T item) {
        return 0;
    }

    @Override
    public void atomicUpdate(T item) {

    }

    @Override
    public void atomicDelete(T item) {

    }
}
