package edu.acceso.test_dao.backend.core;

/**
 * Excepción para errores de acceso a datos.
 */
public class DataAccessException extends Exception {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
 
    public DataAccessException(Throwable cause) {
        super(cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
