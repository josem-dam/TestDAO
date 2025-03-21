package edu.acceso.test_dao.backend;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.acceso.test_dao.modelo.Entity;

public interface Crud<T extends Entity> {

    public Optional<T> get(Long id) throws DataAccessException;
    public List<T> get() throws DataAccessException;

    public boolean delete(Long id) throws DataAccessException;
    default boolean delete(T obj) throws DataAccessException {
        return delete(obj.getId());
    }

    public void insert(T obj) throws DataAccessException;
    default void insert(Iterable<T> objs) throws DataAccessException {
        for(T obj: objs) insert(obj);
    }
    default void insert(T[] objs) throws DataAccessException {
       insert(Arrays.asList(objs));
    }

    public boolean update(T obj) throws DataAccessException;
    public boolean update(Long oldId, Long newId) throws DataAccessException;
    default boolean update(T obj, Long newId) throws DataAccessException {
        return update(obj.getId(), newId);
    }
}
