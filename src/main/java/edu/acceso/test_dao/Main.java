package edu.acceso.test_dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import edu.acceso.sqlutils.errors.DataAccessException;
import edu.acceso.test_dao.backend.Conexion;
import edu.acceso.test_dao.backend.Crud;
import edu.acceso.test_dao.backend.dao.CentroSqlDao;
import edu.acceso.test_dao.backend.dao.EstudianteSqlDao;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Estudiante;
import edu.acceso.test_dao.modelo.Centro.Titularidad;

public class Main {

    private static final String db = "jdbc:sqlite:file::memory:?cache=shared";
    private static final InputStream esquema = Main.class.getResourceAsStream("/esquema.sql");
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) throws DataAccessException {
        Conexion conexion = Conexion.create("DB", db, null, null);

        // Podemos usar directamente el objeto Connection
        conexion.transaction(conn -> {
            try(Statement stmt = conn.createStatement()) {
                // Volcamos el esquema completo (es posible porque usamos sqlite)
                // En sqlutils hay un método para hacer esto mismo de forma genérica.
                String sqlString = new String(esquema.readAllBytes());
                stmt.executeUpdate(sqlString);
            } catch(SQLException e) {
                throw new DataAccessException("Error al crear el esquema de la base de datos", e);
            } catch(IOException e) {
                throw new RuntimeException("Error al leer el esquema de la base de datos", e);
            }
        });

        // Definimos los objetos DAO de acceso a datos persistentes.
        Crud<Centro> centroDao = new CentroSqlDao("DB");
        Crud<Estudiante> estudianteDao = new EstudianteSqlDao("DB");

        System.out.println("=== Centros iniciales ===");
        conexion.transaction(conn -> centroDao.get().forEach(System.out::println));

        conexion.transaction(conn -> {
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

        conexion.transaction(conn -> {
            Centro castillo = centroDao.get(11004866L).orElse(null);
            System.out.println("\n=== Modificación de un centro ===");
            castillo.setNombre("I.E.S. Castillo de Luna");
            centroDao.update(castillo);
            castillo = centroDao.get(castillo.getId()).orElse(null);
            System.out.println(castillo);
        });

        // Transacción que devuelve un resultado (un centro).
        Centro astaroth = conexion.transactionR(conn -> {
            Centro centro = new Centro(11701164L, "IES Astaroth", Titularidad.PUBLICA);
            centroDao.insert(centro);
            return centro;
        });


        conexion.transaction(conn -> {
            System.out.println("\n=== Listado de centros ===");
            centroDao.get().forEach(System.out::println);
        });

        try {
            conexion.transaction(conn -> {
                centroDao.delete(astaroth);
                // Esta inserción falla.
                centroDao.insert(new Centro(11004866L, "IES Centro repetido", Titularidad.PUBLICA));
            });
        } catch(DataAccessException e) {
            System.err.printf("\nError: %s.\n", e.getMessage());
        }

        System.out.println("\n=== Listado de centros (no desaparece Astaroth) ===");
        conexion.transaction(conn -> centroDao.get().forEach(System.out::println));
    }
}