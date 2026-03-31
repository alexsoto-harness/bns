# BNS Spring Boot Sample

Spring Boot 3.2 demo app with a full CI/CD pipeline targeting Kubernetes.

## Tech Stack

- **Java 17** / **Spring Boot 3.2.5** (Maven)
- **Docker** (Eclipse Temurin 17 Alpine)
- **Kubernetes** (Go template manifests in `k8s/`)

## Endpoints

| Path | Description |
|------|-------------|
| `/` | Static landing page |
| `/api/info` | JSON health/info check |

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
