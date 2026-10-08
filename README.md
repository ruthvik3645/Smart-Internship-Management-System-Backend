# Smart Internship Management System

Spring Boot 4.1 + Java 21 backend (JWT, JPA, H2) and a React 19 + Vite frontend in `frontend/`.

## Run it

Backend (port 8080):

    ./mvnw spring-boot:run

Frontend (port 5173):

    cd frontend
    npm install
    npm run dev

Open http://localhost:5173. The sign-in page has buttons that fill in the demo accounts.

| Role    | Email               | Password   |
|---------|---------------------|------------|
| Admin   | admin@system.com    | admin123   |
| Company | hr@techcorp.com     | company123 |
| Student | student@univ.edu    | student123 |

A second company, `pending@startuplabs.io` / `company123`, is seeded as pending so you can try the admin approval flow.

## Using your existing project

This folder was generated without access to your project, so `pom.xml` is complete rather than a patch.
If you already have a `pom.xml`, `.mvn/` and `mvnw`, keep your wrapper files and merge the dependencies
(`spring-boot-starter-webmvc`, `-security`, `-data-jpa`, `h2`, `postgresql`, the three `jjwt` artifacts, `lombok`).
Code lives in package `com.internship`; if your main class uses another package, move the folders or rename it.

## Configuration (`src/main/resources/application.properties`)

- `JWT_SECRET` environment variable: set a random 32+ character value outside development.
- `app.cors.allowed-origins`: origins allowed to call the API.
- `app.seed-demo-data=false`: turns off the demo accounts and sample data. Do this in production.
- PostgreSQL: swap the datasource lines as shown in the file.
- Frontend: copy `frontend/.env.example` to `.env` to point at a different backend URL.

## How it behaves

- Students register as active. Company accounts start as pending and cannot sign in until an admin approves them. Admin accounts cannot be self-registered.
- Locking an account takes effect on the next request, even with a valid token.
- Students can apply once per open listing before its deadline, and can only log days for an accepted, unfinished internship.
- Returned logs can be edited and resubmitted. Approved logs are final.
- Mentor feedback is kept as a dated history. An internship can be completed only after a rating is given; completion unlocks the certificate PDF.
- Admin exports (applications, logs) come as CSV or PDF. CSV cells that start with `=`, `+`, `-` or `@` are prefixed so spreadsheets don't run them as formulas.

## API summary

- `POST /api/auth/register`, `POST /api/auth/login`
- `GET /api/v1/users/me`; admin: `GET /api/v1/users`, `PATCH /api/v1/users/{id}/approve | status | role`
- `GET /api/v1/internships`; company: `GET /mine`, `POST`, `PUT /{id}`, `PATCH /{id}/archive`
- student: `POST /api/v1/applications`, `GET /mine`, `GET /{id}/certificate`; company: `GET /company`, `PATCH /{id}/status`, `PATCH /{id}/evaluation`
- student: `POST /api/v1/logs`, `PUT /{id}`, `GET /mine`; company: `GET /review`, `PATCH /{id}/review`
- admin: `GET /api/v1/admin/stats`, `GET /api/v1/admin/reports?type=applications|logs&format=csv|pdf`

## Verification status

Checked in a sandbox without internet: every Java file parses, every JSX file compiles under esbuild,
all frontend imports resolve, and the PDF writer was compiled and its output opened in a PDF library.
Not yet run: `./mvnw compile`, `./mvnw test`, `npm install` and `npm run build`. Run those first.
