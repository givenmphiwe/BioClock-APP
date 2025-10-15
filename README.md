Gotcha — let’s tailor the README for a **.NET (C#) backend + native Android (Android Studio)** stack.

---

# BioClock-in (ASP.NET Core + Android)

Offline-first biometric attendance built for harsh environments (mines, plants, field teams).
Backend: **ASP.NET Core Web API** (SQL Server).
Mobile: **Native Android** (Java, Android Studio) with **Senter FingerprintE** SDK.

---

## Table of contents

* Core value
* Features
* Architecture & repo layout
* Tech stack
* Prerequisites
* Quick start

  * Backend (ASP.NET Core)
  * Android app (Android Studio)
* Configuration

  * Backend: `appsettings.json` / env
  * Android: `local.properties` / Gradle
* Fingerprint capture & matching
* Offline-first & sync
* Data model (high-level)
* API surface (high-level)
* Security
* Build & release
* Troubleshooting
* License

---

## Core value

* **Works offline** on rugged tablets, syncs later without data loss.
* **Biometric certainty**: fingerprint capture + on-device matching.
* **Operational context**: gang units, acting jobs, leave, early-leaver flags.
* **Actionable analytics** via reports & exports.

---

## Features

* Clock-in/out with fingerprint verification (thresholded matching score)
* Manual approval flow with fingerprint validation
* Offline queue of clockings + background sync on reconnect
* Gang-unit aware employee lookups (by Industry Number)
* Acting jobs (temporary roles), leave requests/schedules
* Exports (CSV/PDF) and admin/reporting endpoints
* Role-based auth (JWT Bearer)

---

## Architecture & repo layout

```
bioclockin/
├─ src/
│  └─ Api/                         # ASP.NET Core Web API
│     ├─ BioClockIn.API.csproj
│     ├─ Controllers/
│     ├─ Data/                     # EF Core DbContext, Migrations
│     ├─ Models/
│     ├─ Services/
│     └─ appsettings*.json
└─ android/
   └─ app/                         # Native Android app (Java)
      ├─ src/main/java/...
      ├─ src/main/res/...
      ├─ libs/                     # Vendor AAR/JAR (FingerprintE)
      └─ build.gradle
```

---

## Tech stack

**Backend**

* .NET **8** (LTS) / ASP.NET Core Web API
* SQL Server, EF Core
* JWT Bearer auth
* Serilog (logs), Swagger (dev)

**Android**

* Android Studio (Gradle **4.6** compatible setup when needed)
* **Java 8** (for older vendor SDKs)
* Senter **FingerprintE** SDK (AAR/JAR)
* SQLite (Room/ORM optional) for offline cache & queue

---

## Prerequisites

* **Backend**

  * .NET SDK 8.x
  * SQL Server 2019+ (or Azure SQL)
  * ODBC/SQL Server drivers on host
* **Android**

  * Android Studio (SDK 29+ device compatibility)
  * **Java 8** toolchain installed
  * Vendor SDK files for **FingerprintE** (AAR/JAR + docs)
  * Rugged device with fingerprint scanner (e.g., Senter S917)

> Timezone: project assumes **Africa/Johannesburg** for ops reporting.

---

## Quick start

### 1) Backend (ASP.NET Core)

```bash
cd src/Api
dotnet restore
# Set connection string in appsettings.Development.json (see below)
dotnet ef database update   # create schema
dotnet run                  # http://localhost:5089 (example)
```

Enable Swagger (dev): open `/swagger` while running locally.

### 2) Android app (Android Studio)

1. Open `android/` in Android Studio.
2. Copy vendor **FingerprintE** SDK AAR/JAR into `android/app/libs/`.
3. In `app/build.gradle`:

   * Add `implementation files('libs/fingerprinte.aar')` (or the actual name).
   * Ensure `compileOptions { sourceCompatibility 1.8; targetCompatibility 1.8 }`.
   * Stick with **Gradle 4.6** + Android plugin versions compatible with the SDK (if required).
4. Grant permissions in `AndroidManifest.xml`:

   ```xml
   <uses-permission android:name="android.permission.USE_BIOMETRIC"/>
   <uses-permission android:name="android.permission.USE_FINGERPRINT"/>
   <uses-feature android:name="android.hardware.usb.host" android:required="false"/>
   ```
5. Configure the API base URL (e.g., in a constants file or `BuildConfig`).
6. Build & run on a **physical** device (emulators won’t expose the fingerprint hardware).

---

## Configuration

### Backend – `appsettings.json`

```json
{
  "App": {
    "Timezone": "Africa/Johannesburg",
    "MatchThreshold": 65
  },
  "ConnectionStrings": {
    "Default": "Server=127.0.0.1,1433;Database=BioClockIn;User Id=sa;Password=YourStrong!Passw0rd;TrustServerCertificate=true;"
  },
  "Jwt": {
    "Issuer": "BioClockIn",
    "Audience": "BioClockInClients",
    "Key": "super-secret-key-change-me",
    "AccessTokenMinutes": 60
  },
  "Logging": { "LogLevel": { "Default": "Information" } },
  "AllowedHosts": "*"
}
```

Environment overrides (production examples):

```bash
# Windows (PowerShell) / Linux (export) style
setx ASPNETCORE_URLS "http://0.0.0.0:5089"
setx ConnectionStrings__Default "Server=...;Database=BioClockIn;..."
setx Jwt__Key "change-me"
```

### Android – Gradle & constants

* `local.properties` should point to your Android SDK.
* In code, define:

  ```java
  public final class ApiConfig {
      public static final String BASE_URL = "http://<server>:5089/api";
      public static final int MATCH_THRESHOLD = 65;
  }
  ```

---

## Fingerprint capture & matching

* **Device SDK**: Senter FingerprintE (via AAR/JAR).
* Typical Java calls:

  * `openDevice()` / `closeDevice()`
  * `captureFingerImage()` → template extraction
  * `compareFingerprints(List<String> storedTemplates)` → `{ success:Boolean, score:int }`
* **Flow**

  1. Open device.
  2. Prompt: *“Place finger, then tap Capture.”*
  3. Convert to template, compare against enrolled templates for the employee.
  4. Accept if `score >= MATCH_THRESHOLD`.
  5. Log **scores** and metadata; **never** log templates.

**UX rule**: after **3 failed** attempts, navigate back to Manual Approval; reset the failure counter on navigation.

---

## Offline-first & sync

* **Local cache**: employees, gang units, acting jobs, leave schedules.

* **Pending clockings**: stored in SQLite with idempotency keys:

  Key idea: `offline_clockings:<gangId>:<YYYY-MM-DD>`

* **Sync service**:

  * On connectivity regained, POST queued items in FIFO.
  * Backend resolves conflicts via timestamps + idempotency.

* **UI states**: Offline banner, “X items queued”, “Syncing…”, mismatch/approval prompts.

---

## Data model (high-level)

* **Employees**: `uuid`, `industry_number`, `first_name`, `last_name`, `gang_unit_id`, `is_enrolled`
* **GangUnits**: `uuid`, `name`, `code`
* **Clockings**: `employee_id`, `direction` (in/out), `timestamp`, `device_id`, `match_score`
* **ActingJobs**: `employee_id`, `acting_occupation_id`, `start_date`, `end_date`
* **Leave**: `leave_requests` vs `leave_schedules`
* **Projections**: `TinyEmployee` list for mobile (includes tags, RAG indicators)

---

## API surface (high-level)

```
POST   /api/auth/login
POST   /api/auth/refresh

GET    /api/employees?gang_unit_id=&q=
GET    /api/employees/{id}
GET    /api/employees/{id}/fingerprints

POST   /api/clockings
GET    /api/gang-units
GET    /api/acting-jobs?employee_id=
GET    /api/reports/attendance?from=&to=
GET    /api/dashboard/metrics
```

> See code in `src/Api/Controllers` for authoritative routes.

---

## Security

* HTTPS everywhere in production (valid CA chain).
* JWT Bearer auth (short-lived access tokens, refresh flow).
* Role-based authorization for sensitive endpoints.
* **PII & biometrics**:

  * Encrypt biometric data at rest.
  * Never store raw images in logs; store templates securely.
  * Redact all templates from telemetry.

---

## Build & release

**Backend**

```bash
cd src/Api
dotnet publish -c Release -o ./out
# Deploy ./out to your host; set env vars; run as service.
```

**Android**

* Vendor SDK present in `app/libs`.
* Generate signed APK/AAB from Android Studio.
* Target SDK/Min SDK per device constraints (USB host if needed).

---

## Troubleshooting

* **Android resource linking failed (missing string)**
  Add the missing key in `res/values/strings.xml` (e.g., `capture_fingerprint`).

* **Gradle / Java 8 compatibility**
  Vendor SDKs sometimes require **Java 8** + **Gradle 4.6**.
  Set:

  ```gradle
  compileOptions { sourceCompatibility JavaVersion.VERSION_1_8
                   targetCompatibility JavaVersion.VERSION_1_8 }
  ```

  If the Android Gradle Plugin is too new for the vendor SDK, use a compatible version.

* **SSL “unable to find valid certification path” on Android**
  Install full CA chain on server; avoid self-signed in prod.
  For dev, trust a local CA (e.g., mkcert) or use a trusted tunnel.

* **SQL Server / ODBC login failures**
  Confirm SQL Auth enabled; connection string correct; port 1433 open; driver installed.

* **Three-fail fingerprint loop**
  Only reset the failure counter **after** navigating away; guard against double-navigation.

---

## License

Proprietary — all rights reserved.
For evaluation or licensing, contact the maintainers.

---

### Maintainers

BioClock-in Engineering • Ops & Product

> Tip: keep `Timezone= Africa/Johannesburg` aligned across API hosts and reporting jobs to avoid off-by-one-day issues on midnight boundaries.
