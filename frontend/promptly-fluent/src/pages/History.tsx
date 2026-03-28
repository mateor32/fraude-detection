import { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { StatusBadge } from "@/components/StatusBadge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { obtenerHistorial, TransaccionResponse } from "@/services/transaccionService";
import { toast } from "sonner";

const HistoryPage = () => {
  const { user } = useAuth();
  const [transactions, setTransactions] = useState<TransaccionResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const cargarHistorial = async () => {
      if (!user) return;
      try {
        setLoading(true);
        const historial = await obtenerHistorial(user.numeroCuenta);
        setTransactions(historial);
      } catch (error) {
        toast.error("Error al cargar el historial");
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    cargarHistorial();
  }, [user]);

  if (!user) return null;

  const mapearEstado = (estadoId?: number): string => {
    const estadoMap: { [key: number]: string } = {
      4: "SOSPECHOSA",
      5: "APROBADA",
      6: "RECHAZADA",
    };
    return estadoMap[estadoId || 0] || "DESCONOCIDO";
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Historial de Transferencias</h1>
        <p className="text-muted-foreground text-sm mt-1">{transactions.length} transacciones encontradas</p>
      </div>

      <div className="bg-card rounded-2xl shadow-card overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-muted-foreground">Cargando historial...</div>
        ) : transactions.length === 0 ? (
          <div className="p-8 text-center text-muted-foreground">No hay transacciones</div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow className="border-border hover:bg-transparent">
                <TableHead className="text-muted-foreground font-semibold text-xs uppercase tracking-wider">Fecha</TableHead>
                <TableHead className="text-muted-foreground font-semibold text-xs uppercase tracking-wider">De / Para</TableHead>
                <TableHead className="text-muted-foreground font-semibold text-xs uppercase tracking-wider text-right">Monto</TableHead>
                <TableHead className="text-muted-foreground font-semibold text-xs uppercase tracking-wider text-center">Estado</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {transactions.map((txn) => (
                <TableRow key={txn.id} className="border-border">
                  <TableCell className="text-sm text-foreground">
                    {txn.fechaCreacion ? new Date(txn.fechaCreacion).toLocaleDateString("es-ES") : "-"}
                  </TableCell>
                  <TableCell className="text-sm text-foreground">
                    {txn.cuentaOrigenId === user.numeroCuenta ? `→ ${txn.cuentaDestinoId}` : `← ${txn.cuentaOrigenId}`}
                  </TableCell>
                  <TableCell className="text-sm font-bold text-foreground text-right">
                    ${txn.monto.toLocaleString("es-MX", { minimumFractionDigits: 2 })}
                  </TableCell>
                  <TableCell className="text-center">
                    <StatusBadge status={mapearEstado(txn.estadoId)} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </div>
    </div>
  );
};

export default HistoryPage;
