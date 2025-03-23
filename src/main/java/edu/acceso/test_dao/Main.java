package edu.acceso.test_dao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import edu.acceso.test_dao.backend.Crud;
import edu.acceso.test_dao.backend.sql.CentroSqlDao;
import edu.acceso.test_dao.backend.sql.ConnectionPool;
import edu.acceso.test_dao.backend.sql.EstudianteSqlDao;
import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Estudiante;
import edu.acceso.test_dao.modelo.Centro.Titularidad;

public class Main {

    private static final String db = "jdbc:sqlite:file::memory:?cache=shared";
    private static final Path esquema = Path.of(System.getProperty("user.dir"), "src", "main", "resources", "esquema.sql");
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) throws Exception {
        try(ConnectionPool cp = ConnectionPool.getInstance(db)) {
            // Volcamos el esquema completo (es posible porque usamos sqlite)
            try(
                Connection conn = cp.getConnection();
                Statement stmt = conn.createStatement()
            ) {
                String sqlString = Files.readString(esquema);
                stmt.executeUpdate(sqlString);
            }

            // Definimos los objetos DAO de acceso a datos persistentes.
            // (Con estos objetos cada operación es una transacción)
            Crud<Centro> centroDao = new CentroSqlDao(cp.getDataSource());
            Crud<Estudiante> estudianteDao = new EstudianteSqlDao(cp.getDataSource());

            System.out.println("=== Centros iniciales ===");
            centroDao.get().forEach(System.out::println);

            // Obtenemos uno de los centros.
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

            System.out.println("\n=== Modificación de un centro ===");
            castillo.setNombre("I.E.S. Castillo de Luna");
            centroDao.update(castillo);
            castillo = centroDao.get(castillo.getId()).orElse(null);
            System.out.println(castillo);

            Centro astaroth = new Centro(11701164L, "IES Astaroth", Titularidad.PUBLICA);
            centroDao.insert(astaroth);            


            System.out.println("\n=== Listado de centros ===");
            centroDao.get().forEach(System.out::println);

        }
    }
}