# Database Shop Reference Example

Demonstrates how to create custom tables with **foreign key relationships** to the App Server's `SHOPWARE_SHOP` table.

> **Prerequisite:** Understand [custom-db-migration](../custom-db-migration/) first. This example builds on those
> concepts.

## When to Use Foreign Keys to SHOPWARE_SHOP

- **Store shop-specific data** - Track information per registered shop
- **Ensure referential integrity** - Prevent orphaned records
- **Automatic cleanup** - Related data removed when shop unregisters

## Project Structure

```
db-shop-reference-example/
├── src/main/java/com/appbackend/
│   ├── entity/EventLog.java              # JPA entity with FK to SHOPWARE_SHOP
│   └── repository/EventLogRepository.java
├── src/main/resources/db/changelog/
│   └── changesets/0001-add-event-log.xml # Table with FK constraint
├── manifest.xml                          # Webhooks for event logging
└── build.gradle
```

## Quick Start

```bash
cd examples/db-shop-reference-example
./gradlew bootRun
```

The server will start on `http://localhost:8080`.

## Key Differences from custom-db-migration

### 1. Foreign Key in Migration

See [0001-add-event-log.xml](src/main/resources/db/changelog/changesets/0001-add-event-log.xml):

```xml

<column name="SHOP_ID" type="BIGINT">
    <constraints nullable="false"
                 foreignKeyName="fk_event_log_shop"
                 references="SHOPWARE_SHOP(ID)"/>
</column>
```

### 2. JPA Entity References Shop by ID

See [EventLog.java](src/main/java/com/appbackend/entity/EventLog.java):

```java

@Column(name = "SHOP_ID", nullable = false)
private Long shopId;  // References SHOPWARE_SHOP.ID
```

**Note:** We use `Long shopId` instead of `@ManyToOne` because `SHOPWARE_SHOP` is managed internally by the App Server.

### 3. Repository with Shop Queries

See [EventLogRepository.java](src/main/java/com/appbackend/repository/EventLogRepository.java):

```java
List<EventLog> findByShopIdOrderByReceivedAtDesc(Long shopId);
```

## Testing with Shopware

### Installing in Shopware

* Zip the `DatabaseExampleApp` directory
* Upload via Shopware Administration → Extensions → My Extensions → Upload Extension

### Test Event Logging

To trigger events in Shopware:

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
sqlite3 shopware_app.db "SELECT * FROM EVENT_LOG ORDER BY RECEIVED_AT DESC"
```

## Troubleshooting

| Problem                             | Solution                                                          |
|-------------------------------------|-------------------------------------------------------------------|
| `FOREIGN KEY constraint failed`     | Shop must exist before inserting events                           |
| `Table SHOPWARE_SHOP doesn't exist` | Include `app-server-changelog-master.xml` first in your changelog |

## Resources

- [custom-db-migration example](../custom-db-migration/) - Migration basics
- [Shopware App Server Documentation](../../README.md)
