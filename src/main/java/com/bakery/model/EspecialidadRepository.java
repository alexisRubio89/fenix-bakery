package com.bakery.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    List<Especialidad> findAllByOrderByPosicionAsc();

    Optional<Especialidad> findByPosicion(Integer posicion);
}
