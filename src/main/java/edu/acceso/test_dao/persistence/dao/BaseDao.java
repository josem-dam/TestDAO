package edu.acceso.test_dao.persistence.dao;

import java.sql.Connection;

import edu.acceso.sqlutils.tx.event.LoggingManager;
import edu.acceso.test_dao.modelo.Entity;
import edu.acceso.test_dao.persistence.Conexion;

public abstract class BaseDao<T extends Entity> implements Crud<T> {

    private final Conexion cx;

    protected BaseDao(String key) {
        cx = Conexion.get(key);
    }

    /**
     * Obtiene el {@link LoggingManager} asociado a la conexión actual.
     * @return El gestor de logging solicitado.
     */
    public LoggingManager getLoggingManager() {
        return cx.getLoggingManager();
    }

    /**
     * Obtiene la conexión asociada a la transacción actual.
     * @return La conexión solicitada.
     */
    public Connection getConnection() {
        return cx.getConnection();
    }
}
