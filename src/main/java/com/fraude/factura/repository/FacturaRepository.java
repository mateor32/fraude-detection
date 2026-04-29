package com.fraude.factura.repository;

import com.fraude.factura.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {
    List<Factura> findByNumDocumentoOrderByFechaVencimientoDesc(String numDocumento);

    List<Factura> findByNumDocumentoAndEstado(String numDocumento, String estado);
}
