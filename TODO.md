# TODO - Improvements for Shopware App Server

This document contains recommended improvements to make before publishing the App Server to Maven Central.

## High Priority (Must Fix Before Publishing)

### 1. Security Enhancements

- [x] **Fix error message in SignatureService** (`SignatureService.java:33`) ✅
  - ~~Current: "Could not initialize hash function SHA3-256"~~
  - ~~Should be: "Could not initialize hash function SHA-256"~~
  - ~~Algorithm is correct, just the error message is wrong~~
  - **COMPLETED**: Error message now correctly shows "SHA-256"

- [x] **Replace generic RuntimeException with specific exceptions** (`TokenServiceImpl.java:29-31`) ✅
  - ~~Create `InvalidTokenDataException` or similar domain-specific exception~~
  - ~~Improves error handling and API clarity~~
  - **COMPLETED**: Created domain-specific exception hierarchy with `ShopwareAppException` base class

### 2. Code Quality Issues

- [x] **Resolve SignatureService TODO comments** (`SignatureService.java:16,24`) ✅
  - ~~Decide: Convert to static helper methods OR keep as service with shared ObjectMapper~~
  - ~~If keeping as service: Inject shared ObjectMapper bean instead of creating new instance~~
  - ~~If converting to static: Remove @Component annotation and update usage~~
  - **COMPLETED**: Kept as service with injected shared ObjectMapper and thread-safe MessageDigest

- [x] **Make ShopwareSignatureVerificationFilter a proper bean** (`ShopwareSignatureVerificationFilter.java:28`) ✅
  - ~~Remove TODO comment~~
  - ~~Add proper auto-configuration~~
  - ~~Consider making constructor public and adding @Component~~
  - **COMPLETED**: Added @Component annotation, public constructor, and proper DI

### 3. Nullability Annotations

- [x] **Add @NonNull/@Nullable annotations to public APIs** ✅
  - ~~`ShopwareApp` interface methods (lines 20, 26, 32, 44, 85)~~
  - ~~Service method parameters throughout codebase~~
  - ~~Improves IDE support and documentation clarity~~
  - **COMPLETED**: Standardized Jakarta annotations across all public APIs

### 4. Error Handling Improvements

- [x] **Replace all generic RuntimeException with domain-specific exceptions** ✅
  - ~~`SignatureService.java:39, 46, 53, 61, 87`~~
  - ~~`TokenServiceImpl.java:30`~~
  - ~~Create exception hierarchy: `ShopwareAppException` -> specific exceptions~~
  - ~~Add proper error context and messages~~
  - **COMPLETED**: Created comprehensive exception hierarchy with specific exception types

## Medium Priority (Recommended Before Publishing)

### 5. Performance Optimizations

- [x] **Fix MessageDigest thread-safety issue** (`SignatureService.java:25`) ✅
  - ~~MessageDigest is not thread-safe but used as instance field~~
  - ~~Options:~~
    - ~~Use ThreadLocal<MessageDigest>~~
    - ~~Create new MessageDigest instances per operation~~
    - ~~Synchronize access~~
  - **COMPLETED**: Implemented ThreadLocal<MessageDigest> for thread-safe operation

- [x] **ObjectMapper reuse** (`ShopwareSignatureVerificationFilter.java:43`) ✅
  - ~~Inject shared ObjectMapper instead of creating new instance~~
  - ~~Better performance and consistency with rest of application~~
  - **COMPLETED**: Injected shared ObjectMapper bean in both SignatureService and ShopwareSignatureVerificationFilter

### 6. Configuration Validation

- [ ] **Add validation for AppServerProperties**
  - Validate required configuration at startup
  - Provide clear error messages for misconfigurations
  - Consider using @Validated and @ConfigurationProperties validation

- [ ] **Add configuration constraints documentation**
  - Document valid ranges/values for configuration properties
  - Add examples for common deployment scenarios

### 7. Logging Improvements

- [ ] **Add structured logging with correlation IDs**
  - Add request correlation IDs for tracing
  - Use structured logging format (JSON) for production environments

- [ ] **Reduce sensitive information in logs**
  - Review all log statements for potential security leaks
  - Mask or exclude sensitive data (tokens, secrets, signatures)

### 8. API Consistency

- [x] **Extract timestamp validation logic** (`TokenServiceImpl.java:76-78`) ✅
  - ~~Create separate `isTokenExpired(String token)` method~~
  - ~~Improves readability and testability~~
  - **COMPLETED**: Created separate `isTokenExpired` method with improved logic and error handling

- [ ] **Standardize exception handling patterns**
  - Create consistent exception handling across all services
  - Document exception handling strategy for users

