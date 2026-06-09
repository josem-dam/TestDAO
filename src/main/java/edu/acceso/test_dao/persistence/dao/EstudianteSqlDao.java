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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Estudiante;
import edu.acceso.test_dao.persistence.LoggingManager;

/**
 * Implementación de {@link Crud} para la entidad {@link Estudiante} usando SQL.
 * Esta clase proporciona métodos para realizar operaciones CRUD sobre estudiantes
 * en una base de datos relacional.
 */
@Repository
public class EstudianteSqlDao implements Crud<Estudiante> {
    private static final Logger logger = LoggerFactory.getLogger(CentroSqlDao.class);

    private final JdbcTemplate jt;
    private final ApplicationEventPublisher publisher;

    /**
     * Constructor que inicializa el proveedor de conexiones con una conexión existente.
     * @param jt El {@link JdbcTemplate} a usar para las operaciones de base de datos.
     * @param publisher El publicador de eventos de la aplicación.
     */
    public EstudianteSqlDao(JdbcTemplate jt, ApplicationEventPublisher publisher) {
        this.jt = jt;
        this.publisher = publisher;
    }

    /**
     * Convierte un {@link ResultSet} en un objeto {@link Estudiante}.
     *
     * @param rs El {@link ResultSet} que contiene los datos del estudiante.
     * @param prefix Prefijo para los nombres de las columnas del estudiante.
     * @param cPrefix Prefijo para los nombres de las columnas del centro.
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
    public Optional<Estudiante> get(Long id) {
        String sqlString = """
            SELECT e.*, c.id_centro AS c_id, c.nombre AS c_nombre, c.titularidad AS c_titularidad
            FROM Centro c JOIN Estudiante e ON e.centro = c.id 
            WHERE e.id = ?
            """;

        Estudiante estudiante = null;

        try {
            estudiante = jt.queryForObject(sqlString, (rs, rowNum) -> resultSetToEstudiante(rs, "", "c_"), id);
            logger.trace("Estudiante con ID={} encontrado: {}", id, estudiante);
        } catch(EmptyResultDataAccessException e) {
            logger.trace("Estudiante con ID={} no encontrado", id);
        }
        
        return Optional.ofNullable(estudiante);
    }

    @Override
    public List<Estudiante> get() {
        String sqlString = """
            SELECT e.*, c.id_centro AS c_id, c.nombre AS c_nombre, c.titularidad AS c_titularidad
            FROM Centro c JOIN Estudiante e ON e.centro = c.id
            """;

        List<Estudiante> estudiantes = jt.query(sqlString, (rs, rowNum) -> resultSetToEstudiante(rs, "", "c_"));
        logger.trace("{} estudiantes encontrados", estudiantes.size());
        return estudiantes;
    }

    public void delete(Long id) {
        String sqlString = "DELETE FROM Estudiante WHERE id = ?";

        boolean deleted = jt.update(sqlString, id) > 0;
        if(deleted) publisher.publishEvent(new LoggingManager.EventMessage("borrado de estudiante con ID=%d".formatted(id)));
        else logger.trace("Estudiante con ID={} no encontrado", id);
    }

    @Override
    public void insert(Estudiante estudiante) {
        String sqlString = "INSERT INTO Estudiante (nombre, nacimiento, centro, id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jt.update(conn -> {
            PreparedStatement pstmt = conn.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS);
            estudianteToParams(pstmt, estudiante);
            return pstmt;
        }, keyHolder);

        if(keyHolder.getKey() != null) {
            estudiante.setId(keyHolder.getKey().longValue());
            publisher.publishEvent(new LoggingManager.EventMessage("inserción de estudiante con ID=%d".formatted(estudiante.getId())));
        }
        else logger.warn("No se pudo obtener el ID generado para el estudiante agregado");
    }

    @Override
    public void update(Estudiante estudiante) {
        String sqlString = "UPDATE Estudiante SET nombre = ?, nacimiento = ?, centro = ? WHERE id = ?";

        boolean updated = jt.update(sqlString, estudiante.getNombre(), estudiante.getNacimiento(), estudiante.getCentro() == null?null:estudiante.getCentro().getId(), estudiante.getId()) > 0;
        if(updated) publisher.publishEvent(new LoggingManager.EventMessage("actualización de estudiante con ID=%d".formatted(estudiante.getId())));
        else logger.trace("Estudiante con ID={} no encontrado", estudiante.getId());
    }

    @Override
    public void update(Long oldId, Long newId) {
        String sqlString = "UPDATE Estudiante SET id = ? WHERE id = ?";

        boolean updated = jt.update(sqlString, newId, oldId) > 0;
        if(updated) publisher.publishEvent(new LoggingManager.EventMessage("actualización del ID del estudiante con ID=%d".formatted(oldId)));
        else logger.trace("Estudiante con ID={} no encontrado", oldId);
    }
}
