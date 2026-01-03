# Testing Guide - Frontend Authentication Implementation

## Дата: 3 января 2026

## ⚠️ Перед тестированием

### 1. Проверить статус компиляции auth-service

```powershell
cd C:\Users\kostiantyn_vozniuk\Documents\GitHub\who-cloud
.\gradlew.bat :auth-service:build -x test
```

**Ожидаемый результат:** `BUILD SUCCESSFUL`

### 2. Перезапустить auth-service

```powershell
# Если запущен через docker-compose
docker-compose restart auth-service

# Если запущен вручную - остановить старый процесс и запустить:
cd auth-service
java -jar build/libs/auth-service-*.jar
```

### 3. Запустить frontend-service

```powershell
cd C:\Users\kostiantyn_vozniuk\Documents\GitHub\who-cloud\frontend-service
npm install  # если первый запуск
npm run dev
```

**Ожидаемый вывод:**
```
> frontend-service@0.1.0 dev
> next dev

  ▲ Next.js 14.2.0
  - Local:        http://localhost:3000
  - Ready in 2.3s
```

---

## 🧪 Тестовые сценарии

### Тест 1: Главная страница (Public)

**URL:** http://localhost:3000/

**Ожидаемое поведение:**
- ✅ Отображается публичная landing page
- ✅ Кнопка "🔐 Sign In with Google" видна
- ✅ Показывается статистика базы данных
- ✅ Если НЕ авторизован - остается на главной

**Проверка:**
```powershell
# Открыть в браузере
Start-Process "http://localhost:3000/"
```

---

### Тест 2: OAuth Login Flow

**Шаги:**
1. На главной странице нажать "🔐 Sign In with Google"
2. Должен произойти редирект на Google OAuth
3. После авторизации в Google
4. **ВАЖНО: Должен произойти редирект на `http://localhost:3000/auth/callback?token=...`**

**Ожидаемое поведение:**

**🔴 СТАРОЕ (неправильное) поведение:**
```
→ Показывается HTML страница от auth-service с токеном
→ URL: http://localhost:8080/login/oauth2/code/google
→ Видно большую HTML форму с кнопками "Copy Token"
```

**🟢 НОВОЕ (правильное) поведение:**
```
→ Редирект на http://localhost:3000/auth/callback?token=eyJhbG...
→ Показывается красивая анимация "Authenticating..."
→ Затем "✅ Success! Welcome, [Имя]!"
→ Автоматический редирект на /dashboard через 1.5 секунды
```

**Проверка в DevTools:**
```
F12 → Network tab
Фильтр: "callback"
Должен быть GET запрос к /auth/callback?token=...
```

---

### Тест 3: Доступ к защищенным роутам БЕЗ авторизации

**Попытка 1: Открыть Dashboard без логина**
```
http://localhost:3000/dashboard
```

**Ожидаемое поведение:**
- ✅ Middleware ловит запрос
- ✅ Редирект на `http://localhost:3000/?error=auth_required`
- ✅ Показывается главная страница

**Попытка 2: Открыть Admin panel без логина**
```
http://localhost:3000/admin
```

**Ожидаемое поведение:**
- ✅ Middleware ловит запрос
- ✅ Редирект на `http://localhost:3000/?error=auth_required`

**Проверка:**
```powershell
# В браузере открыть
Start-Process "http://localhost:3000/dashboard"

# Должно редиректить на http://localhost:3000/?error=auth_required
```

---

### Тест 4: Dashboard для авторизованного пользователя

**Предусловие:** Выполнить Тест 2 (OAuth login)

**URL после логина:** http://localhost:3000/dashboard

**Ожидаемое поведение:**

1. **Sidebar видна слева:**
   - Gradient синий фон
   - Логотип "WHO Cloud"
   - Меню:
     - 🏠 Dashboard
     - 💼 Equipment (только если USER+)
     - 👥 Custodians (только если USER+)
     - 📊 Reports (только если MODERATOR+)
     - ⚙️ Admin Panel (только если ADMIN)
     - 👤 Profile
   - Внизу: User info + Logout кнопка

