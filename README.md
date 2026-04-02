# BNS Spring Boot sample

Small **Spring Boot 3** web app (Java 17) used for CI/CD and deployment demos. Serves a static UI and a few JSON APIs.

## Run locally

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). Actuator exposes `health` and `info` under `/actuator/*` on the same port.

## Build and Docker

```bash
mvn -q -DskipTests package
docker build -t bns-sample .
docker run --rm -p 8080:8080 bns-sample
```

## API

| Method | Path | Notes |
|--------|------|--------|
| GET | `/api/info` | App name, status, JAR version, git/build from env (`GIT_COMMIT_SHA`, `GITHUB_SHA`, `COMMIT_SHA`, `BUILD_ID`, `RUN_NUMBER`) |
| GET | `/api/config` | `enableDarkMode` from `ENABLE_DARK_MODE` (default `false`) |
| POST | `/api/contact` | JSON `{"email": "...", "message": "..."}` — `email` required |

## Tests

```bash
mvn test
```
