# Pool Reservation API

REST API для управления клиентами и бронированием посещений бассейна.

## Технологии

- Java 25
- Spring Boot
- JDBC
- PostgreSQL
- Maven

## Запуск проекта

1. Установить PostgreSQL.
2. Создать базу данных `pool_ufanet_db` и пользователя `pool_app`.
3. Настроить подключение к БД в `src/main/resources/application.yaml`.
4. Запустить приложение:

```bash
./mvnw spring-boot:run
```

## Схема базы данных

### clients

- `id` — идентификатор клиента, первичный ключ.
- `name` — ФИО клиента.
- `phone` — номер телефона.
- `email` — электронная почта.

### reservations

- `id` — идентификатор бронирования.
- `client_id` — ссылка на клиента (`clients.id`).
- `start_time` — дата и время посещения.
- `status` — статус (`ACTIVE` или `CANCELLED`).
- `cancel_reason` — причина отмены.

### working_hours

- `id` — идентификатор расписания.
- `day_of_week` — день недели от 1 до 7.
- `open_time` — время открытия.
- `close_time` — время закрытия.

## API

| Метод | Путь | Назначение |
|---|---|---|
| GET | `/api/v0/pool/clients` | Список клиентов |
| GET | `/api/v0/pool/clients/{id}` | Информация о клиенте |
| POST | `/api/v0/pool/clients` | Создание клиента |
| PATCH | `/api/v0/pool/clients/{id}` | Изменение клиента |
| GET | `/api/v0/pool/time-slots/available?date=YYYY-MM-DD` | Свободные места |
| GET | `/api/v0/pool/reservations?date=YYYY-MM-DD` | Записи на дату |
| POST | `/api/v0/pool/reservations` | Создание записи |
| POST | `/api/v0/pool/reservations/{id}/cancel` | Отмена записи |

## Проверка

Запуск тестов:

```bash
./mvnw test
```
