# Custom Database Migration Example

This example demonstrates how to add custom database tables to your Shopware app backend using Liquibase migrations with the Shopware App Server library.

## Features Demonstrated

- ✅ **Custom Database Tables** - Create your own tables alongside App Server tables
- ✅ **Liquibase Integration** - Manage schema changes with user-defined migrations
- ✅ **Migration Management** - Include required App Server migrations in your changelog
- ✅ **Database Persistence** - Custom tables coexist with App Server's SHOPWARE_SHOP table
- ✅ **Foreign Key Relationships** - Second migration (optional) demonstrates FK to SHOPWARE_SHOP
- ✅ **JPA Entities & Repositories** - Complete Spring Data JPA integration
- ✅ **Automated Tests** - Unit tests for repository methods

## Project Structure

```
custom-db-migration/
├── src/main/java/com/appbackend/
│   ├── AppBackendApplication.java         # Spring Boot main class
│   ├── MyShopwareBackend.java             # ShopwareApp implementation
│   ├── entity/
│   │   ├── SystemMessage.java           # JPA entity for system message
│   │   └── EventLog.java                  # JPA entity for event log (optional)
│   └── repository/
│       ├── SystemMessageRepository.java # Spring Data repository
│       └── EventLogRepository.java        # Repository for events (optional)
├── src/main/resources/
│   ├── application.yaml                   # Configuration with user-migrations enabled
│   └── db/changelog/
│       ├── user-changelog-master.xml      # Master changelog
│       └── changesets/
│           ├── 0001-add-my-table.xml      # Maintenance info table
│           └── 0002-add-event-log.xml     # Event log with FK (commented out)
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
- Track historical data or logs in dedicated tables

## Prerequisites

- Java 17 or later
- Gradle 8+
- Published `shopware-app-server` library to Maven Local

## Quick Start

### 1. Publish the Library Locally

First, publish the Shopware App Server library to your local Maven repository:

```bash
cd ../..  # Go to library root
./gradlew publishToMavenLocal
```

### 2. Build and Test

```bash
cd examples/custom-db-migration
../../gradlew build
```

This will compile the code and run the included tests.

### 3. Run the App Server

```bash
../../gradlew bootRun
```

The server will start on `http://localhost:8080` and automatically run migrations.

### 4. Verify Database Tables

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

**Important:** Always include `db/changelog/app-server-changelog-master.xml` **first** to ensure App Server tables are created before your custom migrations run.

### Custom Changeset

`src/main/resources/db/changelog/changesets/0001-add-my-table.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">
    <changeSet author="Shopware Dev" id="create-maintenance-info-table">
        <createTable tableName="SYSTEM_MESSAGE">
            <column name="ID" type="INTEGER" autoIncrement="true">
                <constraints primaryKey="true" primaryKeyName="PK_SYSTEM_MESSAGE"/>
            </column>
            <column name="MESSAGE" type="TEXT">
                <constraints nullable="false"/>
            </column>
            <column name="CREATOR" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="ACTIVE" type="BOOLEAN" defaultValueBoolean="false">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

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

The App Server migrations use the `app-server-core` Liquibase context. This allows you to:

- Run App Server migrations in all contexts (they have no context restriction)
- Optionally use contexts in your own migrations
- Control which migrations run in different environments

Example with context in your changeset:

```xml
<changeSet author="Shopware Dev" id="add my table" context="production">
    <!-- This only runs in production context -->
</changeSet>
```

## Second Migration: Event Log with Foreign Key

This example includes a second migration (commented out by default) that demonstrates:
- Creating a table with a foreign key to `SHOPWARE_SHOP`
- Adding indexes for query optimization
- CASCADE delete behavior

### Enabling the Event Log Migration

To enable the event log table, uncomment it in the master changelog:

**File**: `src/main/resources/db/changelog/user-changelog-master.xml`

```xml
<!-- Uncomment to enable the event log table with foreign key to SHOPWARE_SHOP -->
<!--
<include file="changesets/0002-add-event-log.xml" relativeToChangelogFile="true"/>
-->
```

Change to:

```xml
<!-- Event log table with foreign key to SHOPWARE_SHOP -->
<include file="changesets/0002-add-event-log.xml" relativeToChangelogFile="true"/>
```

### Event Log Table Structure

**File**: `src/main/resources/db/changelog/changesets/0002-add-event-log.xml`

The migration creates:

```sql
CREATE TABLE EVENT_LOG (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    SHOP_ID BIGINT NOT NULL,
    EVENT_NAME VARCHAR(255) NOT NULL,
    RECEIVED_AT TIMESTAMP NOT NULL,
    FOREIGN KEY (SHOP_ID) REFERENCES SHOPWARE_SHOP(ID) ON DELETE CASCADE
);
CREATE INDEX IDX_EVENT_LOG_SHOP_EVENT ON EVENT_LOG(SHOP_ID, EVENT_NAME, RECEIVED_AT);
```

### Using the Event Log

After uncommenting the migration:

1. **Restart the app** to run the migration
2. **Use the repository** in your code:

```java
@Component
public class MyShopwareBackend implements ShopwareApp {

    private final EventLogRepository eventLogRepository;

