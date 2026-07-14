package edu.acceso.test_dao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.acceso.test_dao.modelo.Centro;

@Repository
public interface CentroRepository extends JpaRepository<Centro, Long> {

    // TODO: Mirar que se pueden declarar métodos de consulta personalizados si es necesario, por ejemplo:
    // List<Centro> findByNombre(String nombre);
}
