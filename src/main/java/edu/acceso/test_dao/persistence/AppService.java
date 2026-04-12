package edu.acceso.test_dao.persistence;

import java.util.List;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;
import edu.acceso.test_dao.persistence.dao.Crud;
import edu.acceso.test_dao.modelo.Estudiante;

/**
 * Clase de servicio para manejar la lógica de negocio relacionada con la persistencia de datos.
 * Como apenas hay operaciones, creamos una única clase para todas las operaciones de persistencia.
 */
public class AppService {
    private final Conexion cx;

    public AppService(Conexion conexion) {
        this.cx = conexion;
    }

    /**
     * Obtiene la lista de centros educativos desde la base de datos.
     * @return La lista solicitada
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public List<Centro> listarCentros() throws DataAccessException {
        return cx.transactionR(ctxt -> cx.getDao(Centro.class).get());
    }

    /**
     * Agrega un nuevo estudiante a la base de datos.
     * @param estudiante El estudiante a agregar.
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public void agregarEstudiante(Estudiante estudiante) throws DataAccessException {
        cx.transaction(ctxt -> cx.getDao(Estudiante.class).insert(estudiante));
    }

    /**
     * Modifica un centro educativo existente en la base de datos.
     * @param centro El centro con los datos modificados (el ID debe seguir siendo el mismo).
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public void modificarCentro(Centro centro) throws DataAccessException {
        cx.transaction(ctxt -> cx.getDao(Centro.class).update(centro));
    }

    /**
     * Agrega un nuevo centro educativo a la base de datos.
     * @param centro El centro a agregar (su ID se asignará automáticamente al insertarlo).
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public void agregarCentro(Centro centro) throws DataAccessException {
        cx.transaction(ctxt -> cx.getDao(Centro.class).insert(centro));
    }

    public void operacionMultiple() throws DataAccessException {
        cx.transaction(ctxt -> {
            Crud<Centro> centroDao = cx.getDao(Centro.class);

            centroDao.delete(11701164L);

            // Intentamos añadir un centro cuyo ID ya existe.
            centroDao.insert(new Centro(11004866L, "IES Centro repetido", Titularidad.PUBLICA));
        });
    }
}