    @Override
    public void onEvent(ShopwareEventDto event, long internalShopId,
                        @Nullable Locale userLocale, String shopwareLanguageId) {
        // Log the event
        EventLog log = new EventLog(
                internalShopId,
                event.data().event(),
                LocalDateTime.now()
        );
        eventLogRepository.save(log);

        // Query events for this shop
        List<EventLog> recentEvents = eventLogRepository
                .findByShopIdOrderByReceivedAtDesc(internalShopId);
    }
}
```

3. **Query the data**:

```bash
sqlite3 shopware_app.db
SELECT * FROM EVENT_LOG;
SELECT el.*, ss.SHOP_URL
FROM EVENT_LOG el
JOIN SHOPWARE_SHOP ss ON el.SHOP_ID = ss.ID;
```

### Why the Foreign Key?

The foreign key ensures:
- **Referential integrity** - Cannot log events for non-existent shops
- **Cascade deletion** - When a shop is deleted, its events are automatically removed
- **Query performance** - The index speeds up queries filtering by shop

## Adding More Custom Tables

### Create a New Changeset

1. Create a new changeset file:

```bash
touch src/main/resources/db/changelog/changesets/0003-add-another-table.xml
```

2. Add your table definition (see `0002-add-event-log.xml` as an example)

3. Include it in your master changelog:

```xml
<databaseChangeLog ...>
    <include file="db/changelog/app-server-changelog-master.xml"/>
    <include file="changesets/0001-add-my-table.xml" relativeToChangelogFile="true"/>
    <include file="changesets/0002-add-event-log.xml" relativeToChangelogFile="true"/>
    <include file="changesets/0003-add-another-table.xml" relativeToChangelogFile="true"/>
</databaseChangeLog>
```

**Tip:** For tables that reference `SHOPWARE_SHOP`, see the Event Log migration (0002) as a complete working example including foreign keys, indexes, and cascade deletion.

## JPA Entity Example

This example includes a complete JPA entity implementation for the `SYSTEM_MESSAGE` table.

### Entity Class

`src/main/java/com/appbackend/entity/SystemMessage.java`:

```java
package com.appbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SYSTEM_MESSAGE")
public class SystemMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "MESSAGE", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "CREATOR", nullable = false, length = 255)
    private String creator;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = false;

    // Constructors
    public SystemMessage() {
    }

    public SystemMessage(String message, String creator, Boolean active) {
        this.message = message;
        this.creator = creator;
        this.active = active;
    }

    // Getters and setters...
}
```

### Repository Interface

`src/main/java/com/appbackend/repository/SystemMessageRepository.java`:

```java
package com.appbackend.repository;

import com.appbackend.entity.SystemMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemMessageRepository extends JpaRepository<SystemMessage, Long> {

    /**
     * Find the currently active maintenance message, if any.
     */
    Optional<SystemMessage> findFirstByActiveTrue();
}
```

### Using the Repository

In `MyShopwareBackend.java`, the repository is used to save system message when a shop registers:

```java
@Component
public class MyShopwareBackend implements ShopwareApp {

    @Autowired
    private SystemMessageRepository maintenanceInfoRepository;

    @Override
    public void onRegisterShop(@Nonnull String shopHost, @Nonnull String shopId,
                               long internalShopId) {
        log.info("Shop registered: {} (ID: {})", shopHost, shopId);

        // Create a system message entry
        SystemMessage info = new SystemMessage(
                "System is operational. Shop " + shopHost + " successfully registered.",
                "System",
                false
        );
        maintenanceInfoRepository.save(info);
        log.info("Saved system message: {}", info);
    }
}
```

### Testing the Repository

After running the app and registering a shop, check the database:

```bash
sqlite3 shopware_app.db
SELECT * FROM SYSTEM_MESSAGE;
```

You should see entries created when shops register.

## Automated Tests

The example includes a test class demonstrating how to test JPA repositories.

### Test Class

`src/test/java/com/appbackend/repository/SystemMessageRepositoryTest.java`:

This test class demonstrates:

1. **Save and retrieve** - Testing basic CRUD operations
2. **Custom queries** - Testing the `findFirstByActiveTrue()` method
3. **Empty results** - Testing query behavior when no data matches

Key annotations used:

```java
@DataJpaTest  // Configures JPA test slice
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // Use existing SQLite config
class SystemMessageRepositoryTest {
    // Tests...
}
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests SystemMessageRepositoryTest

# Run with detailed output
./gradlew test --info
```

### Test Results

After running tests, view the report:

```bash
# Open in browser (Linux)
xdg-open build/reports/tests/test/index.html

# Or check the command line output
./gradlew test
```

All 3 tests should pass:
- ✅ `shouldSaveAndRetrieveSystemMessage()`
- ✅ `shouldFindActiveSystemMessage()`
- ✅ `shouldReturnEmptyWhenNoActiveSystemMessage()`

## Production Considerations

### Database Selection

For production, consider more robust databases:

**PostgreSQL:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shopware_app
    username: ${DB_USER}
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

**MySQL:**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shopware_app
    username: ${DB_USER}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
```

### Security

- Never commit database credentials
- Use environment variables: `${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`
- Set `app-server.ssl-only: true`
- Set `app-server.map-localhost-ip-to-localhost-domain-name: false`

### Migration Best Practices

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

## Next Steps

Extend this example:

1. **Add JPA entities** - Map your tables to Java objects
2. **Create repositories** - Use Spring Data JPA for data access
3. **Implement business logic** - Store and retrieve custom data in event handlers
4. **Add indexes** - Optimize queries with Liquibase `<createIndex>`
5. **Migration rollbacks** - Define `<rollback>` for each changeset

## Resources

- [Shopware App Server Documentation](../../README.md)
- [Liquibase Documentation](https://docs.liquibase.com/)
- [Spring Boot Liquibase Integration](https://docs.spring.io/spring-boot/reference/data/sql.html#data.sql.migration.liquibase)
- [Liquibase Changelog Reference](https://docs.liquibase.com/change-types/home.html)
