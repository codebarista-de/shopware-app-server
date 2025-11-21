# Database Shop Reference Example

This example demonstrates how to create custom database tables with foreign key relationships to the App Server's
`SHOPWARE_SHOP` table using Liquibase migrations.

## Features Demonstrated

- ✅ **Foreign Key Relationships** - Reference the App Server's SHOPWARE_SHOP table
- ✅ **CASCADE Delete** - Automatic cleanup when shops are unregistered
- ✅ **Database Indexes** - Optimize queries with multi-column indexes
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
- Published `shopware-app-server` library to Maven Local

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
SELECT el.*, ss.SHOP_URL
FROM EVENT_LOG el
JOIN SHOPWARE_SHOP ss ON el.SHOP_ID = ss.ID;
```

## Installing in Shopware

To install this app in a Shopware instance and start logging events:

### 2. Package the App

The `manifest.xml` is already included with webhooks configured. Simply zip it:

```bash
mkdir MyEventLogApp
cp manifest.xml MyEventLogApp/
zip -r MyEventLogApp.zip MyEventLogApp
```

### 3. Install via Shopware Administration

1. Ensure the app server is running: `../../gradlew bootRun`
2. Go to Shopware Administration → Extensions → My Extensions
3. Upload `MyEventLogApp.zip`
4. Install and activate the app

The app will automatically:

- Register with your app server
- Create the database tables via Liquibase migrations
- Store the shop credentials in `SHOPWARE_SHOP` table
- Log the registration event in `EVENT_LOG` table
- Subscribe to configured webhooks (order, product, customer events)

### 4. Test Event Logging

Trigger events in Shopware:

**Create/edit an order:**

1. Go to Orders in Shopware Administration
2. Create or edit an order → `order.written` event is logged

**Create/edit a product:**

1. Go to Products
2. Create or edit a product → `product.written` event is logged

**Check the logs:**

```bash
sqlite3 shopware_app.db

-- View all events
SELECT * FROM EVENT_LOG ORDER BY RECEIVED_AT DESC;

-- View events with shop information
SELECT
    el.EVENT_NAME,
    el.RECEIVED_AT,
    ss.SHOP_URL
FROM EVENT_LOG el
JOIN SHOPWARE_SHOP ss ON el.SHOP_ID = ss.ID
ORDER BY el.RECEIVED_AT DESC;

-- Count events by type
SELECT EVENT_NAME, COUNT(*) as count
FROM EVENT_LOG
GROUP BY EVENT_NAME;
```

### 5. Configured Webhooks

The manifest includes these webhooks:

- `order.written` - Logged when orders are created/modified
- `order.placed` - Logged when orders are placed
- `product.written` - Logged when products are created/modified
- `customer.written` - Logged when customers are created/modified

All events are automatically logged in the `EVENT_LOG` table with the shop ID reference.

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

`src/main/resources/db/changelog/changesets/0001-add-event-log.xml`:

This migration creates:

```sql
CREATE TABLE EVENT_LOG (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    SHOP_ID BIGINT NOT NULL,
    EVENT_NAME VARCHAR(255) NOT NULL,
    RECEIVED_AT TIMESTAMP NOT NULL,
    FOREIGN KEY (SHOP_ID) REFERENCES SHOPWARE_SHOP(ID) ON DELETE CASCADE
);

CREATE INDEX IDX_EVENT_LOG_SHOP_EVENT
ON EVENT_LOG(SHOP_ID, EVENT_NAME, RECEIVED_AT);
```

### Foreign Key Details

```xml
<addForeignKeyConstraint
    baseTableName="EVENT_LOG"
    baseColumnNames="SHOP_ID"
    constraintName="FK_EVENT_LOG_SHOP"
    referencedTableName="SHOPWARE_SHOP"
    referencedColumnNames="ID"
    onDelete="CASCADE"/>
