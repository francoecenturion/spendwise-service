# spendwise-service

## Production database migrations

Production uses Flyway against the dedicated `spendwise` Supabase schema.
`V1__initial_schema.sql` will create the complete schema from scratch; later
migrations in `src/main/resources/db/migration` run in version order.

Configure the Render service with these database variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://<supabase-session-host>:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres.<project-ref>
SPRING_DATASOURCE_PASSWORD=<supabase-database-password>
SPRING_FLYWAY_URL=jdbc:postgresql://<supabase-session-or-direct-host>:5432/postgres
SPRING_FLYWAY_USERNAME=postgres.<project-ref>
SPRING_FLYWAY_PASSWORD=<supabase-database-password>
```

Use the Supabase Connect panel to get the values. Use the session pooler for
an IPv4-only Render service; use the direct connection for Flyway when the
runtime can reach Supabase over IPv6. Both URLs must be JDBC URLs, so prefix
the Supabase `postgresql://...` connection string with `jdbc:`.

Do not use the transaction pooler (`:6543`) for `SPRING_FLYWAY_URL`.
`SPRING_FLYWAY_URL` is intentionally required so migrations cannot silently
fall back to a transaction-pooled connection.