2. **Главная область:**
   - Приветствие зависит от роли:
     - GUEST: "👋 Welcome to WHO Cloud!"
     - USER: "✨ Welcome back!"
     - MODERATOR: "📊 Moderator Dashboard"
     - ADMIN: "⚙️ Administrator Dashboard"
   - 4 карточки статистики (Equipment, Categories, Custodians, History)
   - Quick Actions кнопки (если не GUEST)

3. **URL должен остаться:** http://localhost:3000/dashboard

**Проверка роли GUEST:**
```
→ В sidebar НЕ должно быть пунктов Equipment, Custodians, Reports
→ Должен показываться желтый alert: "Limited Access"
→ Quick Actions секция НЕ показывается
```

---

### Тест 5: Profile страница

**URL:** http://localhost:3000/dashboard/profile

**Ожидаемое поведение:**

1. **Account Information карточка:**
   - Full Name: [ваше имя из Google]
   - Email: [ваш email]
   - Role: [цветной badge с ролью]
   - Google ID: [длинный ID]

2. **OAuth Details карточка:**
   - Provider: Google
   - Account Created: [дата]
   - Last Login: [дата и время]
   - Email Verified: ✅ Yes

3. **JWT Debug Information (черная карточка):**
   - Decoded Payload: JSON с полями sub, role, exp, iat, fullName, googleId
   - Raw Token: длинная строка eyJhbGc...
   - Issued At: форматированная дата
   - Expires At: форматированная дата (через 24 часа)

4. **Permissions карточка:**
   - View Equipment: ✅ или ❌
   - Edit Equipment: ✅ или ❌
   - Generate Reports: ✅ или ❌
   - Admin Panel Access: ✅ или ❌

**Проверка в DevTools:**
```
F12 → Network → найти запрос к /api/auth/user-info
Должен быть статус 200
Response должен содержать ApiResponse с data объектом
```

**Проверка localStorage:**
```javascript
// В консоли браузера
localStorage.getItem('auth_token')
// Должен вернуть JWT токен
```

**Проверка cookies:**
```javascript
// В консоли браузера
document.cookie
// Должно содержать: auth_token=eyJhbGc...
```

---

### Тест 6: Admin Panel (только для ADMIN)

**Предусловие:** Нужна роль ADMIN

**Как получить ADMIN роль:**
```sql
-- Подключиться к PostgreSQL
psql -h localhost -U postgres -d who_cloud_db

-- Найти своего пользователя
SELECT id, username, email, role FROM users WHERE email = 'ваш_email@gmail.com';

-- Обновить роль на ADMIN
UPDATE users SET role = 'ADMIN' WHERE email = 'ваш_email@gmail.com';

-- Проверить
SELECT id, username, email, role FROM users WHERE email = 'ваш_email@gmail.com';
```

**После обновления роли:**
1. Выйти из аккаунта (Logout)
2. Залогиниться снова через Google
3. Новый JWT токен будет содержать role: "ADMIN"

**URL:** http://localhost:3000/admin

**Ожидаемое поведение:**

1. **Sidebar красная (red-800 → red-900)**
   - ⚙️ Admin Panel заголовок
   - Меню:
     - 📊 Overview
     - 👥 User Management
     - 🔐 Roles & Permissions
     - ⚙️ System Settings
     - 📜 Audit Logs
     - ← Back to Dashboard

2. **Главная область:**
   - ⚠️ Administrator Alert (красный)
   - 4 статистики (Equipment, Users, Custodians, History)
   - 6 Quick Actions кнопок
   - Recent Activity секция
   - System Health бары

**Тест защиты: USER пытается зайти в /admin**
```
1. Роль USER или GUEST
2. Открыть http://localhost:3000/admin
3. Ожидается: редирект на /dashboard?error=insufficient_permissions
```

