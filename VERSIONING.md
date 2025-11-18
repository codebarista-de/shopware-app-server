# Versioning Policy

This project follows [Semantic Versioning 2.0.0](https://semver.org/).

## Version Format

`MAJOR.MINOR.PATCH[-PRERELEASE]`

**We increment:**

- **MAJOR** - Breaking changes (incompatible API changes)
- **MINOR** - New features (backward compatible)
- **PATCH** - Bug fixes (backward compatible)

## Breaking Changes

Changes that break existing code:

- Removing/renaming public classes, interfaces, or methods
- Changing method signatures
- Changing configuration properties
- Upgrading Spring Boot major version
- Changing minimum Java version

## Pre-Release Versions

- **`X.Y.Z-alpha.N`** - Early testing, API unstable
- **`X.Y.Z-beta.N`** - Feature-complete, API stabilizing
- **`X.Y.Z-rc.N`** - Release candidate, production-ready testing
- **`X.Y.Z-SNAPSHOT`** - Development only, never published to Maven Central

## Release Process

1. **Update version** in `build.gradle` (remove `-SNAPSHOT`)
2. **Update CHANGELOG.md** (move `[Unreleased]` items to new version)
3. **Commit changes**: `git commit -m "Release version X.Y.Z"`
4. **Push**: `git push`
5. **Trigger GitHub Actions** workflow `publish-to-maven-central.yaml`
6. **Release in Maven Central Portal**
7. **Update to next SNAPSHOT**: `version = 'X.Y.Z+1-SNAPSHOT'`

The workflow automatically:

- Validates version (no SNAPSHOT, tag doesn't exist)
- Runs tests
- Publishes to Maven Central staging
- Creates git tag (`vX.Y.Z`)
- Creates GitHub Release

## Deprecation

```java
/**
 * @deprecated Since 1.5.0, use {@link #newMethod()} instead.
 * Will be removed in 2.0.0.
 */
@Deprecated(since = "1.5.0", forRemoval = true)
public void oldMethod() {
}
```

- Keep deprecated APIs for at least one MINOR version
- Document in CHANGELOG.md under `### Deprecated`
- Remove in next MAJOR version
