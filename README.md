# Shopware App SDK

A Java Spring Boot SDK for building Shopware 6 app backends that integrate with the Shopware App System.

## Overview

This SDK provides the core framework components needed to build Shopware app backends, handling:

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

The SDK follows clean architecture principles:

```
├── api/           # DTOs and API contracts
├── controller/    # Framework HTTP endpoints
├── service/       # Core business services
├── web/          # Security filters and configuration
├── repository/   # Data persistence
└── util/         # Helper utilities
```

### Package Structure

- `de.codebarista.shopware.appserver.sdk.api` - API DTOs and contracts
- `de.codebarista.shopware.appserver.sdk.controller` - Framework controllers
- `de.codebarista.shopware.appserver.sdk.service` - Core services
- `de.codebarista.shopware.appserver.sdk.web` - Security and web configuration
- `de.codebarista.shopware.appserver.sdk.repository` - Data access layer
- `de.codebarista.shopware.appserver.sdk.util` - Utility classes

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
    implementation 'de.codebarista:shopware-app-sdk:1.5.0'
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

The SDK automatically provides these framework endpoints:

- `POST /shopware/app/register` - App registration
- `POST /shopware/app/confirm` - Registration confirmation
- `POST /shopware/app/lifecycle/{event}` - Lifecycle events
- `POST /shopware/api/v1/webhook` - Webhook handling
- `GET /shopware/admin/{app}/**` - Admin extension serving

## Configuration

The SDK provides comprehensive auto-configuration that's organized by functionality areas for better maintainability and clarity.

### Auto-Configuration Architecture

The SDK uses a modular auto-configuration approach:

- **`AppBackendSdkCoreAutoConfiguration`** - General SDK settings (SSL enforcement, localhost mapping)
- **`AppBackendSdkHttpAutoConfiguration`** - HTTP client configuration (RestTemplate, logging, error handling)
- **`AppBackendSdkDatabaseAutoConfiguration`** - Database defaults (SQLite in-memory fallback)
- **`AppBackendSdkLiquibaseAutoConfiguration`** - SDK core migrations (SHOPWARE_SHOP table, etc.)

### SDK Properties

All SDK configuration uses the `app-backend.sdk` prefix:

```yaml
app-backend:
  sdk:
    # HTTP request/response logging for debugging
    http-request-response-logging-enabled: false  # Default: false

    # Enforce SSL-only communication with Shopware
    ssl-only: true  # Default: true

    # Map localhost IP to domain name for development
    map-localhost-ip-to-localhost-domain-name: false  # Default: false
```

### Database Configuration

The SDK uses **Spring Boot's standard DataSource configuration** with smart defaults for easy development.

#### Development (Zero Configuration)

No configuration needed - the SDK automatically provides SQLite in-memory:

```yaml
# No configuration required!
# SDK automatically configures:
# - SQLite in-memory database (jdbc:sqlite::memory:)
# - Proper SQLite dialect
# - Liquibase migrations
```

#### Production (Standard Spring Boot)

Use standard Spring Boot DataSource configuration - the SDK automatically detects it:

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
- SDK provides `DataSourceProperties` defaults only when none exist
- Spring Boot's `DataSourceAutoConfiguration` handles the rest
- Your configuration automatically overrides SDK defaults
- No manual switches or SDK-specific properties needed

**Supported databases:** Any database supported by Spring Boot DataSource auto-configuration (PostgreSQL, MySQL, SQLite, H2, etc.)

#### Database Migrations

The SDK uses a **dual Liquibase setup** to separate core SDK migrations from your custom migrations:

**SDK Migrations (Automatic):**
- Run automatically when the application starts
- Create core tables required by the SDK (`SHOPWARE_SHOP`, etc.)
- Use `sdk-core` context to avoid conflicts
- Cannot be disabled (required for SDK functionality)

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
- SDK migrations are always applied automatically
- No conflicts between SDK and user migrations
- Users can safely add their own database changes
- Both use the same DataSource and transaction

### HTTP Client Configuration

The SDK provides a pre-configured `RestTemplate` optimized for Shopware API communication:

**Features:**
- **Disabled redirects** - Prevents infinite redirect loops with Shopware APIs
- **Buffered requests** - Disables chunked encoding (required for some endpoints)
- **Enhanced error handling** - Treats all non-2xx responses as errors
- **Optional logging** - Request/response logging for debugging

**Configuration:**
```yaml
app-backend:
  sdk:
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

### Core SDK Configuration

The SDK provides several configuration options for different deployment scenarios:

**SSL Enforcement:**
```yaml
app-backend:
  sdk:
    ssl-only: true  # Default: true - Reject HTTP URLs in production
```

**Development Helpers:**
```yaml
app-backend:
  sdk:
    map-localhost-ip-to-localhost-domain-name: true  # Map 127.0.0.1 to localhost
```

**Access Configuration Bean:**
```java
@Autowired
private AppBackendSdkConfiguration sdkConfiguration;

if (sdkConfiguration.isSslOnly()) {
    // Enforce HTTPS URLs only
}
```

### Environment Variables

- `SHOPWARE_APP_URL` - Public URL of your app backend
- `SHOPWARE_APP_SECRET` - App secret for signature verification

## Security

### Signature Verification

All incoming requests from Shopware are automatically verified using HMAC-SHA256 signatures. The SDK handles:

- App registration signature verification
- Shop signature verification for webhooks
- Automatic signature validation in security filters

### Token Management

OAuth tokens are automatically managed:

- Initial token exchange during registration
- Automatic token refresh when expired
- Secure token storage in database

## Database Schema

The SDK automatically creates and manages these tables via Liquibase:

- `shop_registration` - Registered shop information
- `access_token` - OAuth access tokens
- Additional tables via Liquibase migrations

### Supported Databases

The SDK works with any database supported by Spring Boot's DataSource auto-configuration:

- **SQLite** (default) - In-memory for development, file-based for simple deployments
- **PostgreSQL** - Recommended for production environments
- **MySQL/MariaDB** - Production ready with excellent performance
- **H2** - Alternative in-memory database for testing
- **SQL Server** - Enterprise environments
- **Oracle** - Enterprise database systems

Simply configure using standard Spring Boot `spring.datasource.*` properties.

## Testing

The SDK includes comprehensive test utilities:

- Mock Shopware server setup
- Test data builders
- Integration test base classes

## Architectural Rules

The SDK enforces clean architecture through:

- **Compile-time checks** - Gradle plugin prevents importing app-specific code
- **Module boundaries** - Physical separation from business logic
- **Interface segregation** - Clean API contracts

## License

This SDK is designed to be open-sourced under Apache 2.0 license.

## Contributing

When contributing to the SDK:

1. **Maintain independence** - Never import app-specific packages
2. **Follow conventions** - Use existing patterns and naming
3. **Add tests** - Comprehensive test coverage required
4. **Update docs** - Keep documentation current

## Support

For questions and support:

- Review the main project documentation
- Check architectural rules in `/docs/ARCHITECTURAL_RULES.md`
- Examine example implementations in the main module