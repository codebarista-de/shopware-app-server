# Custom Database Migration Example

Demonstrates how to add custom database tables alongside App Server tables using Liquibase migrations.

> For tables with foreign keys to `SHOPWARE_SHOP`, see [db-shop-reference-example](../db-shop-reference-example/).

## Features Demonstrated

- **Custom Database Tables** - Create your own tables alongside App Server tables
- **Liquibase Integration** - Manage schema changes with user-defined migrations
- **JPA Entities & Repositories** - Complete Spring Data JPA integration

## Project Structure

```
custom-db-migration/
├── src/main/java/com/appbackend/
│   ├── AppBackendApplication.java
│   ├── MyShopwareBackend.java
│   ├── entity/SystemMessage.java
│   └── repository/SystemMessageRepository.java
├── src/main/resources/
│   ├── application.yaml
│   └── db/changelog/
│       ├── user-changelog-master.xml
│       └── changesets/0001-add-my-table.xml
└── build.gradle
```

## Quick Start

```bash
cd examples/custom-db-migration
./gradlew bootRun
```

Verify tables were created:

```bash
sqlite3 shopware_app.db ".tables"
# Shows: DATABASECHANGELOG, DATABASECHANGELOGLOCK, SHOPWARE_SHOP, SYSTEM_MESSAGE
```

## How It Works

### 1. Enable User Migrations

In `application.yaml`:

```yaml
app-server:
  database:
    user-migrations: true
```

This tells App Server **not** to create its own Liquibase bean, so you can define your own.

### 2. Create Master Changelog

In `src/main/resources/db/changelog/user-changelog-master.xml`:

```xml
<databaseChangeLog ...>
    <!-- REQUIRED: Include App Server migrations first -->
    <include file="db/changelog/app-server-changelog-master.xml"/>

    <!-- Your custom migrations -->
    <include file="changesets/0001-add-my-table.xml" relativeToChangelogFile="true"/>
</databaseChangeLog>
```

**Important:** Always include `app-server-changelog-master.xml` **first**.

### 3. Configure Spring Liquibase

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/user-changelog-master.xml
```

### 4. Add JPA Annotations

Add `@EnableJpaRepositories` and `@EntityScan` to your main class.
See [AppBackendApplication.java](src/main/java/com/appbackend/AppBackendApplication.java).

## Adding More Tables

1. Create a new changeset: `changesets/0002-add-another-table.xml`
2. Include it in your master changelog after the first changeset

## Migration Best Practices

- **Never modify existing changesets** - Liquibase tracks them by ID
- **Number changesets sequentially** - Makes tracking easier
- **Add rollback instructions** - Support migration reversals

## Troubleshooting

| Problem               | Solution                                                   |
|-----------------------|------------------------------------------------------------|
| Tables not created    | Verify `app-server.database.user-migrations: true` is set  |
| SHOPWARE_SHOP missing | Ensure `app-server-changelog-master.xml` is included first |
| Migration runs twice  | Check `DATABASECHANGELOG` table for duplicates             |

## Resources

- [Shopware App Server - Database Migrations](../../README.md#database-migrations)
- [Liquibase Documentation](https://docs.liquibase.com/)
