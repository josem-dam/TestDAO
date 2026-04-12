package edu.acceso.test_dao.persistence.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.sqlutils.tx.event.LoggingManager;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Estudiante;

/**
 * Implementación de {@link Crud} para la entidad {@link Estudiante} usando SQL.
 * Esta clase proporciona métodos para realizar operaciones CRUD sobre estudiantes
 * en una base de datos relacional.
 */
public class EstudianteSqlDao extends BaseDao<Estudiante> {
    private static final Logger logger = LoggerFactory.getLogger(CentroSqlDao.class);

    /**
     * Constructor que inicializa el proveedor de conexiones con una conexión existente.
     * @param key La clave de la conexión a usar.
     */
    public EstudianteSqlDao(String key) {
        super(key);
    }

    /**
     * Convierte un {@link ResultSet} en un objeto {@link Estudiante}.
     *
     * @param rs El {@link ResultSet} que contiene los datos del estudiante.
     * @param conn Conexión para cargar el centro asociado al estudiante.
     * @return Un objeto {@link Estudiante} con los datos del {@link ResultSet}.
     * @throws SQLException Si ocurre un error al acceder a los datos del {@link ResultSet}.
     */
    private static Estudiante resultSetToEstudiante(ResultSet rs, String prefix, String cPrefix) throws SQLException {
        Long id = rs.getLong(prefix + "id");
        String nombre = rs.getString(prefix + "nombre");
        Date nac = rs.getDate(prefix + "nacimiento");
        LocalDate nacimiento = nac == null?null:nac.toLocalDate();
    
        rs.getLong("centro");
        Centro centro = rs.wasNull()?null:CentroSqlDao.resultSetToCentro(rs, cPrefix);

        return new Estudiante(id, nombre, nacimiento, centro);
    }

    /**
     * Establece los parámetros de un {@link PreparedStatement} con los datos de un {@link Estudiante}.
     *
     * @param pstmt El {@link PreparedStatement} donde se establecerán los parámetros.
     * @param estudiante El objeto {@link Estudiante} cuyos datos se usarán para establecer los parámetros.
     * @throws SQLException Si ocurre un error al establecer los parámetros en el {@link PreparedStatement}.
     */
    private static void estudianteToParams(PreparedStatement pstmt, Estudiante estudiante) throws SQLException {
        pstmt.setString(1, estudiante.getNombre());
        LocalDate nacimiento = estudiante.getNacimiento();
        pstmt.setDate(2, nacimiento == null?null:Date.valueOf(nacimiento));
        Centro centro = estudiante.getCentro();
        pstmt.setObject(3, centro == null?null:centro.getId(), Types.BIGINT);
        pstmt.setObject(4, estudiante.getId() == null?null:estudiante.getId(), Types.BIGINT);
    }

    @Override
    public Optional<Estudiante> get(Long id) throws DataAccessException {
        String sqlString = """
            SELECT e.*, c.id_centro AS c_id, c.nombre AS c_nombre, c.titularidad AS c_titularidad
            FROM Centro c JOIN Estudiante e ON e.centro = c.id 
            WHERE e.id = ?
            """;

        try(Connection conn = getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, id);
                try(ResultSet rs = pstmt.executeQuery()) {
                    Estudiante estudiante = rs.next() ? resultSetToEstudiante(rs, "", "c_") : null;
                    if(estudiante == null) logger.trace("Estudiante con ID={} no encontrado", id);
                    else logger.trace("Estudiante con ID={} encontrado", id);
                    return Optional.ofNullable(estudiante);
                }
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible obtener el estudiante: %s".formatted(e.getMessage()), e);
        }
    }

    @Override
    public List<Estudiante> get() throws DataAccessException {
        String sqlString = """
            SELECT e.*, c.id_centro AS c_id, c.nombre AS c_nombre, c.titularidad AS c_titularidad
            FROM Centro c JOIN Estudiante e ON e.centro = c.id
            """;
        List<Estudiante> estudiantes = new ArrayList<>();

        try(Connection conn = getConnection()) {
            try(Statement pstmt = conn.createStatement()) {
                try(ResultSet rs = pstmt.executeQuery(sqlString)) {
                    while(rs.next()) {
                        estudiantes.add(resultSetToEstudiante(rs, "", "c_"));
                    }
                    logger.trace("Obtenidos {} estudiantes", estudiantes.size());
                    return estudiantes;
                }
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible obtener el listado de estudiantes: %s".formatted(e.getMessage()), e);
        }
    }

    public void delete(Long id) throws DataAccessException {
        String sqlString = "DELETE FROM Estudiante WHERE id = ?";
        LoggingManager lm = getLoggingManager();

        try(Connection conn = getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, id);
                boolean deleted = pstmt.executeUpdate() > 0;
                if(deleted) {
                    lm.sendMessage(
                        getClass(),
                        Level.DEBUG,
                        "Estudiante con ID=%d borrado".formatted(id),
                        "Trasacción fallida: Estudiante con ID=%d no se llega a borrar".formatted(id)
                    );
                }
                else logger.trace("Estudiante con ID={} no encontrado", id);
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible borrar el estudiante %d: %s".formatted(id, e.getMessage()), e);
        }
    }

    @Override
    public void insert(Estudiante estudiante) throws DataAccessException {
        String sqlString = "INSERT INTO Estudiante (nombre, nacimiento, centro, id) VALUES (?, ?, ?, ?)";
        LoggingManager lm = getLoggingManager();

        try(Connection conn = getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS)) {
                estudianteToParams(pstmt, estudiante);
                pstmt.executeUpdate();
                try(ResultSet rs = pstmt.getGeneratedKeys())  {
                    if(rs.next()) estudiante.setId(rs.getLong(1));
                }
                lm.sendMessage(
                    getClass(),
                    Level.DEBUG,
                    "Estudiante con ID=%d agregado".formatted(estudiante.getId()),
                    "Trasacción fallida: Estudiante con ID=%d no se llega a agregar".formatted(estudiante.getId())
                );
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible agregar el estudiante con ID=%d: %s".formatted(estudiante.getId(), e.getMessage()), e);
        }
    }

    @Override
    public void update(Estudiante estudiante) throws DataAccessException {
        String sqlString = "UPDATE Estudiante SET nombre = ?, nacimiento = ?, centro = ? WHERE id = ?";
        LoggingManager lm = getLoggingManager();

        try(Connection conn = getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                estudianteToParams(pstmt, estudiante);
                boolean updated = pstmt.executeUpdate() > 0;
                if(updated) {
                    lm.sendMessage(
                        getClass(),
                        Level.DEBUG,
                        "Estudiante con ID=%d actualizado".formatted(estudiante.getId()),
                        "Trasacción fallida: Estudiante con ID=%d no se llega a actualizar".formatted(estudiante.getId())
                    );
                }
                else logger.trace("Estudiante con ID={} no encontrado", estudiante.getId());
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible actualizar el estudiante con ID=%d: %s".formatted(estudiante.getId(), e.getMessage()), e);
        }
    }

    @Override
    public void update(Long oldId, Long newId) throws DataAccessException {
        String sqlString = "UPDATE Estudiante SET id_estudiante = ? WHERE id_estudiante = ?";
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
                        "Estudiante con ID=%d actualizado a ID=%d".formatted(oldId, newId),
                        "Trasacción fallida: Estudiante con ID=%d no se llega a actualizar a ID=%d".formatted(oldId, newId)
                    );
                }
                else logger.trace("Estudiante con ID={} no encontrado", oldId);
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible actualizar el identificador del estudiante: %s".formatted(e.getMessage()), e);
        }
    }
}
