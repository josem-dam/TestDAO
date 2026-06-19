package edu.acceso.test_dao.persistence.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;
import edu.acceso.test_dao.persistence.Conexion;

/**
 * Implementación de {@link Crud} para la entidad {@link Centro} usando SQL.
 * Esta clase proporciona métodos para realizar operaciones CRUD sobre centros
 * en una base de datos relacional.
 */
public class CentroSqlDao implements Crud<Centro> {
    private static final Logger logger = LoggerFactory.getLogger(CentroSqlDao.class);

    private final Conexion cx;

    /**
     * Constructor que inicializa el proveedor de conexiones con una conexión existente.
     * @param key La clave de la conexión a usar.
     */
    public CentroSqlDao(String key) {
        cx = Conexion.get(key);
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

    @Override
    public Optional<Centro> get(Long id) throws DataAccessException {
        String sqlString = "SELECT * FROM Centro WHERE id = ?";
        return cx.getSqlAssistant().selectOne(sqlString, (rs, num) -> resultSetToCentro(rs, ""), id);
    }

    @Override
    public List<Centro> get() throws DataAccessException {
        String sqlString = "SELECT * FROM Centro";
        return cx.getSqlAssistant().select(sqlString, (rs, num) -> resultSetToCentro(rs, ""));
    }

    @Override
    public void delete(Long id) throws DataAccessException {
        String sqlString = "DELETE FROM Centro WHERE id = ?";
        cx.getSqlAssistant().execute(sqlString, id);
    }

    @Override
    public void insert(Centro centro) throws DataAccessException {
        String sqlString = "INSERT INTO Centro (nombre, titularidad, id) VALUES (?, ?, ?)";
        cx.getSqlAssistant().execute(sqlString, centro.getNombre(), centro.getTitularidad().toString(), centro.getId());
    }

    @Override
    public void update(Centro centro) throws DataAccessException {
        String sqlString = "UPDATE Centro SET nombre = ?, titularidad = ? WHERE id = ?";
        cx.getSqlAssistant().execute(sqlString, centro.getNombre(), centro.getTitularidad().toString(), centro.getId());
    }

    @Override
    public void update(Long oldId, Long newId) throws DataAccessException {
        String sqlString = "UPDATE Centro SET id_centro = ? WHERE id_centro = ?";
        cx.getSqlAssistant().execute(sqlString, newId, oldId);
    }
}