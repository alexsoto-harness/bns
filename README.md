# BNS Spring Boot Sample

Spring Boot 3.2 demo app with a full CI/CD pipeline targeting Kubernetes.

## Tech Stack

- **Java 17** / **Spring Boot 3.2.5** (Maven)
- **Docker** (Eclipse Temurin 17 Alpine)
- **Kubernetes** (Go template manifests in `k8s/`)

## Runtime configuration

`/api/info` and `/api/config` read **environment variables on the pod**. Those values are wired from `env.config` in [`k8s/values.yaml`](k8s/values.yaml) (templated into a ConfigMap and mounted with `envFrom`).

**Where Harness expressions go:** Harness resolves `<+...>` in **Values YAML** files, not in the raw Kubernetes templates under `k8s/templates/`. This repo’s service points at `k8s/values.yaml` via `valuesPaths` in [`.harness/service.yaml`](.harness/service.yaml). At deploy time, Harness substitutes expressions, then applies the manifests.

**Optional service variables:** [`.harness/service.yaml`](.harness/service.yaml) defines `enableDarkMode`; [`k8s/values.yaml`](k8s/values.yaml) references it as `<+serviceVariables.enableDarkMode>`. You can change the default in the service or override it per environment in Harness without committing Git changes. Other keys use built-in pipeline/codebase expressions (see Harness [built-in variables](https://developer.harness.io/docs/platform/variables-and-expressions/harness-expressions-reference/) and [CI codebase variables](https://developer.harness.io/docs/continuous-integration/use-ci/codebase-configuration/built-in-cie-codebase-variables-reference/) for `codebase.commitSha`).

| Variable | Source in this repo | Used for |
|----------|---------------------|----------|
| `GIT_COMMIT_SHA`, `GITHUB_SHA` | Both use `"<+codebase.commitSha>"` in [`k8s/values.yaml`](k8s/values.yaml) | `gitSha` (first non-blank wins) |
| `BUILD_ID` | `"<+pipeline.executionId>"` | `buildId` |
| `RUN_NUMBER` | `"<+pipeline.sequenceId>"` | `buildId` (fallback after `BUILD_ID` in Java) |
| `ENABLE_DARK_MODE` | `"<+serviceVariables.enableDarkMode>"` | JSON `enableDarkMode` |

## Endpoints

| Path | Description |
|------|-------------|
| `/` | Static demo page (API status panels) |
| `/api/info` | JSON app metadata (version, optional git/build env vars) |
| `/api/config` | JSON config (`ENABLE_DARK_MODE` → `enableDarkMode`) |
| `POST /api/contact` | Demo form submit (logged, returns 202) |
| `/actuator/health` | Actuator health |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |

## CI/CD

**GitHub Actions** (`.github/workflows/ci.yml`) — on every push:
1. Build JAR with Maven
2. Build & push Docker image to Docker Hub, tagged with the short SHA and `latest`

**Harness Pipeline** (`.harness/pipeline.yaml`) — four-stage Kubernetes deploy:
1. **Dev** — rolling deploy to GKE dev cluster
2. **Staging** — rolling deploy to GKE staging cluster
3. **Approval** — manual gate before production
4. **Prod** — rolling deploy to GKE prod cluster

A webhook trigger (`.harness/trigger.yaml`) automatically kicks off the Harness pipeline when a new Docker image is pushed.

**Harness CD and `values.yaml` expressions:** `k8s/values.yaml` uses `<+codebase.commitSha>` for git metadata. That resolves when the **pipeline** defines a **codebase** (same Git connector and repo as the service manifests, e.g. `bns`). Add it under **Pipeline → Pipeline Studio → (YAML) → `properties.ci.codebase`**, or mirror [`.harness/pipeline.yaml`](.harness/pipeline.yaml) if you use Git-backed pipeline definitions. Without a codebase, those expressions can fail at deploy time (similar to the old `manifest.k8s_templates.commitId` error on pipelines like `zest`).

**GitHub Actions vs Harness CI:** Only Harness substitutes `<+...>` when it renders Helm/K8s values at deploy time. Whether the image was built in GitHub Actions or in Harness does not matter; the CD pipeline still needs a resolvable commit source. If you use **Git** webhooks and do not want a pipeline codebase, you can instead set both `GITHUB_SHA` and `GIT_COMMIT_SHA` in `k8s/values.yaml` to `"<+trigger.commitSha>"` (Git-triggered runs only).

## Local Development

```bash
mvn package -DskipTests
java -jar target/spring_boot_sample-0.0.1-SNAPSHOT.jar
```

Or with Docker:

```bash
mvn package -DskipTests
docker build -t bns_spring_boot .
docker run -p 8080:8080 bns_spring_boot
```

The app will be available at `http://localhost:8080`.

## Project Structure

```
├── src/                  # Java source & static assets
├── k8s/                  # Kubernetes manifests (values, deployment, service)
├── .github/workflows/    # GitHub Actions CI
├── .harness/             # Harness CD pipeline, trigger, infra & service defs
├── Dockerfile
└── pom.xml
```
