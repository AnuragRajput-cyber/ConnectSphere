# ConnectSphere
# ConnectSphere 🚀

ConnectSphere is a **microservices-based social media platform** built using **Spring Boot, Angular, MySQL, Redis, RabbitMQ, Elasticsearch, Docker, and Jenkins**.
It provides modern social networking features like authentication, posts, comments, likes, follows, notifications, stories, chat, and admin moderation.

---

## 📌 Project Overview

ConnectSphere is designed using **Microservices Architecture**, where each business module runs as an independent service.

### Key Features

### 👤 User Features

* User Registration & Login
* Email OTP Verification
* JWT Authentication
* Google / GitHub OAuth Login
* Create / Edit / Delete Posts
* Upload Images & Videos
* Like / React / Comment on Posts
* Follow / Unfollow Users
* Search Users / Posts / Hashtags
* Stories (24-hour temporary posts)
* Real-time Chat
* Notifications
* Public / Private Profiles

### 🛡️ Admin Features

* Manage Users
* Moderate Posts / Comments
* Review Reports
* Broadcast Notifications
* Dashboard Monitoring

---

# 🏗️ Architecture

```text
Frontend (Angular)
        ↓
API Gateway
        ↓
-------------------------------------------------
| Auth Service | Post Service | Comment Service |
| Like Service | Follow Service | Search Service |
| Chat Service | Notification Service | Media Service |
-------------------------------------------------
        ↓
MySQL | Redis | RabbitMQ | Elasticsearch
```

---

# ⚙️ Tech Stack

## Frontend

* Angular
* TypeScript
* Bootstrap / CSS

## Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Cloud Gateway
* Spring Data JPA
* Eureka Service Registry

## Database & Storage

* MySQL
* Redis
* Elasticsearch
* AWS S3 / Azure Blob Storage

## DevOps

* Docker
* Docker Compose
* Jenkins
* Nginx

---

# 📁 Microservices

| Service              | Port | Purpose          |
| -------------------- | ---- | ---------------- |
| service-registry     | 8761 | Eureka Discovery |
| api-gateway          | 8080 | API Routing      |
| auth-service         | 8081 | Authentication   |
| post-service         | 8082 | Posts            |
| comment-service      | 8083 | Comments         |
| like-service         | 8084 | Likes            |
| follow-service       | 8085 | Followers        |
| notification-service | 8086 | Notifications    |
| media-service        | 8087 | Media Upload     |
| search-service       | 8088 | Search           |
| chat-service         | 8089 | Messaging        |
| connectsphere-web    | 4200 | Angular Frontend |

---

# 🚀 Local Setup

## 1️⃣ Clone Repository

```bash
git clone https://github.com/yourusername/connectsphere.git
cd connectsphere
```

---

## 2️⃣ Install Requirements

Make sure installed:

* Java 17
* Maven
* Node.js
* MySQL
* Redis
* RabbitMQ
* Elasticsearch

---

## 3️⃣ Create Databases

```sql
CREATE DATABASE connectsphere_auth;
CREATE DATABASE connectsphere_post;
CREATE DATABASE connectsphere_comment;
CREATE DATABASE connectsphere_like;
CREATE DATABASE connectsphere_follow;
CREATE DATABASE connectsphere_notification;
CREATE DATABASE connectsphere_media;
CREATE DATABASE connectsphere_search;
CREATE DATABASE connectsphere_chat;
```

---

## 4️⃣ Start Infrastructure

Run:

* MySQL
* Redis
* RabbitMQ
* Elasticsearch

---

## 5️⃣ Start Services

```bash
mvn -pl service-registry spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl post-service spring-boot:run
mvn -pl comment-service spring-boot:run
mvn -pl like-service spring-boot:run
mvn -pl follow-service spring-boot:run
mvn -pl notification-service spring-boot:run
mvn -pl media-service spring-boot:run
mvn -pl search-service spring-boot:run
mvn -pl chat-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

---

## 6️⃣ Start Frontend

```bash
cd connectsphere-web
npm install
npm start
```

Frontend:

```text
http://localhost:4200
```

Eureka Dashboard:

```text
http://localhost:8761
```

---

# 🐳 Docker Deployment

```bash
docker-compose up -d --build
```

For the production-style compose file:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

The production-style compose file also has safe local defaults for development. For Azure/AWS production, copy one of the templates in `deployment/` to `deployment/.env.production`, fill real values, and run:

```bash
docker compose --env-file deployment/.env.production -f docker-compose.prod.yml up -d --build
```

For Azure production deployment, follow [docs/azure-production-checklist.md](docs/azure-production-checklist.md).

---

# 🔐 Authentication Flow

* Register User
* Verify OTP
* Login
* Receive JWT Access Token
* Use token for protected APIs
* Refresh token support

---

# 🔍 Search Features

* Search Users
* Search Posts
* Hashtag Search
* Trending Hashtags

Powered by Elasticsearch.

---

# ⚡ Performance Optimizations

* Redis Caching
* RabbitMQ Async Events
* Elasticsearch Full-text Search
* CDN Ready Media Storage

---

# 🌍 Production Deployment

Supported on:

* AWS EC2
* Microsoft Azure
* DigitalOcean
* Docker VPS

Recommended stack:

* Ubuntu Server
* Docker Compose
* Nginx Reverse Proxy
* SSL via Certbot

---

# 📸 Demo Flow

1. Register User
2. Verify OTP
3. Login
4. Create Post
5. Like / Comment
6. Follow User
7. Search Hashtag
8. Upload Story
9. Chat
10. Admin Dashboard

---

# 🎯 Why This Project?

ConnectSphere demonstrates:

* Real Industry Microservices Architecture
* Secure Authentication System
* Scalable Social Media Backend
* DevOps CI/CD Pipeline
* Cloud Deployment Ready

---

# 👨‍💻 Author

**Anurag**
B.Tech CSE Student
GLA University

---

# ⭐ Future Improvements

* Kubernetes Deployment
* Realtime Notifications
* AI Content Moderation
* Video Calling
* Mobile App (Flutter / React Native)

---

# 📜 License

This project is for educational and portfolio purposes.
