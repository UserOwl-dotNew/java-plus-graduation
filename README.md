# Explore With Me

Explore With Me — сервис для поиска мероприятий и организации совместного досуга. Пользователи могут создавать мероприятия, искать опубликованные события, подавать заявки на участие и оставлять комментарии. Администраторы управляют пользователями, категориями и подборками, модерируют события и комментарии.

На втором этапе дипломного проекта основное приложение разделено на четыре микросервиса. Публичный API доступен через Gateway по адресу `http://localhost:8080`.

## Архитектура

| Сервис | Ответственность | База в Docker Compose |
| --- | --- | --- |
| [`event-service`](core/main-service/event-service) | События, категории и подборки | `ewm-event` |
| [`request-service`](core/main-service/request-service) | Заявки на участие, подтверждение и отклонение | `ewm-request` |
| [`user-service`](core/main-service/user-service) | Администрирование пользователей и предоставление данных о них | `ewm-user` |
| [`comment-service`](core/main-service/comment-service) | Создание, чтение, редактирование и модерация комментариев | `ewm-comment` |
| [`stats-server`](statistics-service/statistics-service-server) | Запись обращений и выдача статистики просмотров | `ewm-stats` |

[`main-service`](core/main-service/pom.xml) объединяет четыре прикладных сервиса при сборке Maven. В [`common`](common) находятся общие DTO, исключения и Feign-клиенты. PostgreSQL запущен в отдельных контейнерах для каждого сервиса. Межсервисные ссылки, например `initiator_id` и `author_id`, хранятся как идентификаторы.

- [`discovery-server`](infra/discovery-server) ведёт реестр Eureka. Панель реестра открывается на `http://localhost:8761`.
- [`config-server`](infra/config-server) загружает настройки из [`infra/config-repo`](infra/config-repo) в режиме `native`. Общие параметры находятся в [`application.yml`](infra/config-repo/application.yml), конфигурации прикладных сервисов — в [`core`](infra/config-repo/core), статистики — в [`statistics-service`](infra/config-repo/statistics-service).
- [`gateway-server`](infra/gateway-server) направляет клиентские запросы в нужный сервис по его имени в Eureka. Правила маршрутизации описаны в [`gateway-server.yml`](infra/config-repo/infra/gateway-server.yml). Параметры для запуска в контейнерах задаются в [`docker-compose.yml`](docker-compose.yml).

Стек проекта включает Java 21, Spring Boot 3.5, Spring Cloud 2025.0, OpenFeign, PostgreSQL и Maven.

## Внутренний API

Сервисы обращаются друг к другу через OpenFeign по именам из Eureka. Контракты клиентов находятся в [`common/openfeign`](common/src/main/java/ru/yandex/practicum/common/openfeign).

| Сервис | Запрос | Назначение |
| --- | --- | --- |
| `event-service` | `GET /internal/events/{id}` | Возвращает `EventFullDto`. Сервисы заявок и комментариев используют статус события, а сервис заявок также использует ID организатора и параметры участия. Запрос не регистрирует просмотр. |
| `event-service` | `GET /internal/events/{id}/exists` | Проверяет, существует ли событие. Используется сервисом комментариев. |
| `event-service` | `GET /internal/events/exists/category/{id}` | Проверяет, есть ли события в категории перед её удалением. |
| `event-service` | `GET /internal/events/initiator/{id}` | Проверяет, организовал ли пользователь какие-либо события. Используется при удалении пользователя. |
| `user-service` | `GET /internal/users/{id}` | Возвращает `UserDto` для сервисов событий и комментариев. |
| `user-service` | `GET /internal/users/{id}/exists` | Проверяет, существует ли пользователь. Используется сервисом комментариев. |
| `request-service` | `GET /internal/requests/event/{eventId}/confirmed-count` | Возвращает число подтверждённых заявок для одного события. |
| `stats-server` | `POST /hit` | Принимает запись об обращении от сервиса событий. |
| `stats-server` | `GET /stats` | Возвращает количество просмотров по URI за период. Принимает `start`, `end`, `uris` и `unique`. |

## Запуск

Понадобятся JDK 21, Maven и Docker с Docker Compose. Выполни команды из корня репозитория. Сначала Maven соберёт JAR-файлы, которые используются в Dockerfile.

```bash
mvn clean package -DskipTests
docker compose up -d --build
docker compose ps
```

Когда сервисы появятся в Eureka, проверь запрос через Gateway.

```bash
curl -i http://localhost:8080/categories
```

Остановить окружение можно командой `docker compose down`.

## Публичный API и тесты

- [OpenAPI основного сервиса](ewm-main-service-spec.json) описывает события, заявки, пользователей, категории и подборки.
- [OpenAPI статистики](ewm-stats-service-spec.json) описывает `POST /hit` и `GET /stats`. При запуске этого проекта оба маршрута доступны через Gateway на порту `8080`.
- Postman-коллекции находятся в [`postman`](postman). Коллекции [основного API](postman/ewm-main-service.json) и [комментариев](postman/feature.json) используют `http://localhost:8080`. Для локального запуска [коллекции статистики](postman/ewm-stat-service.json) через Gateway замени `http://localhost:9090` на `http://localhost:8080` в копии коллекции, включая URL внутри скриптов.

```bash
mvn test
```

Учебный workflow GitHub Actions собирает проект, запускает сервисы и выполняет свой набор Postman-тестов. [Последний успешный прогон для коммита `6d302fb`](https://github.com/UserOwl-dotNew/java-plus-graduation/actions/runs/46156451719).

## Текущее поведение при чтении событий

Публичный список событий загружает просмотры пачкой, а число подтверждённых заявок запрашивает отдельно для каждого события. Если чтение статистики завершается ошибкой, сервис подставляет нулевые значения. Сбой записи просмотра или недоступность сервиса заявок могут привести к ошибке в ответе на запрос событий.
