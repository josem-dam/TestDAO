package edu.acceso.test_dao.backend.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Proveedor de conexiones SQL que permite, al invocarse su método {@link #getConnection()},
 * obtener conexiones {@link Connection} a la base de datos.
 * 
 * <p>
 * Si se construye suministrando un {@link DataSource}, cada vez que se invoca el método
 * {@link #getConnection()} se genera una nueva conexión a través del pool. En cambio,
 * si se construye con una conexión existente, se reutiliza esa conexión, pero esta no
 * se cierra al invocarse el método {@link Connection#close()}.
 * <p>
 * La clase está pensada para ser utilizada en las clases DAO, e impedir que el código
 * incluido en dichas clases cierre la conexión cuando los objetos DAO se construyen
 * usando una conexión en vez de un {@link DataSource},
 */
public class ConnProvider {

    /** DataSource utilizado para obtener conexiones. */
    private final DataSource ds;
    /** Conexión utilizada si se construye el proveedor con una conexión existente. */
    private final Connection conn;

    /**
     *  Wrapper para conexiones que evita que se cierren al invocar close().
     *  Utiliza el patrón Proxy de Java para interceptar llamadas a métodos de la interfaz Connection.
     */
    private static class ConnectionWrapper implements InvocationHandler {

        /** Conexión original que se envuelve. */
        private final Connection conn;

        /**
         * Constructor privado que crea un wrapper para una conexión.
         * @param conn La conexión original a envolver.
         */
        private ConnectionWrapper(Connection conn) {
            this.conn = conn;
        }

        /**
         * Crea un proxy de la conexión original que evita que se cierre al invocar close().
         * @param conn La conexión original a envolver.
         * @return Un objeto Connection que actúa como proxy de la conexión original.
         */
        public static Connection createProxy(Connection conn) {
            return (Connection) Proxy.newProxyInstance(
                conn.getClass().getClassLoader(),
                new Class<?>[]{Connection.class},
                new ConnectionWrapper(conn)
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch(method.getName()) {
                // Evitamos cerrar la conexión al invocar close().
                case "close":
                    return null;
                // Cualquier otro método de la interfaz Connection
                // se delega al objeto Connection original.
                default:
                    return method.invoke(conn, args);
            }
        }
    }

    /**
     * Constructor que inicializa el proveedor de conexiones con un {@link DataSource}.
     * @param ds Fuente de datos para obtener conexiones.
     */
    public ConnProvider(DataSource ds) {
        this.ds = ds;
        conn = null;
    }

    /**
     * Constructor que inicializa el proveedor de conexiones con una conexión existente.
     * @param conn Conexión existente a utilizar.
     */
    public ConnProvider(Connection conn) {
        ds = null;
        this.conn = conn;
    }

    /**
     * Obtiene una conexión a la base de datos.
     * Si se construyó con un {@link DataSource}, devuelve una nueva conexión del pool.
     * Si se construyó con una conexión existente, devuelve esa conexión envuelta en un proxy.
     * @return Una conexión a la base de datos.
     * @throws DataAccessException Si ocurre un error al obtener la conexión.
     */
    public Connection getConnection() throws DataAccessException {
        if(ds != null) {
            try {
                return ds.getConnection();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
        else return ConnectionWrapper.createProxy(conn);
    }
}