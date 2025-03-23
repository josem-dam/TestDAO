package edu.acceso.test_dao.backend.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import edu.acceso.test_dao.backend.DataAccessException;

/**
 * Envoltorio para objetos DataSource y Connection
 * que al cerrarse deja abierta la conexión si se creó
 * con una conexión, pero la cierra si se creó con un DataSource.
 */
public class ConnProvider {

    private final DataSource ds;
    private final Connection conn;

    public static class ConnWrapper implements AutoCloseable {

        private final Connection conn;
        private final boolean closeable;

        public ConnWrapper(DataSource ds) throws DataAccessException {
            try {
                conn = ds.getConnection();
            }
            catch(SQLException e) {
                throw new DataAccessException(e);
            }
            closeable = true;
        }

        public ConnWrapper(Connection conn) {
            this.conn = conn;
            closeable = false;
        }

        public Connection getConnection() {
            return conn;
        }

        @Override
        public void close() throws SQLException {
            if(closeable) conn.close();
        }
    }

    public ConnProvider(DataSource ds) {
        this.ds = ds;
        conn = null;
    }

    public ConnProvider(Connection conn) {
        ds = null;
        this.conn = conn;
    }

    public ConnWrapper getConnWrapper() throws DataAccessException {
        return ds == null?new ConnWrapper(conn):new ConnWrapper(ds);
    }
}