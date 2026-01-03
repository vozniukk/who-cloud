# Business Service 1 - Статус разработки

## ✅ Что реализовано

### 1. Структура проекта
```
business-service-1/
├── entity/          ✅ 8 JPA сущностей
├── repository/      ✅ 8 Spring Data JPA репозиториев
├── dto/             ✅ Request/Response DTO с валидацией
├── mapper/          ✅ Entity ↔ DTO мапперы
├── service/         ✅ EquipmentService с полной бизнес-логикой
├── controller/      ✅ EquipmentController с REST API
├── exception/       ✅ Глобальная обработка ошибок
├── security/        ✅ JWT аутентификация
└── config/          ✅ Spring Security конфигурация
```

### 2. Основные компоненты

#### JPA Сущности (entity/)
- ✅ `Equipment.java` - Оборудование
- ✅ `Custodian.java` - Материально-ответственные лица (локальные аккаунты, связаны с auth-service через authUserId)
- ✅ `Category.java` - Категории (с иерархией)
- ✅ `ObjectStatus.java` - Статусы оборудования
- ✅ `CustodianStatus.java` - Статусы материально-ответственных
- ✅ `ContractType.java` - Типы контрактов
- ✅ `EquipmentHistory.java` - История оборудования
- ✅ `CustodianHistory.java` - История материально-ответственных

#### Репозитории (repository/)
- ✅ Все 8 репозиториев с расширенными запросами
- ✅ Поддержка фильтрации и поиска
- ✅ Пагинация и сортировка

#### DTO (dto/)
- ✅ `EquipmentRequest` / `EquipmentResponse`
- ✅ `CustodianRequest` / `CustodianResponse` (с полем authUserId для связи с auth-service)
- ✅ `TransferEquipmentRequest`
- ✅ `ReferenceRequest` / `ReferenceResponse`
- ✅ `HistoryResponse`
- ✅ Полная валидация с jakarta.validation

#### Сервисы (service/)
- ✅ `EquipmentService.java` - Полная реализация:
  - CRUD операции
  - Валидация уникальности
  - Механизм перемещения
  - Ведение истории
  - Каскадные операции
- ✅ `CustodianService.java` - Управление материально-ответственными:
  - CRUD операции
  - Поиск по authUserId для связи с auth-service
  - Валидация уникальности authUserId
  - Деактивация с автоматическим перемещением оборудования на склад
  - Полная история изменений
- ✅ `CategoryService.java` - Управление категориями:
  - CRUD операции с иерархией
  - Получение корневых категорий и подкатегорий
  - Построение дерева категорий
  - Валидация циклических ссылок
  - Предотвращение удаления категорий с подкатегориями
- ✅ `ReportsService.java` - Генерация отчетов:
  - Отчет по оборудованию с фильтрацией
  - Отчет по движению оборудования
  - Отчет по материально-ответственным лицам
  - Экспорт в CSV, Excel
  - Статистика по статусам оборудования

#### Контроллеры (controller/)
- ✅ `EquipmentController.java`:
  - POST /api/equipment
  - PUT /api/equipment/{id}
  - GET /api/equipment/{id}
  - GET /api/equipment (с фильтрами)
  - GET /api/equipment/search
  - POST /api/equipment/{id}/transfer
  - GET /api/equipment/{id}/history
- ✅ `CustodianController.java`:
  - POST /api/custodians
  - PUT /api/custodians/{id}
  - GET /api/custodians/{id}
  - GET /api/custodians/auth-user/{authUserId} - **связь с auth-service**
  - GET /api/custodians (с фильтрами: statusId, position)
  - GET /api/custodians/search?query=
  - DELETE /api/custodians/{id} (soft delete)
- ✅ `CategoryController.java`:
  - POST /api/categories
  - PUT /api/categories/{id}
  - GET /api/categories/{id}
  - GET /api/categories (пагинация, сортировка)
  - GET /api/categories/roots - корневые категории
  - GET /api/categories/{parentId}/children - подкатегории
  - GET /api/categories/tree - полное дерево
  - DELETE /api/categories/{id}
