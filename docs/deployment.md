# MediBridge deployment

## Local demo (no Docker)

`SPRING_PROFILES_ACTIVE=local` (default) uses a file-backed H2 database at `./data/medibridge`. Suitable for a laptop demo, not production.

## Compose

Requires Docker. Set `JWT_SECRET` (32+ chars) and `DB_PASSWORD` in `.env`, then `docker compose up --build`.

- UI: http://localhost:3004
- API: http://localhost:8084
- OpenAPI: http://localhost:8084/swagger-ui.html

## Suggested production split

| Piece | Typical host |
| --- | --- |
| Next.js | Vercel |
| Spring Boot | A Linux VM or container service |
| PostgreSQL | Managed Postgres |
| TLS | Reverse proxy / platform TLS |

Set `CORS_ORIGINS` to the real UI origin. Disable the H2 console. Set `SEED_DEMO_DATA=false` on a real tenant. Rotate `JWT_SECRET`.

Live URL and GitHub remote go here once published — do not invent them.
