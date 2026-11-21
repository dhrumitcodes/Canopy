# Canopy

Canopy is a multi-college platform for managing campus societies, events, announcements, memberships and participation. It provides role-based access control and college-level data isolation so each college manages its own community.

## Project structure

| Directory | Purpose | Stack |
|---|---|---|
| `societyhub-api/` | REST API, authentication, authorization and business rules | Java 17, Spring Boot 3, Spring Security, JWT, Spring Data JPA |
| `societyweb/` | Browser application | React 19, Vite, React Router |
| PostgreSQL | Application data | PostgreSQL 16+, Flyway migrations |
| `.github/workflows/` | CI, release checks and encrypted backup workflow | GitHub Actions |

## Main capabilities

- User registration and JWT-based authentication.
- College-level tenant isolation and role-based permissions.
- College and society management.
- Society memberships, join requests and role assignments.
- Event creation, approval, discovery and registration.
- QR-based event check-in and attendance tracking.
- Announcements and event notifications/reminders.
- Event feedback and participation certificates.
- Database schema versioning through Flyway.

The API enforces authorization and tenant ownership on the server; frontend route guards are not a substitute for backend permissions.

## Run locally

### Requirements

- Java 17
- Maven 3.9+ (or the Maven wrapper if present)
- Node.js 22+
- npm
- PostgreSQL 16+
- Docker Desktop / Docker Engine (optional, for Compose)

### Option A: Docker Compose

From the repository root:

```bash
cp .env.example .env
```

Edit `.env` and set at least:
- `POSTGRES_PASSWORD`: a strong local database password.
- `JWT_SECRET`: a Base64-encoded secret generated with `openssl rand -base64 48`.

Then start the stack:

```bash
docker compose up --build
```

Open:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Health endpoint: http://localhost:8080/actuator/health

Compose persists PostgreSQL data in the `pgdata` volume. `docker compose down -v` deletes that volume and its database data; do not use it when you need to preserve local data.

### Option B: Run services separately

1. Create a PostgreSQL database and user.
2. Configure backend environment variables (or local Spring properties):
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/canopy`
   - `SPRING_DATASOURCE_USERNAME=<database-user>`
   - `SPRING_DATASOURCE_PASSWORD=<database-password>`
   - `JWT_SECRET=<base64-encoded-secret-of-at-least-32-random-bytes>`
   - `APP_CORS_ALLOWED_ORIGINS=http://localhost:5173`
3. Start the backend:

```bash
cd societyhub-api
mvn spring-boot:run
```

Flyway applies the versioned SQL migrations at application startup. Hibernate validates the resulting schema; it does not create or update production tables.

4. Configure and start the frontend:

```bash
cd societyweb
cp .env.example .env
# Set VITE_API_BASE_URL=http://localhost:8080/api/v1 in .env
npm ci
npm run dev
```

Open http://localhost:5173.

## Configuration reference

Never commit real secrets or production connection strings. Use the hosting provider's secret/environment-variable manager.

### Backend (Render or another Java host)

| Variable | Required | Description |
|---|---:|---|
| `SPRING_DATASOURCE_URL` | Yes | JDBC PostgreSQL URL, including `sslmode=require` for Neon |
| `SPRING_DATASOURCE_USERNAME` | Yes | Database role |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Database password |
| `JWT_SECRET` | Yes | Base64-encoded random secret (at least 32 decoded bytes) |
| `JWT_EXPIRATION_MS` | No | JWT lifetime; defaults to 28,800,000 ms (8 hours) |
| `APP_CORS_ALLOWED_ORIGINS` | Yes in production | Exact frontend origin(s), comma-separated; no trailing slash |
| `ALLOWED_EMAIL_DOMAINS` | Optional | Approved college email domains, comma-separated |
| `PORT` | Host-provided | Spring Boot listens on this port; default is 8080 |

### Frontend (Vercel)

| Variable | Required | Description |
|---|---:|---|
| `VITE_API_BASE_URL` | Yes | Public backend URL ending in `/api/v1` |

Vite variables are embedded into the browser build. Never put database credentials, JWT signing secrets or other private values in `VITE_*` variables.

## Deploy with Neon + Render + Vercel

Recommended separation:
- **Neon:** managed PostgreSQL.
- **Render:** Spring Boot API, built from `societyhub-api/Dockerfile`.
- **Vercel:** React/Vite frontend, with project root `societyweb`.

### 1. Create the Neon database

