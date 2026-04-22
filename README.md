# Landscape Design

**Landscape Design** is a multi-user landscape design web app: projects with a scene (3D / scene state), an items catalog, a personal library, and subscription tiers with limits. The backend is built with Spring Boot and uses JWT authentication, Postgres, and Liquibase migrations.

## Features

- **Authentication**: register / login, **JWT**.
- **Projects**: list / get / create / delete projects, update **scene state**.
- **Catalog**: browse available details/items.
- **Personal library**: CRUD, import (copy) from a public/browseable library.
- **Subscriptions**: tiers and limits (max projects, max scene elements), (demo) change tier via API.

## Tech stack

- **Java 17**, **Spring Boot 3.2**
- **Spring Web / Security / Validation**, **Spring Data JPA**
- **PostgreSQL**
- **Liquibase** (changelogs in `src/main/resources/db/changelog`)
- **JJWT** (JWT tokens)
- Static assets: `src/main/resources/static/`

## Quick start

### 1) Start the database

```bash
docker compose up -d
```

PostgreSQL defaults:

- **DB**: `landscape_design`
- **User/Password**: `landscape` / `landscape`
- **Port**: `5432`

### 2) Run the application

```bash
mvn spring-boot:run
```

- **Backend** default URL: `http://localhost:8080`

## Configuration

Main configuration lives in `src/main/resources/application.yml`.

- **Database**: `spring.datasource.url = jdbc:postgresql://localhost:5432/landscape_design`
- **JWT secret**: override via environment variable **`APP_JWT_SECRET`**

## Main API endpoints

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### Me / Subscription (demo)

- `GET /api/me`
- `POST /api/me/subscription` (enabled via `app.demo.allow-tier-change: true`)

### Projects

- `GET /api/projects`
- `POST /api/projects`
- `GET /api/projects/{id}`
- `PUT /api/projects/{id}/scene`
- `DELETE /api/projects/{id}`

### Catalog

- `GET /api/catalog/details`

### Library

- `GET /api/my-library/items`
- `POST /api/my-library/items`
- `PATCH /api/my-library/items/{id}`
- `DELETE /api/my-library/items/{id}`
- `GET /api/library/browse`
- `POST /api/my-library/items/import/{sourceId}`

