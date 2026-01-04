# Spring Boot Authentication Boilerplate

A **production-ready Spring Boot authentication system** with multiple authentication methods, multi-channel notifications, and JWT-based security.

---

## Features

### Authentication Methods
- **Google OAuth 2.0** - Sign in with Google
- **Email OTP** - 6-digit code sent to email (5 min expiry)
- **Magic Link** - Passwordless email link authentication (10 min expiry)

### Security
- JWT Access Tokens (15 min expiry)
- JWT Refresh Tokens (7 days expiry, stored in Redis)
- Token rotation on refresh
- Stateless session management
- Role-based access control (ROLE_USER, ROLE_ADMIN)

### Notification Service
- Multi-channel support (Email, SMS, WhatsApp, Push, In-App)
- Template-based content with Thymeleaf
- RabbitMQ async processing
- Rate limiting per channel
- Exponential backoff retry mechanism
- Dead Letter Queue for failed messages

### Infrastructure
- PostgreSQL database
- Redis for caching and token storage
- RabbitMQ for message queuing
- Docker Compose for local development
- Swagger/OpenAPI documentation
- Prometheus + Grafana for metrics
- Elasticsearch + Kibana for logs

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration Guide](#configuration-guide)
- [API Endpoints](#api-endpoints)
- [Environment Variables](#environment-variables)
- [Getting Service Credentials](#getting-service-credentials)
- [Ports & Services Reference](#ports--services-reference)
- [Documentation Links](#documentation-links)

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Framework | Spring Boot 3.3.6 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Message Queue | RabbitMQ 3.13 |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| OAuth | Google API Client |
| SMS/WhatsApp | Twilio |
| Push Notifications | Firebase FCM |
| Email | Spring Mail (Gmail SMTP) |
| Documentation | SpringDoc OpenAPI |
| Build Tool | Maven |
| Containerization | Docker |
| Monitoring | Prometheus, Grafana |
| Logging | Elasticsearch, Kibana, Logback |

---

## Project Structure

```
src/main/java/com/example/
├── common/
│   ├── dto/                    # API response DTOs
│   └── exception/              # Global exceptions
├── config/                     # Configuration classes
│   └── notification/           # Notification-specific configs
├── notification/               # Notification service module
│   ├── controller/
│   ├── model/
│   ├── provider/               # Channel providers (Email, SMS, etc.)
│   ├── publisher/              # RabbitMQ publishers
│   ├── service/
│   ├── template/
│   └── worker/                 # RabbitMQ consumers
├── security/
│   ├── auth/                   # Generic auth controller
│   ├── config/                 # Security configuration
│   ├── dto/                    # Auth request/response DTOs
│   ├── emailotp/               # Email OTP authentication
│   ├── exception/              # Security exceptions
│   ├── google/                 # Google OAuth authentication
│   ├── jwt/                    # JWT token handling
│   ├── magiclink/              # Magic link authentication
│   ├── principal/              # Security user details
│   ├── role/                   # User roles enum
│   └── store/                  # Redis stores (OTP, MagicLink)
├── user/
│   ├── controller/
│   ├── entity/
│   ├── repository/
│   └── service/
└── utils/                      # Utility classes

src/main/resources/
├── templates/notifications/    # Email/SMS templates
├── application.yaml            # Main configuration
└── logback-spring.xml          # Logging configuration

docs/
├── NOTIFICATION_SERVICE.md     # Notification system guide
└── FRONTEND_GUIDE.md           # Mobile/Web integration guide
```

---

## Prerequisites

**Required:**
- Java 21 or higher
- Maven 3.8+
- Docker & Docker Compose
- Git

**Optional but recommended:**
- Docker Desktop UI
- lazydocker (terminal Docker UI)
- Postman / Insomnia for API testing

**Verify Installation:**
```bash
java --version    # Should be 21+
mvn --version     # Should be 3.8+
docker --version
docker compose version
```

---

## Quick Start

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd springboot-auth-boilerplate
```

### Step 2: Start Infrastructure Services

```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port `5432`
- PgAdmin on port `5050`
- Redis on port `6379`
- RabbitMQ on ports `5672` (AMQP) and `15672` (Management UI)

### Step 3: Create Environment File

```bash
cp .env.example .env
```

Edit `.env` with your credentials (see [Configuration Guide](#configuration-guide)).

### Step 4: Run the Application

**On Mac/Linux:**
```bash
./mvnw spring-boot:run
```

**On Windows:**
```bash
mvnw.cmd spring-boot:run
```

### Step 5: Access the Application

| Service | URL | Credentials |
|---------|-----|-------------|
| API | http://localhost:8080 | - |
| Swagger UI | http://localhost:8080/api/docs/swagger-ui | - |
| PgAdmin | http://localhost:5050 | admin@admin.com / admin |
| RabbitMQ | http://localhost:15672 | guest / guest |

---

## Configuration Guide

### application.yaml - What You Need to Configure

#### 1. JWT Secrets (REQUIRED)

```yaml
jwt:
  secret: ${APP_JWT_SECRET}           # Generate a secure 256-bit key
  refresh-secret: ${APP_JWT_REFRESH_SECRET}  # Generate another secure key
```

#### 2. Google OAuth (REQUIRED for Google login)

```yaml
google:
  client-id: ${GOOGLE_AUTH_CLIENT_ID}
```

#### 3. Email/SMTP (REQUIRED for OTP/Magic Link)

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: "your-app-password"  # Gmail App Password, NOT regular password
```

#### 4. Magic Link Base URL (REQUIRED for Magic Link)

```yaml
magic-link:
  base-url: ${MAGIC_LINK_BASE_URL:http://localhost:3000/auth/magic-link/verify}
```

#### 5. Twilio (OPTIONAL - for SMS/WhatsApp)

```yaml
notification:
  twilio:
    enabled: ${TWILIO_ENABLED:false}
    account-sid: ${TWILIO_ACCOUNT_SID:}
    auth-token: ${TWILIO_AUTH_TOKEN:}
    from-number: ${TWILIO_FROM_NUMBER:}
    whatsapp-number: ${TWILIO_WHATSAPP_NUMBER:}
```

#### 6. Firebase (OPTIONAL - for Push Notifications)

```yaml
notification:
  firebase:
    enabled: ${FIREBASE_ENABLED:false}
    service-account-path: ${FIREBASE_SERVICE_ACCOUNT_PATH:}
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/google/login` | Login with Google ID token | Public |
| POST | `/api/auth/email/otp/send` | Send OTP to email | Public |
| POST | `/api/auth/email/otp/verify` | Verify OTP and get tokens | Public |
| POST | `/api/auth/magic-link/send` | Send magic link to email | Public |
| GET | `/api/auth/magic-link/verify?token=xxx` | Verify magic link | Public |
| POST | `/api/auth/refresh` | Refresh access token | Public |
| POST | `/api/auth/logout` | Logout (invalidate refresh token) | Public |

### User

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/users` | Get current user info | Bearer Token |

### Notifications

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/notifications` | Get user notifications | Bearer Token |
| GET | `/api/notifications/unread` | Get unread notifications | Bearer Token |
| GET | `/api/notifications/unread/count` | Get unread count | Bearer Token |
| PUT | `/api/notifications/{id}/read` | Mark as read | Bearer Token |
| PUT | `/api/notifications/read-all` | Mark all as read | Bearer Token |

### Documentation

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/docs/swagger-ui` | Swagger UI |
| GET | `/api/docs/schema` | OpenAPI JSON schema |

---

## Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `APP_SERVER_PORT` | Application port | `8080` |
| `DATASOURCE_HOST` | PostgreSQL host | `localhost` |
| `DATASOURCE_PORT` | PostgreSQL port | `5432` |
| `DATASOURCE_NAME` | Database name | `authdb` |
| `DATASOURCE_USERNAME` | Database user | `postgres` |
| `DATASOURCE_PASSWORD` | Database password | `postgres` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `APP_JWT_SECRET` | JWT signing key | (generate secure key) |
| `APP_JWT_REFRESH_SECRET` | Refresh token key | (generate secure key) |
| `GOOGLE_AUTH_CLIENT_ID` | Google OAuth client ID | `xxx.apps.googleusercontent.com` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `MAGIC_LINK_BASE_URL` | Frontend magic link URL | `http://localhost:3000/auth/magic-link/verify` |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_USERNAME` | RabbitMQ user | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |
| `TWILIO_ENABLED` | Enable Twilio | `false` |
| `TWILIO_ACCOUNT_SID` | Twilio Account SID | - |
| `TWILIO_AUTH_TOKEN` | Twilio Auth Token | - |
| `TWILIO_FROM_NUMBER` | Twilio phone number | - |
| `TWILIO_WHATSAPP_NUMBER` | Twilio WhatsApp number | - |
| `FIREBASE_ENABLED` | Enable Firebase | `false` |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | Firebase JSON path | - |

---

## Getting Service Credentials

### 1. JWT Secrets (REQUIRED)

Generate secure random keys for signing tokens:

**Using OpenSSL:**
```bash
# Generate for APP_JWT_SECRET
openssl rand -base64 32

# Generate for APP_JWT_REFRESH_SECRET (use a different key)
openssl rand -base64 32
```

**Using Online Tool:**
- Visit https://generate-random.org/api-key-generator
- Generate two 256-bit keys

---

### 2. Google OAuth Client ID (REQUIRED for Google Login)

**Step-by-step:**

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the **Google+ API** (APIs & Services > Library)
4. Navigate to **APIs & Services** > **Credentials**
5. Click **Create Credentials** > **OAuth 2.0 Client IDs**
6. Configure the consent screen if prompted:
   - User Type: External
   - App name: Your App Name
   - User support email: your email
   - Developer contact email: your email
7. Create OAuth Client ID:
   - Application type: **Web application**
   - Name: Your App Name
   - Authorized JavaScript origins:
     ```
     http://localhost:3000
     https://yourdomain.com
     ```
   - Authorized redirect URIs:
     ```
     http://localhost:3000/auth/callback
     https://yourdomain.com/auth/callback
     ```
8. Click **Create**
9. Copy the **Client ID** (looks like: `xxxx.apps.googleusercontent.com`)

---

### 3. Gmail App Password (REQUIRED for Email OTP/Magic Link)

**Step-by-step:**

1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Under "Signing in to Google", enable **2-Step Verification** if not already
3. After enabling 2FA, go back to Security page
4. Click on **2-Step Verification**
5. Scroll down and click on **App passwords**
6. Select app: **Mail**
7. Select device: **Other (Custom name)** → Enter "Spring Boot App"
8. Click **Generate**
9. Copy the **16-character password** (format: `xxxx xxxx xxxx xxxx`)
10. Use this password in your `.env` file (remove spaces):
    ```
    # In application.yaml or .env
    spring.mail.password=xxxxxxxxxxxxxxxx
    ```

**Important Notes:**
- This is NOT your regular Gmail password
- You must have 2FA enabled to generate app passwords
- If you don't see "App passwords", ensure 2FA is enabled

---

### 4. Twilio Credentials (OPTIONAL - for SMS/WhatsApp)

**Step-by-step:**

1. Sign up at [Twilio](https://www.twilio.com/try-twilio)
2. After sign up, go to Console Dashboard
3. Find and copy:
   - **Account SID** (starts with `AC`)
   - **Auth Token** (click to reveal)
4. Get a phone number:
   - Go to **Phone Numbers** > **Manage** > **Buy a number**
   - Choose a number with SMS capability
   - Copy the number (format: `+1234567890`)
5. For WhatsApp (optional):
   - Go to **Messaging** > **Try it out** > **Send a WhatsApp message**
   - Follow the sandbox setup instructions
   - Note the WhatsApp number provided

```env
TWILIO_ENABLED=true
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_FROM_NUMBER=+1234567890
TWILIO_WHATSAPP_NUMBER=+14155238886
```

---

### 5. Firebase Credentials (OPTIONAL - for Push Notifications)

**Step-by-step:**

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project** or select existing
3. Navigate to **Project Settings** (gear icon)
4. Go to **Service Accounts** tab
5. Click **Generate new private key**
6. Save the downloaded JSON file securely (e.g., `firebase-service-account.json`)
7. Set the path in your environment:

```env
FIREBASE_ENABLED=true
FIREBASE_SERVICE_ACCOUNT_PATH=/path/to/firebase-service-account.json
```

**For Development:**
- Place the file in `src/main/resources/`
- Set path: `classpath:firebase-service-account.json`

**For Production:**
- Store securely outside the repository
- Use absolute path or environment-specific configuration

---

## Ports & Services Reference

| Service | Purpose | Container Name | Port | Access URL |
|---------|---------|----------------|------|------------|
| Auth Service | Spring Boot backend | auth-service | 8080 | http://localhost:8080 |
| PostgreSQL | Primary database | auth-postgres | 5432 | - |
| pgAdmin | DB management UI | auth-pgadmin | 5050 | http://localhost:5050 |
| Redis | Cache / Token storage | auth-redis | 6379 | - |
| RabbitMQ | Message queue | auth-rabbitmq | 5672, 15672 | http://localhost:15672 |
| Prometheus | Metrics collection | auth-prometheus | 9090 | http://localhost:9090 |
| Grafana | Metrics dashboards | auth-grafana | 3001 | http://localhost:3001 |
| Elasticsearch | Log storage | auth-elasticsearch | 9200 | - |
| Kibana | Log visualization | auth-kibana | 5601 | http://localhost:5601 |

---

## Common Commands

**Start services:**
```bash
docker-compose up -d
```

**Stop services:**
```bash
docker-compose down
```

**View logs:**
```bash
docker-compose logs -f auth-rabbitmq
```

**Run application:**
```bash
./mvnw spring-boot:run
```

**Build JAR:**
```bash
./mvnw clean package -DskipTests
```

**Run JAR:**
```bash
java -jar target/springboot-auth-boilerplate-0.0.1-SNAPSHOT.jar
```

---

## Troubleshooting

### Common Issues

1. **Database connection failed**
   - Ensure PostgreSQL container is running: `docker ps`
   - Check credentials in `.env` file

2. **Redis connection refused**
   - Ensure Redis container is running
   - Check Redis host and port configuration

3. **Email not sending**
   - Verify Gmail App Password (not regular password)
   - Ensure 2FA is enabled on Gmail account
   - Check SMTP settings

4. **Google OAuth failing**
   - Verify Client ID is correct
   - Check authorized origins include your frontend URL
   - Ensure frontend sends correct ID token

5. **RabbitMQ connection refused**
   - Ensure RabbitMQ container is running
   - Check credentials (default: guest/guest)

---

## Documentation Links

- [Notification Service Guide](docs/NOTIFICATION_SERVICE.md) - How to use and extend the notification system
- [Frontend Integration Guide](docs/FRONTEND_GUIDE.md) - Mobile/Web frontend development guide
- [Swagger UI](http://localhost:8080/api/docs/swagger-ui) - Interactive API documentation

---

## License

This project is licensed under the MIT License.

---

## Author

**Ajay Kathwate**
[Email](mailto:ajaykathwatee@gmail.com) | [LinkedIn](https://www.linkedin.com/in/ajay-kathwate/)
