package edu.acceso.test_dao.persistence.dao;

import java.sql.Connection;

import edu.acceso.sqlutils.tx.event.LoggingManager;
import edu.acceso.test_dao.modelo.Entity;
import edu.acceso.test_dao.persistence.Conexion;

/**
 * Clase base para los DAOs.
 * <p>
 * Todas las clases específicas derivadas de ella implementarán la interfaz {@link Crud} para su entidad correspondiente.
 * Básicamente, esta clase proporciona acceso a la conexión y a todos los observadores de la transacción.
 * <p>
 * Las clases derivadas se han procurado escribir imitando la forma sencilla en que se escriben
 * operaciones con JDBC: se abre una conexión, se prepara una sentencia, se ejecuta y se cierra la conexión.
 * Esto exige que ya exista una transacción activa. Una alternativa habría sido que cada método
 * se asegurara de abrir una transacción, lo cual no está reñido con que ya hubiera una activa, puesto
 * que las transacciones se pueden anidar:
 * <pre>
 * String sqlString = "SELECT * FROM Centro WHERE id = ?";
 * 
 * return cx.transactionR(ctxt -> {
 *   // Obtenermos conexión y observadores directamente del contexto de la transacción.
 *   Connection conn = ctxt.handle();
 *   LoggingManager logManager = ctxt.listener(LoggingManager.KEY, LoggingManager.class);
 * 
 *   try (PreparedStatement pstmt = conn.prepareStatement(sqlString)) { 
 *     // Se ejecuta la sentencia para obtener el centro.
 *   }
 * })
 * </pre>
 * <p>Ahora bien, si se opta por esta forma, cx debe ser protected, no private. En contraprestación,
 * ya no es necesario definir métodos para obtener la conexión o los observadores, puesto que se puede
 * obtener todo directamente desde el contexto de la transacción.
 * 
 */
public abstract class BaseDao<T extends Entity> implements Crud<T> {

    private final Conexion cx;

    protected BaseDao(String key) {
        cx = Conexion.get(key);
    }

    /**
     * Obtiene la conexión asociada a la transacción actual.
     * @return La conexión solicitada.
     */
    public Connection getConnection() {
        return cx.getTransactionManager().getHandle();
    }

    /**
     * Obtiene el {@link LoggingManager} asociado a la conexión actual.
     * @return El gestor de logging solicitado.
     */
    public LoggingManager getLoggingManager() {
        return cx.getTransactionManager().getListener(LoggingManager.KEY, LoggingManager.class);
    }

    // Podríamos definir más métodos si hubiera las observadores, además del gestor de registros.
}
