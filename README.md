# NOVELLE: Stories Beyond the Ordinary.
WRITE · DISCOVER · CONNECT. A blogging platform with Spring Boot 3 (Java 17), Spring Security sessions, Spring Data JPA and MySQL 8, with a vanilla HTML/CSS/JS single-page frontend.

## Prerequisites
JDK 17+, Maven 3.9+, MySQL 8+ running locally.

## Setup
1. Start MySQL. The database `novelle_db` is created automatically on first run (or run `CREATE DATABASE novelle_db CHARACTER SET utf8mb4;`). Tables are created by Hibernate.
2. Set environment variables (see `.env.example`; Spring does not read `.env` itself):
   - Linux/macOS: `export DB_USERNAME=root DB_PASSWORD=yourpass ADMIN_PASSWORD=StrongPass123`
   - Windows PowerShell: `$env:DB_USERNAME="root"; $env:DB_PASSWORD="yourpass"; $env:ADMIN_PASSWORD="StrongPass123"`
3. Run: `mvn spring-boot:run`
4. Open http://localhost:8080

## Admin
An ADMIN user is created on first start from `ADMIN_EMAIL` (default `admin@novelle.local`) and `ADMIN_PASSWORD` (default `ChangeMe123!`; always override it). Log in, then use the Admin link in the nav.

## Demo content
With `SEED_DEMO=true` (default) 8 sample stories are inserted once, by 3 demo authors (`@demo.novelle` emails, bio "Demo author", random unusable passwords). Set `SEED_DEMO=false` to skip.

## Architecture
`controller` (REST) → `service` (business rules, ownership checks) → `repository` (JPA) → MySQL. DTO records in `dto/Dtos.java`, errors via `GlobalExceptionHandler` as `{status,message}`. Sessions + BCrypt; CSRF via `XSRF-TOKEN` cookie, the frontend sends `X-XSRF-TOKEN` (token returned by `GET /api/auth/me`).

## REST API
Auth: `POST /api/auth/register|login|logout`, `GET /api/auth/me` · Posts: `GET /api/posts?q=&category=&sort=newest|liked&page=&size=`, `GET|PUT|DELETE /api/posts/{id}`, `POST /api/posts` · Comments: `GET|POST /api/posts/{id}/comments`, `DELETE /api/comments/{id}` · Likes: `POST|DELETE /api/posts/{id}/like` · Users: `GET /api/users/{id}`, `PUT /api/users/profile`, `GET /api/users/{id}/posts`, `GET /api/users/authors` · Admin: `GET /api/admin/dashboard|users|posts|comments`, `DELETE /api/admin/users/{id}`, `DELETE /api/admin/posts/{id}`.

## Postman
1. `GET /api/auth/me`: copy `csrf` from the body. 2. `POST /api/auth/login` with header `X-XSRF-TOKEN: <csrf>` and JSON `{"email":"...","password":"..."}` (Postman keeps the session cookie). 3. Send the same header on every POST/PUT/DELETE.

## Troubleshooting
- `Access denied for user`: check `DB_USERNAME`/`DB_PASSWORD`.
- Port 8080 busy: `SERVER_PORT=8081`.
- Cover images blank: external image URLs need internet; the card falls back to a plain block.

## Screenshots
_Add screenshots here: home, explore, article, editor, admin._
