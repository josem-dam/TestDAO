package edu.acceso.test_dao.persistence;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;
import edu.acceso.test_dao.persistence.dao.Crud;
import edu.acceso.test_dao.modelo.Estudiante;

/**
 * Clase de servicio para manejar la lógica de negocio relacionada con la persistencia de datos.
 * Como apenas hay operaciones, creamos una única clase para todas las operaciones de persistencia.
 */
@Service
public class AppService {

    private final Crud<Centro> centroDao;
    private final Crud<Estudiante> estudianteDao;

    /**
     * Constructor
     * @param key La clave de la conexión a usar para acceder a la base de datos.
     */
    public AppService(Crud<Centro> centroDao, Crud<Estudiante> estudianteDao) {
        this.centroDao = centroDao;
        this.estudianteDao = estudianteDao;
    }

    /**
     * Obtiene la lista de centros educativos desde la base de datos.
     * @return La lista solicitada
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public List<Centro> listarCentros() {
        return centroDao.get();
    }

    /**
     * Agrega un nuevo estudiante a la base de datos.
     * @param estudiante El estudiante a agregar.
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public void agregarEstudiante(Estudiante estudiante) {
        estudianteDao.insert(estudiante);
    }

    /**
     * Modifica un centro educativo existente en la base de datos.
     * @param centro El centro con los datos modificados (el ID debe seguir siendo el mismo).
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public void modificarCentro(Centro centro) {
        centroDao.update(centro);
    }

    /**
     * Agrega un nuevo centro educativo a la base de datos.
     * @param centro El centro a agregar (su ID se asignará automáticamente al insertarlo).
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public void agregarCentro(Centro centro) {
        centroDao.insert(centro);
    }

    /**
     * Método que implica varias operaciones de persistencia (un borrado y una inserción).
     * Conceptualemente, no tienen relación: simplemente sirven para probar que ambas
     * forman parte de una misma transacción.
     */
    @Transactional
    public void operacionMultiple() {
            centroDao.delete(11701164L);
            // Intentamos añadir un centro cuyo ID ya existe.
            centroDao.insert(new Centro(11004866L, "IES Centro repetido", Titularidad.PUBLICA));
    }
}
