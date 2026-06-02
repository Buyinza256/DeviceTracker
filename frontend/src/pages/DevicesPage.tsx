import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import type { Device } from "../types";
import { StaleBadge, StatusBadge } from "../components/StatusBadge";
import { RegisterDeviceForm } from "../components/RegisterDeviceForm";
import { formatDateTime, timeAgo } from "../format";

export function DevicesPage() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setError(null);
      setDevices(await api.listDevices());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load devices");
    } finally {
      setLoading(false);
    }
  }, []);

  // Initial load plus a light poll so stale indicators stay current.
  useEffect(() => {
    load();
    const interval = setInterval(load, 15000);
    return () => clearInterval(interval);
  }, [load]);

  const staleCount = devices.filter((d) => d.stale).length;

  return (
    <div className="stack">
      <RegisterDeviceForm onRegistered={load} />

      <section className="card">
        <div className="card__header">
          <h2>Devices ({devices.length})</h2>
          {staleCount > 0 && <span className="badge badge--stale">{staleCount} stale</span>}
        </div>

        {loading && <p>Loading…</p>}
        {error && <p className="error">{error}</p>}

        {!loading && !error && devices.length === 0 && (
          <p className="muted">No devices registered yet. Register one above to get started.</p>
        )}

        {devices.length > 0 && (
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Type</th>
                <th>Hostname</th>
                <th>Location</th>
                <th>Status</th>
                <th>Last report</th>
                <th>Health</th>
              </tr>
            </thead>
            <tbody>
              {devices.map((d) => (
                <tr key={d.id} className={d.stale ? "row--stale" : undefined}>
                  <td>
                    <Link to={`/devices/${d.id}`}>{d.name}</Link>
                  </td>
                  <td>{d.deviceType}</td>
                  <td>{d.hostname}</td>
                  <td>{d.location ?? "—"}</td>
                  <td>
                    <StatusBadge status={d.currentStatus} />
                  </td>
                  <td title={formatDateTime(d.lastReportAt)}>{timeAgo(d.lastReportAt)}</td>
                  <td>
                    <StaleBadge stale={d.stale} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
