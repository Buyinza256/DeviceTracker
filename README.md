# BCS Network Device Monitoring Service

A small service that manages network infrastructure assets (CPEs, routers, switches,
access points, firewalls, ONTs, …) and tracks their operational status. Devices are
registered, periodically submit status reports, and operators can see each device's
current status, last report time and whether it has gone **stale**.

- **Backend** — Java 17, Spring Boot 3.3, Spring Data JPA, PostgreSQL, Liquibase
- **Frontend** — React 18 + TypeScript (Vite)

---

## Domain model

| Concept | Notes |
|---|---|
| **Device** | A network asset. Has a name, type, hostname/IP, optional location and a registration timestamp. Identified by a UUID. |
| **Status report** | An append-only operational reading submitted by a device: `ONLINE` / `OFFLINE` / `DEGRADED`, an optional message, and a `reportedAt` timestamp. |
| **Current status** | Derived from a device's most recent status report. |
| **Stale** | A device is stale if it has **not** submitted a report within the last **15 minutes** (configurable). A device that has never reported is stale by definition. |

### Required capabilities (all implemented)

1. **Register a new device** — `POST /api/devices`
2. **Submit a status report** — `POST /api/devices/{id}/status-reports`
3. **List all registered devices** — `GET /api/devices` — each row includes current status, last report timestamp and a stale indicator
4. **View an individual device** — `GET /api/devices/{id}` — includes the **20 most recent** status reports

---

## API

All responses use a consistent envelope:

```json
{ "returnCode": 200, "returnMessage": "…", "returnObject": { } }
```

Errors use a separate shape (`{ timestamp, status, error, message, path }`) with the
matching HTTP status (`400` validation/bad request, `404` not found, `409` conflict).

### Register a device
```bash
curl -X POST http://localhost:8080/api/devices \
  -H 'Content-Type: application/json' \
  -d '{"name":"Edge Router 1","deviceType":"ROUTER","hostname":"10.0.0.1","location":"Nairobi DC"}'
```
`deviceType` ∈ `CPE, ROUTER, SWITCH, ACCESS_POINT, FIREWALL, ONT, OTHER`.
`name`, `deviceType` and `hostname` are required; `hostname` must be unique.

### Submit a status report
```bash
curl -X POST http://localhost:8080/api/devices/{id}/status-reports \
  -H 'Content-Type: application/json' \
  -d '{"status":"ONLINE","message":"healthy"}'
```
`status` ∈ `ONLINE, OFFLINE, DEGRADED`. `reportedAt` is optional (defaults to now;
cannot be in the future) so devices that buffer readings while offline can backfill them.

### List devices
```bash
curl http://localhost:8080/api/devices
```

### View a device with its recent reports
```bash
curl http://localhost:8080/api/devices/{id}
```

---

## Running it

### Option A — Docker (backend + database)

```bash
docker compose up --build
```

This starts PostgreSQL and the backend (on `http://localhost:8080`). Liquibase
creates the schema automatically on first boot.

Then run the frontend (see below). The Vite dev server proxies `/api` to the backend.

### Option B — run locally

**1. PostgreSQL** — create the database and user:

```sql
CREATE ROLE device_monitor LOGIN PASSWORD 'device_monitor';
CREATE DATABASE device_monitor OWNER device_monitor;
```

(Or just `docker compose up -d db` to get a ready-made Postgres on `localhost:5432`.)

**2. Backend**

```bash
cd backend
./mvnw spring-boot:run
```

Connection settings default to `localhost:5432/device_monitor` and can be overridden
with the `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and
`SPRING_DATASOURCE_PASSWORD` environment variables. The API serves on port `8080`.

**3. Frontend**

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

---

## Tests

```bash
cd backend
./mvnw test
```

The test suite runs against an in-memory **H2** database with the **same Liquibase
migrations** the application ships with, so it needs no external database. Coverage:

- **`DeviceServiceTest`** — registration, duplicate-hostname conflict, current-status
  derivation, the 15-minute staleness rule, the 20-report cap (newest first), unknown
  device and future-timestamp rejection.
- **`DeviceControllerTest`** — full HTTP round-trips for register → submit → list →
  view, plus validation (`400`) and not-found (`404`) responses.

---

## Database migrations

Schema is owned by **Liquibase** (`backend/src/main/resources/liquibase`), not by
Hibernate — the app runs with `ddl-auto=validate`, so the entities are checked against
the migrated schema at startup. Migrations are database-agnostic and run identically on
PostgreSQL (runtime) and H2 (tests).

- `devices` — id (UUID, PK), name, device_type, hostname (unique), location, registered_at
- `status_reports` — id (PK), device_id (FK → devices, `ON DELETE CASCADE`), status,
  message, reported_at; indexed by `(device_id, reported_at desc)` for the recent-reports
  and latest-per-device queries.

---

## Project layout

```
bcs-device-monitor/
├── docker-compose.yml          # PostgreSQL + backend
├── backend/                    # Spring Boot (Maven)
│   └── src/main/java/com/bcs/devicemonitor/
│       ├── common/             # GenericResponse, ErrorResponse, exception handling, CORS
│       └── modules/devices/    # controllers · services · repositories · entities · dtos · enums
└── frontend/                   # React + TypeScript (Vite)
    └── src/
        ├── api/                # typed API client
        ├── pages/              # device list + device detail
        └── components/         # forms, status/stale badges
```

The backend follows a modular, feature-first package layout (`modules/<feature>/…`) with
a shared `common` package — the same structure used across our other Spring Boot services.

---

## Assumptions & notes

- **Current status** is the status of the most recent report; a device with no reports
  has `currentStatus: null` and is reported as stale.
- **Staleness window** is 15 minutes per the brief, configurable via
  `monitoring.stale-after-minutes`. It is computed on read so it is always current
  without a background job.
- **Hostname uniqueness** is enforced both in the service and by a DB unique constraint;
  a duplicate returns `409 Conflict`.
- **`reportedAt`** may be supplied by the caller (to support devices that batch and
  forward readings) but is rejected if it is in the future.
- All timestamps are stored and returned in **UTC** (ISO-8601).
- Authentication/authorisation is intentionally out of scope for this exercise; the API
  is open, and CORS is restricted to the frontend dev origin.
- The "latest report per device" used by the list view is resolved in a single query to
  avoid an N+1 lookup across devices.
