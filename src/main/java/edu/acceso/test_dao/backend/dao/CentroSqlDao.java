package edu.acceso.test_dao.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.sqlutils.tx.LoggingManager;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;

/**
 * Implementación de {@link Crud} para la entidad {@link Centro} usando SQL.
 * Esta clase proporciona métodos para realizar operaciones CRUD sobre centros
 * en una base de datos relacional.
 */
public class CentroSqlDao extends BaseDao<Centro> {
    private static final Logger logger = LoggerFactory.getLogger(CentroSqlDao.class);

    /**
     * Constructor que inicializa el proveedor de conexiones con una conexión existente.
     * @param key La clave de la conexión a usar.
     */
    public CentroSqlDao(String key) {
        super(key);
    }

    /**
     * Convierte un {@link ResultSet} en un objeto {@link Centro}.
     *
     * @param rs El {@link ResultSet} que contiene los datos del centro.
     * @return Un objeto {@link Centro} con los datos del {@link ResultSet}.
     * @throws SQLException Si ocurre un error al acceder a los datos del {@link ResultSet}.
     */
    static Centro resultSetToCentro(ResultSet rs, String prefix) throws SQLException {
        Long id = rs.getLong(prefix + "id");
        String nombre = rs.getString(prefix + "nombre");
        Titularidad titularidad = Titularidad.fromString(rs.getString(prefix + "titularidad"));
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
        String sqlString = "SELECT * FROM Centro WHERE id = ?";

        try(Connection conn = getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, id);
                try(ResultSet rs = pstmt.executeQuery()) {
                    Centro centro = rs.next() ? resultSetToCentro(rs, "") : null;
                    if(centro == null) logger.trace("Centro con ID={} no encontrado", id);
                    else logger.trace("Centro con ID={} encontrado", id);
                    return Optional.ofNullable(centro);
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

        try(Connection conn = getConnection()) {
            try(Statement pstmt = conn.createStatement()) {
                try(ResultSet rs = pstmt.executeQuery(sqlString)) {
                    while(rs.next()) {
                        centros.add(resultSetToCentro(rs, ""));
                    }
                    logger.trace("Obtenidos {} centros", centros.size());
                    return centros;
                }
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible obtener el listado de centros", e);
        }
    }

    @Override
    public void delete(Long id) throws DataAccessException {
        String sqlString = "DELETE FROM Centro WHERE id = ?";
        LoggingManager lm = getLoggingManager();

        try(Connection conn = getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, id);
                boolean deleted = pstmt.executeUpdate() > 0;
                if(deleted) {
                    lm.sendMessage(
                        getClass(),
                        Level.DEBUG,
                        "Centro con ID=%d borrado".formatted(id),
                        "Trasacción fallida: Centro con ID=%d no se llega a borrar".formatted(id)
                    );
                }
                else logger.trace("Centro con ID={} no encontrado", id);
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible borrar el centro con ID=%d".formatted(id), e);
        }
    }

    @Override
    public void insert(Centro centro) throws DataAccessException {
        String sqlString = "INSERT INTO Centro (nombre, titularidad, id) VALUES (?, ?, ?)";
        LoggingManager lm = getLoggingManager();

        try(Connection conn = getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS)) {
                centroToParams(pstmt, centro);
                pstmt.executeUpdate();
                try(ResultSet rs = pstmt.getGeneratedKeys()) {
                    if(rs.next()) centro.setId(rs.getLong(1));
                }
                lm.sendMessage(
                    getClass(),
                    Level.DEBUG,
                    "Centro con ID=%d agregado".formatted(centro.getId()),
                    "Trasacción fallida: Centro con ID=%d no se llega a agregar".formatted(centro.getId())
                );
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible agregar el centro con ID=%d".formatted(centro.getId()), e);
        }
    }

    @Override
    public void update(Centro centro) throws DataAccessException {
        String sqlString = "UPDATE Centro SET nombre = ?, titularidad = ? WHERE id = ?";
        LoggingManager lm = getLoggingManager();

        try(Connection conn = getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                centroToParams(pstmt, centro);
                boolean updated = pstmt.executeUpdate() > 0;
                if(updated) {
                    lm.sendMessage(
                        getClass(),
                        Level.DEBUG,
                        "Centro con ID=%d actualizado".formatted(centro.getId()),
                        "Trasacción fallida: Centro con ID=%d no se llega a actualizar".formatted(centro.getId())
                    );
                }
                else logger.trace("Centro con ID={} no encontrado", centro.getId());
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible actualizar el centro", e);
        }
    }

    @Override
    public void update(Long oldId, Long newId) throws DataAccessException {
        String sqlString = "UPDATE Centro SET id_centro = ? WHERE id_centro = ?";
        LoggingManager lm = getLoggingManager();

        try(Connection conn = getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, oldId);
                pstmt.setLong(2, newId);
                boolean updated = pstmt.executeUpdate() > 0;
                if(updated) {
                    lm.sendMessage(
                        getClass(),
                        Level.DEBUG,
                        "Centro con ID=%d actualizado a ID=%d".formatted(oldId, newId),
                        "Trasacción fallida: Centro con ID=%d no se llega a actualizar a ID=%d".formatted(oldId, newId)
                    );
                }
                else logger.trace("Centro con ID={} no encontrado", oldId);
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible actualizar el identificador del centro", e);
        }
    }
}