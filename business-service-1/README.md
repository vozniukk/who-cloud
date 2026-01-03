# Business Service 1 - Equipment & Material Management System

Микросервис для управления оборудованием, материалами и сотрудниками организации.

## 🎯 Назначение

Система предназначена для автоматизации процессов:
- Регистрации и учета оборудования/материалов
- Управления сотрудниками (до 500 человек)
- Отслеживания перемещений оборудования
- Ведения истории изменений (audit trail)
- Генерации отчетов

## 🏗️ Архитектура

### Технологический стек
- **Java**: 21 LTS
- **Spring Boot**: 3.4.1
- **Spring Security**: JWT-based authentication
- **PostgreSQL**: 16
- **Gradle**: 8.11.1

### Основные компоненты
```
business-service-1/
├── entity/          # JPA сущности
├── repository/      # Spring Data JPA репозитории
├── service/         # Бизнес-логика
├── controller/      # REST контроллеры
├── dto/             # Data Transfer Objects
├── mapper/          # Entity ↔ DTO преобразования
├── exception/       # Обработка ошибок
├── security/        # JWT фильтры и утилиты
└── config/          # Конфигурация Spring
```

## 📊 Модель данных

### Основные сущности

#### Equipment (Оборудование)
- `id` - Уникальный идентификатор
- `manufacturer` - Производитель
- `releaseDate` - Дата выпуска
- `commissioningDate` - Дата ввода в эксплуатацию
- `inventoryNumber` - Инвентарный номер (unique)
- `serialNumber` - Серийный номер (unique, optional)
- `status` - Статус (ManyToOne → ObjectStatus)
- `category` - Категория (ManyToOne → Category)
- `currentOwner` - Текущий владелец (ManyToOne → User, nullable для склада)

#### User (Сотрудник)
- `id` - Уникальный идентификатор
- `firstName`, `lastName` - Имя, фамилия
- `identificationNumber` - Идентификационный номер (unique)
- `position` - Должность
- `phone` - Телефон
- `hireDate` - Дата приема на работу
- `contractEndDate` - Дата окончания контракта (optional)
- `contractType` - Тип контракта (ManyToOne → ContractType)
- `status` - Статус (ManyToOne → UserStatus)

#### Справочники
- **Category** - Категории оборудования (с поддержкой иерархии)
- **ObjectStatus** - Статусы оборудования (WORKING, STORED, BROKEN, WRITTEN_OFF)
- **UserStatus** - Статусы пользователей (ACTIVE, INACTIVE)
- **ContractType** - Типы контрактов (PERMANENT, TEMPORARY, CONTRACT, INTERN)

#### История
- **EquipmentHistory** - История действий над оборудованием
- **UserHistory** - История изменений пользователей

## 🔌 API Endpoints

### Equipment Management

#### Создание оборудования
```http
POST /api/equipment
Content-Type: application/json
Authorization: Bearer {token}

{
  "manufacturer": "Dell",
  "releaseDate": "2023-01-01",
  "commissioningDate": "2023-01-15",
  "statusId": 1,
  "currentOwnerId": 2,
  "inventoryNumber": "INV-001",
  "serialNumber": "SN-12345",
  "categoryId": 1
}
```

#### Обновление оборудования
```http
PUT /api/equipment/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "manufacturer": "Dell",
  "releaseDate": "2023-01-01",
  "commissioningDate": "2023-01-15",
  "statusId": 1,
  "currentOwnerId": 2,
  "inventoryNumber": "INV-001",
  "serialNumber": "SN-12345",
  "categoryId": 1
}
```

#### Получение оборудования
```http
GET /api/equipment/{id}
Authorization: Bearer {token}
```

#### Список оборудования с фильтрами
```http
GET /api/equipment?categoryId=1&statusId=2&ownerId=3&page=0&size=20&sortBy=inventoryNumber&sortDirection=ASC
Authorization: Bearer {token}
```

Параметры:
- `categoryId` (optional) - Фильтр по категории
- `statusId` (optional) - Фильтр по статусу
- `ownerId` (optional) - Фильтр по владельцу
- `page` (default: 0) - Номер страницы
- `size` (default: 20) - Размер страницы
- `sortBy` (default: id) - Поле для сортировки
- `sortDirection` (default: ASC) - Направление сортировки

#### Поиск оборудования
```http
GET /api/equipment/search?query=laptop&page=0&size=20
Authorization: Bearer {token}
```

Поиск по полям: `inventoryNumber`, `serialNumber`, `manufacturer`, `categoryName`

#### Перемещение оборудования
```http
POST /api/equipment/{id}/transfer
Content-Type: application/json
Authorization: Bearer {token}

{
  "toUserId": 5,
  "details": "Transfer to new employee"
}
```

Примечание: `toUserId` может быть `null` для перемещения на склад.

#### История оборудования
```http
GET /api/equipment/{id}/history
Authorization: Bearer {token}
```

Возвращает полную историю действий: создание, обновление, перемещение, изменение статуса.

