# Database Shop Reference Example

This example demonstrates how to create custom database tables with foreign key relationships to the App Server's
`SHOPWARE_SHOP` table using Liquibase migrations.

## Features Demonstrated

- ✅ **Foreign Key Relationships** - Reference the App Server's SHOPWARE_SHOP table
- ✅ **JPA Entities & Repositories** - Complete Spring Data JPA integration
- ✅ **Event Logging** - Track events per shop with referential integrity

## Project Structure

```
db-shop-reference-example/
├── src/main/java/com/appbackend/
│   ├── AppBackendApplication.java         # Spring Boot main class
│   ├── MyShopwareBackend.java             # ShopwareApp implementation with event logging
│   ├── entity/
│   │   └── EventLog.java                  # JPA entity with FK to SHOPWARE_SHOP
│   └── repository/
│       └── EventLogRepository.java        # Spring Data repository with custom queries
├── src/main/resources/
│   ├── application.yaml                   # Configuration with user-migrations enabled
│   └── db/changelog/
│       ├── user-changelog-master.xml      # Master changelog
│       └── changesets/
│           └── 0001-add-event-log.xml     # Event log table with FK
├── src/test/java/com/appbackend/
│   └── repository/
│       └── EventLogRepositoryTest.java    # Repository tests
├── src/test/resources/
│   └── application-test.yaml              # Test configuration
├── manifest.xml                           # Shopware app manifest with webhooks
└── build.gradle                           # Dependencies
```

## When to Use This Approach

Use foreign keys to `SHOPWARE_SHOP` when you need to:

- **Store shop-specific data** - Track information per registered shop
- **Ensure referential integrity** - Prevent orphaned data
- **Automatic cleanup** - Remove related data when a shop unregisters
- **Query across tables** - JOIN custom data with shop information

## Prerequisites

- Java 17 or later
- Gradle 8+

## Quick Start

### 1. Build and Test

```bash
cd examples/db-shop-reference-example
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
- **EVENT_LOG** - Your custom table with FK to SHOPWARE_SHOP

Inspect the event log table:

```sql
.schema EVENT_LOG
SELECT * FROM EVENT_LOG;

-- Query with JOIN
SELECT el.*, ss.SHOP_HOST
FROM EVENT_LOG el
JOIN SHOPWARE_SHOP ss ON el.SHOP_ID = ss.ID;
```

## Installing in Shopware

To install this app in a Shopware instance and start logging events:

### 1. Package the App

Copy the `manifest.xml` to a folder named `DatabaseExampleApp` and zip the folder.

### 2. Install via Shopware Administration

1. Ensure the app server is running: `../../gradlew bootRun`
2. Go to Shopware Administration → Extensions → My Extensions
3. Upload the zip file
4. Install and activate the app

The app will automatically:

- Register with your app server
- Create the database tables via Liquibase migrations
- Store the shop credentials in `SHOPWARE_SHOP` table
- Log the registration event in `EVENT_LOG` table
- Subscribe to configured webhooks (order, product, customer events)

### 3. Test Event Logging

Trigger events in Shopware:

**Create/edit an order:**

1. Go to Orders in Shopware Administration
2. Create or edit an order → `order.written` event is logged

**Create/edit a product:**

1. Go to Products
2. Create or edit a product → `product.written` event is logged

**Create/edit a customer:**

1. Go to Customers
2. Create or edit a customer → `customer.written` event is logged

**Check the logs:**

```bash
sqlite3 shopware_app.db

-- View all events
SELECT * FROM EVENT_LOG ORDER BY RECEIVED_AT DESC;

-- View events with shop information
SELECT
    el.EVENT_NAME,
    el.RECEIVED_AT,
    ss.SHOP_HOST
FROM EVENT_LOG el
JOIN SHOPWARE_SHOP ss ON el.SHOP_ID = ss.ID
ORDER BY el.RECEIVED_AT DESC;

-- Count events by type
SELECT EVENT_NAME, COUNT(*) as count
FROM EVENT_LOG
GROUP BY EVENT_NAME;
```

## Configuration

This example uses the same configuration pattern as the basic custom-db-migration example:

```yaml
app-server:
  database:
    user-migrations: true  # Enable custom Liquibase migrations
```

## Migration Details

### Master Changelog

`src/main/resources/db/changelog/user-changelog-master.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>

        <!-- Required: Include App Server migrations first -->
<include file="db/changelog/app-server-changelog-master.xml"/>

        <!-- Event log table with foreign key to SHOPWARE_SHOP -->
<include file="changesets/0001-add-event-log.xml" relativeToChangelogFile="true"/>

        </databaseChangeLog>
```

**Critical:** The App Server migrations must run first to create the `SHOPWARE_SHOP` table that our foreign key
references.

### Event Log Migration

See `src/main/resources/db/changelog/changesets/0001-add-event-log.xml`.

**Key features:**

- **Referential Integrity**: Cannot insert events for non-existent shops
- **Named Constraint**: Easy to identify in database schema

## JPA Entity with Foreign Key

We added the `@EnableJpaRepositories` and `@EntityScan` annotations to the main method for this example to work.
See the `AppBackendApplication` class.

For the entity class see `src/main/java/com/appbackend/entity/EventLog.java`.

**Note:** We use `Long shopId` instead of a JPA `@ManyToOne` relationship because the `SHOPWARE_SHOP` entity is managed
internally by the App Server library.

For the repository interface see `src/main/java/com/appbackend/repository/EventLogRepository.java`.

The `onEvent` methof of the `MyShopwareBackend` implementation demonstrates logging events.

## Automated Tests

The example includes comprehensive tests for the EventLog repository.
See `src/test/java/com/appbackend/repository/EventLogRepositoryTest.java`.

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests EventLogRepositoryTest

# Run with detailed output
./gradlew test --info
```

## Troubleshooting

### Foreign Key Constraint Errors

**Error:** `FOREIGN KEY constraint failed`

**Cause:** Trying to insert an event for a shop that doesn't exist.

**Solution:** Ensure the shop is registered before logging events.

### Migration Order Issues

**Error:** `Table SHOPWARE_SHOP doesn't exist`

**Cause:** App Server migrations not running before custom migrations.

**Solution:** Verify `app-server-changelog-master.xml` is included first in your master changelog.

## Resources

- [Shopware App Server Documentation](../../README.md)
- [Liquibase Documentation](https://docs.liquibase.com/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)
- [Foreign Key Constraints](https://www.sqlite.org/foreignkeys.html)
