package edu.acceso.test_dao.persistence.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.sqlutils.jdbc.SqlAssistant.KeyHandler;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Estudiante;
import edu.acceso.test_dao.persistence.Conexion;

/**
 * Implementación de {@link Crud} para la entidad {@link Estudiante} usando SQL.
 * Esta clase proporciona métodos para realizar operaciones CRUD sobre estudiantes
 * en una base de datos relacional.
 */
public class EstudianteSqlDao implements Crud<Estudiante> {
    private static final Logger logger = LoggerFactory.getLogger(CentroSqlDao.class);

    private final Conexion cx;

    /**
     * Constructor que inicializa el proveedor de conexiones con una conexión existente.
     * @param key La clave de la conexión a usar.
     */
    public EstudianteSqlDao(String key) {
        cx = Conexion.get(key);
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
        return cx.getSqlAssistant().selectOne(sqlString, (rs, num) -> resultSetToEstudiante(rs, "", "c_"), id);
    }

    @Override
    public List<Estudiante> get() throws DataAccessException {
        String sqlString = """
            SELECT e.*, c.id_centro AS c_id, c.nombre AS c_nombre, c.titularidad AS c_titularidad
            FROM Centro c JOIN Estudiante e ON e.centro = c.id
            """;
        return cx.getSqlAssistant().select(sqlString, (rs, num) -> resultSetToEstudiante(rs, "", "c_"));
    }

    public void delete(Long id) throws DataAccessException {
        String sqlString = "DELETE FROM Estudiante WHERE id = ?";
        cx.getSqlAssistant().execute(sqlString, id);
    }

    @Override
    public void insert(Estudiante estudiante) throws DataAccessException {
        String sqlString = "INSERT INTO Estudiante (nombre, nacimiento, centro, id) VALUES (?, ?, ?, ?)";
        KeyHandler keyHandler = new KeyHandler();

        cx.getSqlAssistant().execute(conn -> {
            PreparedStatement pstmt = conn.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS);
            estudianteToParams(pstmt, estudiante);
            return pstmt;
        }, keyHandler);

        estudiante.setId(((Integer) keyHandler.getGeneratedKeys()[0]).longValue());
    }

    @Override
    public void update(Estudiante estudiante) throws DataAccessException {
        String sqlString = "UPDATE Estudiante SET nombre = ?, nacimiento = ?, centro = ? WHERE id = ?";

        cx.getSqlAssistant().execute(sqlString,
            new Integer[] {
                Types.VARCHAR,
                Types.DATE,
                Types.BIGINT,
                Types.BIGINT
            },
            new Object[] {
                estudiante.getNombre(),
                estudiante.getNacimiento(),
                estudiante.getCentro() == null?null:estudiante.getCentro().getId(),
                estudiante.getId()
        });
    }

    @Override
    public void update(Long oldId, Long newId) throws DataAccessException {
        String sqlString = "UPDATE Estudiante SET id_estudiante = ? WHERE id_estudiante = ?";
        cx.getSqlAssistant().execute(sqlString, newId, oldId);
    }
}
