# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2026-02-24

### Added

- Shop secret rotation: existing shop secret remains active during re-registration until the new registration is confirmed
- Shop signature verification on re-registration: once a shop re-registers with a valid shop signature, all future re-registrations for that shop require a valid shop signature

### Changed

- Shop deletion is now a hard delete (row removed from database) instead of a soft delete (timestamp marker)

## [1.0.1] - 2026-01-09

### Added

- allow security filter chain that targets all endpoints

## [1.0.0] - 2025-11-22

### Added

- App registration and lifecycle management with multi-app support
- Request/response signature verification using HMAC-SHA256
- OAuth token management with automatic expiration handling
- WebClient-based Admin API client with search support
- SQLite persistence with zero-config in-memory default
- Liquibase database migrations
- Spring Boot auto-configuration

[1.1.0]: https://github.com/codebarista-de/shopware-app-server/releases/tag/v1.1.0
[1.0.1]: https://github.com/codebarista-de/shopware-app-server/releases/tag/v1.0.1
[1.0.0]: https://github.com/codebarista-de/shopware-app-server/releases/tag/v1.0.0