```

**Key features:**

- **Referential Integrity**: Cannot insert events for non-existent shops
- **CASCADE Delete**: When a shop is deleted, all its events are automatically removed
- **Named Constraint**: Easy to identify in database schema

### Index for Performance

```xml
<createIndex indexName="IDX_EVENT_LOG_SHOP_EVENT" tableName="EVENT_LOG">
    <column name="SHOP_ID"/>
    <column name="EVENT_NAME"/>
    <column name="RECEIVED_AT"/>
</createIndex>
```

This composite index optimizes:

- Queries filtering by shop
- Queries filtering by shop and event name
- Sorting by received timestamp

## JPA Entity with Foreign Key

We added the `@EnableJpaRepositories` and `@EntityScan` annotations to the main method for this example to work.
See the `AppBackendApplication` class.

### Entity Class

`src/main/java/com/appbackend/entity/EventLog.java`:

```java
@Entity
@Table(name = "EVENT_LOG")
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SHOP_ID", nullable = false)
    private Long shopId;  // References SHOPWARE_SHOP.ID

    @Column(name = "EVENT_NAME", nullable = false, length = 255)
    private String eventName;

    @Column(name = "RECEIVED_AT", nullable = false)
    private LocalDateTime receivedAt;

    // Constructors, getters, setters...
}
```

**Note:** We use `Long shopId` instead of a JPA `@ManyToOne` relationship because the `SHOPWARE_SHOP` entity is managed
internally by the App Server library.

### Repository Interface

`src/main/java/com/appbackend/repository/EventLogRepository.java`:

```java
@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    /**
     * Find all events for a specific shop, ordered by received time descending.
     */
    List<EventLog> findByShopIdOrderByReceivedAtDesc(Long shopId);

    /**
     * Find all events with a specific name for a shop.
     */
    List<EventLog> findByShopIdAndEventName(Long shopId, String eventName);
}
```

## Using the Repository

The `MyShopwareBackend` implementation demonstrates logging events:

```java
@Component
public class MyShopwareBackend implements ShopwareApp {

    private final EventLogRepository eventLogRepository;

    @Override
    public void onEvent(ShopwareEventDto event, long internalShopId,
                        @Nullable Locale userLocale, String shopwareLanguageId) {
        String eventName = event.data().event();

        // Log the Shopware event
        EventLog eventLog = new EventLog(
                internalShopId,
                eventName,
                LocalDateTime.now()
        );
        eventLogRepository.save(eventLog);

        // Query recent events for this shop
        List<EventLog> recentEvents = eventLogRepository
                .findByShopIdOrderByReceivedAtDesc(internalShopId);
        log.info("Total events for shop {}: {}", internalShopId, recentEvents.size());
    }
}
```

### CASCADE Delete Behavior

When a shop is deleted (via `onDeleteShop`), the foreign key's CASCADE delete automatically removes all event logs:

```java
@Override
public void onDeleteShop(@Nonnull String shopHost, @Nonnull String shopId,
                         long internalShopId) {
    log.info("Shop deleted: {}", shopHost);

    // All EventLog entries for this shop will be automatically deleted
    // due to ON DELETE CASCADE in the foreign key constraint
}
```

## Automated Tests

The example includes comprehensive tests for the EventLog repository:

### Test Class

`src/test/java/com/appbackend/repository/EventLogRepositoryTest.java`

Tests include:

1. **Save and retrieve** - Basic CRUD operations
2. **Order by time** - Verifies DESC ordering works correctly
3. **Filter by event name** - Tests custom query methods
4. **Shop isolation** - Ensures data is properly scoped per shop

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests EventLogRepositoryTest

# Run with detailed output
./gradlew test --info
```

All 4 tests should pass:

- ✅ `shouldSaveAndRetrieveEventLog()`
- ✅ `shouldFindEventsByShopIdOrderedByTime()`
- ✅ `shouldFindEventsByShopIdAndEventName()`
- ✅ `shouldOnlyReturnEventsForSpecificShop()`

## Querying Event Data

### Basic Queries

```sql
-- All events
SELECT * FROM EVENT_LOG;

-- Events for specific shop
SELECT * FROM EVENT_LOG WHERE SHOP_ID = 1;

-- Recent events (uses the index)
SELECT * FROM EVENT_LOG
WHERE SHOP_ID = 1
ORDER BY RECEIVED_AT DESC
LIMIT 10;
```

