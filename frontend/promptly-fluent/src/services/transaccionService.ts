// src/services/transaccionService.ts

const API_URL = "http://localhost:8080/api/transacciones";

export interface Transaccion {
  id?: number;
  monto: number;
  cuentaOrigenId: string;
  cuentaDestinoId: string;
  estadoId?: number;
  tipoTransaccionId?: number;
  fechaCreacion?: string;
}

export interface TransaccionResponse extends Transaccion {
  id: number;
  estadoId: number;
  estado?: string;
}

export const crearTransaccion = async (transaccion: Transaccion): Promise<TransaccionResponse> => {
  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(transaccion),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || "Error al crear transacción");
    }

    return await response.json();
  } catch (error) {
    throw error;
  }
};

export const obtenerHistorial = async (numeroCuenta: string): Promise<TransaccionResponse[]> => {
  try {
    const response = await fetch(`${API_URL}/cuenta/${numeroCuenta}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Error al obtener historial");
    }

    return await response.json();
  } catch (error) {
    throw error;
  }
};

