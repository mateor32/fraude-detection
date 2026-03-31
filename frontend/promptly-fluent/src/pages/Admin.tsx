import { useEffect, useMemo, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { isAdminRole } from "@/lib/roles";
import {
  actualizarEstadoTransaccion,
  obtenerTodasTransacciones,
  obtenerTransaccionesPendientes,
  TransaccionResponse,
} from "@/services/transaccionService";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { toast } from "sonner";

const mapEstadoToStatus = (estadoId?: number): "approved" | "rejected" | "pending" => {
  if (estadoId === 5) return "approved";
  if (estadoId === 6) return "rejected";
  return "pending";
};

const AdminPage = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [pending, setPending] = useState<TransaccionResponse[]>([]);
  const [all, setAll] = useState<TransaccionResponse[]>([]);
  const [updatingId, setUpdatingId] = useState<number | null>(null);

  const adminDocumento = user?.numDocumento || "";

  const loadData = async () => {
	if (!adminDocumento) return;
	try {
	  setLoading(true);
	  const [pendientes, todas] = await Promise.all([
		obtenerTransaccionesPendientes(adminDocumento),
		obtenerTodasTransacciones(adminDocumento),
	  ]);
	  setPending(pendientes);
	  setAll(todas);
	} catch (error) {
	  const message = error instanceof Error ? error.message : "Error al cargar módulo de administración";
	  toast.error(message);
	} finally {
	  setLoading(false);
	}
  };

  useEffect(() => {
	if (!user || !isAdminRole(user.rol)) return;
	void loadData();
  }, [user]);

  const handleCambiarEstado = async (id: number, estadoId: 5 | 6) => {
	try {
	  setUpdatingId(id);
	  await actualizarEstadoTransaccion(id, estadoId, adminDocumento);
	  toast.success(estadoId === 5 ? "Transferencia aprobada" : "Transferencia rechazada");
	  await loadData();
	} catch (error) {
	  const message = error instanceof Error ? error.message : "No se pudo actualizar la transferencia";
	  toast.error(message);
	} finally {
	  setUpdatingId(null);
	}
  };

  const stats = useMemo(() => {
	const aprobadas = all.filter((t) => t.estadoId === 5).length;
	const rechazadas = all.filter((t) => t.estadoId === 6).length;
	return {
	  pendientes: pending.length,
	  aprobadas,
	  rechazadas,
	};
  }, [pending, all]);

  if (!user || !isAdminRole(user.rol)) {
	return <div className="text-sm text-muted-foreground">No autorizado.</div>;
  }

  return (
	<div className="space-y-6">
	  <div>
		<h1 className="text-2xl font-bold text-foreground">Administración de Transferencias</h1>
		<p className="text-sm text-muted-foreground mt-1">
		  Valida transferencias pendientes y revisa el historial completo.
		</p>
	  </div>

	  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
		<div className="rounded-xl bg-card border border-border p-4">
		  <p className="text-xs text-muted-foreground">Pendientes</p>
		  <p className="text-2xl font-bold">{stats.pendientes}</p>
		</div>
		<div className="rounded-xl bg-card border border-border p-4">
		  <p className="text-xs text-muted-foreground">Aprobadas</p>
		  <p className="text-2xl font-bold">{stats.aprobadas}</p>
		</div>
		<div className="rounded-xl bg-card border border-border p-4">
		  <p className="text-xs text-muted-foreground">Rechazadas</p>
		  <p className="text-2xl font-bold">{stats.rechazadas}</p>
		</div>
	  </div>

	  <section className="bg-card rounded-2xl border border-border overflow-hidden">
		<div className="px-5 py-4 border-b border-border flex items-center justify-between">
		  <h2 className="font-semibold">Pendientes de validación</h2>
		  <Button variant="outline" onClick={loadData} disabled={loading}>Actualizar</Button>
		</div>
		{loading ? (
		  <div className="p-8 text-center text-muted-foreground">Cargando pendientes...</div>
		) : pending.length === 0 ? (
		  <div className="p-8 text-center text-muted-foreground">No hay transferencias pendientes.</div>
		) : (
		  <Table>
			<TableHeader>
			  <TableRow>
				<TableHead>Fecha</TableHead>
				<TableHead>Origen</TableHead>
				<TableHead>Destino</TableHead>
				<TableHead className="text-right">Monto</TableHead>
				<TableHead className="text-center">Acciones</TableHead>
			  </TableRow>
			</TableHeader>
			<TableBody>
			  {pending.map((txn) => (
				<TableRow key={txn.id}>
				  <TableCell>{txn.fechaCreacion ? new Date(txn.fechaCreacion).toLocaleString("es-ES") : "-"}</TableCell>
				  <TableCell>{txn.cuentaOrigenId}</TableCell>
				  <TableCell>{txn.cuentaDestinoId}</TableCell>
				  <TableCell className="text-right font-semibold">
					${txn.monto.toLocaleString("es-MX", { minimumFractionDigits: 2 })}
				  </TableCell>
				  <TableCell className="text-center space-x-2">
					<Button
					  size="sm"
					  disabled={updatingId === txn.id}
					  onClick={() => handleCambiarEstado(txn.id, 5)}
					>
					  Aprobar
					</Button>
					<Button
					  size="sm"
					  variant="destructive"
					  disabled={updatingId === txn.id}
					  onClick={() => handleCambiarEstado(txn.id, 6)}
					>
					  Rechazar
					</Button>
				  </TableCell>
				</TableRow>
			  ))}
			</TableBody>
		  </Table>
		)}
	  </section>

	  <section className="bg-card rounded-2xl border border-border overflow-hidden">
		<div className="px-5 py-4 border-b border-border">
		  <h2 className="font-semibold">Todas las transferencias</h2>
		</div>
		<Table>
		  <TableHeader>
			<TableRow>
			  <TableHead>Fecha</TableHead>
			  <TableHead>Origen</TableHead>
			  <TableHead>Destino</TableHead>
			  <TableHead className="text-right">Monto</TableHead>
			  <TableHead className="text-center">Estado</TableHead>
			</TableRow>
		  </TableHeader>
		  <TableBody>
			{all.map((txn) => (
			  <TableRow key={`all-${txn.id}`}>
				<TableCell>{txn.fechaCreacion ? new Date(txn.fechaCreacion).toLocaleString("es-ES") : "-"}</TableCell>
				<TableCell>{txn.cuentaOrigenId}</TableCell>
				<TableCell>{txn.cuentaDestinoId}</TableCell>
				<TableCell className="text-right font-semibold">
				  ${txn.monto.toLocaleString("es-MX", { minimumFractionDigits: 2 })}
				</TableCell>
				<TableCell className="text-center">
				  <StatusBadge status={mapEstadoToStatus(txn.estadoId)} />
				</TableCell>
			  </TableRow>
			))}
		  </TableBody>
		</Table>
	  </section>
	</div>
  );
};

export default AdminPage;

