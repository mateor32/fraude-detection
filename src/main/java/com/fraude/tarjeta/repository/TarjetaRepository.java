package com.fraude.tarjeta.repository;

import com.fraude.tarjeta.model.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Integer> {
    List<Tarjeta> findByNumDocumentoAndEstadoId(String numDocumento, Integer estadoId);

    List<Tarjeta> findByNumDocumento(String numDocumento);

    List<Tarjeta> findByEstadoId(Integer estadoId);

    List<Tarjeta> findAllByOrderByFechaCreacionDesc();
}
