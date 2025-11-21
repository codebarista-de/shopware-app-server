# Custom Database Migration Example

This example demonstrates how to add custom database tables to your Shopware app backend using Liquibase migrations with
the Shopware App Server library.

## Features Demonstrated

- ✅ **Custom Database Tables** - Create your own tables alongside App Server tables
- ✅ **Liquibase Integration** - Manage schema changes with user-defined migrations
- ✅ **Migration Management** - Include required App Server migrations in your changelog
- ✅ **Database Persistence** - Custom tables coexist with App Server's SHOPWARE_SHOP table
- ✅ **JPA Entities & Repositories** - Complete Spring Data JPA integration

> **Note:** For an example with foreign keys to SHOPWARE_SHOP,
> see [db-shop-reference-example](../db-shop-reference-example/)

## Project Structure

```
custom-db-migration/
├── src/main/java/com/appbackend/
│   ├── AppBackendApplication.java         # Spring Boot main class
│   ├── MyShopwareBackend.java             # ShopwareApp implementation
│   ├── entity/
│   │   └── SystemMessage.java             # JPA entity for system message
│   └── repository/
│       └── SystemMessageRepository.java   # Spring Data repository
├── src/main/resources/
│   ├── application.yaml                   # Configuration with user-migrations enabled
│   └── db/changelog/
│       ├── user-changelog-master.xml      # Master changelog
│       └── changesets/
│           └── 0001-add-my-table.xml      # System message table
├── src/test/java/com/appbackend/
│   └── repository/
│       └── SystemMessageRepositoryTest.java  # Repository tests
└── build.gradle                           # Dependencies
```

## When to Use This Approach

Use custom database migrations when you need to:

- Store application-specific data beyond shop registrations
- Create custom entities (products sync state, user preferences, etc.)
- Maintain your own data model alongside the App Server framework

## Prerequisites

- Java 17 or later
- Gradle 8+
- Published `shopware-app-server` library to Maven Local

## Quick Start

### 1. Build and Test

```bash
cd examples/custom-db-migration
../../gradlew build
```

This will compile the code and run the included tests.

### 2. Run the App Server

```bash
../../gradlew bootRun
```

The server will start on `http://localhost:8080` and automatically run migrations.

### 3. Verify Database Tables

The app creates a SQLite database with both App Server and custom tables:

```bash
sqlite3 shopware_app.db
.tables
```

You should see:

- **DATABASECHANGELOG** - Liquibase migration tracking
- **DATABASECHANGELOGLOCK** - Liquibase lock management
- **SHOPWARE_SHOP** - App Server's shop registration table
- **SYSTEM_MESSAGE** - Your custom table

Inspect your custom table:

```sql
.schema SYSTEM_MESSAGE
SELECT * FROM SYSTEM_MESSAGE;
```

## Configuration

### Enable User Migrations

The key configuration in `application.yaml`:

```yaml
app-server:
  database:
    user-migrations: true  # Enable custom Liquibase migrations
```

When `user-migrations: true`:

- App Server **does not** automatically create its SpringLiquibase bean
- **You must** define your own Liquibase configuration
- **You must** include the App Server migrations in your changelog

### Liquibase Configuration

Configure your changelog master file:

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/user-changelog-master.xml
```

### Database Configuration

Standard Spring Boot DataSource configuration:

```yaml
spring:
  datasource:
    url: jdbc:sqlite:shopware_app.db
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
      ddl-auto: none  # Let Liquibase handle schema
```

## Migration Files

### Master Changelog

`src/main/resources/db/changelog/user-changelog-master.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <!-- REQUIRED: Include App Server migrations first -->
    <include file="db/changelog/app-server-changelog-master.xml"/>

    <!-- Your custom migrations -->
    <include file="changesets/0001-add-my-table.xml" relativeToChangelogFile="true"/>

</databaseChangeLog>
```

**Important:** Always include `db/changelog/app-server-changelog-master.xml` **first** to ensure App Server tables are
created before your custom migrations run.

See `src/main/resources/db/changelog/changesets/0001-add-my-table.xml` for the changeset.

## How It Works

### Migration Flow

1. **App starts** with `app-server.database.user-migrations: true`
2. **App Server** does not create its own SpringLiquibase bean
3. **Spring Boot** creates SpringLiquibase bean from `spring.liquibase.change-log` configuration
4. **Liquibase runs** the master changelog:
    - First: App Server migrations (creates `SHOPWARE_SHOP` table)
    - Then: Your custom migrations (creates `SYSTEM_MESSAGE` table)
5. **Application ready** with all tables available

### App Server Migrations Context

The App Server migrations use the `app-server-core` Liquibase context.

## Adding More Custom Tables

### Create a New Changeset

1. Create a new changeset file:

```bash
touch src/main/resources/db/changelog/changesets/0002-add-another-table.xml
```

2. Add your table definition:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>
<changeSet author="Your Name" id="create-user-preferences-table">
...
</changeSet>
        </databaseChangeLog>
```

3. Include it in your master changelog:

```xml

<databaseChangeLog ...>
<include file="db/changelog/app-server-changelog-master.xml"/>
<include file="changesets/0001-add-my-table.xml" relativeToChangelogFile="true"/>
<include file="changesets/0002-add-another-table.xml" relativeToChangelogFile="true"/>
        </databaseChangeLog>
```

**Tip:** For tables that need to reference `SHOPWARE_SHOP`, see the *
*[db-shop-reference-example](../db-shop-reference-example/)** for a complete working example with foreign keys, indexes,
and cascade deletion.

## JPA Entity Example

We added the `@EnableJpaRepositories` and `@EntityScan` annotations to the main method for this example to work.
See the `AppBackendApplication` class.

This example includes a complete JPA entity implementation for the `SYSTEM_MESSAGE` table.

See `src/main/java/com/appbackend/entity/SystemMessage.java` for the entity class
and `src/main/java/com/appbackend/repository/SystemMessageRepository.java` for the Repository interface.

The example also includes a test class: `src/test/java/com/appbackend/repository/SystemMessageRepositoryTest.java`.

To run the tests:

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests SystemMessageRepositoryTest

# Run with detailed output
./gradlew test --info
```

## Migration Best Practices

1. **Never modify existing changesets** - Liquibase tracks them by ID
2. **Use meaningful changeset IDs** - Makes tracking easier
3. **Add rollback instructions** - Support migration reversals
4. **Test migrations** - Use a development database first
5. **Version your changesets** - Number them sequentially

## Troubleshooting

### Tables Not Created

- Check logs for Liquibase errors
- Verify `app-server.database.user-migrations: true` is set
- Ensure `spring.liquibase.change-log` points to your master file
- Confirm App Server migrations are included first

### App Server Table Missing

Make sure your master changelog includes:

```xml

<include file="db/changelog/app-server-changelog-master.xml"/>
```

This **must** be the first include in your changelog.

### Migration Runs Twice

Liquibase tracks executed migrations in `DATABASECHANGELOG`. If you need to reset:

```sql
-- CAREFUL: This removes migration history
DROP TABLE DATABASECHANGELOG;
DROP TABLE DATABASECHANGELOGLOCK;
```

Then restart the application.

### Foreign Key Constraint Errors

Ensure the App Server migrations run **before** your migrations that reference `SHOPWARE_SHOP`.

## Resources

- [Shopware App Server Documentation](../../README.md)
- [Liquibase Documentation](https://docs.liquibase.com/)
- [Spring Boot Liquibase Integration](https://docs.spring.io/spring-boot/reference/data/sql.html#data.sql.migration.liquibase)
- [Liquibase Changelog Reference](https://docs.liquibase.com/change-types/home.html)
