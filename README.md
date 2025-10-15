# BioClock-in

Offline-first biometric attendance for tough environments (mines, plants, field teams). BioClock-in pairs rugged Android tablets + fingerprint sensors with a C# core backend and a modern web analytics dashboard.

---

## Contents

* [Core value](#core-value)
* [Features](#features)
* [Architecture](#architecture)
* [Tech stack](#tech-stack)
* [Monorepo layout](#monorepo-layout)
* [Prerequisites](#prerequisites)
* [Quick start](#quick-start)

  * [1) Backend (Laravel, SQL Server)](#1-backend-laravel-sql-server)
  * [2) Mobile app (Ionic/Angular/Capacitor)](#2-mobile-app-ionicangularcapacitor)
  * [3) Web dashboard (React)](#3-web-dashboard-react)
* [Environment variables](#environment-variables)
* [Fingerprint capture & matching](#fingerprint-capture--matching)
* [Offline-first & sync](#offline-first--sync)
* [Key data model concepts](#key-data-model-concepts)
* [API surface (high-level)](#api-surface-high-level)
* [Security](#security)
* [Testing](#testing)
* [Build & release](#build--release)
* [Troubleshooting](#troubleshooting)
* [Contributing](#contributing)
* [License](#license)

---

## Core value

* **Reliable clock-ins without stable internet**: fully functional offline; syncs cleanly later.
* **Trusted identity**: on-device fingerprint capture + matching for “who actually clocked”.
* **Operational clarity**: gang-unit awareness, acting-jobs, early-leaver flags, leave scheduling.
* **Actionable analytics**: a web dashboard for HR/ops to view attendance, exceptions, and trends.

---

## Features

* Biometric **clock-in/out** (device-resident matching; thresholded scores)
* **Offline mode** with resilient local caches and conflict-aware sync
* **Gang unit** context + **industry numbers** for miners/employees
* **Acting jobs** (temporary role assignments) and **early-leaver** tracking
* **Manual approval** flow (with fingerprint validation)
* **Leave** requests vs. **leave schedules**
* **Dashboard** KPIs, RAG tags, and exportable reports (PDF/CSV)
* Role-based access, audit logs, device registry

---

## Architecture

```
apps/
  backend/        # Laravel API (SQL Server)
  mobile/         # Ionic + Angular + Capacitor Android app
  dashboard/      # React (MUI) analytics UI
plugins/
  capacitor-fingerprinte/  # Native plugin wrapping Senter FingerprintE SDK
```

* **Backend** (Laravel + SQL Server): single source of truth; REST API; jobs for sync & reports
* **Mobile** (Capacitor): rugged Android tablet app; offline-first; fingerprint capture/compare
* **Dashboard** (React): web analytics & admin; consumes the same REST API

---

## Tech stack

* **Backend**: PHP 8.1+, Laravel 10, SQL Server (sqlsrv), Redis (queue/cache), Horizon (optional)
* **Mobile**: Ionic 7, Angular 16+, Capacitor 5, Android Studio (Java 8 / Gradle 4.6 target), Senter **FingerprintE** SDK via a Capacitor plugin
* **Dashboard**: React 18, MUI, Vite/CRA, MUI X DataGrid (optional)
* Infra options: Nginx/Apache (Linux/Windows IIS supported), Supervisor/pm2 for queues, S3-compatible storage for exports

---

## Monorepo layout

```
.
├─ apps/
│  ├─ backend/                # Laravel project
│  ├─ mobile/                 # Ionic Angular app
│  └─ dashboard/              # React app
└─ plugins/
   └─ capacitor-fingerprinte/ # Capacitor plugin (Android wrapper for FingerprintE)
```

---

## Prerequisites

* **Global**

  * Node 18+ and npm (or pnpm/yarn)
  * Git
* **Backend**

  * PHP 8.1+ with extensions: `mbstring`, `openssl`, `pdo_sqlsrv`, `bcmath`, `intl`, `gd`
  * Composer 2.x
  * SQL Server 2019+ (on-prem or Azure SQL); Microsoft ODBC driver 17/18
  * Redis (optional but recommended)
* **Mobile**

  * Android Studio (SDK 29+)
  * Java 8 (for Gradle 4.6 compatibility where required)
  * Capacitor CLI
  * Senter FingerprintE SDK files (AAR/JAR) and vendor instructions
* **Dashboard**

  * Modern browser; Node 18+

---

## Quick start

### 1) Backend (Laravel, SQL Server)

```bash
cd apps/backend
cp .env.example .env
composer install
php artisan key:generate
# Set DB_* for SQL Server in .env (see below)
php artisan migrate --seed
php artisan serve # http://127.0.0.1:8000
```

Queues (recommended):

```bash
php artisan queue:work
# or: php artisan horizon
```

Storage & links:

```bash
php artisan storage:link
```

### 2) Mobile app (Ionic/Angular/Capacitor)

```bash
cd apps/mobile
npm install
cp .env.example .env
# Set API base URL, feature flags, etc.
ionic serve  # web preview (no biometrics)

# Android build
ionic build
npx cap sync android
npx cap open android
# Build & run from Android Studio on a Senter (or similar) device
```

> Fingerprint capture/compare works on device builds. Web preview uses a **mock** implementation.

### 3) Web dashboard (React)

```bash
cd apps/dashboard
npm install
cp .env.example .env
# Set VITE_API_URL or REACT_APP_API_URL
npm run dev   # http://localhost:5173 or per config
npm run build # production build
```

---

## Environment variables

### Backend (`apps/backend/.env`)

```env
APP_NAME=BioClockIn
APP_ENV=local
APP_KEY=base64:...
APP_URL=http://127.0.0.1:8000
APP_TIMEZONE=Africa/Johannesburg

LOG_CHANNEL=stack

DB_CONNECTION=sqlsrv
DB_HOST=127.0.0.1
DB_PORT=1433
DB_DATABASE=bioclockin
DB_USERNAME=sa
DB_PASSWORD=YourStrong!Passw0rd

# CORS / API
API_RATE_LIMIT=120

# Queues / Cache
QUEUE_CONNECTION=redis
CACHE_DRIVER=redis
REDIS_CLIENT=phpredis
REDIS_HOST=127.0.0.1
REDIS_PASSWORD=null
REDIS_PORT=6379

# Auth / JWT (if used)
JWT_SECRET=...
```

### Mobile (`apps/mobile/.env`)

```env
NG_APP_API_BASE_URL=http://127.0.0.1:8000/api
NG_APP_OFFLINE_ENABLED=true
NG_APP_MATCH_THRESHOLD=65
```

### Dashboard (`apps/dashboard/.env`)

```env
VITE_API_URL=http://127.0.0.1:8000/api
# or REACT_APP_API_URL for CRA setups
```

---

## Fingerprint capture & matching

* **Device**: Senter-class rugged tablets (e.g., S917) using **FingerprintE** SDK
* **Plugin**: `plugins/capacitor-fingerprinte`

  * Exposes:

    * `openDevice() / closeDevice()`
    * `captureFingerImage(): { captured: boolean; data: base64 }`
    * `compareFingerprints({ fingerPrints: string[] }): { success: boolean; score: number }`
* **Flow**:

  1. Open device
  2. Capture live finger image → convert to template
  3. Compare against enrolled templates (employee’s stored prints)
  4. Accept if `score >= MATCH_THRESHOLD`
* **Mocks**:

  * Web/dev mode uses `fingerprinteMock` with deterministic scores to unblock UI work
* **Best practices**:

  * Show “place finger then tap” instruction
  * After **3 failed** attempts, return to manual approval (counter resets on navigate)
  * Log scores for audit (not templates), redact PII

---

## Offline-first & sync

* **Local caches** (mobile):

  * Employees, gang units, acting-jobs, leave schedules
  * Pending clockings:
    Key pattern: `offline_clockings:<gangId>:<YYYY-MM-DD>`
* **Sync strategy**:

  * Write-behind queue while offline
  * On reconnect, flush pending clockings in FIFO order
  * Server resolves conflicts by timestamp + idempotency keys
* **UI states**:

  * “Offline banner”, “Syncing…”, “X items queued”, “Mismatch/Manual approval”

---

## Key data model concepts

* `employees` (industry number, first/last name, gang_unit_id, is_enrolled)
* `gang_units` (code, name)
* `clockings` (employee_id, direction in/out, timestamp, device_id, match_score)
* `acting_jobs` (employee_id, acting_occupation_id, start_date, end_date)
* `leave_requests` vs `leave_schedules`
* Derived responses:

  * **TinyEmployee** projection for mobile lists
  * RAG tags, early-leaver flags, last-7-day calendar
* Typical backend tables also include audit trails & device registry.

---

## API surface (high-level)

* `POST /auth/login` / `POST /auth/refresh`
* `GET /employees?gang_unit_id=...&q=...`
* `GET /employees/{id}` / `GET /employees/{id}/fingerprints`
* `POST /clockings` (creates, offline idempotency supported)
* `GET /gang-units` / `GET /acting-jobs?employee_id=...`
* `GET /reports/attendance?from=...&to=...`
* `GET /dashboard/metrics` (for charts/cards)

> See `apps/backend/routes/api.php` and controllers for the definitive list.

---

## Security

* HTTPS only in production (pin certs on devices where possible)
* Signed JWT access tokens (short TTL) + refresh tokens
* Role-based authorization on sensitive routes
* PII minimization: never log fingerprint templates; store encrypted at rest
* Device registration & revocation

---

## Testing

**Backend**

```bash
cd apps/backend
php artisan test
# or: ./vendor/bin/pest
```

**Mobile**

* Unit tests: Angular/Jest
* Device tests: run on physical Senter hardware to validate capture/compare

**Dashboard**

```bash
cd apps/dashboard
npm test
```

---

## Build & release

**Backend**

* Tag & deploy via your CI/CD (GitHub Actions, etc.)
* Run `php artisan migrate --force` and queue workers

**Mobile (Android)**

```bash
cd apps/mobile
ionic build
npx cap sync android
# In Android Studio:
# - Set min/target SDK as per device
# - Ensure Java 8 / Gradle 4.6 compatibility if required by vendor SDK
# Generate signed APK/AAB for distribution
```

**Dashboard**

```bash
cd apps/dashboard
npm run build
# Deploy /dist to your hosting (S3+CloudFront, Nginx, etc.)
```

---

## Troubleshooting

* **Android resource linking failed** (missing string):

  * Define missing resource in `apps/mobile/android/app/src/main/res/values/strings.xml`
    e.g., `<string name="capture_fingerprint">Capture Fingerprint</string>`
* **Gradle errors on older stacks**:

  * Stick to Java 8 and Gradle 4.6 if vendor SDK mandates it
  * Clear caches: Android Studio → Invalidate Caches / Restart
* **SSL: unable to find valid certification path**:

  * Install proper CA chain on device/server; for dev, use a trusted tunnel (e.g., `mkcert` locally)
* **SQL Server login/ODBC errors**:

  * Verify ODBC 17/18 installed
  * Check `.env` `DB_HOST`, `DB_PORT`, and SQL auth enabled
* **Three-fail loop on fingerprint**:

  * Ensure failure counter resets **after** navigation; guard against double-navigate

---

## Contributing

1. Fork & create a feature branch
2. Write clean, typed code (Angular/TS, React/TS), PSR-12 for PHP
3. Add/update tests where sensible
4. Open a PR with screenshots (UI) and notes (API changes, migrations)

---

## License

Proprietary. All rights reserved.
For evaluation or commercial licensing, contact the maintainers.

---

### Maintainers

* BioClock-in Team — Ops & Engineering

> Tip: if you’re deploying in South Africa, set `APP_TIMEZONE=Africa/Johannesburg` across services to keep timestamps consistent.
