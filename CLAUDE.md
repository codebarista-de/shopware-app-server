This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

### SDK Library
- **Build**: `./gradlew build` - Builds the SDK library JAR
- **Run tests**: `./gradlew test`
- **Publish locally**: `./gradlew publishToMavenLocal` - Publishes to local Maven repository for testing
- **Publish**: `./gradlew publish` - Publishes to configured repository

## Architecture Overview

This is the **Shopware App Backend SDK** - a Java Spring Boot library for building Shopware 6 app backends that integrate with the
Shopware App System.

### Core Functionality

The SDK provides the essential framework components needed to build Shopware app backends:

1. **App Registration & Lifecycle** - Automatic registration, confirmation, and lifecycle event handling
2. **Authentication & Security** - Shopware signature verification and OAuth token management
3. **API Integration** - Pre-configured clients for Shopware Admin API
4. **Event Handling** - Webhook processing and event dispatching
5. **Database Integration** - Shop registration persistence and token storage

### Package Structure

- `de.codebarista.shopware.appbackend.sdk.api` - API DTOs and contracts
- `de.codebarista.shopware.appbackend.sdk.controller` - Framework controllers
- `de.codebarista.shopware.appbackend.sdk.service` - Core services
- `de.codebarista.shopware.appbackend.sdk.web` - Security and web configuration
- `de.codebarista.shopware.appbackend.sdk.repository` - Data access layer
- `de.codebarista.shopware.appbackend.sdk.util` - Utility classes
- `de.codebarista.shopware.appbackend.sdk.config` - Auto-configuration classes

### Auto-Configuration Architecture

The SDK uses modular auto-configuration:

- **`AppBackendSdkCoreAutoConfiguration`** - General SDK settings (SSL enforcement, localhost mapping)
- **`AppBackendSdkHttpAutoConfiguration`** - HTTP client configuration (RestTemplate, logging, error handling)
- **`AppBackendSdkDatabaseAutoConfiguration`** - Database defaults (SQLite in-memory fallback)
- **`AppBackendSdkLiquibaseAutoConfiguration`** - SDK core migrations (SHOPWARE_SHOP table, etc.)

### Key Features

#### 🔐 Security
- Shopware app signature verification
- Shop signature validation for incoming webhooks
- Secure token exchange and storage

#### 🔄 Lifecycle Management
- Automated app registration flow
- Installation, activation, deactivation, and deletion event handling
- Shop configuration persistence

#### 🌐 API Integration
- Pre-configured WebClient for Shopware Admin API
- Automatic token refresh and management
- Rate limiting and error handling

#### 📊 Event System
- Webhook endpoint handling
- Event dispatching to business logic
- Extensible event processing

### Dependencies

#### Core Dependencies
- Spring Boot 3.4.1 (Web, Security, Data JPA, WebFlux)
- SQLite Database (in-memory by default, for easy development)
- Jackson for JSON processing
- Liquibase for database migrations

#### External Libraries
- `de.codebarista:shopware-model` - Generated Shopware API models
- `jnanoid` - ID generation

## Framework Endpoints

The SDK automatically provides these framework endpoints:

- `POST /shopware/app/register` - App registration
- `POST /shopware/app/confirm` - Registration confirmation
- `POST /shopware/app/lifecycle/{event}` - Lifecycle events
- `POST /shopware/api/v1/webhook` - Webhook handling
- `GET /shopware/admin/{app}/**` - Admin extension serving

## Configuration

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

  Database Configuration

  The SDK uses Spring Boot's standard DataSource configuration with smart defaults:

  Development (Zero Configuration)

  - Automatically provides SQLite in-memory database
  - No configuration required

  Production (Standard Spring Boot)

  spring:
    datasource:
      url: jdbc:sqlite:shopware_apps.db
      driver-class-name: org.sqlite.JDBC
    jpa:
      database-platform: org.hibernate.community.dialect.SQLiteDialect

  Dual Liquibase Migration System

  SDK Migrations (Automatic):
  - Run automatically when application starts
  - Create core tables required by SDK (SHOPWARE_SHOP, etc.)
  - Use sdk-core context to avoid conflicts

  User Migrations (Optional):
  - Users can add their own migrations via spring.liquibase.change-log
  - Both use the same DataSource and transaction

  Important Files for Modifications

  - Controllers: src/main/java/de/codebarista/shopware/appbackend/sdk/controller/
  - Services: src/main/java/de/codebarista/shopware/appbackend/sdk/service/
  - Security: src/main/java/de/codebarista/shopware/appbackend/sdk/web/
  - Auto-Configuration: src/main/java/de/codebarista/shopware/appbackend/sdk/config/
  - Database Migrations: src/main/resources/db/changelog/

  Testing

  Tests are located in src/test/java/ and include:
  - Auto-configuration tests
  - Database integration tests
  - Security filter tests
  - Service layer tests
  - Integration test utilities

  Architectural Rules

  The SDK enforces clean architecture through:

  - Independence - Never imports app-specific packages
  - Module boundaries - Physical separation from business logic
  - Interface segregation - Clean API contracts
  - Compile-time checks - Gradle plugin prevents importing app-specific code

  Usage Example

  1. Add Dependency

  dependencies {
      implementation 'de.codebarista:shopware-app-backend-sdk:1.5.0'
  }

  2. Extend ShopwareApp

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

  3. Handle Events

  @EventListener
  public void handleOrderEvent(ShopwareEvent event) {
      if ("order.placed".equals(event.getEventName())) {
          // Process order placement
      }
  }

  Contributing

  When contributing to the SDK:

  1. Maintain independence - Never import app-specific packages
  2. Follow conventions - Use existing patterns and naming
  3. Add tests - Comprehensive test coverage required
  4. Update docs - Keep documentation current

  Publishing

  The SDK is designed to be published to Maven repositories:

  - GroupId: de.codebarista
  - ArtifactId: shopware-app-backend-sdk
  - Version: Semantic versioning (e.g., 1.5.0)

  License

  This SDK is designed to be open-sourced under MIT license.

  This CLAUDE.md file provides comprehensive guidance for working with the extracted SDK project, covering its architecture,
  configuration, usage patterns, and development workflows.
