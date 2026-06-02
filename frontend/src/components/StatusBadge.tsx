import type { DeviceStatus } from "../types";

interface Props {
  status: DeviceStatus | null;
}

const LABELS: Record<DeviceStatus, string> = {
  ONLINE: "Online",
  OFFLINE: "Offline",
  DEGRADED: "Degraded",
};

export function StatusBadge({ status }: Props) {
  if (!status) {
    return <span className="badge badge--unknown">No reports</span>;
  }
  return <span className={`badge badge--${status.toLowerCase()}`}>{LABELS[status]}</span>;
}

export function StaleBadge({ stale }: { stale: boolean }) {
  return stale ? (
    <span className="badge badge--stale" title="No status report in the last 15 minutes">
      Stale
    </span>
  ) : (
    <span className="badge badge--fresh">Live</span>
  );
}
