# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Added

- Shop secret rotation: existing shop secret remains active during re-registration until the new registration is confirmed
- Shop signature verification on re-registration: once a shop re-registers with a valid shop signature, all future re-registrations for that shop require a valid shop signature

## [1.0.0] - 2025-11-22

### Added

- App registration and lifecycle management with multi-app support
- Request/response signature verification using HMAC-SHA256
- OAuth token management with automatic expiration handling
- WebClient-based Admin API client with search support
- SQLite persistence with zero-config in-memory default
- Liquibase database migrations
- Spring Boot auto-configuration

[Unreleased]: https://github.com/codebarista-de/shopware-app-server/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/codebarista-de/shopware-app-server/releases/tag/v1.0.0
