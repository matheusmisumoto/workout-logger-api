# FitLogr API

This API is part of FitLogr, an application project to track and manage workout data. Users can register and login using their GitHub account, or create a profile using username and password.

It uses Java 17 with Spring framework, and a relational database (MySQL or MariaDB).

Project under development.

## Dependencies
- Spring Boot 3
- Spring Security
- Spring Reactive Web
- Spring Data
- Spring HateOAS
- Validation

## Installing
1. Clone the repository
2. Using your IDE, install the dependencies
3. Modify `application-prod.properties` to use your settings to connect to your local database. You can also create a new profile and set `spring.profiles.active` on `application.properties` to use it.

## Avaliable Routes and Endpoints

### Exercise database

- `GET /v1/exercises`:  List all exercises
- `GET /v1/exercises/{id}`: Get details from an exercise
- `POST /v1/exercises`: Add a new exercise (admin only)
- `PUT /v1/exercises/{id}`: Edit exercise information (admin only)
- `DELETE /v1/exercises/{id}`: Remove an exercise (admin only)

### Workout
- `GET /v1/workouts/user/{userid}`: List all workouts from a user
- `GET /v1/workouts/user/{userid}/latest`: List the 10 latest workouts from a user
- `GET /v1/workouts/user/{userid}/{id}`: Retrieve all details from an workout
- `GET /v1/workouts/user/{userid}/exercise/{exerciseid}`: Get latest user stats from an exercise
- `POST /v1/workouts`: Add workout log
- `PUT /v1/workouts/user/{userid}/{id}`: Edit workout log, including metadata and list of exercises and sets (admin and self profile only)
- `DELETE /v1/workout/{id}`: Remove workout log (admin and self profile only)

### Users
- `GET /v1/users/register`: Add a new user
- `GET /v1/users`: List all users
- `GET /v1/users/{id}`: Retrieve user information
- `PUT /v1/users/{id}`: Edit user profile (admin and self profile only)
- `DELETE /V1/users/{id}`: Delete user (admin and self profile only)

### Authentication
- `POST /auth/oauth`: Login with GitHub
- `POST /auth/login`: Login with username and passwords

---
Developed with ☕ by [Matheus Misumoto](https://matheusmisumoto.dev) in Santos, Brazil