1. Create a Neon project and database.
2. Copy the connection details from the Neon Console.
3. For this long-running Spring Boot service, start with the **direct** PostgreSQL connection string (hostname without `-pooler`). The API uses HikariCP and Flyway runs migrations at startup; keep a direct connection for migration compatibility. Use TLS by retaining `sslmode=require`.
4. Convert the connection string to JDBC form for Spring. For example:

```text
postgresql://USER:PASSWORD@HOST/DBNAME?sslmode=require
```

becomes:

```text
jdbc:postgresql://HOST:5432/DBNAME?sslmode=require
```

Keep the actual username and password in Render's secret settings, not in this repository.

### 2. Deploy the backend on Render

1. Create a new **Web Service** and connect this GitHub repository.
2. Choose the Docker runtime.
3. Set the repository root as the service root and set the Dockerfile path to `societyhub-api/Dockerfile`.
4. Add the backend environment variables from the table above.
5. Set `SPRING_DATASOURCE_URL` to the Neon JDBC URL, and set the matching database username and password.
6. Set `APP_CORS_ALLOWED_ORIGINS` to the final Vercel site origin (for example, `https://your-app.vercel.app`).
7. Set `JWT_SECRET` to a newly generated secret. Do not reuse a development or historically committed secret.
8. Deploy and verify:
   - `https://YOUR-API-HOST/actuator/health`
   - `https://YOUR-API-HOST/actuator/health/readiness`

The API runs Flyway migrations at startup. Review migration logs and confirm the health endpoints before connecting the frontend.

### 3. Deploy the frontend on Vercel

1. Import the same GitHub repository as a Vercel project.
2. Set **Root Directory** to `societyweb`.
3. Framework preset: Vite.
4. Build command: `npm run build`.
5. Output directory: `dist`.
6. Add `VITE_API_BASE_URL=https://YOUR-API-HOST/api/v1`.
7. Deploy and test client-side routes, refreshes and API requests.

The repository includes `societyweb/vercel.json` for SPA route rewrites. If you add a custom domain later, update `APP_CORS_ALLOWED_ORIGINS` on Render to the exact production origin and redeploy the API.

### 4. Production smoke test

Use test accounts and non-sensitive data first:

- [ ] Registration and login work over HTTPS.
- [ ] JWT-protected endpoints reject unauthenticated requests.
- [ ] College A users cannot access College B data.
- [ ] College and society roles cannot perform actions outside their permissions.
- [ ] Society creation, join requests and membership changes work.
- [ ] Event approval, registration, capacity limits and QR check-in work.
- [ ] Feedback and certificate flows work.
- [ ] Browser refreshes on nested frontend routes work.
- [ ] API logs show no database, migration, CORS or authentication errors.

## Tests and quality checks

Frontend:

```bash
cd societyweb
npm ci
npm run lint
npm run build
npm audit --audit-level=high
```

Backend (the CI PostgreSQL test profile expects a PostgreSQL instance and `TEST_DB_PASSWORD`):

```bash
cd societyhub-api
mvn verify
```

The GitHub Actions workflows include backend tests, frontend lint/build, PostgreSQL-backed release checks, Docker image builds and CodeQL analysis. Review the latest workflow results on GitHub before merging or releasing.

## Backups and recovery

The encrypted backup workflow requires these GitHub Actions repository secrets:

- `BACKUP_DATABASE_URL`: database connection URL used by the backup job.
- `BACKUP_PASSPHRASE`: strong, unique encryption passphrase.

Configure both under **Repository Settings → Secrets and variables → Actions**, then run the backup workflow manually and confirm it creates an encrypted artifact. A successful archive integrity check is not a full restore test: restore a backup into an isolated PostgreSQL database and verify the application can start against it before accepting production data. Keep an additional backup copy outside short-lived CI artifacts.

## Security and operational notes

- Rotate database credentials and JWT signing secrets that were ever committed to Git history. Removing a file from the current branch does not remove it from old commits.
- Use HTTPS for the frontend and API.
- Restrict database access and keep credentials in provider secrets.
- Provision the first `PLATFORM_ADMIN` through the documented secure database bootstrap procedure in [societyhub-api/README-SECURITY.md](societyhub-api/README-SECURITY.md); public registration must not grant privileged roles.
- The current authentication attempt limiter is in-memory and local to one API process. Before running multiple API instances, move rate-limit state to a shared store and verify trusted-proxy configuration.
- Configure and test the backup workflow and complete a restore rehearsal before production launch.
- Do not treat a successful build as proof of production readiness; complete the smoke tests and recovery checks above.

## Documentation

- [Backend security, college onboarding and release-readiness guide](societyhub-api/README-SECURITY.md)
- [Frontend README](societyweb/README.md)
