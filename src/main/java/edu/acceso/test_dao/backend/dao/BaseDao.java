package edu.acceso.test_dao.backend.dao;

import java.sql.Connection;

import edu.acceso.sqlutils.crud.Entity;
import edu.acceso.test_dao.backend.Conexion;
import edu.acceso.test_dao.backend.Crud;

public abstract class BaseDao<T extends Entity> implements Crud<T> {

    protected final String key;

    public BaseDao(String key) {
        this.key = key;
    }

    public Connection getConnection() {
        return Conexion.get(key).getConnection();
    }
}
