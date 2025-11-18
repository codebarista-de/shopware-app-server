# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

For detailed information about our versioning policy, release process, and upgrade guidelines, see [VERSIONING.md](VERSIONING.md).

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [0.0.1] - 2025-11-18

### Added

#### Core Features
- **App Registration & Lifecycle Management**
  - Automatic Shopware app registration flow via `POST /shopware/api/v1/registration/register`
  - Registration confirmation handling via `POST /shopware/api/v1/registration/confirm`
  - App lifecycle event handling via `POST /shopware/api/v1/lifecycle/{event}`
  - Multi-app support with subdomain-based app key routing

- **Authentication & Security**
  - Shopware request signature verification using HMAC-SHA256
  - Response signing for secure communication with Shopware
  - Thread-safe signature verification with ThreadLocal MessageDigest
  - Token-based authentication for Admin API requests
  - OAuth token lifecycle management with expiration tracking

- **Admin API Integration**
  - Pre-configured WebClient-based Admin API client
  - Support for Admin API CRUD operations (GET, POST, PATCH, DELETE)
  - Automatic authentication token injection
  - Flexible sync and async HTTP request methods
  - Search endpoint support with Criteria API integration

- **Data Persistence**
  - SQLite database support with automatic configuration
  - Zero-config in-memory database for development
  - Shop registration and credential storage
  - Token management with automatic expiration handling
  - Liquibase database migrations for schema management

- **Auto-Configuration**
  - Spring Boot auto-configuration for zero-config startup
  - Modular configuration classes:
    - `AppServerCoreAutoConfiguration` - Core settings and SSL enforcement
    - `AppServerWebSecurityConfiguration` - Security filter chain configuration
    - `AppServerLiquibaseAutoConfiguration` - Database migration setup
  - Development-friendly defaults with production-ready overrides

- **Framework Components**
  - `ShopwareApp` interface for implementing app backends
  - `AppRegistrationController` for handling registration flow
  - `ShopManagementService` for shop lifecycle management
  - `TokenService` for OAuth token management
  - `SignatureService` for request/response signing

#### Developer Experience
- Comprehensive JavaDoc documentation for all public APIs
- Null-safety annotations (@NonNull, @Nullable) on public methods
- Domain-specific exception hierarchy extending `ShopwareAppException`
- Clear error messages and validation feedback

#### Testing Infrastructure
- Integration test utilities for app backend testing
- Auto-configuration tests for Spring Boot setup
- Security filter chain tests
- Service layer unit tests
- Database integration tests

### Security
- Implemented HMAC-SHA256 signature verification for all Shopware requests
- Thread-safe cryptographic operations using ThreadLocal pattern
- Secure token storage and lifecycle management
- Request signature validation to prevent tampering
- Response signing for integrity verification

### Documentation
- Comprehensive README with architecture overview
- Installation and configuration guide
- Usage examples and code snippets
- JavaDoc for all public APIs
- CLAUDE.md for AI-assisted development guidance

### Dependencies
- Spring Boot 3.4.1 (Web, Security, Data JPA, WebFlux)
- SQLite JDBC 3.45.2.0
- Hibernate Community Dialects 6.4.4.Final
- Liquibase Core 4.29.0
- Jackson Databind Nullable 0.2.6
- jnanoid 2.0.0 for ID generation

### Fixed
- Corrected error message in SignatureService (SHA3-256 → SHA-256)
- Replaced generic RuntimeException with domain-specific exceptions
- Resolved MessageDigest thread-safety issue with ThreadLocal implementation
- Fixed ObjectMapper reuse by using injected shared bean

[Unreleased]: https://github.com/codebarista-de/shopware-app-server/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/codebarista-de/shopware-app-server/releases/tag/v0.0.1
