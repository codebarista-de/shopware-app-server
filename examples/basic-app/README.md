# Basic Shopware App Example

This example demonstrates how to build a Shopware 6 app backend using the Shopware App Server library.

## Features Demonstrated

- ✅ **App Registration** - Automatic registration with Shopware shops
- ✅ **Webhook Handling** - Process events from Shopware (order.written, product.written)
- ✅ **Action Buttons** - Handle custom actions from Shopware Administration
- ✅ **Admin API Integration** - Send notifications to Shopware Administration
- ✅ **Database Persistence** - Shop credentials and tokens stored in SQLite

## Project Structure

```
basic-app/
├── src/main/java/com/example/shopwareapp/
│   ├── Application.java           # Spring Boot main class
│   └── MyShopwareApp.java         # ShopwareApp implementation
├── src/main/resources/
│   └── application.yml            # Configuration
├── manifest.xml                   # Shopware app manifest
└── build.gradle                   # Dependencies
```

## Prerequisites

- Java 17 or later
- Gradle 8+
- Local Shopware 6 installation (for testing)

## Quick Start

### 1. Run the App Server

```bash
cd examples/basic-app
../../gradlew bootRun
```

The server will start on `http://localhost:8080`.

### 2. Install the App in Shopware

#### Install the App

1. Copy `manifest.xml` to a directory named `MyShopwareApp`
2. Zip the `MyShopwareApp` directory
3. Upload via Shopware Administration → Extensions → My Extensions → Upload Extension

The app will automatically:

- Register with your app server
- Exchange credentials
- Be ready to receive webhooks and handle actions

## Configuration

### Development Mode

The `application.yml` is configured for local development:

```yaml
app-server:
  ssl-only: false  # Allow HTTP for local testing
  map-localhost-ip-to-localhost-domain-name: true  # Enable localhost routing
  http-request-response-logging-enabled: true  # Debug logging
```

### Production Mode

For production deployment:

```yaml
app-server:
  ssl-only: true  # Require HTTPS
  map-localhost-ip-to-localhost-domain-name: false
  http-request-response-logging-enabled: false

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shopware_apps  # Use production database
```

Update `manifest.xml` URLs:

- Change `http://my-app.localhost:8080` to `https://my-app.yourdomain.com`
- **Remove** the `<secret>` tag

## Code Overview

### MyShopwareApp.java

The main implementation showing:

**Important configuration:**

```java

@Override
public String getAppKey() {
    return "my-app";  // Must match subdomain
}

@Override
public String getAppSecret() {
    return "my-app-secret";  // Matches manifest.xml
}
```

**Webhook Handling:**

See the `onEvent` method.

**Action Button Handling:**

See the `onAction` method.

**Admin API Usage:**

```java
// Send notifications
adminApi.pushSuccessMessage(this,shopId, "Order processed successfully!");
adminApi.

pushErrorMessage(this,shopId, "Failed to process order: "+e.getMessage());
        adminApi.

pushInfoMessage(this,shopId, "Product sync completed for "+productIds.size() +" products");

// Search entities (example - commented out in code)
// var searchQuery = new SearchQuery();
// searchQuery.setIds(List.of(productId));
// var result = adminApi.postSearch(this, shopId, searchQuery, "product",
//                                  ProductSearchResult.class, shopwareLanguageId);
```

## Testing

### Trigger Webhooks

Create or modify an order in Shopware Administration to trigger the `order.written` webhook.

Check the logs:

```
Received event 'order.written' from shop ...
Order written: {...}
```

### Test Action Buttons

1. Go to Orders → Select an order → Detail view
2. Click "Process Order" button (appears in the action menu "...")
3. Check for the success notification in Shopware Administration
4. Check the app server logs for the action handling output

## Database

The app uses SQLite by default. The database file `shopware_app.db` contains:

- **SHOPWARE_SHOP** table - Registered shop credentials and OAuth tokens

To inspect:

```bash
sqlite3 shopware_app.db
.tables
SELECT * FROM SHOPWARE_SHOP;
```

## Troubleshooting

### App Registration Fails

- Check app server is running on `http://my-app.localhost:8080`
- Verify `/etc/hosts` entry for `my-app.localhost`
- Check logs for signature verification errors

### Webhooks Not Received

- Verify webhook URLs in `manifest.xml` match your app server
- Check Shopware logs: `var/log/`
- Verify app has required permissions

### Action Buttons Not Appearing

- Ensure app is installed and activated
- Check entity and view match in `manifest.xml`
- Clear Shopware cache: `bin/console cache:clear`

## Resources

- [Shopware App Server Documentation](../../README.md)
- [Shopware App Documentation](https://developer.shopware.com/docs/concepts/extensions/apps-concept.html)
- [Admin API Reference](https://shopware.stoplight.io/docs/admin-api)
