package com.fraude.transaccion.controller;

import com.fraude.transaccion.model.Transaccion;
import com.fraude.transaccion.service.TransaccionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService service;

    public TransaccionController(TransaccionService service) {
        this.service = service;
    }

    @PostMapping
    public Transaccion procesarTransaccion(@RequestBody Transaccion transaccion) {
        return service.procesarTransaccion(transaccion);
    }

    @GetMapping("/cuenta/{numeroCuenta}")
    public List<Transaccion> obtenerHistorial(@PathVariable String numeroCuenta) {
        return service.obtenerHistorial(numeroCuenta);
    }
}