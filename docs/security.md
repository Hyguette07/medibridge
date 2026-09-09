# MediBridge — Security

Educational software. **Not a medical device, not a doctor, and not clinical advice.**

## Controls

- BCrypt password hashing
- JWT HMAC with secret from environment (`JWT_SECRET`, min 32 characters), prefix `medibridge.jwt`
- Role-based authorization on mutating endpoints
- `PrivacyGuard`: clinicians cannot read or write care notes without active consent
- `AccessAuditService`: every PHI-adjacent read/write writes an audit row
- Bean Validation on request bodies
- CORS allow-list (`http://localhost:3004` by default)
- Login rate limiting per IP
- 401 JSON entry point (`success: false`, `UNAUTHORIZED`)
- No passwords, JWT, or care-note bodies in logs

## Secrets

All secrets live in environment variables. See `.env.example`. Never commit `.env`.

## Data minimization

Patient profiles store limited demographics. Care notes never appear on patient screens. Audit detail describes the action, not clinical text.

## Transport

TLS is required in deployment (reverse proxy). Local HTTP is for development only.
