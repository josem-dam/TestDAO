package edu.acceso.test_dao.backend.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import edu.acceso.test_dao.backend.core.Crud;
import edu.acceso.test_dao.backend.core.DataAccessException;
import edu.acceso.test_dao.backend.core.ConnProvider;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;

/**
 * Implementación de {@link Crud} para la entidad {@link Centro} usando SQL.
 * Esta clase proporciona métodos para realizar operaciones CRUD sobre centros
 * en una base de datos relacional.
 */
public class CentroSqlDao implements Crud<Centro> {

    /** Proveedor de conexiones. */
    private final ConnProvider cp;

    /**
     * Constructor que inicializa el proveedor de conexiones con un {@link DataSource}.
     *
     * @param ds Fuente de datos para obtener conexiones.
     */
    public CentroSqlDao(DataSource ds) {
        cp = new ConnProvider(ds);
    }

    /**
     * Constructor que inicializa el proveedor de conexiones con una conexión existente.
     * @param conn Conexión existente para el proveedor de conexiones.
     */
    public CentroSqlDao(Connection conn) {
        cp = new ConnProvider(conn);
    }

    /**
     * Convierte un {@link ResultSet} en un objeto {@link Centro}.
     *
     * @param rs El {@link ResultSet} que contiene los datos del centro.
     * @return Un objeto {@link Centro} con los datos del {@link ResultSet}.
     * @throws SQLException Si ocurre un error al acceder a los datos del {@link ResultSet}.
     */
    private static Centro resultSetToCentro(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id_centro");
        String nombre = rs.getString("nombre");
        Titularidad titularidad = Titularidad.fromString(rs.getString("titularidad"));
        return new Centro(id, nombre, titularidad);
    }

    /**
     * Establece los parámetros de un {@link PreparedStatement} con los datos de un {@link Centro}.
     *
     * @param pstmt El {@link PreparedStatement} donde se establecerán los parámetros.
     * @param centro El objeto {@link Centro} cuyos datos se usarán para establecer los parámetros.
     * @throws SQLException Si ocurre un error al establecer los parámetros en el {@link PreparedStatement}.
     */
    private static void centroToParams(PreparedStatement pstmt, Centro centro) throws SQLException {
        pstmt.setString(1, centro.getNombre());
        pstmt.setString(2, centro.getTitularidad().toString());
        // En este caso el ID siempre tiene valor, con lo que puede usarse directamente setInt.
        pstmt.setObject(3, centro.getId(), Types.BIGINT);
    }

    @Override
    public Optional<Centro> get(Long id) throws DataAccessException {
        String sqlString = "SELECT * FROM Centro WHERE id_centro = ?";

        try(Connection conn = cp.getConnection();) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, id);
                try(ResultSet rs = pstmt.executeQuery()) {
                    return rs.next()?Optional.of(resultSetToCentro(rs)):Optional.empty();
                }
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible obtener el centro", e);
        }
    }

    @Override
    public List<Centro> get() throws DataAccessException {
        String sqlString = "SELECT * FROM Centro";
        List<Centro> centros = new ArrayList<>();

        try(Connection conn = cp.getConnection()) {
            try(Statement pstmt = conn.createStatement()) {
                try(ResultSet rs = pstmt.executeQuery(sqlString)) {
                    while(rs.next()) {
                        centros.add(resultSetToCentro(rs));
                    }
                    return centros;
                }
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible obtener el listado de centros", e);
        }
    }

    @Override
    public boolean delete(Long id) throws DataAccessException {
        String sqlString = "DELETE FROM Centro WHERE id_centro = ?";

        try(Connection conn = cp.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, id);
                return pstmt.executeUpdate() > 0;
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible borrar el centro", e);
        }
    }

    @Override
    public void insert(Centro centro) throws DataAccessException {
        String sqlString = "INSERT INTO Centro (nombre, titularidad, id_centro) VALUES (?, ?, ?)";

        try(Connection conn = cp.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                centroToParams(pstmt, centro);
                pstmt.executeUpdate();
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible agregar el centro", e);
        }
    }

    @Override
    public boolean update(Centro centro) throws DataAccessException {
        String sqlString = "UPDATE Centro SET nombre = ?, titularidad = ? WHERE id_centro = ?";

        try(Connection conn = cp.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                centroToParams(pstmt, centro);
                return pstmt.executeUpdate() > 0;
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible actualizar el centro", e);
        }
    }

    @Override
    public boolean update(Long oldId, Long newId) throws DataAccessException {
        String sqlString = "UPDATE Centro SET id_centro = ? WHERE id_centro = ?";
        try(Connection conn = cp.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, oldId);
                pstmt.setLong(2, newId);
                return pstmt.executeUpdate() > 0;
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible actualizar el identificador del centro", e);
        }
    }
}