---

### Тест 7: Logout

**Шаги:**
1. Находясь в Dashboard
2. Нажать кнопку "🚪 Logout" внизу sidebar

**Ожидаемое поведение:**
- ✅ localStorage.removeItem('auth_token') вызван
- ✅ cookie auth_token удален
- ✅ Редирект на http://localhost:3000/
- ✅ Попытка зайти на /dashboard снова редиректит на главную

**Проверка:**
```javascript
// В консоли после logout
localStorage.getItem('auth_token')
// Должно вернуть: null

document.cookie
// auth_token НЕ должен быть в списке
```

---

### Тест 8: Middleware Auto-redirect

**Сценарий:** Авторизованный пользователь открывает главную страницу

**URL:** http://localhost:3000/

**Ожидаемое поведение:**
- ✅ Middleware проверяет наличие токена в cookies
- ✅ Если токен есть и валиден
- ✅ Автоматический редирект на http://localhost:3000/dashboard

**Проверка:**
```powershell
# Убедитесь что залогинены
# Затем откройте главную
Start-Process "http://localhost:3000/"

# Должно сразу редиректить на /dashboard
```

---

## 🐛 Troubleshooting

### Проблема 1: Frontend не запускается

**Симптомы:**
```
Error: Cannot find module 'next'
```

**Решение:**
```powershell
cd frontend-service
npm install
npm run dev
```

---

### Проблема 2: OAuth редиректит на старую HTML страницу

**Симптомы:**
- После Google OAuth показывается большая HTML страница с токенами
- URL: http://localhost:8080/login/oauth2/code/google

**Причина:** auth-service не был перезапущен с новыми изменениями

**Решение:**
```powershell
# Остановить старый процесс
Stop-Process -Name java -Force

# Пересобрать
cd C:\Users\kostiantyn_vozniuk\Documents\GitHub\who-cloud
.\gradlew.bat :auth-service:clean :auth-service:build -x test

# Если через docker-compose
docker-compose restart auth-service

# Если вручную
cd auth-service
java -jar build/libs/auth-service-*.jar
```

---

### Проблема 3: 404 на /api/auth/user-info

**Симптомы:**
```
GET http://localhost:3000/api/auth/user-info 404 Not Found
```

**Причина:** nginx не проксирует запрос на auth-service

**Решение - проверить nginx.conf:**
```nginx
location /api/auth/ {
    proxy_pass http://auth-service:9001/api/auth/;
    proxy_set_header Authorization $http_authorization;
}
```

**Перезапустить nginx:**
```powershell
docker-compose restart nginx
```

---

### Проблема 4: Токен сохраняется но middleware не видит

**Симптомы:**
- localStorage содержит токен
- При переходе на /dashboard все равно редиректит на главную

**Причина:** Cookie не устанавливается правильно

**Проверка:**
```javascript
// В консоли браузера на /dashboard
document.cookie
// Должно содержать: auth_token=eyJhbGc...

// Если нет - проверить в lib/auth.ts функцию saveToken()
```

**Временное решение:**
```javascript
// Вручную установить cookie
document.cookie = `auth_token=${localStorage.getItem('auth_token')}; path=/; max-age=604800`;
// Затем обновить страницу
```

---

### Проблема 5: CORS ошибки

**Симптомы:**
```
Access to fetch at 'http://localhost:8080/api/auth/user-info' 
from origin 'http://localhost:3000' has been blocked by CORS
```

**Решение - проверить SecurityConfig в auth-service:**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

## 📋 Чеклист успешной реализации

### Frontend
- [ ] ✅ npm run dev запускает frontend на :3000
- [ ] ✅ Главная страница открывается
- [ ] ✅ Кнопка "Sign In with Google" видна
- [ ] ✅ OAuth редиректит на /auth/callback?token=...
- [ ] ✅ Callback страница показывает анимацию
- [ ] ✅ Автоматический редирект на /dashboard
- [ ] ✅ Dashboard показывает sidebar с меню
- [ ] ✅ Profile страница показывает JWT debug info
- [ ] ✅ Logout работает и очищает токены
- [ ] ✅ Middleware блокирует доступ без авторизации
- [ ] ✅ Admin panel недоступен для не-админов

