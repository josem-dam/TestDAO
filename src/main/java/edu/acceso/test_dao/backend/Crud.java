package edu.acceso.test_dao.backend;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.acceso.test_dao.modelo.Entity;

public interface Crud<T extends Entity> {

    /**
     * Obtiene una entidad a partir de su identificador.
     * @param id Identificador de la entidad.
     * @return La entidad requerida.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos.
     */
    public Optional<T> get(Long id) throws DataAccessException;
    /**
     * Obtiene la relación completa de entidades de un tipo.
     * @return Una lista con todas las entidades.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos.
     */
    public List<T> get() throws DataAccessException;

    /**
     * Borra una entidad con un determinado identificador.
     * @param id Identificador de la entidad.
     * @return true, si se encontró la entidad y se borró.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos.
     */
    public boolean delete(Long id) throws DataAccessException;
    /**
     * Borra una entidad.
     * @param obj La entidad que se quiere borrar.
     * @return true, si la entidad existía y se borro.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos.
     */
    default boolean delete(T obj) throws DataAccessException {
        return delete(obj.getId());
    }

    /**
     * Agrega una entidad a la base de datos.
     * @param obj La entidad que se quiere agregar.
     * @throws DataAccessException Si hubo algún problema en el acceso o ya existía una entidad con ese identificador.
     */
    public void insert(T obj) throws DataAccessException;
    /**
     * Agrega una multitud de entidades de un determinado tipo a la base de datos.
     * @param objs Las entidades a agregar.
     * @throws DataAccessException Si hubo algún problema en el acceso o ya existía alguna de las entidades.
     */
    default void insert(Iterable<T> objs) throws DataAccessException {
        for(T obj: objs) insert(obj);
    }
    /**
     * Agrega un array de entidades a la base de datos.
     * @param objs Las entidades a agregar.
     * @throws DataAccessException Si hubo algún problema en el acceso o ya existía alguna de las entidades.
     */
    default void insert(T[] objs) throws DataAccessException {
       insert(Arrays.asList(objs));
    }

    /**
     * Actualiza los campos de una entidad cuyo identificador no ha cambiado.
     * @param obj La entidad con los valores actualizados.
     * @return true, si la entidad existía y se pudo actualizar.
     * @throws DataAccessException Si hubo algún problema en el acceso.
     */
    public boolean update(T obj) throws DataAccessException;
    /**
     * Actualiza el identificador de una entidad.
     * @param oldId El valor antiguo del identificador.
     * @param newId El nuevo valor de identificador.
     * @return true, si la entidad existía y se pudo actualizar.
     * @throws DataAccessException Si hubo algún problema en el acceso.
     */
    public boolean update(Long oldId, Long newId) throws DataAccessException;
    /**
     * Actualiza el identificador de una entidad.
     * @param obj La entidad con el identificador sin actualizar. 
     * @param newId El nuevo valor de identificador.
     * @return true, si la entidad existía y se pudo actualizar.
     * @throws DataAccessException Si hubo algún problema en el acceso.
     */
    default boolean update(T obj, Long newId) throws DataAccessException {
        return update(obj.getId(), newId);
    }
}
