# Student Management System

A Spring Boot application with role-based access control (RBAC) for managing students, teachers, and courses.

## Features

- **Authentication & Authorization**: JWT-based authentication with role-based access control
- **Three Roles**: Admin, Teacher, Student
- **CRUD Operations**: Full CRUD for students, teachers, and courses
- **Database**: PostgreSQL with Liquibase migrations
- **Containerization**: Docker and Docker Compose support

## Tech Stack

- Spring Boot 4.0.2
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Liquibase (Database Migrations)
- Docker & Docker Compose
- Lombok

## Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven (or use the included Maven wrapper)

### Running with Docker Compose (Development)

1. Start PostgreSQL:
```bash
docker-compose up -d
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

### Running Full Stack with Docker (Production)

```bash
docker-compose -f docker-compose.prod.yaml up --build
```

## Default Admin Credentials

- **Username**: admin
- **Password**: admin123

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login and get JWT token |
| POST | `/api/auth/register` | Register a new user |

### Admin Endpoints (ROLE_ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | Get all users |
| GET | `/api/admin/users/{id}` | Get user by ID |
| PUT | `/api/admin/users/{id}` | Update user |
| DELETE | `/api/admin/users/{id}` | Delete user |
| GET | `/api/admin/students` | Get all students |
| POST | `/api/admin/students` | Create student |
| PUT | `/api/admin/students/{id}` | Update student |
| DELETE | `/api/admin/students/{id}` | Delete student |
| GET | `/api/admin/teachers` | Get all teachers |
| POST | `/api/admin/teachers` | Create teacher |
| PUT | `/api/admin/teachers/{id}` | Update teacher |
| DELETE | `/api/admin/teachers/{id}` | Delete teacher |
| GET | `/api/admin/courses` | Get all courses |
| POST | `/api/admin/courses` | Create course |
| PUT | `/api/admin/courses/{id}` | Update course |
| DELETE | `/api/admin/courses/{id}` | Delete course |

### Teacher Endpoints (ROLE_ADMIN, ROLE_TEACHER)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/teacher/students` | View all students |
| GET | `/api/teacher/students/{id}` | View student by ID |
| PUT | `/api/teacher/students/{id}` | Update student (grades) |
| GET | `/api/teacher/courses` | View all courses |
| GET | `/api/teacher/my-courses` | View own courses |
| POST | `/api/teacher/courses` | Create course |
| PUT | `/api/teacher/courses/{id}` | Update course |
| DELETE | `/api/teacher/courses/{id}` | Delete course |

### Student Endpoints (ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/student/profile` | View own profile |
| PUT | `/api/student/profile` | Update own profile |
| GET | `/api/student/courses` | View all courses |
| GET | `/api/student/courses/{id}` | View course by ID |

## API Usage Examples

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@studentms.com",
  "roles": ["ROLE_ADMIN"]
}
```

### Create a Student (Admin)
```bash
curl -X POST http://localhost:8080/api/admin/students \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "studentId": "STU001",
    "department": "Computer Science",
    "semester": 5,
    "enrollmentDate": "2023-01-15",
    "gpa": 3.75
  }'
```

### Create a Teacher (Admin)
```bash
curl -X POST http://localhost:8080/api/admin/teachers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username": "jane.smith",
    "email": "jane.smith@example.com",
    "password": "password123",
    "firstName": "Jane",
    "lastName": "Smith",
    "employeeId": "EMP001",
    "department": "Computer Science",
    "designation": "Professor",
    "joiningDate": "2020-08-01"
  }'
```

### Create a Course (Admin/Teacher)
```bash
curl -X POST http://localhost:8080/api/admin/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "courseCode": "CS101",
    "courseName": "Introduction to Programming",
    "description": "Basic programming concepts",
    "credits": 3,
    "teacherId": 1
  }'
```

## Project Structure

```
src/main/java/com/example/StudentMS/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AdminController.java
│   ├── AuthController.java
│   ├── StudentController.java
│   └── TeacherController.java
├── dto/
│   ├── CourseDTO.java
│   ├── CreateStudentRequest.java
│   ├── CreateTeacherRequest.java
│   ├── JwtResponse.java
│   ├── LoginRequest.java
│   ├── MessageResponse.java
│   ├── RegisterRequest.java
│   ├── StudentDTO.java
│   ├── TeacherDTO.java
│   └── UserDTO.java
├── entity/
│   ├── Course.java
│   ├── Role.java
│   ├── Student.java
│   ├── Teacher.java
│   └── User.java
├── exception/
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── CourseRepository.java
│   ├── RoleRepository.java
│   ├── StudentRepository.java
│   ├── TeacherRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/
│   ├── AuthService.java
│   ├── CourseService.java
│   ├── StudentService.java
│   ├── TeacherService.java
│   └── UserService.java
└── StudentMsApplication.java
```

## Database Migrations

Liquibase migrations are located in `src/main/resources/db/changelog/changes/`:

1. `001-create-users-table.yaml` - Users table
2. `002-create-roles-table.yaml` - Roles table
3. `003-create-user-roles-table.yaml` - User-Roles junction table
4. `004-create-students-table.yaml` - Students table
5. `005-create-teachers-table.yaml` - Teachers table
6. `006-create-courses-table.yaml` - Courses table
7. `007-insert-default-roles.yaml` - Default roles (ADMIN, TEACHER, STUDENT)
8. `008-insert-admin-user.yaml` - Default admin user

## Role-Based Access Control

| Role | Access Level |
|------|--------------|
| ADMIN | Full access to all endpoints |
| TEACHER | Can view/update students, manage courses |
| STUDENT | Can view own profile and courses |

## License

This project is for educational purposes.
