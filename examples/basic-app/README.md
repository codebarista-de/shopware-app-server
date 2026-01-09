# Basic Shopware App Example

A complete example demonstrating how to build a Shopware 6 app backend using the Shopware App Server library.

## Features Demonstrated

- **App Registration** - Automatic registration with Shopware shops
- **Webhook Handling** - Process events from Shopware (order.written, product.written)
- **Action Buttons** - Handle custom actions from Shopware Administration
- **Admin API Integration** - Send notifications to Shopware Administration

## Project Structure

```
basic-app/
├── src/main/java/com/example/shopwareapp/
│   ├── Application.java          # Spring Boot main class
│   └── MyShopwareApp.java        # ShopwareApp implementation
├── src/main/resources/
│   └── application.yml           # Configuration
├── manifest.xml                  # Shopware app manifest
└── build.gradle
```

## Quick Start

### 1. Run the App Server

```bash
cd examples/basic-app
./gradlew bootRun
```

The server starts on `http://localhost:8080`.

### 2. Install in Shopware

1. Zip the `MyShopwareApp` directory
2. Upload zip via Shopware Administration → Extensions → My Extensions → Upload Extension

The app will automatically:

- Register with your app server
- Exchange credentials
- Be ready to receive webhooks and handle actions

## Configuration

See [application.yml](src/main/resources/application.yml) for the development configuration.

The `application.yml` is configured for local development:

```yaml
app-server:
  ssl-only: false                                  # Allow HTTP
  map-localhost-ip-to-localhost-domain-name: true  # Enable localhost routing
```

For production, set `ssl-only: true` and update `MyShopwareApp/manifest.xml` URLs to use HTTPS.

## Code Overview

See [MyShopwareApp.java](src/main/java/com/example/shopwareapp/MyShopwareApp.java) for the complete implementation
showing:

### App configuration

**Important configuration:**

```java

@Override
public String getAppKey() {
    return "my-app";  // Must match subdomain
}

@Override
public String getAppSecret() {
    return "my-app-secret";  // Matches <secret> from manifest.xml
}

@Override
public String getAppName() {
    return "MyShopwareApp"; // Matches <name> from manifest.xml
}
```

**Webhook Handling:**

See the `onEvent` method.

**Action Button Handling:**

See the `onAction` method.

## Testing

**Trigger webhooks:** Create or modify an order/product in Shopware Administration.

**Test action buttons:**

1. Go to Orders → Select an order → Detail view
2. Click "Process Order" in the action menu ("...")
3. Check for the success notification

## Troubleshooting

| Problem                | Solution                                                                      |
|------------------------|-------------------------------------------------------------------------------|
| Registration fails     | Check app server is running, verify `/etc/hosts` entry for `my-app.localhost` |
| Webhooks not received  | Verify webhook URLs in manifest.xml, check Shopware logs in `var/log/`        |
| Action buttons missing | Ensure app is activated, clear Shopware cache: `bin/console cache:clear`      |

## Resources

- [Shopware App Server Documentation](../../README.md)
- [Shopware App Documentation](https://developer.shopware.com/docs/concepts/extensions/apps-concept.html)
