package edu.acceso.test_dao.backend.core;

/**
 * Interfaz que define una entidad con un identificador único.
 * Las entidades deben implementar este método para proporcionar
 * su identificador.
 */
public interface Entity {
    public Long getId();
    public void setId(Long id);
}