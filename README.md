# device-profile-service

Device profile management for the IoTMining/IIoTEdge platform: reusable
templates that describe a class of device — its type, protocol, inputs,
outputs, dashboards, and metadata — with optional single-level inheritance
from a parent profile.

## Features

- **CRUD** on device profiles, tenant-scoped, with paginated/searchable
  listing (`GET /api/v1/device-profile?page=&size=&textSearch=`) and a
  flat list variant for dropdowns/selectors.
- **Profile inheritance** — a profile can reference a `parent` profile.
  `GET /api/v1/device-profile/{id}/resolved` merges parent + child into
  one effective view; `POST /api/v1/device-profile/batch-resolved` does
  the same for a batch of IDs in one call, for callers (like
  device-management-service) that need resolved profiles for many
  devices at once without N round trips.
- **RBAC** — create/update/delete require an admin-tier authority
  (`ROLE_SUPER_ADMIN`/`ROLE_TENANT_ADMIN`/`ROLE_ADMIN` or their
  unprefixed equivalents); read access on a specific profile is
  additionally checked per-request via `DeviceProfileSecurityEvaluator`
  (`@deviceProfileSecurity.canAccessProfile`), not just the method-level
  authority.
- **Stateless JWT auth** — `LiteJwtAuthenticationFilter` validates the
  bearer token and builds the `Authentication` from its claims; no
  session state, no login endpoint here (tokens are issued by
  auth-service and shared across services via a common signing secret).
- **Service discovery + centralized config** — registers with Eureka and
  pulls config from Spring Cloud Config Server when available, falling
  back to its own bundled `application*.yml` otherwise.

## Running locally

Requires local Postgres (`iotmining` database), and optionally Eureka
(`localhost:8761`) and Config Server (`localhost:8888`) — both config
sources are `optional:`, so the service still boots without them using
the `dev` profile's bundled defaults.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Default port: `8056` (overridable via `SERVER_PORT`). JWT verification
requires `JWT_SECRET_KEY_BASE64` to match the key auth-service signs
with; the `dev` profile falls back to the same shared local-dev secret
used across this platform's other services if unset.

## Build

```bash
./mvnw clean verify
```

Depends on `com.iotmining.common:base` from the separate
[`common`](https://github.com/iiotedge/common) repo — build/install that
module first if it isn't already in your local `~/.m2`.

## Parent / dependency management

Resolves `iiotedge-parent`/`iiotedge-bom` from GitHub Packages
([`platform-build`](https://github.com/iiotedge/platform-build)) rather
than a local relative-path parent — this repo can be built standalone
with just this checkout (plus `common`, above), no monorepo checkout
required.

## Docker

```bash
./mvnw clean package -DskipTests
docker build -t device-profile-service .
docker run -p 8056:8056 device-profile-service
```

The image is runtime-only (see `Dockerfile`): the jar is built beforehand
by CI, not inside the image, so quality gates in CI (`mvn verify`) are a
real gate that can't be bypassed by skipping straight to the Docker
build.
