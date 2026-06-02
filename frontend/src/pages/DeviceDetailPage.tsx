import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api/client";
import type { DeviceDetail } from "../types";
import { StaleBadge, StatusBadge } from "../components/StatusBadge";
import { SubmitReportForm } from "../components/SubmitReportForm";
import { formatDateTime, timeAgo } from "../format";

export function DeviceDetailPage() {
  const { deviceId } = useParams<{ deviceId: string }>();
  const [device, setDevice] = useState<DeviceDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!deviceId) return;
    try {
      setError(null);
      setDevice(await api.getDevice(deviceId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load device");
    } finally {
      setLoading(false);
    }
  }, [deviceId]);

  useEffect(() => {
    load();
  }, [load]);

  if (loading) return <p>Loading…</p>;
  if (error) return <p className="error">{error}</p>;
  if (!device) return <p>Device not found.</p>;

  return (
    <div className="stack">
      <p>
        <Link to="/">← Back to devices</Link>
      </p>

      <section className="card">
        <div className="card__header">
          <h2>{device.name}</h2>
          <div className="badge-group">
            <StatusBadge status={device.currentStatus} />
            <StaleBadge stale={device.stale} />
          </div>
        </div>
        <dl className="details">
          <div>
            <dt>Type</dt>
            <dd>{device.deviceType}</dd>
          </div>
          <div>
            <dt>Hostname / IP</dt>
            <dd>{device.hostname}</dd>
          </div>
          <div>
            <dt>Location</dt>
            <dd>{device.location ?? "—"}</dd>
          </div>
          <div>
            <dt>Registered</dt>
            <dd>{formatDateTime(device.registeredAt)}</dd>
          </div>
          <div>
            <dt>Last report</dt>
            <dd title={formatDateTime(device.lastReportAt)}>{timeAgo(device.lastReportAt)}</dd>
          </div>
        </dl>
      </section>

      <SubmitReportForm deviceId={device.id} onSubmitted={load} />

      <section className="card">
        <h2>Recent reports (latest {device.recentReports.length})</h2>
        {device.recentReports.length === 0 ? (
          <p className="muted">This device has not submitted any status reports yet.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Status</th>
                <th>Message</th>
                <th>Reported at</th>
              </tr>
            </thead>
            <tbody>
              {device.recentReports.map((r) => (
                <tr key={r.id}>
                  <td>
                    <StatusBadge status={r.status} />
                  </td>
                  <td>{r.message ?? "—"}</td>
                  <td title={timeAgo(r.reportedAt)}>{formatDateTime(r.reportedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
