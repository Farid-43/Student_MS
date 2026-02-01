# Student Management System 🎓

A comprehensive Spring Boot application with role-based access control for managing students, teachers, courses, departments, and enrollments.

## 🚀 Features

- **JWT Authentication** - Secure token-based authentication
- **Role-Based Access Control** - Admin, Teacher, and Student roles with different permissions
- **Student Management** - Complete CRUD operations for student profiles
- **Teacher Management** - Manage teacher accounts and assignments
- **Course Management** - Create courses, assign teachers, track enrollments
- **Department Management** - Organize departments with course associations
- **Enrollment System** - Students can enroll/drop courses
- **View Classmates** - Students can see their department peers
- **Multi-Page Web UI** - Responsive HTML interface with dark teal theme

## 🛠️ Tech Stack

| Component         | Technology                  |
| ----------------- | --------------------------- |
| Backend Framework | Spring Boot 4.0.2           |
| Security          | Spring Security + JWT       |
| Database          | PostgreSQL 16               |
| ORM               | Spring Data JPA + Hibernate |
| Migrations        | Liquibase                   |
| Build Tool        | Maven                       |
| Container         | Docker + Docker Compose     |
| Frontend          | HTML/CSS/JavaScript         |

## 📋 Prerequisites

- Java 21 or higher
- Docker & Docker Compose
- Maven 3.6+ (or use included wrapper)

## 🏃 Quick Start

### Option 1: Development Mode

```bash
# Start PostgreSQL
docker-compose up -d

# Run application (Windows)
mvnw.cmd spring-boot:run

# Run application (Linux/Mac)
./mvnw spring-boot:run
```

Application runs on: **http://localhost:9090**

### Option 2: Production Mode (Docker)

```bash
docker-compose -f docker-compose.prod.yaml up --build
```

## 🔐 Default Credentials

| Role  | Username | Password   |
| ----- | -------- | ---------- |
| Admin | `admin`  | `admin123` |

## 📡 API Endpoints

### Authentication (Public)

- `POST /api/auth/login` - Login and receive JWT token
- `POST /api/auth/register` - Register new user account

### Admin Endpoints (ROLE_ADMIN)

**Users**

- `GET /api/admin/users` - List all users
- `POST /api/admin/users` - Create new user
- `PUT /api/admin/users/{id}` - Update user
- `DELETE /api/admin/users/{id}` - Delete user

**Students**

- `GET /api/admin/students` - List all students
- `POST /api/admin/students` - Create student with credentials
- `PUT /api/admin/students/{id}` - Update student
- `DELETE /api/admin/students/{id}` - Delete student

**Teachers**

- `GET /api/admin/teachers` - List all teachers
- `POST /api/admin/teachers` - Create teacher with credentials
- `PUT /api/admin/teachers/{id}` - Update teacher
- `DELETE /api/admin/teachers/{id}` - Delete teacher

**Courses**

- `GET /api/admin/courses` - List all courses
- `POST /api/admin/courses` - Create course
- `PUT /api/admin/courses/{id}` - Update course
- `DELETE /api/admin/courses/{id}` - Delete course

**Departments**

- `GET /api/admin/departments` - List all departments
- `POST /api/admin/departments` - Create department
- `PUT /api/admin/departments/{id}` - Update department
- `DELETE /api/admin/departments/{id}` - Delete department

### Teacher Endpoints (ROLE_TEACHER)

- `GET /api/teacher/students` - View all students (Read-only)
- `GET /api/teacher/courses` - View courses (View-only, no edit)
- `GET /api/teacher/my-courses` - View assigned courses

### Student Endpoints (ROLE_STUDENT)

- `GET /api/student/profile` - View own profile
- `PUT /api/student/profile` - Update own profile
- `GET /api/student/courses` - View available courses
- `POST /api/student/enroll/{courseId}` - Enroll in course
- `DELETE /api/student/drop/{courseId}` - Drop course
- `GET /api/student/my-enrollments` - View enrolled courses
- `GET /api/student/department-students` - View classmates

## 🧪 API Usage Example

### Login

```bash
curl -X POST http://localhost:9090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "id": 1,
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

### Create Student (Admin Only)

```bash
curl -X POST http://localhost:9090/api/admin/students \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username":"john.doe",
    "email":"john@example.com",
    "password":"pass123",
    "firstName":"John",
    "lastName":"Doe",
    "studentId":"STU001",
    "department":"CSE",
    "gpa":3.75
  }'
```

## 🗂️ Project Structure

```
src/main/java/com/example/StudentMS/
├── config/                     # Security & app configuration
├── controller/                 # REST API endpoints (5 controllers)
├── dto/                        # Data Transfer Objects (12 DTOs)
├── entity/                     # JPA entities (7 tables)
├── exception/                  # Global exception handler
├── repository/                 # Data access layer (7 repositories)
├── security/                   # JWT authentication components
├── service/                    # Business logic (7 services)
└── util/                       # Helper utilities

src/main/resources/
├── application.yaml            # App configuration
├── db/changelog/               # Liquibase migrations (10 files)
└── static/                     # Frontend HTML pages (12 pages)
```

**See [explain.md](explain.md) for detailed technical documentation.**

## 🗄️ Database Schema

**Tables:**

- `users` - User accounts with authentication
- `roles` - Role definitions (ADMIN, TEACHER, STUDENT)
- `user_roles` - Many-to-many user-role mapping
- `students` - Student profiles linked to users
- `teachers` - Teacher profiles linked to users
- `courses` - Course information with teacher assignment
- `departments` - Department organization
- `enrollments` - Student course enrollments

**Migrations managed by Liquibase** - See `src/main/resources/db/changelog/changes/`

## 🔒 Role-Based Access Summary

| Role        | Permissions                                                                  |
| ----------- | ---------------------------------------------------------------------------- |
| **ADMIN**   | Full CRUD on all resources (users, students, teachers, courses, departments) |
| **TEACHER** | View students, view courses (read-only), manage own profile                  |
| **STUDENT** | Enroll in courses, view classmates, manage own profile                       |

## 📱 Web Interface

Access the web UI at **http://localhost:9090** after starting the application.

**Available Pages:**

- Login (`/login.html`)
- Dashboard (`/dashboard.html`)
- Students Management (`/students.html`)
- Teachers Management (`/teachers.html`)
- Courses Management (`/courses.html`)
- Departments Management (`/departments.html`)
- Users Management (`/users.html`)
- Profile Management (`/profile.html`)
- My Enrollments (`/my-enrollments.html`)
- Classmates View (`/classmates.html`)

## 🤝 Contributing

This project was developed as part of a Software Engineering lab course.

## 📄 License

This project is for educational purposes.

---
