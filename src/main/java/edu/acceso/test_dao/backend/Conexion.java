package edu.acceso.test_dao.backend;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import edu.acceso.sqlutils.ConnectionPool;
import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.sqlutils.tx.TransactionManager;
import edu.acceso.sqlutils.tx.TransactionManager.Transactionable;
import edu.acceso.sqlutils.tx.TransactionManager.TransactionableR;

public class Conexion {

    private static final Map<String, Conexion> instances = new ConcurrentHashMap<>(); 

    private final DataSource ds;
    private final TransactionManager tm;

    private Conexion(String key, String dbUrl, String user, String password) {
        ds = ConnectionPool.getInstance(dbUrl, user, password);
        tm = TransactionManager.create(key, ds);
    }

    public static Conexion create(String key, String dbUrl, String user, String password) {
        return instances.computeIfAbsent(key, k -> {
            if(instances.containsKey(k)) throw new IllegalStateException("Ya existe una conexión para la clave %s".formatted(k));
            return new Conexion(k, dbUrl, user, password);
        });
    }

    public static Conexion get(String key) {
        Conexion instance = instances.get(key);
        if (instance == null) throw new IllegalStateException("No existe una conexión para la clave %s".formatted(key));
        return instance;
    }

    public Connection getConnection() {
        return tm.getConnection();
    }

    public <T> T transactionR(TransactionableR<T> operations) throws DataAccessException {
        return tm.transaction(operations);
    }

    public void transaction(Transactionable operations) throws DataAccessException {
        tm.transaction(operations);
    }
}
