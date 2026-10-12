# Project Instructions

## Product Scope
- Reusable basic development framework for an admin web application and optional H5, WeChat Mini Program, Android, and iOS clients.
- Keep the repository focused on common authentication, authorization, user management, system configuration, monitoring, code generation, and reusable infrastructure.
- Domain-specific business features should be added only after their scope and module ownership are explicitly defined.

## Architecture
- Backend: Java 21, Spring Boot 3.2.2, Maven multi-module modular monolith.
- Admin: Vue 3, Vite 5, Element Plus, Pinia, Vue Router, Axios.
- Mobile: uni-app Vue 3; one codebase must remain compatible with H5, WeChat Mini Program, Android, and iOS.
- Backend modules are split into `common`, `framework`, `system`, `generator`, and `admin`.
- Controllers are transport adapters only. Transactions and business invariants belong in application services.

## Backend Conventions
- Follow the existing Controller -> Service -> Mapper/XML flow and existing `AjaxResult`/`TableDataInfo` envelopes.
- Admin operations require `@PreAuthorize`; mutations require `@Log` where audit-relevant.
- Use Jakarta Validation at API boundaries and service validation for business invariants.
- Use string IDs through the existing ID utilities; do not introduce auto-increment IDs for new framework entities.
- Status values require enums plus database comments/dictionaries; do not scatter magic numbers.
- Multi-write use cases require `@Transactional(rollbackFor = Exception.class)` at the application-service boundary.

## Data and Security
- MySQL 8 and Redis are infrastructure dependencies.
- SQL changes live under `zjude-basic-service/zjude-aden-admin/src/main/resources/db/migration`.
- Database table names for system functions must use the `sys_` prefix; all other business tables must use the `zjudg_` prefix.
- Every business table has an explicit primary key, status, timestamps, and indexes derived from query paths.
- Never commit secrets, real AppIDs, tokens, passwords, private keys, or payment certificates.
- Do not log passwords, tokens, payment payload secrets, identity data, or full receiving-account details.

## Frontend Conventions
- Admin API modules live under `zjude-basic-ui/src/api/system` or the directory belonging to the feature.
- Admin pages live under `zjude-basic-ui/src/views`.
- Reuse the existing request wrapper, permission directives, pagination, dictionary, upload, and editor components.
- Mobile API access must go through one request adapter; pages must not call `uni.request` directly.
- Isolate platform differences with uni-app conditional compilation and capability adapters.

## Quality Gates
- Backend gate: `mvn -B test` from `zjude-basic-service`.
- Admin gate: `pnpm install --frozen-lockfile`, `pnpm run lint`, `pnpm run test`, and `pnpm run build:prod` from `zjude-basic-ui`.
- Mobile gate: source check and the configured H5, Mini Program, and App builds from `basic-mini`.
- Add regression tests for authentication, authorization, state transitions, validation, and data integrity.

## Git Workflow
- Development branch: `know-dev/dev-0.1`.
- Use Conventional Commit prefixes such as `feat:`, `fix:`, `test:`, `docs:`, and `refactor:`.
- Do not mix baseline repair, dependency upgrades, and business features in one commit.
- Never push, merge, rebase, or create a pull request unless the user explicitly requests it.
