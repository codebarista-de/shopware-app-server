# Shopware App Server

This file provides guidance to Claude Code (claude.ai/code) when working with this repository.

## Quick Commands

- **Build**: `./gradlew build`
- **Test**: `./gradlew test`
- **Publish locally**: `./gradlew publishToMavenLocal`
- **Publish**: `./gradlew publish`

## Project Overview

The **Shopware App Server** is a Java Spring Boot library that simplifies building Shopware 6 app backends. It provides a complete framework for integrating with the Shopware App System.

### Core Features

1. **App Registration & Lifecycle** - Automatic registration, confirmation, and lifecycle event handling
2. **Authentication & Security** - Shopware signature verification and OAuth token management
3. **API Integration** - Pre-configured clients for Shopware Admin API
4. **Event Handling** - Webhook processing and event dispatching
5. **Database Integration** - Shop registration persistence and token storage

## Architecture

### Package Structure

```
de.codebarista.shopware.appserver
├── api/          # API DTOs and contracts
├── config/       # Auto-configuration classes
├── controller/   # Framework controllers
├── repository/   # Data access layer
├── service/      # Core services
└── util/         # Utility classes
```

### Auto-Configuration

The App Server uses modular Spring Boot auto-configuration:

- **`AppServerCoreAutoConfiguration`** - Core settings and SSL enforcement
- **`AppServerWebSecurityConfiguration`** - Security configuration
- **`AppServerLiquibaseAutoConfiguration`** - Database migrations

### Database Configuration

**Development (Zero Config)**
- Automatic SQLite in-memory database
- No configuration required

**Production**
```yaml
spring:
  datasource:
    url: jdbc:sqlite:shopware_apps.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
```

### Framework Endpoints

The App Server automatically provides:

- `POST /shopware/app/register` - App registration
- `POST /shopware/app/confirm` - Registration confirmation
- `POST /shopware/app/lifecycle/{event}` - Lifecycle events
- `POST /shopware/api/v1/webhook` - Webhook handling

## Usage Example

### 1. Add Dependency

```gradle
dependencies {
    implementation 'de.codebarista:shopware-app-server:X.Y.Z'
}
```

### 2. Create App Configuration

```java
@Component
public class MyShopwareApp extends ShopwareApp {

    @Override
    public String getAppName() {
        return "my-app";
    }

    @Override
    public String getAppSecret() {
        return "my-app-secret";
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of("product:read", "order:read");
    }
}
```

### 3. Handle Events

```java
@EventListener
public void handleOrderEvent(ShopwareEvent event) {
    if ("order.placed".equals(event.getEventName())) {
        // Process order placement
    }
}
```

## Development Guidelines

### Key Directories

- **Controllers**: `src/main/java/de/codebarista/shopware/appserver/controller/`
- **Services**: `src/main/java/de/codebarista/shopware/appserver/service/`
- **Configuration**: `src/main/java/de/codebarista/shopware/appserver/config/`
- **Migrations**: `src/main/resources/db/changelog/`
- **Tests**: `src/test/java/`

### Architectural Principles

- **Independence** - Never import app-specific packages
- **Module boundaries** - Physical separation from business logic
- **Interface segregation** - Clean API contracts
- **Testability** - Comprehensive test coverage required

### Testing

Tests include:
- Auto-configuration tests
- Database integration tests
- Security filter tests
- Service layer tests
- Integration test utilities

## Dependencies

### Core
- Spring Boot 3.4.1 (Web, Security, Data JPA, WebFlux)
- SQLite Database (in-memory default)
- Jackson JSON processing
- Liquibase migrations

### External
- `jnanoid` - ID generation

## Publishing

- **GroupId**: `de.codebarista`
- **ArtifactId**: `shopware-app-server`
- **License**: MIT
