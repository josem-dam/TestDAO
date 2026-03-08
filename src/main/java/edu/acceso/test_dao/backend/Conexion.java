package edu.acceso.test_dao.backend;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.acceso.sqlutils.ConnectionPool;
import edu.acceso.sqlutils.SqlUtils;
import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.sqlutils.tx.LoggingManager;
import edu.acceso.sqlutils.tx.TransactionManager;
import edu.acceso.sqlutils.tx.TransactionManager.Transactionable;
import edu.acceso.sqlutils.tx.TransactionManager.TransactionableR;

/**
 * Gestiona las conexiones a la base de datos.
 * Utiliza un patrón Multiton para manejar las conexiones basadas en claves únicas.
 * y utiliza de forma práctica {@link ConnectionPool} y {@link TransactionManager}.
 * La clase maneja con seguridad mútiples conexiones concurrentes, por lo que
 * resuelve un escenario bastante más amplio que el de este ejemplo.
 */
public class Conexion implements AutoCloseable {

    private static final Map<String, Conexion> instances = new ConcurrentHashMap<>(); 

    private final ConnectionPool cp;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Constructor privado.
     * @param key La clave única para esta conexión.
     * @param dbUrl La URL de la base de datos.
     * @param user El nombre de usuario para la base de datos.
     * @param password La contraseña para la base de datos.
     */
    private Conexion(String key, String dbUrl, String user, String password) {
        cp = ConnectionPool.create(key, dbUrl, user, password);
        // Iniciamos el gestor de transacciones con un gestor de logs.
        cp.initTransactionManager(Map.of(
            LoggingManager.KEY, new LoggingManager()
        ));
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
        Objects.requireNonNull(key, "La clave no puede ser nula.");

        if(instances.containsKey(key)) throw new IllegalStateException("Ya existe una conexión para la clave %s".formatted(key));

        Conexion instance = new Conexion(key, dbUrl, user, password);
        Conexion previa = instances.putIfAbsent(key, instance);
        if(previa != null) {
            instance.close();
            throw new IllegalStateException("Ya existe una conexión para la clave %s".formatted(key));
        }

        return instance;
    }

    /**
     * Obtiene la instancia de Conexion asociada a la clave dada.
     * @param key La clave única para esta conexión.
     * @return La instancia de Conexion asociada a la clave.
     * @throws IllegalStateException Si no existe una conexión para la clave dada.
     */
    public static Conexion get(String key) {
        Objects.requireNonNull(key, "La clave no puede ser nula.");

        Conexion instance = instances.get(key);
        if (instance == null) throw new IllegalStateException("No existe una conexión para la clave %s".formatted(key));

        if(instance.isOpen()) return instance;
        else {
            instances.remove(key, instance);
            throw new IllegalStateException("La conexión solicitada no existe.");
        }
    }

    /**
     * Inicializa la base de datos con el esquema dado. Si la base de datos ya está inicializada, no hace nada.
     * @param esquema Un InputStream con el esquema SQL para inicializar la base de datos.
     * @return La propia instancia de Conexion, para permitir encadenar llamadas.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos durante la inicialización.
     */
    public Conexion initialize(InputStream esquema) throws DataAccessException {
        transaction(ctxt -> {
            Connection conn = ctxt.connection();

            // Si la base de datos ya está inicializada, no hacemos nada.
            if(SqlUtils.isDatabaseInitialized(conn)) return;

            try {
                SqlUtils.executeSQL(conn, esquema);
            } catch(SQLException e) {
                throw new DataAccessException("Error al crear el esquema en la base de datos", e);
            } catch(IOException e) {
                throw new RuntimeException("Error al intentar leer el esquema", e);
            }
        });         
        return this;
    }

    /**
     * Verifica si la conexión está abierta.
     * @return true si la conexión está abierta, false si está cerrada.
     */
    public boolean isOpen() {
        return !closed.get() && cp.isOpen();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            instances.remove(cp.getKey(), this);
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
        return cp.getTransactionManager().transaction(operations);
    }

    /**
     * Ejecuta una transacción sin resultado.
     * @param operations Las operaciones a ejecutar dentro de la transacción.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos.
     */
    public void transaction(Transactionable operations) throws DataAccessException {
        if(!isOpen()) throw new IllegalStateException("La conexión está cerrada.");
        cp.getTransactionManager().transaction(operations);
    }

    /**
     * Obtiene la conexión asociada a la transacción actual.
     * @return La conexión solicitada.
     * @throws IllegalStateException Si no hay ninguna transacción activa.
     */
    public Connection getConnection() {
        if(!isOpen()) throw new IllegalStateException("La conexión está cerrada.");
        return cp.getTransactionManager().getConnection();
    }

    public LoggingManager getLoggingManager() {
        if(!isOpen()) throw new IllegalStateException("La conexión está cerrada.");
        return cp.getTransactionManager().getListener(LoggingManager.KEY, LoggingManager.class);
    }
}