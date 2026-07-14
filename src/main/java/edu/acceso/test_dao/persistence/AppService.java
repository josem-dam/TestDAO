package edu.acceso.test_dao.persistence;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;
import edu.acceso.test_dao.modelo.Estudiante;
import edu.acceso.test_dao.persistence.repository.CentroRepository;
import edu.acceso.test_dao.persistence.repository.EstudianteRepository;

/**
 * Clase de servicio para manejar la lógica de negocio relacionada con la persistencia de datos.
 * Como apenas hay operaciones, creamos una única clase para todas las operaciones de persistencia.
 */
@Service
public class AppService {
    private static final Logger logger = LoggerFactory.getLogger(AppService.class);

    private final CentroRepository centroRepository;
    private final EstudianteRepository estudianteRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor
     * @param key La clave de la conexión a usar para acceder a la base de datos.
     */
    public AppService(CentroRepository centroRepository, EstudianteRepository estudianteRepository, ApplicationEventPublisher eventPublisher) {
        this.centroRepository = centroRepository;
        this.estudianteRepository = estudianteRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Obtiene la lista de centros educativos desde la base de datos.
     * @return La lista solicitada
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    @Transactional(readOnly = true)
    public List<Centro> listarCentros() throws DataAccessException {
        List<Centro> centros = centroRepository.findAll();
        logger.trace("Se han recuperado {} centros educativos de la base de datos.", centros.size());
        return centros;
    }

    /**
     * Agrega un nuevo estudiante a la base de datos.
     * @param estudiante El estudiante a agregar.
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    @Transactional
    public void agregarEstudiante(Estudiante estudiante) throws DataAccessException {
        estudianteRepository.save(estudiante);
        eventPublisher.publishEvent(new LogTransactionalListener.AuditEvent(Level.DEBUG, "Agregado estudiante con ID=%d".formatted(estudiante.getId())));
    }

    /**
     * Modifica un centro educativo existente en la base de datos.
        });
    }

    /**
     * Modifica un centro educativo existente en la base de datos.
     * @param centro El centro con los datos modificados (el ID debe seguir siendo el mismo).
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    @Transactional
    public void modificarCentro(Centro centro) throws DataAccessException {
        centroRepository.save(centro);
        eventPublisher.publishEvent(new LogTransactionalListener.AuditEvent(Level.DEBUG, "Modificado centro con ID=%d".formatted(centro.getId())));
    }

    /**
     * Agrega un nuevo centro educativo a la base de datos.
     * @param centro El centro a agregar (su ID se asignará automáticamente al insertarlo).
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    @Transactional
    public void agregarCentro(Centro centro) throws DataAccessException {
        centroRepository.save(centro);
        eventPublisher.publishEvent(new LogTransactionalListener.AuditEvent(Level.DEBUG, "Agregado centro con ID=%d".formatted(centro.getId())));
    }

    /**
     * Elimina un centro educativo de la base de datos.
     * @param centro El centro a eliminar.
     * @throws DataAccessException Si ocurre un error al acceder a los datos.
     */
    @Transactional
    public void eliminarCentro(Centro centro) throws DataAccessException {
        centroRepository.delete(centro);
        eventPublisher.publishEvent(new LogTransactionalListener.AuditEvent(Level.DEBUG, "Eliminado centro con ID=%d".formatted(centro.getId())));
    }

    public void operacionMultiple() throws DataAccessException {
        Long idCentro = 11004866L;
        centroRepository.findById(idCentro).ifPresent(centroRepository::delete);
        eventPublisher.publishEvent(new LogTransactionalListener.AuditEvent(Level.DEBUG, "Eliminado centro con ID=%s".formatted(idCentro)));

        // Esto debe fallar.
        centroRepository.save(new Centro(11004866L, "IES Centro repetido", Titularidad.PUBLICA));   
    }
}
