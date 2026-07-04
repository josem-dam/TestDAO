package edu.acceso.test_dao.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Modela un estudiante.
 */
@Entity
public class Estudiante {

    /**
     * Identificador del estudiante.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Nombre completo del estudiante.
     */
    @Column(nullable = false)
    private String nombre;
    /**
     * Fecha de nacimiento del estudiante.
     */
    @Column(nullable = false)
    private LocalDate nacimiento;

    /**
     * Centro al que está adscrito.
     */
    @ManyToOne
    @JoinColumn(name = "centro", nullable = true)
    private Centro centro;

    public Estudiante() {
        super();
    }

    /**
     * Carga los datos del estudiante.
     * @param id El identificador del estudiante.
     * @param nombre El nombre del estudiante.
     * @param nacimiento La fecha de nacimiento.
     * @param centro El centro al que está adscrito.
     * @return El propio objeto.
     */
    public Estudiante cargarDatos(Long id, String nombre, LocalDate nacimiento, Centro centro) {
        setId(id);
        setNombre(nombre);
        setNacimiento(nacimiento);
        setCentro(centro);

        return this;
    }

    /**
     * Constructor que carga todos los datos.
     * @param id El identificador del estudiante.
     * @param nombre El nombre del estudiante.
     * @param nacimiento La fecha de nacimiento.
     * @param centro El centro al que está adscrito.
     */
    public Estudiante(Long id, String nombre, LocalDate nacimiento, Centro centro) {
        this.cargarDatos(id, nombre, nacimiento, centro);
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
    public LocalDate getNacimiento() {
        return nacimiento;
    }
    public void setNacimiento(LocalDate nacimiento) {
        this.nacimiento = nacimiento;
    }
    public Centro getCentro() {
        return centro;
    }
    public void setCentro(Centro centro) {
        this.centro = centro;
    }
    
    @Override
    public String toString() {
        LocalDate hoy = LocalDate.now();
        return String.format("%s (%d años)", getNombre(), ChronoUnit.YEARS.between(getNacimiento(), hoy));
    }
}
