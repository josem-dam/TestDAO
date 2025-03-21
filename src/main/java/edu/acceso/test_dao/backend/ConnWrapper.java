package edu.acceso.test_dao.backend;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Envoltorio para objetos DataSource y Connection
 * que al cerrarse deja abierta la conexión si se creó
 * con una conexión, pero la cierra si se creó con un DataSource.
 */
public class ConnWrapper implements AutoCloseable {

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
