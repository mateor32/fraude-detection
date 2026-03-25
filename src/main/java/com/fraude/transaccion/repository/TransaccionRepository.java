package com.fraude.transaccion.repository;
import java.util.List;
import com.fraude.transaccion.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {

    List<Transaccion> findByCuentaOrigenId(String cuentaOrigenId);
    List<Transaccion> findByCuentaDestinoId(String cuentaDestinoId);
}