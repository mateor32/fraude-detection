package com.fraude.transaccion.controller;

import com.fraude.transaccion.model.Transaccion;
import com.fraude.transaccion.service.TransaccionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    private final TransaccionService service;

    public TransaccionController(TransaccionService service) {
        this.service = service;
    }

    @PostMapping("/transferir")
    public Transaccion transferir(@RequestParam String origen,
                                  @RequestParam String destino,
                                  @RequestParam BigDecimal monto) {
        return service.transferir(origen, destino, monto);
    }

    @GetMapping("/cuenta/{numeroCuenta}")
    public List<Transaccion> obtenerHistorial(@PathVariable String numeroCuenta) {
        return service.obtenerHistorial(numeroCuenta);
    }
}