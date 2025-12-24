# Spring-Recommendation-System

Проект Spring-Recommendation-System — это веб-приложение на основе Spring Boot, реализующее систему рекомендаций на основе контента с использованием векторных эмбеддингов. Проект использует Spring AI, Ollama и PostgreSQL с расширением pgvector для хранения и поиска векторных представлений.

## Технологический стек

- **Spring Boot 3.5.9** — основной фреймворк
- **Spring Data JPA** — работа с базой данных
- **Hibernate Vector** — поддержка векторных полей в JPA
- **PostgreSQL + pgvector** — хранение векторных эмбеддингов
- **Ollama** — генерация эмбеддингов с моделью `mxbai-embed-large`
- **Spring AI** — интеграция с LLM и векторным хранилищем
- **Flyway** — управление миграциями базы данных
- **Docker & Docker Compose** — контейнеризация сервисов
- **Java 25** — язык программирования

## Структура проекта

```
src/
├── main/java/ru/kuzmin/recommendation
│   ├── ai/batch/EmbeddingBatchProcessor.java
│   ├── ai/service/OllamaEmbeddingService.java
│   ├── config/AsyncConfig.java
│   ├── controller/EmbeddingController.java
│   ├── controller/RecommendationController.java
│   ├── model/dto/ — DTO для запросов и ответов
│   ├── model/entity/Item.java — сущность товара с векторным полем
│   ├── repository/ItemRepository.java — репозиторий с векторными запросами
│   ├── service/RecommendationService.java — логика рекомендаций
│   └── SpringRecommendationSystemApplication.java
└── test/

Docker-сервисы:
- `postgres` — база данных с pgvector
- `ollama` — сервер для генерации эмбеддингов
```

## Запуск проекта

### 1. Запуск инфраструктуры через Docker Compose

```bash
  docker-compose up -d postgres ollama
```

Это запустит:
- PostgreSQL на порту `5431` (логин: `postgres`, пароль: `password`, БД: `recommendation_db`)
- Ollama на порту `11434`, которая автоматически загрузит модель `mxbai-embed-large`

### 2. Запуск Spring Boot приложения

#### Через Maven:
```bash
  ./mvnw spring-boot:run
```

#### Или сборка и запуск JAR:
```bash
  ./mvnw clean package
  java -jar target/Recommendation-System-0.0.1-SNAPSHOT.jar
```

Приложение будет доступно на `http://localhost:8080`.

## Примеры использования API

### Получение статистики по эмбеддингам
```bash
    curl -X GET "http://localhost:8080/api/v1/admin/embeddings/stats"
    -H "Accept: application/json"
```

### Получение рекомендаций по ID товара
```bash
    curl -X GET "http://localhost:8080/api/v1/recommendations/content-based/2a50563e-72b9-440c-9601-cfb6ebd2c569?limit=3"
    -H "Accept: application/json"
```

### Получение рекомендаций по ID товара
```bash
    curl -X GET "http://localhost:8080/api/v1/recommendations/content-based/2a50563e-72b9-440c-9601-cfb6ebd2c569?limit=3" \
      -H "Accept: application/json"
```

### POST-запрос на получение рекомендаций
```http
POST http://localhost:8080/api/v1/recommendations/content-based/
Content-Type: application/json

{
  "itemId": "30624494-44fe-44d1-ab60-90dc33a4a899",
  "limit": 3
}
```

## Примечания

- Модель `mxbai-embed-large` используется для генерации эмбеддингов (1024-мерные векторы).
- Векторные эмбеддинги хранятся в поле `embedding` сущности `Item`.
- Рекомендации основаны на косинусной близости векторов.
- Для первоначальной загрузки данных можно добавить еще миграций в `db/migration`.
- Для тестирования запросов используется файл `requests.http` (поддерживается в IntelliJ IDEA).
