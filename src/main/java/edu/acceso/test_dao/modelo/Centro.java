package edu.acceso.test_dao.modelo;

import java.util.Arrays;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

/**
 * Modela un centro de enseñanza.
 */
@Entity
public class Centro {

    /**
     * Enumeración de las titularidades de un centro.
     * <p>
     * Las titularidades pueden ser públicas o privadas.
     */
    public static enum Titularidad {
        /** Titularidad pública. */
        PUBLICA("público"),
        /** Titularidad privada. */
        PRIVADA("privado");

        /** Nombre de la titularidad. */
        private String desc;

        /**
         * Constructor de la titularidad.
         * @param desc Descripción de la titularidad.
         */
        Titularidad(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }

        /**
         * Obtiene la titularidad a partir de la descripción.
         * @param desc La descripción que quiere utilizarse para identificar la titularidad
         * @return El enum Titularidad o null, si no hay ninguno con esa descripción.
         */
        public static Titularidad fromString(String desc) {
            return Arrays.stream(Titularidad.values())
                .filter(t -> t.toString().compareToIgnoreCase(desc) == 0)
                .findFirst()
                .orElse(null);
        }
    }

    /**
     * Código identificativo del centro.
     */
    @Id
    private Long id;
    /**
     * Nombre del centro.
     */
    @Column(nullable = false, length = 100)
    private String nombre;
    /**
     * Titularidad: pública o privada.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Titularidad titularidad;

    /**
     * Constructor por defecto.
     * Inicializa el objeto sin datos.
     */
    public Centro() {
        super();
    }

    /**
     * Carga todos los datos en el objeto.
     * @param id Código del centro.
     * @param nombre Nombre del centro.
     * @param titularidad Titularidad del centro.
     * @return El propio objeto.
     */
    public Centro inicializar(Long id, String nombre, Titularidad titularidad) {
        setId(id);
        setNombre(nombre);
        setTitularidad(titularidad);
        return this;
    }

    /**
     * Constructor que admite todos los datos de definición del centro.
     * @param id Código del centro.
     * @param nombre Nombre del centro.
     * @param titularidad Titularidad del centro (pública o privada)
     */
    public Centro(Long id, String nombre, Titularidad titularidad) {
        inicializar(id, nombre, titularidad);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Titularidad getTitularidad() {
        return titularidad;
    }

    public void setTitularidad(Titularidad titularidad) {
        this.titularidad = titularidad;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getNombre(), getId());
    }
}