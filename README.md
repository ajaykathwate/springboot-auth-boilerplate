# Spring Boot Auth Boilerplate

## 1. Project Overview

This repository provides a **production-ready Spring Boot authentication boilerplate** built with Java 21.  
It is designed as a **reusable base template** that handles all foundational concerns—configuration, infrastructure, observability, and local development—so you can focus on building business features.

**Problem it solves**
- Eliminates repetitive setup for authentication projects
- Provides a consistent, observable, and debuggable local environment
- Reduces time spent configuring Docker, databases, logging, and monitoring

**High-level architecture**
- Spring Boot application (Auth Service)
- PostgreSQL for persistence
- Redis for caching / short-lived data (OTP, tokens)
- Docker Compose for orchestration
- Prometheus + Grafana for metrics
- Elasticsearch + Kibana for logs
- JSON logging to STDOUT and file

---

## 2. Tech Stack

### Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Validation
- Spring Actuator
- Swagger (springdoc-openapi)

### Database
- PostgreSQL 16

### Cache
- Redis 7

### Observability & Monitoring
- Prometheus (metrics scraping)
- Grafana (metrics dashboards)
- Elasticsearch (log storage & search)
- Kibana (log visualization)

### Containerization
- Docker
- Docker Compose

### Build & Tooling
- Maven
- Lombok
- Logback (JSON logging)

---

## 3. Prerequisites

Required:
- Docker Desktop (includes Docker Compose)
- Git

Optional but recommended:
- Docker Desktop UI
- lazydocker (terminal Docker UI)
- pgAdmin (if not using containerized pgAdmin)
- Postman / Insomnia for API testing

Verify Docker:
```bash
docker --version
docker compose version

---

## 4. Getting Started (Local Development Setup)

### Step 1: Clone the repository

```bash
git clone <REPOSITORY_URL>
cd springboot-auth-boilerplate
```

### Step 2: Create environment file

```bash
cp .env.example .env
```

### Step 3: Configure environment variables

The application reads configuration from environment variables via Docker Compose.

All required variables are listed in `.env.example`.

---

### Step 4: Start the project

```bash
docker compose up --build
```

This command:

* Builds the application image
* Starts PostgreSQL
* Starts Redis
* Starts the Auth Service
* Attaches all services to the same Docker network

### Step 5: Stop the project

```bash
docker compose down
```

### Step 6: Rebuild containers

```bash
docker compose down
docker compose up --build
```

---

## 5. Running the Application

* The application is started using Docker Compose
* The **Dockerfile** builds a production-style image
* Containers are orchestrated via `docker-compose.yml`

Startup flow:

1. PostgreSQL container starts and becomes healthy
2. Redis container starts
3. Auth Service starts
4. Spring Boot initializes:

    * Loads configuration
    * Initializes datasource
    * Connects to Redis
    * Starts embedded Tomcat
    * Exposes Actuator and API endpoints

---

## 6. Application Access URLs

Base application:

```
http://localhost:8080
```

Health check:

```
http://localhost:8080/actuator/health
```

Swagger UI:

```
http://localhost:8080/api/docs/swagger-ui
```

---

## 7. Ports & Services Reference

| Service       | Purpose             | Container Name     | Port | Access URL                                     |
| ------------- | ------------------- | ------------------ | ---- | ---------------------------------------------- |
| Auth Service  | Spring Boot backend | auth-service       | 8080 | [http://localhost:8080](http://localhost:8080) |
| PostgreSQL    | Primary database    | auth-postgres      | 5432 | localhost:5432                                 |
| Redis         | Cache / OTP storage | auth-redis         | 6379 | localhost:6379                                 |
| Prometheus    | Metrics collection  | auth-prometheus    | 9090 | [http://localhost:9090](http://localhost:9090) |
| Grafana       | Metrics dashboards  | auth-grafana       | 3001 | [http://localhost:3001](http://localhost:3001) |
| Elasticsearch | Log storage         | auth-elasticsearch | 9200 | [http://localhost:9200](http://localhost:9200) |
| Kibana        | Log visualization   | auth-kibana        | 5601 | [http://localhost:5601](http://localhost:5601) |
| pgAdmin       | DB management UI    | auth-pgadmin       | 5050 | [http://localhost:5050](http://localhost:5050) |
| Portainer     | Docker UI           | portainer          | 9000 | [http://localhost:9000](http://localhost:9000) |

---

## 8. Observability & Monitoring

### Prometheus

* Scrapes metrics from `/actuator/prometheus`
* Stores time-series application metrics
* Access: `http://localhost:9090`

### Grafana

* Visualizes Prometheus metrics
* JVM, memory, threads, HTTP metrics
* Access: `http://localhost:3001`

### Elasticsearch & Kibana

* Elasticsearch stores logs
* Kibana provides search and visualization
* Elasticsearch: `http://localhost:9200`
* Kibana: `http://localhost:5601`

**Metrics vs Logs**

* Metrics: numeric, time-series data (latency, memory, requests)
* Logs: discrete events with context (errors, requests, traces)

---

## 9. Logs

Logs are written to:

* **STDOUT** (JSON) – primary source for containers
* **File** – `/app/logs/application.log`

### View logs via Docker

```bash
docker compose logs -f auth-service
```

### View logs via lazydocker

* Select `auth-service`
* Press `l` to view logs

### View logs via Docker Desktop

* Containers → auth-service → Logs

**Why JSON logging**

* Machine-readable
* Easy to ship to Elasticsearch
* Works with modern observability stacks

---

## 10. Database Access

### Using pgAdmin (containerized)

Access:

```
http://localhost:5050
```

Login:

* Email: `admin@admin.com`
* Password: `admin`

Database connection:

* Host: `auth-postgres`
* Port: `5432`
* Database: `authdb`
* Username: `postgres`
* Password: `postgres`

**Docker networking note**

* Containers communicate using service names (`auth-postgres`)
* From host tools, use `localhost`

---

## 11. Common Commands (Cheat Sheet)

Start services:

```bash
docker compose up
```

Start with rebuild:

```bash
docker compose up --build
```

Stop services:

```bash
docker compose down
```

View logs:

```bash
docker compose logs -f auth-service
```

Exec into container:

```bash
docker exec -it auth-service sh
```

Rebuild images:

```bash
docker compose down
docker compose build
docker compose up
```

---

This repository is intended to be cloned and extended as a foundation for authentication-driven Spring Boot services.

---

### Author

**Ajay Kathwate**   
[Email](mailto:ajaykathwatee@gmail.com) | [LinkedIn](https://www.linkedin.com/in/ajay-kathwate/)