### Backend
- [ ] ✅ auth-service пересобран (gradlew build)
- [ ] ✅ OAuth2LoginSuccessHandler делает redirect на frontend
- [ ] ✅ /api/auth/user-info endpoint отвечает
- [ ] ✅ JWT токен содержит role, fullName, googleId
- [ ] ✅ CORS настроен для localhost:3000

### Integration
- [ ] ✅ nginx проксирует /api/auth/ на auth-service
- [ ] ✅ nginx проксирует /login/oauth2/ на auth-service
- [ ] ✅ Токен передается в Authorization header
- [ ] ✅ Profile страница получает данные от backend

---

## 🎯 Quick Start для тестирования

**Быстрый запуск всего стека:**

```powershell
# 1. Перейти в корень проекта
cd C:\Users\kostiantyn_vozniuk\Documents\GitHub\who-cloud

# 2. Пересобрать auth-service (если еще не сделали)
.\gradlew.bat :auth-service:build -x test

# 3. Запустить через docker-compose (если используете)
docker-compose up -d

# 4. ИЛИ запустить сервисы вручную в отдельных терминалах:

# Terminal 1 - auth-service
cd auth-service
java -jar build/libs/auth-service-*.jar

# Terminal 2 - frontend
cd frontend-service
npm run dev

# Terminal 3 - nginx (если вручную)
nginx -c $(pwd)/nginx/nginx.conf

# 5. Открыть браузер
Start-Process "http://localhost:3000"
```

**Базовый тест:**
1. Открыть http://localhost:3000
2. Нажать "Sign In with Google"
3. Авторизоваться в Google
4. **Проверить:** URL должен быть http://localhost:3000/dashboard
5. **Проверить:** Должен показываться sidebar и welcome message
6. Открыть http://localhost:3000/dashboard/profile
7. **Проверить:** Должна показываться JWT Debug информация

**Если что-то не работает** - смотреть раздел Troubleshooting выше!

---

## 📸 Скриншоты ожидаемого результата

### ДО (старое поведение):
```
http://localhost:8080/login/oauth2/code/google
╔════════════════════════════════════════╗
║ ✓ Authentication Successful!           ║
║                                        ║
║ User Information:                      ║
║ Username: john.doe                     ║
║ Email: john@example.com               ║
║                                        ║
║ Access Token (JWT):                    ║
║ eyJhbGciOiJIUzI1NiIsInR5cCI...       ║
║ [Copy Access Token]                    ║
║                                        ║
║ Refresh Token:                         ║
║ a3d2e1f4-...                          ║
║ [Copy Refresh Token]                   ║
╚════════════════════════════════════════╝
```

### ПОСЛЕ (новое поведение):
```
http://localhost:3000/dashboard
╔════════════════════════════════════════════════════════════╗
║ 🏥 WHO Cloud Platform          [👤 John] [🚪 Logout]      ║
║ ════════════════════════════════════════════════════════   ║
║ ┌──────────┐                                              ║
║ │ 🏠 Dashboard │  Welcome, John Doe!                      ║
║ │ 💼 Equipment │                                          ║
║ │ 👥 Custodians│  You have User access.                   ║
║ │ 👤 Profile   │                                          ║
║ │              │  [💼 150]  [📁 25]  [👥 45]  [📜 320]   ║
║ │ [Logout]     │  Equipment Categories Custodians History ║
║ └──────────┘                                              ║
║                   Quick Actions:                           ║
║                   [➕ Add Equipment] [👤 Add Custodian]   ║
╚════════════════════════════════════════════════════════════╝
```

---

Удачного тестирования! 🚀
