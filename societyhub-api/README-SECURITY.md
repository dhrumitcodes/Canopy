# Canopy backend security and college onboarding

## Platform bootstrap

Public registration always creates STUDENT accounts. To provision the first PLATFORM_ADMIN, register a normal account then run this SQL as the database owner:

```sql
UPDATE users SET role = 'PLATFORM_ADMIN', college_id = NULL
WHERE email = 'platform-admin@your-domain.example';
```

Never expose this operation through a public API.

## College onboarding

1. The college admin first registers a normal account using the college's official email domain, before that domain is onboarded.
2. A PLATFORM_ADMIN calls POST /api/v1/platform/colleges with name, emailDomain and adminEmail.
3. The API checks that the admin email belongs to the domain, creates the college, assigns the account and promotes it to COLLEGE_ADMIN.
4. New registrations matching a configured college domain are automatically assigned to that college.

The V10 migration assigns existing users and societies to a migration-created Default College to preserve existing installations. Society names are unique within each college.

## Local backend tests

The test profile expects PostgreSQL at localhost:5433 with database societyhub_test. Set TEST_DB_PASSWORD in your shell before running Maven tests. The GitHub Actions workflow provisions this database automatically.

## Database backups

The scheduled encrypted backup workflow requires two GitHub Actions repository secrets:

- BACKUP_DATABASE_URL: the hosted PostgreSQL connection URL, including credentials.
- BACKUP_PASSPHRASE: a strong passphrase used to encrypt the backup artifact.

Add them under repository Settings > Secrets and variables > Actions. The backup job now checks both before attempting a dump.

## Deployment security

The database password and JWT signing secret were previously committed to Git history. Removing .env files from the working branch does not erase their historical values. Rotate both secrets before deployment and invalidate existing JWTs by replacing the signing secret. Also remove any exposed credentials from deployment provider settings and ensure the new secrets are stored only in the provider's secret manager.