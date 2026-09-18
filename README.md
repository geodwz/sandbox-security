# sandbox-security

Spring Security demonstrations — backend API, frontend SPA, and machine-to-machine client.

## Modules

- [backend-api](./backend-api/) — Spring Boot REST service (Java 25, Spring Boot 4.1.1)
- [frontend-vue](./frontend-vue/) — Vue 3 + Vite SPA with Pinia state management
- [m2m-client](./m2m-client/) — Spring Boot OAuth 2 client for machine-to-machine authentication

## Prerequisites

Java 25, Node.js 22+, Docker (for compose)

## Build and Run

Each module is independent. See individual READMEs for setup details.

```bash
# backend-api
cd backend-api && mvn spring-boot:run

# frontend-vue
cd frontend-vue && npm run dev

# m2m-client
cd m2m-client && mvn spring-boot:run
```
