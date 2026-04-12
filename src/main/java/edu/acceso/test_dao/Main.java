package edu.acceso.test_dao;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Estudiante;
import edu.acceso.test_dao.modelo.Centro.Titularidad;
import edu.acceso.test_dao.persistence.AppService;
import edu.acceso.test_dao.persistence.Conexion;
import edu.acceso.test_dao.persistence.dao.Crud;

public class Main {
    private static final String db = "jdbc:sqlite:file::memory:?cache=shared";
    private static final InputStream esquema = Main.class.getResourceAsStream("/esquema.sql");
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Level DEFAULT_LOG_LEVEL = Level.DEBUG;

    private static final String KEY = "DB";

    public static void main(String[] args) {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(DEFAULT_LOG_LEVEL);

        Conexion conexion = null;
        
        try {
            // Establecemos la conexión a la base de datos y la inicializamos con el esquema.
            conexion = Conexion.create(KEY, db, null, null)
                .initialize(esquema);
        }
        catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al crear la conexión: %s.\n", e.getMessage());
            return;
        }

        AppService service = new AppService(conexion);

        System.out.println("=== Centros iniciales ===");
        List<Centro> centros = null;
        try {
            centros = service.listarCentros();
            centros.forEach(System.out::println);
        } catch(DataAccessException e) {
            System.err.printf("ERROR INESPERADO al listar centros: %s.\n", e.getMessage());
            return;
        }

        Estudiante[] estudiantes = {
            new Estudiante(null, "Perico de los Palotes", LocalDate.parse("08/10/2005", df), centros.get(0)),
            new Estudiante(null, "Mariquella de La O", LocalDate.parse("04/05/2003", df), centros.get(0)),
            new Estudiante(null, "Pústula von der Brujen", LocalDate.parse("12/03/2007", df), centros.get(1))
        };

        try {
            for(Estudiante e: estudiantes) {
                service.agregarEstudiante(e);
            }

            System.out.println("\n=== Estudiantes agregados ===");
            for(Estudiante e: estudiantes) {
                System.out.printf("%d: %s\n", e.getId(), e);
            }
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al agregar estudiantes: %s.\n", e.getMessage());
        }

        try {
            Centro castillo = centros.get(0);
            castillo.setNombre("I.E.S. Castillo de Luna");
            service.modificarCentro(castillo);
            System.out.println(castillo);
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al modificar un centro: %s.\n", e.getMessage());
        }

        try {
            Centro astaroth = new Centro(11701164L, "IES Astaroth", Titularidad.PUBLICA);
            service.agregarCentro(astaroth);
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al agregar el centro Astaroth: %s.\n", e.getMessage());
            return;
        }

        try {
            System.out.println("\n=== Listado de centros ===");
            service.listarCentros().forEach(System.out::println);
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al listar centros: %s.\n", e.getMessage());
        }

        try {
            service.operacionMultiple();
        } catch(DataAccessException e) {
            System.err.printf("\nERROR ESPERADO: %s.\n", e.getMessage());
        }

        try {
            System.out.println("\n=== Listado de centros (no desaparece Astaroth) ===");
            service.listarCentros().forEach(System.out::println);
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al listar centros: %s.\n", e.getMessage());
        }

        conexion.close();
    }
}