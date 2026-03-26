package com.fraude.transaccion.service;

import com.fraude.transaccion.model.Transaccion;
import org.springframework.stereotype.Service;

@Service
public class FraudeService {

    public Integer evaluarFraude(Transaccion transaccion) {
        if (transaccion.getMonto() > 5000) {
            return 4;
        } else {
            return 5;
        }
    }
}