- ✅ `ReportsController.java`:
  - GET /api/reports/equipment - отчет по оборудованию (JSON)
  - GET /api/reports/equipment/export/csv - экспорт в CSV
  - GET /api/reports/equipment/export/excel - экспорт в Excel
  - GET /api/reports/movement - отчет по движению
  - GET /api/reports/movement/export/csv - экспорт движения в CSV
  - GET /api/reports/custodians - отчет по МОЛ
  - GET /api/reports/custodians/export/excel - экспорт МОЛ в Excel
  - GET /api/reports/dashboard - статистика для дашборда

#### Безопасность (security/, config/)
- ✅ `SecurityConfig.java` - Spring Security
- ✅ `JwtUtil.java` - JWT утилиты
- ✅ `JwtAuthenticationFilter.java` - JWT фильтр

#### Обработка ошибок (exception/)
- ✅ `GlobalExceptionHandler.java`
- ✅ Кастомные исключения
- ✅ `ErrorResponse.java`

### 3. Конфигурация

#### build.gradle.kts
- ✅ Spring Boot 3.4.1
- ✅ Spring Data JPA
- ✅ Spring Security
- ✅ PostgreSQL
- ✅ JWT (JJWT 0.12.6)
- ✅ Lombok
- ✅ Apache POI (Excel/CSV)
- ✅ iText (PDF)

#### application.yml
- ✅ PostgreSQL подключение
- ✅ JPA настройки
- ✅ JWT конфигурация
- ✅ Actuator endpoints

#### data.sql
- ✅ Начальные данные для справочников
- ✅ Тестовые пользователи
- ✅ Демо-оборудование

## 🔧 Решение проблемы компиляции

### Проблема
Lombok не генерирует геттеры/сеттеры в IDE (IntelliJ IDEA/VS Code).

### Решение

1. **Для IntelliJ IDEA:**
```bash
# 1. Установите Lombok Plugin
File → Settings → Plugins → Search "Lombok" → Install

# 2. Включите annotation processing
File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
☑ Enable annotation processing

# 3. Пересоберите проект
Build → Rebuild Project
```

2. **Для VS Code:**
```bash
# 1. Установите Extension Pack for Java
# 2. Обновите Java Language Support
# 3. Перезагрузите окно: Ctrl+Shift+P → "Reload Window"
```

3. **Gradle сборка (работает всегда):**
```bash
# Очистка + сборка
.\gradlew clean :business-service-1:build -x test

# Только компиляция
.\gradlew :business-service-1:compileJava

# Запуск
.\gradlew :business-service-1:bootRun
```

### Важно!
Gradle сборка работает корректно - Lombok правильно настроен в `build.gradle.kts`:
```kotlin
compileOnly("org.projectlombok:lombok")
annotationProcessor("org.projectlombok:lombok")
```

Ошибки в IDE - это проблема индексации, не реальные ошибки компиляции.

## 🚀 Как запустить

### Вариант 1: Через Gradle (рекомендуется)

```bash
# 1. Убедитесь что PostgreSQL запущен
docker-compose up -d postgres

# 2. Соберите проект
.\gradlew :business-service-1:build -x test

# 3. Запустите
.\gradlew :business-service-1:bootRun
```

### Вариант 2: Через Docker Compose

```bash
# Запустить весь стек
docker-compose up -d

# Или только business-service-1
docker-compose up -d postgres business-service-1
```

**ВАЖНО:** Docker Compose конфигурация обновлена с переменными окружения:
- `DB_HOST=postgres` - имя сервиса PostgreSQL в Docker сети
- `DB_PORT=5432`
- `DB_NAME=whocloud`
- `DB_USER=whocloud`
- `DB_PASSWORD=whocloud123`
- `JWT_SECRET` - секрет для JWT токенов
- `depends_on: postgres` - ожидание запуска БД

### Проверка работы