### Joining with Shop Data

```sql
-- Event log with shop information
SELECT
    el.ID,
    el.EVENT_NAME,
    el.RECEIVED_AT,
    ss.SHOP_URL,
    ss.SHOP_ID
FROM EVENT_LOG el
JOIN SHOPWARE_SHOP ss ON el.SHOP_ID = ss.ID
ORDER BY el.RECEIVED_AT DESC;
```

### Event Statistics

```sql
-- Count events per shop
SELECT
    ss.SHOP_URL,
    COUNT(el.ID) as event_count
FROM SHOPWARE_SHOP ss
LEFT JOIN EVENT_LOG el ON ss.ID = el.SHOP_ID
GROUP BY ss.ID, ss.SHOP_URL;

-- Most common events
SELECT
    EVENT_NAME,
    COUNT(*) as count
FROM EVENT_LOG
GROUP BY EVENT_NAME
ORDER BY count DESC;
```

## Production Considerations

### Database Selection

For production, use a more robust database:

**PostgreSQL:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shopware_app
    username: ${DB_USER}
    password: ${DB_PASSWORD}
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
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
```

### Security

- Never commit database credentials
- Use environment variables: `${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`
- Set `app-server.ssl-only: true`
- Set `app-server.map-localhost-ip-to-localhost-domain-name: false`

### Index Strategy

The composite index `(SHOP_ID, EVENT_NAME, RECEIVED_AT)` is optimized for:

```java
// Efficient: Uses the index
findByShopIdOrderByReceivedAtDesc(shopId)
findByShopIdAndEventName(shopId, eventName)

// Less efficient: Only uses first part of index
findByEventName(eventName)
```

Consider additional indexes if you need to query by `EVENT_NAME` alone.

### Data Retention

Implement a cleanup strategy for old events:

```java
@Scheduled(cron = "0 0 2 * * *")  // Run daily at 2 AM
public void cleanupOldEvents() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
    // Add a method to your repository:
    // eventLogRepository.deleteByReceivedAtBefore(cutoff);
}
```

## Extending the Example

### Add More Foreign Keys

Create additional tables that reference `SHOPWARE_SHOP`:

```xml
<changeSet author="Dev" id="create-shop-settings-table">
    <createTable tableName="SHOP_SETTINGS">
        <column name="ID" type="INTEGER" autoIncrement="true">
            <constraints primaryKey="true"/>
        </column>
        <column name="SHOP_ID" type="BIGINT">
            <constraints nullable="false" unique="true"/>
        </column>
        <column name="SETTING_KEY" type="VARCHAR(255)"/>
        <column name="SETTING_VALUE" type="TEXT"/>
    </createTable>

    <addForeignKeyConstraint
        baseTableName="SHOP_SETTINGS"
        baseColumnNames="SHOP_ID"
        constraintName="FK_SHOP_SETTINGS_SHOP"
        referencedTableName="SHOPWARE_SHOP"
        referencedColumnNames="ID"
        onDelete="CASCADE"/>
</changeSet>
```

### Add More Columns

Extend the EventLog table:

```xml
<changeSet author="Dev" id="add-event-payload">
    <addColumn tableName="EVENT_LOG">
        <column name="PAYLOAD" type="TEXT">
            <constraints nullable="true"/>
        </column>
    </addColumn>
</changeSet>
```

Then update your entity:

```java
@Column(name = "PAYLOAD", columnDefinition = "TEXT")
private String payload;  // Store event data as JSON
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

### CASCADE Not Working

**Error:** Events not deleted when shop is removed.

**Cause:** Foreign key not created or CASCADE not specified.

**Solution:** Check that `onDelete="CASCADE"` is in your foreign key constraint.

## Resources

- [Shopware App Server Documentation](../../README.md)
- [Liquibase Documentation](https://docs.liquibase.com/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)
- [Foreign Key Constraints](https://www.sqlite.org/foreignkeys.html)