## Low Priority (Nice to Have)

### 9. Documentation Improvements

- [ ] **Add comprehensive JavaDoc to ShopwareApp interface**
  - Document all method parameters and return values
  - Include usage examples and best practices
  - Document security considerations

- [ ] **Add security best practices guide**
  - Document signature verification process
  - Explain token lifecycle and security
  - Provide deployment security recommendations

- [ ] **Add troubleshooting guide**
  - Common configuration issues
  - Debugging signature verification problems
  - Performance tuning recommendations

### 10. Testing Enhancements

**Current Test Coverage Analysis** *(Completed 2025-09-25)*:

**✅ Already Covered:**
- Basic unit tests: `SignatureServiceTest`, `TokenServiceTest`
- Integration tests: `AppRegistrationApiTest`, `LifecycleEventApiTest`, `AdminExtensionApiTest`
- Configuration tests: `SqliteConfigurationTest`
- Test infrastructure: Good test utilities (`TestAppA/B`, `WebServerTest`, etc.)

**❌ Major Gaps Identified:**

- [ ] **Add integration tests for error scenarios** *(Priority 1 - Most Important)*
  - Test new domain-specific exceptions (`InvalidSignatureException`, `InvalidTokenException`, etc.)
  - Test `isTokenExpired` method edge cases (exactly expired, invalid format, too short tokens)
  - Invalid signature verification with tampered data
  - Expired token handling in authentication flow
  - Malformed request bodies (invalid JSON, missing fields)
  - Network failure simulation in `AdminApiService` and `ShopwareAccessTokenClientService`
  - Database connection failures and transaction rollbacks
  - Test signature verification with null/empty inputs
  - Test ObjectMapper serialization failures

- [ ] **Add security-focused test cases** *(Priority 2 - Critical for App Server)*
  - Signature verification edge cases:
    - Signature with different algorithms
    - Timing attack resistance (constant-time comparison)
    - Very long signatures/payloads
    - Unicode and special character handling
  - Token validation boundary conditions:
    - Tokens exactly at TTL boundary
    - Tokens with future timestamps
    - Tokens with negative timestamps
    - Token format validation (length, character set)
  - Authentication/authorization flows:
    - Shop registration with mismatched hosts
    - Registration confirmation with wrong credentials
    - Authentication bypass attempts
    - Replay attack prevention
    - SQL injection resistance in shop lookups
  - Thread-safety verification for concurrent signature operations

- [ ] **Add performance/load tests** *(Priority 3 - Nice to Have)*
  - High concurrent request handling:
    - Multiple shops registering simultaneously
    - Concurrent token validation requests
    - ThreadLocal MessageDigest performance under load
  - Memory usage under load:
    - Memory leak detection in long-running tests
    - ObjectMapper reuse efficiency
    - Shop entity caching behavior
  - Signature verification performance:
    - Benchmark signature calculation vs verification
    - Large payload signature handling
    - Thread contention in signature service
  - Database performance:
    - Concurrent shop lookups
    - Connection pool exhaustion scenarios

### 11. Build and Release Improvements

- [ ] **Add code quality checks to build**
  - SpotBugs or similar static analysis
  - Checkstyle for code formatting consistency
  - PMD for code quality rules

- [ ] **Add dependency vulnerability scanning**
  - OWASP dependency check
  - Regular dependency updates
  - Security advisory monitoring

- [ ] **Improve version management**
  - Change version from `0.0.1` to `1.0.0` for first release
  - Document versioning strategy (semantic versioning)
  - Add release notes template

## Implementation Notes

### Exception Hierarchy Suggestion

```java
public class ShopwareAppException extends RuntimeException {
    // Base exception for all App Server-related errors
}

public class InvalidSignatureException extends ShopwareAppException {
    // Signature verification failures
}

public class InvalidTokenException extends ShopwareAppException {
    // Token validation failures
}

public class ShopConfigurationException extends ShopwareAppException {
    // Shop setup/configuration issues
}
```

### Thread-Safe SignatureService Approach

```java
// Option 1: ThreadLocal (recommended)
private static final ThreadLocal<MessageDigest> HASH_DIGEST =
    ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    });

// Option 2: Create per operation
public String hash(String data) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        // ... rest of implementation
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("SHA-256 not available", e);
    }
}
```

## Priority Assessment

- **High Priority**: Must be fixed before publishing to ensure security and code quality
- **Medium Priority**: Should be addressed for production readiness and performance
- **Low Priority**: Enhances user experience and maintainability but not blocking

Total estimated effort: 2-3 days for High + Medium priority items.