# MediBridge tests

Meaningful coverage over vanity percentages.

This is educational software — not a medical device, not a doctor, and not clinical advice.

## Backend (`mvn test`)

| Test | Why it exists |
| --- | --- |
| `PrivacyGuardTest` | Deny notes without consent; allow with consent — the product promise |
| `AuthControllerTest` | Register/login/JWT and unauthenticated appointment access |

Add service tests when you change consent rules or audit actions.

## Frontend

`privacy.test.ts` mirrors the consent gate and keeps the educational disclaimer wording (`npm test -- --run`).

Do not log passwords or tokens in test output.
