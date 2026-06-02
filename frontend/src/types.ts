export type DeviceType =
  | "CPE"
  | "ROUTER"
  | "SWITCH"
  | "ACCESS_POINT"
  | "FIREWALL"
  | "ONT"
  | "OTHER";

export type DeviceStatus = "ONLINE" | "OFFLINE" | "DEGRADED";

export interface Device {
  id: string;
  name: string;
  deviceType: DeviceType;
  hostname: string;
  location: string | null;
  registeredAt: string;
  currentStatus: DeviceStatus | null;
  lastReportAt: string | null;
  stale: boolean;
}

export interface StatusReport {
  id: number;
  status: DeviceStatus;
  message: string | null;
  reportedAt: string;
}

export interface DeviceDetail extends Device {
  recentReports: StatusReport[];
}

/** The standard envelope every endpoint returns. */
export interface GenericResponse<T> {
  returnCode: number;
  returnMessage: string;
  returnObject: T;
}

export interface RegisterDevicePayload {
  name: string;
  deviceType: DeviceType;
  hostname: string;
  location?: string;
}

export interface SubmitReportPayload {
  status: DeviceStatus;
  message?: string;
}
