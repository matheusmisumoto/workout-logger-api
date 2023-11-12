# Workout Logger API

The Workout Logger API is part of an application project to track and manage my workout data. This API is designed to record, analyze, and visualize workout progress.

It uses Java 17 with Spring framework, and a relational database (MySQL or MariaDB).

Project under development.

## Dependencies
- Spring Boot 3
- Spring Reactive Web
- Spring Data
- Spring HateOAS
- Validation

## Installing
1. Clone the repository
2. Using your IDE, install the dependencies
3. Modify `application-prod.properties` to use your settings to connect to your local database. You can also create a new profile and set `spring.profiles.active` on `application.properties` to use it.

## Avaliable Routes

### Exercise database
- `GET /v1/exercises`:  List all exercises
- `GET /v1/exercises/{id}`: Get details from an exercise
- `POST /v1/exercises`: Add a new exercise
- `PUT /v1/exercises/{id}`: Edit exercise information
- `DELETE /v1/exercises/{id}`: Remove an exercise

### Workout
- `GET /v1/workout`: List all workouts (temporary endpoint used only for development purposes)
- `GET /v1/workout/{id}`: Get all details from an workout
- `POST /v1/workout`: Add workout log
- `PUT /v1/workout/{id}`: Edit workout log, including metadata and list of exercises and sets
- `DELETE /v1/workout/{id}`: Remove workout log

---
Developed with ☕ by [Matheus Misumoto](https://matheusmisumoto.dev) in Santos, Brazil