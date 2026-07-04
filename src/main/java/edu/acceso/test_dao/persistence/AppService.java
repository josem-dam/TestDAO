package edu.acceso.test_dao.persistence;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.sqlutils.tx.event.LoggingManager;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;
import edu.acceso.test_dao.modelo.Estudiante;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * Clase de servicio para manejar la lógica de negocio relacionada con la persistencia de datos.
 * Como apenas hay operaciones, creamos una única clase para todas las operaciones de persistencia.
 */
public class AppService {
    private static final Logger logger = LoggerFactory.getLogger(AppService.class);

    private final Conexion cx;

    /**
     * Constructor
     * @param key La clave de la conexión a usar para acceder a la base de datos.
     */
    public AppService(String key) {
        cx = Conexion.get(key);
    }

    /**
     * Obtiene todas las entidades de una clase dada.
     * 
     * <p>Equivale al SQL:</p>
     * <pre>SELECT e.* FROM Entity e</pre>
     * @param <T> Tipo de la entidad.
     * @param em El EntityManager.
     * @param entityClass La clase de la entidad.
     * @return Una lista de todas las entidades de la clase dada.
     */
    private static <T> List<T> getAll(EntityManager em, Class<T> entityClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = cb.createQuery(entityClass);
        Root<T> entity = criteriaQuery.from(entityClass);
        criteriaQuery.select(entity);
        List<T> result = em.createQuery(criteriaQuery).getResultList();
        logger.debug("Se han recuperado {} registros de la entidad {}", result.size(), entityClass.getSimpleName());
        return result;
    }

    /**
     * Obtiene las entidades de una clase dada que cumplen uno de sus atributos es igual a un valor dado.
     * @param <T> Tipo de la entidad.
     * @param <V> Tipo del atributo.
     * @param em El EntityManager.
     * @param entityClass La clase de la entidad.
     * @param attribute El atributo por el que filtrar.
     * @param value El valor del atributo por el que filtrar.
     * @return Una lista de entidades que cumplen la condición.
     */
    private static <T, V> List<T> getWhere(EntityManager em, Class<T> entityClass, SingularAttribute<? super T, V> attribute, V value) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = cb.createQuery(entityClass);
        Root<T> entity = criteriaQuery.from(entityClass);
        criteriaQuery.select(entity).where(cb.equal(entity.get(attribute), value));
        List<T> result = em.createQuery(criteriaQuery).getResultList();
        logger.debug("Se han recuperado {} registros de la entidad {}", result.size(), entityClass.getSimpleName());
        return result;
    }

    /**
     * Obtiene la lista de centros educativos desde la base de datos.
     * @return La lista solicitada
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public List<Centro> listarCentros() throws DataAccessException {
        return cx.transactionR(ctxt -> {
            EntityManager em = ctxt.handle();
            List<Centro> centros = getAll(em, Centro.class);
            logger.trace("Se han recuperado {} centros educativos de la base de datos.", centros.size());
            return centros;
        });
    }

    /**
     * Agrega un nuevo estudiante a la base de datos.
     * @param estudiante El estudiante a agregar.
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public void agregarEstudiante(Estudiante estudiante) throws DataAccessException {
        cx.transaction(ctxt  -> {
            EntityManager em = ctxt.handle();
            LoggingManager lm = ctxt.getEventListener(LoggingManager.KEY, LoggingManager.class);

            em.persist(estudiante);
            lm.sendMessage(
                getClass(),
                Level.DEBUG,
                "Agregado un estudiante con ID=%d".formatted(estudiante.getId()),
                "Transacción fallida: no se ha agregado el estudiante con ID=%d".formatted(estudiante.getId())
            );
        });
    }

    /**
     * Modifica un centro educativo existente en la base de datos.
     * @param centro El centro con los datos modificados (el ID debe seguir siendo el mismo).
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public void modificarCentro(Centro centro) throws DataAccessException {
        cx.transaction(ctxt -> {
            EntityManager em = ctxt.handle();
            LoggingManager lm = ctxt.getEventListener(LoggingManager.KEY, LoggingManager.class);

            em.merge(centro);
            lm.sendMessage(
                getClass(),
                Level.DEBUG,
                "Modificado el centro con ID=%d".formatted(centro.getId()),
                "Transacción fallida: no se ha modificado el centro con ID=%d".formatted(centro.getId())
            );
        });
    }

    /**
     * Agrega un nuevo centro educativo a la base de datos.
     * @param centro El centro a agregar (su ID se asignará automáticamente al insertarlo).
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    public void agregarCentro(Centro centro) throws DataAccessException {
        cx.transaction(ctxt -> {
            EntityManager em = ctxt.handle();
            LoggingManager lm = ctxt.getEventListener(LoggingManager.KEY, LoggingManager.class);

            em.persist(centro);
            lm.sendMessage(
                getClass(),
                Level.DEBUG,
                "Agregado un centro con ID=%d".formatted(centro.getId()),
                "Transacción fallida: no se ha agregado el centro con ID=%d".formatted(centro.getId())
            );
        });
    }

    public void eliminarCentro(Centro centro) throws DataAccessException {
        cx.transaction(ctxt -> {
            EntityManager em = ctxt.handle();
            LoggingManager lm = ctxt.getEventListener(LoggingManager.KEY, LoggingManager.class);

            em.remove(centro);
            lm.sendMessage(
                getClass(),
                Level.DEBUG,
                "Eliminado el centro con ID=%d".formatted(centro.getId()),
                "Transacción fallida: no se ha eliminado el centro con ID=%d".formatted(centro.getId())
            );
        });
    }

    public void operacionMultiple() throws DataAccessException {
        cx.transaction(ctxt -> {
            EntityManager em = ctxt.handle();
            LoggingManager lm = ctxt.getEventListener(LoggingManager.KEY, LoggingManager.class);

            Centro centro = em.find(Centro.class, 11701164L);
            if(centro != null) eliminarCentro(centro);

            // Intentamos añadir un centro cuyo ID ya existe.
            agregarCentro(new Centro(11004866L, "IES Centro repetido", Titularidad.PUBLICA));
        });
    }
}
