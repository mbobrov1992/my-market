# Витрина интернет-магазина

Приложение состоит из подмодулей:

- market-core - это реактивное веб-приложение, предназначенное для поиска, просмотра и заказа товаров.  
Использует Spring Boot, Spring WebFlux, Thymeleaf для шаблонов, базу данных PostgreSQL и Redis для кэша товаров.

- payment - реактивное веб-приложение, предназначенное для управления балансом пользователя.  
Использует Spring Boot и Spring WebFlux.

---

## Требования

- Java 21
- PostgreSQL (рекомендуется версия 17+)
- Redis 7.4+
- Maven 3.6+
- Keycloak 26.4+

---

## Конфигурация

### Настройка подключения к базе данных

Необходимо создать базу данных `my_market` и пользователя с соответствующими правами.

Приложение использует PostgreSQL. Параметры подключения задаются через переменные окружения или значения по умолчанию в `application.yml`:
- `DB_HOST` — адрес сервера базы данных (по умолчанию `localhost`)
- `DB_USERNAME` — имя пользователя PostgreSQL
- `DB_PASSWORD` — пароль пользователя PostgreSQL

### Настройка подключения к Redis

Приложение использует Redis для кэша. Параметры подключения задаются через переменные окружения или значения по умолчанию в `application.yml`:
- `REDIS_HOST` — адрес сервера Redis (по умолчанию `localhost`)

### Настройка подключения к Keycloak

Приложение использует Keycloak как сервер авторизации с поддержкой OAuth2 Client Credentials Flow.
- подмодуль **market-core** выступает в роли OAuth2 клиента, который получает access token, используя client credentials (client_id и client_secret).
- подмодуль **payment** выступает как сервер ресурсов, который защищает свои REST API и валидирует входящие JWT access tokens, выданные Keycloak.

Параметры сервиса авторизации задаются через переменные окружения или значения по умолчанию в `application.yml`:
- `KEYCLOAK_ISSUER_URI` — URL эндпоинта issuer (например, https://keycloak.example.com/realms/myrealm).
- `KEYCLOAK_CLIENT_ID` — идентификатор клиента в Keycloak, зарегистрированного для market-core.
- `KEYCLOAK_CLIENT_SECRET` — секрет клиента в Keycloak, зарегистрированного для market-core.

Ключевые моменты настройки:
- В **market-core** в Spring Security настроен OAuth2 client с grant type client_credentials, для автоматического получения и обновления токена доступа.
- В **payment** настроен OAuth2 resource server, который проверяет JWT через issuer-uri, без необходимости отдельного client-id/secret.
- Для корректной работы необходимо зарегистрировать клиента в Keycloak для **market-core** и включить для него опцию `Service Accounts Enabled`, чтобы клиент мог получать токены по client credentials.

---

## Сборка и размещение приложения

### Сборка JAR файла через Maven

Используется Maven для сборки исполняемого "fat" JAR файла.

### Команды для сборки и деплоя
Соберите подмодули, выполнив команду из корня проекта:
`mvn clean package`

При успешной сборке исполняемый JAR-файл будет автоматически создан в директории target каждого подмодуля.

Запустите JAR-файлы следующей командой, при необходимости указав порт (по умолчанию 8080):
`java -jar <application-name>.jar --server.port=9090`

Доступ к соответствующему приложению будет по адресу:
`http://<host>:<port>/`

### Команды для сборки и запуска в Docker
Создайте Docker-образы, используя команды:  

```bash
docker build -t market-core ./market-core
```
Будет создан образ с именем `market-core` на основе [Dockerfile](market-core/Dockerfile)

```bash
docker build -t payment ./payment
```
Будет создан образ с именем `payment` на основе [Dockerfile](payment/Dockerfile)

Конфигурация и внешние порты описаны в [docker-compose.yml](./docker-compose.yml).  

Переменные, используемые в файле [docker-compose.yml](./docker-compose.yml), могут быть заданы в файле [.env](./.env),  
расположенном рядом с [docker-compose.yml](./docker-compose.yml).  
Docker Compose автоматически подгружает переменные из этого файла, например:
```text
DB_USERNAME=admin
```

Запустите контейнеры, используя команду:
```bash
docker compose up
```
Будут запущены контейнеры: `postgres`, `redis`, `keycloak`, `market-core` и `payment`.

Доступ к приложению будет по адресу:
`http://<host>:<port>/`

---

## Тестирование приложения

### Команды для запуска тестов
Запустите тесты командой:
`mvn clean test`

### Загрузка товаров на витрину
Для предварительной загрузки списка товаров на витрину можно использовать [SQL-скрипт](src/test/resources/v1.0.0_00_insert_item.sql)