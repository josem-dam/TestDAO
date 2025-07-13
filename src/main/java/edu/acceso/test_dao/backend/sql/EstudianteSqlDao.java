package edu.acceso.test_dao.backend.sql;

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

import javax.sql.DataSource;

import edu.acceso.test_dao.backend.core.Crud;
import edu.acceso.test_dao.backend.core.DataAccessException;
import edu.acceso.test_dao.backend.core.ConnProvider;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Estudiante;

/**
 * Implementación de {@link Crud} para la entidad {@link Estudiante} usando SQL.
 * Esta clase proporciona métodos para realizar operaciones CRUD sobre estudiantes
 * en una base de datos relacional.
 */
public class EstudianteSqlDao implements Crud<Estudiante> {

    /** Proveedor de conexiones. */
    private final ConnProvider cp;

    /**
     * Constructor que inicializa el proveedor de conexiones con un {@link DataSource}.
     *
     * @param ds Fuente de datos para obtener conexiones.
     */
    public EstudianteSqlDao(DataSource ds) {
        cp = new ConnProvider(ds);
    }

    /**
     * Constructor que inicializa el proveedor de conexiones con una conexión existente.
     * @param conn Conexión existente para el proveedor de conexiones.
     */
    public EstudianteSqlDao(Connection conn) {
        cp = new ConnProvider(conn);
    }

    /**
     * Convierte un {@link ResultSet} en un objeto {@link Estudiante}.
     *
     * @param rs El {@link ResultSet} que contiene los datos del estudiante.
     * @param conn Conexión para cargar el centro asociado al estudiante.
     * @return Un objeto {@link Estudiante} con los datos del {@link ResultSet}.
     * @throws SQLException Si ocurre un error al acceder a los datos del {@link ResultSet}.
     */
    private static Estudiante resultSetToEstudiante(ResultSet rs, Connection conn) throws SQLException {
        Long id = rs.getLong("id_estudiante");
        String nombre = rs.getString("nombre");
        Date nac = rs.getDate("nacimiento");
        LocalDate nacimiento = nac == null?null:nac.toLocalDate();
    
        Long idCentro = rs.getLong("centro");
        Centro centro = null;

        // Carga inmediata.
        if(idCentro != null) {
            CentroSqlDao centroDao = new CentroSqlDao(conn);
            try {
                centro = centroDao.get(idCentro).orElse(null);
                assert centro != null: "Identificador como clave foránea no existe";
            }
            catch(DataAccessException e) {
                throw new SQLException(e);
            }
        }

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
        String sqlString = "SELECT * FROM Estudiante WHERE id_estudiante = ?";

        try(Connection conn = cp.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, id);
                try(ResultSet rs = pstmt.executeQuery()) {
                    return rs.next()?Optional.of(resultSetToEstudiante(rs, conn)):Optional.empty();
                }
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible obtener el estudiante", e);
        }
    }

    @Override
    public List<Estudiante> get() throws DataAccessException {
        String sqlString = "SELECT * FROM Estudiante";
        List<Estudiante> estudiantes = new ArrayList<>();

        try(Connection conn = cp.getConnection()) {
            try(Statement pstmt = conn.createStatement()) {
                try(ResultSet rs = pstmt.executeQuery(sqlString)) {
                    while(rs.next()) {
                        estudiantes.add(resultSetToEstudiante(rs, conn));
                    }
                    return estudiantes;
                }
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible obtener el listado de estudiantes", e);
        }
    }

    public boolean delete(Long id) throws DataAccessException {
        String sqlString = "DELETE FROM Estudiante WHERE id_estudiante = ?";

        try(Connection conn = cp.getConnection();) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, id);
                return pstmt.executeUpdate() > 0;
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible borrar el estudiante", e);
        }
    }

    @Override
    public void insert(Estudiante estudiante) throws DataAccessException {
        String sqlString = "INSERT INTO Estudiante (nombre, nacimiento, centro, id_estudiante) VALUES (?, ?, ?, ?)";

        try(Connection conn = cp.getConnection();) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS)) {
                estudianteToParams(pstmt, estudiante);
                pstmt.executeUpdate();
                try(ResultSet rs = pstmt.getGeneratedKeys())  {
                    if(rs.next()) estudiante.setId(rs.getLong(1));
                }
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible agregar el estudiante", e);
        }
    }

    @Override
    public boolean update(Estudiante estudiante) throws DataAccessException {
        String sqlString = "UPDATE Estudiante Centro SET nombre = ?, nacimiento = ?, centro = ? WHERE id_estudiante = ?";

        try(Connection conn = cp.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                estudianteToParams(pstmt, estudiante);
                return pstmt.executeUpdate() > 0;
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible actualizar el estudiante", e);
        }
    }

    @Override
    public boolean update(Long oldId, Long newId) throws DataAccessException {
        String sqlString = "UPDATE Estudiante SET id_estudiante = ? WHERE id_estudiante = ?";
        try(Connection conn = cp.getConnection();) {
            try(PreparedStatement pstmt = conn.prepareStatement(sqlString)) {
                pstmt.setLong(1, oldId);
                pstmt.setLong(2, newId);
                return pstmt.executeUpdate() > 0;
            }
        }
        catch(SQLException e) {
            throw new DataAccessException("Imposible actualizar el identificador del estudiante", e);
        }
    }
}
