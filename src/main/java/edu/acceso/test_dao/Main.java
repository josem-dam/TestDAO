package edu.acceso.test_dao;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.test_dao.backend.Conexion;
import edu.acceso.test_dao.backend.dao.CentroSqlDao;
import edu.acceso.test_dao.backend.dao.Crud;
import edu.acceso.test_dao.backend.dao.EstudianteSqlDao;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Estudiante;
import edu.acceso.test_dao.modelo.Centro.Titularidad;

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

        // Definimos los objetos DAO de acceso a datos persistentes.
        Crud<Centro> centroDao = new CentroSqlDao(KEY);
        Crud<Estudiante> estudianteDao = new EstudianteSqlDao(KEY);

        System.out.println("=== Centros iniciales ===");
        try {
            conexion.transaction(ctxt -> centroDao.get().forEach(System.out::println));
        } catch(DataAccessException e) {
            System.err.printf("ERROR INESPERADO al listar centros: %s.\n", e.getMessage());
        }

        try {
            conexion.transaction(ctxt -> {
                Centro castillo = centroDao.get(11004866L).orElse(null);
                Centro arroyo = centroDao.get(11007533L).orElse(null);

                Estudiante[] estudiantes = {
                    new Estudiante(null, "Perico de los Palotes", LocalDate.parse("08/10/2005", df), castillo),
                    new Estudiante(null, "Mariquella de La O", LocalDate.parse("04/05/2003", df), castillo),
                    new Estudiante(null, "Pústula von der Brujen", LocalDate.parse("12/03/2007", df), arroyo)
                };

                estudianteDao.insert(estudiantes);

                System.out.println("\n=== Estudiantes agregados ===");
                for(Estudiante e: estudiantes) {
                    System.out.printf("%d: %s\n", e.getId(), e);
                }
            });
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al agregar estudiantes: %s.\n", e.getMessage());
        }

        try {
            conexion.transaction(ctxt -> {
                Centro castillo = centroDao.get(11004866L).orElse(null);
                System.out.println("\n=== Modificación de un centro ===");
                castillo.setNombre("I.E.S. Castillo de Luna");
                centroDao.update(castillo);
                castillo = centroDao.get(castillo.getId()).orElse(null);
                System.out.println(castillo);
            });
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al modificar un centro: %s.\n", e.getMessage());
        }

        final Centro astaroth;
        try {
            astaroth = conexion.transactionR(ctxt -> {
                Centro centro = new Centro(11701164L, "IES Astaroth", Titularidad.PUBLICA);
                centroDao.insert(centro);
                return centro;
            });
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al agregar el centro Astaroth: %s.\n", e.getMessage());
            return;
        }

        try {
            conexion.transaction(ctxt -> {
                System.out.println("\n=== Listado de centros ===");
                centroDao.get().forEach(System.out::println);
            });
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al listar centros: %s.\n", e.getMessage());
        }

        try {
            conexion.transaction(ctxt -> {
                centroDao.delete(astaroth);
                // Esta inserción falla porque el código ya existe.
                centroDao.insert(new Centro(11004866L, "IES Centro repetido", Titularidad.PUBLICA));
            });
        } catch(DataAccessException e) {
            System.err.printf("\nERROR ESPERADO: %s.\n", e.getMessage());
        }

        try {
            System.out.println("\n=== Listado de centros (no desaparece Astaroth) ===");
            conexion.transaction(ctxt -> centroDao.get().forEach(System.out::println));
        } catch(DataAccessException e) {
            System.err.printf("\nERROR INESPERADO al listar centros: %s.\n", e.getMessage());
        }
    }
}