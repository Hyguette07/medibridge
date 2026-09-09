# Lessons learned — MediBridge

- An appointment list is not a privacy product. The access timeline is what patients remember.
- Consent that is not checked on the note endpoint is theater. `PrivacyGuard` is a unit-tested gate, not a UI checkbox.
- Audit logs that store the care-note body recreate the PHI problem. Log the action, not the text.
- H2 for local and PostgreSQL for production is a trade-off: faster demos, extra discipline on SQL dialect. Flyway is the source of truth for Postgres.
- Educational healthcare software must repeat the disclaimer on the landing page, the shell, and the README. Once is not enough.
