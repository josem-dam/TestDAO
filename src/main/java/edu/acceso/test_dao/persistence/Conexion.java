package edu.acceso.test_dao.persistence;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.zaxxer.hikari.HikariConfig;

import edu.acceso.sqlutils.DbmsSelector;
import edu.acceso.sqlutils.DataSourceFactory;
import edu.acceso.sqlutils.datasource.hikari.HikariCPFactory;
import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.sqlutils.jpa.JpaConnection;
import edu.acceso.sqlutils.jpa.tx.TransactionManager;
import edu.acceso.sqlutils.tx.Transactionable;
import edu.acceso.sqlutils.tx.TransactionableR;
import edu.acceso.sqlutils.tx.event.LoggingManager;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;
import jakarta.persistence.EntityManager;

/**
 * Gestiona las conexiones a la base de datos.
 * Utiliza un patrón Multiton para manejar las conexiones basadas en claves únicas.
 * y utiliza de forma práctica {@link JdbcConnection} y {@link TransactionManager}.
 * La clase maneja con seguridad mútiples conexiones concurrentes, por lo que
 * resuelve un escenario bastante más amplio que el de este ejemplo.
 */
public class Conexion implements AutoCloseable {

    private static final Map<String, Conexion> instances = new ConcurrentHashMap<>(); 

    private final JpaConnection jc;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Constructor privado.
     * @param key La clave única para esta conexión.
     * @param dbUrl La URL de la base de datos.
     * @param user El nombre de usuario para la base de datos.
     * @param password La contraseña para la base de datos.
     */
    private Conexion(String key, Map<String, Object> props) {
        // Conector con gestor de transacciones y logging integrado
        jc = JpaConnection.create(key, props)
            .withTransactionManager(Map.of(LoggingManager.KEY, new LoggingManager()));
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

        DbmsSelector sgbd = DbmsSelector.fromUrl(dbUrl);
        JpaProvider provider = JpaProvider.HIBERNATE.withSgbd(sgbd);

        // Configuramos el DataSourceFactory para la conexión
        // Si no lo configuraramos, se usaría también HikariCPFactory,
        // pero con la configuración por defecto incluida en sqlutils-hikaricp.
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        DataSourceFactory df = new HikariCPFactory(config);

        // Construimos el mapa dinámico con las propiedades de conexión
        Map<String, Object> props = Map.of(
            "jakarta.persistence.jdbc.url", dbUrl,
            //"jakarta.persistence.jdbc.user", user,
            //"jakarta.persistence.jdbc.password", password
            provider.getDialectKey(), provider.getDialect(),
            "sqlutils.datasource.factory", df
        );

        Conexion instance = new Conexion(key, props);
        Conexion previa = instances.putIfAbsent(key, instance);
        if(previa != null) {
            instance.close();
            throw new IllegalStateException("Ya existe una conexión para la clave %s".formatted(key));
        }

        return instance;
    }

    public Conexion initialize() {
        List<Centro> centros = List.of(
            new Centro(11004866L, "IES Castillo de Luna", Titularidad.PUBLICA),
            new Centro(11700603L, "IES Pintor Juan Lara", Titularidad.PUBLICA),
            new Centro(11007533L, "IES Arroyo Hondo", Titularidad.PUBLICA)
        );

        AppService service = new AppService(jc.getKey());
        centros.forEach(service::agregarCentro);
        return this;
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
     * Verifica si la conexión está abierta.
     * @return true si la conexión está abierta, false si está cerrada.
     */
    public boolean isOpen() {
        return !closed.get() && jc.isOpen();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            instances.remove(jc.getKey(), this);
            jc.close();
        }
    }

    /**
     * Ejecuta una transacción con resultado.
     * @param <T> El tipo de resultado de la transacción.
     * @param operations Las operaciones a ejecutar dentro de la transacción.
     * @return El resultado de la transacción.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos.
     */
    public <T> T transactionR(TransactionableR<EntityManager, T> operations) throws DataAccessException {
        if(!isOpen()) throw new IllegalStateException("La conexión está cerrada.");
        return jc.getTransactionManager().transaction(operations);
    }

    /**
     * Ejecuta una transacción sin resultado.
     * @param operations Las operaciones a ejecutar dentro de la transacción.
     * @throws DataAccessException Si hubo algún problema en el acceso a los datos.
     */
    public void transaction(Transactionable<EntityManager> operations) throws DataAccessException {
        if(!isOpen()) throw new IllegalStateException("La conexión está cerrada.");
        jc.getTransactionManager().transaction(operations);
    }
}