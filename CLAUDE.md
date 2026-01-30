# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Development mode with live reload
./mvnw quarkus:dev

# Run tests
./mvnw test

# Run integration tests
./mvnw verify

# Build package
./mvnw package

# Build native executable (requires GraalVM or container build)
./mvnw package -Dnative
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Docker Builds

```bash
# JVM container
./mvnw package && docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-ai-02-jvm .

# Native container (smallest footprint)
./mvnw package -Dnative && docker build -f src/main/docker/Dockerfile.native-micro -t quarkus/quarkus-ai-02 .
```

## Architecture

This is a Quarkus 3.31.1 project using Java 21 with:
- **Quarkus REST** with Jackson for JSON serialization
- **Quarkus ARC** for CDI dependency injection
- **JUnit 5** with Quarkus test extensions

### Project Layout

- `src/main/java/` - Application source code
- `src/main/resources/application.properties` - Configuration
- `src/main/docker/` - Container configurations (JVM, native, native-micro, legacy-jar)
- `src/test/java/` - Test classes

## Quarkus Patterns

- Use `@ApplicationScoped`, `@RequestScoped` for CDI beans
- Use `@Path`, `@GET`, `@POST` etc. for REST endpoints
- Configuration via `application.properties` or `@ConfigProperty`
- Dev mode (`quarkus:dev`) enables live reload and dev services
