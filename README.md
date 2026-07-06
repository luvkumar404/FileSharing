# Secure File Sharing System

A secure file sharing system built with Spring Boot, Maven, and a plain HTML/CSS/JavaScript frontend.

This project supports user registration, JWT-based login, authenticated file upload, file download URL generation, listing a user's own files, deleting owned files, and sharing files through temporary public links. File metadata is stored in PostgreSQL, while the actual uploaded files are stored in Cloudinary. The frontend is served directly by Spring Boot from `src/main/resources/static`.

## Key Features

- User registration and login
- Password hashing with BCrypt
- JWT authentication
- Plain HTML, CSS, and JavaScript frontend
- JWT storage in browser `localStorage`
- Protected file APIs
- Upload files to Cloudinary
- Store file metadata in PostgreSQL
- List files uploaded by the logged-in user
- Return secure Cloudinary URLs for owned private files
- Delete owned files from Cloudinary and PostgreSQL
- Generate temporary public share links
- Expire shared links after a configured time
- Global exception handling
- Request validation using DTOs

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Build Tool | Maven |
| Web | Spring Web |
| Frontend | HTML, CSS, JavaScript |
| Security | Spring Security, JWT, BCrypt |
| File Storage | Cloudinary |
| Database | PostgreSQL |
| Persistence | Spring Data JPA, Hibernate |
| Utilities | Lombok |
| Validation | Jakarta Validation |

## Project Architecture

The project follows a clean layered architecture:

| Layer | Responsibility |
| --- | --- |
| `controller` | Handles HTTP requests and responses |
| `service` | Contains business logic, including Cloudinary file storage |
| `repository` | Communicates with the database |
| `entity` | Defines database models |
| `dto` | Defines request and response objects |
| `security` | Handles JWT and authenticated user details |
| `config` | Contains application, security, and Cloudinary configuration |
| `exception` | Handles application errors globally |
| `static` | Contains the frontend files served by Spring Boot |

## Working Diagram

```mermaid
flowchart LR
    User[User] --> Auth[Auth APIs]
    User --> Frontend[HTML CSS JavaScript Frontend]
    Frontend --> Auth
    Auth --> JWT[JWT Token]
    Frontend --> JWT
    JWT --> FileAPIs[Protected File APIs]
    FileAPIs --> PostgreSQL[(PostgreSQL Metadata)]
    FileAPIs --> Cloudinary[Cloudinary Storage]
```

## Frontend

The frontend is available at:

```text
http://localhost:8080
```

Frontend files:

| File | Purpose |
| --- | --- |
| `src/main/resources/static/index.html` | Page structure and forms |
| `src/main/resources/static/styles.css` | Responsive styling |
| `src/main/resources/static/app.js` | API calls, JWT handling, and UI behavior |

Frontend features:

- Register and login forms
- Stores JWT token in `localStorage`
- Sends protected requests with `Authorization: Bearer <token>`
- Uploads files using `FormData`
- Lists logged-in user's files
- Opens Cloudinary secure file URLs
- Deletes owned files
- Creates temporary share links
- Logs out by clearing browser storage

## API Endpoints

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Login and receive a JWT token |
| `POST` | `/api/files/upload` | Protected | Upload a file to Cloudinary |
| `GET` | `/api/files` | Protected | List logged-in user's files |
| `GET` | `/api/files/{id}/download` | Protected | Get the Cloudinary secure URL for an owned file |
| `DELETE` | `/api/files/{id}` | Protected | Delete an owned file from Cloudinary and PostgreSQL |
| `POST` | `/api/files/{id}/share` | Protected | Create a temporary public share link |
| `GET` | `/api/share/{token}` | Public | Get the Cloudinary secure URL using a valid share token |

## Database Entities

### User

| Field | Type | Description |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `name` | `String` | User's name |
| `email` | `String` | Unique email address |
| `password` | `String` | BCrypt hashed password |
| `role` | `Role` | User role |
| `createdAt` | `LocalDateTime` | Account creation time |

### File

| Field | Type | Description |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `originalFileName` | `String` | Original uploaded filename |
| `fileType` | `String` | MIME type |
| `fileSize` | `Long` | File size in bytes |
| `cloudinaryPublicId` | `String` | Cloudinary public ID |
| `cloudinarySecureUrl` | `String` | Cloudinary secure file URL |
| `uploadedBy` | `User` | Owner of the file |
| `createdAt` | `LocalDateTime` | Upload time |

### Shared File Link

| Field | Type | Description |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `token` | `String` | Public share token |
| `file` | `FileEntity` | Shared file |
| `createdBy` | `User` | User who created the link |
| `expiresAt` | `LocalDateTime` | Link expiration time |
| `createdAt` | `LocalDateTime` | Link creation time |

## Setup Instructions

### 1. Clone the Project

```bash
git clone <repository-url>
cd share
```

### 2. Create PostgreSQL Database

Create a database named:

```sql
CREATE DATABASE file_sharing_db;
```

### 3. Configure Cloudinary

Create a Cloudinary account and copy your cloud name, API key, and API secret from the Cloudinary dashboard.

### 4. Configure Application Properties

Update `src/main/resources/application.properties` with your PostgreSQL username, password, JWT secret, and Cloudinary credentials.

### 5. Run the Application

On Windows:

```bash
mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

The application will start at:

```text
http://localhost:8080
```

Open this URL in the browser to use the frontend.

## Environment Variables / application.properties Example

```properties
spring.application.name=java.share

spring.datasource.url=jdbc:postgresql://localhost:5432/file_sharing_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

app.jwt.secret=change-this-secret-key-to-at-least-32-characters
app.jwt.expiration-ms=86400000

cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret

spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

## Folder Structure

```text
src
`-- main
    |-- java
    |   `-- com.example.java.share
    |       |-- config
    |       |-- controller
    |       |-- dto
    |       |-- entity
    |       |-- exception
    |       |-- repository
    |       |-- security
    |       `-- service
    `-- resources
        |-- application.properties
        `-- static
            |-- index.html
            |-- styles.css
            `-- app.js
```

## Contribution Guidelines

Contributions are welcome. To contribute:

1. Fork the repository.
2. Create a new feature branch.
3. Make your changes with clear and readable code.
4. Test your changes before submitting.
5. Open a pull request with a short explanation of your changes.

Please keep the code beginner-friendly, consistent with the existing layered structure, and focused on the backend API.

## Author

**Love Kumar Chaudhary**

Project: **Secure File Sharing System**
