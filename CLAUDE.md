This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

### App Server
- **Build**: `./gradlew build` - Builds the App Server library JAR
- **Run tests**: `./gradlew test`
- **Publish locally**: `./gradlew publishToMavenLocal` - Publishes to local Maven repository for testing
- **Publish**: `./gradlew publish` - Publishes to configured repository

## Architecture Overview

This is the **Shopware App Server** - a Java Spring Boot library for building Shopware 6 app backends that integrate with the
Shopware App System.

### Core Functionality

The App Server provides the essential framework components needed to build Shopware app backends:

1. **App Registration & Lifecycle** - Automatic registration, confirmation, and lifecycle event handling
2. **Authentication & Security** - Shopware signature verification and OAuth token management
3. **API Integration** - Pre-configured clients for Shopware Admin API
4. **Event Handling** - Webhook processing and event dispatching
5. **Database Integration** - Shop registration persistence and token storage

### Package Structure

- `de.codebarista.shopware.appserver.api` - API DTOs and contracts
- `de.codebarista.shopware.appserver.controller` - Framework controllers
- `de.codebarista.shopware.appserver.service` - Core services
- `de.codebarista.shopware.appserver.web` - Security and web configuration
- `de.codebarista.shopware.appserver.repository` - Data access layer
- `de.codebarista.shopware.appserver.util` - Utility classes
- `de.codebarista.shopware.appserver.config` - Auto-configuration classes

### Auto-Configuration Architecture

The App Server uses modular auto-configuration:

- **`AppServerCoreAutoConfiguration`** - General App Server settings (SSL enforcement, localhost mapping)
- **`AppServerHttpAutoConfiguration`** - HTTP client configuration (RestTemplate, logging, error handling)
- **`AppServerDatabaseAutoConfiguration`** - Database defaults (SQLite in-memory fallback)
- **`AppServerLiquibaseAutoConfiguration`** - App Server core migrations (SHOPWARE_SHOP table, etc.)

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

The App Server automatically provides these framework endpoints:

- `POST /shopware/app/register` - App registration
- `POST /shopware/app/confirm` - Registration confirmation
- `POST /shopware/app/lifecycle/{event}` - Lifecycle events
- `POST /shopware/api/v1/webhook` - Webhook handling
- `GET /shopware/admin/{app}/**` - Admin extension serving

## Configuration

### App Server Properties

  Database Configuration

  The App Server uses Spring Boot's standard DataSource configuration with smart defaults:

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

  App Server Migrations (Automatic):
  - Run automatically when application starts
  - Create core tables required by App Server (SHOPWARE_SHOP, etc.)
  - Use sdk-core context to avoid conflicts

  User Migrations (Optional):
  - Users can add their own migrations via spring.liquibase.change-log
  - Both use the same DataSource and transaction

  Important Files for Modifications

  - Controllers: src/main/java/de/codebarista/shopware/appserver/controller/
  - Services: src/main/java/de/codebarista/shopware/appserver/service/
  - Security: src/main/java/de/codebarista/shopware/appserver/config/
  - Auto-Configuration: src/main/java/de/codebarista/shopware/appserver/config/
  - Database Migrations: src/main/resources/db/changelog/

  Testing

  Tests are located in src/test/java/ and include:
  - Auto-configuration tests
  - Database integration tests
  - Security filter tests
  - Service layer tests
  - Integration test utilities

  Architectural Rules

  The App Server enforces clean architecture through:

  - Independence - Never imports app-specific packages
  - Module boundaries - Physical separation from business logic
  - Interface segregation - Clean API contracts
  - Compile-time checks - Gradle plugin prevents importing app-specific code

  Usage Example

  1. Add Dependency

  dependencies {
      implementation 'de.codebarista:shopware-app-server:X.Y.Z'
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

  When contributing to the App Server:

  1. Maintain independence - Never import app-specific packages
  2. Follow conventions - Use existing patterns and naming
  3. Add tests - Comprehensive test coverage required
  4. Update docs - Keep documentation current

  Publishing

  The App Server is designed to be published to Maven repositories:

  - GroupId: de.codebarista
  - ArtifactId: shopware-app-server
  - Version: Semantic versioning (e.g., 1.5.0)

  License

  This App Server is designed to be open-sourced under MIT license.

  This CLAUDE.md file provides comprehensive guidance for working with the extracted App Server project, covering its architecture,
  configuration, usage patterns, and development workflows.
