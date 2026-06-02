import type {
  Device,
  DeviceDetail,
  GenericResponse,
  RegisterDevicePayload,
  StatusReport,
  SubmitReportPayload,
} from "../types";

// In dev, requests go through the Vite proxy (relative URL). In production the
// base URL can be supplied at build time via VITE_API_BASE_URL.
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });

  const body = await response.json().catch(() => null);

  if (!response.ok) {
    const message = body?.message ?? body?.returnMessage ?? response.statusText;
    throw new Error(message);
  }

  return (body as GenericResponse<T>).returnObject;
}

export const api = {
  listDevices: () => request<Device[]>("/api/devices"),

  getDevice: (id: string) => request<DeviceDetail>(`/api/devices/${id}`),

  registerDevice: (payload: RegisterDevicePayload) =>
    request<Device>("/api/devices", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  submitReport: (deviceId: string, payload: SubmitReportPayload) =>
    request<StatusReport>(`/api/devices/${deviceId}/status-reports`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),
};
