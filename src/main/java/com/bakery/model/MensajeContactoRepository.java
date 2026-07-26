package com.bakery.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {

    // Todos los mensajes, más recientes primero
    List<MensajeContacto> findAllByOrderByFechaDesc();

    // Contar mensajes no leídos (para el badge del sidebar)
    long countByLeidoFalse();
}
