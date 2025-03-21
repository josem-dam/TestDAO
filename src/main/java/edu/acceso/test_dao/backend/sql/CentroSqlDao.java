package edu.acceso.test_dao.backend.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import edu.acceso.test_dao.backend.ConnWrapper;
import edu.acceso.test_dao.backend.Crud;
import edu.acceso.test_dao.backend.DataAccessException;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;

public class CentroSqlDao implements Crud<Centro> {

    private final ConnWrapper conector;

    public CentroSqlDao(DataSource ds) throws DataAccessException {
        conector = new ConnWrapper(ds);
    }

    public CentroSqlDao(Connection conn) {
        conector = new ConnWrapper(conn);
    }

    public static Centro resultSetToCentro(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String nombre = rs.getString("nombre");
        Titularidad titularidad = Titularidad.fromNombre(rs.getString("titularidad"));
        return new Centro(id, nombre, titularidad);
    }

    @Override
    public Optional<Centro> get(Long id) throws DataAccessException {
        String sqlString = "SELECT * FROM Centro WHERE id = ?";

        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public List<Centro> get() throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public boolean delete(Long id) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void insert(Centro obj) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insert'");
    }

    @Override
    public boolean update(Centro obj) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public boolean update(Long oldId, Long newId) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

}
