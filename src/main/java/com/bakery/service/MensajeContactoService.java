package com.bakery.service;

import com.bakery.model.MensajeContacto;
import com.bakery.model.MensajeContactoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MensajeContactoService {

    @Autowired
    private MensajeContactoRepository repo;

    @Autowired
    private FiltroPalabrasService filtro;

    @Autowired
    private SpamDetectionService spamDetection;

    // Guarda el mensaje aplicando el filtro de groserías y el filtro anti-spam.
    // Si se detecta spam (texto sin sentido, email desechable o dominio listado
    // en Spamhaus), lanza MensajeSpamException y NO se guarda nada.
    public MensajeContacto guardar(String nombre, String email, String telefono, String asunto, String mensaje) {
        if (spamDetection.esTextoSinSentido(nombre, asunto, mensaje)) {
            throw new MensajeSpamException("texto sin sentido");
        }
        if (spamDetection.esEmailDesechable(email)) {
            throw new MensajeSpamException("email desechable");
        }
        if (spamDetection.estaEnListaSpamhaus(email)) {
            throw new MensajeSpamException("dominio listado en Spamhaus");
        }

        MensajeContacto m = new MensajeContacto(
            filtro.censurar(nombre),
            email,
            telefono,
            filtro.censurar(asunto),
            filtro.censurar(mensaje)
        );
        return repo.save(m);
    }

    public List<MensajeContacto> obtenerTodos() {
        return repo.findAllByOrderByFechaDesc();
    }

    public Optional<MensajeContacto> obtenerPorId(Long id) {
        return repo.findById(id);
    }

    public long contarNoLeidos() {
        return repo.countByLeidoFalse();
    }

    public void marcarLeido(Long id, boolean leido) {
        repo.findById(id).ifPresent(m -> {
            m.setLeido(leido);
            repo.save(m);
        });
    }

    // Marcar varios como leídos
    public void marcarLeidosEnLote(List<Long> ids) {
        repo.findAllById(ids).forEach(m -> {
            m.setLeido(true);
            repo.save(m);
        });
    }

    // Eliminar varios
    public void eliminarEnLote(List<Long> ids) {
        repo.deleteAllById(ids);
    }

    // Eliminar todos
    public void eliminarTodos() {
        repo.deleteAll();
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
