# Canopy backend security and college onboarding

Public registration always creates STUDENT accounts. To provision the first PLATFORM_ADMIN, register a normal account then run this SQL as the database owner:

```sql
UPDATE users SET role = 'PLATFORM_ADMIN', college_id = NULL WHERE email = 'platform-admin@your-domain.example';
```

To onboard a college, the admin first registers using the college email domain. A PLATFORM_ADMIN then calls POST /api/v1/platform/colleges with name, emailDomain and adminEmail. The admin email must belong to that domain. New registrations matching a configured college domain are assigned automatically.

The V10 migration assigns existing data to Default College to preserve existing installations. Rotate the previously committed database password and JWT secret before deployment; removing .env from the branch does not remove credentials from Git history.