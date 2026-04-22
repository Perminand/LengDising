# Landscape Design

**Landscape Design** — многопользовательское веб‑приложение для ландшафтного дизайна: проекты со сценой (3D/состояние сцены), каталог элементов, личная библиотека и система подписок с лимитами. Backend на Spring Boot с JWT‑аутентификацией, Postgres и миграциями Liquibase.

## Возможности

- **Авторизация**: регистрация/логин, **JWT**.
- **Проекты**: список/получение/создание/удаление проекта, обновление **scene state**.
- **Каталог**: просмотр доступных деталей/элементов.
- **Личная библиотека**: CRUD, импорт (копия) из публичной библиотеки.
- **Подписки**: тарифы и лимиты (кол-во проектов, кол-во элементов сцены), (в демо) смена тарифа через API.

## Технологии

- **Java 17**, **Spring Boot 3.2**
- **Spring Web / Security / Validation**, **Spring Data JPA**
- **PostgreSQL**
- **Liquibase** (changelog’и в `src/main/resources/db/changelog`)
- **JJWT** (JWT токены)
- Статика: `src/main/resources/static/`

## Быстрый старт

### 1) Поднять базу данных

```bash
docker compose up -d
```

По умолчанию поднимается PostgreSQL:

- **DB**: `landscape_design`
- **User/Password**: `landscape` / `landscape`
- **Port**: `5432`

### 2) Запустить приложение

```bash
mvn spring-boot:run
```

- **Backend** по умолчанию: `http://localhost:8080`

## Конфигурация

Основные настройки находятся в `src/main/resources/application.yml`.

- **БД**: `spring.datasource.url = jdbc:postgresql://localhost:5432/landscape_design`
- **JWT secret**: можно переопределить переменной окружения **`APP_JWT_SECRET`**

## Основные API

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### Me / Subscription (demo)

- `GET /api/me`
- `POST /api/me/subscription` (включается флагом `app.demo.allow-tier-change: true`)

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

