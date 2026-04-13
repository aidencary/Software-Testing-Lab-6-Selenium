# Software Testing Lab 6 - Selenium
- Link to spreadsheet with test cases and component identification: https://docs.google.com/spreadsheets/d/1sCFsYqJDbTEZPkhhnT78DUWYHXquGM1UgX_JBbBRZts/edit?usp=sharing

## For my Peers on Peerceptive

Use this section to quickly find the artifacts for grading:

- Selenium screenshots: `end2end-tests/screenshots/`
- Java backend source code: `backend/src/main/java/com/baarsch_bytes/studentRegDemo/`
- Java Selenium test code: `end2end-tests/src/test/java/com/baarsch_bytes/end2end/`
- Maven/Surefire test reports: `end2end-tests/target/surefire-reports/`

Main Selenium test classes:
- `end2end-tests/src/test/java/com/baarsch_bytes/end2end/Step1_StudentCRUDTest.java`
- `end2end-tests/src/test/java/com/baarsch_bytes/end2end/Step2_CourseCRUDTest.java`
- `end2end-tests/src/test/java/com/baarsch_bytes/end2end/FrontendAccessibilityTest.java`
- `end2end-tests/src/test/java/com/baarsch_bytes/end2end/FrontendAccessibilityWaitsTest.java`

## Baarsch Comments
This is a demonstration project used for CSCI 4325 Software Testing at the University of Central Arkansas.
The purpose of this demonstration is to provide an environment for creating end-to-end tests using Selenium in a container environment.

The project requires Docker or Podman to run (I use Podman): https://podman-desktop.io/

Be sure that the docker-compose functionality is installed along with the container service.

The backend is written using Maven, SpringBoot along with an H2 database for simplicity.  It is exposed on 8080 by default.
The frontend makes use of React and Typescript, along with a Vite webserver.
The end2end-tests uses Maven and Selenium and is set up to run through the `testing` compose profile.

## Current expected result for the full test profile is:
- 29 tests run
- 0 failures
- 0 errors

---

## Running with Podman (normal)

From the project root directory:

```bash
# Start frontend and backend
podman compose up -d

# Build and run the Selenium tests
podman compose --profile testing up --build end2end-tests
```

---

## Running Locally (without Docker/Podman)

Two URL settings must be changed before running locally, and reverted before running via Podman.

### 1. Vite proxy — `frontend/vite.config.ts`

Change the proxy target from the Docker service name to localhost:

```
// For local dev:
target: 'http://localhost:8080',

// For Podman (revert to this before running with podman compose):
// target: 'http://backend:8080',
```

### 2. Selenium base URL — `end2end-tests/src/test/java/com/baarsch_bytes/end2end/Step1_StudentCRUDTest.java`

Same swap for the test file:

```java
// For local dev:
private static final String BASE_URL = "http://localhost:5173/students";

// For Podman (revert to this before running with podman compose):
// private static final String BASE_URL = "http://frontend:5173/students";
```

Do the same swap in `end2end-tests/src/test/java/com/baarsch_bytes/end2end/Step2_CourseCRUDTest.java`.

### 3. Start servers and run tests

Open three terminals:

```bash
# Terminal 1 — backend (from IdeaProjects/backend/)
mvn spring-boot:run

# Terminal 2 — frontend (from IdeaProjects/frontend/)
npm run dev

# Terminal 3 — run only the new tests (from IdeaProjects/end2end-tests/)
mvn test
```

The two pre-existing tests (FrontendAccessibilityTest, FrontendAccessibilityWaitsTest) use the Docker hostname and will fail when run locally — this is expected.

---

## Inspecting the Database (H2 Console)

While the backend is running, the in-memory H2 database can be inspected at:

```
http://localhost:8080/h2-console/
```

Use these credentials:

| Field        | Value                    |
|--------------|--------------------------|
| JDBC URL     | `jdbc:h2:mem:studentdb`  |
| User Name    | `sa`                     |
| Password     | `password`               |

This is useful for verifying that test data was actually written to the database, or for checking what records exist before running a test.
