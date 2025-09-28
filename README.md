# Shopware App Server

A Java Spring Boot library for building Shopware 6 app backends that integrate with the Shopware App System.

## Overview

This App Server provides the core framework components needed to build Shopware app backends, handling:

- **App Registration & Lifecycle** - Automatic registration, confirmation, and lifecycle event handling
- **Authentication & Security** - Shopware signature verification and OAuth token management
- **API Integration** - Pre-configured clients for Shopware Admin API
- **Event Handling** - Webhook processing and event dispatching
- **Database Integration** - Shop registration persistence and token storage

## Key Features

### 🔐 Security
- Shopware app signature verification
- Shop signature validation for incoming webhooks
- Secure token exchange and storage

### 🔄 Lifecycle Management
- Automated app registration flow
- Installation, activation, deactivation, and deletion event handling
- Shop configuration persistence

### 🌐 API Integration
- Pre-configured WebClient for Shopware Admin API
- Automatic token refresh and management
- Rate limiting and error handling

### 📊 Event System
- Webhook endpoint handling
- Event dispatching to business logic
- Extensible event processing

## Architecture

The App Server follows clean architecture principles:

```
├── api/           # DTOs and API contracts
├── controller/    # Framework HTTP endpoints
├── service/       # Core business services
├── web/          # Security filters and configuration
├── repository/   # Data persistence
└── util/         # Helper utilities
```

### Package Structure

- `de.codebarista.shopware.appserver.api` - API DTOs and contracts
- `de.codebarista.shopware.appserver.controller` - Framework controllers
- `de.codebarista.shopware.appserver.service` - Core services
- `de.codebarista.shopware.appserver.config` - Security and configuration
- `de.codebarista.shopware.appserver.model` - Data access layer
- `de.codebarista.shopware.appserver.util` - Utility classes

## Dependencies

### Core Dependencies
- Spring Boot 3.4.1 (Web, Security, Data JPA, WebFlux)
- SQLite Database (in-memory by default, for easy development)
- Jackson for JSON processing
- Liquibase for database migrations

### External Libraries
- `de.codebarista:shopware-model` - Generated Shopware API models
- `jnanoid` - ID generation

## Usage

### 1. Add Dependency

```gradle
dependencies {
    implementation 'de.codebarista:shopware-app-server:1.5.0'
}
```

### 2. Extend ShopwareApp

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

### 4. Create Business Controllers

```java
@RestController
@RequestMapping("/api/v1/myapp")
public class MyAppController {

    private final TokenService tokenService;

    @GetMapping("/data")
    public ResponseEntity<?> getData(@RequestHeader("shopware-shop-signature") String signature) {
        // Verify signature and process request
        return ResponseEntity.ok(data);
    }
}
```

## Framework Endpoints

The App Server automatically provides these framework endpoints:

- `POST /shopware/app/register` - App registration
- `POST /shopware/app/confirm` - Registration confirmation
- `POST /shopware/app/lifecycle/{event}` - Lifecycle events
- `POST /shopware/api/v1/webhook` - Webhook handling
- `GET /shopware/admin/{app}/**` - Admin extension serving

## Configuration

The App Server provides comprehensive auto-configuration that's organized by functionality areas for better maintainability and clarity.

### Auto-Configuration Architecture

The App Server uses a modular auto-configuration approach:

- **`AppServerCoreAutoConfiguration`** - General App Server settings (SSL enforcement, localhost mapping)
- **`AppServerHttpAutoConfiguration`** - HTTP client configuration (RestTemplate, logging, error handling)
- **`AppServerDatabaseAutoConfiguration`** - Database defaults (SQLite in-memory fallback)
- **`AppServerLiquibaseAutoConfiguration`** - App Server core migrations (SHOPWARE_SHOP table, etc.)

### App Server Properties

All App Server configuration uses the `app-server` prefix:

```yaml
app-backend:
  app-server:
    # HTTP request/response logging for debugging
    http-request-response-logging-enabled: false  # Default: false

    # Enforce SSL-only communication with Shopware
    ssl-only: true  # Default: true

    # Map localhost IP to domain name for development
    map-localhost-ip-to-localhost-domain-name: false  # Default: false
```

### Database Configuration

The App Server uses **Spring Boot's standard DataSource configuration** with smart defaults for easy development.

#### Development (Zero Configuration)

No configuration needed - the App Server automatically provides SQLite in-memory:

```yaml
# No configuration required!
# App Server automatically configures:
# - SQLite in-memory database (jdbc:sqlite::memory:)
# - Proper SQLite dialect
# - Liquibase migrations
```

#### Production (Standard Spring Boot)

Use standard Spring Boot DataSource configuration - the App Server automatically detects it:

```yaml
# Standard Spring Boot DataSource configuration
spring:
  datasource:
    url: jdbc:sqlite:shopware_apps.db
    driver-class-name: org.sqlite.JDBC
    hikari:
      maximum-pool-size: 1
      connection-timeout: 5000
      data-source-properties:
        foreign_keys: true
        busy_timeout: 5000
        journal_mode: WAL

  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: none  # Use Liquibase
    open-in-view: false

  liquibase:
    change-log: classpath:db/changelog/user-changelog-master.xml  # Your custom migrations
```

