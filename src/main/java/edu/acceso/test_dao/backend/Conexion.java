package edu.acceso.test_dao.backend;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import edu.acceso.sqlutils.ConnectionPool;
import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.sqlutils.tx.TransactionManager;
import edu.acceso.sqlutils.tx.TransactionManager.Transactionable;
import edu.acceso.sqlutils.tx.TransactionManager.TransactionableR;

/**
 * Gestiona las conexiones a la base de datos.
 * Utiliza un patrón Multiton para manejar las conexiones basadas en claves únicas.
 * y utiliza de forma práctica {@link ConnectionPool} y {@link TransactionManager}.
 * La clase maneja con seguridad mútiples conexiones concurrentes.
 */
public class Conexion implements AutoCloseable {

    private static final Map<String, Conexion> instances = new ConcurrentHashMap<>(); 

    private final ConnectionPool cp;
    private final TransactionManager tm;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Constructor privado.
     * @param key La clave única para esta conexión.
     * @param dbUrl La URL de la base de datos.
     * @param user El nombre de usuario para la base de datos.
     * @param password La contraseña para la base de datos.
     */
    private Conexion(String key, String dbUrl, String user, String password) {
        cp = ConnectionPool.getInstance(dbUrl, user, password);
        tm = TransactionManager.create(key, cp.getDataSource());
    }

    /**
     * Crea una nueva instancia de Conexion.
     * @param key La clave única para esta conexión.
     * @param dbUrl La URL de conexión a la base de datos. 
     * @param user El nombre de usuario para la base de datos.
     * @param password La contraseña para la base de datos.
     * @return La instancia de Conexion creada.
     * @throws IllegalStateException Si ya existe una conexión para la clave dada.
     */
    public static Conexion create(String key, String dbUrl, String user, String password) {
        return instances.compute(key, (k, value) -> {
            if(value != null) throw new IllegalStateException("Ya existe una conexión para la clave %s".formatted(k));
            return new Conexion(k, dbUrl, user, password);
        });
    }

    /**
     * Obtiene la instancia de Conexion asociada a la clave dada.
     * @param key La clave única para esta conexión.
     * @return La instancia de Conexion asociada a la clave.
     * @throws IllegalStateException Si no existe una conexión para la clave dada.
     */
    public static Conexion get(String key) {
        Conexion instance = instances.get(key);
        if (instance == null) throw new IllegalStateException("No existe una conexión para la clave %s".formatted(key));
        return instance;
    }

    /**
     * Verifica si la conexión está abierta.
     * @return true si la conexión está abierta, false si está cerrada.
     */
    public boolean isOpen() {
        return !closed.get();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            instances.remove(tm.getKey(), this);
            cp.close();
        }
    }

    /**
     * Ejecuta una transacción con resultado.
     * @param <T> El tipo de resultado de la transacción.
     * @param operations Las operaciones a ejecutar dentro de la transacción.
     * @return El resultado de la transacción.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos.
     */
    public <T> T transactionR(TransactionableR<T> operations) throws DataAccessException {
        if(!isOpen()) throw new IllegalStateException("La conexión está cerrada.");
        return tm.transaction(operations);
    }

    /**
     * Ejecuta una transacción sin resultado.
     * @param operations Las operaciones a ejecutar dentro de la transacción.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos.
     */
    public void transaction(Transactionable operations) throws DataAccessException {
        if(!isOpen()) throw new IllegalStateException("La conexión está cerrada.");
        tm.transaction(operations);
    }
}