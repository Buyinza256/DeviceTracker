import { useState } from "react";
import { api } from "../api/client";
import type { DeviceType } from "../types";

const DEVICE_TYPES: DeviceType[] = [
  "CPE",
  "ROUTER",
  "SWITCH",
  "ACCESS_POINT",
  "FIREWALL",
  "ONT",
  "OTHER",
];

interface Props {
  onRegistered: () => void;
}

export function RegisterDeviceForm({ onRegistered }: Props) {
  const [name, setName] = useState("");
  const [deviceType, setDeviceType] = useState<DeviceType>("ROUTER");
  const [hostname, setHostname] = useState("");
  const [location, setLocation] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.registerDevice({ name, deviceType, hostname, location: location || undefined });
      setName("");
      setHostname("");
      setLocation("");
      onRegistered();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to register device");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <h2>Register device</h2>
      <div className="form__row">
        <label>
          Name
          <input value={name} onChange={(e) => setName(e.target.value)} required placeholder="Edge Router 1" />
        </label>
        <label>
          Type
          <select value={deviceType} onChange={(e) => setDeviceType(e.target.value as DeviceType)}>
            {DEVICE_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </label>
      </div>
      <div className="form__row">
        <label>
          Hostname / IP
          <input value={hostname} onChange={(e) => setHostname(e.target.value)} required placeholder="10.0.0.1" />
        </label>
        <label>
          Location
          <input value={location} onChange={(e) => setLocation(e.target.value)} placeholder="Nairobi DC" />
        </label>
      </div>
      {error && <p className="error">{error}</p>}
      <button type="submit" disabled={submitting}>
        {submitting ? "Registering…" : "Register device"}
      </button>
    </form>
  );
}
