# Spring Boot 3 API Template

As part of a series of templates i created to meet my own projects needs and regularly use, i am sharing in this repo a modern feature-rich REST API template i built with Spring Boot 3 and Java 17 that can be used to jumpstart your backend development with industry best practices. It is designed with multi tenant/business SaaS solutions in mind.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.7-brightgreen)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Latest-green)](https://www.mongodb.com/)

## 📋 Features

- **Authentication & Authorization**:
  - JWT-based authentication
  - OAuth2 integration (Google, Apple)
  - Role-based access control (SUPER_ADMIN, ADMIN, DEFAULT, PENDING)
  - Password management with secure validation
  - Email verification flow
  - Password reset functionality

- **Security**:
  - Request rate limiting
  - CSRF protection configuration
  - Stateless session management
  - Configurable security policies

- **MongoDB Integration**:
  - Document-based data storage
  - Repository pattern implementation
  - Configurable via environment variables

- **API Features**:
  - RESTful endpoints
  - Structured request/response models
  - DTO pattern implementation
  - Comprehensive model architecture

- **File Management**:
  - File upload/download capabilities
  - File metadata storage in MongoDB
  - File type detection with Apache Tika
  - WebP/Jpg/Png image support

- **Email Services**:
  - SendGrid integration
  - Email templating with Thymeleaf
  - Verification and notification flows

- **DevOps Ready**:
  - Docker and Docker Compose setup
  - Health checks via Spring Actuator
  - Environment variable configuration

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.3.7**
- **Spring Security** with JWT authentication
- **Spring Data MongoDB**
- **OAuth2** for social login
- **Docker** & **Docker Compose** for containerization
- **SendGrid** for email services
- **Thymeleaf** for HTML templates
- **Lombok** for boilerplate reduction
- **Apache Tika** for file type detection
- **Bucket4j** & **Caffeine** for rate limiting
- **Maven** for dependency management

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose (for containerized deployment)
- MongoDB (can be run via Docker)
- SendGrid API key (for email functionality)

### Environment Setup

Create a `.env` file in the project root with the following variables:

```
# MongoDB Configuration
MONGODB_USER=your_mongodb_user
MONGODB_PASSWORD=your_mongodb_password
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=springboot3_api

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_at_least_32_chars

# File Storage
UPLOAD_PATH=/path/to/file/storage
MAX_FILE_SIZE=2MB

# Email Configuration
SENDGRID_KEY=your_sendgrid_api_key
EMAIL_FROM=your_email@example.com

# OAuth2 Configuration
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
OAUTH2_GOOGLE_REPLY_URI=https://your-domain.com/login/oauth2/code/google
APPLE_CLIENT_ID=your_apple_client_id
APPLE_CLIENT_SECRET=your_apple_client_secret
OAUTH2_APPLE_REPLY_URI=https://your-domain.com/login/oauth2/code/apple
OAUTH2_REDIRECT_URI=https://your-domain.com/oauth2/redirect
API_DEEPLINK_URI=your-app://oauth2/redirect

# Server Configuration
SERVER_ADDRESS=0.0.0.0
SERVER_PORT=8080
SERVER_NAME=your-domain.com
```

### Running Locally

1. Clone the repository
   ```
   git clone https://github.com/yourusername/springboot3-api.git
   cd springboot3-api
   ```

2. Build the project
   ```
   ./mvnw clean install
   ```

3. Run the application
   ```
   ./mvnw spring-boot:run
   ```

### Docker Deployment

1. Build and run with Docker Compose
   ```
   docker-compose up -d
   ```

2. Check container status
   ```
   docker ps
   ```

## 📝 API Documentation

### Authentication Endpoints

- **POST /v1/auth/register** - Register new user
- **POST /v1/auth/login** - Login and receive JWT token
- **POST /v1/auth/password-reset/request** - Request password reset
- **GET /v1/auth/password-reset** - Display password reset form
- **POST /v1/auth/password-reset** - Submit new password
- **GET /v1/auth/verify-email** - Verify email address
- **POST /v1/auth/resend-verification** - Resend verification email
- **GET /v1/auth/me** - Get current user information

### User Endpoints

- **GET /v1/users** - List users (role restricted)
- **GET /v1/users/{id}** - Get user by ID
- **PUT /v1/users/{id}** - Update user information
- **DELETE /v1/users/{id}** - Delete a user

### File Management Endpoints

- **POST /v1/files/upload** - Upload file
- **GET /v1/files/{id}** - Download file
- **GET /v1/files/public/{id}** - Public file access
- **DELETE /v1/files/{id}** - Delete file

### Post Management Endpoints

- **POST /v1/posts** - Create a new post
- **GET /v1/posts** - List posts
- **GET /v1/posts/{id}** - Get post by ID
- **PUT /v1/posts/{id}** - Update a post
- **DELETE /v1/posts/{id}** - Delete a post

### Business Management Endpoints

- **POST /v1/business** - Create a new business
- **GET /v1/business** - List businesses
- **GET /v1/business/{id}** - Get business by ID
- **PUT /v1/business/{id}** - Update a business
- **DELETE /v1/business/{id}** - Delete a business

## 🔒 Security Features

### Rate Limiting

The API implements a tiered rate limiting system:
- Unauthenticated users: 30 requests per minute
- Authenticated users: 60 requests per minute
- Admin users: 100 requests per minute

### JWT Authentication

- Token-based authentication
- 1-day expiration by default (configurable)
- Role-based endpoint access

### Password Security

All passwords must meet these requirements:
- 8-50 characters in length
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

### TODO:
- Add refresh tokens
- Add integration with push notification providers
- Add Swagger and Prometheus integrations

## 🔧 Customization

### Extending the API

1. Create new models in `model/` directory
2. Add corresponding repositories in `repository/`
3. Create DTOs in `dto/` if needed (or request/response classes)
4. Implement service layer in `service/`
5. Create controllers in `controller/`
6. Update security configurations if necessary

### Configuration Properties

Most aspects of the application can be customized through `application.properties` or environment variables.

## 📦 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/roytemplates/springboot3_api/
│   │       ├── config/            # Configuration classes
│   │       ├── controller/        # REST controllers
│   │       ├── dto/               # Data transfer objects
│   │       ├── filter/            # Security filters
│   │       ├── model/             # Domain models
│   │       ├── repository/        # Data repositories
│   │       ├── request/           # API request models
│   │       ├── response/          # API response models
│   │       ├── security/          # Security components
│   │       ├── service/           # Business logic
│   │       └── ApiApplication.java # Main application entry
│   └── resources/
│       ├── application.properties  # Application configuration
│       └── templates/             # Email templates
└── test/
    └── java/
        └── com/roytemplates/springboot3/
            └── ApiApplicationTests.java #Minimal
```

## 📄 License

This project is licensed under the MIT License - see below for details:

```
MIT License

Copyright (c) 2025 Roy Templates

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🙏 Acknowledgments

- Spring Boot and Spring Security documentation
- MongoDB documentation
- OAuth2 specifications
- All the open source libraries used in this project