# Canopy security and release-readiness guide

## Platform bootstrap

Public registration always creates STUDENT accounts. To provision the first PLATFORM_ADMIN, register a normal account then run this SQL as the database owner:

```sql
UPDATE users SET role = 'PLATFORM_ADMIN', college_id = NULL
WHERE email = 'platform-admin@your-domain.example';
```

Never expose this operation through a public API. Use a unique, monitored admin mailbox and change the example email to the intended account.

## College onboarding

1. The college admin first registers a normal account using the college's official email domain, before that domain is onboarded.
2. A PLATFORM_ADMIN calls POST /api/v1/platform/colleges with name, emailDomain and adminEmail.
3. The API checks that the admin email belongs to the domain, creates the college, assigns the account and promotes it to COLLEGE_ADMIN.
4. New registrations matching a configured college domain are automatically assigned to that college.

The V10 migration assigns existing users and societies to a migration-created Default College to preserve existing installations. Society names are unique within one college.

## Automated release checks

The Release readiness workflow runs:
- Backend Maven verification against PostgreSQL 16, including Flyway migrations and integration tests.
- Frontend dependency installation, ESLint and production build.
- Docker Compose configuration validation and production API/frontend image builds.

The existing backend and frontend workflows remain as fast, path-filtered checks. A green release-readiness workflow is required before considering a release candidate.

## Local backend tests

The test profile expects PostgreSQL at localhost:5433 with database societyhub_test. Set TEST_DB_PASSWORD in your shell before running Maven tests. GitHub Actions provisions this database automatically.

## Backups and restore verification

The scheduled encrypted backup workflow requires two GitHub Actions repository secrets:
- BACKUP_DATABASE_URL: hosted PostgreSQL connection URL, including credentials.
- BACKUP_PASSPHRASE: a strong, unique encryption passphrase.

Add them under repository Settings > Secrets and variables > Actions. The workflow validates both secrets, creates a custom-format pg_dump, checks that PostgreSQL can read the archive, encrypts it with AES-256, decrypts it in the runner to verify integrity, then uploads only the encrypted artifact for 14 days.

A successful archive integrity check is not a full recovery rehearsal. Before production launch and periodically thereafter, restore a downloaded backup into an isolated PostgreSQL instance and verify the application can start against it. Store a second backup copy outside GitHub Actions artifacts for disaster recovery.

## Deployment checklist

- [ ] Rotate the database password and JWT signing secret. Both were previously committed to Git history; deleting local env files does not erase historical values. Remove old credentials from provider settings and invalidate existing JWTs by replacing the signing secret.
- [ ] Create production secrets in the hosting provider's secret manager. Never commit a real .env file.
- [ ] Set POSTGRES_PASSWORD and JWT_SECRET. JWT_SECRET must be Base64 encoding of at least 32 random bytes; generate with `openssl rand -base64 48`.
- [ ] Set WEB_ORIGIN to the exact HTTPS frontend origin and API_PUBLIC_URL to the HTTPS API base URL ending in /api/v1.
- [ ] Set ALLOWED_EMAIL_DOMAINS to the approved college domains, comma-separated, if registration must be restricted.
- [ ] Configure BACKUP_DATABASE_URL and BACKUP_PASSPHRASE in GitHub Actions, then run the Database backup workflow manually and confirm the encrypted artifact exists.
- [ ] Perform a restore rehearsal from that encrypted artifact before production data is accepted.
- [ ] Confirm the platform administrator is provisioned securely and college onboarding is tested.
- [ ] Confirm HTTPS, database network restrictions, persistent database storage, log access, alerting and a rollback procedure at the hosting provider.
- [ ] Verify /actuator/health/liveness and /actuator/health/readiness from the hosting environment. Health endpoints intentionally expose no component details.
- [ ] Smoke-test registration, login, tenant isolation, society permissions, event approval, registration, QR check-in, feedback and certificate download using non-production accounts.

## Operational limitations to address

The current authentication attempt limiter is in-memory and local to one API process. For multi-instance production deployments, replace it with a shared rate-limit store (for example Redis) and configure trusted proxy handling so client IP rate limits cannot be spoofed by untrusted forwarded headers.

The scheduled backup workflow cannot succeed until its two repository secrets are configured. Do not remove the validation failure or substitute dummy values: that would create a false impression that backups are running.