```bash
# Health check
Invoke-RestMethod http://localhost:8085/actuator/health

# Список endpoints
Invoke-RestMethod http://localhost:8085/actuator

# Доступные endpoints:
# - http://localhost:8085/actuator/health
# - http://localhost:8085/actuator/info
# - http://localhost:8085/actuator/metrics
# - http://localhost:8085/actuator/metrics/{metricName}

# С JWT токеном (получите от auth-service)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8085/api/equipment
```

## 📝 Что делать дальше

### Архитектурное решение: User vs Custodian

**Проблема:** Конфликт именования между глобальным User (auth-service) и локальным User (business-service-1).

**Решение:** 
- Глобальная аутентификация: `User` в **auth-service** (email: kvoznjuk@gmail.com, ID=123)
- Локальная материальная ответственность: `Custodian` в **business-service-1** (username: vozniukk, authUserId=123)

**Связь:**
```json
// GET /api/custodians/auth-user/123
{
  "id": 45,
  "authUserId": 123,  // ссылка на User из auth-service
  "firstName": "Kostiantyn",
  "lastName": "Vozniuk",
  "identificationNumber": "EMP-KV-001",
  "position": "IT Manager"
}
```

Это позволяет:
- Один глобальный User может иметь разные локальные роли в разных микросервисах
- business-service-1 управляет материальной ответственностью независимо от auth-service
- Простая интеграция через authUserId field

### Высокий приоритет
1. ✅ Equipment API - **ГОТОВО**
2. ✅ Custodian Management API - **ГОТОВО** (переименовано с User для избежания конфликта с auth-service)
   - Создан CustodianService + CustodianController
   - Поле `authUserId` для связи с глобальным User из auth-service
   - API endpoint: `GET /api/custodians/auth-user/{authUserId}` для получения локального Custodian по ID глобального User
3. ✅ Categories API - **ГОТОВО**
   - Создан CategoryService + CategoryController
   - Поддержка иерархии (parent-child)
   - Endpoints для дерева категорий и подкатегорий
   - Валидация циклических ссылок и защита от удаления родителей с детьми
4. ✅ Reports Module - **ГОТОВО**
   - Создан ReportsService + ReportsController
   - Отчеты: оборудование, движение, МОЛ
   - Экспорт в CSV, Excel
   - Фильтрация по датам, категориям, статусам, МОЛ

### Средний приоритет
5. ⏳ Export/Import - CSV, Excel, PDF
6. ⏳ Swagger/OpenAPI документация
7. ⏳ Unit тесты

### Низкий приоритет
8. ⏳ Elasticsearch интеграция
9. ⏳ Kafka интеграция
10. ⏳ WebSockets для уведомлений

## 📚 API Примеры

### Создание оборудования
```bash
curl -X POST http://localhost:8085/api/equipment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT" \
  -d '{
    "manufacturer": "Dell",
    "releaseDate": "2023-01-01",
    "commissioningDate": "2023-01-15",
    "statusId": 1,
    "currentOwnerId": 2,
    "inventoryNumber": "INV-NEW-001",
    "serialNumber": "SN-NEW-001",
    "categoryId": 1
  }'
```

### Поиск оборудования
```bash
curl "http://localhost:8085/api/equipment/search?query=laptop&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT"
```

### Перемещение оборудования
```bash
curl -X POST http://localhost:8085/api/equipment/1/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT" \
  -d '{
    "toUserId": 3,
    "details": "Transferred to new employee"
  }'
```

### Получение дерева категорий
```bash
# Получить полное дерево категорий
curl "http://localhost:8085/api/categories/tree" \
  -H "Authorization: Bearer YOUR_JWT"

# Результат:
[
  {
    "id": 1,
    "name": "COMPUTERS",
    "translations": {"en": "Computers", "ru": "Компьютеры", "uk": "Комп'ютери"},
    "children": [
      {
        "id": 8,
        "name": "DESKTOP",
        "translations": {"en": "Desktop Computers", "ru": "Настольные компьютеры"},
        "children": []
      },
      {
        "id": 9,
        "name": "LAPTOP",
        "translations": {"en": "Laptops", "ru": "Ноутбуки"},
        "children": []
      }
    ]
  }
]
```

