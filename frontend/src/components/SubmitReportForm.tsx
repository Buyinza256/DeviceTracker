import { useState } from "react";
import { api } from "../api/client";
import type { DeviceStatus } from "../types";

const STATUSES: DeviceStatus[] = ["ONLINE", "OFFLINE", "DEGRADED"];

interface Props {
  deviceId: string;
  onSubmitted: () => void;
}

export function SubmitReportForm({ deviceId, onSubmitted }: Props) {
  const [status, setStatus] = useState<DeviceStatus>("ONLINE");
  const [message, setMessage] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.submitReport(deviceId, { status, message: message || undefined });
      setMessage("");
      onSubmitted();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to submit report");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <h2>Submit status report</h2>
      <div className="form__row">
        <label>
          Status
          <select value={status} onChange={(e) => setStatus(e.target.value as DeviceStatus)}>
            {STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </label>
        <label className="grow">
          Message (optional)
          <input value={message} onChange={(e) => setMessage(e.target.value)} placeholder="CPU 92%, link flapping" />
        </label>
      </div>
      {error && <p className="error">{error}</p>}
      <button type="submit" disabled={submitting}>
        {submitting ? "Submitting…" : "Submit report"}
      </button>
    </form>
  );
}
