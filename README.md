# FitBuddy

A fitness tracking application built with Spring Boot that helps users manage their workout programs and track their progress.

## Prerequisites

- JDK 21
- Maven 3.9.7+
- PostgreSQL 15.4+
- Docker (optional)

## Development Setup

### Environment Configuration

1. Create a .env file from the template:
```shell
cp .env.example .env
```
2. Update the .env file with your configuration:
```properties
# Database Configuration
DB_NAME=your_database_name
DB_USER=your_database_user
DB_PASSWORD=your_secure_password
DB_HOST=localhost
DB_PORT=5432

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION_MS=86400000  # 24 hours in milliseconds

# Server Configuration
SERVER_PORT=8080
```
3. Generate a secure JWT secret:
```shell
# Using OpenSSL (recommended)
openssl rand -base64 32

# Copy the output and update JWT_SECRET in your .env file
```

### Database Setup

1. Install PostgreSQL 15.4 or higher
2. Create your .env file (if you haven't already):
```shell
cp .env.example .env
```
3. Update the .env file with your database configuration
4. Run the database setup script:
#### For Linux/Mac:
```shell
./setup_db.sh
```
#### For Windows:
```shell
setup_db.bat
```
Note: You will need to enter your PostgreSQL superuser (postgres) password when prompted.

### Application Configuration

1. Clone the repository:
```shell
git clone [repository-url]
cd fitbuddy
```
2. Create startup scripts:
#### For Linux/Mac(start.sh):
```shell
#!/bin/bash

# Load environment variables safely
while IFS= read -r line; do
  if [[ ! "$line" =~ ^#.*$ ]] && [[ -n "$line" ]]; then
    eval "export ${line}"
  fi
done < .env

# Start the application
./mvnw spring-boot:run -Dspring.profiles.active=dev
```
#### For Windows (start.bat):
```shell
@echo off
:: Load environment variables
for /f "tokens=*" %%a in (.env) do (
  set %%a
)

:: Start the application
mvnw.cmd spring-boot:run -Dspring.profiles.active=dev
```
3. Make the script executable (Linux/Mac only):
```shell
chmod +x start.sh
```

## Running the application

### Using Start Scripts:
#### Linux/Mac:
```shell
./start.sh
```
#### Windows:
```shell
start.bat
```
### Using Docker

```shell
# Build and run using Docker Compose
docker-compose up --build
```
The application will be available at http://localhost:8080

## API Documentation

The API documentation is available at:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

## Testing

Run the tests using:
```shell
./mvnw test
```

## Security

- All endpoints except /api/auth/** require authentication
- JWT tokens are used for authentication
- Tokens expire after 24 hours by default

## Database Migrations

Flyway is used for database migrations. New migrations should be added to:
```text
src/main/resources/db/migration/
```
Migration files should follow the naming convention:
```text
V{version}__{description}.sql
```
To run migrations manually:
```shell
./mvnw flyway:migrate
```

## Docker Support

The application includes Docker support for both development and production environments:
1. Development:
```shell
docker-compose up
```
2. Production:
```shell
docker-compose -f docker-compose.prod.yml up
```

## Contributing

1. Create a new branch for your feature
2. Make your changes
3. Create a pull request

Please ensure your code follows the existing style and includes appropriate tests.