### Создание категории с родителем
```bash
curl -X POST http://localhost:8085/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT" \
  -d '{
    "name": "GAMING_LAPTOP",
    "parentId": 9,
    "translations": {
      "en": "Gaming Laptops",
      "ru": "Игровые ноутбуки",
      "uk": "Ігрові ноутбуки"
    }
  }'
```

### Получение подкатегорий
```bash
# Получить все подкатегории категории COMPUTERS (id=1)
curl "http://localhost:8085/api/categories/1/children" \
  -H "Authorization: Bearer YOUR_JWT"
```

### Отчет по оборудованию с фильтрацией
```bash
# Получить отчет по оборудованию с фильтрами (JSON)
curl "http://localhost:8085/api/reports/equipment?categoryIds=1,2&statusIds=1&dateFrom=2024-01-01&dateTo=2024-12-31" \
  -H "Authorization: Bearer YOUR_JWT"

# Экспорт в CSV
curl "http://localhost:8085/api/reports/equipment/export/csv?categoryIds=1,2" \
  -H "Authorization: Bearer YOUR_JWT" \
  -o equipment_report.csv

# Экспорт в Excel
curl "http://localhost:8085/api/reports/equipment/export/excel?statusIds=1,2" \
  -H "Authorization: Bearer YOUR_JWT" \
  -o equipment_report.xlsx
```

### Отчет по движению оборудования
```bash
# Получить отчет по движению за период
curl "http://localhost:8085/api/reports/movement?dateFrom=2024-01-01&dateTo=2024-12-31" \
  -H "Authorization: Bearer YOUR_JWT"

# Экспорт движения в CSV
curl "http://localhost:8085/api/reports/movement/export/csv?custodianIds=2,3" \
  -H "Authorization: Bearer YOUR_JWT" \
  -o movement_report.csv
```

### Отчет по материально-ответственным лицам
```bash
# Получить отчет по МОЛ со статистикой оборудования
curl "http://localhost:8085/api/reports/custodians" \
  -H "Authorization: Bearer YOUR_JWT"

# Результат:
[
  {
    "id": 2,
    "authUserId": 123,
    "firstName": "Kostiantyn",
    "lastName": "Vozniuk",
    "identificationNumber": "EMP-KV-001",
    "position": "IT Manager",
    "statusName": "ACTIVE",
    "equipmentCount": 15,
    "activeEquipmentCount": 12,
    "inRepairEquipmentCount": 2,
    "retiredEquipmentCount": 1
  }
]

# Экспорт в Excel
curl "http://localhost:8085/api/reports/custodians/export/excel" \
  -H "Authorization: Bearer YOUR_JWT" \
  -o custodians_report.xlsx
```

## 🐛 Известные проблемы

### ✅ РЕШЕНО: Actuator не работал в Docker
**Проблема:** После `docker-compose up business-service-1` actuator возвращал 404.

**Причина:** В Docker Compose не были указаны переменные окружения для подключения к PostgreSQL. Сервис не мог запуститься и actuator не инициализировался.

**Решение:** Добавлены переменные окружения в docker-compose.yml:
```yaml
environment:
  - DB_HOST=postgres
  - DB_PORT=5432
  - DB_NAME=whocloud
  - DB_USER=whocloud
  - DB_PASSWORD=whocloud123
  - JWT_SECRET=your-256-bit-secret-key-change-this-in-production
depends_on:
  - postgres
```

### Текущие ограничения

1. **IDE показывает ошибки Lombok** - это нормально, Gradle компилирует корректно
2. **Placeholder currentUserId** - нужно получать из SecurityContext после интеграции с auth-service
3. **User/Categories/Reports API** - еще не реализованы
4. **Prometheus endpoint** - требует дополнительной зависимости `micrometer-registry-prometheus` (опционально)

## ✅ Итог

**Микросервис готов к запуску и тестированию Equipment API!**

Остальные модули можно добавлять по аналогии с Equipment:
1. Создать Service (копировать структуру EquipmentService)
2. Создать Controller (копировать структуру EquipmentController)
3. Добавить специфичную бизнес-логику

Все основы заложены: Entity, Repository, DTO, Security, Exception handling.