### Валидация

#### Equipment Request
- `manufacturer` - обязательное, max 255 символов
- `releaseDate` - обязательное, не может быть в будущем
- `commissioningDate` - обязательное, должна быть >= releaseDate
- `inventoryNumber` - обязательное, уникальное, max 50 символов
- `serialNumber` - уникальное (если указан), max 50 символов
- `statusId` - обязательное
- `categoryId` - обязательное

#### User Request
- `firstName`, `lastName` - обязательные, max 100 символов
- `identificationNumber` - обязательное, уникальное, max 50 символов
- `phone` - pattern: `^\\+?[0-9]{10,20}$`
- `hireDate` - обязательное, не может быть в будущем
- `contractEndDate` - должна быть в будущем (если указана)

## 🔒 Безопасность

### JWT Authentication

Сервис использует JWT токены от `auth-service`:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

JWT payload содержит:
- `sub` - username
- `userId` - ID пользователя
- `roles` - список ролей (USER, ADMIN, etc.)
- `exp` - время истечения токена

### Роли и доступ

- **USER** - базовый доступ к просмотру и редактированию оборудования
- **ADMIN** - полный доступ + управление справочниками

## 📝 Бизнес-логика

### Каскадные операции

**При деактивации пользователя (status = INACTIVE):**
- Все оборудование пользователя автоматически перемещается на склад (currentOwner = null)
- Создаются записи в истории для каждого перемещения

**При перемещении оборудования:**
- Проверяется активность целевого пользователя
- Нельзя передать оборудование неактивному пользователю
- Автоматически создается запись в `equipment_history`

### История изменений (Audit Trail)

Все операции логируются с записью:
- Типа действия (CREATED, UPDATED, TRANSFERRED, STATUS_CHANGED)
- Временной метки
- ID пользователя, выполнившего действие
- IP-адреса
- Подробностей изменения

## 🗄️ База данных

### Подключение

По умолчанию используется PostgreSQL:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/whocloud
    username: whocloud
    password: whocloud123
```

### Инициализация данных

При первом запуске выполняется `data.sql`:
- Создаются базовые справочники (статусы, типы контрактов, категории)
- Добавляются тестовые пользователи
- Регистрируется демо-оборудование

## 🚀 Запуск

### Требования
- Java 21 LTS
- PostgreSQL 16
- Gradle 8.11+

### Локальный запуск

```bash
# Сборка
./gradlew :business-service-1:build

# Запуск
./gradlew :business-service-1:bootRun
```

### Docker

```bash
docker-compose up business-service-1
```

Сервис будет доступен на `http://localhost:8085`

### Конфигурация

Основные параметры в `application.yml`:

```yaml
server:
  port: 8085

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:whocloud}
    username: ${DB_USER:whocloud}
    password: ${DB_PASSWORD:whocloud123}
  
  jpa:
    hibernate:
      ddl-auto: update  # Для production использовать 'validate'

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret}
  expiration: 86400000  # 24 hours

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Переменные окружения

- `DB_HOST` - хост БД (default: localhost)
- `DB_PORT` - порт БД (default: 5432)
- `DB_NAME` - имя БД (default: whocloud)
- `DB_USER` - пользователь БД
- `DB_PASSWORD` - пароль БД
- `JWT_SECRET` - секретный ключ для JWT (минимум 256 бит)
- `LOG_LEVEL` - уровень логирования (default: INFO)

## 📊 Мониторинг

### Actuator Endpoints

- `/actuator/health` - статус здоровья приложения
- `/actuator/info` - информация о приложении
- `/actuator/metrics` - метрики приложения
- `/actuator/prometheus` - метрики для Prometheus

## 🧪 Тестирование

```bash
# Unit тесты
./gradlew :business-service-1:test

# Интеграционные тесты
./gradlew :business-service-1:integrationTest
```

## 📚 Дополнительная документация

- [Техническое задание](TZ.md) - полное ТЗ на систему
- [Архитектура проекта](../README.md) - общая архитектура who-cloud
- [Authentication Flow](../AUTHENTICATION_AUTHORIZATION_FLOW.md) - процесс аутентификации

## 🔄 Интеграция с другими сервисами

- **auth-service** (8081) - аутентификация и авторизация
- **api-gateway** (8080) - точка входа, маршрутизация запросов
- **PostgreSQL** (5432) - хранилище данных

## 📝 TODO / Roadmap

- [ ] Добавить модуль отчетности (Reports)
- [ ] Реализовать экспорт в CSV/Excel/PDF
- [ ] Интеграция с Elasticsearch для полнотекстового поиска
- [ ] Интеграция с Kafka для асинхронных событий
- [ ] Добавить User Management API
- [ ] Swagger/OpenAPI документация
- [ ] WebSocket для real-time уведомлений

## 📞 Контакты

Для вопросов по развертыванию и разработке: см. [CONTRIBUTING.md](../CONTRIBUTING.md)