**How it works:**
- App Server provides `DataSourceProperties` defaults only when none exist
- Spring Boot's `DataSourceAutoConfiguration` handles the rest
- Your configuration automatically overrides App Server defaults
- No manual switches or App Server-specific properties needed

**Supported databases:** Any database supported by Spring Boot DataSource auto-configuration (PostgreSQL, MySQL, SQLite, H2, etc.)

#### Database Migrations

The App Server uses a **dual Liquibase setup** to separate core App Server migrations from your custom migrations:

**App Server Migrations (Automatic):**
- Run automatically when the application starts
- Create core tables required by the App Server (`SHOPWARE_SHOP`, etc.)
- Use `app-server-core` context to avoid conflicts
- Cannot be disabled (required for App Server functionality)

**User Migrations (Optional):**
```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/user-changelog-master.xml
```

**Adding Custom Tables:**
1. Create `src/main/resources/db/changelog/user-changelog-master.xml`
2. Add your changesets:
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

       <include file="db/changelog/changesets/0001-add-my-table.xml" relativeToChangelogFile="true"/>
   </databaseChangeLog>
   ```

**Benefits:**
- App Server migrations are always applied automatically
- No conflicts between App Server and user migrations
- Users can safely add their own database changes
- Both use the same DataSource and transaction

### HTTP Client Configuration

The App Server provides a pre-configured `RestTemplate` optimized for Shopware API communication:

**Features:**
- **Disabled redirects** - Prevents infinite redirect loops with Shopware APIs
- **Buffered requests** - Disables chunked encoding (required for some endpoints)
- **Enhanced error handling** - Treats all non-2xx responses as errors
- **Optional logging** - Request/response logging for debugging

**Configuration:**
```yaml
app-backend:
  app-server:
    http-request-response-logging-enabled: true  # Enable detailed HTTP logging
```

**Bean name:** `shopwareRestTemplate` - Inject this RestTemplate for Shopware API calls

**Example usage:**
```java
@Autowired
private RestTemplate shopwareRestTemplate;

// This RestTemplate is automatically configured for Shopware APIs
ResponseEntity<String> response = shopwareRestTemplate.exchange(
    shopwareApiUrl, HttpMethod.GET, requestEntity, String.class);
```

### Core App Server Configuration

The App Server provides several configuration options for different deployment scenarios:

**SSL Enforcement:**
```yaml
app-backend:
  app-server:
    ssl-only: true  # Default: true - Reject HTTP URLs in production
```

**Development Helpers:**
```yaml
app-backend:
  app-server:
    map-localhost-ip-to-localhost-domain-name: true  # Map 127.0.0.1 to localhost
```

**Access Configuration Bean:**
```java
@Autowired
private AppServerConfiguration appServerConfiguration;

if (appServerConfiguration.isSslOnly()) {
    // Enforce HTTPS URLs only
}
```

### Environment Variables

- `SHOPWARE_APP_URL` - Public URL of your app backend
- `SHOPWARE_APP_SECRET` - App secret for signature verification

## Security

### Signature Verification

All incoming requests from Shopware are automatically verified using HMAC-SHA256 signatures. The App Server handles:

- App registration signature verification
- Shop signature verification for webhooks
- Automatic signature validation in security filters

### Token Management

OAuth tokens are automatically managed:

- Initial token exchange during registration
- Automatic token refresh when expired
- Secure token storage in database

## Database Schema

The App Server automatically creates and manages these tables via Liquibase:

- `shop_registration` - Registered shop information
- `access_token` - OAuth access tokens
- Additional tables via Liquibase migrations

### Supported Databases

The App Server works with any database supported by Spring Boot's DataSource auto-configuration:

- **SQLite** (default) - In-memory for development, file-based for simple deployments
- **PostgreSQL** - Recommended for production environments
- **MySQL/MariaDB** - Production ready with excellent performance
- **H2** - Alternative in-memory database for testing
- **SQL Server** - Enterprise environments
- **Oracle** - Enterprise database systems

Simply configure using standard Spring Boot `spring.datasource.*` properties.

## Testing

The App Server includes comprehensive test utilities:

- Mock Shopware server setup
- Test data builders
- Integration test base classes

## Architectural Rules

The App Server enforces clean architecture through:

- **Compile-time checks** - Gradle plugin prevents importing app-specific code
- **Module boundaries** - Physical separation from business logic
- **Interface segregation** - Clean API contracts

## License

This App Server is designed to be open-sourced under MIT license.

## Contributing

When contributing to the App Server:

1. **Maintain independence** - Never import app-specific packages
2. **Follow conventions** - Use existing patterns and naming
3. **Add tests** - Comprehensive test coverage required
4. **Update docs** - Keep documentation current

## Support

For questions and support:

- Review the main project documentation
- Check architectural rules in `/docs/ARCHITECTURAL_RULES.md`
- Examine example implementations in the main module