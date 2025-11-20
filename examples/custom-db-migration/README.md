# Custom Database Migration Example

This example demonstrates how to add custom database tables to your Shopware app backend using Liquibase migrations with the Shopware App Server library.

## Features Demonstrated

- ✅ **Custom Database Tables** - Create your own tables alongside App Server tables
- ✅ **Liquibase Integration** - Manage schema changes with user-defined migrations
- ✅ **Migration Management** - Include required App Server migrations in your changelog
- ✅ **Database Persistence** - Custom tables coexist with App Server's SHOPWARE_SHOP table

## Project Structure

```
custom-db-migration/
├── src/main/java/com/appbackend/
│   ├── AppBackendApplication.java         # Spring Boot main class
│   └── MyShopwareBackend.java             # Minimal ShopwareApp implementation
├── src/main/resources/
│   ├── application.yaml                   # Configuration with user-migrations enabled
│   └── db/changelog/
│       ├── user-changelog-master.xml      # Master changelog
│       └── changesets/
│           └── 0001-add-my-table.xml      # Custom table creation
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

### 2. Run the App Server

```bash
cd examples/custom-db-migration
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
- **MY_CUSTOM_TABLE** - Your custom table

Inspect your custom table:

```sql
.schema MY_CUSTOM_TABLE
SELECT * FROM MY_CUSTOM_TABLE;
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
    <changeSet author="Shopware Dev" id="add my table">
        <createTable tableName="MY_CUSTOM_TABLE">
            <column name="ID" type="int">
                <constraints primaryKey="true" primaryKeyName="PK_ID"/>
            </column>
            <column name="MY_CUSTOM_COLUMN" type="text">
                <constraints/>
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
   - Then: Your custom migrations (creates `MY_CUSTOM_TABLE`)
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

## Adding More Custom Tables

### Create a New Changeset

1. Create a new changeset file:

```bash
touch src/main/resources/db/changelog/changesets/0002-add-another-table.xml
```

2. Add your table definition:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">
    <changeSet author="Your Name" id="add product sync state table">
        <createTable tableName="PRODUCT_SYNC_STATE">
            <column name="ID" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true" primaryKeyName="PK_PRODUCT_SYNC_STATE"/>
            </column>
            <column name="SHOP_ID" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="PRODUCT_ID" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="LAST_SYNC_AT" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <!-- Add foreign key to SHOPWARE_SHOP -->
        <addForeignKeyConstraint
            baseTableName="PRODUCT_SYNC_STATE"
            baseColumnNames="SHOP_ID"
            constraintName="FK_PRODUCT_SYNC_SHOP"
            referencedTableName="SHOPWARE_SHOP"
            referencedColumnNames="ID"/>
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

### Foreign Key to SHOPWARE_SHOP

Reference the App Server's `SHOPWARE_SHOP` table in your custom tables:

```xml
<addForeignKeyConstraint
    baseTableName="YOUR_TABLE"
    baseColumnNames="SHOP_ID"
    constraintName="FK_YOUR_TABLE_SHOP"
    referencedTableName="SHOPWARE_SHOP"
    referencedColumnNames="ID"
    onDelete="CASCADE"/>
```

This ensures data integrity when shops are unregistered.

## JPA Entity Example

Create JPA entities for your custom tables:

```java
package com.appbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "MY_CUSTOM_TABLE")
public class MyCustomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MY_CUSTOM_COLUMN")
    private String myCustomColumn;

    // Constructors, getters, setters
}
```

Create a Spring Data repository:

```java
package com.appbackend.repository;

import com.appbackend.entity.MyCustomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyCustomEntityRepository extends JpaRepository<MyCustomEntity, Integer> {
}
```

Use it in your app:

```java
@Component
public class MyShopwareBackend implements ShopwareApp {

    @Autowired
    private MyCustomEntityRepository repository;

    @Override
    public void onEvent(ShopwareEventDto event, long internalShopId,
                        @Nullable Locale userLocale, String shopwareLanguageId) {
        // Save custom data
        MyCustomEntity entity = new MyCustomEntity();
        entity.setMyCustomColumn("Data from event: " + event.data().event());
        repository.save(entity);
    }
}
```

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
