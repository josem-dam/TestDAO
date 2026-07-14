package edu.acceso.test_dao.persistence;

import java.util.Objects;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import edu.acceso.sqlutils.DbmsSelector;
import jakarta.persistence.EntityManagerFactory;

/**
 * Gestiona las conexiones a la base de datos.
 * Utiliza un patrón Multiton para manejar las conexiones basadas en claves únicas.
 * y utiliza de forma práctica {@link JdbcConnection} y {@link TransactionManager}.
 * La clase maneja con seguridad mútiples conexiones concurrentes, por lo que
 * resuelve un escenario bastante más amplio que el de este ejemplo.
 */
@Configuration
@ComponentScan(basePackages = "edu.acceso.test_dao.persistence")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "edu.acceso.test_dao.persistence.repository")
public class Conexion {

    private static final String PROVIDER_ADAPTER_CLASS = "provider.adapter.class";
    private static final String PROVIDER_DIALECT_KEY = "provider.dialect.key";
    private static final String PROVIDER_DIALECT_PREFIX = "provider.dialect";

    private final String dbUrl;
    private final String user;
    private final String password;
    private final Properties properties;

    private final String adapterClass;

    /**
     * Constructor privado.
     * @param dbUrl La URL de conexión a la base de datos. 
     * @param user El nombre de usuario para la base de datos.
     * @param password La contraseña para la base de datos.
     * @param provider El proveedor de JPA que da nombre al archivo de propiedades.
     */
    public Conexion(String dbUrl, String user, String password, String provider) {
        Objects.requireNonNull(dbUrl, "La URL de conexión no puede ser nula.");
        Objects.requireNonNull(provider, "El proveedor de JPA no puede ser nulo.");

        this.dbUrl = dbUrl;
        this.user = user;
        this.password = password;

        // Cargamos el archivo de propiedades de Hibernate.
        properties = new Properties();
        String fileName = "%s.properties".formatted(provider);
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el archivo de propiedades: " + fileName);
            }
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar el archivo de propiedades: " + fileName, e);
        }

        String dbms = DbmsSelector.fromUrl(dbUrl).toString().toLowerCase();

        adapterClass = properties.getProperty(PROVIDER_ADAPTER_CLASS);
        String dialectKey = properties.getProperty(PROVIDER_DIALECT_KEY);
        String sqlDialect = properties.getProperty("%s.%s".formatted(PROVIDER_DIALECT_PREFIX, dbms));

        Objects.requireNonNull(adapterClass, "No ha incluido en el archivo de propiedades la clase del adaptador JPA (%s).".formatted(PROVIDER_ADAPTER_CLASS));
        Objects.requireNonNull(dialectKey, "No ha incluido en el archivo de propiedades la clave del dialecto JPA (%s).".formatted(PROVIDER_DIALECT_KEY));
        Objects.requireNonNull(sqlDialect, "No ha incluido en el archivo de propiedades el dialecto JPA para el SGBD (%s.%s).".formatted(PROVIDER_DIALECT_PREFIX, dbms));

        // Eliminamos las propiedades que no configuran JPA.
        properties.remove(PROVIDER_ADAPTER_CLASS);
        properties.remove(PROVIDER_DIALECT_KEY);
        properties.keySet().removeIf(k -> ((String) k).startsWith(PROVIDER_DIALECT_PREFIX + "."));

        properties.put(dialectKey, sqlDialect);
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(dbUrl);
        if(user != null) config.setUsername(user);
        if(password != null) config.setPassword(password);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        // Paquete que contiene las clases del modelo
        em.setPackagesToScan("edu.acceso.test_dao.modelo");

        // Unidad de persistencia (contiene únicamente la configuración del proveedor)
        //em.setPersistenceUnitName(key);

        // Configuración del proveedor que depende del SGBD.
        em.setJpaProperties(properties);

        // Instanciamos dinámicamente el adaptador de proveedor de JPA para Spring.
        try {
            Class<?> vendorAdapterClass = Class.forName(adapterClass);
            em.setJpaVendorAdapter((JpaVendorAdapter) vendorAdapterClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Error al instanciar el proveedor JPA: " + adapterClass, e);
        }

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}