# TODO - Improvements for Shopware App Server

This document contains recommended improvements to make before publishing the App Server to Maven Central.

## Critical for Public Release

### 1. CI/CD Pipeline
- [x] **Create GitHub Actions workflows**
  - ✅ `ci.yaml` - Build and test on push/PR (with test result publishing)
  - ✅ `publish-to-maven-central.yaml` - Automated publish with tagging and GitHub releases
  - [ ] `codeql.yml` - Security analysis (optional, nice-to-have)
  - **Status**: .github/workflows/ directory created with complete CI/CD pipeline
  - ~~**Blocker**: Cannot ensure build quality without automated testing~~ **COMPLETE!**

### 2. Version History and Tracking
- [x] **Create CHANGELOG.md**
  - Track version history and breaking changes
  - Follow [Keep a Changelog](https://keepachangelog.com/) format
  - Essential for users to understand what changed between versions
  - **Blocker**: Required for professional open source projects

### 3. Example Project
- [ ] **Create a working example application**
  - The README mentions "Getting Started Guide TODO" (line 39)
  - Need a demo app showing:
    - Basic ShopwareApp implementation
    - Webhook handling example
    - Action button handling example
    - Admin API usage examples
  - Place in `examples/` or separate repository
  - **Blocker**: Users need to see how to use the library

### 4. Versioning Strategy
- [x] **Define and document versioning policy**
  - ✅ Created comprehensive VERSIONING.md with semantic versioning 2.0.0 commitment
  - ✅ Documented when to release 1.0.0 (production-ready, stable API, >80% test coverage)
  - ✅ Defined pre-release strategy (alpha, beta, RC, SNAPSHOT)
  - ✅ Documented version support policy and deprecation process
  - ✅ Included release process and breaking change migration guidelines
  - Currently at `0.0.1` - version bump happens before public release

### 5. Code TODOs in Source Files
- [ ] **AdminApi.java:66** - Expose `postObject()` method for binary data (PDFs, etc.)
- [ ] **AdminApi.java:68** - Expose `getObject()` method for generic GET requests
- [x] **AppRegistrationController.java:83** - ~~Create error response template~~ (Removed - current implementation is clean and follows Spring best practices)
- [ ] **ShopManagementService.java:87** - Add cleanup job for shops marked as deleted
- [x] **ByteArrayServletInputStream.java:9** - Move to appropriate package (not config)

### 6. Build Configuration
- [x] **Create settings.gradle**
  - Define `rootProject.name = 'shopware-app-server'`
  - Currently missing, which can cause issues in multi-project builds

## High Priority (Strongly Recommended Before Publishing)

### 7. Community Guidelines
- [ ] **Create CONTRIBUTING.md**
  - How to contribute code
  - Code style guidelines
  - Testing requirements
  - PR process and expectations
  - Development environment setup

### 8. Release Process Documentation
- [ ] **Document release workflow**
  - How to cut a release
  - GPG key setup for signing
  - Sonatype credentials configuration
  - Version bumping process
  - Post-release checklist

### 9. GitHub Repository Setup
- [ ] **Create issue templates**
  - Bug report template
  - Feature request template
  - Question template
- [ ] **Create PR template**
  - Checklist for contributors
  - Link to CONTRIBUTING.md
- [ ] **Configure branch protection**
  - Require PR reviews
  - Require status checks to pass
  - Protect main branch from force pushes

### 10. README Enhancements
- [ ] **Add badges**
  - Build status badge (GitHub Actions)
  - Maven Central version badge
  - Test coverage badge
  - License badge
- [ ] **Improve Installation section**
  - Add complete Gradle dependency snippet
  - Add Maven dependency snippet
  - Specify minimum Java version clearly
- [ ] **Add Quick Start code example**
  - Replace "Getting Started Guide TODO" with actual working example
  - Show minimal app implementation inline

### 11. Configuration Validation
- [ ] **Add validation for AppServerProperties**
  - Validate required configuration at startup
  - Provide clear error messages for misconfigurations
  - Consider using @Validated and @ConfigurationProperties validation

### 12. Standardize Exception Handling
- [ ] **Create consistent exception handling patterns**
  - Document exception handling strategy for library users
  - Ensure all exceptions extend ShopwareAppException
  - Add exception hierarchy diagram to docs

## Medium Priority (Recommended for Production Readiness)

### 13. Documentation Improvements
- [ ] **Complete JavaDoc for all public APIs**
  - All public methods should have complete JavaDoc (mostly done)
  - Include usage examples where helpful
  - Document thread-safety guarantees

- [ ] **Add security best practices guide**
  - Document signature verification process
  - Explain token lifecycle and security
  - Provide deployment security recommendations
  - Secret management best practices

- [ ] **Add troubleshooting guide**
  - Common configuration issues and solutions
  - Debugging signature verification problems
  - Performance tuning recommendations
  - FAQ section

### 14. Logging Improvements
- [ ] **Add structured logging with correlation IDs**
  - Add request correlation IDs for tracing
  - Use structured logging format (JSON) for production environments
  - Make correlation ID accessible to user code

- [ ] **Reduce sensitive information in logs**
  - Review all log statements for potential security leaks
  - Mask or exclude sensitive data (tokens, secrets, signatures)
  - Add logging security guidelines to docs

### 15. Testing Enhancements

**Current Test Coverage**: 17 test files, good basic coverage

**Test Gaps to Address**:

- [ ] **Add integration tests for error scenarios** *(Priority 1)*
  - Test all domain-specific exceptions (InvalidSignatureException, InvalidTokenException, etc.)
  - Test `isTokenExpired` method edge cases
  - Invalid signature verification with tampered data
  - Expired token handling in authentication flow
  - Malformed request bodies (invalid JSON, missing fields)
  - Network failure simulation in AdminApi
  - Database connection failures and transaction rollbacks

- [ ] **Add security-focused test cases** *(Priority 2)*
  - Signature verification edge cases:
    - Different algorithms, timing attack resistance
    - Very long signatures/payloads
    - Unicode and special character handling
  - Token validation boundary conditions:
    - Tokens exactly at TTL boundary
    - Tokens with future/negative timestamps
    - Token format validation
  - Authentication/authorization flows:
    - Shop registration with mismatched hosts
    - Registration confirmation with wrong credentials
    - Replay attack prevention
    - SQL injection resistance
  - Thread-safety verification for concurrent operations

- [ ] **Add performance/load tests** *(Priority 3)*
  - High concurrent request handling
  - Memory usage under load
  - Signature verification performance benchmarks
  - Database performance under concurrent access

## Low Priority (Nice to Have)

### 16. Community and Governance
- [ ] **Create CODE_OF_CONDUCT.md**
  - Define community standards
  - Specify enforcement procedures

- [ ] **Create SECURITY.md**
  - Security policy and vulnerability reporting process
  - Supported versions
  - Security update timeline commitments

### 17. Automated Maintenance
- [ ] **Set up automated dependency updates**
  - Configure Dependabot or Renovate
  - Auto-merge minor/patch updates with passing tests

- [ ] **Add dependency vulnerability scanning**
  - OWASP dependency check
  - GitHub security advisories
  - Regular dependency audit schedule

### 18. Enhanced Publishing
- [ ] **Set up GitHub Releases**
  - Automated release notes generation
  - Link to Maven Central artifacts
  - Include migration guides for breaking changes

- [ ] **Publish JavaDoc**
  - Host on javadoc.io or GitHub Pages
  - Link from README
  - Update with each release

- [ ] **Set up code coverage reporting**
  - Integrate Codecov or Coveralls
  - Add coverage badge to README
  - Fail builds on coverage decrease

### 19. Advanced Examples
- [ ] **Create multi-module example project**
  - Show how to use in larger applications
  - Demonstrate testing strategies
  - Show integration with other Spring Boot features

- [ ] **Create Docker example**
  - Containerized demo application
  - docker-compose.yml for local testing with Shopware
  - Production-ready Dockerfile example

- [ ] **Create deployment guides**
  - Cloud deployment examples (AWS, GCP, Azure)
  - Kubernetes deployment manifests
  - Performance tuning for production

### 20. Build Quality Tools
- [ ] **Add static analysis tools**
  - SpotBugs for bug detection
  - Checkstyle for code formatting consistency
  - PMD for code quality rules
  - Configure in Gradle build

- [ ] **Add mutation testing**
  - PIT mutation testing for test quality
  - Ensure tests actually verify behavior

## Completed Items ✅

### Security Enhancements
- [x] Fix error message in SignatureService (SHA3-256 → SHA-256)
- [x] Replace generic RuntimeException with specific exceptions
- [x] Create domain-specific exception hierarchy

### Code Quality
- [x] Resolve SignatureService TODO comments
- [x] Make ShopwareSignatureVerificationFilter a proper bean
- [x] Add @NonNull/@Nullable annotations to public APIs
- [x] Move ByteArrayServletInputStream to util package (proper separation of concerns)
- [x] Remove unnecessary TODO from AppRegistrationController (buildConfirmationUrl already uses Spring best practices)

### Performance
- [x] Fix MessageDigest thread-safety issue (implemented ThreadLocal)
- [x] ObjectMapper reuse (injected shared bean)

### API Improvements
- [x] Extract timestamp validation logic (created isTokenExpired method)

### Documentation
- [x] Add comprehensive JavaDoc to all library interfaces
- [x] Document ShopwareApp interface methods
- [x] Document AdminApi methods
- [x] Document TokenService
- [x] Document DTOs and utility classes
- [x] Improve README.md structure and clarity
- [x] Create CHANGELOG.md following Keep a Changelog format
- [x] Create VERSIONING.md with comprehensive semantic versioning policy
  - Semantic versioning 2.0.0 commitment
  - Pre-release strategy (alpha, beta, RC)
  - Version support and EOL policy
  - Release process documentation
  - Breaking change and deprecation guidelines
  - When to release 1.0.0 criteria

### Build & Configuration
- [x] Create settings.gradle file with proper rootProject.name

### CI/CD
- [x] Create GitHub Actions CI workflow (ci.yaml) for automated build and test on push/PR
  - Builds with Java 17 on Ubuntu
  - Runs full test suite with Gradle caching
  - Uploads test results, reports, and coverage as artifacts
  - Publishes test results to PR checks
- [x] Create GitHub Actions publish workflow (publish-to-maven-central.yaml)
  - Manual trigger via workflow_dispatch
  - Gradle dependency caching for faster builds
  - Version extraction from build.gradle
  - Tag existence validation (prevents duplicate releases)
  - SNAPSHOT version validation (blocks accidental snapshot publishes)
  - Runs tests before publishing
  - Publishes to Maven Central staging area
  - Creates git tags with "v" prefix (e.g., v0.0.1)
  - Auto-generates GitHub Releases with installation instructions
  - Includes manual release reminder for Maven Central Portal

## Estimated Effort

- **Critical items (1-6)**: 3-5 days
- **High Priority items (7-12)**: 2-3 days
- **Medium Priority items (13-15)**: 3-4 days
- **Low Priority items (16-20)**: 5-7 days (ongoing)

**Minimum for Public Release**: Complete all Critical items (~1 week)

**Recommended for 1.0.0 Release**: Critical + High Priority (~2 weeks)
